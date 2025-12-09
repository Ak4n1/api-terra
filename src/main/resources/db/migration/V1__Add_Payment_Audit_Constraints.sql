-- =========================================
-- MEJORAS ACID PARA SISTEMA DE PAGOS
-- Flyway Migration V1
-- Se ejecuta AUTOMÁTICAMENTE al iniciar Spring Boot
-- =========================================

-- 1. Índices para performance
-- =========================================

-- Índice para buscar por cuenta y estado (si no existe)
CREATE INDEX IF NOT EXISTS idx_payment_transactions_account_status 
ON payment_transactions(account_id, status);

-- Índice para buscar transacciones recientes
CREATE INDEX IF NOT EXISTS idx_payment_transactions_created 
ON payment_transactions(created_at);

-- Índice para auditoría por cuenta  
CREATE INDEX IF NOT EXISTS idx_payment_audit_account_created 
ON payment_audit(account_id, created_at);

-- Índice para auditoría por tipo
CREATE INDEX IF NOT EXISTS idx_payment_audit_type 
ON payment_audit(transaction_type, created_at);

-- 2. Índice para proveedor de pago
-- =========================================

CREATE INDEX IF NOT EXISTS idx_payment_transactions_provider 
ON payment_transactions(provider, created_at);

-- 3. Índices para IDs únicos de proveedores
-- =========================================

CREATE INDEX IF NOT EXISTS idx_paypal_order_id 
ON payment_transactions(paypal_order_id);

CREATE INDEX IF NOT EXISTS idx_mp_payment_id 
ON payment_transactions(mp_payment_id);

-- =========================================
-- FIN DE MIGRACIÓN V1
-- =========================================
-- Flyway registrará esta migración como ejecutada
-- No se volverá a ejecutar en futuros reinicios
-- =========================================
