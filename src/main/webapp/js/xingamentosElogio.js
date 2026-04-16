const API = {
    avisos: `${APP_CONTEXT}/api/avisos`
};

let avisosLista = [];
let avisosCache = new Map();
let filtroAtual = "";
let editandoAvisoId = null;
let editandoAvisoTipo = "XINGAMENTO";

function escapeHtml(text) {
    return $("<div>").text(text || "").html();
}

function showToast(message) {
    const $toast = $("#toast-status");
    $toast.text(message).addClass("show");
    setTimeout(() => $toast.removeClass("show"), 2300);
}

function extrairMensagemErro(xhr, fallback) {
    if (xhr && xhr.responseText) {
        return xhr.responseText;
    }
    return fallback;
}

function normalizarTexto(valor) {
    return (valor || "")
        .toString()
        .normalize("NFD")
        .replace(/[\u0300-\u036f]/g, "")
        .toLowerCase()
        .trim();
}

function atualizarContadorRegistros(quantidadeVisivel, quantidadeTotal) {
    const texto = quantidadeVisivel === quantidadeTotal
        ? `${quantidadeTotal}`
        : `${quantidadeVisivel}/${quantidadeTotal}`;
    $("#count-avisos").text(texto);
}

function criarAviso(payload) {
    return $.ajax({
        url: API.avisos,
        method: "POST",
        contentType: "application/json",
        data: JSON.stringify(payload)
    });
}

function atualizarAviso(id, payload) {
    return $.ajax({
        url: `${API.avisos}/${id}`,
        method: "PUT",
        contentType: "application/json",
        data: JSON.stringify(payload)
    });
}

function excluirAviso(id) {
    return $.ajax({
        url: `${API.avisos}/${id}`,
        method: "DELETE"
    });
}

function filtrarAvisos() {
    const termo = normalizarTexto(filtroAtual);
    if (!termo) {
        return avisosLista;
    }

    return avisosLista.filter(function (aviso) {
        const alvo = [
            aviso.mensagem,
            aviso.tipo
        ].map(normalizarTexto).join(" ");

        return alvo.includes(termo);
    });
}

function buildAvisoCard(aviso) {
    return `
        <article class="aviso-card" data-id="${aviso.id}">
            <header class="d-flex justify-content-between align-items-start gap-3 mb-2">
                <div class="d-flex align-items-center gap-2 flex-wrap">
                    <span class="aviso-badge">FRASE #${escapeHtml(aviso.id)}</span>
                    <span class="badge text-bg-dark aviso-tipo">${escapeHtml(aviso.tipo || "XINGAMENTO")}</span>
                </div>
            </header>
            <p class="mb-3">${escapeHtml(aviso.mensagem || "Sem mensagem")}</p>
            <div class="d-flex justify-content-end">
                <button type="button" class="btn btn-sm btn-outline-dark btn-view-aviso">Ver detalhes</button>
            </div>
        </article>`;
}

function renderizarAvisos() {
    const avisosFiltrados = filtrarAvisos();
    avisosCache = new Map(avisosLista.map(a => [a.id, a]));
    atualizarContadorRegistros(avisosFiltrados.length, avisosLista.length);

    if (!avisosFiltrados.length) {
        const mensagem = filtroAtual.trim()
            ? "Nenhuma frase encontrada para este filtro."
            : "Nenhuma frase cadastrada ainda.";
        $("#lista-avisos").html(`<div class='text-secondary aviso-empty'>${mensagem}</div>`);
        return;
    }

    $("#lista-avisos").html(avisosFiltrados.map(buildAvisoCard).join(""));
}

function carregarAvisos() {
    $.get(API.avisos)
        .done(function (avisos) {
            avisosLista = Array.isArray(avisos) ? avisos : [];
            renderizarAvisos();
        })
        .fail(() => showToast("Falha ao carregar xingamentos e elogios."));
}

function preencherModalAviso(aviso) {
    editandoAvisoId = aviso.id;
    editandoAvisoTipo = (aviso.tipo || "XINGAMENTO").toUpperCase();
    $("#detalhe-aviso-mensagem").val(aviso.mensagem || "");
    $("#detalhe-aviso-tipo-label").text(editandoAvisoTipo);
}

function montarPayloadFormulario($mensagem, $tipo) {
    return {
        mensagem: $mensagem.val().trim(),
        tipo: ($tipo.val() || "XINGAMENTO").trim().toUpperCase()
    };
}

$(function () {
    carregarAvisos();

    $("#filtro-avisos").on("input", function () {
        filtroAtual = $(this).val();
        renderizarAvisos();
    });

    $("#aviso-form").on("submit", function (event) {
        event.preventDefault();

        const payload = montarPayloadFormulario(
            $("#aviso-mensagem"),
            $("#aviso-tipo")
        );

        if (!payload.mensagem) {
            showToast("Escreva uma mensagem antes de salvar.");
            return;
        }

        criarAviso(payload)
            .done(function () {
                $("#aviso-form")[0].reset();
                carregarAvisos();
                showToast("Frase cadastrada com sucesso.");
            })
            .fail((xhr) => showToast(extrairMensagemErro(xhr, "Erro ao salvar frase.")));
    });

    $(document).on("click", ".btn-view-aviso", function () {
        const id = $(this).closest(".aviso-card").data("id");
        const aviso = avisosCache.get(id);
        if (!aviso) {
            showToast("Nao foi possivel carregar os detalhes da frase.");
            return;
        }

        preencherModalAviso(aviso);
        bootstrap.Modal.getOrCreateInstance(document.getElementById("avisoDetailsModal")).show();
    });

    $("#btn-atualizar-aviso").on("click", function () {
        if (editandoAvisoId == null) {
            showToast("Nenhuma frase selecionada para edicao.");
            return;
        }

        const payload = {
            mensagem: $("#detalhe-aviso-mensagem").val().trim(),
            tipo: editandoAvisoTipo
        };

        if (!payload.mensagem) {
            showToast("Mensagem e obrigatoria.");
            return;
        }

        atualizarAviso(editandoAvisoId, payload)
            .done(function () {
                bootstrap.Modal.getOrCreateInstance(document.getElementById("avisoDetailsModal")).hide();
                carregarAvisos();
                showToast("Frase atualizada com sucesso.");
            })
            .fail((xhr) => showToast(extrairMensagemErro(xhr, "Erro ao atualizar frase.")));
    });

    $("#btn-excluir-aviso").on("click", function () {
        if (editandoAvisoId == null) {
            showToast("Nenhuma frase selecionada para exclusao.");
            return;
        }

        if (!window.confirm("Tem certeza que deseja excluir esta frase?")) {
            return;
        }

        excluirAviso(editandoAvisoId)
            .done(function () {
                bootstrap.Modal.getOrCreateInstance(document.getElementById("avisoDetailsModal")).hide();
                editandoAvisoId = null;
                carregarAvisos();
                showToast("Frase excluida com sucesso.");
            })
            .fail((xhr) => {
                const mensagemErro = extrairMensagemErro(xhr, "Erro ao excluir frase.");
                if (xhr && xhr.status === 409) {
                    window.alert(mensagemErro);
                    return;
                }
                showToast(mensagemErro);
            });
    });
});

