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
        <div class="navbar-nav ms-auto gap-lg-3">
            <span class="nav-link">Quadro de Avisos</span>
            <span class="nav-link active fw-bold border-bottom border-dark">Lista do Não Fazer</span>
            <span class="nav-link">Timer de Descanso</span>
            <span class="nav-link">Log de Desculpas</span>
        </div>
    </div>
</nav>

<div class="container-fluid">
    <div class="row min-vh-100">
        <aside class="col-12 col-lg-2 border-end bg-body-tertiary p-3">
            <h6 class="text-uppercase text-secondary small fw-bold mb-3">Painel</h6>
            <div class="list-group list-group-flush small">
                <span class="list-group-item bg-transparent text-secondary">Quadro de Avisos</span>
                <span class="list-group-item bg-white fw-semibold rounded">Lista do Não Fazer</span>
                <span class="list-group-item bg-transparent text-secondary">Timer de Descanso</span>
                <span class="list-group-item bg-transparent text-secondary">Log de Desculpas</span>
                <span class="list-group-item bg-transparent text-secondary">Estatísticas de Culpa</span>
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

<div id="toast-status" class="toast-box">Log de registro detectado! Tarefa movida com sucesso.</div>

<script src="https://code.jquery.com/jquery-3.7.1.min.js"></script>
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
<script>
    const APP_CONTEXT = '${pageContext.request.contextPath}';
</script>
<script src="${pageContext.request.contextPath}/js/app.js"></script>
</body>
</html>
