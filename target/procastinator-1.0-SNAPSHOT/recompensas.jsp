<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html lang="pt-BR">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Procrastinator | Recompensas</title>
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/procrastinator.css">
</head>
<body class="bg-light">
<nav class="navbar navbar-expand-lg bg-white border-bottom">
    <div class="container-fluid">
        <span class="navbar-brand fw-bold">Procrastinator</span>
        <div class="navbar-nav ms-auto gap-lg-3 align-items-lg-center">
            <a class="nav-link btn btn-link p-0" href="${pageContext.request.contextPath}/estatisticas.jsp">Estatisticas de Culpa</a>
            <a class="nav-link btn btn-link p-0" href="${pageContext.request.contextPath}/index.jsp">Lista do Nao Fazer</a>
            <a class="nav-link btn btn-link p-0" href="${pageContext.request.contextPath}/timer.jsp">Timer de Descanso</a>
            <a class="nav-link btn btn-link p-0" href="${pageContext.request.contextPath}/desculpas.jsp">Log de Desculpas</a>
            <a class="nav-link btn btn-link p-0" href="${pageContext.request.contextPath}/xingamentosElogio.jsp">Xingamentos e Elogio</a>
            <a class="nav-link btn btn-link p-0 fw-bold border-bottom border-dark" href="${pageContext.request.contextPath}/recompensas.jsp">Recompensas</a>
        </div>
    </div>
</nav>

<div class="container-fluid">
    <div class="row min-vh-100">
        <aside class="col-12 col-lg-2 border-end bg-body-tertiary p-3">
            <h6 class="text-uppercase text-secondary small fw-bold mb-3">Painel</h6>
            <div class="list-group list-group-flush small">
                <a class="list-group-item list-group-item-action bg-transparent text-secondary" href="${pageContext.request.contextPath}/estatisticas.jsp">Estatisticas de Culpa</a>
                <a class="list-group-item list-group-item-action bg-transparent text-secondary" href="${pageContext.request.contextPath}/index.jsp">Lista do Nao Fazer</a>
                <a class="list-group-item list-group-item-action bg-transparent text-secondary" href="${pageContext.request.contextPath}/timer.jsp">Timer de Descanso</a>
                <a class="list-group-item list-group-item-action bg-transparent text-secondary" href="${pageContext.request.contextPath}/desculpas.jsp">Log de Desculpas</a>
                <a class="list-group-item list-group-item-action bg-transparent text-secondary" href="${pageContext.request.contextPath}/xingamentosElogio.jsp">Xingamentos e Elogio</a>
                <a class="list-group-item list-group-item-action bg-white fw-semibold rounded" href="${pageContext.request.contextPath}/recompensas.jsp">Recompensas</a>
            </div>
            <div class="card mt-4 border-0 bg-transparent">
                <div class="card-body p-0 small">
                    <div class="fw-bold text-uppercase">Setor de Inercia</div>
                    <div class="text-muted">Protocolo 0800-NADA</div>
                    <button type="button" class="btn btn-dark btn-sm w-100 mt-2">Desistir Cedo</button>
                </div>
            </div>
        </aside>

        <main class="col-12 col-lg-10 p-4 avisos-page">
            <section class="avisos-hero mb-4">
                <div class="d-flex justify-content-between align-items-start gap-3 flex-wrap">
                    <div>
                        <h1 class="h2 mb-2">Recompensas</h1>
                        <p class="text-secondary mb-0">Cadastre premios para registrar progresso nas tarefas.</p>
                    </div>
                    <div class="d-flex flex-column align-items-end gap-1">
                        <div class="text-uppercase small text-secondary fw-semibold">Gestao de Recompensas</div>
                        <div class="badge text-bg-success fs-6 px-3 py-2">Total de pontos: <span id="total-pontos">0</span></div>
                    </div>
                </div>
            </section>

            <div class="row g-3">
                <div class="col-12">
                    <div class="d-flex justify-content-between align-items-center gap-2 mb-2 flex-wrap">
                        <h2 class="h6 text-uppercase text-secondary mb-0">Recompensas cadastradas <span id="count-recompensas" class="badge text-bg-light ms-1">0</span></h2>
                        <input id="filtro-recompensas" type="search" class="form-control form-control-sm aviso-filtro" placeholder="Filtrar por titulo, descricao ou tarefa...">
                    </div>
                    <div id="lista-recompensas" class="d-flex flex-column gap-3"></div>
                </div>
            </div>
        </main>
    </div>
</div>

<div id="toast-status" class="toast-box"></div>

<script src="https://code.jquery.com/jquery-3.7.1.min.js"></script>
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
<script>
    const APP_CONTEXT = '${pageContext.request.contextPath}';
</script>
<script src="${pageContext.request.contextPath}/js/recompensas.js?v=<%= System.currentTimeMillis() %>"></script>
</body>
</html>



