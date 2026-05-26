const chartRefs = { status: null, categorias: null };

function traduzirStatus(s) {
    if (s === "BACKLOG") return "Backlog";
    if (s === "ESPERANDO") return "Esperando";
    if (s === "QUASE_FIZ") return "Quase Fiz";
    return s;
}

function escapeHtml(t) {
    return $("<div>").text(t || "").html();
}

function renderKpis(m) {
    $("#kpi-total-tarefas").text(m.totalTarefas);
    $("#kpi-pendentes").text(m.pendentes);
    $("#kpi-total-desculpas").text(m.totalDesculpas);
    $("#kpi-score-pontos").text(m.scorePontos);
}

function destroyChart(n) {
    if (chartRefs[n]) { chartRefs[n].destroy(); chartRefs[n] = null; }
}

function renderStatusChart(ds) {
    const ctx = document.getElementById("chart-status");
    if (!ctx || typeof Chart === "undefined") return;
    destroyChart("status");
    chartRefs.status = new Chart(ctx, {
        type: "bar",
        data: {
            labels: ds.labels.map(traduzirStatus),
            datasets: [{ label: "Qtd", data: ds.valores, borderRadius: 8, backgroundColor: ["#2563eb", "#60a5fa", "#ef4444"] }]
        },
        options: { maintainAspectRatio: false, plugins: { legend: { display: false } },
            scales: { x: { ticks: { color: "#334155" } }, y: { ticks: { precision: 0, color: "#334155" } } } }
    });
}

function renderCategoriaChart(ds) {
    const ctx = document.getElementById("chart-categorias");
    if (!ctx || typeof Chart === "undefined") return;
    destroyChart("categorias");
    chartRefs.categorias = new Chart(ctx, {
        type: "doughnut",
        data: {
            labels: ds.labels,
            datasets: [{ data: ds.valores, backgroundColor: ["#2563eb", "#3b82f6", "#60a5fa", "#93c5fd", "#ef4444", "#f87171"], borderWidth: 2, borderColor: "#fff" }]
        },
        options: { maintainAspectRatio: false, plugins: { legend: { position: "bottom" } } }
    });
}

function renderRankingPrazos(itens) {
    const $r = $("#ranking-prazos");
    if (!itens || !itens.length) {
        $r.html("<li class='culpa-vazio'>Sem tarefas pendentes no momento.</li>");
        return;
    }
    $r.html(itens.map(function (item) {
        let detalhe;
        if (item.diasRestantes == null) detalhe = "sem prazo";
        else if (item.diasRestantes < 0) detalhe = Math.abs(item.diasRestantes) + "d atrasada";
        else if (item.diasRestantes === 0) detalhe = "vence hoje";
        else detalhe = "vence em " + item.diasRestantes + "d";
        return "<li>" + escapeHtml(item.titulo) + " <span class='culpa-ranking-tag'>" + escapeHtml(item.prazoTexto) + " - " + detalhe + "</span></li>";
    }).join(""));
}

$(function () {
    const el = document.getElementById("dashboard-data");
    if (!el || !el.textContent.trim()) return;
    const D = JSON.parse(el.textContent);
    renderKpis(D.metricas);
    renderStatusChart(D.status);
    renderCategoriaChart(D.categorias);
    renderRankingPrazos(D.rankingPrazos);
});
