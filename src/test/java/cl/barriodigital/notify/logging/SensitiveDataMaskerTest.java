package cl.barriodigital.notify.logging;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class SensitiveDataMaskerTest {

    @Test
    void enmascaraCorreos() {
        assertThat(SensitiveDataMasker.mask("fallo con ana.perez@municipio.cl al enviar"))
                .isEqualTo("fallo con a***@municipio.cl al enviar");
    }

    @Test
    void enmascaraRutConYSinPuntos() {
        assertThat(SensitiveDataMasker.mask("rut 11.111.111-1 y 22222222-K"))
                .isEqualTo("rut [RUT] y [RUT]");
    }

    @Test
    void dejaIntactoElTextoSinDatosPersonales() {
        assertThat(SensitiveDataMasker.mask("El mensaje no trae payload"))
                .isEqualTo("El mensaje no trae payload");
    }

    @Test
    void aceptaNulos() {
        assertThat(SensitiveDataMasker.mask(null)).isNull();
    }
}