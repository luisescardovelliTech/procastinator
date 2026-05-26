package com.example.procastinator.servlet;

import com.example.procastinator.dao.HistoricoDAO;
import com.example.procastinator.dao.TarefaDAO;
import com.example.procastinator.model.Categoria;
import com.example.procastinator.model.Historico;
import com.example.procastinator.model.StatusTarefa;
import com.example.procastinator.model.Tarefa;
import com.example.procastinator.model.Xingamento;
import com.example.procastinator.web.FlashMensagens;
import com.example.procastinator.web.IncentivoSorteador;
import com.example.procastinator.web.SessaoUsuario;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@WebServlet(urlPatterns = {"/tarefas", "/lista"})
public class TarefaServlet extends HttpServlet {

    private static final String VIEW = "/WEB-INF/jsp/lista.jsp";

    private final TarefaDAO dao = new TarefaDAO();
    private final HistoricoDAO historicoDao = new HistoricoDAO();
    private final IncentivoSorteador sorteador = new IncentivoSorteador();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        FlashMensagens.consumir(req);
        prepararLista(req);

        Integer usuarioId = SessaoUsuario.obterId(req);
        String verDesculpas = req.getParameter("verDesculpas");
        if (verDesculpas != null && !verDesculpas.isBlank()) {
            try {
                int tarefaId = Integer.parseInt(verDesculpas.trim());
                Tarefa tarefa = dao.buscarPorId(tarefaId, usuarioId);
                if (tarefa != null) {
                    List<Historico> desculpas = historicoDao.listarDesculpas(usuarioId).stream()
                            .filter(h -> h.getTarefa() != null && tarefaId == h.getTarefa().getId())
                            .toList();
                    req.setAttribute("verDesculpasTarefa", tarefa);
                    req.setAttribute("verDesculpasLista", desculpas);
                    req.setAttribute("abrirModalDesculpas", true);
                }
            } catch (NumberFormatException ignored) {
                // ignora parametro invalido
            }
        }

        req.getRequestDispatcher(VIEW).forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        req.setCharacterEncoding("UTF-8");
        String acao = trim(req.getParameter("acao"));
        String base = req.getContextPath() + "/tarefas";
        HttpSession session = req.getSession();

        try {
            Integer usuarioId = SessaoUsuario.obterId(req);
            switch (acao) {
                case "criar" -> criar(req, session, usuarioId);
                case "atualizar" -> atualizar(req, usuarioId);
                case "mover" -> mover(req, session, usuarioId);
                case "excluir" -> excluir(req, usuarioId);
                default -> {
                    FlashMensagens.erro(session, "Acao invalida.");
                    resp.sendRedirect(base);
                    return;
                }
            }
        } catch (IllegalArgumentException ex) {
            FlashMensagens.erro(session, "Nao foi possivel concluir a acao. Verifique os dados.");
            resp.sendRedirect(base);
            return;
        }

