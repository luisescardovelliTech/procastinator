<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<!DOCTYPE html>
<html lang="pt-BR">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Procrastinator | <c:out value="${equipe.nome}"/></title>
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/procrastinator.css">
</head>
<body class="bg-light">
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<c:set var="equipeId" value="${equipe.id}"/>
<jsp:include page="fragments/nav.jsp"/>

<div class="container-fluid">
    <div class="row min-vh-100">
        <jsp:include page="fragments/sidebar.jsp"/>

        <main class="col-12 col-lg-10 p-4">

            <%-- Cabecalho --%>
            <div class="d-flex justify-content-between align-items-start mb-1 flex-wrap gap-2">
                <div>
                    <a href="${ctx}/equipes" class="text-secondary text-decoration-none small">&#8592; Minhas Equipes</a>
                    <h1 class="h4 fw-bold mb-0 mt-1"><c:out value="${equipe.nome}"/></h1>
                    <p class="text-secondary small mb-0">
                        Lider: <strong><c:out value="${equipe.lider.nome}"/></strong>
                        <c:if test="${not empty equipe.descricao}">
                            &nbsp;&mdash;&nbsp;<c:out value="${equipe.descricao}"/>
                        </c:if>
                    </p>
                </div>
                <div class="d-flex gap-2 flex-wrap">
                    <button class="btn btn-sm btn-outline-dark" data-bs-toggle="modal" data-bs-target="#membrosModal">
                        Membros (<c:out value="${fn:length(membros)}"/>)
                    </button>
                    <c:if test="${isLider}">
                        <button class="btn btn-sm btn-outline-dark" data-bs-toggle="modal" data-bs-target="#criarTarefaEquipeModal">
                            + Nova Tarefa
                        </button>
                        <button class="btn btn-sm btn-outline-secondary" data-bs-toggle="modal" data-bs-target="#editarEquipeModal">
                            Editar
                        </button>
                        <form method="post" action="${ctx}/equipes"
                              onsubmit="return confirm('Excluir a equipe permanentemente?')">
                            <input type="hidden" name="acao" value="excluir"/>
                            <input type="hidden" name="equipeId" value="${equipeId}"/>
                            <button type="submit" class="btn btn-sm btn-outline-danger">Excluir</button>
                        </form>
                    </c:if>
                </div>
            </div>

            <hr class="my-3"/>

            <section id="view-kanban-equipe">
                <div class="row mb-3 text-uppercase fw-bold small text-secondary">
                    <div class="col-12 col-lg-4">Backlog</div>
                    <div class="col-12 col-lg-4">Esperando</div>
                    <div class="col-12 col-lg-4">Quase Fiz Alguma Coisa</div>
                </div>
                <div class="row g-3">

                    <div class="col-12 col-lg-4">
                        <div class="kanban-column">
                            <c:forEach var="t" items="${tarefasEquipe}">
                                <c:if test="${t.status == 'BACKLOG' || t.status == null}">
                                    <c:set var="tarefa" scope="request" value="${t}"/>
                                    <%@ include file="fragments/equipe-task-card.jsp" %>
                                </c:if>
                            </c:forEach>
                        </div>
                        <c:if test="${isLider}">
                            <button type="button" class="btn btn-outline-secondary btn-sm w-100 border-dashed mt-2"
                                    data-bs-toggle="modal" data-bs-target="#criarTarefaEquipeModal">+ ADICIONAR TAREFA</button>
                        </c:if>
                    </div>

                    <div class="col-12 col-lg-4">
                        <div class="kanban-column">
                            <c:forEach var="t" items="${tarefasEquipe}">
                                <c:if test="${t.status == 'ESPERANDO'}">
                                    <c:set var="tarefa" scope="request" value="${t}"/>
                                    <%@ include file="fragments/equipe-task-card.jsp" %>
                                </c:if>
                            </c:forEach>
                        </div>
                    </div>

                    <div class="col-12 col-lg-4">
                        <div class="kanban-column">
                            <c:forEach var="t" items="${tarefasEquipe}">
                                <c:if test="${t.status == 'QUASE_FIZ'}">
                                    <c:set var="tarefa" scope="request" value="${t}"/>
                                    <%@ include file="fragments/equipe-task-card.jsp" %>
                                </c:if>
                            </c:forEach>
                        </div>
                    </div>

                </div>
            </section>

        </main>
    </div>
</div>

