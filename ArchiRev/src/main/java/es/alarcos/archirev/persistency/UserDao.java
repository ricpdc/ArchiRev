package es.alarcos.archirev.persistency;

import java.util.List;

import javax.persistence.TypedQuery;

import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;

import es.alarcos.archirev.model.User;

@Repository
@Component
public class UserDao extends AbstractDao<User> {

	private static final long serialVersionUID = -3142827589560074428L;

	public UserDao() {
		super();
		setClazz(User.class);
	}

	public User getUserByUsername(final String username) {
		TypedQuery<User> query = entityManager.createNamedQuery("User.findByUsername", User.class);
		query.setParameter("username", username);
		List<User> results = query.getResultList();
		return !CollectionUtils.isEmpty(results) ? results.get(0) : null;
	}
}