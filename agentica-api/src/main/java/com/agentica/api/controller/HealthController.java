package com.agentica.api.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.time.Instant;
import java.util.Map;

/**
 * Health check endpoints for monitoring.
 */
@RestController
@RequestMapping("/api/v1/health")
@Tag(name = "Health", description = "Health check endpoints")
public class HealthController {

    @GetMapping
    @Operation(summary = "Health check", description = "Returns the health status of the application")
    public ResponseEntity<Map<String, Object>> health() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "timestamp", Instant.now().toString(),
                "application", "Agentica"
        ));
    }

    @GetMapping("/ready")
    @Operation(summary = "Readiness check", description = "Returns whether the application is ready to serve traffic")
    public ResponseEntity<Map<String, Object>> ready() {
        return ResponseEntity.ok(Map.of(
                "ready", true,
                "timestamp", Instant.now().toString()
        ));
    }

    @GetMapping("/live")
    @Operation(summary = "Liveness check", description = "Returns whether the application is alive")
    public ResponseEntity<Map<String, Object>> live() {
        return ResponseEntity.ok(Map.of(
                "live", true,
                "timestamp", Instant.now().toString()
        ));
    }

}
