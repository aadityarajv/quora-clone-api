package org.example.quora.service;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * Enabling the component scan and entity scan of classes respectively.
 */
@Configuration
@ComponentScan("org.example.quora.service")
@EntityScan("org.example.quora.service.entity")
public class ServiceConfiguration {
}
