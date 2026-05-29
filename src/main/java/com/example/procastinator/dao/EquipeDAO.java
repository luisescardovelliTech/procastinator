package com.example.procastinator.dao;

import com.example.procastinator.model.Equipe;
import com.example.procastinator.model.MembroEquipe;
import com.example.procastinator.model.PapelMembro;
import com.example.procastinator.model.Usuario;
import com.example.procastinator.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.util.List;

public class EquipeDAO {

    /**
     * Cria uma nova equipe e automaticamente adiciona o criador como LIDER.
     */
    public Equipe criar(String nome, String descricao, Integer liderId) {
        if (nome == null || nome.isBlank() || liderId == null) {
            throw new IllegalArgumentException("nome e liderId sao obrigatorios");
        }
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();

            Usuario lider = session.getReference(Usuario.class, liderId);

            Equipe equipe = new Equipe();
            equipe.setNome(nome.trim());
            equipe.setDescricao(descricao != null ? descricao.trim() : "");
            equipe.setLider(lider);
            session.persist(equipe);
            session.flush();

            MembroEquipe membroLider = new MembroEquipe();
            membroLider.setEquipe(equipe);
            membroLider.setUsuario(lider);
            membroLider.setPapel(PapelMembro.LIDER);
            session.persist(membroLider);

            tx.commit();
            return equipe;
        }
    }

    /**
     * Atualiza nome e descricao de uma equipe. Apenas o lider pode chamar.
     */
    public void atualizar(Integer equipeId, String novoNome, String novaDescricao, Integer liderId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();
            Equipe equipe = buscarEquipeDoLider(session, equipeId, liderId);
            if (equipe != null) {
                if (novoNome != null && !novoNome.isBlank()) {
                    equipe.setNome(novoNome.trim());
                }
                if (novaDescricao != null) {
                    equipe.setDescricao(novaDescricao.trim());
                }
                session.merge(equipe);
            }
            tx.commit();
        }
    }

    /**
     * Exclui uma equipe e todos os seus membros. Apenas o lider pode chamar.
     */
    public void excluir(Integer equipeId, Integer liderId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();
            Equipe equipe = buscarEquipeDoLider(session, equipeId, liderId);
            if (equipe != null) {
                // Desvincula tarefas da equipe antes de excluir
                session.createMutationQuery(
                                "update Tarefa t set t.equipe = null, t.responsavel = null where t.equipe.id = :eId")
                        .setParameter("eId", equipeId)
                        .executeUpdate();
                // Remove membros
                session.createMutationQuery(
                                "delete from MembroEquipe m where m.equipe.id = :eId")
                        .setParameter("eId", equipeId)
                        .executeUpdate();
                session.remove(equipe);
            }
            tx.commit();
        }
    }

    /**
     * Lista todas as equipes onde o usuario é membro (incluindo as que é líder).
     */
    public List<Equipe> listarPorMembro(Integer usuarioId) {
        if (usuarioId == null) return List.of();
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery(
                            "select m.equipe from MembroEquipe m " +
                                    "left join fetch m.equipe.lider " +
                                    "where m.usuario.id = :uid order by m.equipe.nome", Equipe.class)
                    .setParameter("uid", usuarioId)
                    .list();
        }
    }

    /**
     * Busca uma equipe por id, verificando que o usuario é membro.
     */
    public Equipe buscarPorIdEMembro(Integer equipeId, Integer usuarioId) {
        if (equipeId == null || usuarioId == null) return null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery(
                            "select e from Equipe e " +
                                    "join fetch e.lider " +
                                    "where e.id = :eId " +
                                    "and exists (select m from MembroEquipe m where m.equipe.id = :eId and m.usuario.id = :uid)",
                            Equipe.class)
                    .setParameter("eId", equipeId)
                    .setParameter("uid", usuarioId)
                    .uniqueResult();
        }
    }

    /**
     * Lista todos os membros de uma equipe.
     */
    public List<MembroEquipe> listarMembros(Integer equipeId) {
        if (equipeId == null) return List.of();
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery(
                            "from MembroEquipe m join fetch m.usuario where m.equipe.id = :eId order by m.papel, m.usuario.nome",
                            MembroEquipe.class)
                    .setParameter("eId", equipeId)
                    .list();
        }
    }

    /**
     * Adiciona um usuario existente como membro de uma equipe. Apenas o lider pode chamar.
     */
    public String adicionarMembro(Integer equipeId, String emailNovoMembro, Integer liderId) {
        if (equipeId == null || emailNovoMembro == null || liderId == null) {
            return "Dados invalidos.";
        }
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();
            Equipe equipe = buscarEquipeDoLider(session, equipeId, liderId);
            if (equipe == null) {
                return "Equipe nao encontrada ou voce nao e o lider.";
            }
            Usuario novoMembro = session.createQuery(
                            "from Usuario u where lower(u.email) = :email", Usuario.class)
                    .setParameter("email", emailNovoMembro.trim().toLowerCase())
                    .uniqueResult();
            if (novoMembro == null) {
                return "Nenhum usuario encontrado com o email informado.";
            }
            // Verifica se ja e membro
            Long jaExiste = session.createQuery(
                            "select count(m) from MembroEquipe m where m.equipe.id = :eId and m.usuario.id = :uid", Long.class)
                    .setParameter("eId", equipeId)
                    .setParameter("uid", novoMembro.getId())
                    .uniqueResult();
            if (jaExiste != null && jaExiste > 0) {
                return "Esse usuario ja e membro da equipe.";
            }
            MembroEquipe membro = new MembroEquipe();
            membro.setEquipe(equipe);
            membro.setUsuario(novoMembro);
            membro.setPapel(PapelMembro.MEMBRO);
            session.persist(membro);
            tx.commit();
            return null; // null = sucesso
        }
    }

    /**
     * Remove um membro da equipe. Lider nao pode remover a si mesmo.
     */
    public String removerMembro(Integer equipeId, Integer membroId, Integer liderId) {
        if (equipeId == null || membroId == null || liderId == null) {
            return "Dados invalidos.";
        }
        if (membroId.equals(liderId)) {
            return "O lider nao pode remover a si mesmo da equipe.";
        }
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();
            Equipe equipe = buscarEquipeDoLider(session, equipeId, liderId);
            if (equipe == null) {
                return "Equipe nao encontrada ou voce nao e o lider.";
            }
            // Desvincula tarefas do membro removido nessa equipe
            session.createMutationQuery(
                            "update Tarefa t set t.responsavel = null " +
                                    "where t.equipe.id = :eId and t.responsavel.id = :uid")
                    .setParameter("eId", equipeId)
                    .setParameter("uid", membroId)
                    .executeUpdate();
            int removidos = session.createMutationQuery(
                            "delete from MembroEquipe m where m.equipe.id = :eId and m.usuario.id = :uid and m.papel <> 'LIDER'")
                    .setParameter("eId", equipeId)
                    .setParameter("uid", membroId)
                    .executeUpdate();
            tx.commit();
            return removidos == 0 ? "Membro nao encontrado." : null;
        }
    }

    /**
     * Verifica se o usuario e lider da equipe.
     */
    public boolean isLider(Integer equipeId, Integer usuarioId) {
        if (equipeId == null || usuarioId == null) return false;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Long count = session.createQuery(
                            "select count(m) from MembroEquipe m " +
                                    "where m.equipe.id = :eId and m.usuario.id = :uid and m.papel = 'LIDER'", Long.class)
                    .setParameter("eId", equipeId)
                    .setParameter("uid", usuarioId)
                    .uniqueResult();
            return count != null && count > 0;
        }
    }

    // --- helpers privados ---

    private Equipe buscarEquipeDoLider(Session session, Integer equipeId, Integer liderId) {
        return session.createQuery(
                        "from Equipe e where e.id = :eId and e.lider.id = :lid", Equipe.class)
                .setParameter("eId", equipeId)
                .setParameter("lid", liderIdParm(liderId))
                .uniqueResult();
    }

    // workaround para evitar ambiguidade de tipo no setParameter
    private static Integer liderIdParm(Integer id) {
        return id;
    }
}
