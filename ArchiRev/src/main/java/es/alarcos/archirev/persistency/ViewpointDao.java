package es.alarcos.archirev.persistency;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.persistence.Query;

import org.hibernate.exception.SQLGrammarException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;

import es.alarcos.archirev.model.InputArtifact;
import es.alarcos.archirev.model.Stakeholder;
import es.alarcos.archirev.model.Viewpoint;

@Repository
@Component
public class ViewpointDao extends AbstractDao<Viewpoint> {

	private static final long serialVersionUID = -513674061420256678L;

	static Logger LOGGER = LoggerFactory.getLogger(ViewpointDao.class);

	public ViewpointDao() {
		super();
		setClazz(Viewpoint.class);
	}

	@SuppressWarnings("unchecked")
	public List<QueriedViewpointDTO> listViewpointsByTechniques(final Long id) {
		String stringQuery = "select v.id as id, v.name as viewpoint from av_viewpoint as v where v.id=:id";

		Query query = entityManager.createNativeQuery(stringQuery);
		query.setParameter("id", 1);

		List<Object[]> resultSet = query.getResultList();

		List<QueriedViewpointDTO> resultList = new ArrayList<>();

		for (Object[] tuple : resultSet) {
			QueriedViewpointDTO viewpoint = new QueriedViewpointDTO();
			viewpoint.setId(((BigInteger) tuple[0]).longValue());
			viewpoint.setName((String) tuple[1]);
			resultList.add(viewpoint);
		}

		return resultList;
	}

	@SuppressWarnings("unchecked")
	public List<QueriedViewpointDTO> listViewpointsByArtefacts(final List<InputArtifact> artefacts) {
		List<QueriedViewpointDTO> resultList = new ArrayList<>();

		if (artefacts == null || artefacts.isEmpty()) {
			return resultList;
		}

		String stringQuery = "select v.id as id, v.name as viewpoint, t.name as technique, count(distinct e.name) as num_element, ("
				+ "		select count(e2.name) as num_elements from av_viewpoint as v2, av_viewpoint_element as ve2, av_element as e2"
				+ "		where v2.id = ve2.viewpoint_id and ve2.element_id = e2.id and v2.id = v.id "
				+ "	) as total_element," + "	(cast(count(distinct e.name) as real)*100) / ("
				+ "		select count(e2.name) as num_elements from av_viewpoint as v2, av_viewpoint_element as ve2, av_element as e2"
				+ "		where v2.id = ve2.viewpoint_id and ve2.element_id = e2.id and v2.id = v.id "
				+ "	) as percentage"
				+ "	from av_viewpoint as v, av_viewpoint_element as ve, av_element as e, av_mining_point as m, av_input_artifact as a, av_technique as t"
				+ "	where v.id = ve.viewpoint_id and ve.element_id = e.id and m.element_id = e.id and m.technique_id = t.id and m.input_id = a.id and a.id in (:artefactsIds) "
				+ "	group by v.id, viewpoint, technique" + "	order by viewpoint, percentage desc";

		try {
			Query query = entityManager.createNativeQuery(stringQuery);

			List<Long> artefactsIds = artefacts.stream().map(InputArtifact::getId).collect(Collectors.toList());

			query.setParameter("artefactsIds", artefactsIds);

			List<Object[]> resultSet = query.getResultList();

			for (Object[] tuple : resultSet) {
				QueriedViewpointDTO viewpoint = new QueriedViewpointDTO();
				viewpoint.setId(((BigInteger) tuple[0]).longValue());
				viewpoint.setName((String) tuple[1]);
				viewpoint.setMaxNumElementsAutomatic(Integer.parseInt(tuple[3].toString()));
				viewpoint.setTotalElementsAutomatic(Integer.parseInt(tuple[4].toString()));
				viewpoint.setMaxPercentageElementsAutomatic(Double.parseDouble(tuple[5].toString()));
				resultList.add(viewpoint);
			}

		} catch (SQLGrammarException e) {
			LOGGER.error(e.getMessage());
		}

		return resultList;
	}

