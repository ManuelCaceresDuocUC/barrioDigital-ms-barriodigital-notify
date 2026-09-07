# Proyecto: BarrioDigital - Servicio de Notificaciones

**Componente:** Microservicio de Dominio (Notificaciones)
**Integrantes:** [Nombre Apellido 1], [Nombre Apellido 2], [Nombre Apellido 3]

## Descripción
Servicio completamente asíncrono y desacoplado (sin base de datos propia) responsable de la comunicación saliente del sistema. Consume mensajes desde las colas para enviar alertas por email o push a los vecinos sobre el estado de sus trámites, y para generar los tickets de visita correspondientes a las cuadrillas en terreno. 

## Tecnologías a usar
* **Framework:** Spring Boot (Java)
* **Mensajería (Consumer):** RabbitMQ (Colas: `q.cmd.email`, `q.cmd.crew`, `q.cmd.certificate`)
* **Persistencia:** Ninguna (Sin DB, dependiente del bróker y mecanismos DLQ)
