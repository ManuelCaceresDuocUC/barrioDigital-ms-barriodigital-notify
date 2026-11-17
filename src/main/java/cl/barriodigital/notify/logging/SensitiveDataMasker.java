package cl.barriodigital.notify.logging;

import java.util.regex.Pattern;

/**
 * Oculta correos y RUT antes de escribirlos en un log.
 *
 * QUÉ ES: utilitario estático (clase final con constructor privado: no se instancia) que protege datos
 * personales. Ejemplos: "vecino@correo.cl" -> "v***@correo.cl"; "12.345.678-9" -> "[RUT]".
 */
public final class SensitiveDataMasker {

    // Correo: captura la primera letra y el dominio; el resto de la parte local se reemplaza por ***.
    private static final Pattern EMAIL = Pattern.compile("([\\w.+-])[\\w.+-]*@([\\w-]+(?:\\.[\\w-]+)+)");
    // RUT chileno: 1-2 dígitos, dos grupos de 3 dígitos (con o sin puntos), guion y dígito verificador (0-9 o K).
    private static final Pattern RUT = Pattern.compile("\\b\\d{1,2}(?:\\.?\\d{3}){2}-[\\dkK]\\b");

    private SensitiveDataMasker() {
    }

    public static String mask(String text) {
        if (text == null) {
            return null;
        }
        // $1 y $2 son los grupos capturados (primera letra y dominio).
        String masked = EMAIL.matcher(text).replaceAll("$1***@$2");
        return RUT.matcher(masked).replaceAll("[RUT]");
    }
}
