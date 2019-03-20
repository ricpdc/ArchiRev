package es.alarcos.archirev.persistency;

import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;

import es.alarcos.archirev.model.Viewpoint;

@Repository
@Component
public class ViewpointDao extends AbstractDao<Viewpoint> {

	private static final long serialVersionUID = -513674061420256678L;

	public ViewpointDao() {
		super();
		setClazz(Viewpoint.class);
	}

	
}