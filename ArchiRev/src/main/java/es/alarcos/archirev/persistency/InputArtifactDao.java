package es.alarcos.archirev.persistency;

import java.util.List;

import javax.persistence.TypedQuery;

import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;

import es.alarcos.archirev.model.InputArtifact;

@Repository
@Component
public class InputArtifactDao extends AbstractDao<InputArtifact> {

	private static final long serialVersionUID = -672166759371530731L;

	public InputArtifactDao() {
		super();
		setClazz(InputArtifact.class);
	}
	
	public List<String> getInputArtifactNames() {
		TypedQuery<String> query = entityManager.createNamedQuery("InputArtifact.getNames", String.class);
		return query.getResultList();
	}

	
}