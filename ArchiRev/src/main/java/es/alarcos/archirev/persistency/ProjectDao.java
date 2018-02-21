package es.alarcos.archirev.persistency;

import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;

import es.alarcos.archirev.model.Project;

@Repository
@Component
public class ProjectDao extends AbstractJpaDAO<Project> {

	public ProjectDao() {
		super();
		setClazz(Project.class);
	}
}