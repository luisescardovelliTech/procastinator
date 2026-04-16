package com.example.procastinator.servlet;

import com.example.procastinator.dao.RecompensaDAO;
import com.example.procastinator.model.Recompensa;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;

@WebServlet("/api/recompensas/*")
public class RecompensaServlet extends HttpServlet {
    private final RecompensaDAO dao = new RecompensaDAO();
    private final Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json; charset=UTF-8");

        Integer id = extrairId(req);
        if (id != null) {
            Recompensa recompensa = dao.buscarPorId(id);
            if (recompensa == null) {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Recompensa nao encontrada.");
                return;
            }
            resp.getWriter().write(gson.toJson(RecompensaResponse.from(recompensa)));
            return;
        }

        List<RecompensaResponse> payload = dao.listarTodos().stream()
                .map(RecompensaResponse::from)
                .toList();
        resp.getWriter().write(gson.toJson(payload));
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        JsonObject body = JsonParser.parseReader(req.getReader()).getAsJsonObject();
        String titulo = getTextoObrigatorio(body, resp);
        if (titulo == null) {
            return;
        }

        Recompensa salva = dao.salvar(
                titulo,
                getTexto(body, "descricao", "")
        );

        resp.setStatus(HttpServletResponse.SC_CREATED);
        resp.setContentType("application/json; charset=UTF-8");
        resp.getWriter().write(gson.toJson(RecompensaResponse.from(salva)));
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Integer id = extrairId(req);
        if (id == null) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Id da recompensa invalido.");
            return;
        }

        JsonObject body = JsonParser.parseReader(req.getReader()).getAsJsonObject();
        String titulo = getTextoObrigatorio(body, resp);
        if (titulo == null) {
            return;
        }

        Recompensa atualizada = dao.atualizar(
                id,
                titulo,
                getTexto(body, "descricao", "")
        );

        if (atualizada == null) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Recompensa nao encontrada.");
            return;
        }

        resp.setContentType("application/json; charset=UTF-8");
        resp.getWriter().write(gson.toJson(RecompensaResponse.from(atualizada)));
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Integer id = extrairId(req);
        if (id == null) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Id da recompensa invalido.");
            return;
        }

        boolean removido = dao.excluir(id);
        if (!removido) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Recompensa nao encontrada.");
            return;
        }

        resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
    }

    private String getTextoObrigatorio(JsonObject body, HttpServletResponse resp) throws IOException {
        String titulo = getTexto(body, "titulo", "");
        if (titulo.isBlank()) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Titulo da recompensa e obrigatorio.");
            return null;
        }
        return titulo;
    }

    private String getTexto(JsonObject body, String campo, String valorPadrao) {
        if (!body.has(campo) || body.get(campo).isJsonNull()) {
            return valorPadrao;
        }
        return body.get(campo).getAsString().trim();
    }

    private Integer extrairId(HttpServletRequest req) {
        String pathInfo = req.getPathInfo();
        if (pathInfo == null || pathInfo.equals("/") || pathInfo.isBlank()) {
            return null;
        }

        String valor = pathInfo.startsWith("/") ? pathInfo.substring(1) : pathInfo;
        if (valor.contains("/")) {
            return null;
        }

        try {
            return Integer.parseInt(valor);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private record RecompensaResponse(
            Integer id,
            String titulo,
            String descricao,
            Integer pontos,
            String dataConquista,
            Integer tarefaId,
            String tarefaTitulo
    ) {
        static RecompensaResponse from(Recompensa recompensa) {
            return new RecompensaResponse(
                    recompensa.getId(),
                    recompensa.getTitulo(),
                    recompensa.getDescricao(),
                    recompensa.getPontos(),
                    recompensa.getDataConquista() != null ? recompensa.getDataConquista().toString() : null,
                    recompensa.getTarefa() != null ? recompensa.getTarefa().getId() : null,
                    recompensa.getTarefa() != null ? recompensa.getTarefa().getTitulo() : "Sem tarefa associada"
            );
        }
    }
}


