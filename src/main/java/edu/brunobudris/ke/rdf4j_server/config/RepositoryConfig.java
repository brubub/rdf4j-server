package edu.brunobudris.ke.rdf4j_server.config;

import org.eclipse.rdf4j.repository.manager.LocalRepositoryManager;
import org.eclipse.rdf4j.repository.manager.RepositoryManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.File;

@Configuration
public class RepositoryConfig {

    private static final String BASE_DIR_PATH = "src/main/resources/repo/";

    @Bean
    public RepositoryManager repositoryManager() {
        LocalRepositoryManager manager = new LocalRepositoryManager(new File(BASE_DIR_PATH));
        manager.init();

        return manager;
    }
}
