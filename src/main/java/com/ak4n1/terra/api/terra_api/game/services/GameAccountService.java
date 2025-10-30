package com.ak4n1.terra.api.terra_api.game.services;

import com.ak4n1.terra.api.terra_api.game.dto.AccountGameRequestDTO;
import com.ak4n1.terra.api.terra_api.game.dto.AccountGameResponseDTO;
import com.ak4n1.terra.api.terra_api.game.dto.ChangePasswordGameDTO;
import com.ak4n1.terra.api.terra_api.game.entities.AccountGame;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

/**
 * Servicio principal para la gestión de cuentas de juego.
 * 
 * <p>Este servicio maneja todas las operaciones relacionadas con las cuentas de juego,
 * incluyendo creación, recuperación, cambio de contraseñas y generación de códigos
 * de verificación. Es el servicio RECOMENDADO para interactuar con las cuentas de juego
 * desde los controladores.
 * 
 * @see GameAccountServiceImpl
 * @see AccountGame
 * @author ak4n1
 * @since 1.0
 */
@Service
public interface GameAccountService {

    /**
     * Obtiene todas las cuentas de juego asociadas a un email.
     * 
     * @param email Email del usuario
     * @return Lista de DTOs con la información de las cuentas
     */
    List<AccountGameResponseDTO> getAccountsByEmail(String email);

    /**
     * Genera y envía un código de reset de contraseña al email asociado a una cuenta.
     * 
     * <p>El código tiene una validez de 15 minutos y se valida que no se haya
     * enviado otro código recientemente.
     * 
     * @param login Nombre de login de la cuenta de juego
     * @return Map con el estado de la operación ("success" o "forbidden") y mensaje
     */
    Map<String, String> generateAndSendCode(String login);

    /**
     * Crea una nueva cuenta de juego validando el código de creación.
     * 
     * <p>El código de creación debe estar activo y no haber sido utilizado previamente.
     * La contraseña se codifica usando el algoritmo de L2J antes de guardarse.
     * 
     * @param dto DTO con los datos de la cuenta (username, password, código de creación)
     * @param email Email del usuario propietario
     * @return Entidad AccountGame creada
     * @throws com.ak4n1.terra.api.terra_api.game.exceptions.CreationCodeNotFoundException si el código no existe
     * @throws com.ak4n1.terra.api.terra_api.game.exceptions.CreationCodeAlreadyUsedException si el código ya fue usado
     * @throws com.ak4n1.terra.api.terra_api.game.exceptions.InvalidCreationCodeException si el código es inválido
     * @throws com.ak4n1.terra.api.terra_api.game.exceptions.CreationCodeExpiredException si el código expiró
     * @throws com.ak4n1.terra.api.terra_api.game.exceptions.AccountAlreadyExistsException si la cuenta ya existe
     */
    AccountGame createAccount(AccountGameRequestDTO dto, String email);

    /**
     * Genera y envía un código de creación de cuenta al email especificado.
     * 
     * <p>El código tiene una validez de 15 minutos y se valida que no se haya
     * enviado otro código recientemente.
     * 
     * @param email Email del usuario que solicita crear la cuenta
     * @return Map con el estado de la operación ("success" o "forbidden") y mensaje
     */
    Map<String, String> generateAndSendCreateCode(String email);

    /**
     * Cambia la contraseña de una cuenta de juego usando un código de reset.
     * 
     * <p>El código debe estar activo y no haber expirado. Después del cambio,
     * el código se invalida automáticamente.
     * 
     * @param dto DTO con login, código de reset y nueva contraseña
     * @return Map con el estado de la operación ("success", "unauthorized", "expired" o "error") y mensaje
     * @throws com.ak4n1.terra.api.terra_api.game.exceptions.GameAccountNotFoundException si la cuenta no existe
     */
    Map<String, String> changePassword(ChangePasswordGameDTO dto);


}
