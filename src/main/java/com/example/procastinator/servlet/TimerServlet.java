package com.example.procastinator.servlet;

import com.example.procastinator.dao.TarefaDAO;
import com.example.procastinator.model.StatusTarefa;
import com.example.procastinator.model.Tarefa;
import com.example.procastinator.web.FlashMensagens;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

@WebServlet("/timer")
public class TimerServlet extends HttpServlet {

    private static final String VIEW = "/WEB-INF/jsp/timer.jsp";

    private final TarefaDAO dao = new TarefaDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        FlashMensagens.consumir(req);
        List<Tarefa> all = dao.listarTodos();
        long pendentes = all.stream().filter(t -> t.getStatus() != StatusTarefa.QUASE_FIZ).count();
        LocalDate proximo = all.stream()
                .map(Tarefa::getDataPrazo)
                .filter(Objects::nonNull)
                .min(LocalDate::compareTo)
                .orElse(null);
        String proximoFmt = "--/--/----";
        if (proximo != null) {
            proximoFmt = proximo.format(DateTimeFormatter.ofPattern("dd/MM/yyyy").withLocale(Locale.forLanguageTag("pt-BR")));
        }
        req.setAttribute("timerPendentes", pendentes);
        req.setAttribute("timerAcoes", all.size());
        req.setAttribute("timerProximoPrazo", proximoFmt);
        req.setAttribute("navAtivo", "timer");
        req.getRequestDispatcher(VIEW).forward(req, resp);
    }
}
