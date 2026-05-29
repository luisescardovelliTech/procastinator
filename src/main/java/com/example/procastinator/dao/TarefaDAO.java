package com.example.procastinator.dao;

import com.example.procastinator.model.Categoria;
import com.example.procastinator.model.Historico;
import com.example.procastinator.model.Recompensa;
import com.example.procastinator.model.StatusTarefa;
import com.example.procastinator.model.Tarefa;
import com.example.procastinator.model.Usuario;
import com.example.procastinator.model.Xingamento;
import com.example.procastinator.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class TarefaDAO implements GenericDAO<Tarefa, Integer> {

    @Override
    public void salvar(Tarefa obj) {
        throw new UnsupportedOperationException("Use salvar(Tarefa, Integer usuarioId)");
    }

    public void salvar(Tarefa obj, Integer usuarioId) {
        if (usuarioId == null) {
            throw new IllegalArgumentException("usuarioId");
        }
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();
            obj.setCategoria(resolveCategoria(session, obj.getCategoria()));
            obj.setUsuario(session.getReference(Usuario.class, usuarioId));
            List<Xingamento> xingamentos = resolveXingamentos(session, obj.getXingamentos());
            if (xingamentos.isEmpty()) {
                xingamentos = carregarXingamentosPadrao(session, usuarioId);
            }
            obj.setXingamentos(xingamentos);
            session.persist(obj);
            registrarHistorico(session, obj, "CRIACAO", usuarioId);
            tx.commit();
        }
    }

    @Override
    public Tarefa buscarPorId(Integer id) {
        throw new UnsupportedOperationException("Use buscarPorId(Integer, Integer)");
    }

    public Tarefa buscarPorId(Integer id, Integer usuarioId) {
        if (id == null || usuarioId == null) {
            return null;
        }
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery(
                            "select distinct t from Tarefa t " +
                                    "left join fetch t.categoria " +
                                    "left join fetch t.xingamentos " +
                                    "where t.id = :id and t.usuario.id = :usuarioId", Tarefa.class)
                    .setParameter("id", id)
                    .setParameter("usuarioId", usuarioId)
                    .uniqueResult();
        }
    }

    @Override
    public List<Tarefa> listarTodos() {
        throw new UnsupportedOperationException("Use listarPorUsuario(Integer)");
    }

    public List<Tarefa> listarPorUsuario(Integer usuarioId) {
        if (usuarioId == null) {
            return List.of();
        }
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery(
                            "select distinct t from Tarefa t " +
                                    "left join fetch t.categoria " +
                                    "left join fetch t.xingamentos " +
                                    "where t.usuario.id = :usuarioId " +
                                    "order by t.id desc", Tarefa.class)
                    .setParameter("usuarioId", usuarioId)
                    .list();
        }
    }

    @Override
    public void atualizar(Tarefa obj) {
        throw new UnsupportedOperationException("Use atualizar(Tarefa, Integer usuarioId)");
    }

    public void atualizar(Tarefa obj, Integer usuarioId) {
        if (usuarioId == null || obj.getId() == null) {
            return;
        }
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();
            Tarefa atual = buscarTarefaDoUsuario(session, obj.getId(), usuarioId);
            if (atual != null) {
                StatusTarefa statusAnterior = atual.getStatus();
                if (obj.getTitulo() != null) atual.setTitulo(obj.getTitulo());
                if (obj.getDescricao() != null) atual.setDescricao(obj.getDescricao());
                if (obj.getStatus() != null) atual.setStatus(obj.getStatus());
                if (obj.getDataPrazo() != null) atual.setDataPrazo(obj.getDataPrazo());
                if (obj.getCategoria() != null) atual.setCategoria(resolveCategoria(session, obj.getCategoria()));
                if (obj.getXingamentos() != null) atual.setXingamentos(resolveXingamentos(session, obj.getXingamentos()));
                session.merge(atual);
                registrarHistorico(session, atual, "ATUALIZACAO", usuarioId);
                registrarRecompensaPorStatus(session, atual, statusAnterior, atual.getStatus(), usuarioId);
            }
            tx.commit();
        }
    }

    @Override
    public void deletar(Integer id) {
        throw new UnsupportedOperationException("Use deletar(Integer, Integer)");
    }

    public void deletar(Integer id, Integer usuarioId) {
        if (id == null || usuarioId == null) {
            return;
        }
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();
            try {
                Tarefa tarefa = buscarTarefaDoUsuario(session, id, usuarioId);
                if (tarefa != null) {
                    session.createMutationQuery(
                                    "update Historico h set h.tarefa = null where h.tarefa.id = :id")
                            .setParameter("id", id).executeUpdate();
                    session.createMutationQuery(
                                    "update Recompensa r set r.tarefa = null where r.tarefa.id = :id")
                            .setParameter("id", id).executeUpdate();
                    session.createNativeQuery("delete from tarefa_xingamento where id_tarefa = :id")
                            .setParameter("id", id).executeUpdate();
                    registrarHistorico(session, tarefa, "EXCLUSAO", usuarioId);
                    session.remove(tarefa);
                }
                tx.commit();
            } catch (RuntimeException ex) {
                if (tx != null && tx.isActive()) tx.rollback();
                throw ex;
            }
        }
    }

    public void atualizarStatus(Integer id, StatusTarefa status, Integer usuarioId) {
        if (id == null || usuarioId == null) return;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();
            Tarefa tarefa = buscarTarefaDoUsuario(session, id, usuarioId);
            if (tarefa != null) {
                StatusTarefa statusAnterior = tarefa.getStatus();
                tarefa.setStatus(status);
                session.merge(tarefa);
                registrarHistorico(session, tarefa, "STATUS", usuarioId);
                registrarRecompensaPorStatus(session, tarefa, statusAnterior, status, usuarioId);
            }
            tx.commit();
        }
    }

    // --- métodos de equipe ---

    public List<Tarefa> listarPorEquipe(Integer equipeId) {
        if (equipeId == null) return List.of();
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery(
                            "select distinct t from Tarefa t " +
                                    "left join fetch t.categoria " +
                                    "left join fetch t.responsavel " +
                                    "left join fetch t.xingamentos " +
                                    "where t.equipe.id = :eId " +
                                    "order by t.id desc", Tarefa.class)
                    .setParameter("eId", equipeId)
                    .list();
        }
    }

    public void salvarEquipe(Tarefa obj, Integer usuarioId, Integer equipeId, Integer responsavelId) {
        if (usuarioId == null || equipeId == null) {
            throw new IllegalArgumentException("usuarioId e equipeId sao obrigatorios");
        }
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();
            obj.setCategoria(resolveCategoria(session, obj.getCategoria()));
            obj.setUsuario(session.getReference(Usuario.class, usuarioId));
            obj.setEquipe(session.getReference(com.example.procastinator.model.Equipe.class, equipeId));
            if (responsavelId != null) {
                obj.setResponsavel(session.getReference(Usuario.class, responsavelId));
            }
            List<Xingamento> xingamentos = resolveXingamentos(session, obj.getXingamentos());
            if (xingamentos.isEmpty()) {
                xingamentos = carregarXingamentosPadrao(session, usuarioId);
            }
            obj.setXingamentos(xingamentos);
            session.persist(obj);
            registrarHistorico(session, obj, "CRIACAO", usuarioId);
            tx.commit();
        }
    }

    public void atualizarStatusEquipe(Integer tarefaId, StatusTarefa status, Integer equipeId) {
        if (tarefaId == null || equipeId == null) return;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();
            Tarefa tarefa = session.createQuery(
                            "from Tarefa t where t.id = :id and t.equipe.id = :eId", Tarefa.class)
                    .setParameter("id", tarefaId)
                    .setParameter("eId", equipeId)
                    .uniqueResult();
            if (tarefa != null) {
                tarefa.setStatus(status);
                session.merge(tarefa);
            }
            tx.commit();
        }
    }

    public void atribuirResponsavel(Integer tarefaId, Integer responsavelId, Integer equipeId) {
        if (tarefaId == null || equipeId == null) return;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();
            Tarefa tarefa = session.createQuery(
                            "from Tarefa t where t.id = :id and t.equipe.id = :eId", Tarefa.class)
                    .setParameter("id", tarefaId)
                    .setParameter("eId", equipeId)
                    .uniqueResult();
            if (tarefa != null) {
                tarefa.setResponsavel(responsavelId != null
                        ? session.getReference(Usuario.class, responsavelId) : null);
                session.merge(tarefa);
            }
            tx.commit();
        }
    }

    /**
     * Atualiza os dados de uma tarefa de equipe. Apenas o lider chama.
     */
    public void atualizarEquipe(Tarefa obj, Integer equipeId, Integer responsavelId) {
        if (obj.getId() == null || equipeId == null) return;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();
            Tarefa atual = session.createQuery(
                            "from Tarefa t where t.id = :id and t.equipe.id = :eId", Tarefa.class)
                    .setParameter("id", obj.getId())
                    .setParameter("eId", equipeId)
                    .uniqueResult();
            if (atual != null) {
                if (obj.getTitulo() != null && !obj.getTitulo().isBlank())
                    atual.setTitulo(obj.getTitulo());
                if (obj.getDescricao() != null)
                    atual.setDescricao(obj.getDescricao());
                if (obj.getStatus() != null)
                    atual.setStatus(obj.getStatus());
                if (obj.getDataPrazo() != null)
                    atual.setDataPrazo(obj.getDataPrazo());
                if (obj.getCategoria() != null)
                    atual.setCategoria(resolveCategoria(session, obj.getCategoria()));
                atual.setResponsavel(responsavelId != null
                        ? session.getReference(Usuario.class, responsavelId) : null);
                session.merge(atual);
            }
            tx.commit();
        }
    }
    /**
     * Exclui uma tarefa de equipe. Apenas o lider chama.
     */
    public void deletarEquipe(Integer tarefaId, Integer equipeId) {
        if (tarefaId == null || equipeId == null) return;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();
            try {
                Tarefa tarefa = session.createQuery(
                                "from Tarefa t where t.id = :id and t.equipe.id = :eId", Tarefa.class)
                        .setParameter("id", tarefaId)
                        .setParameter("eId", equipeId)
                        .uniqueResult();
                if (tarefa != null) {
                    session.createMutationQuery(
                                    "update Historico h set h.tarefa = null where h.tarefa.id = :id")
                            .setParameter("id", tarefaId).executeUpdate();
                    session.createMutationQuery(
                                    "update Recompensa r set r.tarefa = null where r.tarefa.id = :id")
                            .setParameter("id", tarefaId).executeUpdate();
                    session.createNativeQuery("delete from tarefa_xingamento where id_tarefa = :id")
                            .setParameter("id", tarefaId).executeUpdate();
                    session.remove(tarefa);
                }
                tx.commit();
            } catch (RuntimeException ex) {
                if (tx != null && tx.isActive()) tx.rollback();
                throw ex;
            }
        }
    }

    // --- helpers privados ---

    private Tarefa buscarTarefaDoUsuario(Session session, Integer id, Integer usuarioId) {
        return session.createQuery(
                        "from Tarefa t where t.id = :id and t.usuario.id = :usuarioId", Tarefa.class)
                .setParameter("id", id)
                .setParameter("usuarioId", usuarioId)
                .uniqueResult();
    }

    private void registrarRecompensaPorStatus(Session session, Tarefa tarefa, StatusTarefa anterior, StatusTarefa novo, Integer usuarioId) {
        if (tarefa == null || novo == null || novo == anterior) return;
        int pontos;
        String titulo;
        if (novo == StatusTarefa.ESPERANDO) {
            pontos = -5;
            titulo = "Subiu para Esperando";
        } else if (novo == StatusTarefa.QUASE_FIZ) {
            pontos = -10;
            titulo = "Chegou em Quase Fiz";
        } else {
            return;
        }
        Recompensa recompensa = new Recompensa();
        recompensa.setTitulo(titulo);
        recompensa.setDescricao("Movimentacao da tarefa: " + (tarefa.getTitulo() == null ? "Sem titulo" : tarefa.getTitulo()));
        recompensa.setPontos(pontos);
        recompensa.setDataConquista(LocalDateTime.now());
        recompensa.setTarefa(tarefa);
        if (usuarioId != null) {
            recompensa.setUsuario(session.getReference(Usuario.class, usuarioId));
        }
        session.persist(recompensa);
    }

    private Categoria resolveCategoria(Session session, Categoria categoria) {
        if (categoria == null) return ensureDefaultCategoria(session);
        if (categoria.getId() != null) {
            Categoria persistent = session.find(Categoria.class, categoria.getId());
            if (persistent != null) return persistent;
        }
        if (categoria.getNome() != null && !categoria.getNome().isBlank()) {
            Categoria byName = session.createQuery(
                            "from Categoria c where lower(c.nome) = :nome", Categoria.class)
                    .setParameter("nome", categoria.getNome().toLowerCase())
                    .uniqueResult();
            if (byName != null) return byName;
        }
        Categoria novaCategoria = new Categoria();
        novaCategoria.setNome((categoria.getNome() == null || categoria.getNome().isBlank()) ? "GERAL" : categoria.getNome());
        session.persist(novaCategoria);
        session.flush();
        return novaCategoria;
    }

    private Categoria ensureDefaultCategoria(Session session) {
        Categoria categoria = session.find(Categoria.class, 1);
        if (categoria == null) {
            categoria = new Categoria();
            categoria.setNome("GERAL");
            session.persist(categoria);
            session.flush();
        }
        return categoria;
    }

    private List<Xingamento> resolveXingamentos(Session session, List<Xingamento> xingamentos) {
        List<Xingamento> resolved = new ArrayList<>();
        if (xingamentos == null) return resolved;
        for (Xingamento xingamento : xingamentos) {
            Xingamento persistent = null;
            if (xingamento.getId() != null) {
                persistent = session.find(Xingamento.class, xingamento.getId());
            }
            if (persistent == null && xingamento.getMensagem() != null && !xingamento.getMensagem().isBlank()) {
                persistent = session.createQuery(
                                "from Xingamento x where lower(x.mensagem) = :mensagem and lower(coalesce(x.tipo, '')) = :tipo",
                                Xingamento.class)
                        .setParameter("mensagem", xingamento.getMensagem().toLowerCase())
                        .setParameter("tipo", xingamento.getTipo() == null ? "" : xingamento.getTipo().toLowerCase())
                        .uniqueResult();
            }
            if (persistent != null) resolved.add(persistent);
        }
        return resolved;
    }

    private List<Xingamento> carregarXingamentosPadrao(Session session, Integer usuarioId) {
        List<Xingamento> cadastrados = session.createQuery(
                        "from Xingamento x where x.usuario.id = :usuarioId order by x.id", Xingamento.class)
                .setParameter("usuarioId", usuarioId)
                .list();
        if (cadastrados.isEmpty()) return cadastrados;
        List<Xingamento> padrao = new ArrayList<>();
        for (Xingamento item : cadastrados) {
            String tipo = item.getTipo() == null ? "" : item.getTipo().toUpperCase(Locale.ROOT);
            if ("XINGAMENTO".equals(tipo) || "ELOGIO".equals(tipo)) padrao.add(item);
        }
        return padrao.isEmpty() ? cadastrados : padrao;
    }

    private void registrarHistorico(Session session, Tarefa tarefa, String acao, Integer usuarioId) {
        Historico historico = new Historico();
        historico.setAcao(acao);
        historico.setDataHora(LocalDateTime.now());
        historico.setTarefa(tarefa);
        if (usuarioId != null) {
            historico.setUsuario(session.getReference(Usuario.class, usuarioId));
        }
        session.persist(historico);
    }
}
