<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<aside class="col-12 col-lg-2 border-end bg-body-tertiary p-3">
    <h6 class="text-uppercase text-secondary small fw-bold mb-3">Painel</h6>
    <div class="list-group list-group-flush small">
        <a class="list-group-item list-group-item-action ${navAtivo == 'estatisticas' ? 'bg-white fw-semibold rounded' : 'bg-transparent text-secondary'}"
           href="${ctx}/estatisticas">Estatisticas de Culpa</a>
        <a class="list-group-item list-group-item-action ${navAtivo == 'lista' ? 'bg-white fw-semibold rounded' : 'bg-transparent text-secondary'}"
           href="${ctx}/tarefas">Lista do Nao Fazer</a>
        <a class="list-group-item list-group-item-action ${navAtivo == 'timer' ? 'bg-white fw-semibold rounded' : 'bg-transparent text-secondary'}"
           href="${ctx}/timer">Timer de Descanso</a>
        <a class="list-group-item list-group-item-action ${navAtivo == 'desculpas' ? 'bg-white fw-semibold rounded' : 'bg-transparent text-secondary'}"
           href="${ctx}/desculpas">Log de Desculpas</a>
        <a class="list-group-item list-group-item-action ${navAtivo == 'avisos' ? 'bg-white fw-semibold rounded' : 'bg-transparent text-secondary'}"
           href="${ctx}/avisos">Xingamentos e Elogio</a>
        <a class="list-group-item list-group-item-action ${navAtivo == 'recompensas' ? 'bg-white fw-semibold rounded' : 'bg-transparent text-secondary'}"
           href="${ctx}/recompensas">Recompensas</a>
    </div>
    <div class="card mt-4 border-0 bg-transparent">
        <div class="card-body p-0 small">
            <div class="fw-bold text-uppercase mb-1">
                <c:out value="${empty sessionScope.usuarioNome ? 'Usuario' : sessionScope.usuarioNome}"/>
            </div>
            <div class="text-muted text-break mb-2">
                <c:out value="${empty sessionScope.usuarioEmail ? '' : sessionScope.usuarioEmail}"/>
            </div>
            <form method="post" action="${ctx}/logout" class="mb-0">
                <button type="submit" class="btn btn-dark btn-sm w-100">Sair</button>
            </form>
        </div>
    </div>
</aside>
