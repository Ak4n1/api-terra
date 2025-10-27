package com.ak4n1.terra.api.terra_api.auth.interceptors;

import com.ak4n1.terra.api.terra_api.auth.entities.RecentActivity;
import com.ak4n1.terra.api.terra_api.auth.repositories.RecentActivityRepository;
import com.ak4n1.terra.api.terra_api.auth.repositories.AccountMasterRepository;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.*;

@Component
public class ActivityInterceptor implements HandlerInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(ActivityInterceptor.class);

    private final RecentActivityRepository activityRepo;
    private final AccountMasterRepository userRepo;


    public ActivityInterceptor(RecentActivityRepository activityRepo, AccountMasterRepository userRepo) {
        this.activityRepo = activityRepo;
        this.userRepo = userRepo;
    }

    private String getEmailFromToken() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getName().equals("anonymousUser")) {
            return null;
        }
        String email = auth.getName();
        return email;
    }


            // NOTA: Login y Logout se manejan en los filtros/controladores
    private static final List<String> pathsToLog = List.of(
            "/api/auth/login",
            "/api/auth/google/login",
            "/api/game/auth/changePassword",
            "/api/game/auth/registerGameAccount"
    );

    @Override
    public void afterCompletion(HttpServletRequest req, HttpServletResponse res, Object handler, Exception ex) {
        String path = req.getRequestURI();
        int status = res.getStatus();

        // Solo procesar paths específicos
        if (!pathsToLog.contains(path)) {
            return;
        }
        
        if (status >= 400) {
            logger.warn("❌ [ACTIVITY] Acción fallida, no se registra actividad. Status: {}", status);
            return;
        }

        String email = getEmailFromToken();
        if (email == null) {
            logger.warn("❌ [ACTIVITY] No se pudo obtener email del token para path: {}", path);
            return;
        }

        userRepo.findByEmail(email).ifPresent(user -> {
            RecentActivity activity = new RecentActivity();
            activity.setAccountMaster(user);
            activity.setTimestamp(new Date());
            activity.setIpAddress(req.getRemoteAddr());

            
            if (path.equals("/api/auth/login")) {
                activity.setAction("Login");
            } else if (path.equals("/api/auth/google/login")) {
                activity.setAction("Google Login");
            } else if (path.equals("/api/game/auth/changePassword")) {
                activity.setAction("Game Password Changed");
            } else if (path.equals("/api/game/auth/registerGameAccount")) {
                activity.setAction("Game Account Created");
            }

            activityRepo.save(activity);
            logger.info("✅ [ACTIVITY] Actividad registrada: {} - {}", activity.getAction(), email);
        });
    }
}
