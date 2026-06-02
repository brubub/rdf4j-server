package edu.brunobudris.ke.rdf4j_server.service.repositories;

import edu.brunobudris.ke.rdf4j_server.exception.CommonControllerException;
import edu.brunobudris.ke.rdf4j_server.util.RdfMediaUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.impl.TreeModel;
import org.eclipse.rdf4j.repository.config.RepositoryConfig;
import org.eclipse.rdf4j.repository.manager.RepositoryManager;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFParser;
import org.eclipse.rdf4j.rio.Rio;
import org.eclipse.rdf4j.rio.helpers.StatementCollector;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;

import static edu.brunobudris.ke.rdf4j_server.exception.CommonControllerExceptionType.CONFLICT;
import static edu.brunobudris.ke.rdf4j_server.exception.CommonControllerExceptionType.INTERNAL_ERROR;
import static org.eclipse.rdf4j.repository.config.RepositoryConfigUtil.getRepositoryConfig;

@Slf4j
@Service
@RequiredArgsConstructor
public class CreateRepositoryService {

    private final RepositoryManager repositoryManager;

    public void createRepository(String contentType, String repositoryID, InputStream requestBody) {
        boolean hasRepositoryConfig = repositoryManager.hasRepositoryConfig(repositoryID);
        if (hasRepositoryConfig) {
            throw new CommonControllerException(CONFLICT, "Repository already exists");
        }

        RDFFormat rdfFormat = RdfMediaUtils.getRDFFormatByContentType(contentType);
        RepositoryConfig repositoryConfig = prepareRepositoryConfig(rdfFormat, repositoryID, requestBody);
        repositoryManager.addRepositoryConfig(repositoryConfig);
    }

    private RepositoryConfig prepareRepositoryConfig(
            RDFFormat rdfFormat,
            String repositoryID,
            InputStream repoConfig
    ) {
        Model model = new TreeModel();

        try {
            RDFParser rdfParser = Rio.createParser(rdfFormat);
            rdfParser.setRDFHandler(new StatementCollector(model));
            rdfParser.parse(repoConfig);
        } catch (IOException ex) {
            log.error("Repo configuration parsing error", ex);
            throw new CommonControllerException(INTERNAL_ERROR, "Repo configuration parsing error");
        }

        return getRepositoryConfig(model, repositoryID);
    }
}
