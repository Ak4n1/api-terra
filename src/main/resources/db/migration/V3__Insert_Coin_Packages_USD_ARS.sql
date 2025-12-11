-- Verificar si la tabla coin_packages existe antes de continuar
SET @table_exists = (
    SELECT COUNT(*) 
    FROM information_schema.tables 
    WHERE table_schema = DATABASE() 
    AND table_name = 'coin_packages'
);

-- Eliminar paquetes existentes (solo si la tabla existe)
SET @sql = IF(@table_exists > 0,
    'DELETE FROM coin_packages;',
    'SELECT 1;'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- ========================================
-- PAQUETES USD (PayPal)
-- ========================================
-- Solo insertar si la tabla existe
SET @sql = IF(@table_exists > 0,
    'INSERT INTO coin_packages (name, coins_amount, bonus_coins, price, original_price, bonus_percentage, description, currency, popular, active, sort_order, created_at) 
    VALUES 
    (''Starter Pack'', 50, 5, 5.00, NULL, 10, ''Perfect to start your adventure'', ''USD'', false, true, 1, NOW()),
    (''Basic Pack'', 100, 15, 10.00, NULL, 15, ''Ideal for casual players'', ''USD'', false, true, 2, NOW()),
    (''Standard Pack'', 200, 40, 20.00, NULL, 20, ''Most popular choice'', ''USD'', true, true, 3, NOW()),
    (''Advanced Pack'', 400, 100, 35.00, NULL, 25, ''For experienced adventurers'', ''USD'', false, true, 4, NOW()),
    (''Premium Pack'', 600, 180, 50.00, NULL, 30, ''Maximum power at a great value'', ''USD'', false, true, 5, NOW()),
    (''Ultimate Pack'', 1000, 350, 100.00, NULL, 35, ''The ultimate boost for champions'', ''USD'', false, true, 6, NOW());',
    'SELECT 1;'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- ========================================
-- PAQUETES ARS (MercadoPago)
-- ========================================
-- Solo insertar si la tabla existe
SET @sql = IF(@table_exists > 0,
    'INSERT INTO coin_packages (name, coins_amount, bonus_coins, price, original_price, bonus_percentage, description, currency, popular, active, sort_order, created_at) 
    VALUES 
    (''Starter Pack'', 50, 5, 7230.00, NULL, 10, ''Perfect to start your adventure'', ''ARS'', false, true, 7, NOW()),
    (''Basic Pack'', 100, 15, 14460.00, NULL, 15, ''Ideal for casual players'', ''ARS'', false, true, 8, NOW()),
    (''Standard Pack'', 200, 40, 28920.00, NULL, 20, ''Most popular choice'', ''ARS'', true, true, 9, NOW()),
    (''Advanced Pack'', 400, 100, 50610.00, NULL, 25, ''For experienced adventurers'', ''ARS'', false, true, 10, NOW()),
    (''Premium Pack'', 600, 180, 72300.00, NULL, 30, ''Maximum power at a great value'', ''ARS'', false, true, 11, NOW()),
    (''Ultimate Pack'', 1000, 350, 144600.00, NULL, 35, ''The ultimate boost for champions'', ''ARS'', false, true, 12, NOW());',
    'SELECT 1;'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

