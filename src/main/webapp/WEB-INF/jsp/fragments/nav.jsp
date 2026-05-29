
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<nav class="navbar navbar-expand-lg bg-white border-bottom">
    <div class="container-fluid">
        <span class="navbar-brand fw-bold">Procrastinator</span>
        <div class="navbar-nav ms-auto gap-lg-3 align-items-lg-center">
            <a class="nav-link btn btn-link p-0 ${navAtivo == 'estatisticas' ? 'fw-bold border-bottom border-dark' : ''}"
               href="${ctx}/estatisticas">Estatisticas de Culpa</a>
            <a class="nav-link btn btn-link p-0 ${navAtivo == 'lista' ? 'fw-bold border-bottom border-dark' : ''}"
               href="${ctx}/tarefas">Lista do Nao Fazer</a>
            <a class="nav-link btn btn-link p-0 ${navAtivo == 'equipes' ? 'fw-bold border-bottom border-dark' : ''}"
               href="${ctx}/equipes">Equipes</a>
            <a class="nav-link btn btn-link p-0 ${navAtivo == 'timer' ? 'fw-bold border-bottom border-dark' : ''}"
               href="${ctx}/timer">Timer de Descanso</a>
            <a class="nav-link btn btn-link p-0 ${navAtivo == 'desculpas' ? 'fw-bold border-bottom border-dark' : ''}"
               href="${ctx}/desculpas">Log de Desculpas</a>
            <a class="nav-link btn btn-link p-0 ${navAtivo == 'avisos' ? 'fw-bold border-bottom border-dark' : ''}"
               href="${ctx}/avisos">Xingamentos e Elogio</a>
            <a class="nav-link btn btn-link p-0 ${navAtivo == 'recompensas' ? 'fw-bold border-bottom border-dark' : ''}"
               href="${ctx}/recompensas">Recompensas</a>
        </div>
    </div>
</nav>
