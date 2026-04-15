const API = {
    tarefas: `${APP_CONTEXT}/api/tarefas`
};

const chartRefs = {
    status: null,
    categorias: null,
    tendencia: null
};

function showToast(message) {
    const $toast = $("#toast-status");
    $toast.text(message).addClass("show");
    setTimeout(function () {
        $toast.removeClass("show");
    }, 2300);
}

function escapeHtml(text) {
    return $("<div>").text(text || "").html();
}

function traduzirStatus(status) {
    if (status === "BACKLOG") {
        return "Backlog";
    }
    if (status === "ESPERANDO") {
        return "Esperando";
    }
    if (status === "QUASE_FIZ") {
        return "Quase Fiz";
    }
    return "Nao informado";
}

function parseDataIso(dataIso) {
    if (!dataIso) {
        return null;
    }
    const data = new Date(`${String(dataIso).substring(0, 10)}T00:00:00`);
    return Number.isNaN(data.getTime()) ? null : data;
}

function hojeSemHora() {
    const agora = new Date();
    return new Date(agora.getFullYear(), agora.getMonth(), agora.getDate());
}

function diasAtePrazo(dataIso) {
    const prazo = parseDataIso(dataIso);
    if (!prazo) {
        return null;
    }
    const diffMs = prazo.getTime() - hojeSemHora().getTime();
    return Math.floor(diffMs / 86400000);
}

function calcularMetricas(tarefas) {
    const listaTarefas = Array.isArray(tarefas) ? tarefas : [];

    const pendentes = listaTarefas.filter(function (tarefa) {
        return tarefa.status !== "QUASE_FIZ";
    }).length;

    const atrasadas = listaTarefas.filter(function (tarefa) {
        const dias = diasAtePrazo(tarefa.dataPrazo);
        return dias != null && dias < 0 && tarefa.status !== "QUASE_FIZ";
    }).length;

    const reincidencia = listaTarefas.length
        ? Math.round((pendentes / listaTarefas.length) * 100)
        : 0;

    return {
        totalTarefas: listaTarefas.length,
        pendentes,
        totalDesculpas: atrasadas,
        reincidencia
    };
}

function agruparStatus(tarefas) {
    const base = {
        BACKLOG: 0,
        ESPERANDO: 0,
        QUASE_FIZ: 0
    };

    (Array.isArray(tarefas) ? tarefas : []).forEach(function (tarefa) {
        const chave = tarefa.status || "BACKLOG";
        if (base[chave] == null) {
            base[chave] = 0;
        }
        base[chave] += 1;
    });

    return {
        labels: ["BACKLOG", "ESPERANDO", "QUASE_FIZ"],
        valores: [base.BACKLOG || 0, base.ESPERANDO || 0, base.QUASE_FIZ || 0]
    };
}

function agruparCategorias(tarefas) {
    const mapa = {};
    (Array.isArray(tarefas) ? tarefas : []).forEach(function (tarefa) {
        const nome = tarefa.categoria && tarefa.categoria.nome
            ? tarefa.categoria.nome
            : "Geral";
        mapa[nome] = (mapa[nome] || 0) + 1;
    });

    const pares = Object.keys(mapa)
        .map(function (chave) {
            return { label: chave, valor: mapa[chave] };
        })
        .sort(function (a, b) {
            return b.valor - a.valor;
        })
        .slice(0, 6);

    return {
        labels: pares.map(function (item) { return item.label; }),
        valores: pares.map(function (item) { return item.valor; })
    };
}

function agruparTendencia(tarefas) {
    const mapa = {};
    (Array.isArray(tarefas) ? tarefas : []).forEach(function (item) {
        const dataBase = item.dataPrazo ? String(item.dataPrazo).substring(0, 10) : "Sem prazo";
        if (!dataBase) {
            return;
        }
        mapa[dataBase] = (mapa[dataBase] || 0) + 1;
    });

    const dias = Object.keys(mapa).sort(function (a, b) {
        if (a === "Sem prazo") {
            return 1;
        }
        if (b === "Sem prazo") {
            return -1;
        }
        return a.localeCompare(b);
    });
    const ultimos = dias.slice(-12);
    return {
        labels: ultimos,
        valores: ultimos.map(function (dia) { return mapa[dia]; })
    };
}

function calcularRankingReincidencia(tarefas) {
    return (Array.isArray(tarefas) ? tarefas : [])
        .map(function (tarefa) {
            const dias = diasAtePrazo(tarefa.dataPrazo);
            const atrasoPeso = dias != null && dias < 0 ? Math.abs(dias) : 0;
            const statusPeso = tarefa.status === "BACKLOG" ? 3 : (tarefa.status === "ESPERANDO" ? 2 : 0);
            const score = statusPeso + atrasoPeso;
            return {
                titulo: tarefa.titulo || "Sem titulo",
                total: score,
                diasAtraso: atrasoPeso
            };
        })
        .filter(function (item) {
            return item.total > 0;
        })
        .sort(function (a, b) {
            return b.total - a.total;
        })
        .slice(0, 5);
}

function renderKpis(metricas) {
    $("#kpi-total-tarefas").text(metricas.totalTarefas);
    $("#kpi-pendentes").text(metricas.pendentes);
    $("#kpi-total-desculpas").text(metricas.totalDesculpas);
    $("#kpi-reincidencia").text(`${metricas.reincidencia}%`);
}

function destroyChart(nome) {
    if (chartRefs[nome]) {
        chartRefs[nome].destroy();
        chartRefs[nome] = null;
    }
}

