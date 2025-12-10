package com.ak4n1.terra.api.terra_api.notifications.domain;

/**
 * Enum que representa los tipos de notificaciones del sistema.
 * 
 * <p>Cada tipo de notificación tiene un propósito específico y puede tener
 * metadata diferente asociado. Los tipos están diseñados para ser extensibles
 * sin requerir cambios en el esquema de base de datos.
 * 
 * @author ak4n1
 * @since 1.0
 */
public enum NotificationType {
    
    /**
     * Notificación de pago exitoso.
     * Metadata esperado: payment_id, amount, provider, terra_coins_added, transaction_id
     */
    PAYMENT_SUCCESS,
    
    /**
     * Notificación de pago fallido.
     * Metadata esperado: payment_id, error_message, provider
     */
    PAYMENT_FAILED,
    
    /**
     * Notificación de venta en Terra Market (futuro).
     * Metadata esperado: market_item_id, item_name, price, buyer_name, commission, net_amount
     */
    MARKET_SALE,
    
    /**
     * Notificación de compra en Terra Market (futuro).
     * Metadata esperado: market_item_id, item_name, price, seller_name
     */
    MARKET_PURCHASE,
    
    /**
     * Notificación del sistema (mantenimiento, actualizaciones, etc.).
     * Metadata esperado: system_event, scheduled_time, duration_minutes, affected_services
     */
    SYSTEM_NOTIFICATION,
    
    /**
     * Notificación administrativa.
     * Metadata esperado: admin_action, details
     */
    ADMIN_NOTIFICATION
}

