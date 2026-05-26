function showToast(message) {
    $("#toast-status").text(message).addClass("show");
    setTimeout(function () { $("#toast-status").removeClass("show"); }, 2300);
}

function normalizarTexto(v) {
    return (v || "").toString().normalize("NFD").replace(/[\u0300-\u036f]/g, "").toLowerCase().trim();
}

function aplicarFiltro() {
    const termo = normalizarTexto($("#filtro-avisos").val());
    const $cards = $("#lista-avisos .aviso-card");
    let visiveis = 0;
    $cards.each(function () {
        const match = !termo || normalizarTexto($(this).find(".aviso-search-blob").text()).includes(termo);
        $(this).toggleClass("d-none", !match);
        if (match) visiveis++;
    });
    const total = $cards.length;
    $("#count-avisos").text(visiveis === total ? String(total) : visiveis + "/" + total);
}

function aplicarFlash() {
    const toast = $("#server-flash-toast").text();
    const erro = $("#server-flash-erro").text();
    if (toast) showToast(toast);
    if (erro) {
        showToast(erro);
        if (erro.includes("vinculado")) {
            window.alert(erro);
        }
    }
    const t = new URLSearchParams(window.location.search).get("t");
    const mapa = { "1": "Frase cadastrada.", "2": "Frase atualizada.", "3": "Frase excluida." };
    if (!toast && t && mapa[t]) showToast(mapa[t]);
}

$(function () {
    aplicarFlash();
    $("#filtro-avisos").on("input", aplicarFiltro);
    $(document).on("click", ".btn-view-aviso", function () {
        const $c = $(this).closest(".aviso-card");
        $("#edit-aviso-id, #excluir-aviso-id").val($c.data("id"));
        $("#edit-aviso-tipo").val(($c.data("tipo") || "XINGAMENTO").toString().toUpperCase());
        $("#detalhe-aviso-tipo-label").text($("#edit-aviso-tipo").val());
        $("#detalhe-aviso-mensagem").val($c.find(".aviso-mensagem-store").text());
        bootstrap.Modal.getOrCreateInstance(document.getElementById("avisoDetailsModal")).show();
    });
});
