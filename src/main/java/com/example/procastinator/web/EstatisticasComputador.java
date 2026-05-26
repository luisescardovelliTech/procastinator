package com.example.procastinator.web;

import com.example.procastinator.model.Recompensa;
import com.example.procastinator.model.StatusTarefa;
import com.example.procastinator.model.Tarefa;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public final class EstatisticasComputador {

    private EstatisticasComputador() {
    }

    public static DashboardPayload build(List<Tarefa> tarefas, List<Recompensa> recompensas) {
        List<Tarefa> lista = tarefas != null ? tarefas : List.of();
        List<Recompensa> recs = recompensas != null ? recompensas : List.of();
        Metricas metricas = calcularMetricas(lista, recs);
        ChartSeries status = agruparStatus(lista);
        ChartSeries categorias = agruparCategorias(lista);
        List<RankingPrazoItem> rankingPrazos = calcularRankingPrazos(lista);
        return new DashboardPayload(metricas, status, categorias, rankingPrazos);
    }

    private static Integer diasAtePrazo(LocalDate dataPrazo) {
        if (dataPrazo == null) {
            return null;
        }
        return (int) ChronoUnit.DAYS.between(LocalDate.now(), dataPrazo);
    }

    private static Metricas calcularMetricas(List<Tarefa> lista, List<Recompensa> recompensas) {
        int pendentes = (int) lista.stream()
                .filter(t -> t.getStatus() != StatusTarefa.QUASE_FIZ)
                .count();
        int atrasadas = (int) lista.stream()
                .filter(t -> {
                    Integer dias = diasAtePrazo(t.getDataPrazo());
                    return dias != null && dias < 0 && t.getStatus() != StatusTarefa.QUASE_FIZ;
                })
                .count();
        int scorePontos = recompensas.stream()
                .mapToInt(r -> r.getPontos() != null ? r.getPontos() : 0)
                .sum();
        return new Metricas(lista.size(), pendentes, atrasadas, scorePontos);
    }

    private static ChartSeries agruparStatus(List<Tarefa> lista) {
        int backlog = 0;
        int esperando = 0;
        int quase = 0;
        for (Tarefa t : lista) {
            StatusTarefa s = t.getStatus() != null ? t.getStatus() : StatusTarefa.BACKLOG;
            switch (s) {
                case BACKLOG -> backlog++;
                case ESPERANDO -> esperando++;
                case QUASE_FIZ -> quase++;
            }
        }
        return new ChartSeries(
                List.of("BACKLOG", "ESPERANDO", "QUASE_FIZ"),
                List.of(backlog, esperando, quase)
        );
    }

    private static ChartSeries agruparCategorias(List<Tarefa> lista) {
        Map<String, Integer> mapa = new HashMap<>();
        for (Tarefa t : lista) {
            String nome = (t.getCategoria() != null && t.getCategoria().getNome() != null && !t.getCategoria().getNome().isBlank())
                    ? t.getCategoria().getNome()
                    : "Geral";
            mapa.merge(nome, 1, Integer::sum);
        }
        List<Map.Entry<String, Integer>> pares = mapa.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(6)
                .collect(Collectors.toList());
        return new ChartSeries(
                pares.stream().map(Map.Entry::getKey).collect(Collectors.toList()),
                pares.stream().map(Map.Entry::getValue).collect(Collectors.toList())
        );
    }

    private static List<RankingPrazoItem> calcularRankingPrazos(List<Tarefa> lista) {
        return lista.stream()
                .filter(t -> t.getStatus() != StatusTarefa.QUASE_FIZ)
                .map(t -> {
                    LocalDate prazo = t.getDataPrazo();
                    Integer dias = diasAtePrazo(prazo);
                    String prazoTexto = prazo != null ? prazo.toString() : "Sem prazo";
                    return new RankingPrazoItem(
                            t.getTitulo() != null ? t.getTitulo() : "Sem titulo",
                            t.getStatus() != null ? t.getStatus().name() : "BACKLOG",
                            prazoTexto,
                            dias
                    );
                })
                .sorted(Comparator
                        .comparing((RankingPrazoItem r) -> r.diasRestantes() == null ? 1 : 0)
                        .thenComparing(r -> r.diasRestantes() != null ? r.diasRestantes() : Integer.MAX_VALUE)
                        .thenComparing(RankingPrazoItem::titulo))
                .limit(5)
                .collect(Collectors.toList());
    }

    public record Metricas(int totalTarefas, int pendentes, int totalDesculpas, int scorePontos) {
    }

    public record ChartSeries(List<String> labels, List<Integer> valores) {
    }

    public record RankingPrazoItem(String titulo, String status, String prazoTexto, Integer diasRestantes) {
    }

    public record DashboardPayload(
            Metricas metricas,
            ChartSeries status,
            ChartSeries categorias,
            List<RankingPrazoItem> rankingPrazos
    ) {
    }
}
