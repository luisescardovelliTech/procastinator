package com.example.procastinator.web;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

public final class FlashMensagens {

    public static final String TOAST = "flashToast";
    public static final String ELOGIO = "flashElogio";
    public static final String XINGAMENTO = "flashXingamento";
    public static final String ERRO = "flashErro";

    private FlashMensagens() {
    }

    public static void toast(HttpSession session, String msg) {
        session.setAttribute(TOAST, msg);
    }

    public static void elogio(HttpSession session, String msg) {
        session.setAttribute(ELOGIO, msg);
    }

    public static void xingamento(HttpSession session, String msg) {
        session.setAttribute(XINGAMENTO, msg);
    }

    public static void erro(HttpSession session, String msg) {
        session.setAttribute(ERRO, msg);
    }

    public static void consumir(HttpServletRequest req) {
        HttpSession session = req.getSession(false);
        if (session == null) {
            return;
        }
        copiar(session, req, TOAST);
        copiar(session, req, ELOGIO);
        copiar(session, req, XINGAMENTO);
        copiar(session, req, ERRO);
    }

    private static void copiar(HttpSession session, HttpServletRequest req, String chave) {
        Object valor = session.getAttribute(chave);
        if (valor != null) {
            req.setAttribute(chave, valor);
            session.removeAttribute(chave);
        }
    }
}
