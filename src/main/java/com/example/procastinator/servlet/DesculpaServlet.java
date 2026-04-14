package com.example.procastinator.servlet;

import com.example.procastinator.dao.HistoricoDAO;
import com.example.procastinator.model.Historico;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;

@WebServlet("/api/desculpas/*")
public class DesculpaServlet extends HttpServlet {
	private final HistoricoDAO dao = new HistoricoDAO();
	private final Gson gson = new Gson();

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		resp.setContentType("application/json; charset=UTF-8");
		List<DesculpaResponse> payload = dao.listarDesculpas().stream()
				.map(DesculpaResponse::from)
				.toList();
		resp.getWriter().write(gson.toJson(payload));
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		JsonObject body = JsonParser.parseReader(req.getReader()).getAsJsonObject();
		String comentario = body.has("comentario") && !body.get("comentario").isJsonNull()
				? body.get("comentario").getAsString().trim()
				: "";
		if (comentario.isBlank()) {
			resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Comentario da desculpa e obrigatorio.");
			return;
		}

		Integer tarefaId = body.has("tarefaId") && !body.get("tarefaId").isJsonNull()
				? body.get("tarefaId").getAsInt()
				: null;
		Integer nivelEficacia = body.has("nivelEficacia") && !body.get("nivelEficacia").isJsonNull()
				? body.get("nivelEficacia").getAsInt()
				: null;

		Historico salvo = dao.salvarDesculpa(tarefaId, comentario, nivelEficacia);
		resp.setStatus(HttpServletResponse.SC_CREATED);
		resp.setContentType("application/json; charset=UTF-8");
		resp.getWriter().write(gson.toJson(DesculpaResponse.from(salvo)));
	}

	@Override
	protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		Integer id = extrairId(req);
		if (id == null) {
			resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Id da desculpa invalido.");
			return;
		}

		JsonObject body = JsonParser.parseReader(req.getReader()).getAsJsonObject();
		String comentario = body.has("comentario") && !body.get("comentario").isJsonNull()
				? body.get("comentario").getAsString().trim()
				: "";
		if (comentario.isBlank()) {
			resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Comentario da desculpa e obrigatorio.");
			return;
		}

		Integer tarefaId = body.has("tarefaId") && !body.get("tarefaId").isJsonNull()
				? body.get("tarefaId").getAsInt()
				: null;
		Integer nivelEficacia = body.has("nivelEficacia") && !body.get("nivelEficacia").isJsonNull()
				? body.get("nivelEficacia").getAsInt()
				: null;

		Historico atualizado = dao.atualizarDesculpa(id, tarefaId, comentario, nivelEficacia);
		if (atualizado == null) {
			resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Desculpa nao encontrada.");
			return;
		}

		resp.setContentType("application/json; charset=UTF-8");
		resp.getWriter().write(gson.toJson(DesculpaResponse.from(atualizado)));
	}

	@Override
	protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		Integer id = extrairId(req);
		if (id == null) {
			resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Id da desculpa invalido.");
			return;
		}

		boolean removido = dao.excluirDesculpa(id);
		if (!removido) {
			resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Desculpa nao encontrada.");
			return;
		}

		resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
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

	private record DesculpaResponse(
			Integer id,
			String dataHora,
			String comentario,
			Integer nivelEficacia,
			Integer tarefaId,
			String tarefaTitulo,
			String usuarioNome
	) {
		static DesculpaResponse from(Historico historico) {
			return new DesculpaResponse(
					historico.getId(),
					historico.getDataHora() != null ? historico.getDataHora().toString() : null,
					historico.getComentario(),
					historico.getNivelEficacia(),
					historico.getTarefa() != null ? historico.getTarefa().getId() : null,
					historico.getTarefa() != null ? historico.getTarefa().getTitulo() : "Sem tarefa associada",
					historico.getUsuario() != null ? historico.getUsuario().getNome() : "Setor de Inercia"
			);
		}
	}
}


