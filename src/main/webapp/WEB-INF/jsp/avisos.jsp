<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<!DOCTYPE html>
<html lang="pt-BR">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Procrastinator | Xingamentos e Elogio</title>
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/procrastinator.css">
</head>
<body class="bg-light">
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<jsp:include page="fragments/nav.jsp"/>
<div class="container-fluid">
    <div class="row min-vh-100">
        <jsp:include page="fragments/sidebar.jsp"/>
        <main class="col-12 col-lg-10 p-4 avisos-page">
            <section class="avisos-hero mb-4">
                <h1 class="h2 mb-2">Xingamentos e Elogio</h1>
                <p class="text-secondary mb-0">Cadastre avisos com mensagem e tipo para usar no sistema.</p>
            </section>
            <div class="row g-3">
                <div class="col-12 col-xl-4">
                    <div class="card border-0 shadow-sm h-100">
                        <div class="card-body p-4">
                            <h2 class="h6 text-uppercase text-secondary mb-3">Novo aviso</h2>
                            <form method="post" action="${ctx}/avisos" class="d-flex flex-column gap-3">
                                <input type="hidden" name="acao" value="criar"/>
                                <textarea name="mensagem" class="form-control" rows="4" maxlength="255" required
                                          placeholder="Ex: Vamos focar antes que o prazo cobre."></textarea>
                                <select name="tipo" class="form-select form-select-sm">
                                    <option value="XINGAMENTO">Xingamento</option>
                                    <option value="ELOGIO">Elogio</option>
                                </select>
                                <button type="submit" class="btn btn-dark">Salvar aviso</button>
                            </form>
                        </div>
                    </div>
                </div>
                <div class="col-12 col-xl-8">
                    <div class="d-flex justify-content-between align-items-center gap-2 mb-2 flex-wrap">
                        <h2 class="h6 text-uppercase text-secondary mb-0">Avisos <span id="count-avisos" class="badge text-bg-light ms-1"><c:out value="${fn:length(avisos)}"/></span></h2>
                        <input id="filtro-avisos" type="search" class="form-control form-control-sm aviso-filtro" placeholder="Filtrar...">
                    </div>
                    <div id="lista-avisos" class="d-flex flex-column gap-3">
                        <c:forEach var="a" items="${avisos}">
                            <article class="aviso-card" data-id="<c:out value='${a.id}'/>"
                                     data-tipo="<c:out value='${a.tipo}'/>">
                                <span class="d-none aviso-mensagem-store"><c:out value="${a.mensagem}"/></span>
                                <span class="d-none aviso-search-blob"><c:out value="${a.mensagem}"/> <c:out value="${a.tipo}"/></span>
                                <header class="d-flex justify-content-between mb-2">
                                    <span class="aviso-badge">FRASE #<c:out value="${a.id}"/></span>
                                    <span class="badge text-bg-dark"><c:out value="${a.tipo}"/></span>
                                </header>
                                <p class="mb-3"><c:out value="${a.mensagem}"/></p>
                                <div class="text-end">
                                    <button type="button" class="btn btn-sm btn-outline-dark btn-view-aviso">Ver detalhes</button>
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

<div class="modal fade" id="avisoDetailsModal" tabindex="-1" aria-hidden="true">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header"><h2 class="modal-title fs-5">Detalhes do Aviso</h2><button type="button" class="btn-close" data-bs-dismiss="modal"></button></div>
            <form id="form-atualizar-aviso" method="post" action="${ctx}/avisos">
                <input type="hidden" name="acao" value="atualizar"/>
                <input type="hidden" name="id" id="edit-aviso-id"/>
                <input type="hidden" name="tipo" id="edit-aviso-tipo"/>
                <div class="modal-body d-flex flex-column gap-3">
                    <div>Tipo: <span id="detalhe-aviso-tipo-label" class="badge text-bg-dark"></span></div>
                    <textarea name="mensagem" id="detalhe-aviso-mensagem" class="form-control" rows="4" maxlength="255" required></textarea>
                </div>
            </form>
            <form id="form-excluir-aviso" method="post" action="${ctx}/avisos" class="d-none" onsubmit="return confirm('Excluir esta frase?');">
                <input type="hidden" name="acao" value="excluir"/>
                <input type="hidden" name="id" id="excluir-aviso-id"/>
            </form>
            <div class="modal-footer">
                <button type="submit" form="form-excluir-aviso" class="btn btn-outline-danger me-auto">Excluir</button>
                <button type="button" class="btn btn-outline-secondary" data-bs-dismiss="modal">Cancelar</button>
                <button type="submit" form="form-atualizar-aviso" class="btn btn-dark">Salvar</button>
            </div>
        </div>
    </div>
</div>
<script src="${ctx}/js/jquery-4.0.0.min.js"></script>
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
<script>const APP_CONTEXT = '${ctx}';</script>
<script src="${ctx}/js/xingamentosElogio.js"></script>
</body>
</html>