<div class="modal fade" id="membrosModal" tabindex="-1" aria-hidden="true">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header">
                <h5 class="modal-title fw-bold">Membros da Equipe</h5>
                <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
            </div>
            <div class="modal-body p-0">
                <ul class="list-group list-group-flush">
                    <c:forEach var="m" items="${membros}">
                        <li class="list-group-item d-flex justify-content-between align-items-center">
                            <div>
                                <div class="fw-semibold small"><c:out value="${m.usuario.nome}"/></div>
                                <div class="text-muted" style="font-size:.75rem"><c:out value="${m.usuario.email}"/></div>
                            </div>
                            <div class="d-flex align-items-center gap-2">
                                <span class="badge ${m.papel == 'LIDER' ? 'bg-dark' : 'bg-secondary'}">
                                    <c:out value="${m.papel}"/>
                                </span>
                                <c:if test="${isLider && m.papel != 'LIDER'}">
                                    <form method="post" action="${ctx}/equipes"
                                          onsubmit="return confirm('Remover este membro?')">
                                        <input type="hidden" name="acao" value="removerMembro"/>
                                        <input type="hidden" name="equipeId" value="${equipeId}"/>
                                        <input type="hidden" name="membroId" value="${m.usuario.id}"/>
                                        <button type="submit" class="btn btn-sm btn-outline-danger py-0 px-1"
                                                style="font-size:.7rem">✕</button>
                                    </form>
                                </c:if>
                            </div>
                        </li>
                    </c:forEach>
                </ul>
            </div>
            <c:if test="${isLider}">
                <div class="modal-footer flex-column align-items-stretch gap-2">
                    <div class="fw-semibold small text-uppercase text-secondary">Adicionar Membro</div>
                    <form action="${ctx}/equipes" method="post" class="d-flex gap-2">
                        <input type="hidden" name="acao" value="adicionarMembro"/>
                        <input type="hidden" name="equipeId" value="${equipeId}"/>
                        <input name="email" type="email" class="form-control form-control-sm"
                               placeholder="email@exemplo.com" required>
                        <button type="submit" class="btn btn-dark btn-sm text-nowrap">Adicionar</button>
                    </form>
                    <div class="form-text mt-0">O usuario ja deve ter conta no Procrastinator.</div>
                </div>
            </c:if>
        </div>
    </div>
</div>

<c:if test="${isLider}">
    <div class="modal fade" id="criarTarefaEquipeModal" tabindex="-1" aria-hidden="true">
        <div class="modal-dialog">
            <div class="modal-content">
                <form action="${ctx}/equipes/tarefa" method="post">
                    <input type="hidden" name="acao" value="criarEquipe"/>
                    <input type="hidden" name="equipeId" value="${equipeId}"/>
                    <div class="modal-header">
                        <h5 class="modal-title fw-bold">Nova Tarefa da Equipe</h5>
                        <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
                    </div>
                    <div class="modal-body d-flex flex-column gap-3">
                        <input name="titulo" class="form-control" placeholder="Titulo da tarefa" maxlength="100" required>
                        <textarea name="descricao" class="form-control" rows="2" placeholder="Descricao"></textarea>
                        <div>
                            <label class="form-label small text-uppercase text-secondary">Prazo *</label>
                            <input name="dataPrazo" type="date" class="form-control" required>
                        </div>
                        <div>
                            <label class="form-label small text-uppercase text-secondary">Responsavel</label>
                            <select name="responsavelId" class="form-select">
                                <option value="">-- Nao atribuir --</option>
                                <c:forEach var="m" items="${membros}">
                                    <option value="${m.usuario.id}">
                                        <c:out value="${m.usuario.nome}"/> (<c:out value="${m.usuario.email}"/>)
                                    </option>
                                </c:forEach>
                            </select>
                        </div>
                        <select name="status" class="form-select">
                            <option value="BACKLOG">Backlog</option>
                            <option value="ESPERANDO">Esperando</option>
                            <option value="QUASE_FIZ">Quase Fiz</option>
                        </select>
                        <input name="categoria" class="form-control" placeholder="Categoria (ex: Backend)">
                    </div>
                    <div class="modal-footer">
                        <button type="button" class="btn btn-outline-secondary" data-bs-dismiss="modal">Cancelar</button>
                        <button type="submit" class="btn btn-dark">Criar Tarefa</button>
                    </div>
                </form>
            </div>
        </div>
    </div>

    <div class="modal fade" id="editarTarefaEquipeModal" tabindex="-1" aria-hidden="true">
        <div class="modal-dialog">
            <div class="modal-content">
                <form action="${ctx}/equipes/tarefa" method="post">
                    <input type="hidden" name="acao" value="editarEquipe"/>
                    <input type="hidden" name="equipeId" value="${equipeId}"/>
                    <input type="hidden" name="tarefaId" id="edit-tarefa-id"/>
                    <div class="modal-header">
                        <h5 class="modal-title fw-bold">Editar Tarefa</h5>
                        <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
                    </div>
                    <div class="modal-body d-flex flex-column gap-3">
                        <input name="titulo" id="edit-titulo" class="form-control"
                               placeholder="Titulo da tarefa" maxlength="100" required>
                        <textarea name="descricao" id="edit-descricao" class="form-control"
                                  rows="3" placeholder="Descricao"></textarea>
                        <div>
                            <label class="form-label small text-uppercase text-secondary">Prazo *</label>
                            <input name="dataPrazo" id="edit-prazo" type="date" class="form-control" required>
                        </div>
                        <select name="status" id="edit-status" class="form-select">
                            <option value="BACKLOG">Backlog</option>
                            <option value="ESPERANDO">Esperando</option>
                            <option value="QUASE_FIZ">Quase Fiz</option>
                        </select>
                        <input name="categoria" id="edit-categoria" class="form-control"
                               placeholder="Categoria (ex: Backend)">
                        <div>
                            <label class="form-label small text-uppercase text-secondary">Responsavel</label>
                            <select name="responsavelId" id="edit-responsavel" class="form-select">
                                <option value="">-- Nao atribuir --</option>
                                <c:forEach var="m" items="${membros}">
                                    <option value="${m.usuario.id}">
                                        <c:out value="${m.usuario.nome}"/> (<c:out value="${m.usuario.email}"/>)
                                    </option>
                                </c:forEach>
                            </select>
                        </div>
                    </div>
                    <div class="modal-footer">
                        <button type="button" class="btn btn-outline-secondary" data-bs-dismiss="modal">Cancelar</button>
                        <button type="submit" class="btn btn-dark">Salvar</button>
                    </div>
                </form>
            </div>
        </div>
    </div>

    <div class="modal fade" id="editarEquipeModal" tabindex="-1" aria-hidden="true">
        <div class="modal-dialog">
            <div class="modal-content">
                <form action="${ctx}/equipes" method="post">
                    <input type="hidden" name="acao" value="atualizar"/>
                    <input type="hidden" name="equipeId" value="${equipeId}"/>
                    <div class="modal-header">
                        <h5 class="modal-title fw-bold">Editar Equipe</h5>
                        <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
                    </div>
                    <div class="modal-body d-flex flex-column gap-3">
                        <div>
                            <label class="form-label small text-uppercase text-secondary">Nome *</label>
                            <input name="nome" class="form-control" maxlength="100" required
                                   value="<c:out value='${equipe.nome}'/>">
                        </div>
                        <div>
                            <label class="form-label small text-uppercase text-secondary">Descricao</label>
                            <textarea name="descricao" class="form-control" rows="3"><c:out value="${equipe.descricao}"/></textarea>
                        </div>
                    </div>
                    <div class="modal-footer">
                        <button type="button" class="btn btn-outline-secondary" data-bs-dismiss="modal">Cancelar</button>
                        <button type="submit" class="btn btn-dark">Salvar</button>
                    </div>
                </form>
            </div>
        </div>
    </div>

    <div class="modal fade" id="atribuirModal" tabindex="-1" aria-hidden="true">
        <div class="modal-dialog">
            <div class="modal-content">
                <form action="${ctx}/equipes/tarefa" method="post">
                    <input type="hidden" name="acao" value="atribuir"/>
                    <input type="hidden" name="equipeId" value="${equipeId}"/>
                    <input type="hidden" name="tarefaId" id="atribuir-tarefa-id"/>
                    <div class="modal-header">
                        <h5 class="modal-title fw-bold">Atribuir Responsavel</h5>
                        <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
                    </div>
                    <div class="modal-body">
                        <select name="responsavelId" id="atribuir-select" class="form-select">
                            <option value="">-- Remover responsavel --</option>
                            <c:forEach var="m" items="${membros}">
                                <option value="${m.usuario.id}">
                                    <c:out value="${m.usuario.nome}"/> (<c:out value="${m.usuario.email}"/>)
                                </option>
                            </c:forEach>
                        </select>
                    </div>
                    <div class="modal-footer">
                        <button type="button" class="btn btn-outline-secondary" data-bs-dismiss="modal">Cancelar</button>
                        <button type="submit" class="btn btn-dark">Atribuir</button>
                    </div>
                </form>
            </div>
        </div>
    </div>
