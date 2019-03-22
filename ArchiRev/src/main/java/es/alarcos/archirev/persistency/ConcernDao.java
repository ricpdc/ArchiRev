package es.alarcos.archirev.persistency;

import java.util.List;

import javax.persistence.TypedQuery;

import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;

import es.alarcos.archirev.model.Concern;

@Repository
@Component
public class ConcernDao extends AbstractDao<Concern> {

	private static final long serialVersionUID = 998064176838016266L;

	public ConcernDao() {
		super();
		setClazz(Concern.class);
	}
	
	public List<String> getConcernNames() {
		TypedQuery<String> query = entityManager.createNamedQuery("Concern.getNames", String.class);
		return query.getResultList();
	}

	
}