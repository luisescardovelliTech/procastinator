<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html lang="pt-BR">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Procrastinator | Timer de Descanso</title>
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/procrastinator.css">
</head>
<body class="bg-light">
<nav class="navbar navbar-expand-lg bg-white border-bottom">
    <div class="container-fluid">
        <span class="navbar-brand fw-bold">Procrastinator</span>
        <div class="navbar-nav ms-auto gap-lg-3 align-items-lg-center">
            <a class="nav-link btn btn-link p-0" href="${pageContext.request.contextPath}/quadro-avisos.jsp">Quadro de Avisos</a>
            <a class="nav-link btn btn-link p-0" href="${pageContext.request.contextPath}/index.jsp">Lista do Nao Fazer</a>
            <a class="nav-link btn btn-link p-0 fw-bold border-bottom border-dark"
               href="${pageContext.request.contextPath}/timer.jsp">Timer de Descanso</a>
            <a class="nav-link btn btn-link p-0" href="${pageContext.request.contextPath}/desculpas.jsp">Log de Desculpas</a>
        </div>
    </div>
</nav>

<div class="container-fluid">
    <div class="row min-vh-100">
        <aside class="col-12 col-lg-2 border-end bg-body-tertiary p-3">
            <h6 class="text-uppercase text-secondary small fw-bold mb-3">Painel</h6>
            <div class="list-group list-group-flush small">
                <a class="list-group-item list-group-item-action bg-transparent text-secondary" href="${pageContext.request.contextPath}/quadro-avisos.jsp">Quadro de Avisos</a>
                <a class="list-group-item list-group-item-action bg-transparent text-secondary"
                   href="${pageContext.request.contextPath}/index.jsp">Lista do Nao Fazer</a>
                <a class="list-group-item list-group-item-action bg-white fw-semibold rounded"
                   href="${pageContext.request.contextPath}/timer.jsp">Timer de Descanso</a>
                <a class="list-group-item list-group-item-action bg-transparent text-secondary"
                   href="${pageContext.request.contextPath}/desculpas.jsp">Log de Desculpas</a>
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

        <main class="col-12 col-lg-10 p-4 timer-page">
            <section class="text-center mb-4 mt-3">
                <h2 class="display-5 fw-bold timer-title">Nao se preocupe, o trabalho nao vai a lugar nenhum.</h2>
            </section>

            <section class="row g-3 align-items-center">
                <div class="col-12 col-xl-3">
                    <div class="timer-side-card">
                        <div class="small text-uppercase text-secondary fw-bold">Tarefas no limbo</div>
                        <div id="timer-pendentes" class="h3 text-danger mb-3">0 Pendentes</div>
                        <div class="small text-uppercase text-secondary fw-bold">Acoes registradas</div>
                        <div id="timer-acoes" class="h4 mb-3">0 "Amanha"</div>
                        <div class="small text-uppercase text-secondary fw-bold">Data prazo proxima</div>
                        <div id="timer-proximo-prazo" class="h6 mb-0">--/--/----</div>
                    </div>
                </div>

                <div class="col-12 col-xl-6 text-center">
                    <div id="timer-ring" class="timer-ring mx-auto" role="timer" aria-live="polite">
                        <div class="timer-ring-inner">
                            <div id="timer-phase" class="timer-phase-label">Fase de Descanso</div>
                            <div id="timer-display" class="timer-display">25:00</div>
                        </div>
                    </div>
                    <div class="d-flex justify-content-center gap-2 mt-4">
                        <button id="btn-timer-start" class="btn btn-dark btn-lg px-5">Comecar Descanso</button>
                        <button id="btn-timer-reset" class="btn btn-outline-secondary btn-lg px-4">Resetar</button>
                    </div>
                </div>
                <div class="d-none d-xl-block col-xl-3"></div>
            </section>
        </main>
    </div>
</div>

<script src="https://code.jquery.com/jquery-3.7.1.min.js"></script>
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
<script>
    const APP_CONTEXT = '${pageContext.request.contextPath}';
</script>
<script src="${pageContext.request.contextPath}/js/timer.js"></script>
</body>
</html>
