package com.example.procastinator.servlet;

import com.example.procastinator.dao.EquipeDAO;
import com.example.procastinator.dao.RecompensaDAO;
import com.example.procastinator.dao.TarefaDAO;
import com.example.procastinator.model.Equipe;
import com.example.procastinator.model.MembroEquipe;
import com.example.procastinator.model.Tarefa;
import com.example.procastinator.web.EstatisticasComputador;
import com.example.procastinator.web.FlashMensagens;
import com.example.procastinator.web.SessaoUsuario;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.List;

@WebServlet(urlPatterns = {"/equipes", "/equipes/*"})
public class EquipeServlet extends HttpServlet {

    private static final String VIEW_LISTA   = "/WEB-INF/jsp/equipes.jsp";
    private static final String VIEW_DETALHE = "/WEB-INF/jsp/equipe-detalhe.jsp";

    private final EquipeDAO equipeDAO = new EquipeDAO();
    private final TarefaDAO tarefaDAO = new TarefaDAO();
    private final RecompensaDAO recompensaDAO = new RecompensaDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        FlashMensagens.consumir(req);
        Integer usuarioId = SessaoUsuario.obterId(req);
        String pathInfo = req.getPathInfo(); // ex: /42

        if (pathInfo != null && pathInfo.length() > 1) {
            Integer equipeId = parseId(pathInfo.substring(1));
            if (equipeId == null) {
                resp.sendRedirect(req.getContextPath() + "/equipes");
                return;
            }
            Equipe equipe = equipeDAO.buscarPorIdEMembro(equipeId, usuarioId);
            if (equipe == null) {
                FlashMensagens.erro(req.getSession(), "Equipe nao encontrada ou sem acesso.");
                resp.sendRedirect(req.getContextPath() + "/equipes");
                return;
            }
            List<MembroEquipe> membros = equipeDAO.listarMembros(equipeId);
            List<Tarefa> tarefas = tarefaDAO.listarPorEquipe(equipeId);
            boolean isLider = equipeDAO.isLider(equipeId, usuarioId);
            var dashboard = EstatisticasComputador.build(
                    tarefas,
                    recompensaDAO.listarPorEquipe(equipeId)
            );

            req.setAttribute("equipe", equipe);
            req.setAttribute("membros", membros);
            req.setAttribute("tarefasEquipe", tarefas);
            req.setAttribute("isLider", isLider);
            req.setAttribute("totalPontos", dashboard.metricas().scorePontos());
            req.setAttribute("totalPendentes", dashboard.metricas().pendentes());
            req.setAttribute("navAtivo", "equipes");
            req.getRequestDispatcher(VIEW_DETALHE).forward(req, resp);
        } else {
            // lista de equipes do usuario
            List<Equipe> equipes = equipeDAO.listarPorMembro(usuarioId);
            req.setAttribute("equipes", equipes);
            req.setAttribute("navAtivo", "equipes");
            req.getRequestDispatcher(VIEW_LISTA).forward(req, resp);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        req.setCharacterEncoding("UTF-8");
        Integer usuarioId = SessaoUsuario.obterId(req);
        HttpSession session = req.getSession();
        String acao = trim(req.getParameter("acao"));
        String base = req.getContextPath() + "/equipes";

        switch (acao) {
            case "criar" -> {
                String nome = trim(req.getParameter("nome"));
                String descricao = trim(req.getParameter("descricao"));
                if (nome.isEmpty()) {
                    FlashMensagens.erro(session, "O nome da equipe e obrigatorio.");
                    resp.sendRedirect(base);
                    return;
                }
                Equipe criada = equipeDAO.criar(nome, descricao, usuarioId);
                FlashMensagens.toast(session, "Equipe \"" + criada.getNome() + "\" criada com sucesso.");
                resp.sendRedirect(base + "/" + criada.getId());
            }
            case "atualizar" -> {
                Integer equipeId = parseId(req.getParameter("equipeId"));
                if (equipeId == null) { resp.sendRedirect(base); return; }
                equipeDAO.atualizar(equipeId, trim(req.getParameter("nome")),
                        trim(req.getParameter("descricao")), usuarioId);
                FlashMensagens.toast(session, "Equipe atualizada.");
                resp.sendRedirect(base + "/" + equipeId);
            }
            case "excluir" -> {
                Integer equipeId = parseId(req.getParameter("equipeId"));
                if (equipeId != null) equipeDAO.excluir(equipeId, usuarioId);
                FlashMensagens.toast(session, "Equipe excluida.");
                resp.sendRedirect(base);
            }
            case "adicionarMembro" -> {
                Integer equipeId = parseId(req.getParameter("equipeId"));
                String email = trim(req.getParameter("email"));
                if (equipeId == null || email.isEmpty()) {
                    FlashMensagens.erro(session, "Informe o email do membro.");
                    resp.sendRedirect(base + (equipeId != null ? "/" + equipeId : ""));
                    return;
                }
                String erro = equipeDAO.adicionarMembro(equipeId, email, usuarioId);
                if (erro != null) {
                    FlashMensagens.erro(session, erro);
                } else {
                    FlashMensagens.toast(session, "Membro adicionado com sucesso.");
                }
                resp.sendRedirect(base + "/" + equipeId);
            }
            case "removerMembro" -> {
                Integer equipeId = parseId(req.getParameter("equipeId"));
                Integer membroId = parseId(req.getParameter("membroId"));
                if (equipeId != null && membroId != null) {
                    String erro = equipeDAO.removerMembro(equipeId, membroId, usuarioId);
                    if (erro != null) FlashMensagens.erro(session, erro);
                    else FlashMensagens.toast(session, "Membro removido.");
                }
                resp.sendRedirect(base + (equipeId != null ? "/" + equipeId : ""));
            }
            default -> resp.sendRedirect(base);
        }
    }

    private static String trim(String s) {
        return s == null ? "" : s.trim();
    }

    private static Integer parseId(String raw) {
        if (raw == null || raw.isBlank()) return null;
        try { return Integer.parseInt(raw.trim()); }
        catch (NumberFormatException e) { return null; }
    }
}
