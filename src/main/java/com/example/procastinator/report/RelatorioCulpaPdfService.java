package com.example.procastinator.report;

import com.example.procastinator.web.EstatisticasComputador.ChartSeries;
import com.example.procastinator.web.EstatisticasComputador.DashboardPayload;
import com.example.procastinator.web.EstatisticasComputador.Metricas;
import com.example.procastinator.web.EstatisticasComputador.RankingPrazoItem;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.export.JRPdfExporter;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimpleOutputStreamExporterOutput;
import net.sf.jasperreports.export.SimplePdfExporterConfiguration;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

public class RelatorioCulpaPdfService {

    private static final String TEMPLATE = "/reports/estatisticas_culpa.jrxml";

    public byte[] gerarPdf(DashboardPayload dashboard) throws Exception {
        JasperReport report = compilarRelatorio();
        Map<String, Object> params = montarParametros(dashboard);
        List<LinhaRankingRelatorio> linhas = montarLinhasRanking(dashboard.rankingPrazos());
        if (linhas.isEmpty()) {
            linhas.add(new LinhaRankingRelatorio(
                    "(nenhuma tarefa pendente)",
                    "-",
                    "-",
                    "O ranking esta de ferias. Crie tarefas para alimentar a culpa."
            ));
        }

        JRBeanCollectionDataSource ds = new JRBeanCollectionDataSource(linhas);
        JasperPrint print = JasperFillManager.fillReport(report, params, ds);

        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        JRPdfExporter exporter = new JRPdfExporter();
        exporter.setExporterInput(new SimpleExporterInput(print));
        exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(buffer));
        SimplePdfExporterConfiguration pdfConfig = new SimplePdfExporterConfiguration();
        pdfConfig.setCompressed(true);
        exporter.setConfiguration(pdfConfig);
        exporter.exportReport();
        return buffer.toByteArray();
    }

    private static JasperReport compilarRelatorio() throws Exception {
        try (InputStream in = RelatorioCulpaPdfService.class.getResourceAsStream(TEMPLATE)) {
            if (in == null) {
                throw new IllegalStateException("Template JRXML nao encontrado: " + TEMPLATE);
            }
            return JasperCompileManager.compileReport(in);
        }
    }

    private static Map<String, Object> montarParametros(DashboardPayload d) {
        Metricas m = d.metricas();
        Map<String, Object> params = new HashMap<>();
        params.put("DATA_EMISSAO", LocalDateTime.now().format(
                DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm", Locale.forLanguageTag("pt-BR"))));
        params.put("SUBTITULO_ZOEIRA", sortearSubtituloZoeira());
        params.put("TOTAL_TAREFAS", String.valueOf(m.totalTarefas()));
        params.put("PENDENTES", String.valueOf(m.pendentes()));
        params.put("ATRASADAS", String.valueOf(m.totalDesculpas()));
        params.put("SCORE_PONTOS", String.valueOf(m.scorePontos()));
        params.put("RESUMO_STATUS", formatarResumoStatus(d.status()));
        params.put("RESUMO_CATEGORIAS", formatarResumoCategorias(d.categorias()));
        params.put("PARECER_INERCIA", montarParecerInercia(m, d.rankingPrazos()));
        return params;
    }

    private static List<LinhaRankingRelatorio> montarLinhasRanking(List<RankingPrazoItem> ranking) {
        List<LinhaRankingRelatorio> linhas = new ArrayList<>();
        if (ranking == null) {
            return linhas;
        }
        for (RankingPrazoItem item : ranking) {
            linhas.add(new LinhaRankingRelatorio(
                    sanitizar(item.titulo()),
                    traduzirStatus(item.status()),
                    sanitizar(item.prazoTexto()),
                    formatarAlertaPrazo(item.diasRestantes())
            ));
        }
        return linhas;
    }

    private static String sanitizar(String texto) {
        if (texto == null) {
            return "";
        }
        return texto
                .replace('\u2022', '-')
                .replace('\u2014', '-')
                .replace('\u2013', '-');
    }

    private static String formatarResumoStatus(ChartSeries status) {
        if (status == null || status.labels().isEmpty()) {
            return "Nenhum dado. O quadro esta tao vazio que ecoa.";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < status.labels().size(); i++) {
            if (i > 0) {
                sb.append("\n");
            }
            sb.append("- ").append(traduzirStatus(status.labels().get(i)))
                    .append(": ").append(status.valores().get(i));
        }
        return sb.toString();
    }

    private static String formatarResumoCategorias(ChartSeries categorias) {
        if (categorias == null || categorias.labels().isEmpty()) {
            return "Tudo caiu em 'Geral'. Classificacao preguicosa padrao.";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < categorias.labels().size(); i++) {
            if (i > 0) {
                sb.append("\n");
            }
            sb.append("- ").append(sanitizar(categorias.labels().get(i)))
                    .append(": ").append(categorias.valores().get(i)).append(" violacoes");
        }
        return sb.toString();
    }

    private static String montarParecerInercia(Metricas m, List<RankingPrazoItem> ranking) {
        StringBuilder sb = new StringBuilder();
        sb.append("Conclusao: o contribuinte da Lista do Nao Fazer apresenta ");
        sb.append(m.pendentes()).append(" pendencia(s) ativas em um total de ");
        sb.append(m.totalTarefas()).append(" tarefa(s).\n\n");

        if (m.totalDesculpas() > 0) {
            sb.append("Alerta vermelho: ").append(m.totalDesculpas())
                    .append(" tarefa(s) ja passaram do prazo. Recomenda-se inventar desculpas criativas ")
                    .append("(consultar Log de Desculpas) ou fingir que o prazo era sugestao.\n\n");
        } else {
            sb.append("Milagre temporario: nenhuma tarefa atrasada no momento. Aproveite para procrastinar ")
                    .append("em paz antes que o universo perceba.\n\n");
        }

        sb.append("Score de pontos nas recompensas: ").append(m.scorePontos());
        if (m.scorePontos() < 0) {
            sb.append(" - nivel 'desastre produtivo certificado'.");
        } else if (m.scorePontos() == 0) {
            sb.append(" - neutro. Nem heroi, nem vilao da preguica.");
        } else {
            sb.append(" - ha esperanca, mas nao exagere no otimismo.");
        }
        sb.append("\n\n");

        if (ranking != null && !ranking.isEmpty()) {
            sb.append("Prioridade maxima: \"").append(sanitizar(ranking.get(0).titulo()))
                    .append("\" - tratar antes que vire lenda urbana do atraso.\n\n");
        }

        sb.append("Este documento nao substitui terapia, cafe forte ou um planner que voce nao vai abrir.");
        return sb.toString();
    }

    private static String formatarAlertaPrazo(Integer dias) {
        if (dias == null) {
            return "Sem prazo (liberdade perigosa)";
        }
        if (dias < 0) {
            return Math.abs(dias) + "d de atraso - modo emergencia";
        }
        if (dias == 0) {
            return "Vence HOJE - corra (ou nao)";
        }
        return "Vence em " + dias + "d - ainda da tempo de adiar";
    }

    private static String traduzirStatus(String status) {
        if (status == null) {
            return "Nao informado";
        }
        return switch (status.toUpperCase(Locale.ROOT)) {
            case "BACKLOG" -> "Backlog";
            case "ESPERANDO" -> "Esperando";
            case "QUASE_FIZ" -> "Quase Fiz";
            default -> status;
        };
    }

    private static String sortearSubtituloZoeira() {
        List<String> frases = List.of(
                "Este PDF comprova que voce adiou com competencia profissional.",
                "Relatorio auditado pelo Departamento de Desculpas Criativas.",
                "Certidao oficial de reincidencia na Lista do Nao Fazer.",
                "Mapa da culpa: pendencias sao estilo de vida, nao bug.",
                "Gerado enquanto voce pensava em comecar amanha."
        );
        return frases.get(ThreadLocalRandom.current().nextInt(frases.size()));
    }
}
