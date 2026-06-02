package edu.brunobudris.ke.rdf4j_server.service.repositories;

import edu.brunobudris.ke.rdf4j_server.exception.CommonControllerException;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.tuple.Pair;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.query.QueryResults;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.query.impl.IteratingTupleQueryResult;
import org.eclipse.rdf4j.query.impl.MapBindingSet;
import org.eclipse.rdf4j.query.resultio.QueryResultWriter;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryResult;
import org.eclipse.rdf4j.repository.manager.RepositoryManager;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import static edu.brunobudris.ke.rdf4j_server.exception.CommonControllerExceptionType.NOT_FOUND;
import static edu.brunobudris.ke.rdf4j_server.util.ControllerUtils.APPLICATION_SPARQL_RESULTS_JSON;
import static edu.brunobudris.ke.rdf4j_server.util.RdfInOutUtils.getAcceptAndQueryResultWriterOrDefaultIfAcceptable;

@Service
@RequiredArgsConstructor
public class RepositoryContextsService {

    private static final String CONTEXT_ID = "contextID";

    private static final List<String> BINDING_NAMES = List.of(CONTEXT_ID);

    private final RepositoryManager repositoryManager;

    public void getContexts(
            HttpServletResponse servletResponse,
            String repositoryID,
            List<String> acceptHeaders
    ) {
        Repository repository = repositoryManager.getRepository(repositoryID);
        if (repository == null) {
            throw new CommonControllerException(NOT_FOUND, "Repository not found");
        }

        OutputStream outputStream;
        try {
            outputStream = servletResponse.getOutputStream();
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }

        Pair<String, QueryResultWriter> acceptAndWriter = getAcceptAndQueryResultWriterOrDefaultIfAcceptable(acceptHeaders, outputStream, APPLICATION_SPARQL_RESULTS_JSON);
        servletResponse.setContentType(acceptAndWriter.getLeft());

        try (RepositoryConnection connection = repository.getConnection()) {
            RepositoryResult<Resource> contextIDs = connection.getContextIDs();

            List<MapBindingSet> bindingSets = new ArrayList<>();
            for (Resource contextID : contextIDs) {
                MapBindingSet set = new MapBindingSet();
                set.setBinding(CONTEXT_ID, contextID);
                bindingSets.add(set);
            }

            TupleQueryResult result = new IteratingTupleQueryResult(BINDING_NAMES, bindingSets);

            QueryResultWriter writer = acceptAndWriter.getRight();
            writer.startDocument();
            writer.startHeader();
            QueryResults.report(result, writer);
        }
    }
}
