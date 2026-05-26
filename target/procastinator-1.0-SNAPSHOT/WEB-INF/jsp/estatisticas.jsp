<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="pt-BR">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Procrastinator | Estatisticas de Culpa</title>
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css">
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/procrastinator.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/estatisticas.css">
</head>
<body class="bg-light">
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<jsp:include page="fragments/nav.jsp"/>
<div class="container-fluid">
    <div class="row min-vh-100">
        <jsp:include page="fragments/sidebar.jsp"/>
        <main class="col-12 col-lg-10 p-4 estatisticas-page">
            <section class="estatisticas-hero mb-4 d-flex justify-content-between flex-wrap gap-3">
                <div>
                    <h1 class="h2 mb-2">Estatisticas de Culpa</h1>
                    <p class="mb-0 text-secondary">Mapa da reincidencia da sua Lista do Nao Fazer.</p>
                </div>
                <div class="d-flex flex-wrap gap-2 align-items-center estatisticas-acoes">
                    <a href="${ctx}/estatisticas" class="btn btn-outline-secondary btn-sm d-inline-flex align-items-center gap-1">
                        <i class="bi bi-arrow-clockwise" aria-hidden="true"></i>
                        <span>Atualizar dados</span>
                    </a>
                    <a href="${ctx}/estatisticas/pdf"
                       class="btn btn-danger btn-sm d-inline-flex align-items-center gap-2 shadow-sm btn-exportar-pdf"
                       title="Baixar relatorio oficial de inercia em PDF">
                        <i class="bi bi-file-earmark-pdf fs-5" aria-hidden="true"></i>
                        <span>Exportar PDF da Culpa</span>
                    </a>
                </div>
            </section>
            <section class="row g-3 mb-3">
                <div class="col-12 col-md-6 col-xl-3"><article class="culpa-kpi-card"><div class="culpa-kpi-label">Total de tarefas</div><div id="kpi-total-tarefas" class="culpa-kpi-value">0</div></article></div>
                <div class="col-12 col-md-6 col-xl-3"><article class="culpa-kpi-card"><div class="culpa-kpi-label">Pendencias ativas</div><div id="kpi-pendentes" class="culpa-kpi-value">0</div></article></div>
                <div class="col-12 col-md-6 col-xl-3"><article class="culpa-kpi-card"><div class="culpa-kpi-label">Tarefas atrasadas</div><div id="kpi-total-desculpas" class="culpa-kpi-value">0</div></article></div>
                <div class="col-12 col-md-6 col-xl-3"><article class="culpa-kpi-card"><div class="culpa-kpi-label">Score de pontos</div><div id="kpi-score-pontos" class="culpa-kpi-value">0</div></article></div>
            </section>
            <section class="row g-3">
                <div class="col-12 col-xl-6"><article class="culpa-panel"><h2 class="h6 text-uppercase mb-3">Frequencia por status</h2><div class="chart-slot"><canvas id="chart-status"></canvas></div></article></div>
                <div class="col-12 col-xl-6"><article class="culpa-panel"><h2 class="h6 text-uppercase mb-3">Categorias mais violadas</h2><div class="chart-slot"><canvas id="chart-categorias"></canvas></div></article></div>
                <div class="col-12"><article class="culpa-panel"><h2 class="h6 text-uppercase mb-3">Ranking de tarefas para fazer</h2><ol id="ranking-prazos" class="culpa-ranking mb-0"></ol></article></div>
            </section>
        </main>
    </div>
</div>
<script type="application/json" id="dashboard-data"><%= (String) request.getAttribute("dashboardJson") %></script>
<script src="${ctx}/js/jquery-4.0.0.min.js"></script>
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
<script src="https://cdn.jsdelivr.net/npm/chart.js@4.4.3/dist/chart.umd.min.js"></script>
<script>const APP_CONTEXT = '${ctx}';</script>
<script src="${ctx}/js/estatisticas.js"></script>
</body>
</html>
