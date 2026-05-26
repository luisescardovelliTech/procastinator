function showToast(message) {
    $("#toast-status").text(message).addClass("show");
    setTimeout(function () { $("#toast-status").removeClass("show"); }, 2300);
}

function montarEstrelas(n) {
    const cheias = Math.max(0, Math.min(5, Math.round((Number(n) || 0) / 2)));
    return "★".repeat(cheias) + "☆".repeat(5 - cheias);
}

function normalizarTexto(v) {
    return (v || "").toString().normalize("NFD").replace(/[\u0300-\u036f]/g, "").toLowerCase().trim();
}

function aplicarFiltro() {
    const termo = normalizarTexto($("#filtro-desculpas").val());
    const $cards = $("#lista-desculpas .desculpa-card");
    let visiveis = 0;
    $cards.each(function () {
        const match = !termo || normalizarTexto($(this).find(".desculpa-search-blob").text()).includes(termo);
        $(this).toggleClass("d-none", !match);
        if (match) visiveis++;
    });
    const total = $cards.length;
    $("#count-desculpas").text(visiveis === total ? String(total) : visiveis + "/" + total);
}

function aplicarFlash() {
    const toast = $("#server-flash-toast").text();
    const erro = $("#server-flash-erro").text();
    if (toast) showToast(toast);
    if (erro) showToast(erro);
    const t = new URLSearchParams(window.location.search).get("t");
    const mapa = { "1": "Desculpa arquivada.", "2": "Desculpa atualizada.", "3": "Desculpa excluida." };
    if (!toast && t && mapa[t]) showToast(mapa[t]);
}

$(function () {
    $(".desculpa-stars-preview").each(function () {
        $(this).text(montarEstrelas($(this).data("ef")));
    });
    aplicarFlash();
    $("#desculpa-eficacia").on("input", function () { $("#eficacia-valor").text($(this).val() + "/10"); });
    $("#filtro-desculpas").on("input", aplicarFiltro);
    $(document).on("click", ".btn-view-desculpa", function () {
        const $c = $(this).closest(".desculpa-card");
        $("#edit-desculpa-id, #excluir-desculpa-id").val($c.data("id"));
        $("#detalhe-desculpa-tarefa").val($c.data("tarefa-id") || "");
        $("#detalhe-desculpa-comentario").val($c.find(".desculpa-comentario-store").text());
        $("#detalhe-desculpa-eficacia").val($c.data("eficacia") || 5);
        bootstrap.Modal.getOrCreateInstance(document.getElementById("desculpaDetailsModal")).show();
    });
});
