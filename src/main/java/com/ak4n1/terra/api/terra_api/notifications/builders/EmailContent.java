package com.ak4n1.terra.api.terra_api.notifications.builders;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Builder para construir el contenido HTML de los emails del sistema.
 * 
 * <p>Esta clase proporciona métodos para construir diferentes tipos de cuerpos de email
 * en formato HTML, incluyendo templates para reset de contraseñas, verificación de emails,
 * y códigos para cuentas de juego. Es la clase RECOMENDADA para construir contenido de emails.
 * 
 * @see com.ak4n1.terra.api.terra_api.notifications.services.EmailNotificationService
 * @author ak4n1
 * @since 1.0
 */
@Service
public class EmailContent {

    @Value("${server.url.config}")
    private String urlBackEnd;

    @Value("${frontend.url.config}")
    private String urlFrontEnd;
    
    private static final String LOGO_URL = "https://i.ibb.co/q625FwD/logo-Juan-Lineage-baja-en-Negro.png";

    /**
     * Construye el cuerpo del email para reset de contraseña de usuario.
     * 
     * @param resetLink URL completa del enlace de reset de contraseña
     * @param email Email del usuario
     * @return String con el HTML del cuerpo del email
     */
    public String buildPasswordResetEmailBody(String resetLink, String email) {
        StringBuilder body = new StringBuilder();
        body.append("<div style='")
                .append("font-family: \"Segoe UI\", Tahoma, Geneva, Verdana, sans-serif;")
                .append("color: #dabe64; padding: 40px 20px; text-align: center;'>")
                .append("<img src='").append(LOGO_URL).append("' ")
                .append("alt='L2 Terra Logo' style='max-width: 280px; margin-bottom: 30px;' /><br>")
                .append("<div style='display: inline-block; background: linear-gradient(to bottom, #0c0f20, #000000);")
                .append("padding: 30px; border-radius: 12px; max-width: 360px; width: 100%; box-sizing: border-box;'>")
                .append("<p style='font-size: 16px; margin-bottom: 12px;'>Hello <strong>")
                .append(email)
                .append("</strong>!</p>")
                .append("<p style='font-size: 16px;'>We received a request to reset your password.</p>")
                .append("<a href='")
                .append(resetLink)
                .append("' style='display: inline-block; padding: 12px 24px; font-weight: 600; font-size: 16px;")
                .append("color: #0c0f20; background: linear-gradient(to right, #746535, #dabe64); border-radius: 8px;")
                .append("text-decoration: none; margin: 20px 0;'>Reset Password</a>")
                .append("<p style='font-size: 12px; opacity: 0.6;'>This link will expire in 15 minutes. If you didn't request this, just ignore it.</p>")
                .append("</div></div>");
        return body.toString();
    }

    /**
     * Construye el cuerpo del email para verificación de cuenta de usuario.
     * 
     * @param verificationCode Código de verificación
     * @param email Email del usuario
     * @return String con el HTML del cuerpo del email
     */
    public String buildVerificationEmailBody(String verificationCode, String email) {
        StringBuilder body = new StringBuilder();
        body.append("<div style='")
                .append("font-family: \"Segoe UI\", Tahoma, Geneva, Verdana, sans-serif;")
                .append("color: #dabe64; padding: 40px 20px; text-align: center;'>")
                .append("<img src='").append(LOGO_URL).append("' ")
                .append("alt='L2 Terra Logo' style='max-width: 280px; margin-bottom: 30px;' /><br>")
                .append("<div style='display: inline-block; background: linear-gradient(to bottom, #0c0f20, #000000);")
                .append("padding: 30px; border-radius: 12px; max-width: 360px; width: 100%; box-sizing: border-box;'>")
                .append("<p style='font-size: 16px; margin-bottom: 12px;'>Hello <strong>")
                .append(email)
                .append("</strong>!</p>")
                .append("<p style='font-size: 16px;'>To confirm your account, click below:</p>")
                .append("<a href='")
                .append(urlBackEnd)
                .append("/api/users/verifyEmail?token=")
                .append(verificationCode)
                .append("' style='display: inline-block; padding: 12px 24px; font-weight: 600; font-size: 16px;")
                .append("color: #0c0f20; background: linear-gradient(to right, #746535, #dabe64); border-radius: 8px;")
                .append("text-decoration: none; margin: 20px 0;'>Verify Email</a>")
                .append("<p style='font-size: 12px; opacity: 0.6;'>If you didn't request this, please ignore it.</p>")
                .append("</div></div>");
        return body.toString();
    }

