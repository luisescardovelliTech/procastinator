<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="pt-BR">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Procrastinator | Cadastro</title>

    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/registro.css">
</head>

<body class="auth-page">

<c:set var="ctx" value="${pageContext.request.contextPath}"/>

<div class="container min-vh-100 d-flex align-items-center justify-content-center">
    <div class="col-12 col-sm-10 col-md-7 col-lg-5 col-xl-4">
        <div class="card register-card shadow-lg border-0">
            <div class="card-body">
                <div class="text-center mb-4">
                    <h1 class="page-title mb-2">Criar conta</h1>
                    <p class="subtitle mb-0"> Cadastre-se e organize sua culpa em um só lugar.</p>
                </div>
                <c:if test="${not empty flashErro}">
                    <div class="alert alert-danger py-2" role="alert"><c:out value="${flashErro}"/></div>
                </c:if>

                <form method="post" action="${ctx}/registro" class="vstack gap-3">
                    <div>
                        <label for="nome" class="form-label fw-semibold">Nome</label>
                        <input type="text" class="form-control" id="nome" name="nome" required minlength="2" autocomplete="name" placeholder="Digite seu nome">
                    </div>
                    <div>
                        <label for="email" class="form-label fw-semibold">E-mail</label>
                        <input type="email" class="form-control" id="email" name="email" required autocomplete="email" placeholder="Digite seu e-mail">
                    </div>
                    <div>
                        <label for="senha" class="form-label fw-semibold">Senha</label>
                        <input type="password" class="form-control" id="senha" name="senha" required minlength="6" autocomplete="new-password" placeholder="Digite sua senha">
                    </div>
                    <div>
                        <label for="confirmarSenha" class="form-label fw-semibold">Confirmar senha</label>
                        <input type="password" class="form-control" id="confirmarSenha" name="confirmarSenha" required minlength="6" autocomplete="new-password" placeholder="Confirme sua senha">
                    </div>
                    <button type="submit" class="btn btn-dark btn-register w-100">Cadastrar</button>
                </form>

                <p class="text-center text-secondary mt-4 mb-0 small login-link">Já tem conta?<a href="${ctx}/login">Entrar</a></p>
            </div>
        </div>
    </div>
</div>

<span id="server-flash-erro" class="d-none">
    <c:out value="${flashErro}"/>
</span>

<script src="${pageContext.request.contextPath}/js/jquery-4.0.0.min.js"></script>
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
<script src="${pageContext.request.contextPath}/js/app.js"></script>

</body>
</html>