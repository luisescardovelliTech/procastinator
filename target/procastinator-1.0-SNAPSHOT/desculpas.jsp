<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html lang="pt-BR">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Procrastinator | Log de Desculpas</title>
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/procrastinator.css">
</head>
<body class="bg-light">
<nav class="navbar navbar-expand-lg bg-white border-bottom">
    <div class="container-fluid">
        <span class="navbar-brand fw-bold">Procrastinator</span>
        <div class="navbar-nav ms-auto gap-lg-3 align-items-lg-center">
            <span class="nav-link">Quadro de Avisos</span>
            <a class="nav-link btn btn-link p-0" href="${pageContext.request.contextPath}/index.jsp">Lista do Nao Fazer</a>
            <span class="nav-link">Timer de Descanso</span>
            <a class="nav-link btn btn-link p-0 fw-bold border-bottom border-dark" href="${pageContext.request.contextPath}/desculpas.jsp">Log de Desculpas</a>
        </div>
    </div>
</nav>

<div class="container-fluid">
    <div class="row min-vh-100">
        <aside class="col-12 col-lg-2 border-end bg-body-tertiary p-3">
            <h6 class="text-uppercase text-secondary small fw-bold mb-3">Painel</h6>
            <div class="list-group list-group-flush small">
                <span class="list-group-item bg-transparent text-secondary">Quadro de Avisos</span>
                <a class="list-group-item list-group-item-action bg-transparent text-secondary" href="${pageContext.request.contextPath}/index.jsp">Lista do Nao Fazer</a>
                <span class="list-group-item bg-transparent text-secondary">Timer de Descanso</span>
                <a class="list-group-item list-group-item-action bg-white fw-semibold rounded" href="${pageContext.request.contextPath}/desculpas.jsp">Log de Desculpas</a>
                <span class="list-group-item bg-transparent text-secondary">Estatisticas de Culpa</span>
            </div>
            <div class="card mt-4 border-0 bg-transparent">
                <div class="card-body p-0 small">
                    <div class="fw-bold text-uppercase">Setor de Inercia</div>
                    <div class="text-muted">Protocolo 0800-NADA</div>
                    <button type="button" class="btn btn-dark btn-sm w-100 mt-2">Desistir Cedo</button>
                </div>
            </div>
        </aside>

        <main class="col-12 col-lg-10 p-4 desculpas-page">
            <section class="desculpas-hero mb-4">
                <div class="d-flex justify-content-between align-items-start gap-3 flex-wrap">
                    <div>
                        <h1 class="h2 mb-2">Log de Desculpas</h1>
                        <p class="text-secondary mb-0">Arquivo oficial das justificativas para o nao-cumprimento de obrigacoes.</p>
                    </div>
                    <div class="text-uppercase small text-secondary fw-semibold">Historico de Inercia</div>
                </div>
            </section>

            <div class="row g-3">
                <div class="col-12 col-xl-4">
                    <div class="card border-0 shadow-sm h-100 desculpa-form-card">
                        <div class="card-body p-4">
                            <h2 class="h6 text-uppercase text-secondary mb-3">Nova justificativa</h2>
                            <form id="desculpa-form" class="d-flex flex-column gap-3">
                                <div>
                                    <label for="desculpa-tarefa" class="form-label small text-uppercase text-secondary">Tarefa associada</label>
                                    <select id="desculpa-tarefa" class="form-select form-select-sm"></select>
                                </div>
                                <div>
                                    <label for="desculpa-comentario" class="form-label small text-uppercase text-secondary">A desculpa</label>
                                    <textarea id="desculpa-comentario" class="form-control" rows="5"
                                              placeholder="Ex: A internet caiu exatamente quando eu ia comecar." required></textarea>
                                    <div class="form-text">Seja especifico: boas desculpas rendem mais estrelas.</div>
                                </div>
                                <div>
                                    <label for="desculpa-eficacia" class="form-label small text-uppercase text-secondary d-flex justify-content-between">
                                        <span>Nivel de eficacia</span>
                                        <span id="eficacia-valor">5/10</span>
                                    </label>
                                    <input id="desculpa-eficacia" type="range" class="form-range" min="0" max="10" value="5">
                                </div>
                                <button id="btn-salvar-desculpa" type="submit" class="btn btn-dark">Arquivar omissao</button>
                            </form>
                        </div>
                    </div>
                </div>

                <div class="col-12 col-xl-8">
                    <div class="d-flex justify-content-between align-items-center gap-2 mb-2 flex-wrap">
                        <h2 class="h6 text-uppercase text-secondary mb-0">Registros recentes <span id="count-desculpas" class="badge text-bg-light ms-1">0</span></h2>
                        <input id="filtro-desculpas" type="search" class="form-control form-control-sm desculpa-filtro" placeholder="Filtrar por tarefa ou texto...">
                    </div>
                    <div id="lista-desculpas" class="d-flex flex-column gap-3"></div>
                </div>
            </div>
        </main>
    </div>
</div>

<div id="toast-status" class="toast-box"></div>

<div class="modal fade" id="desculpaDetailsModal" tabindex="-1" aria-hidden="true">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header">
                <h2 class="modal-title fs-5">Detalhes da Desculpa</h2>
                <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Fechar"></button>
            </div>
            <div class="modal-body d-flex flex-column gap-3">
                <div>
                    <label for="detalhe-desculpa-tarefa" class="form-label small text-uppercase text-secondary">Tarefa associada</label>
                    <select id="detalhe-desculpa-tarefa" class="form-select form-select-sm"></select>
                </div>
                <div>
                    <label for="detalhe-desculpa-comentario" class="form-label small text-uppercase text-secondary">Comentario</label>
                    <textarea id="detalhe-desculpa-comentario" class="form-control" rows="4" required></textarea>
                </div>
                <div>
                    <label for="detalhe-desculpa-eficacia" class="form-label small text-uppercase text-secondary d-flex justify-content-between">
                        <span>Nivel de eficacia</span>
                        <span id="detalhe-eficacia-valor">5/10</span>
                    </label>
                    <input id="detalhe-desculpa-eficacia" type="range" class="form-range" min="0" max="10" value="5">
                </div>
            </div>
            <div class="modal-footer">
                <button type="button" id="btn-excluir-desculpa" class="btn btn-outline-danger me-auto">Excluir</button>
                <button type="button" class="btn btn-outline-secondary" data-bs-dismiss="modal">Cancelar</button>
                <button type="button" id="btn-atualizar-desculpa" class="btn btn-dark">Salvar alteracoes</button>
            </div>
        </div>
    </div>
</div>

<script src="https://code.jquery.com/jquery-3.7.1.min.js"></script>
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
<script>
    const APP_CONTEXT = '${pageContext.request.contextPath}';
</script>
<script src="${pageContext.request.contextPath}/js/desculpas.js?v=<%= System.currentTimeMillis() %>"></script>
</body>
</html>

