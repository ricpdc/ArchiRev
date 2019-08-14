package es.alarcos.archirev.persistency;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import es.alarcos.archirev.model.AbstractEntity;

@Service
@Transactional
public class AbstractDao<T extends AbstractEntity> implements Serializable {

	private static final long serialVersionUID = 1812196413677495102L;

	private Class<T> clazz;

	@PersistenceContext
	protected transient EntityManager entityManager;

	public void setClazz(Class<T> clazzToSet) {
		this.clazz = clazzToSet;
	}

	public T findById(Long id) {
		return entityManager.find(clazz, id);
	}

	@SuppressWarnings("unchecked")
	public List<T> findAll() {
		return entityManager.createQuery("from " + clazz.getName()).getResultList();
	}
	
	@SuppressWarnings("unchecked")
	public List<T> findAllOrderById() {
		return entityManager.createQuery("from " + clazz.getName() + " order by id").getResultList();
	}

	public void persist(T entity) {
		Timestamp now = new Timestamp(new Date().getTime());
		entity.setCreatedAt(now);
		entity.setModifiedAt(now);
		entityManager.persist(entity);
	}

	public T update(T entity) {
		Timestamp now = new Timestamp(new Date().getTime());
		entity.setModifiedAt(now);
		return entityManager.merge(entity);
	}
	
	public void refresh(T entity) {
		entityManager.refresh(entity);
	}

	public T persistOrUpdate(T entity) {
		if (entity.getId() == null) {
			persist(entity);
			return null;
		} else {
			return update(entity);
		}
	}

	public void remove(T entity) {
		entityManager.remove(entityManager.contains(entity) ? entity : entityManager.merge(entity));
	}

	public void removeById(Long entityId) {
		T entity = findById(entityId);
		remove(entity);
	}
}