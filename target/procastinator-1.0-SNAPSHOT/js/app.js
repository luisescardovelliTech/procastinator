function showToast(message) {
    const $toast = $("#toast-status");
    $toast.text(message).addClass("show");
    setTimeout(function () {
        $toast.removeClass("show");
    }, 2300);
}

function showElogioModal(message) {
    $("#elogio-modal-mensagem").text(message || "Boa! Continua assim.");
    bootstrap.Modal.getOrCreateInstance(document.getElementById("elogioModal")).show();
}

function showXingamentoModal(message) {
    $("#xingamento-modal-mensagem").text(message || "Anda logo e para de enrolar.");
    bootstrap.Modal.getOrCreateInstance(document.getElementById("xingamentoModal")).show();
}

function normalizarDataParaInput(valor) {
    if (!valor) {
        return "";
    }
    const texto = String(valor).trim();
    return texto.length >= 10 ? texto.substring(0, 10) : texto;
}

function limparFormularioModoCriacao() {
    $("#hidden-acao").val("criar");
    $("#hidden-id").val("");
    $("#task-form")[0].reset();
    $("#task-modal-title").text("Adicionar Tarefa Inacabavel");
}

function preencherFormularioEdicao($card) {
    $("#hidden-acao").val("atualizar");
    $("#hidden-id").val($card.data("id"));
    $("#titulo").val($card.data("titulo") || "");
    $("#descricao").val($card.data("descricao") || "");
    $("#data-prazo").val(normalizarDataParaInput($card.data("prazo")));
    $("#status").val($card.data("status") || "BACKLOG");
    $("#categoria").val($card.data("categoria") || "");
    $("#task-modal-title").text("Editar Tarefa");
}

function preencherDetalhesTarefa($card) {
    $("#detalhe-titulo").text($card.data("titulo") || "Sem titulo");
    $("#detalhe-descricao").text($card.data("descricao") || "Sem descricao");
    $("#detalhe-status").text($card.data("status") || "BACKLOG");
    $("#detalhe-categoria").text($card.data("categoria") || "GERAL");
    const prazo = $card.data("prazo");
    $("#detalhe-prazo").text(prazo ? String(prazo) : "Nao definido");
}

function aplicarFlashServidor() {
    const toast = $("#server-flash-toast").text();
    const elogio = $("#server-flash-elogio").text();
    const xingamento = $("#server-flash-xingamento").text();
    const erro = $("#server-flash-erro").text();
    if (toast) {
        showToast(toast);
    }
    if (erro) {
        showToast(erro);
    }
    if (elogio) {
        showElogioModal(elogio);
    }
    if (xingamento) {
        showXingamentoModal(xingamento);
    }
    const params = new URLSearchParams(window.location.search);
    const t = params.get("t");
    const mapa = {
        "1": "Tarefa registrada para futura procrastinacao.",
        "2": "Tarefa atualizada.",
        "3": "Status atualizado.",
        "4": "Tarefa removida."
    };
    if (!toast && t && mapa[t]) {
        showToast(mapa[t]);
    }
    if (window.history && window.history.replaceState && (t || toast || elogio || xingamento)) {
        window.history.replaceState({}, document.title, window.location.pathname);
    }
}

$(function () {
    aplicarFlashServidor();

    if ($("#abrir-modal-desculpas").length) {
        bootstrap.Modal.getOrCreateInstance(document.getElementById("desculpasTarefaModal")).show();
    }

    $('[data-bs-target="#taskModal"]').on("click", function () {
        limparFormularioModoCriacao();
    });

    $("#task-form").on("submit", function (event) {
        if (!$("#titulo").val().trim()) {
            event.preventDefault();
            showToast("Preencha o titulo da tarefa.");
            return;
        }
        if (!$("#data-prazo").val()) {
            event.preventDefault();
            showToast("Informe a data de prazo da tarefa.");
        }
    });

    $(document).on("click", ".btn-edit", function () {
        preencherFormularioEdicao($(this).closest(".task-card"));
        bootstrap.Modal.getOrCreateInstance(document.getElementById("taskModal")).show();
    });

    $(document).on("click", ".btn-view", function () {
        preencherDetalhesTarefa($(this).closest(".task-card"));
        bootstrap.Modal.getOrCreateInstance(document.getElementById("taskDetailsModal")).show();
    });
});
