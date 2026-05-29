<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<!DOCTYPE html>
<html lang="pt-BR">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Procrastinator | Equipes</title>
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

            <%-- Flash oculto para o app.js processar --%>
            <c:if test="${not empty flashToast}">
                <span id="server-flash-toast" class="d-none"><c:out value="${flashToast}"/></span>
            </c:if>
            <c:if test="${not empty flashErro}">
                <span id="server-flash-erro" class="d-none"><c:out value="${flashErro}"/></span>
            </c:if>

            <div class="d-flex justify-content-between align-items-center mb-4">
                <h1 class="h4 fw-bold mb-0">Minhas Equipes</h1>
                <button class="btn btn-dark btn-sm" data-bs-toggle="modal" data-bs-target="#criarEquipeModal">
                    + Nova Equipe
                </button>
            </div>

            <c:choose>
                <c:when test="${empty equipes}">
                    <div class="text-center text-secondary py-5">
                        <p class="fs-5">Voce ainda nao faz parte de nenhuma equipe.</p>
                        <p>Crie uma equipe ou peca ao seu lider para te adicionar.</p>
                    </div>
                </c:when>
                <c:otherwise>
                    <div class="row g-3">
                        <c:forEach var="e" items="${equipes}">
                            <div class="col-12 col-md-6 col-xl-4">
                                <div class="card h-100 shadow-sm">
                                    <div class="card-body">
                                        <h5 class="card-title fw-bold">
                                            <a href="${ctx}/equipes/${e.id}" class="text-decoration-none text-dark">
                                                <c:out value="${e.nome}"/>
                                            </a>
                                        </h5>
                                        <p class="card-text text-secondary small">
                                            <c:out value="${empty e.descricao ? 'Sem descricao' : e.descricao}"/>
                                        </p>
                                        <div class="small text-muted">
                                            Lider: <strong><c:out value="${e.lider.nome}"/></strong>
                                        </div>
                                    </div>
                                    <div class="card-footer bg-transparent border-top-0">
                                        <a href="${ctx}/equipes/${e.id}" class="btn btn-sm btn-outline-dark w-100">
                                            Ver Equipe
                                        </a>
                                    </div>
                                </div>
                            </div>
                        </c:forEach>
                    </div>
                </c:otherwise>
            </c:choose>

        </main>
    </div>
</div>

<div class="modal fade" id="criarEquipeModal" tabindex="-1" aria-hidden="true">
    <div class="modal-dialog">
        <div class="modal-content">
            <form action="${ctx}/equipes" method="post">
                <input type="hidden" name="acao" value="criar"/>
                <div class="modal-header">
                    <h5 class="modal-title fw-bold">Nova Equipe</h5>
                    <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
                </div>
                <div class="modal-body d-flex flex-column gap-3">
                    <div>
                        <label for="equipe-nome" class="form-label small text-uppercase text-secondary">Nome da equipe *</label>
                        <input id="equipe-nome" name="nome" class="form-control" maxlength="100" required
                               placeholder="Ex: Squad Backend">
                    </div>
                    <div>
                        <label for="equipe-desc" class="form-label small text-uppercase text-secondary">Descricao</label>
                        <textarea id="equipe-desc" name="descricao" class="form-control" rows="3"
                                  placeholder="Descreva o objetivo da equipe (opcional)"></textarea>
                    </div>
                </div>
                <div class="modal-footer">
                    <button type="button" class="btn btn-outline-secondary" data-bs-dismiss="modal">Cancelar</button>
                    <button type="submit" class="btn btn-dark">Criar Equipe</button>
                </div>
            </form>
        </div>
    </div>
</div>

<div id="toast-status" class="toast-box"></div>
<script src="${ctx}/js/jquery-4.0.0.min.js"></script>
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
<script src="${ctx}/js/app.js"></script>
</body>
</html>