	@SuppressWarnings("unchecked")
	public List<QueriedViewpointDTO> listViewpointsMaxPercentageByArtefacts(final List<InputArtifact> artefacts) {
		List<QueriedViewpointDTO> resultList = new ArrayList<>();

		if (artefacts == null || artefacts.isEmpty()) {
			return resultList;
		}

		String stringQuery = "select v.id, v.name as viewpoint, count(distinct e.name) as num_element, (cast(count(distinct e.name) as real)*100) / ("
				+ "		select count(e2.name) as num_elements from av_viewpoint as v2, av_viewpoint_element as ve2, av_element as e2"
				+ "		where v2.id = ve2.viewpoint_id and ve2.element_id = e2.id and v2.id = v.id "
				+ "	) as percentage "
				+ "	from av_viewpoint as v, av_viewpoint_element as ve, av_element as e, av_mining_point as m, av_input_artifact as a, av_technique as t "
				+ "	where v.id = ve.viewpoint_id and ve.element_id = e.id and m.element_id = e.id and m.input_id = a.id and a.id in (:artefactsIds) and m.technique_id=t.id "
				+ "	group by v.id, viewpoint" + "	order by percentage desc";

		try {
			Query query = entityManager.createNativeQuery(stringQuery);

			List<Long> artefactsIds = artefacts.stream().map(InputArtifact::getId).collect(Collectors.toList());

			query.setParameter("artefactsIds", artefactsIds);

			List<Object[]> resultSet = query.getResultList();

			for (Object[] tuple : resultSet) {
				QueriedViewpointDTO viewpoint = new QueriedViewpointDTO();
				viewpoint.setId(((BigInteger) tuple[0]).longValue());
				viewpoint.setName((String) tuple[1]);
				viewpoint.setMaxNumElementsAutomatic(Integer.parseInt(tuple[2].toString()));
				viewpoint.setMaxPercentageElementsAutomatic(Double.parseDouble(tuple[3].toString()));
				resultList.add(viewpoint);
			}

		} catch (SQLGrammarException e) {
			LOGGER.error(e.getMessage());
		}

		return resultList;
	}

	@SuppressWarnings("unchecked")
	public QueriedViewpointDTO getViewpointPercentagesByArtefacts(final List<InputArtifact> artefacts,
			QueriedViewpointDTO viewpointDTO) {

		if (artefacts == null || artefacts.isEmpty()) {
			return viewpointDTO;
		}

		String stringQuery = "select distinct t.id, t.name as technique, e.name as element"
				+ "	from av_viewpoint as v, av_viewpoint_element as ve, av_element as e, av_mining_point as m, av_input_artifact as a, av_technique as t "
				+ "	where v.id = ve.viewpoint_id and ve.element_id = e.id and m.element_id = e.id and m.input_id = a.id and a.id in (:artefactsIds) and m.technique_id=t.id and v.id=:viewpointId "
				+ "	order by t.id";

		String stringQueryTotalElements = "select count(e.name) as num_elements from av_viewpoint as v, av_viewpoint_element as ve, av_element as e "
				+ "		where v.id = ve.viewpoint_id and ve.element_id = e.id and v.id = :viewpointId";

		try {
			Query queryTotal = entityManager.createNativeQuery(stringQueryTotalElements);
			queryTotal.setParameter("viewpointId", viewpointDTO.getId());
			viewpointDTO.setTotalElementsAutomatic(Integer.parseInt(queryTotal.getSingleResult().toString()));

			Query query = entityManager.createNativeQuery(stringQuery);

			List<Long> artefactsIds = artefacts.stream().map(InputArtifact::getId).collect(Collectors.toList());
			query.setParameter("artefactsIds", artefactsIds);
			query.setParameter("viewpointId", viewpointDTO.getId());

			List<Object[]> resultSet = query.getResultList();

			viewpointDTO.setElementsByTechnique(new HashMap<String, Set<String>>());

			for (Object[] tuple : resultSet) {
				viewpointDTO.addElementByTechnique((String) tuple[1], (String) tuple[2]);
			}

		} catch (SQLGrammarException e) {
			LOGGER.error(e.getMessage());
		}

		return viewpointDTO;
	}
	
	
	
