package es.alarcos.archirev.logic;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.io.StringReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.jdom2.Document;
import org.jdom2.Namespace;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import com.archimatetool.model.impl.AccessRelationship;
import com.archimatetool.model.impl.AggregationRelationship;
import com.archimatetool.model.impl.ArchimateElement;
import com.archimatetool.model.impl.ArchimateFactory;
import com.archimatetool.model.impl.ArchimateRelationship;
import com.archimatetool.model.impl.CompositionRelationship;
import com.archimatetool.model.impl.RealizationRelationship;
import com.archimatetool.model.impl.ServingRelationship;
import com.archimatetool.model.impl.SpecializationRelationship;
import com.archimatetool.model.impl.TriggeringRelationship;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;

import es.alarcos.archirev.model.Source;

public abstract class AbstractSourceCodeParser implements Serializable {

	private static final long serialVersionUID = -714549315038788426L;

	private static Logger logger = LoggerFactory.getLogger(AbstractSourceCodeParser.class);

	protected static final String MAPPED_SUPERCLASS_ANNOTATION = "MappedSuperclass";

	private static final String JSON_NAME = "name";
	private static final String JSON_MAPPING = "mapping";
	private static final String JSON_PAIRS = "pairs";
	private static final String JSON_ANNOTATION = "annotation";
	private static final String JSON_ELEMENT = "element";
	private static final String JSON_EXCLUSION = "exclusions";
	private static final String JSON_EXCLUSION_TAGS = "exclusion_tags";
	private static final String JSON_TAG = "tag";
	private static final String JSON_PRIORITIZATION = "prioritization";
	private static final String JSON_TUPLES = "tuples";
	private static final String JSON_TARGETS = "targets";
	private static final String JSON_FROM = "from";
	private static final String JSON_TO = "to";
	private static final String JSON_RELATIONSHIP = "relationship";
	private static final String SETUP_CLASS_ROOT = "com.archimatetool.model.impl.";
	
	
	protected static final String NS_KDM = "http://kdm.omg.org/kdm";
	protected static final String NS_XMI = "http://www.omg.org/XMI";
	protected static final String NS_XSI = "http://www.w3.org/2001/XMLSchema-instance";
	protected static final String NS_ACTION = "http://kdm.omg.org/action";
	protected static final String NS_CODE = "http://kdm.omg.org/code";

	protected transient MultiValueMap<String, ArchimateElementEnum> mapping = new LinkedMultiValueMap<>();
	protected transient Set<String> exclusions = new HashSet<>();
	protected transient Map<Class<? extends ArchimateElement>, Map<Class<? extends ArchimateElement>, Class<? extends ArchimateRelationship>>> mapPrioritizedRelationship = new HashMap<>();

	public AbstractSourceCodeParser(final String setup) {
		loadSetup(setup);
	}

	public abstract MultiValueMap<String, ArchimateElement> computeModelElementsByClassName(Source warSource)
			throws ZipException, IOException;

	public abstract MultiValueMap<String, ArchimateRelationship> computeModelRelationshipsByClassName(Source warSource,
			MultiValueMap<String, ArchimateElement> modelElementsByClassName) throws ZipException, IOException;
	
	public abstract Document generateKdmCodeElements(Source zipSource, Document document) throws ZipException, IOException;
	
	public abstract Document generateKdmRelationships(Source zipSource, Document kdmDocument) throws ZipException, IOException;
	

	protected ZipFile getZipFile(final File file) throws ZipException, IOException {
		ZipFile zipFile = new ZipFile(file);
		return zipFile;
	}

	protected String getFormattedName(String className) {
		String simpleClassName = className.substring(className.lastIndexOf(".") + 1);
		simpleClassName = StringUtils.join(StringUtils.splitByCharacterTypeCamelCase(simpleClassName), " ");
		return simpleClassName;
	}

	protected ArchimateRelationship getPrioritizedRelationship(ArchimateElement source, ArchimateElement target) {
		ArchimateRelationship DEFAULT = (ArchimateRelationship) ArchimateFactory.eINSTANCE.createAccessRelationship();
		Map<Class<? extends ArchimateElement>, Class<? extends ArchimateRelationship>> relationshipSourceClass = mapPrioritizedRelationship
				.get(source.getClass());
		if (relationshipSourceClass == null) {
			return DEFAULT;
		}
		Class<? extends ArchimateRelationship> relationshipClass = relationshipSourceClass.get(target.getClass());

		if (relationshipClass == null) {
			return DEFAULT;
		} else if (relationshipClass.equals(AccessRelationship.class)) {
			return DEFAULT;
		} else if (relationshipClass.equals(AggregationRelationship.class)) {
			return (ArchimateRelationship) ArchimateFactory.eINSTANCE.createAggregationRelationship();
		} else if (relationshipClass.equals(CompositionRelationship.class)) {
			return (ArchimateRelationship) ArchimateFactory.eINSTANCE.createCompositionRelationship();
		} else if (relationshipClass.equals(RealizationRelationship.class)) {
			return (ArchimateRelationship) ArchimateFactory.eINSTANCE.createRealizationRelationship();
		} else if (relationshipClass.equals(ServingRelationship.class)) {
			return (ArchimateRelationship) ArchimateFactory.eINSTANCE.createServingRelationship();
		} else if (relationshipClass.equals(SpecializationRelationship.class)) {
			return (ArchimateRelationship) ArchimateFactory.eINSTANCE.createSpecializationRelationship();
		} else if (relationshipClass.equals(TriggeringRelationship.class)) {
			return (ArchimateRelationship) ArchimateFactory.eINSTANCE.createTriggeringRelationship();
		} else {
			return DEFAULT;
		}
	}

