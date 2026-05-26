package com.example.procastinator.servlet;

import com.example.procastinator.dao.RecompensaDAO;
import com.example.procastinator.dao.TarefaDAO;
import com.example.procastinator.report.RelatorioCulpaPdfService;
import com.example.procastinator.web.EstatisticasComputador;
import com.example.procastinator.web.SessaoUsuario;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.logging.Level;
import java.util.logging.Logger;

@WebServlet("/estatisticas/pdf")
public class EstatisticasPdfServlet extends HttpServlet {

    private static final Logger LOG = Logger.getLogger(EstatisticasPdfServlet.class.getName());

    private final TarefaDAO tarefaDao = new TarefaDAO();
    private final RecompensaDAO recompensaDao = new RecompensaDAO();
    private final RelatorioCulpaPdfService relatorioService = new RelatorioCulpaPdfService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            Integer usuarioId = SessaoUsuario.obterId(req);
            var dashboard = EstatisticasComputador.build(
                    tarefaDao.listarPorUsuario(usuarioId),
                    recompensaDao.listarPorUsuario(usuarioId)
            );

            byte[] pdf = relatorioService.gerarPdf(dashboard);
            if (pdf == null || pdf.length == 0) {
                responderErro(resp, "O relatorio foi gerado vazio.");
                return;
            }

            String nomeArquivo = "procastinator-culpa-"
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
            LOG.log(Level.SEVERE, "Falha ao gerar PDF de estatisticas", ex);
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
                <h1 class="h4">Nao foi possivel gerar o PDF da Culpa</h1>
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
}
