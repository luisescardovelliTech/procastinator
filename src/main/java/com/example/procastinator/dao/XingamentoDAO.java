package com.example.procastinator.dao;

import com.example.procastinator.model.Tarefa;
import com.example.procastinator.model.Xingamento;
import com.example.procastinator.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class XingamentoDAO {

    public List<Xingamento> listarTodos() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("from Xingamento x order by x.id desc", Xingamento.class).list();
        }
    }

    public Xingamento buscarPorId(Integer id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.find(Xingamento.class, id);
        }
    }

    public Xingamento salvarAviso(String mensagem, String tipo, List<Integer> tarefaIds) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();
            Xingamento aviso = new Xingamento();
            aviso.setMensagem(mensagem);
            aviso.setTipo(normalizarTipo(tipo));
            session.persist(aviso);
            session.flush();
            sincronizarRelacoesComTarefas(session, aviso.getId(), tarefaIds);
            tx.commit();
            return session.find(Xingamento.class, aviso.getId());
        }
    }

    public Xingamento atualizarAviso(Integer id, String mensagem, String tipo, List<Integer> tarefaIds) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();
            Xingamento aviso = session.find(Xingamento.class, id);
            if (aviso == null) {
                tx.rollback();
                return null;
            }

            aviso.setMensagem(mensagem);
            aviso.setTipo(normalizarTipo(tipo));
            sincronizarRelacoesComTarefas(session, id, tarefaIds);
            tx.commit();
            return session.find(Xingamento.class, id);
        }
    }

    public boolean excluirAviso(Integer id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();
            Xingamento aviso = session.find(Xingamento.class, id);
            if (aviso == null) {
                tx.rollback();
                return false;
            }

            session.createNativeQuery("delete from tarefa_xingamento where id_xingamento = :id")
                    .setParameter("id", id)
                    .executeUpdate();
            session.remove(aviso);
            tx.commit();
            return true;
        }
    }

    public List<TarefaResumo> listarTarefasVinculadas(Integer xingamentoId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            List<Tarefa> tarefas = session.createQuery(
                            "select t from Tarefa t join t.xingamentos x where x.id = :id order by t.id desc",
                            Tarefa.class)
                    .setParameter("id", xingamentoId)
                    .list();

            List<TarefaResumo> resumo = new ArrayList<>();
            for (Tarefa tarefa : tarefas) {
                resumo.add(new TarefaResumo(tarefa.getId(), tarefa.getTitulo()));
            }
            return resumo;
        }
    }

    private void sincronizarRelacoesComTarefas(Session session, Integer xingamentoId, List<Integer> tarefaIds) {
        session.createNativeQuery("delete from tarefa_xingamento where id_xingamento = :id")
                .setParameter("id", xingamentoId)
                .executeUpdate();

        for (Integer tarefaId : sanitizarIds(tarefaIds)) {
            session.createNativeQuery("insert into tarefa_xingamento (id_tarefa, id_xingamento) values (:idTarefa, :idXingamento)")
                    .setParameter("idTarefa", tarefaId)
                    .setParameter("idXingamento", xingamentoId)
                    .executeUpdate();
        }
    }

    private List<Integer> sanitizarIds(List<Integer> tarefaIds) {
        if (tarefaIds == null || tarefaIds.isEmpty()) {
            return List.of();
        }

        Set<Integer> idsUnicos = new LinkedHashSet<>();
        for (Integer id : tarefaIds) {
            if (id != null && id > 0) {
                idsUnicos.add(id);
            }
        }
        return new ArrayList<>(idsUnicos);
    }

    private String normalizarTipo(String tipo) {
        if (tipo == null || tipo.isBlank()) {
            return "XINGAMENTO";
        }
        String normalizado = tipo.trim().toUpperCase(Locale.ROOT);
        return "ELOGIO".equals(normalizado) ? "ELOGIO" : "XINGAMENTO";
    }

    public record TarefaResumo(Integer id, String titulo) {
    }
}
