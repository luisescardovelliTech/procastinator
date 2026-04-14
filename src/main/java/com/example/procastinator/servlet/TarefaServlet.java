package com.example.procastinator.servlet;

import com.example.procastinator.dao.TarefaDAO;
import com.example.procastinator.model.Categoria;
import com.example.procastinator.model.StatusTarefa;
import com.example.procastinator.model.Tarefa;
import com.example.procastinator.model.Xingamento;
import com.example.procastinator.util.LocalDateAdapter;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@WebServlet("/api/tarefas/*")
public class TarefaServlet extends HttpServlet {
    private final TarefaDAO dao = new TarefaDAO();
    private final Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDate.class, new LocalDateAdapter())
            .create();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json; charset=UTF-8");
        List<TarefaCardResponse> payload = dao.listarTodos().stream()
                .map(TarefaCardResponse::from)
                .toList();
        resp.getWriter().write(gson.toJson(payload));
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        JsonObject body = JsonParser.parseReader(req.getReader()).getAsJsonObject();
        Tarefa tarefa = new Tarefa();
        tarefa.setTitulo(getString(body, "titulo", "Nova tarefa de inercia"));
        tarefa.setDescricao(getString(body, "descricao", ""));
        tarefa.setStatus(parseStatus(body));
        tarefa.setDataCriacao(LocalDate.now());
        LocalDate prazo = parsePrazo(body, null);
        if (prazo == null) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Informe a data de prazo da tarefa.");
            return;
        }
        tarefa.setDataPrazo(prazo);
        tarefa.setCategoria(parseCategoria(body));
        tarefa.setXingamentos(parseXingamentos(body));
        dao.salvar(tarefa);
        resp.setStatus(HttpServletResponse.SC_CREATED);
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String pathInfo = req.getPathInfo();
        JsonObject body = JsonParser.parseReader(req.getReader()).getAsJsonObject();
        Integer id = parseId(pathInfo, body);
        if (id == null) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "ID da tarefa nao informado.");
            return;
        }

        if (body.has("status") && body.size() == 1) {
            dao.atualizarStatus(id, StatusTarefa.valueOf(body.get("status").getAsString()));
        } else {
            Tarefa tarefa = new Tarefa();
            tarefa.setId(id);
            if (body.has("titulo")) {
                tarefa.setTitulo(body.get("titulo").getAsString());
            }
            if (body.has("descricao")) {
                tarefa.setDescricao(body.get("descricao").getAsString());
            }
            if (body.has("dataPrazo")) {
                tarefa.setDataPrazo(parsePrazo(body, null));
            }
            if (body.has("status")) {
                tarefa.setStatus(StatusTarefa.valueOf(body.get("status").getAsString()));
            }
            if (body.has("categoria")) {
                tarefa.setCategoria(parseCategoria(body));
            }
            if (body.has("xingamentos")) {
                tarefa.setXingamentos(parseXingamentos(body));
            }
            dao.atualizar(tarefa);
        }
        resp.setStatus(HttpServletResponse.SC_OK);
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String pathInfo = req.getPathInfo();
        if (pathInfo == null || pathInfo.length() <= 1) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "ID da tarefa nao informado.");
            return;
        }
        dao.deletar(Integer.parseInt(pathInfo.substring(1)));
        resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
    }

    private Integer parseId(String pathInfo, JsonObject body) {
        if (pathInfo != null && pathInfo.length() > 1) {
            return Integer.parseInt(pathInfo.substring(1));
        }
        if (body.has("id")) {
            return body.get("id").getAsInt();
        }
        return null;
    }

    private String getString(JsonObject json, String field, String defaultValue) {
        return json.has(field) && !json.get(field).isJsonNull()
                ? json.get(field).getAsString()
                : defaultValue;
    }

    private StatusTarefa parseStatus(JsonObject body) {
        if (!body.has("status") || body.get("status").isJsonNull()) {
            return StatusTarefa.BACKLOG;
        }
        return StatusTarefa.valueOf(body.get("status").getAsString());
    }

    private LocalDate parsePrazo(JsonObject body, LocalDate defaultValue) {
        if (!body.has("dataPrazo") || body.get("dataPrazo").isJsonNull()) {
            return defaultValue;
        }
        String valor = body.get("dataPrazo").getAsString();
        if (valor == null || valor.isBlank()) {
            return defaultValue;
        }
        String normalizado = valor.trim();
        if (normalizado.length() > 10) {
            normalizado = normalizado.substring(0, 10);
        }
        return LocalDate.parse(normalizado);
    }

    private Categoria parseCategoria(JsonObject body) {
        if (!body.has("categoria") || body.get("categoria").isJsonNull()) {
            return null;
        }
        JsonObject item = body.getAsJsonObject("categoria");
        Categoria categoria = new Categoria();
        if (item.has("id")) {
            categoria.setId(item.get("id").getAsInt());
        }
        if (item.has("nome")) {
            categoria.setNome(item.get("nome").getAsString());
        }
        return categoria;
    }

    private List<Xingamento> parseXingamentos(JsonObject body) {
        List<Xingamento> xingamentos = new ArrayList<>();
        if (!body.has("xingamentos") || body.get("xingamentos").isJsonNull()) {
            return xingamentos;
        }
        JsonArray jsonXingamentos = body.getAsJsonArray("xingamentos");
        for (int i = 0; i < jsonXingamentos.size(); i++) {
            JsonObject item = jsonXingamentos.get(i).getAsJsonObject();
            Xingamento xingamento = new Xingamento();
            if (item.has("id")) {
                xingamento.setId(item.get("id").getAsInt());
            }
            if (item.has("mensagem")) {
                xingamento.setMensagem(item.get("mensagem").getAsString());
            }
            if (item.has("tipo")) {
                xingamento.setTipo(item.get("tipo").getAsString());
            }
            xingamentos.add(xingamento);
        }
        return xingamentos;
    }

    private record CategoriaDTO(Integer id, String nome) {
    }

    private record XingamentoDTO(Integer id, String mensagem, String tipo) {
    }

    private record TarefaCardResponse(
            Integer id,
            String titulo,
            String descricao,
            String status,
            String dataPrazo,
            CategoriaDTO categoria,
            List<XingamentoDTO> xingamentos
    ) {
        static TarefaCardResponse from(Tarefa tarefa) {
            CategoriaDTO categoria = tarefa.getCategoria() != null
                    ? new CategoriaDTO(tarefa.getCategoria().getId(), tarefa.getCategoria().getNome())
                    : null;
            List<XingamentoDTO> xingamentos = tarefa.getXingamentos().stream()
                    .map(x -> new XingamentoDTO(x.getId(), x.getMensagem(), x.getTipo()))
                    .toList();
            return new TarefaCardResponse(
                    tarefa.getId(),
                    tarefa.getTitulo(),
                    tarefa.getDescricao(),
                    tarefa.getStatus().name(),
                    tarefa.getDataPrazo() != null ? tarefa.getDataPrazo().toString() : null,
                    categoria,
                    xingamentos
            );
        }
    }
}
