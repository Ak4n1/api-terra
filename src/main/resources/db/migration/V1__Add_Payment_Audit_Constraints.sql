-- =========================================
-- MEJORAS ACID PARA SISTEMA DE PAGOS
-- Flyway Migration V1
-- Se ejecuta AUTOMÁTICAMENTE al iniciar Spring Boot
-- NOTA: Verifica si las tablas existen antes de crear índices
-- =========================================

-- 1. Índices para performance (solo si las tablas existen)
-- =========================================

-- Verificar si payment_transactions existe antes de crear índices
SET @table_exists = (
    SELECT COUNT(*) 
    FROM information_schema.tables 
    WHERE table_schema = DATABASE() 
    AND table_name = 'payment_transactions'
);

-- Crear índices solo si la tabla existe
SET @sql = IF(@table_exists > 0,
    'CREATE INDEX IF NOT EXISTS idx_payment_transactions_account_status ON payment_transactions(account_id, status);',
    'SELECT 1;'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = IF(@table_exists > 0,
    'CREATE INDEX IF NOT EXISTS idx_payment_transactions_created ON payment_transactions(created_at);',
    'SELECT 1;'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = IF(@table_exists > 0,
    'CREATE INDEX IF NOT EXISTS idx_payment_transactions_provider ON payment_transactions(provider, created_at);',
    'SELECT 1;'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = IF(@table_exists > 0,
    'CREATE INDEX IF NOT EXISTS idx_paypal_order_id ON payment_transactions(paypal_order_id);',
    'SELECT 1;'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = IF(@table_exists > 0,
    'CREATE INDEX IF NOT EXISTS idx_mp_payment_id ON payment_transactions(mp_payment_id);',
    'SELECT 1;'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Verificar si payment_audit existe antes de crear índices
SET @table_exists_audit = (
    SELECT COUNT(*) 
    FROM information_schema.tables 
    WHERE table_schema = DATABASE() 
    AND table_name = 'payment_audit'
);

-- Índice para auditoría por cuenta
SET @sql = IF(@table_exists_audit > 0,
    'CREATE INDEX IF NOT EXISTS idx_payment_audit_account_created ON payment_audit(account_id, created_at);',
    'SELECT 1;'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Índice para auditoría por tipo
SET @sql = IF(@table_exists_audit > 0,
    'CREATE INDEX IF NOT EXISTS idx_payment_audit_type ON payment_audit(transaction_type, created_at);',
    'SELECT 1;'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- =========================================
-- FIN DE MIGRACIÓN V1
-- =========================================
-- Flyway registrará esta migración como ejecutada
-- No se volverá a ejecutar en futuros reinicios
-- =========================================
