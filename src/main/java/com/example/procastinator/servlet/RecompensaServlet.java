package com.example.procastinator.servlet;

import com.example.procastinator.dao.RecompensaDAO;
import com.example.procastinator.model.Recompensa;
import com.example.procastinator.model.Tarefa;
import com.example.procastinator.web.FlashMensagens;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;

@WebServlet("/recompensas")
public class RecompensaServlet extends HttpServlet {

    private static final String VIEW = "/WEB-INF/jsp/recompensas.jsp";

    private final RecompensaDAO dao = new RecompensaDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        FlashMensagens.consumir(req);
        List<Recompensa> lista = dao.listarTodos();
        req.setAttribute("recompensas", lista);
        req.setAttribute("totalPontos", lista.stream().mapToInt(r -> r.getPontos() != null ? r.getPontos() : 0).sum());
        req.setAttribute("navAtivo", "recompensas");

        String detalhesId = req.getParameter("detalhes");
        if (detalhesId != null && !detalhesId.isBlank()) {
            try {
                int id = Integer.parseInt(detalhesId.trim());
                Recompensa recompensa = dao.buscarPorId(id);
                if (recompensa != null) {
                    Integer tarefaId = recompensa.getTarefa() != null ? recompensa.getTarefa().getId() : null;
                    Tarefa tarefa = recompensa.getTarefa();
                    req.setAttribute("detalheRecompensaId", id);
                    req.setAttribute("detalheTarefaTitulo", tarefa != null ? tarefa.getTitulo() : "Sem tarefa associada");
                    req.setAttribute("detalheTarefaDescricao", tarefa != null ? tarefa.getDescricao() : "");
                    req.setAttribute("detalheTarefaCriacao", tarefa != null && tarefa.getDataCriacao() != null
                            ? tarefa.getDataCriacao().toString() : null);
                    req.setAttribute("detalheMudancas", dao.listarMudancasDeColunaDaTarefa(tarefaId));
                    req.setAttribute("abrirModalDetalhes", true);
                }
            } catch (NumberFormatException ignored) {
                // ignora
            }
        }

        req.getRequestDispatcher(VIEW).forward(req, resp);
    }

    private static String normalizarDestino(String tituloEvento) {
        if (tituloEvento == null || tituloEvento.isBlank()) {
            return "Mudanca de coluna";
        }
        if (tituloEvento.startsWith("Subiu para ")) {
            return tituloEvento.substring("Subiu para ".length());
        }
        if (tituloEvento.startsWith("Chegou em ")) {
            return tituloEvento.substring("Chegou em ".length());
        }
        return tituloEvento;
    }

    public static String destinoColuna(Recompensa r) {
        return normalizarDestino(r.getTitulo());
    }
}
