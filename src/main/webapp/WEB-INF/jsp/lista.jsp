<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<!DOCTYPE html>
<html lang="pt-BR">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Procrastinator | Lista do Nao Fazer</title>
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
            <section id="view-kanban">
                <div class="row mb-3 text-uppercase fw-bold small text-secondary">
                    <div class="col-12 col-lg-4">Backlog <span class="ms-2"><c:out value="${fn:length(backlog)}"/> tarefas</span></div>
                    <div class="col-12 col-lg-4">Esperando <span class="ms-2"><c:out value="${fn:length(esperando)}"/> tarefas</span></div>
                    <div class="col-12 col-lg-4">Quase Fiz Alguma Coisa <span class="ms-2"><c:out value="${fn:length(quaseFiz)}"/> tarefas</span></div>
                </div>
                <div class="row g-3">
                    <div class="col-12 col-lg-4">
                        <div class="kanban-column">
                            <c:forEach var="t" items="${backlog}">
                                <jsp:include page="fragments/task-card.jsp"/>
                            </c:forEach>
                        </div>
                        <button type="button" class="btn btn-outline-secondary btn-sm w-100 border-dashed mt-2"
                                data-bs-toggle="modal" data-bs-target="#taskModal">+ ADICIONAR TAREFA</button>
                    </div>
                    <div class="col-12 col-lg-4">
                        <div class="kanban-column">
                            <c:forEach var="t" items="${esperando}">
                                <jsp:include page="fragments/task-card.jsp"/>
                            </c:forEach>
                        </div>
                    </div>
                    <div class="col-12 col-lg-4">
                        <div class="kanban-column">
                            <c:forEach var="t" items="${quaseFiz}">
                                <jsp:include page="fragments/task-card.jsp"/>
                            </c:forEach>
                        </div>
                    </div>
                </div>
            </section>
        </main>
    </div>
</div>

<div class="modal fade" id="taskModal" tabindex="-1" aria-hidden="true">
    <div class="modal-dialog">
        <div class="modal-content">
            <form id="task-form" action="${ctx}/tarefas" method="post">
                <input type="hidden" name="acao" id="hidden-acao" value="criar"/>
                <input type="hidden" name="id" id="hidden-id" value=""/>
                <div class="modal-header">
                    <h1 class="modal-title fs-5" id="task-modal-title">Adicionar Tarefa Inacabavel</h1>
                    <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Fechar"></button>
                </div>
                <div class="modal-body d-flex flex-column gap-3">
                    <input name="titulo" id="titulo" class="form-control" placeholder="Titulo da tarefa" maxlength="40" required>
                    <textarea name="descricao" id="descricao" class="form-control" rows="3" placeholder="Descricao curta"></textarea>
                    <div>
                        <label for="data-prazo" class="form-label small text-uppercase text-secondary">Data prevista de termino</label>
                        <input name="dataPrazo" id="data-prazo" type="date" class="form-control" required>
                    </div>
                    <select name="status" id="status" class="form-select">
                        <option value="BACKLOG">Backlog</option>
                        <option value="ESPERANDO">Esperando</option>
                        <option value="QUASE_FIZ">Quase Fiz Alguma Coisa</option>
                    </select>
                    <input name="categoria" id="categoria" class="form-control" placeholder="Categoria (ex: Trabalho)">
                </div>
                <div class="modal-footer">
                    <button type="button" class="btn btn-outline-secondary" data-bs-dismiss="modal">Cancelar</button>
                    <button type="submit" class="btn btn-dark">Salvar</button>
                </div>
            </form>
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
                <div><div class="small text-uppercase text-secondary">Titulo</div><div id="detalhe-titulo" class="fw-semibold"></div></div>
                <div><div class="small text-uppercase text-secondary">Descricao</div><div id="detalhe-descricao"></div></div>
                <div class="row g-2">
                    <div class="col-6"><div class="small text-uppercase text-secondary">Status</div><div id="detalhe-status"></div></div>
                    <div class="col-6"><div class="small text-uppercase text-secondary">Categoria</div><div id="detalhe-categoria"></div></div>
                </div>
                <div><div class="small text-uppercase text-secondary">Prazo</div><div id="detalhe-prazo"></div></div>
            </div>
            <div class="modal-footer">
                <button type="button" class="btn btn-outline-secondary" data-bs-dismiss="modal">Fechar</button>
            </div>
        </div>
    </div>
