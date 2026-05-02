package com.jeu.reflexion.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan(basePackages = {"com.jeu.reflexion.service"})
public class AppRootConfig {
}
