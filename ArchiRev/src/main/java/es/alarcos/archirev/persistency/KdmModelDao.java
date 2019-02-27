package es.alarcos.archirev.persistency;

import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;

import es.alarcos.archirev.model.KdmModel;

@Repository
@Component
public class KdmModelDao extends AbstractDao<KdmModel> {


	/**
	 * 
	 */
	private static final long serialVersionUID = -3619685785339351977L;

	public KdmModelDao() {
		super();
		setClazz(KdmModel.class);
	}
	
	
}