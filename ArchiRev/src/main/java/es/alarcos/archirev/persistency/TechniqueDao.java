package es.alarcos.archirev.persistency;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.TypedQuery;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.hibernate.exception.SQLGrammarException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import es.alarcos.archirev.model.InputArtifact;
import es.alarcos.archirev.model.Technique;

@Repository
@Component
public class TechniqueDao extends AbstractDao<Technique> {

	private static final long serialVersionUID = -672166759371530731L;
	
	static Logger logger = LoggerFactory.getLogger(TechniqueDao.class);

	public TechniqueDao() {
		super();
		setClazz(Technique.class);
	}
	
	public List<String> getTechniquesNames() {
		TypedQuery<String> query = entityManager.createNamedQuery("Technique.getNames", String.class);
		return query.getResultList();
	}

	
	
//	public MultiValueMap<InputArtifact, Technique> getTechniqueMapByArtifact(List<InputArtifact> artifacts) {
//		MultiValueMap<InputArtifact, Technique> techniqueMapByArtifact = new LinkedMultiValueMap<>();
//
//		if (artifacts == null || artifacts.isEmpty()) {
//			return null;
//		}
//
//		String stringQuery = "SELECT distinct t FROM Technique t, InputArtifact a, MiningPoint m " + 
//				"WHERE m.technique.id = t.id and m.artifact = a.id and a.id = (:artifactId)";
//
//		try {
//			
//			//Query query = entityManager.createNativeQuery(stringQuery);
//			TypedQuery<Technique> query = entityManager.createQuery(stringQuery, Technique.class);
//			
//			for(InputArtifact artifact : artifacts) {
//				query.setParameter("artifactId", artifact.getId());
//				List<Technique> techniques = query.getResultList();
//				techniqueMapByArtifact.put(artifact, techniques);
//			}
//			
//		} catch (SQLGrammarException e) {
//			logger.error(e.getMessage());
//		}
//
//		return techniqueMapByArtifact;
//	}
	
	public List<Pair<InputArtifact, Technique>> getTechniqueListByArtifact(List<InputArtifact> artifacts) {
		List<Pair<InputArtifact, Technique>> techniqueMapByArtifact = new ArrayList<>();

		if (artifacts == null || artifacts.isEmpty()) {
			return null;
		}

		String stringQuery = "SELECT distinct t FROM Technique t, InputArtifact a, MiningPoint m " + 
				"WHERE m.technique.id = t.id and m.artifact = a.id and a.id = (:artifactId)";

		try {
			
			//Query query = entityManager.createNativeQuery(stringQuery);
			TypedQuery<Technique> query = entityManager.createQuery(stringQuery, Technique.class);
			
			for(InputArtifact artifact : artifacts) {
				query.setParameter("artifactId", artifact.getId());
				List<Technique> techniques = query.getResultList();
				for(Technique technique : techniques) {
					techniqueMapByArtifact.add(new ImmutablePair<>(artifact, technique));
				}
			}
			
		} catch (SQLGrammarException e) {
			logger.error(e.getMessage());
		}

		return techniqueMapByArtifact;
	}
	
}