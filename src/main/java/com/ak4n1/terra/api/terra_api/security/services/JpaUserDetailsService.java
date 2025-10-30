package com.ak4n1.terra.api.terra_api.security.services;

import com.ak4n1.terra.api.terra_api.auth.entities.AccountMaster;
import com.ak4n1.terra.api.terra_api.auth.exceptions.EmailNotVerifiedException;
import com.ak4n1.terra.api.terra_api.auth.exceptions.UserDisabledException;
import com.ak4n1.terra.api.terra_api.auth.repositories.AccountMasterRepository;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementación de UserDetailsService para Spring Security usando JPA.
 * 
 * <p>Este servicio carga los detalles del usuario desde la base de datos para
 * la autenticación. Valida que el email esté verificado y que la cuenta esté habilitada.
 * Es la clase RECOMENDADA para cargar usuarios en el contexto de seguridad.
 * 
 * @see UserDetailsService
 * @see AccountMasterRepository
 * @see com.ak4n1.terra.api.terra_api.auth.entities.AccountMaster
 * @author ak4n1
 * @since 1.0
 */
@Service
public class JpaUserDetailsService implements UserDetailsService {

    private final AccountMasterRepository accountMasterRepository;

    /**
     * Constructor que recibe el repositorio de usuarios.
     * 
     * @param accountMasterRepository Repositorio para consultar usuarios
     */
    public JpaUserDetailsService(AccountMasterRepository accountMasterRepository) {
        this.accountMasterRepository = accountMasterRepository;
    }

    /**
     * Carga un usuario por su email para la autenticación.
     * 
     * <p>Valida que el usuario exista, que el email esté verificado y que la cuenta
     * esté habilitada. Construye un objeto User de Spring Security con los roles
     * del usuario.
     * 
     * @param email Email del usuario a cargar
     * @return UserDetails con la información del usuario para Spring Security
     * @throws UsernameNotFoundException si el usuario no existe
     * @throws com.ak4n1.terra.api.terra_api.auth.exceptions.EmailNotVerifiedException si el email no está verificado
     * @throws com.ak4n1.terra.api.terra_api.auth.exceptions.UserDisabledException si la cuenta está deshabilitada
     */
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        AccountMaster user = accountMasterRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));

        if (!user.isEmailVerified()) {
            throw new EmailNotVerifiedException();
        }
        if (!user.isEnabled()) {
            throw new UserDisabledException();
        }


        List<GrantedAuthority> authorities = user.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority(role.getName()))
                .collect(Collectors.toList());

        return new User(
                user.getEmail(),
                user.getPassword(),
                user.isEnabled(),      
                true,                 
                true,                 
                true,                 
                authorities
        );
    }
}

