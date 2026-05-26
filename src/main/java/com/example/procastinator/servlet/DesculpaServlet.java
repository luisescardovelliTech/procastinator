package com.example.procastinator.servlet;

import com.example.procastinator.dao.HistoricoDAO;
import com.example.procastinator.dao.TarefaDAO;
import com.example.procastinator.web.FlashMensagens;
import com.example.procastinator.web.SessaoUsuario;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet("/desculpas")
public class DesculpaServlet extends HttpServlet {

    private static final String VIEW = "/WEB-INF/jsp/desculpas.jsp";

    private final HistoricoDAO dao = new HistoricoDAO();
    private final TarefaDAO tarefaDao = new TarefaDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        FlashMensagens.consumir(req);
        Integer usuarioId = SessaoUsuario.obterId(req);
        req.setAttribute("desculpas", dao.listarDesculpas(usuarioId));
        req.setAttribute("tarefasSelect", tarefaDao.listarPorUsuario(usuarioId));
        req.setAttribute("navAtivo", "desculpas");
        req.getRequestDispatcher(VIEW).forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        req.setCharacterEncoding("UTF-8");
        String acao = trim(req.getParameter("acao"));
        String base = req.getContextPath() + "/desculpas";
        var session = req.getSession();

        Integer usuarioId = SessaoUsuario.obterId(req);
        try {
            switch (acao) {
                case "criar" -> criar(req, usuarioId);
                case "atualizar" -> atualizar(req, usuarioId);
                case "excluir" -> excluir(req, usuarioId);
                default -> {
                    FlashMensagens.erro(session, "Acao invalida.");
                    resp.sendRedirect(base);
                    return;
                }
            }
        } catch (IllegalArgumentException ex) {
            FlashMensagens.erro(session, "Nao foi possivel concluir a acao.");
            resp.sendRedirect(base);
            return;
        }

        String t = switch (acao) {
            case "criar" -> "1";
            case "atualizar" -> "2";
            case "excluir" -> "3";
            default -> "";
        };
        FlashMensagens.toast(session, switch (acao) {
            case "criar" -> "Desculpa arquivada no log com sucesso.";
            case "atualizar" -> "Desculpa atualizada com sucesso.";
            case "excluir" -> "Desculpa excluida com sucesso.";
            default -> "";
        });
        resp.sendRedirect(base + "?t=" + t);
    }

    private void criar(HttpServletRequest req, Integer usuarioId) {
        String comentario = trim(req.getParameter("comentario"));
        if (comentario.isBlank()) {
            throw new IllegalArgumentException("comentario");
        }
        dao.salvarDesculpa(parseIntOrNull(req.getParameter("tarefaId")), comentario,
                parseIntOrNull(req.getParameter("nivelEficacia")), usuarioId);
    }

    private void atualizar(HttpServletRequest req, Integer usuarioId) {
        Integer id = parseIntRequired(req.getParameter("id"));
        String comentario = trim(req.getParameter("comentario"));
        if (comentario.isBlank()) {
            throw new IllegalArgumentException("comentario");
        }
        if (dao.atualizarDesculpa(id, parseIntOrNull(req.getParameter("tarefaId")), comentario,
                parseIntOrNull(req.getParameter("nivelEficacia")), usuarioId) == null) {
            throw new IllegalArgumentException("notfound");
        }
    }

    private void excluir(HttpServletRequest req, Integer usuarioId) {
        Integer id = parseIntRequired(req.getParameter("id"));
        if (!dao.excluirDesculpa(id, usuarioId)) {
            throw new IllegalArgumentException("notfound");
        }
    }

    private static String trim(String s) {
        return s == null ? "" : s.trim();
    }

    private static Integer parseIntOrNull(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        try {
            return Integer.parseInt(raw.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static Integer parseIntRequired(String raw) {
        Integer v = parseIntOrNull(raw);
        if (v == null) {
            throw new IllegalArgumentException("id");
        }
        return v;
    }
}
