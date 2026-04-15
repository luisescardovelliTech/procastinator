const API = {
    tarefas: `${APP_CONTEXT}/api/tarefas`,
    desculpas: `${APP_CONTEXT}/api/desculpas`
};
let desculpasCache = new Map();
let desculpasLista = [];
let filtroAtual = "";
let editandoDesculpaId = null;

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

function carregarTarefasNoSelect() {
    $.get(API.tarefas)
        .done(function (tarefas) {
            const options = ["<option value=''>Sem tarefa associada</option>"];
            tarefas.forEach(function (tarefa) {
                options.push(`<option value="${tarefa.id}">${escapeHtml(tarefa.titulo)}</option>`);
            });
            $("#desculpa-tarefa").html(options.join(""));
            $("#detalhe-desculpa-tarefa").html(options.join(""));
        })
        .fail(() => showToast("Nao foi possivel carregar tarefas para associar."));
}

function criarDesculpa(payload) {
    return $.ajax({
        url: API.desculpas,
        method: "POST",
        contentType: "application/json",
        data: JSON.stringify(payload)
    });
}

function atualizarDesculpa(id, payload) {
    return $.ajax({
        url: `${API.desculpas}/${id}`,
        method: "PUT",
        contentType: "application/json",
        data: JSON.stringify(payload)
    });
}