	@SuppressWarnings("unchecked")
	private void loadSetup(final String setup) {
		JsonParser parser = new JsonParser();

		JsonReader jsonReader = new JsonReader(new StringReader(setup));
		jsonReader.setLenient(true);

		try {
			JsonElement root = parser.parse(jsonReader);
			Validate.isTrue(root.isJsonArray());
			JsonArray objects = root.getAsJsonArray();
			Validate.isTrue(objects.get(0).isJsonObject());

			// load mapping object
			JsonObject mappingObject = objects.get(0).getAsJsonObject();
			Validate.isTrue(JSON_MAPPING.equals(mappingObject.get(JSON_NAME).getAsString()));
			Validate.isTrue(mappingObject.get(JSON_PAIRS).isJsonArray());
			JsonArray mappingPairs = mappingObject.get(JSON_PAIRS).getAsJsonArray();
			mapping = new LinkedMultiValueMap<>();
			for (int i = 0; i < mappingPairs.size(); i++) {
				Validate.isTrue(mappingPairs.get(i).isJsonObject());
				JsonObject pair = mappingPairs.get(i).getAsJsonObject();
				String annotation = pair.get(JSON_ANNOTATION).getAsString();
				String element = pair.get(JSON_ELEMENT).getAsString();
				mapping.add(annotation, ArchimateElementEnum.valueOf(element));
			}

			// load exclusions
			JsonObject exclusionsObject = objects.get(1).getAsJsonObject();
			Validate.isTrue(JSON_EXCLUSION.equals(exclusionsObject.get(JSON_NAME).getAsString()));
			Validate.isTrue(exclusionsObject.get(JSON_EXCLUSION_TAGS).isJsonArray());
			JsonArray exclusionsTags = exclusionsObject.get(JSON_EXCLUSION_TAGS).getAsJsonArray();
			exclusions = new HashSet<>();
			for (int i = 0; i < exclusionsTags.size(); i++) {
				Validate.isTrue(exclusionsTags.get(i).isJsonObject());
				JsonObject tag = exclusionsTags.get(i).getAsJsonObject();
				String exclusion_tag = tag.get(JSON_TAG).getAsString();
				exclusions.add(exclusion_tag);
			}

			// load prioritization object
			JsonObject prioritzationObject = objects.get(2).getAsJsonObject();
			Validate.isTrue(JSON_PRIORITIZATION.equals(prioritzationObject.get(JSON_NAME).getAsString()));
			Validate.isTrue(prioritzationObject.get(JSON_TUPLES).isJsonArray());
			JsonArray prioritizationTuples = prioritzationObject.get(JSON_TUPLES).getAsJsonArray();
			mapPrioritizedRelationship = new HashMap<>();

			for (int i = 0; i < prioritizationTuples.size(); i++) {
				Validate.isTrue(prioritizationTuples.get(i).isJsonObject());
				JsonObject tuple = prioritizationTuples.get(i).getAsJsonObject();
				String from = tuple.get(JSON_FROM).getAsString();

				Validate.isTrue(tuple.get(JSON_TARGETS).isJsonArray());
				JsonArray targets = tuple.get(JSON_TARGETS).getAsJsonArray();

				try {
					Map<Class<? extends ArchimateElement>, Class<? extends ArchimateRelationship>> tupleMap = new HashMap<>();
					for (int j = 0; j < targets.size(); j++) {
						Validate.isTrue(targets.get(j).isJsonObject());
						JsonObject target = targets.get(j).getAsJsonObject();
						String to = target.get(JSON_TO).getAsString();
						String relationship = target.get(JSON_RELATIONSHIP).getAsString();
						tupleMap.put((Class<? extends ArchimateElement>) Class.forName(SETUP_CLASS_ROOT + to),
								(Class<? extends ArchimateRelationship>) Class
										.forName(SETUP_CLASS_ROOT + relationship));
					}
					mapPrioritizedRelationship
							.put((Class<? extends ArchimateElement>) Class.forName(SETUP_CLASS_ROOT + from), tupleMap);
				} catch (ClassNotFoundException e) {
					logger.warn("Extraction setup is not valid" + e.getMessage());
				}
			}
		} catch (Exception ex) {
			logger.error("Extraction setup is not valid" + ex.getMessage());
		}
	}

