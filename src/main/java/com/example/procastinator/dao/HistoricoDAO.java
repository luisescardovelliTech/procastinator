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
    public List<Historico> listarDesculpas() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery(
                            "select h from Historico h " +
                                    "left join fetch h.tarefa " +
                                    "left join fetch h.usuario " +
                                    "where h.acao = :acao " +
                                    "order by h.dataHora desc",
                            Historico.class)
                    .setParameter("acao", "DESCULPA")
                    .list();
        }
    }

    public Historico salvarDesculpa(Integer tarefaId, String comentario, Integer nivelEficacia) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();
            Historico historico = new Historico();
            historico.setAcao("DESCULPA");
            historico.setDataHora(LocalDateTime.now());
            historico.setComentario(comentario);
            historico.setNivelEficacia(normalizarEficacia(nivelEficacia));
            historico.setUsuario(ensureDefaultUser(session));
            if (tarefaId != null) {
                historico.setTarefa(session.find(Tarefa.class, tarefaId));
            }
            session.persist(historico);
            tx.commit();
            return carregarDesculpaDetalhada(session, historico.getId());
        }
    }

    public Historico atualizarDesculpa(Integer id, Integer tarefaId, String comentario, Integer nivelEficacia) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();
            Historico historico = session.find(Historico.class, id);
            if (historico == null || !"DESCULPA".equals(historico.getAcao())) {
                tx.rollback();
                return null;
            }

            historico.setComentario(comentario);
            historico.setNivelEficacia(normalizarEficacia(nivelEficacia));
            historico.setDataHora(LocalDateTime.now());
            if (tarefaId != null) {
                historico.setTarefa(session.find(Tarefa.class, tarefaId));
            } else {
                historico.setTarefa(null);
            }

            tx.commit();
            return carregarDesculpaDetalhada(session, historico.getId());
        }
    }

    public boolean excluirDesculpa(Integer id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();
            Historico historico = session.find(Historico.class, id);
            if (historico == null || !"DESCULPA".equals(historico.getAcao())) {
                tx.rollback();
                return false;
            }

            session.remove(historico);
            tx.commit();
            return true;
        }
    }

    private Historico carregarDesculpaDetalhada(Session session, Integer id) {
        return session.createQuery(
                        "select h from Historico h " +
                                "left join fetch h.tarefa " +
                                "left join fetch h.usuario " +
                                "where h.id = :id",
                        Historico.class)
                .setParameter("id", id)
                .uniqueResult();
    }

    private int normalizarEficacia(Integer valor) {
        if (valor == null) {
            return 5;
        }
        return Math.max(0, Math.min(10, valor));
    }

    private Usuario ensureDefaultUser(Session session) {
        Usuario usuario = session.find(Usuario.class, 1);
        if (usuario == null) {
            usuario = new Usuario();
            usuario.setNome("Setor de Inercia");
            usuario.setEmail("inercia@procrastinator.local");
            usuario.setSenha("123456");
            session.persist(usuario);
            session.flush();
        }
        return usuario;
    }
}


