package es.alarcos.archirev.persistency;

import java.util.List;

import javax.persistence.TypedQuery;

import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;

import es.alarcos.archirev.model.Stakeholder;

@Repository
@Component
public class StakeholderDao extends AbstractDao<Stakeholder> {

	private static final long serialVersionUID = 6662505706257239356L;

	public StakeholderDao() {
		super();
		setClazz(Stakeholder.class);
	}
	
	public List<String> getStakeholdersNames() {
		TypedQuery<String> query = entityManager.createNamedQuery("Stakeholder.getNames", String.class);
		return query.getResultList();
	}

	
}