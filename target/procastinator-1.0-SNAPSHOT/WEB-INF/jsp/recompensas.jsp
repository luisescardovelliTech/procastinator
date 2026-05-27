<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
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
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<jsp:include page="fragments/nav.jsp"/>
<div class="container-fluid">
    <div class="row min-vh-100">
        <jsp:include page="fragments/sidebar.jsp"/>
        <main class="col-12 col-lg-10 p-4">
            <section class="avisos-hero mb-4">
                <div class="d-flex justify-content-between flex-wrap gap-3">
                    <div>
                        <h1 class="h2 mb-2">Recompensas</h1>
                        <p class="text-secondary mb-0">Cadastre premios para registrar progresso nas tarefas.</p>
                    </div>
                    <div class="badge text-bg-danger fs-6 px-2 py-1">Total de pontos: <c:out value="${totalPontos}"/></div>
                </div>
            </section>
            <div class="d-flex justify-content-between align-items-center gap-2 mb-2 flex-wrap">
                <h2 class="h6 text-uppercase text-secondary mb-0">Recompensas <span id="count-recompensas" class="badge text-bg-light ms-1"><c:out value="${fn:length(recompensas)}"/></span></h2>
                <input id="filtro-recompensas" type="search" class="form-control form-control-sm aviso-filtro" placeholder="Filtrar...">
            </div>
            <div id="lista-recompensas" class="d-flex flex-column gap-3">
                <c:forEach var="r" items="${recompensas}">
                    <c:set var="pts" value="${r.pontos != null ? r.pontos : 0}"/>
                    <c:set var="tituloTarefa" value="${r.tarefa != null ? r.tarefa.titulo : 'Sem tarefa associada'}"/>
                    <article class="aviso-card recompensa-card" data-id="<c:out value='${r.id}'/>">
                        <span class="d-none recompensa-search-blob"><c:out value="${r.titulo}"/> <c:out value="${r.descricao}"/> <c:out value="${tituloTarefa}"/></span>
                        <header class="d-flex justify-content-between mb-2">
                            <div class="d-flex gap-2 align-items-center">
                                <span class="aviso-badge">RECOMPENSA #<c:out value="${r.id}"/></span>
                                <span class="badge ${pts < 0 ? 'text-bg-danger' : 'text-bg-success'}"><c:out value="${pts}"/> pts</span>
                            </div>
                            <span class="small text-secondary"><c:out value="${r.dataConquista}"/></span>
                        </header>
                        <h3 class="h6"><c:out value="${r.titulo}"/></h3>
                        <p class="mb-2"><c:out value="${r.descricao}"/></p>
                        <div class="text-end">
                            <a class="btn btn-sm btn-outline-dark" href="${ctx}/recompensas?detalhes=<c:out value='${r.id}'/>">Detalhes</a>
                        </div>
                    </article>
                </c:forEach>
            </div>
        </main>
    </div>
</div>

<c:if test="${not empty detalheRecompensaId}">
<div class="modal fade" id="recompensaDetailsModal" tabindex="-1" aria-hidden="true">
    <div class="modal-dialog modal-dialog-centered">
        <div class="modal-content">
            <div class="modal-header"><h5 class="modal-title">Detalhes da Tarefa</h5><button type="button" class="btn-close" data-bs-dismiss="modal"></button></div>
            <div class="modal-body">
                <div class="mb-2"><strong>Titulo:</strong> <c:out value="${detalheTarefaTitulo}"/></div>
                <div class="mb-3"><strong>Descricao:</strong> <c:out value="${detalheTarefaDescricao}"/></div>
                <div class="mb-3"><strong>Criada em:</strong> <c:out value="${detalheTarefaCriacao}"/></div>
                <strong>Mudancas de coluna:</strong>
                <ul class="mt-2 mb-0 ps-3 small">
                    <c:forEach var="m" items="${detalheMudancas}">
                        <li><c:out value="${m.titulo}"/> — <c:out value="${m.dataConquista}"/></li>
                    </c:forEach>
                    <c:if test="${empty detalheMudancas}"><li>Nenhuma mudanca registrada.</li></c:if>
                </ul>
            </div>
        </div>
    </div>
</div>
<span id="abrir-modal-detalhes" class="d-none">1</span>
</c:if>

<div id="toast-status" class="toast-box"></div>
<script src="${ctx}/js/jquery-4.0.0.min.js"></script>
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
<script>const APP_CONTEXT = '${ctx}';</script>
<script src="${ctx}/js/recompensas.js"></script>
</body>
</html>