    /**
     * Construye el cuerpo del email de confirmación cuando el email ha sido verificado exitosamente.
     * 
     * @return String con el HTML del cuerpo del email
     */
    public String buildVerifyEmailBody() {
        StringBuilder body = new StringBuilder();
        body.append("<div style='")
                .append("font-family: \"Segoe UI\", Tahoma, Geneva, Verdana, sans-serif;")
                .append("color: #dabe64; padding: 40px 20px; text-align: center;'>")
                .append("<img src='").append(LOGO_URL).append("' ")
                .append("alt='L2 Terra Logo' style='max-width: 280px; margin-bottom: 30px;' /><br>")
                .append("<div style='display: inline-block; background: linear-gradient(to bottom, #0c0f20, #000000);")
                .append("padding: 30px; border-radius: 12px; max-width: 360px; width: 100%; box-sizing: border-box;'>")
                .append("<p style='font-size: 16px;'>Email verified successfully!</p>")
                .append("<p style='font-size: 14px;'>You can now log in to the site.</p>")
                .append("<a href='")
                .append(urlFrontEnd)
                .append("' style='display: inline-block; padding: 12px 24px; font-weight: 600; font-size: 16px;")
                .append("color: #0c0f20; background: linear-gradient(to right, #746535, #dabe64); border-radius: 8px;")
                .append("text-decoration: none; margin-top: 10px;'>Go to the site</a>")
                .append("</div></div>");
        return body.toString();
    }
    
    /**
     * Construye el cuerpo del email para verificación durante el registro.
     * 
     * @param verificationCode Token de verificación
     * @param email Email del usuario que se está registrando
     * @return String con el HTML del cuerpo del email
     */
    public String buildRegistrationVerificationEmailBody(String verificationCode, String email) {
        StringBuilder body = new StringBuilder();
        body.append("<div style='")
                .append("font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;")
                .append("color: #dabe64;")
                .append("padding: 40px 20px;")
                .append("text-align: center;")
                .append("'>")
                .append("<img src='").append(LOGO_URL).append("' ")
                .append("alt='L2 Terra Logo' ")
                .append("style='max-width: 280px; margin-bottom: 30px;' />")
                .append("<br>")
                .append("<div style='")
                .append("display: inline-block;")
                .append("background: linear-gradient(to bottom, #0c0f20, #000000);")
                .append("padding: 30px;")
                .append("border-radius: 12px;")
                .append("max-width: 360px;")
                .append("width: 100%;")
                .append("color:#dabe64;")
                .append("box-sizing: border-box;")
                .append("'>")
                .append("<p style='font-size: 16px; margin-bottom: 10px;'>")
                .append("Click the button below to verify your email:").append(" ").append(email).append(" ")
                .append("</p>")
                .append("<p style='font-size: 14px; margin-bottom: 10px;'>")
                .append("This code of verification expires in 15 minutes.")
                .append("</p>")
                .append("<p style='font-size: 12px; opacity: 0.6; margin-bottom: 20px;'>")
                .append("If you did not request this, just ignore this email.")
                .append("</p>")
                .append("<a href='https://l2terra.online/verify-email?token=")
                .append(verificationCode)
                .append("' style='")
                .append("display: inline-block;")
                .append("padding: 12px 24px;")
                .append("font-weight: 600;")
                .append("font-size: 16px;")
                .append("color: #0c0f20;")
                .append("background: linear-gradient(to right, #746535, #dabe64);")
                .append("border-radius: 8px;")
                .append("text-decoration: none;")
                .append("margin-top: 10px;")
                .append("'>")
                .append("Verify email")
                .append("</a>")
                .append("</div>")
                .append("</div>");
        return body.toString();
    }

