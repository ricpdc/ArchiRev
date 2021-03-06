package es.alarcos.archirev.persistency;

import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;

import es.alarcos.archirev.model.Source;

@Repository
@Component
public class SourceDao extends AbstractDao<Source> {

	private static final long serialVersionUID = 5100814968920748886L;

	public SourceDao() {
		super();
		setClazz(Source.class);
	}
}