package com.example.procastinator.servlet;

import com.example.procastinator.web.SessaoUsuario;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet("/logout")
public class LogoutServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        encerrarSessao(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        encerrarSessao(req, resp);
    }

    private void encerrarSessao(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        SessaoUsuario.logout(req.getSession(false));
        resp.sendRedirect(req.getContextPath() + "/login");
    }
}
