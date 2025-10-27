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

@Service
public interface GameAccountService {

    List<AccountGameResponseDTO> getAccountsByEmail(String email);

    Map<String, String> generateAndSendCode(String login);

    AccountGame createAccount(AccountGameRequestDTO dto, String email);

    Map<String, String> generateAndSendCreateCode(String email);

    Map<String, String> changePassword(ChangePasswordGameDTO dto);


}
