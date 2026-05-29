package com.example.procastinator.servlet;

import com.example.procastinator.dao.EquipeDAO;
import com.example.procastinator.dao.TarefaDAO;
import com.example.procastinator.model.Categoria;
import com.example.procastinator.model.StatusTarefa;
import com.example.procastinator.model.Tarefa;
import com.example.procastinator.model.Xingamento;
import com.example.procastinator.web.FlashMensagens;
import com.example.procastinator.web.IncentivoSorteador;
import com.example.procastinator.web.SessaoUsuario;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Locale;

@WebServlet("/equipes/tarefa")
public class EquipeTarefaServlet extends HttpServlet {

    private final TarefaDAO tarefaDAO = new TarefaDAO();
    private final EquipeDAO equipeDAO = new EquipeDAO();
    private final IncentivoSorteador sorteador = new IncentivoSorteador();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        req.setCharacterEncoding("UTF-8");
        Integer usuarioId = SessaoUsuario.obterId(req);
        HttpSession session = req.getSession();
        String acao = trim(req.getParameter("acao"));
        Integer equipeId = parseId(req.getParameter("equipeId"));
        String base = req.getContextPath() + "/equipes" + (equipeId != null ? "/" + equipeId : "");

        if (equipeId == null) {
            resp.sendRedirect(req.getContextPath() + "/equipes");
            return;
        }

        if (equipeDAO.buscarPorIdEMembro(equipeId, usuarioId) == null) {
            FlashMensagens.erro(session, "Acesso negado.");
            resp.sendRedirect(base);
            return;
        }

        switch (acao) {
            case "criarEquipe" -> {
                if (!equipeDAO.isLider(equipeId, usuarioId)) {
                    FlashMensagens.erro(session, "Apenas o lider pode criar tarefas na equipe.");
                    resp.sendRedirect(base);
                    return;
                }
                String titulo = trim(req.getParameter("titulo"));
                if (titulo.isEmpty()) {
                    FlashMensagens.erro(session, "Titulo e obrigatorio.");
                    resp.sendRedirect(base);
                    return;
                }
                LocalDate prazo = parsePrazo(req.getParameter("dataPrazo"));
                if (prazo == null) {
                    FlashMensagens.erro(session, "Data de prazo invalida.");
                    resp.sendRedirect(base);
                    return;
                }
                Tarefa tarefa = new Tarefa();
                tarefa.setTitulo(titulo);
                tarefa.setDescricao(trim(req.getParameter("descricao")));
                tarefa.setStatus(parseStatus(req.getParameter("status")));
                tarefa.setDataCriacao(LocalDate.now());
                tarefa.setDataPrazo(prazo);
                tarefa.setCategoria(parseCategoriaNome(trim(req.getParameter("categoria"))));
                tarefaDAO.salvarEquipe(tarefa, usuarioId, equipeId, parseId(req.getParameter("responsavelId")));
                FlashMensagens.toast(session, "Tarefa criada na equipe.");
                resp.sendRedirect(base);
            }
            case "editarEquipe" -> {
                if (!equipeDAO.isLider(equipeId, usuarioId)) {
                    FlashMensagens.erro(session, "Apenas o lider pode editar tarefas da equipe.");
                    resp.sendRedirect(base);
                    return;
                }
                Integer tarefaId = parseId(req.getParameter("tarefaId"));
                if (tarefaId == null) {
                    FlashMensagens.erro(session, "Tarefa invalida.");
                    resp.sendRedirect(base);
                    return;
                }
                LocalDate prazo = parsePrazo(req.getParameter("dataPrazo"));
                if (prazo == null) {
                    FlashMensagens.erro(session, "Data de prazo invalida.");
                    resp.sendRedirect(base);
                    return;
                }
                Tarefa tarefa = new Tarefa();
                tarefa.setId(tarefaId);
                tarefa.setTitulo(trim(req.getParameter("titulo")));
                tarefa.setDescricao(trim(req.getParameter("descricao")));
                tarefa.setStatus(parseStatus(req.getParameter("status")));
                tarefa.setDataPrazo(prazo);
                tarefa.setCategoria(parseCategoriaNome(trim(req.getParameter("categoria"))));
                tarefaDAO.atualizarEquipe(tarefa, equipeId, parseId(req.getParameter("responsavelId")));
                FlashMensagens.toast(session, "Tarefa atualizada.");
                resp.sendRedirect(base);
            }
            case "excluirEquipe" -> {
                if (!equipeDAO.isLider(equipeId, usuarioId)) {
                    FlashMensagens.erro(session, "Apenas o lider pode excluir tarefas da equipe.");
                    resp.sendRedirect(base);
                    return;
                }
                Integer tarefaId = parseId(req.getParameter("tarefaId"));
                if (tarefaId != null) {
                    tarefaDAO.deletarEquipe(tarefaId, equipeId);
                    FlashMensagens.toast(session, "Tarefa removida.");
                }
                resp.sendRedirect(base);
            }
            case "moverEquipe" -> {
                Integer tarefaId = parseId(req.getParameter("tarefaId"));
                StatusTarefa status = parseStatus(req.getParameter("status"));
                if (tarefaId != null) {
                    if (status == StatusTarefa.ESPERANDO || status == StatusTarefa.QUASE_FIZ) {
                        Integer ultimoId = (Integer) session.getAttribute("ultimoXingamentoId");
                        Xingamento xingamento = sorteador.sortearXingamento(ultimoId, usuarioId);
                        if (xingamento != null) {
                            session.setAttribute("ultimoXingamentoId", xingamento.getId());
                            if (xingamento.getMensagem() != null) {
                                FlashMensagens.xingamento(session, xingamento.getMensagem());
                            }
                        }
                    }
                    tarefaDAO.atualizarStatusEquipe(tarefaId, status, equipeId);
                    FlashMensagens.toast(session, "Status atualizado.");
                }
                resp.sendRedirect(base);
            }
            case "atribuir" -> {
                if (!equipeDAO.isLider(equipeId, usuarioId)) {
                    FlashMensagens.erro(session, "Apenas o lider pode atribuir responsaveis.");
                    resp.sendRedirect(base);
                    return;
                }
                Integer tarefaId = parseId(req.getParameter("tarefaId"));
                if (tarefaId != null) {
                    tarefaDAO.atribuirResponsavel(tarefaId, parseId(req.getParameter("responsavelId")), equipeId);
                    FlashMensagens.toast(session, "Responsavel atribuido.");
                }
                resp.sendRedirect(base);
            }
            default -> resp.sendRedirect(base);
        }
    }

    private static String trim(String s) { return s == null ? "" : s.trim(); }

    private static Integer parseId(String raw) {
        if (raw == null || raw.isBlank()) return null;
        try { return Integer.parseInt(raw.trim()); }
        catch (NumberFormatException e) { return null; }
    }

    private static LocalDate parsePrazo(String raw) {
        if (raw == null || raw.isBlank()) return null;
        String n = raw.trim();
        if (n.length() > 10) n = n.substring(0, 10);
        try { return LocalDate.parse(n); }
        catch (Exception e) { return null; }
    }

    private static StatusTarefa parseStatus(String raw) {
        if (raw == null || raw.isBlank()) return StatusTarefa.BACKLOG;
        try { return StatusTarefa.valueOf(raw.trim().toUpperCase(Locale.ROOT)); }
        catch (Exception e) { return StatusTarefa.BACKLOG; }
    }

    private static Categoria parseCategoriaNome(String nome) {
        if (nome == null || nome.isBlank()) return null;
        Categoria c = new Categoria();
        c.setNome(nome);
        return c;
    }
}