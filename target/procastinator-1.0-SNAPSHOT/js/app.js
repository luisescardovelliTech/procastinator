const API = {
    tarefas: `${APP_CONTEXT}/api/tarefas`
};
let editandoId = null;
let tarefasCache = new Map();

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

function normalizarDataParaInput(valor) {
    if (!valor) {
        return "";
    }
    const texto = String(valor).trim();
    if (texto.length >= 10) {
        return texto.substring(0, 10);
    }
    return texto;
}

function buildTaskCard(tarefa) {
    const categoria = tarefa.categoria || { nome: "GERAL" };
    const xingamentos = (tarefa.xingamentos || []).map(x => x.mensagem).join(" | ");

    return `
        <article class="task-card" data-id="${tarefa.id}">
            <span class="task-badge">${escapeHtml(categoria.nome || "GERAL")}</span>
            <h3 class="task-title">${escapeHtml(tarefa.titulo)}</h3>
            <p class="task-desc">${escapeHtml(tarefa.descricao || "Sem descricao")}</p>
            <div class="task-tags">${escapeHtml(xingamentos || "Sem incentivo agressivo")}</div>
            <div class="task-actions task-status-actions d-flex gap-2 mb-2">
                <button class="btn btn-sm btn-outline-secondary btn-move" data-status="BACKLOG">Backlog</button>
                <button class="btn btn-sm btn-outline-secondary btn-move" data-status="ESPERANDO">Esperando</button>
                <button class="btn btn-sm btn-outline-secondary btn-move" data-status="QUASE_FIZ">Quase Fiz</button>
            </div>
            <div class="task-actions task-crud-actions d-flex gap-2">
                <button class="btn btn-sm btn-outline-dark btn-view">Visualizar</button>
                <button class="btn btn-sm btn-outline-primary btn-edit">Editar</button>
                <button class="btn btn-sm btn-outline-danger btn-delete">Excluir</button>
            </div>
        </article>`;
}

function renderizarKanban(tarefas) {
    tarefasCache = new Map(tarefas.map(t => [t.id, t]));
    const backlog = tarefas.filter(t => t.status === "BACKLOG");
    const esperando = tarefas.filter(t => t.status === "ESPERANDO");
    const quaseFiz = tarefas.filter(t => t.status === "QUASE_FIZ");

    $("#column-backlog").html(backlog.map(buildTaskCard).join(""));
    $("#column-esperando").html(esperando.map(buildTaskCard).join(""));
    $("#column-quase-fiz").html(quaseFiz.map(buildTaskCard).join(""));

    $("#count-backlog").text(String(backlog.length).padStart(2, "0") + " tarefas");
    $("#count-esperando").text(String(esperando.length).padStart(2, "0") + " tarefas");
    $("#count-quase-fiz").text(String(quaseFiz.length).padStart(2, "0") + " tarefas");
}

function carregarTarefas() {
    $.get(API.tarefas)
        .done(renderizarKanban)
        .fail(() => showToast("Falha ao carregar tarefas."));
}

function criarTarefa(payload) {
    return $.ajax({
        url: API.tarefas,
        method: "POST",
        contentType: "application/json",
        data: JSON.stringify(payload)
    });
}

function atualizarStatus(id, novoStatus) {
    return $.ajax({
        url: `${API.tarefas}/${id}`,
        method: "PUT",
        contentType: "application/json",
        data: JSON.stringify({ status: novoStatus })
    });
}

function atualizarTarefa(id, payload) {
    return $.ajax({
        url: `${API.tarefas}/${id}`,
        method: "PUT",
        contentType: "application/json",
        data: JSON.stringify(payload)
    });
}

function excluirTarefa(id) {
    return $.ajax({
        url: `${API.tarefas}/${id}`,
        method: "DELETE"
    });
}

function isTransicaoComXingamento(statusAtual, statusDestino) {
    return (statusAtual === "BACKLOG" && statusDestino === "ESPERANDO")
        || (statusAtual === "ESPERANDO" && statusDestino === "QUASE_FIZ");
}

function escolherXingamentoParaTransicao(tarefa, statusDestino) {
    if (!tarefa || !isTransicaoComXingamento(tarefa.status, statusDestino)) {
        return null;
    }

    const xingamentos = Array.isArray(tarefa.xingamentos) ? tarefa.xingamentos : [];
    const candidatos = xingamentos.filter(function (item) {
        return (item.tipo || "").toUpperCase() === "XINGAMENTO";
    });

    if (!candidatos.length) {
        return `A tarefa "${tarefa.titulo || "Sem titulo"}" mudou de fase. Sem enrolar agora.`;
    }

    const sorteado = candidatos[Math.floor(Math.random() * candidatos.length)];
    return sorteado && sorteado.mensagem
        ? sorteado.mensagem
        : `A tarefa "${tarefa.titulo || "Sem titulo"}" mudou de fase. Foque no proximo passo.`;
}

