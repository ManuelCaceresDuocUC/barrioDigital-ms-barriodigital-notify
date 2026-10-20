package cl.barriodigital.notify.logging;

import java.util.regex.Pattern;

/** Oculta correos y RUT antes de escribirlos en un log. */
public final class SensitiveDataMasker {

    private static final Pattern EMAIL = Pattern.compile("([\\w.+-])[\\w.+-]*@([\\w-]+(?:\\.[\\w-]+)+)");
    private static final Pattern RUT = Pattern.compile("\\b\\d{1,2}(?:\\.?\\d{3}){2}-[\\dkK]\\b");

    private SensitiveDataMasker() {
    }

    public static String mask(String text) {
        if (text == null) {
            return null;
        }
        String masked = EMAIL.matcher(text).replaceAll("$1***@$2");
        return RUT.matcher(masked).replaceAll("[RUT]");
    }
}