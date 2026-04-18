package com.example.procastinator.dao;

import com.example.procastinator.model.Recompensa;
import com.example.procastinator.model.StatusTarefa;
import com.example.procastinator.model.Tarefa;
import com.example.procastinator.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.time.LocalDateTime;
import java.util.List;

public class RecompensaDAO {

    public List<Recompensa> listarTodos() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery(
                            "select r from com.example.procastinator.model.Recompensa r " +
                                    "left join fetch r.tarefa " +
                                    "order by r.dataConquista desc, r.id desc",
                            Recompensa.class)
                    .list();
        }
    }

    public Recompensa buscarPorId(Integer id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return carregarDetalhada(session, id);
        }
    }

    public List<Recompensa> listarMudancasDeColunaDaTarefa(Integer tarefaId) {
        if (tarefaId == null) {
            return List.of();
        }
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery(
                            "select r from com.example.procastinator.model.Recompensa r " +
                                    "where r.tarefa.id = :tarefaId " +
                                    "and (r.titulo like 'Subiu para %' or r.titulo like 'Chegou em %') " +
                                    "order by r.dataConquista asc, r.id asc",
                            Recompensa.class)
                    .setParameter("tarefaId", tarefaId)
                    .list();
        }
    }

    public Recompensa salvar(String titulo, String descricao) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();
            Recompensa recompensa = new Recompensa();
            String tituloNormalizado = normalizarTitulo(titulo);
            String descricaoNormalizada = normalizarDescricao(descricao);
            Tarefa tarefa = buscarTarefaPadrao(session);
            recompensa.setTitulo(tituloNormalizado);
            recompensa.setDescricao(descricaoNormalizada);
            recompensa.setTarefa(tarefa);
            recompensa.setPontos(calcularPontos(tarefa, tituloNormalizado, descricaoNormalizada));
            recompensa.setDataConquista(LocalDateTime.now());
            session.persist(recompensa);
            tx.commit();
            return carregarDetalhada(session, recompensa.getId());
        }
    }

    public Recompensa atualizar(Integer id, String titulo, String descricao) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();
            Recompensa recompensa = session.find(Recompensa.class, id);
            if (recompensa == null) {
                tx.rollback();
                return null;
            }

            String tituloNormalizado = normalizarTitulo(titulo);
            String descricaoNormalizada = normalizarDescricao(descricao);
            Tarefa tarefa = recompensa.getTarefa();
            if (tarefa == null) {
                tarefa = buscarTarefaPadrao(session);
            }
            recompensa.setTitulo(tituloNormalizado);
            recompensa.setDescricao(descricaoNormalizada);
            recompensa.setTarefa(tarefa);
            recompensa.setPontos(calcularPontos(tarefa, tituloNormalizado, descricaoNormalizada));

            tx.commit();
            return carregarDetalhada(session, recompensa.getId());
        }
    }

    public boolean excluir(Integer id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();
            Recompensa recompensa = session.find(Recompensa.class, id);
            if (recompensa == null) {
                tx.rollback();
                return false;
            }

            session.remove(recompensa);
            tx.commit();
            return true;
        }
    }

    private Recompensa carregarDetalhada(Session session, Integer id) {
        return session.createQuery(
                        "select r from com.example.procastinator.model.Recompensa r " +
                                "left join fetch r.tarefa " +
                                "where r.id = :id",
                        Recompensa.class)
                .setParameter("id", id)
                .uniqueResult();
    }

    private Tarefa buscarTarefaPadrao(Session session) {
        return session.createQuery("from Tarefa t order by t.id desc", Tarefa.class)
                .setMaxResults(1)
                .uniqueResult();
    }

    private String normalizarTitulo(String titulo) {
        if (titulo == null || titulo.isBlank()) {
            return "Recompensa sem titulo";
        }
        return titulo.trim();
    }

    private String normalizarDescricao(String descricao) {
        if (descricao == null) {
            return "";
        }
        return descricao.trim();
    }

    private int calcularPontos(Tarefa tarefa, String titulo, String descricao) {
        int pontosBase = 8;
        if (tarefa != null && tarefa.getStatus() != null) {
            if (tarefa.getStatus() == StatusTarefa.BACKLOG) {
                pontosBase = 6;
            } else if (tarefa.getStatus() == StatusTarefa.ESPERANDO) {
                pontosBase = 12;
            } else if (tarefa.getStatus() == StatusTarefa.QUASE_FIZ) {
                pontosBase = 18;
            }
        }

        int bonusTitulo = Math.min(5, Math.max(0, titulo.length() / 12));
        int bonusDescricao = Math.min(7, Math.max(0, descricao.length() / 30));
        int total = pontosBase + bonusTitulo + bonusDescricao;
        int limitado = Math.min(50, total);
        return -limitado;
    }
}



