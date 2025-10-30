package com.ak4n1.terra.api.terra_api.security.filters;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Filtro que agrega headers HTTP de seguridad adicionales.
 * 
 * <p>Agrega headers estándar de seguridad para proteger contra:
 * - Clickjacking (X-Frame-Options ya está en Spring Security)
 * - XSS (X-XSS-Protection)
 * - MIME type sniffing (X-Content-Type-Options ya está en Spring Security)
 * - Referrer leaks (Referrer-Policy)
 * 
 * @author ak4n1
 * @since 1.0
 */
@Component
@Order(0) // Ejecutar muy temprano, antes que otros filtros
public class SecurityHeadersFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // X-XSS-Protection: activar protección XSS del navegador
        response.setHeader("X-XSS-Protection", "1; mode=block");

        // Referrer-Policy: controlar qué información de referrer se envía
        response.setHeader("Referrer-Policy", "strict-origin-when-cross-origin");

        // Permissions-Policy: deshabilitar features no necesarias (opcional)
        response.setHeader("Permissions-Policy", 
                "geolocation=(), microphone=(), camera=(), payment=(), usb=()");

        // Continuar con la cadena de filtros
        filterChain.doFilter(request, response);
    }
}

