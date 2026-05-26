<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
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
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<jsp:include page="fragments/nav.jsp"/>
<div class="container-fluid">
    <div class="row min-vh-100">
        <jsp:include page="fragments/sidebar.jsp"/>
        <main class="col-12 col-lg-10 p-4 desculpas-page">
            <section class="desculpas-hero mb-4">
                <h1 class="h2 mb-2">Log de Desculpas</h1>
                <p class="text-secondary mb-0">Arquivo oficial das justificativas para o nao-cumprimento de obrigacoes.</p>
            </section>
            <div class="row g-3">
                <div class="col-12 col-xl-4">
                    <div class="card border-0 shadow-sm h-100 desculpa-form-card">
                        <div class="card-body p-4">
                            <h2 class="h6 text-uppercase text-secondary mb-3">Nova justificativa</h2>
                            <form class="d-flex flex-column gap-3" method="post" action="${ctx}/desculpas">
                                <input type="hidden" name="acao" value="criar"/>
                                <select name="tarefaId" id="desculpa-tarefa" class="form-select form-select-sm">
                                    <option value="">Sem tarefa associada</option>
                                    <c:forEach var="tf" items="${tarefasSelect}">
                                        <option value="<c:out value='${tf.id}'/>"><c:out value="${tf.titulo}"/></option>
                                    </c:forEach>
                                </select>
                                <textarea name="comentario" id="desculpa-comentario" class="form-control" rows="5" required
                                          placeholder="Ex: A internet caiu exatamente quando eu ia comecar."></textarea>
                                <div>
                                    <label class="form-label small d-flex justify-content-between">
                                        <span>Nivel de eficacia</span><span id="eficacia-valor">5/10</span>
                                    </label>
                                    <input name="nivelEficacia" id="desculpa-eficacia" type="range" class="form-range" min="0" max="10" value="5">
                                </div>
                                <button type="submit" class="btn btn-dark">Arquivar omissao</button>
                            </form>
                        </div>
                    </div>
                </div>
                <div class="col-12 col-xl-8">
                    <div class="d-flex justify-content-between align-items-center gap-2 mb-2 flex-wrap">
                        <h2 class="h6 text-uppercase text-secondary mb-0">Registros <span id="count-desculpas" class="badge text-bg-light ms-1"><c:out value="${fn:length(desculpas)}"/></span></h2>
                        <input id="filtro-desculpas" type="search" class="form-control form-control-sm desculpa-filtro" placeholder="Filtrar...">
                    </div>
                    <div id="lista-desculpas" class="d-flex flex-column gap-3">
                        <c:forEach var="h" items="${desculpas}">
                            <c:set var="tituloTarefa" value="${h.tarefa != null ? h.tarefa.titulo : 'Sem tarefa associada'}"/>
                            <c:set var="tid" value="${h.tarefa != null ? h.tarefa.id : ''}"/>
                            <c:set var="ef" value="${h.nivelEficacia != null ? h.nivelEficacia : 5}"/>
                            <article class="desculpa-card" data-id="<c:out value='${h.id}'/>"
                                     data-tarefa-id="<c:out value='${tid}'/>"
                                     data-eficacia="<c:out value='${ef}'/>">
                                <span class="d-none desculpa-comentario-store"><c:out value="${h.comentario}"/></span>
                                <span class="d-none desculpa-search-blob"><c:out value="${h.comentario}"/> <c:out value="${tituloTarefa}"/></span>
                                <header class="d-flex justify-content-between mb-2">
                                    <div>
                                        <span class="desculpa-badge">TAREFA #<c:out value="${empty tid ? '--' : tid}"/></span>
                                        <span class="small text-secondary ms-2"><c:out value="${h.dataHora}"/></span>
                                        <div class="small fw-semibold mt-1"><c:out value="${tituloTarefa}"/></div>
                                    </div>
                                    <div class="desculpa-stars-preview" data-ef="<c:out value='${ef}'/>"></div>
                                </header>
                                <p class="mb-3 fst-italic">"<c:out value="${h.comentario}"/>"</p>
                                <div class="text-end">
                                    <button type="button" class="btn btn-sm btn-outline-dark btn-view-desculpa">Ver detalhes</button>
                                </div>
                            </article>
                        </c:forEach>
                    </div>
                </div>
            </div>
        </main>
    </div>
</div>
<div id="toast-status" class="toast-box"></div>
<c:if test="${not empty flashToast}"><span id="server-flash-toast" class="d-none"><c:out value="${flashToast}"/></span></c:if>
<c:if test="${not empty flashErro}"><span id="server-flash-erro" class="d-none"><c:out value="${flashErro}"/></span></c:if>

<div class="modal fade" id="desculpaDetailsModal" tabindex="-1" aria-hidden="true">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header"><h2 class="modal-title fs-5">Detalhes da Desculpa</h2><button type="button" class="btn-close" data-bs-dismiss="modal"></button></div>
            <form id="form-atualizar-desculpa" method="post" action="${ctx}/desculpas">
                <input type="hidden" name="acao" value="atualizar"/>
                <input type="hidden" name="id" id="edit-desculpa-id"/>
                <div class="modal-body d-flex flex-column gap-3">
                    <select name="tarefaId" id="detalhe-desculpa-tarefa" class="form-select form-select-sm">
                        <option value="">Sem tarefa associada</option>
                        <c:forEach var="tf" items="${tarefasSelect}">
                            <option value="<c:out value='${tf.id}'/>"><c:out value="${tf.titulo}"/></option>
                        </c:forEach>
                    </select>
                    <textarea name="comentario" id="detalhe-desculpa-comentario" class="form-control" rows="4" required></textarea>
                    <input name="nivelEficacia" id="detalhe-desculpa-eficacia" type="range" class="form-range" min="0" max="10" value="5">
                </div>
            </form>
            <form id="form-excluir-desculpa" method="post" action="${ctx}/desculpas" class="d-none" onsubmit="return confirm('Excluir esta desculpa?');">
                <input type="hidden" name="acao" value="excluir"/>
                <input type="hidden" name="id" id="excluir-desculpa-id"/>
            </form>
            <div class="modal-footer">
                <button type="submit" form="form-excluir-desculpa" class="btn btn-outline-danger me-auto">Excluir</button>
                <button type="button" class="btn btn-outline-secondary" data-bs-dismiss="modal">Cancelar</button>
                <button type="submit" form="form-atualizar-desculpa" class="btn btn-dark">Salvar</button>
            </div>
        </div>
    </div>
</div>
<script src="${ctx}/js/jquery-4.0.0.min.js"></script>
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
<script>const APP_CONTEXT = '${ctx}';</script>
<script src="${ctx}/js/desculpas.js"></script>
</body>
</html>
