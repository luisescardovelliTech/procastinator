package com.example.procastinator.servlet;

import com.example.procastinator.dao.XingamentoDAO;
import com.example.procastinator.web.FlashMensagens;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet(urlPatterns = {"/avisos", "/xingamentos"})
public class AvisoServlet extends HttpServlet {

    private static final String VIEW = "/WEB-INF/jsp/avisos.jsp";

    private final XingamentoDAO dao = new XingamentoDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        FlashMensagens.consumir(req);
        req.setAttribute("avisos", dao.listarTodos());
        req.setAttribute("navAtivo", "avisos");
        req.getRequestDispatcher(VIEW).forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        req.setCharacterEncoding("UTF-8");
        String acao = trim(req.getParameter("acao"));
        String base = req.getContextPath() + "/avisos";
        var session = req.getSession();

        try {
            switch (acao) {
                case "criar" -> {
                    criar(req);
                    FlashMensagens.toast(session, "Frase cadastrada com sucesso.");
                    resp.sendRedirect(base + "?t=1");
                }
                case "atualizar" -> {
                    atualizar(req);
                    FlashMensagens.toast(session, "Frase atualizada com sucesso.");
                    resp.sendRedirect(base + "?t=2");
                }
                case "excluir" -> {
                    if (!excluir(req, session)) {
                        resp.sendRedirect(base);
                        return;
                    }
                    FlashMensagens.toast(session, "Frase excluida com sucesso.");
                    resp.sendRedirect(base + "?t=3");
                }
                default -> {
                    FlashMensagens.erro(session, "Acao invalida.");
                    resp.sendRedirect(base);
                }
            }
        } catch (IllegalArgumentException ex) {
            FlashMensagens.erro(session, "Nao foi possivel concluir a acao.");
            resp.sendRedirect(base);
        }
        return;
    }

    private void criar(HttpServletRequest req) {
        String mensagem = trim(req.getParameter("mensagem"));
        if (mensagem.isBlank()) {
            throw new IllegalArgumentException("mensagem");
        }
        dao.salvarAviso(mensagem, trim(req.getParameter("tipo")));
    }

    private void atualizar(HttpServletRequest req) {
        Integer id = parseIntRequired(req.getParameter("id"));
        String mensagem = trim(req.getParameter("mensagem"));
        if (mensagem.isBlank()) {
            throw new IllegalArgumentException("mensagem");
        }
        String tipo = trim(req.getParameter("tipo"));
        if (dao.atualizarAviso(id, mensagem, tipo.isEmpty() ? "XINGAMENTO" : tipo) == null) {
            throw new IllegalArgumentException("notfound");
        }
    }

    private boolean excluir(HttpServletRequest req, jakarta.servlet.http.HttpSession session) {
        Integer id = parseIntRequired(req.getParameter("id"));
        XingamentoDAO.ExclusaoAvisoResultado r = dao.excluirAviso(id);
        if (r == XingamentoDAO.ExclusaoAvisoResultado.NAO_ENCONTRADO) {
            throw new IllegalArgumentException("notfound");
        }
        if (r == XingamentoDAO.ExclusaoAvisoResultado.VINCULADO_A_TAREFA) {
            FlashMensagens.erro(session,
                    "Nao e possivel excluir: este xingamento/elogio esta vinculado a uma tarefa.");
            return false;
        }
        return true;
    }

    private static String trim(String s) {
        return s == null ? "" : s.trim();
    }

    private static Integer parseIntRequired(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new IllegalArgumentException("id");
        }
        try {
            return Integer.parseInt(raw.trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("id");
        }
    }
}