</c:if>

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

<c:if test="${not empty flashToast}"><span id="server-flash-toast" class="d-none"><c:out value="${flashToast}"/></span></c:if>
<c:if test="${not empty flashErro}"><span id="server-flash-erro" class="d-none"><c:out value="${flashErro}"/></span></c:if>
<c:if test="${not empty flashXingamento}"><span id="server-flash-xingamento" class="d-none"><c:out value="${flashXingamento}"/></span></c:if>

<div id="toast-status" class="toast-box"></div>
<script src="${ctx}/js/jquery-4.0.0.min.js"></script>
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
<script src="${ctx}/js/app.js"></script>
<script>
    function abrirAtribuir(tarefaId, responsavelAtualId) {
        document.getElementById('atribuir-tarefa-id').value = tarefaId;
        var select = document.getElementById('atribuir-select');
        if (select && responsavelAtualId) select.value = responsavelAtualId;
        new bootstrap.Modal(document.getElementById('atribuirModal')).show();
    }

    $(document).on('click', '.btn-edit-equipe', function () {
        var $card = $(this).closest('.task-card');
        document.getElementById('edit-tarefa-id').value  = $card.data('id');
        document.getElementById('edit-titulo').value     = $card.data('titulo') || '';
        document.getElementById('edit-descricao').value  = $card.data('descricao') || '';
        document.getElementById('edit-status').value     = $card.data('status') || 'BACKLOG';
        document.getElementById('edit-categoria').value  = $card.data('categoria') || '';
        var prazo = $card.data('prazo');
        document.getElementById('edit-prazo').value = prazo ? String(prazo).substring(0, 10) : '';
        new bootstrap.Modal(document.getElementById('editarTarefaEquipeModal')).show();
    });
</script>
</body>
</html>
