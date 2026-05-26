<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
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
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<jsp:include page="fragments/nav.jsp"/>
<div class="container-fluid">
    <div class="row min-vh-100">
        <jsp:include page="fragments/sidebar.jsp"/>
        <main class="col-12 col-lg-10 p-4 timer-page">
            <section class="text-center mb-4 mt-3">
                <h2 class="display-5 fw-bold timer-title">Nao se preocupe, o trabalho nao vai a lugar nenhum.</h2>
            </section>
            <section class="row g-3 align-items-center">
                <div class="col-12 col-xl-3">
                    <div class="timer-side-card">
                        <div class="small text-uppercase text-secondary fw-bold">Tarefas no limbo</div>
                        <div id="timer-pendentes" class="h3 text-danger mb-3"><c:out value="${timerPendentes}"/> Pendentes</div>
                        <div class="small text-uppercase text-secondary fw-bold">Acoes registradas</div>
                        <div id="timer-acoes" class="h4 mb-3"><c:out value="${timerAcoes}"/> "Amanha"</div>
                        <div class="small text-uppercase text-secondary fw-bold">Data prazo proxima</div>
                        <div id="timer-proximo-prazo" class="h6 mb-0"><c:out value="${timerProximoPrazo}"/></div>
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
<script src="${ctx}/js/jquery-4.0.0.min.js"></script>
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
<script>const APP_CONTEXT = '${ctx}';</script>
<script src="${ctx}/js/timer.js"></script>
</body>
</html>
