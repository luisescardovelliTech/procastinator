package com.example.procastinator.util;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

public final class HibernateUtil {
    private static final SessionFactory SESSION_FACTORY;

    static {
        try {
            SESSION_FACTORY = new Configuration().configure().buildSessionFactory();
        } catch (Throwable ex) {
            throw new ExceptionInInitializerError("Falha ao inicializar SessionFactory: " + ex.getMessage());
        }
    }

    private HibernateUtil() {
    }

    public static SessionFactory getSessionFactory() {
        return SESSION_FACTORY;
    }

    public static void shutdown() {
        if (SESSION_FACTORY != null) {
            SESSION_FACTORY.close();
        }
    }
}
