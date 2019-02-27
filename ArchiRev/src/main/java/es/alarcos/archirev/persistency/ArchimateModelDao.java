package es.alarcos.archirev.persistency;

import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;

import es.alarcos.archirev.model.ArchimateModel;

@Repository
@Component
public class ArchimateModelDao extends AbstractDao<ArchimateModel> {

	private static final long serialVersionUID = -4674373127220392179L;

	public ArchimateModelDao() {
		super();
		setClazz(ArchimateModel.class);
	}
	
	
}