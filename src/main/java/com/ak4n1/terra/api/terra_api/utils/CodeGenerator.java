package com.ak4n1.terra.api.terra_api.utils;

import java.security.SecureRandom;

public class CodeGenerator {
    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final String CHARACTERS_UPPERCASE = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int LENGTH = 158;

    public static String generateVerificationCode() {
        SecureRandom random = new SecureRandom();
        StringBuilder code = new StringBuilder(LENGTH);

        for (int i = 0; i < LENGTH; i++) {
            int index = random.nextInt(CHARACTERS.length());
            code.append(CHARACTERS.charAt(index));
        }

        return code.toString();
    }
    /**
     * Genera un código de 6 caracteres (solo mayúsculas y números).
     * Ejemplo: A3X9K2
     * User-friendly: no hay confusión entre mayúsculas/minúsculas.
     */
    public static String generateSixDigitCode() {
        SecureRandom random = new SecureRandom();
        StringBuilder code = new StringBuilder(6);
        for (int i = 0; i < 6; i++) {
            int index = random.nextInt(CHARACTERS_UPPERCASE.length());
            code.append(CHARACTERS_UPPERCASE.charAt(index));
        }
        return code.toString();
    }

}
