package es.alarcos.archirev.persistency;

import java.util.List;

import javax.persistence.TypedQuery;

import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;

import es.alarcos.archirev.model.Technique;

@Repository
@Component
public class TechniqueDao extends AbstractDao<Technique> {

	private static final long serialVersionUID = -672166759371530731L;

	public TechniqueDao() {
		super();
		setClazz(Technique.class);
	}
	
	public List<String> getTechniquesNames() {
		TypedQuery<String> query = entityManager.createNamedQuery("Technique.getNames", String.class);
		return query.getResultList();
	}

	
}