function showXingamentoModal(mensagem) {
    if (!mensagem) {
        return;
    }

    $("#xingamento-modal-mensagem").text(mensagem);
    bootstrap.Modal.getOrCreateInstance(document.getElementById("xingamentoModal")).show();
}

function montarPayloadFormulario() {
    const categoriaNome = $("#categoria").val().trim();
    const dataPrazo = normalizarDataParaInput($("#data-prazo").val());
    return {
        titulo: $("#titulo").val().trim(),
        descricao: $("#descricao").val().trim(),
        status: $("#status").val(),
        dataPrazo: dataPrazo || null,
        categoria: categoriaNome ? { nome: categoriaNome } : null
    };
}

function limparFormularioModoCriacao() {
    editandoId = null;
    $("#task-form")[0].reset();
    $("#btn-salvar").text("Salvar");
    $("#taskModal .modal-title").text("Adicionar Tarefa Inacabavel");
}

function preencherFormularioEdicao(tarefa) {
    editandoId = tarefa.id;
    $("#titulo").val(tarefa.titulo || "");
    $("#descricao").val(tarefa.descricao || "");
    $("#data-prazo").val(normalizarDataParaInput(tarefa.dataPrazo));
    $("#status").val(tarefa.status || "BACKLOG");
    $("#categoria").val(tarefa.categoria && tarefa.categoria.nome ? tarefa.categoria.nome : "");
    $("#btn-salvar").text("Atualizar");
    $("#taskModal .modal-title").text("Editar Tarefa");
}

function preencherDetalhesTarefa(tarefa) {
    const categoriaNome = tarefa.categoria && tarefa.categoria.nome ? tarefa.categoria.nome : "GERAL";
    $("#detalhe-titulo").text(tarefa.titulo || "Sem titulo");
    $("#detalhe-descricao").text(tarefa.descricao || "Sem descricao");
    $("#detalhe-status").text(tarefa.status || "BACKLOG");
    $("#detalhe-categoria").text(categoriaNome);
    $("#detalhe-prazo").text(tarefa.dataPrazo || "Nao definido");
}

$(function () {
    carregarTarefas();

    $('[data-bs-target="#taskModal"]').on("click", function () {
        limparFormularioModoCriacao();
    });

    $("#btn-salvar").on("click", function () {
        const payload = montarPayloadFormulario();
        if (!payload.titulo) {
            showToast("Preencha o titulo da tarefa.");
            return;
        }
        if (!payload.dataPrazo) {
            showToast("Informe a data de prazo da tarefa.");
            return;
        }

        const requisicao = editandoId == null
            ? criarTarefa(payload)
            : atualizarTarefa(editandoId, payload);

        requisicao.done(function () {
                $("#task-form")[0].reset();
                bootstrap.Modal.getInstance(document.getElementById("taskModal")).hide();
                carregarTarefas();
                showToast(editandoId == null
                    ? "Tarefa registrada para futura procrastinacao."
                    : "Tarefa atualizada.");
                editandoId = null;
            })
            .fail((xhr) => showToast(extrairMensagemErro(xhr, "Erro ao salvar tarefa.")));
    });

    $(document).on("click", ".btn-move", function () {
        const id = $(this).closest(".task-card").data("id");
        const status = $(this).data("status");
        const tarefa = tarefasCache.get(id);

        if (!tarefa) {
            showToast("Nao foi possivel identificar a tarefa selecionada.");
            return;
        }

        if (tarefa.status === status) {
            showToast("A tarefa ja esta nesse status.");
            return;
        }

        const mensagemTransicao = escolherXingamentoParaTransicao(tarefa, status);

        atualizarStatus(id, status)
            .done(function () {
                carregarTarefas();
                if (mensagemTransicao) {
                    showXingamentoModal(mensagemTransicao);
                    return;
                }
                showToast("Status atualizado.");
            })
            .fail(() => showToast("Falha ao mover tarefa."));
    });

    $(document).on("click", ".btn-delete", function () {
        const id = $(this).closest(".task-card").data("id");
        excluirTarefa(id)
            .done(function () {
                carregarTarefas();
                showToast("Tarefa removida.");
            })
            .fail(() => showToast("Falha ao excluir tarefa."));
    });

    $(document).on("click", ".btn-edit", function () {
        const id = $(this).closest(".task-card").data("id");
        const tarefa = tarefasCache.get(id);
        if (!tarefa) {
            showToast("Nao foi possivel carregar a tarefa para edicao.");
            return;
        }
        preencherFormularioEdicao(tarefa);
        bootstrap.Modal.getOrCreateInstance(document.getElementById("taskModal")).show();
    });

    $(document).on("click", ".btn-view", function () {
        const id = $(this).closest(".task-card").data("id");
        const tarefa = tarefasCache.get(id);
        if (!tarefa) {
            showToast("Nao foi possivel carregar os detalhes da tarefa.");
            return;
        }
        preencherDetalhesTarefa(tarefa);
        bootstrap.Modal.getOrCreateInstance(document.getElementById("taskDetailsModal")).show();
    });

});
