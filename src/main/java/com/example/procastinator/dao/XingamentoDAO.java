package com.example.procastinator.dao;

import com.example.procastinator.model.Usuario;
import com.example.procastinator.model.Xingamento;
import com.example.procastinator.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.util.List;
import java.util.Locale;

public class XingamentoDAO {

    public enum ExclusaoAvisoResultado {
        REMOVIDO,
        NAO_ENCONTRADO,
        VINCULADO_A_TAREFA
    }

    public List<Xingamento> listarTodos() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("from Xingamento x order by x.id desc", Xingamento.class).list();
        }
    }

    public List<Xingamento> listarPorUsuario(Integer usuarioId) {
        if (usuarioId == null) {
            return List.of();
        }
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery(
                    "from Xingamento x where x.usuario.id = :usuarioId order by x.id desc",
                    Xingamento.class)
                    .setParameter("usuarioId", usuarioId)
                    .list();
        }
    }

    public Xingamento buscarPorId(Integer id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.find(Xingamento.class, id);
        }
    }

    public Xingamento salvarAviso(String mensagem, String tipo, Integer usuarioId) {
        if (usuarioId == null) {
            throw new IllegalArgumentException("usuarioId");
        }
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();
            Xingamento aviso = new Xingamento();
            aviso.setMensagem(mensagem);
            aviso.setTipo(normalizarTipo(tipo));
            aviso.setUsuario(session.getReference(Usuario.class, usuarioId));
            session.persist(aviso);
            tx.commit();
            return session.find(Xingamento.class, aviso.getId());
        }
    }

    public Xingamento atualizarAviso(Integer id, String mensagem, String tipo, Integer usuarioId) {
        if (usuarioId == null) {
            return null;
        }
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();
            Xingamento aviso = buscarAvisoPorUsuario(session, id, usuarioId);
            if (aviso == null) {
                tx.rollback();
                return null;
            }

            aviso.setMensagem(mensagem);
            aviso.setTipo(normalizarTipo(tipo));
            tx.commit();
            return session.find(Xingamento.class, id);
        }
    }

    public ExclusaoAvisoResultado excluirAviso(Integer id, Integer usuarioId) {
        if (usuarioId == null) {
            return ExclusaoAvisoResultado.NAO_ENCONTRADO;
        }
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();
            Xingamento aviso = buscarAvisoPorUsuario(session, id, usuarioId);
            if (aviso == null) {
                tx.rollback();
                return ExclusaoAvisoResultado.NAO_ENCONTRADO;
            }

            Number totalVinculos = (Number) session.createNativeQuery(
                            "select count(*) from tarefa_xingamento where id_xingamento = :id")
                    .setParameter("id", id)
                    .uniqueResult();
            int vinculos = totalVinculos == null ? 0 : totalVinculos.intValue();
            if (vinculos > 0) {
                tx.rollback();
                return ExclusaoAvisoResultado.VINCULADO_A_TAREFA;
            }

            session.remove(aviso);
            tx.commit();
            return ExclusaoAvisoResultado.REMOVIDO;
        }
    }

    private Xingamento buscarAvisoPorUsuario(Session session, Integer id, Integer usuarioId) {
        return session.createQuery(
                        "from Xingamento x where x.id = :id and x.usuario.id = :usuarioId",
                        Xingamento.class)
                .setParameter("id", id)
                .setParameter("usuarioId", usuarioId)
                .uniqueResult();
    }

    private String normalizarTipo(String tipo) {
        if (tipo == null || tipo.isBlank()) {
            return "XINGAMENTO";
        }
        String normalizado = tipo.trim().toUpperCase(Locale.ROOT);
        return "ELOGIO".equals(normalizado) ? "ELOGIO" : "XINGAMENTO";
    }
}
