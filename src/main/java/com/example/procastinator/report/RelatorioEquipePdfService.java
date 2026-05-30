package com.example.procastinator.report;

import com.example.procastinator.model.Equipe;
import com.example.procastinator.model.Tarefa;
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

public class RelatorioEquipePdfService {

    private static final String TEMPLATE = "/reports/equipe_relatorio.jrxml";

    public byte[] gerarPdf(Equipe equipe, int totalMembros, DashboardPayload dashboard, List<Tarefa> tarefas) throws Exception {
        JasperReport report = compilarRelatorio();
        Map<String, Object> params = montarParametros(equipe, totalMembros, dashboard);
        List<LinhaTarefaEquipeRelatorio> linhas = montarLinhas(tarefas, dashboard.rankingPrazos());
        if (linhas.isEmpty()) {
            linhas.add(new LinhaTarefaEquipeRelatorio(
                    "(nenhuma tarefa na equipe)",
                    "-",
                    "-",
                    "-",
                    "O kanban coletivo esta de ferias. Crie tarefas para alimentar a culpa em grupo."
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
        try (InputStream in = RelatorioEquipePdfService.class.getResourceAsStream(TEMPLATE)) {
            if (in == null) {
                throw new IllegalStateException("Template JRXML nao encontrado: " + TEMPLATE);
            }
            return JasperCompileManager.compileReport(in);
        }
    }

    private static Map<String, Object> montarParametros(Equipe equipe, int totalMembros, DashboardPayload d) {
        Metricas m = d.metricas();
        Map<String, Object> params = new HashMap<>();
        params.put("DATA_EMISSAO", LocalDateTime.now().format(
                DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm", Locale.forLanguageTag("pt-BR"))));
        params.put("NOME_EQUIPE", equipe != null && equipe.getNome() != null ? equipe.getNome() : "Equipe");
        params.put("LIDER", equipe != null && equipe.getLider() != null && equipe.getLider().getNome() != null
                ? equipe.getLider().getNome() : "Nao informado");
        params.put("TOTAL_MEMBROS", String.valueOf(totalMembros));
        params.put("SUBTITULO_ZOEIRA", sortearSubtituloZoeira());
        params.put("TOTAL_TAREFAS", String.valueOf(m.totalTarefas()));
        params.put("PENDENTES", String.valueOf(m.pendentes()));
        params.put("ATRASADAS", String.valueOf(m.totalDesculpas()));
        params.put("SCORE_PONTOS", String.valueOf(m.scorePontos()));
        params.put("RESUMO_STATUS", formatarResumoStatus(d.status()));
        params.put("RESUMO_CATEGORIAS", formatarResumoCategorias(d.categorias()));
        params.put("PARECER_EQUIPE", montarParecerEquipe(equipe, m, d.rankingPrazos(), totalMembros));
        return params;
    }

    private static List<LinhaTarefaEquipeRelatorio> montarLinhas(List<Tarefa> tarefas, List<RankingPrazoItem> ranking) {
        Map<String, RankingPrazoItem> alertas = new HashMap<>();
        if (ranking != null) {
            for (RankingPrazoItem item : ranking) {
                alertas.put(item.titulo(), item);
            }
        }

        List<LinhaTarefaEquipeRelatorio> linhas = new ArrayList<>();
        if (tarefas == null) {
            return linhas;
        }
        for (Tarefa t : tarefas) {
            String titulo = t.getTitulo() != null ? t.getTitulo() : "Sem titulo";
            RankingPrazoItem rank = alertas.get(titulo);
            Integer dias = rank != null ? rank.diasRestantes() : null;
            linhas.add(new LinhaTarefaEquipeRelatorio(
                    sanitizar(titulo),
                    traduzirStatus(t.getStatus() != null ? t.getStatus().name() : "BACKLOG"),
                    t.getDataPrazo() != null ? t.getDataPrazo().toString() : "Sem prazo",
                    t.getResponsavel() != null && t.getResponsavel().getNome() != null
                            ? sanitizar(t.getResponsavel().getNome()) : "Nao atribuido",
                    rank != null ? formatarAlertaPrazo(dias) : "Fora do ranking de urgencia"
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
            return "Nenhum dado. O quadro coletivo esta tao vazio que ecoa.";
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
            return "Tudo caiu em 'Geral'. Classificacao preguicosa padrao da equipe.";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < categorias.labels().size(); i++) {
            if (i > 0) {
                sb.append("\n");
            }
            sb.append("- ").append(sanitizar(categorias.labels().get(i)))
                    .append(": ").append(categorias.valores().get(i)).append(" tarefa(s)");
        }
        return sb.toString();
    }

    private static String montarParecerEquipe(Equipe equipe, Metricas m, List<RankingPrazoItem> ranking, int totalMembros) {
        String nome = equipe != null && equipe.getNome() != null ? equipe.getNome() : "Equipe";
        StringBuilder sb = new StringBuilder();
        sb.append("Parecer coletivo da equipe \"").append(sanitizar(nome)).append("\": ");
        sb.append(totalMembros).append(" membro(s) compartilham ");
        sb.append(m.totalTarefas()).append(" tarefa(s), sendo ");
        sb.append(m.pendentes()).append(" ainda em aberto.\n\n");

        if (m.totalDesculpas() > 0) {
            sb.append("Alerta vermelho: ").append(m.totalDesculpas())
                    .append(" tarefa(s) atrasada(s). Recomenda-se reuniao de emergencia ")
                    .append("(ou mensagem no grupo fingindo que nao viram).\n\n");
        } else {
            sb.append("Milagre coletivo: nenhuma tarefa atrasada no momento. Aproveitem antes que alguem crie mais pendencias.\n\n");
        }

        sb.append("Score de pontos das recompensas vinculadas: ").append(m.scorePontos());
        if (m.scorePontos() < 0) {
            sb.append(" - nivel 'desastre produtivo certificado em grupo'.");
        } else if (m.scorePontos() == 0) {
            sb.append(" - neutro. Nem heroi, nem vilao da preguica coletiva.");
        } else {
            sb.append(" - ha esperanca, mas nao exagerem no otimismo em equipe.");
        }
        sb.append("\n\n");

        if (ranking != null && !ranking.isEmpty()) {
            sb.append("Prioridade maxima do squad: \"").append(sanitizar(ranking.get(0).titulo()))
                    .append("\" - tratar antes que vire pauta eterna de reuniao.\n\n");
        }

        sb.append("Este PDF nao substitui daily, stand-up ou cafe forte em conjunto.");
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
            return "Vence HOJE - corram (ou nao)";
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
                "Relatorio auditado pelo Departamento de Culpa Compartilhada.",
                "Certidao oficial de procrastinacao em equipe.",
                "Kanban coletivo: pendencias sao estilo de vida, nao bug.",
                "Gerado enquanto alguem diz 'deixa comigo' e ninguem faz.",
                "PDF aprovado pelo comite de desculpas criativas do squad."
        );
        return frases.get(ThreadLocalRandom.current().nextInt(frases.size()));
    }
}
