package edu.brunobudris.ke.rdf4j_server.service.repositories;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.tuple.Pair;
import org.eclipse.rdf4j.query.QueryResults;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.query.impl.IteratingTupleQueryResult;
import org.eclipse.rdf4j.query.impl.MapBindingSet;
import org.eclipse.rdf4j.query.resultio.QueryResultWriter;
import org.eclipse.rdf4j.repository.manager.RepositoryInfo;
import org.eclipse.rdf4j.repository.manager.RepositoryManager;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static edu.brunobudris.ke.rdf4j_server.util.ControllerUtils.APPLICATION_SPARQL_RESULTS_JSON;
import static edu.brunobudris.ke.rdf4j_server.util.RdfInOutUtils.getAcceptAndQueryResultWriterOrDefaultIfAcceptable;
import static org.eclipse.rdf4j.model.util.Values.iri;
import static org.eclipse.rdf4j.model.util.Values.literal;

@Service
@RequiredArgsConstructor
public class RetrieveRepositoriesService {

    private static final String URI = "uri";
    private static final String ID = "id";
    private static final String TITLE = "title";
    private static final String READABLE = "readable";
    private static final String WRITABLE = "writable";

    private static final List<String> BINDING_NAMES = List.of(
            URI, ID, TITLE, READABLE, WRITABLE
    );

    private final RepositoryManager repositoryManager;

    public void getRepositories(HttpServletResponse servletResponse, List<String> acceptHeaders) {
        OutputStream outputStream;
        try {
            outputStream = servletResponse.getOutputStream();
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }

        Pair<String, QueryResultWriter> acceptAndWriter = getAcceptAndQueryResultWriterOrDefaultIfAcceptable(acceptHeaders, outputStream, APPLICATION_SPARQL_RESULTS_JSON);
        servletResponse.setContentType(acceptAndWriter.getLeft());
        Collection<RepositoryInfo> repositoryInfos = repositoryManager.getAllRepositoryInfos();

        List<MapBindingSet> bindingSets = new ArrayList<>();
        for (RepositoryInfo info : repositoryInfos) {
            MapBindingSet set = new MapBindingSet();
            set.setBinding(URI, iri(info.getLocation().toString()));
            set.setBinding(ID, literal(info.getId()));
            set.setBinding(TITLE, literal(info.getDescription()));
            set.setBinding(READABLE, literal(info.isReadable()));
            set.setBinding(WRITABLE, literal(info.isWritable()));
            bindingSets.add(set);
        }
        TupleQueryResult result = new IteratingTupleQueryResult(BINDING_NAMES, bindingSets);
        QueryResultWriter writer = acceptAndWriter.getRight();

        writer.startDocument();
        writer.startHeader();
        QueryResults.report(result, writer);
    }
}
