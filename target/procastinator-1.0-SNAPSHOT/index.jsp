<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html lang="pt-BR">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Procrastinator | Lista do Não Fazer</title>
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/procrastinator.css">
</head>
<body class="bg-light">
<nav class="navbar navbar-expand-lg bg-white border-bottom">
    <div class="container-fluid">
        <span class="navbar-brand fw-bold">Procrastinator</span>
        <div class="navbar-nav ms-auto gap-lg-3 align-items-lg-center">
            <a class="nav-link btn btn-link p-0" href="${pageContext.request.contextPath}/quadro-avisos.jsp">Xingamentos e Elogios</a>
            <a class="nav-link btn btn-link p-0 fw-bold border-bottom border-dark"
               href="${pageContext.request.contextPath}/index.jsp">Lista do Nao Fazer</a>
            <a class="nav-link btn btn-link p-0"
               href="${pageContext.request.contextPath}/timer.jsp">Timer de Descanso</a>
            <a class="nav-link btn btn-link p-0"
               href="${pageContext.request.contextPath}/desculpas.jsp">Log de Desculpas</a>
            <a class="nav-link btn btn-link p-0"
               href="${pageContext.request.contextPath}/estatisticas.jsp">Estatisticas de Culpa</a>
        </div>
    </div>
</nav>

<div class="container-fluid">
    <div class="row min-vh-100">
        <aside class="col-12 col-lg-2 border-end bg-body-tertiary p-3">
            <h6 class="text-uppercase text-secondary small fw-bold mb-3">Painel</h6>
            <div class="list-group list-group-flush small">
                <a class="list-group-item list-group-item-action bg-transparent text-secondary" href="${pageContext.request.contextPath}/quadro-avisos.jsp">Xingamentos e Elogios</a>
                <a class="list-group-item list-group-item-action bg-white fw-semibold rounded"
                   href="${pageContext.request.contextPath}/index.jsp">Lista do Nao Fazer</a>
                <a class="list-group-item list-group-item-action bg-transparent text-secondary"
                   href="${pageContext.request.contextPath}/timer.jsp">Timer de Descanso</a>
                <a class="list-group-item list-group-item-action bg-transparent text-secondary"
                   href="${pageContext.request.contextPath}/desculpas.jsp">Log de Desculpas</a>
                <a class="list-group-item list-group-item-action bg-transparent text-secondary"
                   href="${pageContext.request.contextPath}/estatisticas.jsp">Estatisticas de Culpa</a>
            </div>
            <div class="card mt-4 border-0 bg-transparent">
                <div class="card-body p-0 small">
                    <div class="fw-bold text-uppercase">Setor de Inércia</div>
                    <div class="text-muted">Protocolo 0800-NADA</div>
                    <button type="button" class="btn btn-dark btn-sm w-100 mt-2">Desistir Cedo</button>
                </div>
            </div>
        </aside>

        <main class="col-12 col-lg-10 p-4">
            <section id="view-kanban">
            <div class="row mb-3 text-uppercase fw-bold small text-secondary">
                <div class="col-12 col-lg-4">Backlog <span id="count-backlog" class="ms-2"></span></div>
                <div class="col-12 col-lg-4">Esperando <span id="count-esperando" class="ms-2"></span></div>
                <div class="col-12 col-lg-4">Quase Fiz Alguma Coisa <span id="count-quase-fiz" class="ms-2"></span></div>
            </div>

            <div class="row g-3">
                <div class="col-12 col-lg-4">
                    <div id="column-backlog" class="kanban-column"></div>
                    <button class="btn btn-outline-secondary btn-sm w-100 border-dashed mt-2"
                            data-bs-toggle="modal" data-bs-target="#taskModal">
                        + ADICIONAR TAREFA
                    </button>
                </div>
                <div class="col-12 col-lg-4">
                    <div id="column-esperando" class="kanban-column"></div>
                </div>
                <div class="col-12 col-lg-4">
                    <div id="column-quase-fiz" class="kanban-column"></div>
                </div>
            </div>
            </section>
        </main>
    </div>