function renderStatusChart(dataset) {
    const ctx = document.getElementById("chart-status");
    if (!ctx || typeof Chart === "undefined") {
        return;
    }

    destroyChart("status");
    chartRefs.status = new Chart(ctx, {
        type: "bar",
        data: {
            labels: dataset.labels.map(traduzirStatus),
            datasets: [{
                label: "Quantidade",
                data: dataset.valores,
                borderRadius: 8,
                backgroundColor: ["#2563eb", "#60a5fa", "#ef4444"]
            }]
        },
        options: {
            maintainAspectRatio: false,
            plugins: {
                legend: { display: false }
            },
            scales: {
                x: { ticks: { color: "#334155" }, grid: { color: "#e2e8f0" } },
                y: { ticks: { color: "#334155", precision: 0 }, grid: { color: "#e2e8f0" } }
            }
        }
    });
}

function renderCategoriaChart(dataset) {
    const ctx = document.getElementById("chart-categorias");
    if (!ctx || typeof Chart === "undefined") {
        return;
    }

    destroyChart("categorias");
    chartRefs.categorias = new Chart(ctx, {
        type: "doughnut",
        data: {
            labels: dataset.labels,
            datasets: [{
                data: dataset.valores,
                backgroundColor: ["#2563eb", "#3b82f6", "#60a5fa", "#93c5fd", "#ef4444", "#f87171"],
                borderColor: "#ffffff",
                borderWidth: 2
            }]
        },
        options: {
            maintainAspectRatio: false,
            plugins: {
                legend: {
                    position: "bottom",
                    labels: {
                        color: "#334155"
                    }
                }
            }
        }
    });
}

function renderTendenciaChart(dataset) {
    const ctx = document.getElementById("chart-tendencia");
    if (!ctx || typeof Chart === "undefined") {
        return;
    }

    destroyChart("tendencia");
    chartRefs.tendencia = new Chart(ctx, {
        type: "line",
        data: {
            labels: dataset.labels,
            datasets: [{
                label: "Tarefas por prazo",
                data: dataset.valores,
                tension: 0.25,
                borderWidth: 2,
                borderColor: "#2563eb",
                backgroundColor: "rgba(37, 99, 235, 0.15)",
                fill: true
            }]
        },
        options: {
            maintainAspectRatio: false,
            plugins: {
                legend: {
                    labels: { color: "#334155" }
                }
            },
            scales: {
                x: { ticks: { color: "#334155" }, grid: { color: "#e2e8f0" } },
                y: { ticks: { color: "#334155", precision: 0 }, grid: { color: "#e2e8f0" } }
            }
        }
    });
}

function renderRanking(itens) {
    const $ranking = $("#ranking-reincidencia");
    if (!itens.length) {
        $ranking.html("<li class='culpa-vazio'>Sem registros suficientes.</li>");
        return;
    }

    const html = itens.map(function (item) {
        const detalhe = item.diasAtraso > 0 ? ` (${item.diasAtraso}d atraso)` : "";
        return `<li>${escapeHtml(item.titulo)} <span class="culpa-ranking-tag">score ${item.total}${detalhe}</span></li>`;
    });
    $ranking.html(html.join(""));
}

function gerarInsights(metricas, ranking, tendencia) {
    const frases = [];
    frases.push(`Pendencias abertas: ${metricas.pendentes} de ${metricas.totalTarefas} tarefas.`);
    frases.push(`Tarefas atrasadas: ${metricas.totalDesculpas}.`);

    if (ranking.length) {
        frases.push(`Prioridade critica: "${ranking[0].titulo}" com score ${ranking[0].total}.`);
    }

    if (tendencia.valores.length >= 2) {
        const atual = tendencia.valores[tendencia.valores.length - 1];
        const anterior = tendencia.valores[tendencia.valores.length - 2];
        if (atual > anterior) {
            frases.push("A carga de tarefas por prazo aumentou no ultimo ponto analisado.");
        } else if (atual < anterior) {
            frases.push("A carga de tarefas por prazo caiu no ultimo ponto analisado.");
        } else {
            frases.push("A carga de tarefas por prazo ficou estavel no ultimo ponto analisado.");
        }
    }

    if (!frases.length) {
        frases.push("Adicione mais dados para gerar insights automaticos.");
    }
    return frases;
}

function renderInsights(insights) {
    const html = insights.map(function (frase) {
        return `<li>${escapeHtml(frase)}</li>`;
    });
    $("#insights-lista").html(html.join(""));
}

function carregarDashboard() {
    return $.get(API.tarefas)
        .done(function (tarefas) {
            const listaTarefas = Array.isArray(tarefas) ? tarefas : [];

            const metricas = calcularMetricas(listaTarefas);
            const statusData = agruparStatus(listaTarefas);
            const categoriaData = agruparCategorias(listaTarefas);
            const tendenciaData = agruparTendencia(listaTarefas);
            const ranking = calcularRankingReincidencia(listaTarefas);
            const insights = gerarInsights(metricas, ranking, tendenciaData);


            renderKpis(metricas);
            renderStatusChart(statusData);
            renderCategoriaChart(categoriaData);
            renderTendenciaChart(tendenciaData);
            renderRanking(ranking);
            renderInsights(insights);
        })
        .fail(function () {
            showToast("Falha ao carregar dados da lista de tarefas.");
        });
}

$(function () {
    carregarDashboard();

    $("#btn-recarregar").on("click", function () {
        carregarDashboard().done(function () {
            showToast("Dashboard atualizada.");
        });
    });
});

