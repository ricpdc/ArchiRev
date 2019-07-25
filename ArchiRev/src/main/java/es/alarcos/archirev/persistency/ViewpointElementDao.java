package es.alarcos.archirev.persistency;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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

	public List<ViewpointElement> getElementsById(final List<Integer> elementIds) {
		if (elementIds == null || elementIds.isEmpty()) {
			return new ArrayList<>();
		}

		List<Long> ids = elementIds.stream().map(Integer::longValue).collect(Collectors.toList());

		TypedQuery<ViewpointElement> query = entityManager.createNamedQuery("ViewpointElement.getElementsById",
				ViewpointElement.class);
		query.setParameter("elementIds", ids);
		return query.getResultList();
	}

}