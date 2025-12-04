package com.ak4n1.terra.api.terra_api.game.l2j.factory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Factory para obtener conexiones a la base de datos.
 * 
 * <p>Implementa patrón Factory para gestionar conexiones de base de datos
 * sin exponer detalles de implementación. Thread-safe singleton.
 * 
 * @author ak4n1
 * @since 1.0
 */
public class DatabaseFactory {
    
    private static final Logger logger = LoggerFactory.getLogger(DatabaseFactory.class);
    
    // Singleton Holder Pattern (thread-safe lazy initialization)
    private static class SingletonHolder {
        protected static final DatabaseFactory INSTANCE = new DatabaseFactory();
    }
    
    public static DatabaseFactory getInstance() {
        return SingletonHolder.INSTANCE;
    }
    
    private DatabaseFactory() {
        // Constructor privado para singleton
    }
    
    private DataSource dataSource;
    
    /**
     * Inicializa el factory con un DataSource.
     * 
     * <p>Debe ser llamado una vez al inicio de la aplicación.
     * 
     * @param dataSource DataSource configurado
     */
    public void init(DataSource dataSource) {
        this.dataSource = dataSource;
    }
    
    /**
     * Obtiene una conexión de la base de datos.
     * 
     * @return Connection a la base de datos
     * @throws SQLException Si ocurre un error al obtener la conexión
     */
    public Connection getConnection() throws SQLException {
        if (dataSource == null) {
            throw new IllegalStateException("DatabaseFactory not initialized. Call init() first.");
        }
        
        return dataSource.getConnection();
    }
}