</div>

<c:if test="${not empty verDesculpasTarefa}">
<div class="modal fade" id="desculpasTarefaModal" tabindex="-1" aria-hidden="true">
    <div class="modal-dialog modal-lg modal-dialog-centered">
        <div class="modal-content">
            <div class="modal-header">
                <h2 class="modal-title fs-5">Desculpas da Tarefa</h2>
                <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Fechar"></button>
            </div>
            <div class="modal-body">
                <div class="small text-uppercase text-secondary mb-1">Tarefa</div>
                <div class="fw-semibold mb-3"><c:out value="${verDesculpasTarefa.titulo}"/></div>
                <div class="d-flex flex-column gap-2">
                    <c:choose>
                        <c:when test="${empty verDesculpasLista}">
                            <div class="text-secondary">Nenhuma desculpa cadastrada para esta tarefa.</div>
                        </c:when>
                        <c:otherwise>
                            <c:forEach var="d" items="${verDesculpasLista}">
                                <article class="border rounded p-2 bg-light-subtle">
                                    <div class="d-flex justify-content-between gap-2 small text-secondary mb-1">
                                        <span><c:out value="${d.dataHora}"/></span>
                                        <span>Eficacia <c:out value="${d.nivelEficacia != null ? d.nivelEficacia : 0}"/>/10</span>
                                    </div>
                                    <div><c:out value="${d.comentario}"/></div>
                                </article>
                            </c:forEach>
                        </c:otherwise>
                    </c:choose>
                </div>
            </div>
            <div class="modal-footer">
                <button type="button" class="btn btn-outline-secondary" data-bs-dismiss="modal">Fechar</button>
            </div>
        </div>
    </div>
</div>
</c:if>

<div class="modal fade" id="elogioModal" tabindex="-1" aria-hidden="true">
    <div class="modal-dialog modal-dialog-centered">
        <div class="modal-content border-0 shadow-lg rounded-4">
            <div class="modal-header bg-dark text-white rounded-top-4">
                <h5 class="modal-title fw-bold">Elogio Selecionado</h5>
                <button type="button" class="btn-close btn-close-white" data-bs-dismiss="modal"></button>
            </div>
            <div class="modal-body text-center py-4">
                <p id="elogio-modal-mensagem" class="fs-5 fw-semibold text-dark mb-0"></p>
            </div>
            <div class="modal-footer border-0 justify-content-center pb-4">
                <button type="button" class="btn btn-dark px-4 rounded-pill" data-bs-dismiss="modal">Fechar</button>
            </div>
        </div>
    </div>
</div>

<div class="modal fade" id="xingamentoModal" tabindex="-1" aria-hidden="true">
    <div class="modal-dialog modal-dialog-centered">
        <div class="modal-content border-0 shadow-lg rounded-4">
            <div class="modal-header bg-dark text-white rounded-top-4">
                <h5 class="modal-title fw-bold">Xingamento Motivacional</h5>
                <button type="button" class="btn-close btn-close-white" data-bs-dismiss="modal"></button>
            </div>
            <div class="modal-body text-center py-4">
                <p id="xingamento-modal-mensagem" class="fs-5 fw-semibold text-dark mb-0"></p>
            </div>
            <div class="modal-footer border-0 justify-content-center pb-4">
                <button type="button" class="btn btn-dark px-4 rounded-pill" data-bs-dismiss="modal">Fechar</button>
            </div>
        </div>
    </div>
</div>

<div id="toast-status" class="toast-box"></div>
<c:if test="${not empty flashToast}"><span id="server-flash-toast" class="d-none"><c:out value="${flashToast}"/></span></c:if>
<c:if test="${not empty flashElogio}"><span id="server-flash-elogio" class="d-none"><c:out value="${flashElogio}"/></span></c:if>
<c:if test="${not empty flashXingamento}"><span id="server-flash-xingamento" class="d-none"><c:out value="${flashXingamento}"/></span></c:if>
<c:if test="${not empty flashErro}"><span id="server-flash-erro" class="d-none"><c:out value="${flashErro}"/></span></c:if>
<c:if test="${abrirModalDesculpas}"><span id="abrir-modal-desculpas" class="d-none">1</span></c:if>

<script src="${ctx}/js/jquery-4.0.0.min.js"></script>
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
<script>const APP_CONTEXT = '${ctx}';</script>
<script src="${ctx}/js/app.js"></script>
</body>
</html>
