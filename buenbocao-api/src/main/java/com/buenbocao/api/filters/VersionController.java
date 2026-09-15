package com.buenbocao.api.filters;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Endpoint publico para que el frontend consulte version minima y estado de
 * mantenimiento.
 * URL final: GET /api/v1/version (context-path + /version)
 */
@RestController
@RequestMapping("/version")
public class VersionController {

    @Value("${app.version.min-android:1.0.0}")
    private String minAndroid;

    @Value("${app.version.min-ios:1.0.0}")
    private String minIos;

    @Value("${app.maintenance:false}")
    private boolean maintenance;

    @Value("${app.maintenance.message:En mantenimiento}")
    private String maintenanceMessage;

    @GetMapping
    public ResponseEntity<?> getVersion() {
        return ResponseEntity.ok(Map.of(
                "minAndroid", minAndroid,
                "minIos", minIos,
                "maintenance", maintenance,
                "maintenanceMessage", maintenanceMessage));
    }
}
