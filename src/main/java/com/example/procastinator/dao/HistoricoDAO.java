package com.example.procastinator.dao;

import com.example.procastinator.model.Historico;
import com.example.procastinator.model.Tarefa;
import com.example.procastinator.model.Usuario;
import com.example.procastinator.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.time.LocalDateTime;
import java.util.List;

public class HistoricoDAO {

    public List<Historico> listarDesculpas(Integer usuarioId) {
        if (usuarioId == null) {
            return List.of();
        }
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery(
                            "select h from Historico h " +
                                    "left join fetch h.tarefa t " +
                                    "where h.acao = :acao " +
                                    "and (h.usuario.id = :usuarioId or t.usuario.id = :usuarioId) " +
                                    "order by h.dataHora desc",
                            Historico.class)
                    .setParameter("acao", "DESCULPA")
                    .setParameter("usuarioId", usuarioId)
                    .list();
        }
    }

    public Historico salvarDesculpa(Integer tarefaId, String comentario, Integer nivelEficacia, Integer usuarioId) {
        if (usuarioId == null) {
            throw new IllegalArgumentException("usuarioId");
        }
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();
            Historico historico = new Historico();
            historico.setAcao("DESCULPA");
            historico.setDataHora(LocalDateTime.now());
            historico.setComentario(comentario);
            historico.setNivelEficacia(normalizarEficacia(nivelEficacia));
            historico.setUsuario(session.getReference(Usuario.class, usuarioId));
            if (tarefaId != null) {
                Tarefa tarefa = buscarTarefaDoUsuario(session, tarefaId, usuarioId);
                if (tarefa == null) {
                    tx.rollback();
                    throw new IllegalArgumentException("tarefa");
                }
                historico.setTarefa(tarefa);
            }
            session.persist(historico);
            tx.commit();
            return carregarDesculpaDetalhada(session, historico.getId(), usuarioId);
        }
    }

    public Historico atualizarDesculpa(Integer id, Integer tarefaId, String comentario, Integer nivelEficacia, Integer usuarioId) {
        if (usuarioId == null) {
            return null;
        }
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();
            Historico historico = buscarDesculpaDoUsuario(session, id, usuarioId);
            if (historico == null) {
                tx.rollback();
                return null;
            }

            historico.setComentario(comentario);
            historico.setNivelEficacia(normalizarEficacia(nivelEficacia));
            historico.setDataHora(LocalDateTime.now());
            if (tarefaId != null) {
                Tarefa tarefa = buscarTarefaDoUsuario(session, tarefaId, usuarioId);
                if (tarefa == null) {
                    tx.rollback();
                    return null;
                }
                historico.setTarefa(tarefa);
            } else {
                historico.setTarefa(null);
            }
            tx.commit();
            return carregarDesculpaDetalhada(session, historico.getId(), usuarioId);
        }
    }

    public boolean excluirDesculpa(Integer id, Integer usuarioId) {
        if (usuarioId == null) {
            return false;
        }
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();
            Historico historico = buscarDesculpaDoUsuario(session, id, usuarioId);
            if (historico == null) {
                tx.rollback();
                return false;
            }

            session.remove(historico);
            tx.commit();
            return true;
        }
    }

    private Historico buscarDesculpaDoUsuario(Session session, Integer id, Integer usuarioId) {
        return session.createQuery(
                        "select h from Historico h " +
                                "left join h.tarefa t " +
                                "where h.id = :id and h.acao = :acao " +
                                "and (h.usuario.id = :usuarioId or t.usuario.id = :usuarioId)",
                        Historico.class)
                .setParameter("id", id)
                .setParameter("acao", "DESCULPA")
                .setParameter("usuarioId", usuarioId)
                .uniqueResult();
    }

    private Tarefa buscarTarefaDoUsuario(Session session, Integer tarefaId, Integer usuarioId) {
        return session.createQuery(
                        "from Tarefa t where t.id = :id and t.usuario.id = :usuarioId", Tarefa.class)
                .setParameter("id", tarefaId)
                .setParameter("usuarioId", usuarioId)
                .uniqueResult();
    }

    private Historico carregarDesculpaDetalhada(Session session, Integer id, Integer usuarioId) {
        return buscarDesculpaDoUsuario(session, id, usuarioId);
    }

    private int normalizarEficacia(Integer valor) {
        if (valor == null) {
            return 5;
        }
        return Math.max(0, Math.min(10, valor));
    }
}
