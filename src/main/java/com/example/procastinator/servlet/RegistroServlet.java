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
import java.util.regex.Pattern;

@WebServlet("/registro")
public class RegistroServlet extends HttpServlet {

    private static final String VIEW = "/WEB-INF/jsp/registro.jsp";
    private static final Pattern EMAIL_VALIDO = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

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

        String nome = trim(req.getParameter("nome"));
        String email = trim(req.getParameter("email"));
        String senha = req.getParameter("senha");
        String confirmar = req.getParameter("confirmarSenha");

        if (nome.length() < 2) {
            FlashMensagens.erro(session, "Informe seu nome (minimo 2 caracteres).");
            resp.sendRedirect(req.getContextPath() + "/registro");
            return;
        }
        if (!EMAIL_VALIDO.matcher(email).matches()) {
            FlashMensagens.erro(session, "Informe um e-mail valido.");
            resp.sendRedirect(req.getContextPath() + "/registro");
            return;
        }
        if (senha == null || senha.length() < 6) {
            FlashMensagens.erro(session, "A senha deve ter no minimo 6 caracteres.");
            resp.sendRedirect(req.getContextPath() + "/registro");
            return;
        }
        if (!senha.equals(confirmar)) {
            FlashMensagens.erro(session, "As senhas nao conferem.");
            resp.sendRedirect(req.getContextPath() + "/registro");
            return;
        }
        if (usuarioDao.emailJaCadastrado(email)) {
            FlashMensagens.erro(session, "Este e-mail ja esta cadastrado.");
            resp.sendRedirect(req.getContextPath() + "/registro");
            return;
        }

        Usuario usuario = usuarioDao.cadastrar(nome, email, senha);
        SessaoUsuario.login(session, usuario);
        FlashMensagens.toast(session, "Conta criada! Hora de procrastinar com estilo.");
        resp.sendRedirect(req.getContextPath() + "/estatisticas");
    }

    private static String trim(String valor) {
        return valor == null ? "" : valor.trim();
    }
}
