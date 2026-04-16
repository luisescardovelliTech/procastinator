const API = {
    tarefas: `${APP_CONTEXT}/api/tarefas`,
    recompensas: `${APP_CONTEXT}/api/recompensas`
};

const chartRefs = {
    status: null,
    categorias: null
};

function showToast(message) {
    const $toast = $("#toast-status");
    $toast.text(message).addClass("show");
    setTimeout(function () {
        $toast.removeClass("show");
    }, 2300);
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

function calcularMetricas(tarefas, recompensas) {
    const listaTarefas = Array.isArray(tarefas) ? tarefas : [];
    const listaRecompensas = Array.isArray(recompensas) ? recompensas : [];

    const pendentes = listaTarefas.filter(function (tarefa) {
        return tarefa.status !== "QUASE_FIZ";
    }).length;

    const atrasadas = listaTarefas.filter(function (tarefa) {
        const dias = diasAtePrazo(tarefa.dataPrazo);
        return dias != null && dias < 0 && tarefa.status !== "QUASE_FIZ";
    }).length;

    const scorePontos = listaRecompensas.reduce(function (total, item) {
        return total + Number(item.pontos || 0);
    }, 0);

    return {
        totalTarefas: listaTarefas.length,
        pendentes,
        totalDesculpas: atrasadas,
        scorePontos
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

function calcularRankingPrazos(tarefas) {
    return (Array.isArray(tarefas) ? tarefas : [])
        .map(function (tarefa) {
            const dataPrazo = parseDataIso(tarefa.dataPrazo);
            return {
                titulo: tarefa.titulo || "Sem titulo",
                status: tarefa.status || "BACKLOG",
                dataPrazo,
                prazoTexto: tarefa.dataPrazo ? String(tarefa.dataPrazo).substring(0, 10) : "Sem prazo",
                diasRestantes: diasAtePrazo(tarefa.dataPrazo)
            };
        })
        .filter(function (item) {
            return item.status !== "QUASE_FIZ";
        })
        .sort(function (a, b) {
            if (!a.dataPrazo && !b.dataPrazo) {
                return a.titulo.localeCompare(b.titulo);
            }
            if (!a.dataPrazo) {
                return 1;
            }
            if (!b.dataPrazo) {
                return -1;
            }
            return a.dataPrazo - b.dataPrazo;
        })
        .slice(0, 5);
}

function renderKpis(metricas) {
    $("#kpi-total-tarefas").text(metricas.totalTarefas);
    $("#kpi-pendentes").text(metricas.pendentes);
    $("#kpi-total-desculpas").text(metricas.totalDesculpas);
    $("#kpi-score-pontos").text(metricas.scorePontos);
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

function renderRankingPrazos(itens) {
    const $ranking = $("#ranking-prazos");
    if (!$ranking.length) {
        return;
    }
    if (!itens.length) {
        $ranking.html("<li class='culpa-vazio'>Sem tarefas pendentes no momento.</li>");
        return;
    }

    const html = itens.map(function (item) {
        let detalhe;
        if (item.diasRestantes == null) {
            detalhe = "sem prazo";
        } else if (item.diasRestantes < 0) {
            detalhe = `${Math.abs(item.diasRestantes)}d atrasada`;
        } else if (item.diasRestantes === 0) {
            detalhe = "vence hoje";
        } else {
            detalhe = `vence em ${item.diasRestantes}d`;
        }
        return `<li>${item.titulo} <span class="culpa-ranking-tag">${item.prazoTexto} - ${detalhe}</span></li>`;
    });

    $ranking.html(html.join(""));
}

function carregarDashboard() {
    return $.when($.get(API.tarefas), $.get(API.recompensas))
        .done(function (tarefasResp, recompensasResp) {
            const listaTarefas = Array.isArray(tarefasResp[0]) ? tarefasResp[0] : [];
            const listaRecompensas = Array.isArray(recompensasResp[0]) ? recompensasResp[0] : [];

            const metricas = calcularMetricas(listaTarefas, listaRecompensas);
            const statusData = agruparStatus(listaTarefas);
            const categoriaData = agruparCategorias(listaTarefas);
            const rankingPrazos = calcularRankingPrazos(listaTarefas);

            renderKpis(metricas);
            renderStatusChart(statusData);
            renderCategoriaChart(categoriaData);
            renderRankingPrazos(rankingPrazos);
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

