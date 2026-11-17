package cl.barriodigital.notify.dto;

import java.io.Serializable;

/**
 * QUÉ ES: carga útil (payload) del comando "generar certificado" (routing key certificate.gen).
 * Es un record: una clase inmutable que solo transporta datos.
 *
 * @param citizenRut      RUT del ciudadano (obligatorio; si falta, el mensaje va a la DLQ)
 * @param certificateType tipo de certificado solicitado
 * @param documentUrl     dirección del documento generado
 */
public record CertificateEvent(
    String citizenRut,
    String certificateType,
    String documentUrl
) implements Serializable {}
