package com.example.procastinator.dao;

import com.example.procastinator.model.Usuario;
import com.example.procastinator.util.HibernateUtil;
import com.example.procastinator.util.SenhaHasher;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.util.List;

public class UsuarioDAO {

    public Usuario buscarPorId(Integer id) {
        if (id == null) {
            return null;
        }
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.find(Usuario.class, id);
        }
    }

    /**
     * Retorna o usuario mais antigo com esse e-mail (ignora duplicatas no banco).
     */
    public Usuario buscarPorEmail(String email) {
        if (email == null || email.isBlank()) {
            return null;
        }
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery(
                            "from Usuario u where lower(u.email) = :email order by u.id asc", Usuario.class)
                    .setParameter("email", email.trim().toLowerCase())
                    .setMaxResults(1)
                    .uniqueResult();
        }
    }

    public boolean emailJaCadastrado(String email) {
        if (email == null || email.isBlank()) {
            return false;
        }
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Long total = session.createQuery(
                            "select count(u) from Usuario u where lower(u.email) = :email", Long.class)
                    .setParameter("email", email.trim().toLowerCase())
                    .uniqueResult();
            return total != null && total > 0;
        }
    }

    public Usuario cadastrar(String nome, String email, String senhaPlana) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();
            Usuario usuario = new Usuario();
            usuario.setNome(nome.trim());
            usuario.setEmail(email.trim().toLowerCase());
            usuario.setSenha(SenhaHasher.hash(senhaPlana));
            session.persist(usuario);
            tx.commit();
            return usuario;
        }
    }

    /**
     * Autentica mesmo se existirem linhas duplicadas com o mesmo e-mail: testa a senha em todas.
     */
    public Usuario autenticar(String email, String senhaPlana) {
        if (email == null || email.isBlank() || senhaPlana == null) {
            return null;
        }
        String em = email.trim().toLowerCase();
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            List<Usuario> candidatos = session.createQuery(
                            "from Usuario u where lower(u.email) = :email order by u.id asc", Usuario.class)
                    .setParameter("email", em)
                    .list();
            for (Usuario u : candidatos) {
                if (SenhaHasher.verificar(senhaPlana, u.getSenha())) {
                    return u;
                }
            }
            return null;
        }
    }
}