</div>

<div class="modal fade" id="taskModal" tabindex="-1" aria-hidden="true">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header">
                <h1 class="modal-title fs-5">Adicionar Tarefa Inacabável</h1>
                <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Fechar"></button>
            </div>
            <div class="modal-body">
                <form id="task-form" class="d-flex flex-column gap-3">
                    <input id="titulo" class="form-control" placeholder="Título da tarefa" maxlength="40" required>
                    <textarea id="descricao" class="form-control" rows="3" placeholder="Descrição curta"></textarea>
                    <div>
                        <label for="data-prazo" class="form-label small text-uppercase text-secondary">Data prevista de termino</label>
                        <input id="data-prazo" type="date" class="form-control" required>
                    </div>
                    <select id="status" class="form-select">
                        <option value="BACKLOG">Backlog</option>
                        <option value="ESPERANDO">Esperando</option>
                        <option value="QUASE_FIZ">Quase Fiz Alguma Coisa</option>
                    </select>
                    <input id="categoria" class="form-control" placeholder="Categoria (ex: Trabalho)">
                </form>
            </div>
            <div class="modal-footer">
                <button type="button" class="btn btn-outline-secondary" data-bs-dismiss="modal">Cancelar</button>
                <button type="button" id="btn-salvar" class="btn btn-dark">Salvar</button>
            </div>
        </div>
    </div>
</div>

<div class="modal fade" id="taskDetailsModal" tabindex="-1" aria-hidden="true">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header">
                <h2 class="modal-title fs-5">Detalhes da Tarefa</h2>
                <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Fechar"></button>
            </div>
            <div class="modal-body d-flex flex-column gap-3">
                <div>
                    <div class="small text-uppercase text-secondary">Titulo</div>
                    <div id="detalhe-titulo" class="fw-semibold"></div>
                </div>
                <div>
                    <div class="small text-uppercase text-secondary">Descricao</div>
                    <div id="detalhe-descricao"></div>
                </div>
                <div class="row g-2">
                    <div class="col-6">
                        <div class="small text-uppercase text-secondary">Status</div>
                        <div id="detalhe-status"></div>
                    </div>
                    <div class="col-6">
                        <div class="small text-uppercase text-secondary">Categoria</div>
                        <div id="detalhe-categoria"></div>
                    </div>
                </div>
                <div>
                    <div class="small text-uppercase text-secondary">Prazo</div>
                    <div id="detalhe-prazo"></div>
                </div>
            </div>
            <div class="modal-footer">
                <button type="button" class="btn btn-outline-secondary" data-bs-dismiss="modal">Fechar</button>
            </div>
        </div>
    </div>
</div>

<div class="modal fade" id="xingamentoModal" tabindex="-1" aria-hidden="true">
    <div class="modal-dialog modal-dialog-centered">
        <div class="modal-content">
            <div class="modal-header border-0 pb-0">
                <h2 class="modal-title fs-5">Xingamento motivacional</h2>
                <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Fechar"></button>
            </div>
            <div class="modal-body pt-1">
                <div class="xingamento-modal-card">
                    <div id="xingamento-modal-mensagem" class="xingamento-modal-msg"></div>
                </div>
            </div>
            <div class="modal-footer border-0 pt-0">
                <button type="button" class="btn btn-dark" data-bs-dismiss="modal">Continuar</button>
            </div>
        </div>
    </div>
</div>

<div id="toast-status" class="toast-box">Log de registro detectado! Tarefa movida com sucesso.</div>

<script src="https://code.jquery.com/jquery-3.7.1.min.js"></script>
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
<script>
    const APP_CONTEXT = '${pageContext.request.contextPath}';
</script>
<script src="${pageContext.request.contextPath}/js/app.js"></script>
</body>
</html>
