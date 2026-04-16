package com.example.procastinator.dao;

import com.example.procastinator.model.Categoria;
import com.example.procastinator.model.Historico;
import com.example.procastinator.model.Recompensa;
import com.example.procastinator.model.StatusTarefa;
import com.example.procastinator.model.Tarefa;
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
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();
            obj.setCategoria(resolveCategoria(session, obj.getCategoria()));
            List<Xingamento> xingamentos = resolveXingamentos(session, obj.getXingamentos());
            if (xingamentos.isEmpty()) {
                xingamentos = carregarXingamentosPadrao(session);
            }
            obj.setXingamentos(xingamentos);
            session.persist(obj);
            registrarHistorico(session, obj, "CRIACAO");
            tx.commit();
        }
    }

    @Override
    public Tarefa buscarPorId(Integer id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery(
                            "select distinct t from Tarefa t " +
                                    "left join fetch t.categoria " +
                                    "left join fetch t.xingamentos where t.id = :id", Tarefa.class)
                    .setParameter("id", id)
                    .uniqueResult();
        }
    }

    @Override
    public List<Tarefa> listarTodos() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery(
                            "select distinct t from Tarefa t " +
                                    "left join fetch t.categoria " +
                                    "left join fetch t.xingamentos " +
                                    "order by t.id desc", Tarefa.class)
                    .list();
        }
    }

    @Override
    public void atualizar(Tarefa obj) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();
            Tarefa atual = session.find(Tarefa.class, obj.getId());
            if (atual != null) {
                StatusTarefa statusAnterior = atual.getStatus();
                if (obj.getTitulo() != null) {
                    atual.setTitulo(obj.getTitulo());
                }
                if (obj.getDescricao() != null) {
                    atual.setDescricao(obj.getDescricao());
                }
                if (obj.getStatus() != null) {
                    atual.setStatus(obj.getStatus());
                }
                if (obj.getDataPrazo() != null) {
                    atual.setDataPrazo(obj.getDataPrazo());
                }
                if (obj.getCategoria() != null) {
                    atual.setCategoria(resolveCategoria(session, obj.getCategoria()));
                }
                if (obj.getXingamentos() != null) {
                    atual.setXingamentos(resolveXingamentos(session, obj.getXingamentos()));
                }
                session.merge(atual);
                registrarHistorico(session, atual, "ATUALIZACAO");
                registrarRecompensaPorStatus(session, atual, statusAnterior, atual.getStatus());
            }
            tx.commit();
        }
    }

    @Override
    public void deletar(Integer id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();
            Tarefa tarefa = session.find(Tarefa.class, id);
            if (tarefa != null) {
                session.createMutationQuery(
                                "update Historico h set h.tarefa = null where h.tarefa.id = :id")
                        .setParameter("id", id)
                        .executeUpdate();
                session.createNativeQuery("delete from tarefa_xingamento where id_tarefa = :id")
                        .setParameter("id", id)
                        .executeUpdate();
                registrarHistorico(session, null, "EXCLUSAO");
                session.remove(tarefa);
            }
            tx.commit();
        }
    }

    public void atualizarStatus(Integer id, StatusTarefa status) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();
            Tarefa tarefa = session.find(Tarefa.class, id);
            if (tarefa != null) {
                StatusTarefa statusAnterior = tarefa.getStatus();
                tarefa.setStatus(status);
                session.merge(tarefa);
                registrarHistorico(session, tarefa, "STATUS");
                registrarRecompensaPorStatus(session, tarefa, statusAnterior, status);
            }
            tx.commit();
        }
    }

    private void registrarRecompensaPorStatus(Session session, Tarefa tarefa, StatusTarefa anterior, StatusTarefa novo) {
        if (tarefa == null || novo == null || novo == anterior) {
            return;
        }

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
        session.persist(recompensa);
    }

    private Categoria resolveCategoria(Session session, Categoria categoria) {
        if (categoria == null) {
            return ensureDefaultCategoria(session);
        }
        if (categoria.getId() != null) {
            Categoria persistent = session.find(Categoria.class, categoria.getId());
            if (persistent != null) {
                return persistent;
            }
        }
        if (categoria.getNome() != null && !categoria.getNome().isBlank()) {
            Categoria byName = session.createQuery(
                            "from Categoria c where lower(c.nome) = :nome", Categoria.class)
                    .setParameter("nome", categoria.getNome().toLowerCase())
                    .uniqueResult();
            if (byName != null) {
                return byName;
            }
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
        if (xingamentos == null) {
            return resolved;
        }
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
            if (persistent != null) {
                resolved.add(persistent);
            }
        }
        return resolved;
    }

    private List<Xingamento> carregarXingamentosPadrao(Session session) {
        List<Xingamento> cadastrados = session.createQuery(
                        "from Xingamento x order by x.id", Xingamento.class)
                .list();
        if (cadastrados.isEmpty()) {
            return cadastrados;
        }

        List<Xingamento> padrao = new ArrayList<>();
        for (Xingamento item : cadastrados) {
            String tipo = item.getTipo() == null ? "" : item.getTipo().toUpperCase(Locale.ROOT);
            if ("XINGAMENTO".equals(tipo) || "ELOGIO".equals(tipo)) {
                padrao.add(item);
            }
        }
        return padrao.isEmpty() ? cadastrados : padrao;
    }

    private void registrarHistorico(Session session, Tarefa tarefa, String acao) {
        Historico historico = new Historico();
        historico.setAcao(acao);
        historico.setDataHora(LocalDateTime.now());
        historico.setTarefa(tarefa);
        session.persist(historico);
    }
}
