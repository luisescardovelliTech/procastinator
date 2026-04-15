<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html lang="pt-BR">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Procrastinator | Estatisticas de Culpa</title>
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/procrastinator.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/estatisticas.css">
</head>
<body class="bg-light">
<nav class="navbar navbar-expand-lg bg-white border-bottom">
    <div class="container-fluid">
        <span class="navbar-brand fw-bold">Procrastinator</span>
        <div class="navbar-nav ms-auto gap-lg-3 align-items-lg-center">
            <span class="nav-link">Quadro de Avisos</span>
            <a class="nav-link btn btn-link p-0" href="${pageContext.request.contextPath}/index.jsp">Lista do Nao Fazer</a>
            <a class="nav-link btn btn-link p-0" href="${pageContext.request.contextPath}/timer.jsp">Timer de Descanso</a>
            <a class="nav-link btn btn-link p-0" href="${pageContext.request.contextPath}/desculpas.jsp">Log de Desculpas</a>
            <a class="nav-link btn btn-link p-0 fw-bold border-bottom border-dark" href="${pageContext.request.contextPath}/estatisticas.jsp">Estatisticas de Culpa</a>
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
                <a class="list-group-item list-group-item-action bg-transparent text-secondary" href="${pageContext.request.contextPath}/timer.jsp">Timer de Descanso</a>
                <a class="list-group-item list-group-item-action bg-transparent text-secondary" href="${pageContext.request.contextPath}/desculpas.jsp">Log de Desculpas</a>
                <a class="list-group-item list-group-item-action bg-white fw-semibold rounded" href="${pageContext.request.contextPath}/estatisticas.jsp">Estatisticas de Culpa</a>
            </div>
            <div class="card mt-4 border-0 bg-transparent">
                <div class="card-body p-0 small">
                    <div class="fw-bold text-uppercase">Setor de Inercia</div>
                    <div class="text-muted">Protocolo 0800-NADA</div>
                    <button type="button" class="btn btn-dark btn-sm w-100 mt-2">Desistir Cedo</button>
                </div>
            </div>
        </aside>

        <main class="col-12 col-lg-10 p-4 estatisticas-page">
            <section class="estatisticas-hero mb-4">
                <div class="d-flex justify-content-between align-items-start flex-wrap gap-3">
                    <div>
                        <h1 class="h2 mb-2">Estatisticas de Culpa</h1>
                        <p class="mb-0 text-secondary">Mapa da reincidencia da sua Lista do Nao Fazer.</p>
                    </div>
                    <button id="btn-recarregar" class="btn btn-outline-light btn-sm">Atualizar dados</button>
                </div>
            </section>

            <section class="row g-3 mb-3" id="kpi-cards">
                <div class="col-12 col-md-6 col-xl-3">
                    <article class="culpa-kpi-card">
                        <div class="culpa-kpi-label">Total de tarefas</div>
                        <div id="kpi-total-tarefas" class="culpa-kpi-value">0</div>
                    </article>
                </div>
                <div class="col-12 col-md-6 col-xl-3">
                    <article class="culpa-kpi-card">
                        <div class="culpa-kpi-label">Pendencias ativas</div>
                        <div id="kpi-pendentes" class="culpa-kpi-value">0</div>
                    </article>
                </div>
                <div class="col-12 col-md-6 col-xl-3">
                    <article class="culpa-kpi-card culpa-kpi-alerta">
                        <div class="culpa-kpi-label">Tarefas atrasadas</div>
                        <div id="kpi-total-desculpas" class="culpa-kpi-value">0</div>
                    </article>
                </div>
                <div class="col-12 col-md-6 col-xl-3">
                    <article class="culpa-kpi-card">
                        <div class="culpa-kpi-label">Taxa de reincidencia</div>
                        <div id="kpi-reincidencia" class="culpa-kpi-value">0%</div>
                    </article>
                </div>
            </section>

            <section class="row g-3">
                <div class="col-12 col-xl-6">
                    <article class="culpa-panel">
                        <h2 class="h6 text-uppercase mb-3">Frequencia por status</h2>
                        <div class="chart-slot">
                            <canvas id="chart-status" aria-label="Grafico de status das tarefas"></canvas>
                        </div>
                    </article>
                </div>
                <div class="col-12 col-xl-6">
                    <article class="culpa-panel">
                        <h2 class="h6 text-uppercase mb-3">Categorias mais violadas</h2>
                        <div class="chart-slot">
                            <canvas id="chart-categorias" aria-label="Grafico de categorias mais violadas"></canvas>
                        </div>
                    </article>
                </div>
                <div class="col-12">
                    <article class="culpa-panel">
                        <h2 class="h6 text-uppercase mb-3">Evolucao de tarefas por prazo</h2>
                        <div class="chart-slot chart-slot-wide">
                            <canvas id="chart-tendencia" aria-label="Grafico de tendencia de tarefas por prazo"></canvas>
                        </div>
                    </article>
                </div>
                <div class="col-12 col-xl-6">
                    <article class="culpa-panel">
                        <h2 class="h6 text-uppercase mb-3">Top 5 tarefas criticas</h2>
                        <ol id="ranking-reincidencia" class="culpa-ranking mb-0"></ol>
                    </article>
                </div>
                <div class="col-12 col-xl-6">
                    <article class="culpa-panel">
                        <h2 class="h6 text-uppercase mb-3">Insights automaticos</h2>
                        <ul id="insights-lista" class="culpa-insights mb-0"></ul>
                    </article>
                </div>
            </section>
        </main>
    </div>
</div>

<div id="toast-status" class="toast-box"></div>

<script src="https://code.jquery.com/jquery-3.7.1.min.js"></script>
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
<script src="https://cdn.jsdelivr.net/npm/chart.js@4.4.3/dist/chart.umd.min.js"></script>
<script>
    const APP_CONTEXT = '${pageContext.request.contextPath}';
</script>
<script src="${pageContext.request.contextPath}/js/estatisticas.js"></script>
</body>
</html>
