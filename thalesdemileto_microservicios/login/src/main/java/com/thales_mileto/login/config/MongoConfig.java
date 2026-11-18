package com.thales_mileto.login.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.EnableMongoAuditing;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@Configuration
@EnableMongoRepositories(basePackages = "com.thales_mileto.login.repository")
@EnableMongoAuditing
public class MongoConfig
{
    // Configuración adicional si es necesaria
}
