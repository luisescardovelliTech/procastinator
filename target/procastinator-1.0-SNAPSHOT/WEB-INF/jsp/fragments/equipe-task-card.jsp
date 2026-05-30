<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<c:set var="tarefa" value="${requestScope.tarefa}"/>
<c:choose>
    <c:when test="${tarefa.categoria != null && not empty tarefa.categoria.nome}">
        <c:set var="cat" value="${tarefa.categoria.nome}"/>
    </c:when>
    <c:otherwise>
        <c:set var="cat" value="GERAL"/>
    </c:otherwise>
</c:choose>
<article class="task-card mb-3"
         data-id="<c:out value='${tarefa.id}'/>"
         data-titulo="<c:out value='${tarefa.titulo}'/>"
         data-descricao="<c:out value='${tarefa.descricao}'/>"
         data-status="<c:out value='${tarefa.status}'/>"
         data-prazo="<c:out value='${tarefa.dataPrazo}'/>"
         data-categoria="<c:out value='${cat}'/>">
    <span class="task-badge"><c:out value="${cat}"/></span>
    <h3 class="task-title">Titulo: <c:out value="${tarefa.titulo}"/></h3>
    <p class="task-desc">Descricao: <c:out value="${empty tarefa.descricao ? 'Sem descricao' : tarefa.descricao}"/></p>
    <p class="task-desc small text-secondary mb-1">
        Responsavel:
        <c:choose>
            <c:when test="${tarefa.responsavel != null}">
                <strong><c:out value="${tarefa.responsavel.nome}"/></strong>
            </c:when>
            <c:otherwise>Nao atribuido</c:otherwise>
        </c:choose>
        <c:if test="${isLider}">
            <button class="btn btn-link btn-sm p-0 ms-1 text-dark" style="font-size:.72rem"
                    onclick="abrirAtribuir(${tarefa.id}, '${tarefa.responsavel != null ? tarefa.responsavel.id : ''}')">
                [atribuir]
            </button>
        </c:if>
    </p>
    <div class="task-tags">
        <c:choose>
            <c:when test="${empty tarefa.xingamentos}">Sem incentivo agressivo</c:when>
            <c:otherwise>
                <c:forEach items="${tarefa.xingamentos}" var="x" varStatus="st">
                    <c:if test="${not st.first}"> | </c:if><c:out value="${x.mensagem}"/>
                </c:forEach>
            </c:otherwise>
        </c:choose>
    </div>
    <div class="task-actions task-status-actions d-flex gap-2 mb-2 flex-wrap">
        <form method="post" action="${ctx}/equipes/tarefa" class="d-inline">
            <input type="hidden" name="acao" value="moverEquipe"/>
            <input type="hidden" name="equipeId" value="${equipe.id}"/>
            <input type="hidden" name="tarefaId" value="<c:out value='${tarefa.id}'/>"/>
            <input type="hidden" name="status" value="BACKLOG"/>
            <button type="submit" class="btn btn-sm btn-outline-secondary">Backlog</button>
        </form>
        <form method="post" action="${ctx}/equipes/tarefa" class="d-inline">
            <input type="hidden" name="acao" value="moverEquipe"/>
            <input type="hidden" name="equipeId" value="${equipe.id}"/>
            <input type="hidden" name="tarefaId" value="<c:out value='${tarefa.id}'/>"/>
            <input type="hidden" name="status" value="ESPERANDO"/>
            <button type="submit" class="btn btn-sm btn-outline-secondary">Esperando</button>
        </form>
        <form method="post" action="${ctx}/equipes/tarefa" class="d-inline">
            <input type="hidden" name="acao" value="moverEquipe"/>
            <input type="hidden" name="equipeId" value="${equipe.id}"/>
            <input type="hidden" name="tarefaId" value="<c:out value='${tarefa.id}'/>"/>
            <input type="hidden" name="status" value="QUASE_FIZ"/>
            <button type="submit" class="btn btn-sm btn-outline-secondary">Quase Fiz</button>
        </form>
    </div>
    <c:if test="${isLider}">
        <div class="task-actions task-crud-actions d-flex gap-2 flex-wrap">
            <button type="button" class="btn btn-sm btn-outline-primary btn-edit-equipe">Editar</button>
            <form method="post" action="${ctx}/equipes/tarefa" class="d-inline"
                  onsubmit="return confirm('Excluir esta tarefa?')">
                <input type="hidden" name="acao" value="excluirEquipe"/>
                <input type="hidden" name="equipeId" value="${equipe.id}"/>
                <input type="hidden" name="tarefaId" value="<c:out value='${tarefa.id}'/>"/>
                <button type="submit" class="btn btn-sm btn-outline-danger">Excluir</button>
            </form>
        </div>
    </c:if>
</article>
