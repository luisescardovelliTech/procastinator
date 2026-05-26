package com.example.procastinator.web;

import com.example.procastinator.dao.XingamentoDAO;
import com.example.procastinator.model.Xingamento;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ThreadLocalRandom;

public final class IncentivoSorteador {

    private final XingamentoDAO dao = new XingamentoDAO();

    public Xingamento sortearElogio() {
        return sortearPorTipo("ELOGIO", null);
    }

    public Xingamento sortearXingamento(Integer ultimoId) {
        return sortearPorTipo("XINGAMENTO", ultimoId);
    }

    private Xingamento sortearPorTipo(String tipo, Integer ultimoId) {
        List<Xingamento> candidatos = new ArrayList<>();
        for (Xingamento item : dao.listarTodos()) {
            String t = item.getTipo() == null ? "" : item.getTipo().toUpperCase(Locale.ROOT);
            if (tipo.equals(t)) {
                candidatos.add(item);
            }
        }
        if (candidatos.isEmpty()) {
            return null;
        }
        if (candidatos.size() > 1 && ultimoId != null) {
            List<Xingamento> semRepeticao = candidatos.stream()
                    .filter(x -> !ultimoId.equals(x.getId()))
                    .toList();
            if (!semRepeticao.isEmpty()) {
                candidatos = new ArrayList<>(semRepeticao);
            }
        }
        int idx = ThreadLocalRandom.current().nextInt(candidatos.size());
        return candidatos.get(idx);
    }
}
