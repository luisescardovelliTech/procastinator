package com.example.procastinator.servlet;

import com.example.procastinator.dao.EquipeDAO;
import com.example.procastinator.dao.RecompensaDAO;
import com.example.procastinator.dao.TarefaDAO;
import com.example.procastinator.model.Equipe;
import com.example.procastinator.model.Tarefa;
import com.example.procastinator.report.RelatorioEquipePdfService;
import com.example.procastinator.web.EstatisticasComputador;
import com.example.procastinator.web.SessaoUsuario;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@WebServlet("/equipes/pdf")
public class EquipePdfServlet extends HttpServlet {

    private static final Logger LOG = Logger.getLogger(EquipePdfServlet.class.getName());

    private final EquipeDAO equipeDao = new EquipeDAO();
    private final TarefaDAO tarefaDao = new TarefaDAO();
    private final RecompensaDAO recompensaDao = new RecompensaDAO();
    private final RelatorioEquipePdfService relatorioService = new RelatorioEquipePdfService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Integer usuarioId = SessaoUsuario.obterId(req);
        Integer equipeId = parseId(req.getParameter("equipeId"));
        if (equipeId == null) {
            responderErro(resp, "Informe o ID da equipe (equipeId).");
            return;
        }

        Equipe equipe = equipeDao.buscarPorIdEMembro(equipeId, usuarioId);
        if (equipe == null) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Sem acesso a esta equipe.");
            return;
        }

        try {
            List<Tarefa> tarefas = tarefaDao.listarPorEquipe(equipeId);
            var dashboard = EstatisticasComputador.build(
                    tarefas,
                    recompensaDao.listarPorEquipe(equipeId)
            );
            int totalMembros = equipeDao.listarMembros(equipeId).size();

            byte[] pdf = relatorioService.gerarPdf(equipe, totalMembros, dashboard, tarefas);
            if (pdf == null || pdf.length == 0) {
                responderErro(resp, "O relatorio foi gerado vazio.");
                return;
            }

            String slug = equipe.getNome() != null
                    ? equipe.getNome().replaceAll("[^a-zA-Z0-9]+", "-").toLowerCase()
                    : "equipe";
            String nomeArquivo = "procastinator-equipe-"
                    + slug + "-"
                    + LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE)
                    + ".pdf";

            resp.reset();
            resp.setBufferSize(pdf.length);
            resp.setContentType("application/pdf");
            resp.setContentLength(pdf.length);
            resp.setHeader("Content-Disposition", "attachment; filename=\"" + nomeArquivo + "\"");
            resp.getOutputStream().write(pdf);
            resp.getOutputStream().flush();
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, "Falha ao gerar PDF da equipe " + equipeId, ex);
            responderErro(resp, extrairMensagem(ex));
        }
    }

    private static void responderErro(HttpServletResponse resp, String mensagem) throws IOException {
        if (resp.isCommitted()) {
            return;
        }
        resp.reset();
        resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        resp.setContentType("text/html; charset=UTF-8");
        String corpo = """
                <!DOCTYPE html>
                <html lang="pt-BR"><head><meta charset="UTF-8"><title>Erro no PDF</title>
                <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css">
                </head><body class="p-4">
                <div class="alert alert-danger">
                <h1 class="h4">Nao foi possivel gerar o PDF da Equipe</h1>
                <p class="mb-2">%s</p>
                <a class="btn btn-outline-secondary btn-sm" href="javascript:history.back()">Voltar</a>
                </div></body></html>
                """.formatted(escapeHtml(mensagem));
        resp.getWriter().write(corpo);
    }

    private static String extrairMensagem(Exception ex) {
        Throwable causa = ex;
        while (causa.getCause() != null) {
            causa = causa.getCause();
        }
        String msg = causa.getMessage();
        return msg != null && !msg.isBlank() ? msg : ex.getClass().getSimpleName();
    }

    private static String escapeHtml(String texto) {
        return texto.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }

    private static Integer parseId(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        try {
            return Integer.parseInt(raw.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