        String redirect = base + switch (acao) {
            case "criar" -> "?t=1";
            case "atualizar" -> "?t=2";
            case "mover" -> "?t=3";
            case "excluir" -> "?t=4";
            default -> "";
        };
        resp.sendRedirect(redirect);
    }

    private void prepararLista(HttpServletRequest req) {
        List<Tarefa> todos = dao.listarPorUsuario(SessaoUsuario.obterId(req));
        List<Tarefa> backlog = new ArrayList<>();
        List<Tarefa> esperando = new ArrayList<>();
        List<Tarefa> quaseFiz = new ArrayList<>();
        for (Tarefa t : todos) {
            StatusTarefa s = t.getStatus() != null ? t.getStatus() : StatusTarefa.BACKLOG;
            switch (s) {
                case BACKLOG -> backlog.add(t);
                case ESPERANDO -> esperando.add(t);
                case QUASE_FIZ -> quaseFiz.add(t);
            }
        }
        req.setAttribute("backlog", backlog);
        req.setAttribute("esperando", esperando);
        req.setAttribute("quaseFiz", quaseFiz);
        req.setAttribute("navAtivo", "lista");
    }

    private void criar(HttpServletRequest req, HttpSession session, Integer usuarioId) {
        String titulo = trim(req.getParameter("titulo"));
        if (titulo.isEmpty()) {
            throw new IllegalArgumentException("titulo");
        }
        LocalDate prazo = parsePrazo(req.getParameter("dataPrazo"));
        if (prazo == null) {
            throw new IllegalArgumentException("prazo");
        }

        Tarefa tarefa = new Tarefa();
        tarefa.setTitulo(titulo);
        tarefa.setDescricao(trim(req.getParameter("descricao")));
        tarefa.setStatus(parseStatus(req.getParameter("status")));
        tarefa.setDataCriacao(LocalDate.now());
        tarefa.setDataPrazo(prazo);
        tarefa.setCategoria(parseCategoriaNome(trim(req.getParameter("categoria"))));

        Xingamento elogio = sorteador.sortearElogio();
        if (elogio != null) {
            tarefa.setXingamentos(List.of(elogio));
        }

        dao.salvar(tarefa, usuarioId);
        FlashMensagens.toast(session, "Tarefa registrada para futura procrastinacao.");
        if (elogio != null && elogio.getMensagem() != null) {
            FlashMensagens.elogio(session, elogio.getMensagem());
        }
    }

    private void atualizar(HttpServletRequest req, Integer usuarioId) {
        Integer id = parseId(req.getParameter("id"));
        if (id == null) {
            throw new IllegalArgumentException("id");
        }
        LocalDate prazo = parsePrazo(req.getParameter("dataPrazo"));
        if (prazo == null) {
            throw new IllegalArgumentException("prazo");
        }

        Tarefa tarefa = new Tarefa();
        tarefa.setId(id);
        tarefa.setTitulo(trim(req.getParameter("titulo")));
        tarefa.setDescricao(trim(req.getParameter("descricao")));
        tarefa.setStatus(parseStatus(req.getParameter("status")));
        tarefa.setDataPrazo(prazo);
        String catNome = trim(req.getParameter("categoria"));
        if (!catNome.isEmpty()) {
            tarefa.setCategoria(parseCategoriaNome(catNome));
        }
        dao.atualizar(tarefa, usuarioId);
        FlashMensagens.toast(req.getSession(), "Tarefa atualizada.");
    }

    private void mover(HttpServletRequest req, HttpSession session, Integer usuarioId) {
        Integer id = parseId(req.getParameter("id"));
        if (id == null) {
            throw new IllegalArgumentException("id");
        }
        StatusTarefa status = parseStatus(req.getParameter("status"));

        if (status == StatusTarefa.ESPERANDO || status == StatusTarefa.QUASE_FIZ) {
            Integer ultimoId = (Integer) session.getAttribute("ultimoXingamentoId");
            Xingamento xingamento = sorteador.sortearXingamento(ultimoId);
            if (xingamento != null) {
                Tarefa tarefa = new Tarefa();
                tarefa.setId(id);
                tarefa.setStatus(status);
                tarefa.setXingamentos(List.of(xingamento));
                dao.atualizar(tarefa, usuarioId);
                session.setAttribute("ultimoXingamentoId", xingamento.getId());
                if (xingamento.getMensagem() != null) {
                    FlashMensagens.xingamento(session, xingamento.getMensagem());
                }
            } else {
                dao.atualizarStatus(id, status, usuarioId);
            }
        } else {
            dao.atualizarStatus(id, status, usuarioId);
        }
        FlashMensagens.toast(session, "Status atualizado.");
    }

    private void excluir(HttpServletRequest req, Integer usuarioId) {
        Integer id = parseId(req.getParameter("id"));
        if (id == null) {
            throw new IllegalArgumentException("id");
        }
        dao.deletar(id, usuarioId);
        FlashMensagens.toast(req.getSession(), "Tarefa removida.");
    }

    private static Categoria parseCategoriaNome(String nome) {
        if (nome == null || nome.isBlank()) {
            return null;
        }
        Categoria c = new Categoria();
        c.setNome(nome);
        return c;
    }

    private static String trim(String s) {
        return s == null ? "" : s.trim();
    }

    private static Integer parseId(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        try {
            return Integer.parseInt(raw.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static StatusTarefa parseStatus(String raw) {
        if (raw == null || raw.isBlank()) {
            return StatusTarefa.BACKLOG;
        }
        return StatusTarefa.valueOf(raw.trim().toUpperCase(Locale.ROOT));
    }

    private static LocalDate parsePrazo(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        String n = raw.trim();
        if (n.length() > 10) {
            n = n.substring(0, 10);
        }
        return LocalDate.parse(n);
    }
}