	protected void createModelElements(MultiValueMap<String, ArchimateElement> modelElementsByClassName,
			String className, Set<ArchimateElementEnum> uniqueElements, boolean mappedSuperclass) {
		String formattedName = getFormattedName(className);
		for (ArchimateElementEnum archimateElementEnum : uniqueElements) {
			ArchimateElement elementToBeAdded = null;
			switch (archimateElementEnum) {
			case APPLICATION:
				elementToBeAdded = (ArchimateElement) ArchimateFactory.eINSTANCE.createApplicationFunction();
				break;
			case SERVICE:
				elementToBeAdded = (ArchimateElement) ArchimateFactory.eINSTANCE.createApplicationService();
				break;
			case DATA_ENTITY:
				elementToBeAdded = (ArchimateElement) ArchimateFactory.eINSTANCE.createDataObject();
				if (mappedSuperclass) {
					elementToBeAdded.setDocumentation(MAPPED_SUPERCLASS_ANNOTATION);
				}
				break;
			case COMPONENT:
				elementToBeAdded = (ArchimateElement) ArchimateFactory.eINSTANCE.createApplicationComponent();
				break;
			default:
				break;
			}
			elementToBeAdded.setName(formattedName);
			elementToBeAdded.setId("id-" + UUID.randomUUID());
			boolean existent = false;
			List<ArchimateElement> archimateElements = modelElementsByClassName.get(className);
			for (int i = 0; archimateElements != null && i < archimateElements.size(); i++) {
				if (archimateElements.get(i).getClass().equals(elementToBeAdded.getClass())) {
					existent = true;
					break;
				}

			}
			if (!existent) {
				modelElementsByClassName.add(className, elementToBeAdded);
			}
		}
	}

	protected void createModelRelationships(MultiValueMap<String, ArchimateRelationship> modelRelationshipsByClassName,
			Set<String> visitedRelationships, String compilationUnitName, List<ArchimateElement> sourceElements,
			List<ArchimateElement> targetElements) {
		if (sourceElements == null || targetElements == null) {
			return;
		}
		for (ArchimateElement source : sourceElements) {
			for (ArchimateElement target : targetElements) {
				if (!source.equals(target)) {
					ArchimateElement copySource = source;
					ArchimateElement copyTarget = target;

					ArchimateRelationship relationshipToBeAdded = getPrioritizedRelationship(source, target);

					if (relationshipToBeAdded instanceof CompositionRelationship
							&& MAPPED_SUPERCLASS_ANNOTATION.equals(target.getDocumentation())) {
						copySource = target;
						copyTarget = source;
						relationshipToBeAdded = (ArchimateRelationship) ArchimateFactory.eINSTANCE
								.createSpecializationRelationship();
					}

					relationshipToBeAdded.setSource(copySource);
					relationshipToBeAdded.setTarget(copyTarget);
					relationshipToBeAdded.setName(copySource.getName() + "-to-" + copyTarget.getName());
//					String relationshipId = copySource.getClass().getSimpleName() + "[\"" + copySource.getName() + "\"]"
//							+ " --(" + relationshipToBeAdded.getClass().getSimpleName() + ")--> "
//							+ copyTarget.getClass().getSimpleName() + "[\"" + copyTarget.getName() + "\"]";
					String relationshipId = "id-" + UUID.randomUUID();
					relationshipToBeAdded.setId(relationshipId);

					if (visitedRelationships.contains(relationshipId)) {
						continue;
					}
					modelRelationshipsByClassName.add(compilationUnitName, relationshipToBeAdded);
					visitedRelationships.add(relationshipId);
				}
			}
		}
	}
	

	public Set<String> getExclusions() {
		return exclusions;
	}

	public void setExclusions(Set<String> exclusions) {
		this.exclusions = exclusions;
	}

	public Map<Class<? extends ArchimateElement>, Map<Class<? extends ArchimateElement>, Class<? extends ArchimateRelationship>>> getMapPrioritizedRelationship() {
		return mapPrioritizedRelationship;
	}

	public void setMapPrioritizedRelationship(
			Map<Class<? extends ArchimateElement>, Map<Class<? extends ArchimateElement>, Class<? extends ArchimateRelationship>>> mapPrioritizedRelationship) {
		this.mapPrioritizedRelationship = mapPrioritizedRelationship;
	}

	public MultiValueMap<String, ArchimateElementEnum> getMapping() {
		return mapping;
	}

	public void setMapping(MultiValueMap<String, ArchimateElementEnum> mapping) {
		this.mapping = mapping;
	}
	
	protected String addXmiIdentifier(org.jdom2.Element element) {
		Namespace nsXmi = Namespace.getNamespace("xmi", NS_XMI);
		String id = "id." + UUID.randomUUID();
		element.setAttribute("id", id, nsXmi);
		return id;
	}


}
