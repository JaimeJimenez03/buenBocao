package com.buenbocao.api.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * Configuración general de la aplicación.
 */
@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorAware")
public class AppConfig {

}
