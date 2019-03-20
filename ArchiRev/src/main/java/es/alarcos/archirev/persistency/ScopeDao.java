package es.alarcos.archirev.persistency;

import java.util.List;

import javax.persistence.TypedQuery;

import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;

import es.alarcos.archirev.model.Scope;

@Repository
@Component
public class ScopeDao extends AbstractDao<Scope> {

	private static final long serialVersionUID = 2186744595906355921L;

	public ScopeDao() {
		super();
		setClazz(Scope.class);
	}
	
	public List<String> getScopeNames() {
		TypedQuery<String> query = entityManager.createNamedQuery("Scope.getNames", String.class);
		return query.getResultList();
	}

	
}