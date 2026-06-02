package edu.brunobudris.ke.rdf4j_server.service.remover;

import lombok.Getter;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.eclipse.rdf4j.rio.RDFHandlerException;
import org.eclipse.rdf4j.rio.helpers.AbstractRDFHandler;

import static org.eclipse.rdf4j.model.vocabulary.SESAME.WILDCARD;

public class SesameTolerantRDFRemover extends AbstractRDFHandler {

    private final RepositoryConnection con;
    private boolean enforceContext;
    @Getter
    private Resource context;

    public SesameTolerantRDFRemover(RepositoryConnection con) {
        this.con = con;
        this.enforceContext = false;
    }

    public void enforceContext(Resource context) {
        this.context = context;
        this.enforceContext = true;
    }

    public boolean enforcesContext() {
        return this.enforceContext;
    }

    public void handleStatement(Statement st) throws RDFHandlerException {
        try {
            this.con.remove(
                    replaceSesameWildcard(st.getSubject()),
                    replaceSesameWildcard(st.getPredicate()),
                    replaceSesameWildcard(st.getObject()),
                    this.enforceContext ? this.context : st.getContext());
        } catch (RepositoryException e) {
            throw new RDFHandlerException(e);
        }
    }

    private static <T> T replaceSesameWildcard(T object) {
        if (WILDCARD.equals(object)) {
            return null;
        }
        return object;
    }
}
