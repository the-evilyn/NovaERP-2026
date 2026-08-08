package com.novaerp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * NovaERP Main Application Bootstrap.
 *
 * <p>Intelligent ERP Platform for Commercial Management, Inventory Management,
 * Invoicing, and AI Decision Support for Industrial SMEs.</p>
 *
 * @author NovaERP Engineering Team
 * @version 1.0.0
 */
@SpringBootApplication
@ConfigurationPropertiesScan
@EnableJpaAuditing(auditorAwareRef = "auditorAware")
@EnableAsync
@EnableScheduling
@EnableTransactionManagement
public class NovaErpApplication {

    public static void main(String[] args) {
        SpringApplication.run(NovaErpApplication.class, args);
    }
}