	@SuppressWarnings("unchecked")
	public List<QueriedViewpointDTO> listViewpointsMaxPercentageByStakeholder(final List<Stakeholder> stakeholders) {
		List<QueriedViewpointDTO> resultList = new ArrayList<>();

		if (stakeholders == null || stakeholders.isEmpty()) {
			return resultList;
		}

		String stringQuery = "select v.id, v.name as viewpoint, count(distinct e.name), (cast(count(distinct e.name) as real)*100) / ( " + 
				"	select count(e2.name) as num_elements from av_viewpoint as v2, av_viewpoint_element as ve2, av_element as e2 " + 
				"	where v2.id = ve2.viewpoint_id and ve2.element_id = e2.id and v2.id = v.id " + 
				" ) as percentage " + 
				" from av_viewpoint as v, av_viewpoint_element as ve, av_element as e, " + 
				"		av_stakeholder_element se, av_stakeholder s  " + 
				" where (v.id = ve.viewpoint_id and ve.element_id = e.id and e.id = se.element_id and se.stakeholder_id = s.id and s.id in (:stakeholderIds)) " + 
				" group by v.id, viewpoint order by percentage desc";

		try {
			Query query = entityManager.createNativeQuery(stringQuery);

			List<Long> stakeholderIds = stakeholders.stream().map(Stakeholder::getId).collect(Collectors.toList());

			query.setParameter("stakeholderIds", stakeholderIds);

			List<Object[]> resultSet = query.getResultList();

			for (Object[] tuple : resultSet) {
				QueriedViewpointDTO viewpoint = new QueriedViewpointDTO();
				viewpoint.setId(((BigInteger) tuple[0]).longValue());
				viewpoint.setName((String) tuple[1]);
				viewpoint.setMaxNumElementsManual(Integer.parseInt(tuple[2].toString()));
				viewpoint.setMaxPercentageElementsManual(Double.parseDouble(tuple[3].toString()));
				resultList.add(viewpoint);
			}

		} catch (SQLGrammarException e) {
			LOGGER.error(e.getMessage());
		}

		return resultList;
	}

	
	
	@SuppressWarnings("unchecked")
	public QueriedViewpointDTO getViewpointPercentagesByStakeholders(final List<Stakeholder> stakeholder,
			QueriedViewpointDTO viewpointDTO) {

		if (stakeholder == null || stakeholder.isEmpty()) {
			return viewpointDTO;
		}

		String stringQuery = "select distinct s.id, s.name as stakeholder, e.name as element " + 
				"	from av_viewpoint as v, av_viewpoint_element as ve, av_element as e, av_stakeholder_element se, av_stakeholder s " + 
				"	where (v.id = ve.viewpoint_id and ve.element_id = e.id and e.id = se.element_id and se.stakeholder_id = s.id and s.id in (:stakeholderIds)) and v.id=:viewpointId " + 
				"	order by s.id";

		String stringQueryTotalElements = "select count(e.name) as num_elements from av_viewpoint as v, av_viewpoint_element as ve, av_element as e "
				+ "		where v.id = ve.viewpoint_id and ve.element_id = e.id and v.id = :viewpointId";

		try {
			Query queryTotal = entityManager.createNativeQuery(stringQueryTotalElements);
			queryTotal.setParameter("viewpointId", viewpointDTO.getId());
			viewpointDTO.setTotalElementsManual(Integer.parseInt(queryTotal.getSingleResult().toString()));

			Query query = entityManager.createNativeQuery(stringQuery);

			List<Long> stakeholderIds = stakeholder.stream().map(Stakeholder::getId).collect(Collectors.toList());
			query.setParameter("stakeholderIds", stakeholderIds);
			query.setParameter("viewpointId", viewpointDTO.getId());

			List<Object[]> resultSet = query.getResultList();

			viewpointDTO.setElementsByTechnique(new HashMap<String, Set<String>>());

			for (Object[] tuple : resultSet) {
				viewpointDTO.addElementByStakeholder((String) tuple[1], (String) tuple[2]);
			}

		} catch (SQLGrammarException e) {
			LOGGER.error(e.getMessage());
		}

		return viewpointDTO;
	}
	
	
}