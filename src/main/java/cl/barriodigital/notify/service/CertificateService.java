package cl.barriodigital.notify.service;

import org.springframework.stereotype.Service;

import cl.barriodigital.notify.dto.CertificateEvent;
import lombok.extern.slf4j.Slf4j;

/**
 * QUÉ ES: lógica de negocio del dominio "certificado".
 * ESTADO: hoy SIMULA el despacho (solo escribe en el log); no genera el PDF real. Por eso la matriz
 * de funcionalidades marca M12 como "parcial".
 */
@Slf4j
@Service
public class CertificateService {

    public void dispatch(CertificateEvent event) {
        // IllegalArgumentException = error no recuperable: AckHandler enviará el mensaje directo a la DLQ.
        if (event.citizenRut() == null || event.citizenRut().isBlank()) {
            throw new IllegalArgumentException("El RUT del ciudadano es obligatorio");
        }
        log.info("Procesando certificado de tipo {}", event.certificateType());
        // Simulación del despacho del certificado.
        log.info("Certificado de tipo {} despachado (documento: {})",
                event.certificateType(), event.documentUrl());
    }
}
