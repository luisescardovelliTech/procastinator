package com.example.procastinator.servlet;

import com.example.procastinator.dao.RecompensaDAO;
import com.example.procastinator.dao.TarefaDAO;
import com.example.procastinator.web.EstatisticasComputador;
import com.example.procastinator.web.FlashMensagens;
import com.google.gson.Gson;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet("/estatisticas")
public class EstatisticasServlet extends HttpServlet {

    private static final String VIEW = "/WEB-INF/jsp/estatisticas.jsp";

    private final TarefaDAO tarefaDao = new TarefaDAO();
    private final RecompensaDAO recompensaDao = new RecompensaDAO();
    private final Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        FlashMensagens.consumir(req);
        var payload = EstatisticasComputador.build(tarefaDao.listarTodos(), recompensaDao.listarTodos());
        String json = gson.toJson(payload).replace("<", "\\u003c");
        req.setAttribute("dashboardJson", json);
        req.setAttribute("navAtivo", "estatisticas");
        req.getRequestDispatcher(VIEW).forward(req, resp);
    }
}
