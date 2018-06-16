package es.alarcos.archirev.persistency;

import javax.persistence.TypedQuery;

import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;

import es.alarcos.archirev.model.Extraction;

@Repository
@Component
public class ExtractionDao extends AbstractDao<Extraction> {

	private static final long serialVersionUID = -5511188630705887367L;

	public ExtractionDao() {
		super();
		setClazz(Extraction.class);
	}
	
	public Extraction findLastExtraction() {
		TypedQuery<Extraction> query = entityManager.createNamedQuery("Extraction.getLast", Extraction.class);
		return query.getSingleResult();
	}

	
}