package com.example.procastinator.servlet;

import com.example.procastinator.dao.XingamentoDAO;
import com.example.procastinator.model.Xingamento;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@WebServlet("/api/avisos/*")
public class AvisoServlet extends HttpServlet {
    private final XingamentoDAO dao = new XingamentoDAO();
    private final Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json; charset=UTF-8");

        Integer id = extrairId(req);
        if (id != null) {
            Xingamento aviso = dao.buscarPorId(id);
            if (aviso == null) {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Aviso nao encontrado.");
                return;
            }
            resp.getWriter().write(gson.toJson(AvisoResponse.from(aviso, dao.listarTarefasVinculadas(id))));
            return;
        }

        List<AvisoResponse> payload = dao.listarTodos().stream()
                .map(aviso -> AvisoResponse.from(aviso, dao.listarTarefasVinculadas(aviso.getId())))
                .toList();
        resp.getWriter().write(gson.toJson(payload));
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        JsonObject body = JsonParser.parseReader(req.getReader()).getAsJsonObject();
        String mensagem = getTextoObrigatorio(body, resp);
        if (mensagem == null) {
            return;
        }

        String tipo = getTexto(body, "tipo", "XINGAMENTO");
        List<Integer> tarefaIds = parseTarefaIds(body);
        Xingamento salvo = dao.salvarAviso(mensagem, tipo, tarefaIds);

        resp.setStatus(HttpServletResponse.SC_CREATED);
        resp.setContentType("application/json; charset=UTF-8");
        resp.getWriter().write(gson.toJson(AvisoResponse.from(salvo, dao.listarTarefasVinculadas(salvo.getId()))));
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Integer id = extrairId(req);
        if (id == null) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Id do aviso invalido.");
            return;
        }

        JsonObject body = JsonParser.parseReader(req.getReader()).getAsJsonObject();
        String mensagem = getTextoObrigatorio(body, resp);
        if (mensagem == null) {
            return;
        }

        String tipo = getTexto(body, "tipo", "XINGAMENTO");
        List<Integer> tarefaIds = parseTarefaIds(body);
        Xingamento atualizado = dao.atualizarAviso(id, mensagem, tipo, tarefaIds);

        if (atualizado == null) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Aviso nao encontrado.");
            return;
        }

        resp.setContentType("application/json; charset=UTF-8");
        resp.getWriter().write(gson.toJson(AvisoResponse.from(atualizado, dao.listarTarefasVinculadas(id))));
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Integer id = extrairId(req);
        if (id == null) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Id do aviso invalido.");
            return;
        }

        boolean removido = dao.excluirAviso(id);
        if (!removido) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Aviso nao encontrado.");
            return;
        }

        resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
    }

    private String getTextoObrigatorio(JsonObject body, HttpServletResponse resp) throws IOException {
        String mensagem = getTexto(body, "mensagem", "");
        if (mensagem.isBlank()) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Mensagem do aviso e obrigatoria.");
            return null;
        }
        return mensagem;
    }

    private String getTexto(JsonObject body, String campo, String valorPadrao) {
        if (!body.has(campo) || body.get(campo).isJsonNull()) {
            return valorPadrao;
        }
        return body.get(campo).getAsString().trim();
    }

    private List<Integer> parseTarefaIds(JsonObject body) {
        List<Integer> ids = new ArrayList<>();
        if (!body.has("tarefaIds") || body.get("tarefaIds").isJsonNull()) {
            return ids;
        }

        JsonArray jsonIds = body.getAsJsonArray("tarefaIds");
        for (int i = 0; i < jsonIds.size(); i++) {
            if (jsonIds.get(i) != null && !jsonIds.get(i).isJsonNull()) {
                ids.add(jsonIds.get(i).getAsInt());
            }
        }
        return ids;
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

    private record TarefaResumoResponse(Integer id, String titulo) {
        static TarefaResumoResponse from(XingamentoDAO.TarefaResumo resumo) {
            return new TarefaResumoResponse(resumo.id(), resumo.titulo());
        }
    }

    private record AvisoResponse(
            Integer id,
            String mensagem,
            String tipo,
            Integer totalTarefas,
            List<TarefaResumoResponse> tarefas
    ) {
        static AvisoResponse from(Xingamento aviso, List<XingamentoDAO.TarefaResumo> tarefasVinculadas) {
            List<TarefaResumoResponse> tarefas = tarefasVinculadas.stream()
                    .map(TarefaResumoResponse::from)
                    .toList();
            return new AvisoResponse(
                    aviso.getId(),
                    aviso.getMensagem(),
                    aviso.getTipo(),
                    tarefas.size(),
                    tarefas
            );
        }
    }
}

