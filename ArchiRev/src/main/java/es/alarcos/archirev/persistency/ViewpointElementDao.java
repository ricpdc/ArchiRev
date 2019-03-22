package es.alarcos.archirev.persistency;

import java.util.List;

import javax.persistence.TypedQuery;

import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;

import es.alarcos.archirev.model.ViewpointElement;

@Repository
@Component
public class ViewpointElementDao extends AbstractDao<ViewpointElement> {

	private static final long serialVersionUID = -8033225343512213343L;

	public ViewpointElementDao() {
		super();
		setClazz(ViewpointElement.class);
	}
	
	public List<String> getElementNames() {
		TypedQuery<String> query = entityManager.createNamedQuery("ViewpointElement.getNames", String.class);
		return query.getResultList();
	}

	
}