package es.alarcos.archirev.persistency;

import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;

import es.alarcos.archirev.model.Model;

@Repository
@Component
public class ModelDao extends AbstractDao<Model> {

	private static final long serialVersionUID = 8335418223699404609L;

	public ModelDao() {
		super();
		setClazz(Model.class);
	}
	
	
}