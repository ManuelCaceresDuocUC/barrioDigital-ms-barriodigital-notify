package cl.barriodigital.notify.dto;

import java.io.Serializable;

public record CertificateEvent(
    String citizenRut,
    String certificateType,
    String documentUrl
) implements Serializable {}