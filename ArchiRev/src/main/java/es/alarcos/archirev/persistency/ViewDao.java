package es.alarcos.archirev.persistency;

import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;

import es.alarcos.archirev.model.View;

@Repository
@Component
public class ViewDao extends AbstractDao<View> {

	private static final long serialVersionUID = -1453942088432592119L;

	public ViewDao() {
		super();
		setClazz(View.class);
	}
	
	
}