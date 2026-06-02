package edu.brunobudris.ke.rdf4j_server.service.graph;

import edu.brunobudris.ke.rdf4j_server.exception.CommonControllerException;
import lombok.RequiredArgsConstructor;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.manager.RepositoryManager;
import org.springframework.stereotype.Service;

import static edu.brunobudris.ke.rdf4j_server.exception.CommonControllerExceptionType.NOT_FOUND;
import static org.eclipse.rdf4j.model.util.Values.iri;

@Service
@RequiredArgsConstructor
public class ClearGraphStatementsService {

    private final RepositoryManager repositoryManager;

    public void clearGraph(
            String repositoryID,
            String namedGraph
    ) {
        IRI context = iri(namedGraph);

        Repository repository = repositoryManager.getRepository(repositoryID);
        if (repository == null) {
            throw new CommonControllerException(NOT_FOUND, "Repository not found");
        }

        try (RepositoryConnection connection = repository.getConnection()) {
            connection.clear(context);
        }
    }

}
