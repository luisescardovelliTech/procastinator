function normalizarTexto(v) {
    return (v || "").toString().normalize("NFD").replace(/[\u0300-\u036f]/g, "").toLowerCase().trim();
}

function aplicarFiltro() {
    const termo = normalizarTexto($("#filtro-recompensas").val());
    const $cards = $("#lista-recompensas .recompensa-card");
    let visiveis = 0;
    $cards.each(function () {
        const match = !termo || normalizarTexto($(this).find(".recompensa-search-blob").text()).includes(termo);
        $(this).toggleClass("d-none", !match);
        if (match) visiveis++;
    });
    const total = $cards.length;
    $("#count-recompensas").text(visiveis === total ? String(total) : visiveis + "/" + total);
}

$(function () {
    $("#filtro-recompensas").on("input", aplicarFiltro);
    if ($("#abrir-modal-detalhes").length) {
        bootstrap.Modal.getOrCreateInstance(document.getElementById("recompensaDetailsModal")).show();
    }
});
