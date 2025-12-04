package com.ak4n1.terra.api.terra_api.game.l2j.factory;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;

/**
 * Inicializador del DatabaseFactory para Spring.
 * 
 * <p>Componente Spring que inicializa el DatabaseFactory singleton
 * con el DataSource configurado por Spring Boot.
 * 
 * @author ak4n1
 * @since 1.0
 */
@Component
public class DatabaseFactoryManager {
    
    private static final Logger logger = LoggerFactory.getLogger(DatabaseFactoryManager.class);
    
    @Autowired
    private DataSource dataSource;
    
    @PostConstruct
    public void init() {
        DatabaseFactory.getInstance().init(dataSource);
        logger.info("DatabaseFactoryManager initialized DatabaseFactory with DataSource.");
    }
}

