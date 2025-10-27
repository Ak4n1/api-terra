package com.ak4n1.terra.api.terra_api.payments.services;

import com.ak4n1.terra.api.terra_api.payments.dto.CoinPackageResponseDTO;
import com.ak4n1.terra.api.terra_api.payments.dto.CoinPurchaseRequest;
import com.ak4n1.terra.api.terra_api.payments.dto.PaymentPreferenceResponse;
import com.ak4n1.terra.api.terra_api.payments.entities.PaymentTransaction;

import java.util.List;

/**
 * Servicio principal para gestión de pagos
 */
public interface PaymentService {
    
    /**
     * Obtener todos los paquetes de monedas activos
     */
    List<CoinPackageResponseDTO> getAllActivePackages();
    
    /**
     * Obtener un paquete específico por ID
     */
    CoinPackageResponseDTO getPackageById(Long packageId);
    
    /**
     * Obtener paquetes populares
     */
    List<CoinPackageResponseDTO> getPopularPackages();
    
    /**
     * Crear preferencia de pago para Mercado Pago
     */
    PaymentPreferenceResponse createPaymentPreference(CoinPurchaseRequest request);
    
    /**
     * Procesar webhook de Mercado Pago
     */
    boolean processMercadoPagoWebhook(String payload, String signature);
    
    /**
     * Obtener historial de transacciones de una cuenta
     */
    List<PaymentTransaction> getAccountTransactionHistory(Long accountId);
    
    /**
     * Obtener estadísticas de pagos de una cuenta
     */
    CoinService.CoinAccountStats getAccountPaymentStats(Long accountId);
    
    /**
     * Verificar estado de una transacción
     */
    String getTransactionStatus(Long transactionId);
    
    /**
     * Reembolsar una transacción
     */
    boolean refundTransaction(Long transactionId, String reason);
}
