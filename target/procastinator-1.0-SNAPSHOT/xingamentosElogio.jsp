<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
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
<nav class="navbar navbar-expand-lg bg-white border-bottom">
    <div class="container-fluid">
        <span class="navbar-brand fw-bold">Procrastinator</span>
        <div class="navbar-nav ms-auto gap-lg-3 align-items-lg-center">
            <a class="nav-link btn btn-link p-0" href="${pageContext.request.contextPath}/estatisticas.jsp">Estatisticas de Culpa</a>
            <a class="nav-link btn btn-link p-0" href="${pageContext.request.contextPath}/index.jsp">Lista do Nao Fazer</a>
            <a class="nav-link btn btn-link p-0" href="${pageContext.request.contextPath}/timer.jsp">Timer de Descanso</a>
            <a class="nav-link btn btn-link p-0" href="${pageContext.request.contextPath}/desculpas.jsp">Log de Desculpas</a>
            <a class="nav-link btn btn-link p-0 fw-bold border-bottom border-dark" href="${pageContext.request.contextPath}/xingamentosElogio.jsp">Xingamentos e Elogio</a>
            <a class="nav-link btn btn-link p-0" href="${pageContext.request.contextPath}/recompensas.jsp">Recompensas</a>
        </div>
    </div>
</nav>

<div class="container-fluid">
    <div class="row min-vh-100">
        <aside class="col-12 col-lg-2 border-end bg-body-tertiary p-3">
            <h6 class="text-uppercase text-secondary small fw-bold mb-3">Painel</h6>
            <div class="list-group list-group-flush small">
                <a class="list-group-item list-group-item-action bg-transparent text-secondary" href="${pageContext.request.contextPath}/estatisticas.jsp">Estatisticas de Culpa</a>
                <a class="list-group-item list-group-item-action bg-transparent text-secondary" href="${pageContext.request.contextPath}/index.jsp">Lista do Nao Fazer</a>
                <a class="list-group-item list-group-item-action bg-transparent text-secondary" href="${pageContext.request.contextPath}/timer.jsp">Timer de Descanso</a>
                <a class="list-group-item list-group-item-action bg-transparent text-secondary" href="${pageContext.request.contextPath}/desculpas.jsp">Log de Desculpas</a>
                <a class="list-group-item list-group-item-action bg-white fw-semibold rounded" href="${pageContext.request.contextPath}/xingamentosElogio.jsp">Xingamentos e Elogio</a>
                <a class="list-group-item list-group-item-action bg-transparent text-secondary" href="${pageContext.request.contextPath}/recompensas.jsp">Recompensas</a>
            </div>
            <div class="card mt-4 border-0 bg-transparent">
                <div class="card-body p-0 small">
                    <div class="fw-bold text-uppercase">Setor de Inercia</div>
                    <div class="text-muted">Protocolo 0800-NADA</div>
                    <button type="button" class="btn btn-dark btn-sm w-100 mt-2">Desistir Cedo</button>
                </div>
            </div>
        </aside>

        <main class="col-12 col-lg-10 p-4 avisos-page">
            <section class="avisos-hero mb-4">
                <div class="d-flex justify-content-between align-items-start gap-3 flex-wrap">
                    <div>
                        <h1 class="h2 mb-2">Xingamentos e Elogio</h1>
                        <p class="text-secondary mb-0">Cadastre avisos com mensagem e tipo para usar no sistema.</p>
                    </div>
                    <div class="text-uppercase small text-secondary fw-semibold">Gerencia de Mensagens</div>
                </div>
            </section>

            <div class="row g-3">
                <div class="col-12 col-xl-4">
                    <div class="card border-0 shadow-sm h-100 aviso-form-card">
                        <div class="card-body p-4">
                            <h2 class="h6 text-uppercase text-secondary mb-3">Novo aviso</h2>
                            <form id="aviso-form" class="d-flex flex-column gap-3">
                                <div>
                                    <label for="aviso-mensagem" class="form-label small text-uppercase text-secondary">Mensagem</label>
                                    <textarea id="aviso-mensagem" class="form-control" rows="4"
                                              placeholder="Ex: Vamos focar antes que o prazo cobre." maxlength="255" required></textarea>
                                </div>
                                <div>
                                    <label for="aviso-tipo" class="form-label small text-uppercase text-secondary">Tipo</label>
                                    <select id="aviso-tipo" class="form-select form-select-sm">
                                        <option value="XINGAMENTO">Xingamento</option>
                                        <option value="ELOGIO">Elogio</option>
                                    </select>
                                </div>
                                <button type="submit" class="btn btn-dark">Salvar aviso</button>
                            </form>
                        </div>
                    </div>
                </div>

                <div class="col-12 col-xl-8">
                    <div class="d-flex justify-content-between align-items-center gap-2 mb-2 flex-wrap">
                        <h2 class="h6 text-uppercase text-secondary mb-0">Avisos cadastrados <span id="count-avisos" class="badge text-bg-light ms-1">0</span></h2>
                        <input id="filtro-avisos" type="search" class="form-control form-control-sm aviso-filtro" placeholder="Filtrar por mensagem ou tipo...">
                    </div>
                    <div id="lista-avisos" class="d-flex flex-column gap-3"></div>
                </div>
            </div>
        </main>
    </div>
</div>

<div id="toast-status" class="toast-box"></div>

<div class="modal fade" id="avisoDetailsModal" tabindex="-1" aria-hidden="true">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header">
                <h2 class="modal-title fs-5">Detalhes do Aviso</h2>
                <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Fechar"></button>
            </div>
            <div class="modal-body d-flex flex-column gap-3">
                <div>
                    <div class="form-label small text-uppercase text-secondary mb-1">Tipo</div>
                    <div id="detalhe-aviso-tipo-label" class="badge text-bg-dark">XINGAMENTO</div>
                </div>
                <div>
                    <label for="detalhe-aviso-mensagem" class="form-label small text-uppercase text-secondary">Mensagem</label>
                    <textarea id="detalhe-aviso-mensagem" class="form-control" rows="4" maxlength="255" required></textarea>
                </div>
            </div>
            <div class="modal-footer">
                <button type="button" id="btn-excluir-aviso" class="btn btn-outline-danger me-auto">Excluir</button>
                <button type="button" class="btn btn-outline-secondary" data-bs-dismiss="modal">Cancelar</button>
                <button type="button" id="btn-atualizar-aviso" class="btn btn-dark">Salvar alteracoes</button>
            </div>
        </div>
    </div>
</div>

<script src="https://code.jquery.com/jquery-3.7.1.min.js"></script>
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
<script>
    const APP_CONTEXT = '${pageContext.request.contextPath}';
</script>
<script src="${pageContext.request.contextPath}/js/xingamentosElogio.js?v=<%= System.currentTimeMillis() %>"></script>
</body>
</html>


