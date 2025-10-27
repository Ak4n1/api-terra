package com.ak4n1.terra.api.terra_api.payments.entities;

/**
 * Estados de pago para transacciones
 */
public enum PaymentStatus {
    PENDING,    // Pendiente de pago
    APPROVED,   // Pago aprobado
    REJECTED,   // Pago rechazado
    CANCELLED,  // Pago cancelado
    REFUNDED,   // Pago reembolsado
    IN_PROCESS, // En proceso
    EXPIRED     // Expirado
}