    /**
     * Construye el cuerpo del email para reset de contraseña de cuenta de juego.
     * 
     * <p>Genera un template HTML con el código de reset, nombre de la cuenta y
     * un enlace de regreso al sitio web.
     * 
     * @param code Código de reset de 6 dígitos
     * @param login Nombre de login de la cuenta de juego
     * @return String con el HTML del cuerpo del email
     * @see com.ak4n1.terra.api.terra_api.game.services.GameAccountService#generateAndSendCode(String)
     */
    public String buildGameAccountPasswordResetEmailBody(String code, String login) {
        StringBuilder body = new StringBuilder();
        body.append("<div style=\"")
                .append("font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;")
                .append("color: #dabe64;")
                .append("padding: 40px 20px;")
                .append("text-align: center;")
                .append("\">")
                .append("<img src=\"https://assets.l2terra.online/imgs/lineage2Terra_negro.png\" ")
                .append("alt=\"L2 Terra Logo\" ")
                .append("style=\"max-width: 280px; margin-bottom: 30px;\" />")
                .append("<br>")
                .append("<div style=\"")
                .append("display: inline-block;")
                .append("background: linear-gradient(to bottom, #0c0f20, #000000);")
                .append("padding: 30px;")
                .append("border-radius: 12px;")
                .append("max-width: 360px;")
                .append("width: 100%;")
                .append("box-sizing: border-box;")
                .append("box-shadow:0px 0px 30px black;")
                .append("\">")
                .append("<p style=\"font-size: 16px; margin-bottom: 12px;\">")
                .append("Your password reset code is:")
                .append("</p>")
                .append("<p style=\"font-size: 34px; font-weight: bold; color: #dabe64; margin: 20px 0;\">")
                .append(code)
                .append("</p>")
                .append("<p style=\"font-size: 16px; margin-bottom: 12px;\">")
                .append("Game account: <strong>").append(login).append("</strong>")
                .append("</p>")
                .append("<p style=\"font-size: 14px; margin-bottom: 10px;\">")
                .append("This code expires in 15 minutes.")
                .append("</p>")
                .append("<p style=\"font-size: 12px; opacity: 0.6; margin-bottom: 20px;\">")
                .append("If you did not request this, just ignore this email.")
                .append("</p>")
                .append("<a href=\"https://l2terra.online\" style=\"")
                .append("display: inline-block;")
                .append("padding: 12px 24px;")
                .append("font-weight: 600;")
                .append("font-size: 16px;")
                .append("color: #0c0f20;")
                .append("background: linear-gradient(to right, #746535, #dabe64);")
                .append("border-radius: 8px;")
                .append("text-decoration: none;")
                .append("margin-top: 10px;")
                .append("\">")
                .append("Back to Website")
                .append("</a>")
                .append("</div>")
                .append("</div>");
        return body.toString();
    }

    /**
     * Construye el cuerpo del email para código de creación de cuenta de juego.
     * 
     * <p>Genera un template HTML con el código de creación necesario para
     * completar el registro de una nueva cuenta de juego.
     * 
     * @param code Código de creación de 6 dígitos
     * @return String con el HTML del cuerpo del email
     * @see com.ak4n1.terra.api.terra_api.game.services.GameAccountService#generateAndSendCreateCode(String)
     */
    public String buildGameAccountCreationCodeEmailBody(String code) {
        StringBuilder body = new StringBuilder()
                .append("<div style=\"font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; color: #dabe64; padding: 40px 20px; text-align: center;\">")
                .append("<img src=\"https://assets.l2terra.online/logoJuanLineage_baja_enNegro.webp\" alt=\"L2 Terra Logo\" style=\"max-width: 280px; margin-bottom: 30px;\" />")
                .append("<br>")
                .append("<div style=\"display: inline-block; background: linear-gradient(to bottom, #0c0f20, #000000); padding: 30px; border-radius: 12px; max-width: 360px; width: 100%; box-sizing: border-box;\">")
                .append("<p style=\"font-size: 16px; margin-bottom: 12px;color: #dabe64;\">Your code to complete account creation is:</p>")
                .append("<p style=\"font-size: 34px; font-weight: bold; color: #dabe64; margin: 20px 0;\">")
                .append(code)
                .append("</p>")
                .append("<p style=\"font-size: 14px; margin-bottom: 10px;color: #dabe64;\">This code expires in 15 minutes.</p>")
                .append("<p style=\"font-size: 12px; opacity: 0.6; margin-bottom: 20px;color: #dabe64;\">If you didn't request this, you can ignore the message.</p>")
                .append("<a href=\"https://l2terra.online\" style=\"display: inline-block; padding: 12px 24px; font-weight: 600; font-size: 16px; color: #0c0f20; background: linear-gradient(to right, #746535, #dabe64); border-radius: 8px; text-decoration: none; margin-top: 10px;\">")
                .append("Back to Website")
                .append("</a>")
                .append("</div>")
                .append("</div>");
        return body.toString();
    }
} 