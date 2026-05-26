package com.example.procastinator.util;

import org.mindrot.jbcrypt.BCrypt;

public final class SenhaHasher {

    private SenhaHasher() {
    }

    public static String hash(String senhaPlana) {
        return BCrypt.hashpw(senhaPlana, BCrypt.gensalt(12));
    }

    public static boolean verificar(String senhaPlana, String hashArmazenado) {
        if (senhaPlana == null || hashArmazenado == null || hashArmazenado.isBlank()) {
            return false;
        }
        try {
            return BCrypt.checkpw(senhaPlana, hashArmazenado);
        } catch (IllegalArgumentException ex) {
            return false;
        }
    }
}
