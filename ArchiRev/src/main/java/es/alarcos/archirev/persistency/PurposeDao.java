package es.alarcos.archirev.persistency;

import java.util.List;

import javax.persistence.TypedQuery;

import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;

import es.alarcos.archirev.model.Purpose;

@Repository
@Component
public class PurposeDao extends AbstractDao<Purpose> {

	private static final long serialVersionUID = -672166759371530731L;

	public PurposeDao() {
		super();
		setClazz(Purpose.class);
	}
	
	public List<String> getPurposeNames() {
		TypedQuery<String> query = entityManager.createNamedQuery("Purpose.getNames", String.class);
		return query.getResultList();
	}

	
}