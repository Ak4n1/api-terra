package com.ak4n1.terra.api.terra_api.config;

import com.ak4n1.terra.api.terra_api.auth.entities.Role;
import com.ak4n1.terra.api.terra_api.auth.repositories.RoleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * Inicializador de roles del sistema.
 * 
 * <p>Se ejecuta al iniciar la aplicación y verifica que los roles básicos
 * (ROLE_ADMIN, ROLE_USER, ROLE_STREAMER) existan en la base de datos.
 * Si no existen, los crea automáticamente.
 * 
 * @author ak4n1
 * @since 1.0
 */
@Component
public class RoleInitializer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(RoleInitializer.class);
    
    private final RoleRepository roleRepository;
    
    private static final String[] ROLES = {
        "ROLE_ADMIN",
        "ROLE_USER",
        "ROLE_STREAMER"
    };

    public RoleInitializer(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        logger.info("Inicializando roles del sistema...");
        
        for (String roleName : ROLES) {
            roleRepository.findByName(roleName).ifPresentOrElse(
                role -> { /* Rol ya existe */ },
                () -> {
                    Role newRole = new Role();
                    newRole.setName(roleName);
                    roleRepository.save(newRole);
                }
            );
        }
        
        logger.info("Inicialización de roles completada");
    }
}

