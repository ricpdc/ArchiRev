package es.alarcos.archirev.persistency;

import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;

import es.alarcos.archirev.model.Source;

@Repository
@Component
public class SourceDao extends AbstractJpaDAO<Source> {

	public SourceDao() {
		super();
		setClazz(Source.class);
	}
}