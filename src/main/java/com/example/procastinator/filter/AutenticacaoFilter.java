package com.example.procastinator.filter;

import com.example.procastinator.web.SessaoUsuario;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebFilter(urlPatterns = "/*")
public class AutenticacaoFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;

        if (isRotaPublica(req)) {
            chain.doFilter(request, response);
            return;
        }

        if (SessaoUsuario.isAutenticado(req)) {
            chain.doFilter(request, response);
            return;
        }

        String contextPath = req.getContextPath();
        String uri = req.getRequestURI();
        String path = uri.substring(contextPath.length());
        String destino = contextPath + "/login?redirect=" + encodeRedirect(path);
        resp.sendRedirect(destino);
    }

    private static boolean isRotaPublica(HttpServletRequest req) {
        String path = req.getRequestURI().substring(req.getContextPath().length());
        if (path.isEmpty()) {
            path = "/";
        }

        if (path.equals("/login") || path.equals("/registro") || path.equals("/logout")) {
            return true;
        }
        if (path.endsWith(".jsp") && !path.startsWith("/WEB-INF")) {
            return true;
        }
        if (path.startsWith("/css/") || path.startsWith("/js/")) {
            return true;
        }
        return path.equals("/hello-servlet");
    }

    private static String encodeRedirect(String path) {
        if (path == null || path.isBlank() || "/".equals(path)) {
            return "%2Festatisticas";
        }
        return java.net.URLEncoder.encode(path, java.nio.charset.StandardCharsets.UTF_8);
    }
}
