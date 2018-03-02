package es.alarcos.archirev.persistency;

import java.util.List;

import javax.persistence.TypedQuery;

import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;

import es.alarcos.archirev.model.Project;

@Repository
@Component
public class ProjectDao extends AbstractDao<Project> {

	private static final long serialVersionUID = 6066644818853071310L;

	public ProjectDao() {
		super();
		setClazz(Project.class);
	}

	public List<Project> findProjectsByUser(final String loggedUser) {
		TypedQuery<Project> query = entityManager.createNamedQuery("Project.findByUser", Project.class);
		query.setParameter("createdBy", loggedUser);
		return query.getResultList();
	}
}