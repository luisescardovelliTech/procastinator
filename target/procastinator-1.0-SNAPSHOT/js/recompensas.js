const API = {
    recompensas: `${APP_CONTEXT}/api/recompensas`
};

let recompensasLista = [];
let filtroAtual = "";

function escapeHtml(text) {
    return $("<div>").text(text || "").html();
}

function showToast(message) {
    const $toast = $("#toast-status");
    $toast.text(message).addClass("show");
    setTimeout(() => $toast.removeClass("show"), 2300);
}

function normalizarTexto(valor) {
    return (valor || "")
        .toString()
        .normalize("NFD")
        .replace(/[\u0300-\u036f]/g, "")
        .toLowerCase()
        .trim();
}

function formatarDataHora(dataHora) {
    if (!dataHora) {
        return "Agora";
    }
    const data = new Date(dataHora);
    if (Number.isNaN(data.getTime())) {
        return dataHora;
    }
    return data.toLocaleString("pt-BR", {
        day: "2-digit",
        month: "2-digit",
        hour: "2-digit",
        minute: "2-digit"
    });
}

function atualizarContadorRegistros(quantidadeVisivel, quantidadeTotal) {
    const texto = quantidadeVisivel === quantidadeTotal
        ? `${quantidadeTotal}`
        : `${quantidadeVisivel}/${quantidadeTotal}`;
    $("#count-recompensas").text(texto);
}

function atualizarTotalPontos(lista) {
    const total = (lista || []).reduce(function (soma, recompensa) {
        return soma + Number(recompensa.pontos || 0);
    }, 0);
    $("#total-pontos").text(total);
}

function filtrarRecompensas() {
    const termo = normalizarTexto(filtroAtual);
    if (!termo) {
        return recompensasLista;
    }

    return recompensasLista.filter(function (recompensa) {
        const alvo = [
            recompensa.titulo,
            recompensa.descricao,
            recompensa.tarefaTitulo,
            recompensa.id != null ? `recompensa #${recompensa.id}` : ""
        ].map(normalizarTexto).join(" ");

        return alvo.includes(termo);
    });
}

function buildRecompensaCard(recompensa) {
    return `
        <article class="aviso-card" data-id="${recompensa.id}">
            <header class="d-flex justify-content-between align-items-start gap-3 mb-2">
                <div class="d-flex align-items-center gap-2 flex-wrap">
                    <span class="aviso-badge">RECOMPENSA #${escapeHtml(recompensa.id)}</span>
                    <span class="badge text-bg-success">+${escapeHtml(recompensa.pontos || 0)} pts</span>
                </div>
                <span class="small text-secondary">${escapeHtml(formatarDataHora(recompensa.dataConquista))}</span>
            </header>
            <h3 class="h6 mb-1">${escapeHtml(recompensa.titulo || "Sem titulo")}</h3>
            <p class="mb-2">${escapeHtml(recompensa.descricao || "")}</p>
        </article>`;
}

function renderizarRecompensas() {
    const recompensasFiltradas = filtrarRecompensas();
    atualizarContadorRegistros(recompensasFiltradas.length, recompensasLista.length);
    atualizarTotalPontos(recompensasFiltradas);

    if (!recompensasFiltradas.length) {
        const mensagem = filtroAtual.trim()
            ? "Nenhuma recompensa encontrada para este filtro."
            : "Nenhuma recompensa cadastrada ainda.";
        $("#lista-recompensas").html(`<div class='text-secondary aviso-empty'>${mensagem}</div>`);
        return;
    }

    $("#lista-recompensas").html(recompensasFiltradas.map(buildRecompensaCard).join(""));
}

function carregarRecompensas() {
    $.get(API.recompensas)
        .done(function (recompensas) {
            recompensasLista = Array.isArray(recompensas) ? recompensas : [];
            renderizarRecompensas();
        })
        .fail(() => showToast("Falha ao carregar recompensas."));
}

$(function () {
    carregarRecompensas();

    $("#filtro-recompensas").on("input", function () {
        filtroAtual = $(this).val();
        renderizarRecompensas();
    });
});



