package com.example.procastinator.servlet;

import com.example.procastinator.dao.UsuarioDAO;
import com.example.procastinator.model.Usuario;
import com.example.procastinator.web.FlashMensagens;
import com.example.procastinator.web.SessaoUsuario;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

@WebServlet("/login")
public class LoginServlet extends HttpServlet {

    private static final String VIEW = "/WEB-INF/jsp/login.jsp";

    private final UsuarioDAO usuarioDao = new UsuarioDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (SessaoUsuario.isAutenticado(req)) {
            resp.sendRedirect(req.getContextPath() + "/estatisticas");
            return;
        }
        FlashMensagens.consumir(req);
        req.getRequestDispatcher(VIEW).forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        req.setCharacterEncoding("UTF-8");
        HttpSession session = req.getSession();

        String email = trim(req.getParameter("email"));
        String senha = req.getParameter("senha");

        if (email.isEmpty() || senha == null || senha.isBlank()) {
            FlashMensagens.erro(session, "Informe e-mail e senha.");
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        Usuario usuario = usuarioDao.autenticar(email, senha);
        if (usuario == null) {
            FlashMensagens.erro(session, "E-mail ou senha invalidos.");
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        SessaoUsuario.login(session, usuario);
        FlashMensagens.toast(session, "Bem-vindo de volta, " + usuario.getNome() + "!");
        String destino = SessaoUsuario.resolverRedirectPosLogin(req, "/estatisticas");
        resp.sendRedirect(req.getContextPath() + destino);
    }

    private static String trim(String valor) {
        return valor == null ? "" : valor.trim();
    }
}
