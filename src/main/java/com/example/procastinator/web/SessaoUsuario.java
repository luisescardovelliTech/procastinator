package com.example.procastinator.web;

import com.example.procastinator.model.Usuario;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

public final class SessaoUsuario {

    public static final String ATTR_ID = "usuarioId";
    public static final String ATTR_NOME = "usuarioNome";
    public static final String ATTR_EMAIL = "usuarioEmail";

    private SessaoUsuario() {
    }

    public static boolean isAutenticado(HttpServletRequest req) {
        return obterId(req) != null;
    }

    public static Integer obterId(HttpServletRequest req) {
        HttpSession session = req.getSession(false);
        if (session == null) {
            return null;
        }
        Object valor = session.getAttribute(ATTR_ID);
        if (valor instanceof Integer id) {
            return id;
        }
        if (valor instanceof Number numero) {
            return numero.intValue();
        }
        return null;
    }

    public static String obterNome(HttpServletRequest req) {
        HttpSession session = req.getSession(false);
        if (session == null) {
            return null;
        }
        Object valor = session.getAttribute(ATTR_NOME);
        return valor != null ? valor.toString() : null;
    }

    public static void login(HttpSession session, Usuario usuario) {
        session.setAttribute(ATTR_ID, usuario.getId());
        session.setAttribute(ATTR_NOME, usuario.getNome());
        session.setAttribute(ATTR_EMAIL, usuario.getEmail());
    }

    public static void logout(HttpSession session) {
        if (session == null) {
            return;
        }
        session.removeAttribute(ATTR_ID);
        session.removeAttribute(ATTR_NOME);
        session.removeAttribute(ATTR_EMAIL);
        session.invalidate();
    }

    public static String resolverRedirectPosLogin(HttpServletRequest req, String padrao) {
        String destino = req.getParameter("redirect");
        if (destino == null || destino.isBlank()) {
            return padrao;
        }
        if (!destino.startsWith("/") || destino.startsWith("//") || destino.contains("://")) {
            return padrao;
        }
        return destino;
    }
}
