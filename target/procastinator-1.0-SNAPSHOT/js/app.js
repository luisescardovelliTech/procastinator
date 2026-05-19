const API = {
    tarefas: `${APP_CONTEXT}/api/tarefas`,
    avisos: `${APP_CONTEXT}/api/avisos`,
    desculpas: `${APP_CONTEXT}/api/desculpas`
};
let editandoId = null;
let tarefasCache = new Map();
let elogiosDisponiveis = [];
let xingamentosDisponiveis = [];
let ultimoXingamentoId = null;

function escapeHtml(text) {
    return $("<div>").text(text || "").html();
}

function showToast(message) {
    const $toast = $("#toast-status");
    $toast.text(message).addClass("show");
    setTimeout(() => $toast.removeClass("show"), 2300);
}

function showElogioModal(message) {
    $("#elogio-modal-mensagem").text(message || "Boa! Continua assim.");
    bootstrap.Modal.getOrCreateInstance(document.getElementById("elogioModal")).show();
}

function showXingamentoModal(message) {
    $("#xingamento-modal-mensagem").text(message || "Anda logo e para de enrolar.");
    bootstrap.Modal.getOrCreateInstance(document.getElementById("xingamentoModal")).show();
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

function buildTaskCard(tarefa) {
    const categoria = tarefa.categoria || { nome: "GERAL" };
    const xingamentos = (tarefa.xingamentos || []).map(x => x.mensagem).join(" | ");
    return `
        <article class="task-card" data-id="${tarefa.id}">
            <span class="task-badge">${escapeHtml(categoria.nome || "GERAL")}</span>
            <h3 class="task-title">Titulo: ${escapeHtml(tarefa.titulo)}</h3>
            <p class="task-desc">Descricao: ${escapeHtml(tarefa.descricao || "Sem descricao")}</p>
            <div class="task-tags">${escapeHtml(xingamentos || "Sem incentivo agressivo")}</div>
            <div class="task-actions task-status-actions d-flex gap-2 mb-2">
                <button class="btn btn-sm btn-outline-secondary btn-move" data-status="BACKLOG">Backlog</button>
                <button class="btn btn-sm btn-outline-secondary btn-move" data-status="ESPERANDO">Esperando</button>
                <button class="btn btn-sm btn-outline-secondary btn-move" data-status="QUASE_FIZ">Quase Fiz</button>
            </div>
            <div class="task-actions task-crud-actions d-flex gap-2">
                <button class="btn btn-sm btn-outline-dark btn-view">Visualizar</button>
                <button class="btn btn-sm btn-outline-secondary btn-desculpas">Desculpas</button>
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

function atualizarStatus(id, novoStatus, xingamentoId) {
    const payload = { status: novoStatus };
    if (xingamentoId) {
        payload.xingamentos = [{ id: xingamentoId }];
    }
    return $.ajax({
        url: `${API.tarefas}/${id}`,
        method: "PUT",
        contentType: "application/json",
        data: JSON.stringify(payload)
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

function carregarElogios() {
    return $.get(API.avisos)
        .done(function (avisos) {
            const lista = Array.isArray(avisos) ? avisos : [];
            elogiosDisponiveis = lista.filter(function (aviso) {
                return (aviso.tipo || "").toUpperCase() === "ELOGIO";
            });
            xingamentosDisponiveis = lista.filter(function (aviso) {
                return (aviso.tipo || "").toUpperCase() === "XINGAMENTO";
            });
        })
        .fail(function () {
            elogiosDisponiveis = [];
            xingamentosDisponiveis = [];
            showToast("Falha ao carregar elogios cadastrados.");
        });
}

function sortearElogio() {
    if (!elogiosDisponiveis.length) {
        return null;
    }
    const indice = Math.floor(Math.random() * elogiosDisponiveis.length);
    return elogiosDisponiveis[indice];
}

function sortearXingamento() {
    if (!xingamentosDisponiveis.length) {
        return null;
    }
    if (xingamentosDisponiveis.length > 1 && ultimoXingamentoId != null) {
        const semRepeticao = xingamentosDisponiveis.filter(function (item) {
            return Number(item.id) !== Number(ultimoXingamentoId);
        });
        if (semRepeticao.length) {
            const indiceSemRepeticao = Math.floor(Math.random() * semRepeticao.length);
            return semRepeticao[indiceSemRepeticao];
        }
    }
    const indice = Math.floor(Math.random() * xingamentosDisponiveis.length);
    return xingamentosDisponiveis[indice];
}

function montarPayloadFormulario(elogioSelecionado) {
    const categoriaNome = $("#categoria").val().trim();
    const dataPrazo = normalizarDataParaInput($("#data-prazo").val());
    const payload = {
        titulo: $("#titulo").val().trim(),
        descricao: $("#descricao").val().trim(),
        status: $("#status").val(),
        dataPrazo: dataPrazo || null,
        categoria: categoriaNome ? { nome: categoriaNome } : null
    };

    if (elogioSelecionado) {
        payload.xingamentos = [{ id: elogioSelecionado.id }];
    }

    return payload;
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

function carregarDesculpasDaTarefa(tarefaId) {
    return $.get(API.desculpas).then(function (desculpas) {
        const lista = Array.isArray(desculpas) ? desculpas : [];
        return lista.filter(function (item) {
            return Number(item.tarefaId) === Number(tarefaId);
        });
    });
}

function renderizarDesculpasDaTarefa(tarefa, desculpas) {
    $("#desculpas-tarefa-titulo").text(tarefa.titulo || `Tarefa #${tarefa.id}`);

    if (!desculpas.length) {
        $("#desculpas-tarefa-lista").html("<div class='text-secondary'>Nenhuma desculpa cadastrada para esta tarefa.</div>");
        return;
    }

    const cards = desculpas.map(function (desculpa) {
        return `
            <article class="border rounded p-2 bg-light-subtle">
                <div class="d-flex justify-content-between gap-2 flex-wrap small text-secondary mb-1">
                    <span>${escapeHtml(formatarDataHora(desculpa.dataHora))}</span>
                    <span>Eficacia ${escapeHtml(desculpa.nivelEficacia || 0)}/10</span>
                </div>
                <div class="mb-0">${escapeHtml(desculpa.comentario || "Sem comentario")}</div>
            </article>`;
    });
    $("#desculpas-tarefa-lista").html(cards.join(""));
}

function aplicarStatusLocal(id, status) {
    const tarefa = tarefasCache.get(id);
    if (!tarefa) {
        return;
    }
    tarefa.status = status;
    renderizarKanban(Array.from(tarefasCache.values()));
}

function aplicarIncentivoLocal(id, mensagem, tipo) {
    const tarefa = tarefasCache.get(id);
    if (!tarefa || !mensagem) {
        return;
    }
    tarefa.xingamentos = [{
        id: null,
        mensagem: mensagem,
        tipo: tipo || "XINGAMENTO"
    }];
    renderizarKanban(Array.from(tarefasCache.values()));
}

$(function () {
    carregarElogios();
    carregarTarefas();

    $('[data-bs-target="#taskModal"]').on("click", function () {
        limparFormularioModoCriacao();
        carregarElogios();
    });

    $("#btn-salvar").on("click", function () {
        const emCriacao = editandoId == null;
        const elogioSorteado = emCriacao ? sortearElogio() : null;
        const payload = montarPayloadFormulario(elogioSorteado);
        if (!payload.titulo) {
            showToast("Preencha o titulo da tarefa.");
            return;
        }
        if (!payload.dataPrazo) {
            showToast("Informe a data de prazo da tarefa.");
            return;
        }

        const requisicao = emCriacao
            ? criarTarefa(payload)
            : atualizarTarefa(editandoId, payload);
        const mensagemElogio = emCriacao && elogioSorteado
            ? elogioSorteado.mensagem
            : null;

        requisicao.done(function () {
                $("#task-form")[0].reset();
                bootstrap.Modal.getInstance(document.getElementById("taskModal")).hide();
                carregarTarefas();
                showToast(emCriacao
                    ? "Tarefa registrada para futura procrastinacao."
                    : "Tarefa atualizada.");
                if (emCriacao && mensagemElogio) {
                    showElogioModal(mensagemElogio);
                }
                editandoId = null;
            })
            .fail((xhr) => showToast(extrairMensagemErro(xhr, "Erro ao salvar tarefa.")));
    });

    $(document).on("click", ".btn-move", function () {
        const id = $(this).closest(".task-card").data("id");
        const status = $(this).data("status");
        const statusComXingamento = status === "ESPERANDO" || status === "QUASE_FIZ";
        const xingamento = statusComXingamento ? sortearXingamento() : null;

        atualizarStatus(id, status, xingamento ? xingamento.id : null)
            .done(function () {
                aplicarStatusLocal(id, status);
                if (xingamento && xingamento.mensagem) {
                    aplicarIncentivoLocal(id, xingamento.mensagem, "XINGAMENTO");
                }
                carregarTarefas();
                showToast("Status atualizado.");
                if (xingamento && xingamento.mensagem) {
                    ultimoXingamentoId = xingamento.id;
                    showXingamentoModal(xingamento.mensagem);
                }
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

    $(document).on("click", ".btn-desculpas", function () {
        const id = $(this).closest(".task-card").data("id");
        const tarefa = tarefasCache.get(id);
        if (!tarefa) {
            showToast("Nao foi possivel carregar a tarefa.");
            return;
        }

        carregarDesculpasDaTarefa(id)
            .done(function (desculpas) {
                renderizarDesculpasDaTarefa(tarefa, desculpas);
                bootstrap.Modal.getOrCreateInstance(document.getElementById("desculpasTarefaModal")).show();
            })
            .fail(() => showToast("Falha ao carregar desculpas da tarefa."));
    });

});