function excluirDesculpa(id) {
    return $.ajax({
        url: `${API.desculpas}/${id}`,
        method: "DELETE"
    });
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

function montarEstrelas(nivelEficacia) {
    const estrelasCheias = Math.max(0, Math.min(5, Math.round((nivelEficacia || 0) / 2)));
    return "★".repeat(estrelasCheias) + "☆".repeat(5 - estrelasCheias);
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
    $("#count-desculpas").text(texto);
}

function filtrarDesculpas() {
    const termo = normalizarTexto(filtroAtual);
    if (!termo) {
        return desculpasLista;
    }

    return desculpasLista.filter(function (desculpa) {
        const alvo = [
            desculpa.comentario,
            desculpa.tarefaTitulo,
            desculpa.usuarioNome,
            desculpa.tarefaId != null ? `tarefa #${desculpa.tarefaId}` : ""
        ].map(normalizarTexto).join(" ");

        return alvo.includes(termo);
    });
}

function buildDesculpaCard(desculpa) {
    return `
        <article class="desculpa-card" data-id="${desculpa.id}">
            <header class="d-flex justify-content-between align-items-start gap-2 mb-2">
                <div>
                    <span class="desculpa-badge">TAREFA #${escapeHtml(desculpa.tarefaId || "--")}</span>
                    <span class="small text-secondary ms-2">${escapeHtml(formatarDataHora(desculpa.dataHora))}</span>
                    <div class="small fw-semibold mt-1">${escapeHtml(desculpa.tarefaTitulo || "Sem tarefa associada")}</div>
                </div>
                <div class="desculpa-stars">${montarEstrelas(desculpa.nivelEficacia)}</div>
            </header>
            <p class="mb-3 fst-italic">"${escapeHtml(desculpa.comentario || "Sem comentario")}"</p>
            <footer class="d-flex justify-content-between small">
                <span><strong>Tarefa:</strong> ${escapeHtml(desculpa.tarefaTitulo || "Sem tarefa associada")}</span>
                <span class="text-danger-emphasis">Eficacia ${escapeHtml(desculpa.nivelEficacia || 0)}/10</span>
            </footer>
            <div class="mt-3 d-flex justify-content-end">
                <button type="button" class="btn btn-sm btn-outline-dark btn-view-desculpa">Ver detalhes</button>
            </div>
        </article>`;
}

function renderizarDesculpas() {
    const desculpasFiltradas = filtrarDesculpas();
    desculpasCache = new Map(desculpasLista.map(d => [d.id, d]));
    atualizarContadorRegistros(desculpasFiltradas.length, desculpasLista.length);

    if (!desculpasFiltradas.length) {
        const mensagem = filtroAtual.trim()
            ? "Nenhum registro encontrado para este filtro."
            : "Nenhuma desculpa registrada ainda.";
        $("#lista-desculpas").html(`<div class='text-secondary'>${mensagem}</div>`);
        return;
    }

    $("#lista-desculpas").html(desculpasFiltradas.map(buildDesculpaCard).join(""));
}

function preencherModalDesculpa(desculpa) {
    editandoDesculpaId = desculpa.id;
    $("#detalhe-desculpa-tarefa").val(desculpa.tarefaId || "");
    $("#detalhe-desculpa-comentario").val(desculpa.comentario || "");
    const eficacia = Number(desculpa.nivelEficacia || 5);
    $("#detalhe-desculpa-eficacia").val(eficacia);
    $("#detalhe-eficacia-valor").text(`${eficacia}/10`);
}

function carregarDesculpas() {
    $.get(API.desculpas)
        .done(function (desculpas) {
            desculpasLista = Array.isArray(desculpas) ? desculpas : [];
            renderizarDesculpas();
        })
        .fail(() => showToast("Falha ao carregar log de desculpas."));
}

$(function () {
    carregarTarefasNoSelect();
    carregarDesculpas();

    $("#desculpa-eficacia").on("input", function () {
        $("#eficacia-valor").text(`${$(this).val()}/10`);
    });

    $("#detalhe-desculpa-eficacia").on("input", function () {
        $("#detalhe-eficacia-valor").text(`${$(this).val()}/10`);
    });

    $("#filtro-desculpas").on("input", function () {
        filtroAtual = $(this).val();
        renderizarDesculpas();
    });

    $(document).on("click", ".btn-view-desculpa", function () {
        const id = $(this).closest(".desculpa-card").data("id");
        const desculpa = desculpasCache.get(id);
        if (!desculpa) {
            showToast("Nao foi possivel carregar os detalhes da desculpa.");
            return;
        }

        preencherModalDesculpa(desculpa);
        bootstrap.Modal.getOrCreateInstance(document.getElementById("desculpaDetailsModal")).show();
    });

    $("#btn-atualizar-desculpa").on("click", function () {
        if (editandoDesculpaId == null) {
            showToast("Nenhuma desculpa selecionada para edicao.");
            return;
        }

        const comentario = $("#detalhe-desculpa-comentario").val().trim();
        if (!comentario) {
            showToast("Comentario e obrigatorio.");
            return;
        }

        const tarefaId = $("#detalhe-desculpa-tarefa").val();
        const payload = {
            comentario,
            nivelEficacia: Number($("#detalhe-desculpa-eficacia").val()),
            tarefaId: tarefaId ? Number(tarefaId) : null
        };

        atualizarDesculpa(editandoDesculpaId, payload)
            .done(function () {
                bootstrap.Modal.getOrCreateInstance(document.getElementById("desculpaDetailsModal")).hide();
                carregarDesculpas();
                showToast("Desculpa atualizada com sucesso.");
            })
            .fail((xhr) => showToast(extrairMensagemErro(xhr, "Erro ao atualizar desculpa.")));
    });

    $("#btn-excluir-desculpa").on("click", function () {
        if (editandoDesculpaId == null) {
            showToast("Nenhuma desculpa selecionada para exclusao.");
            return;
        }

        if (!window.confirm("Tem certeza que deseja excluir esta desculpa?")) {
            return;
        }

        excluirDesculpa(editandoDesculpaId)
            .done(function () {
                bootstrap.Modal.getOrCreateInstance(document.getElementById("desculpaDetailsModal")).hide();
                editandoDesculpaId = null;
                carregarDesculpas();
                showToast("Desculpa excluida com sucesso.");
            })
            .fail((xhr) => showToast(extrairMensagemErro(xhr, "Erro ao excluir desculpa.")));
    });

    $("#desculpa-form").on("submit", function (event) {
        event.preventDefault();

        const comentario = $("#desculpa-comentario").val().trim();
        if (!comentario) {
            showToast("Escreva uma desculpa antes de arquivar.");
            return;
        }

        const tarefaId = $("#desculpa-tarefa").val();
        const payload = {
            comentario,
            nivelEficacia: Number($("#desculpa-eficacia").val()),
            tarefaId: tarefaId ? Number(tarefaId) : null
        };

        criarDesculpa(payload)
            .done(function () {
                $("#desculpa-comentario").val("");
                $("#desculpa-eficacia").val(5);
                $("#eficacia-valor").text("5/10");
                carregarDesculpas();
                showToast("Desculpa arquivada no log com sucesso.");
            })
            .fail(() => showToast("Erro ao salvar desculpa."));
    });
});
