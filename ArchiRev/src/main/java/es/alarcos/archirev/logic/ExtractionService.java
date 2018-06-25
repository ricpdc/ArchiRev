package es.alarcos.archirev.logic;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import javax.imageio.ImageIO;
import javax.inject.Singleton;
import javax.xml.XMLConstants;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.apache.bcel.classfile.AnnotationEntry;
import org.apache.bcel.classfile.ClassFormatException;
import org.apache.bcel.classfile.ClassParser;
import org.apache.bcel.classfile.Constant;
import org.apache.bcel.classfile.JavaClass;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.jdom2.Attribute;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.xml.sax.SAXException;

import com.archimatetool.model.impl.AccessRelationship;
import com.archimatetool.model.impl.AggregationRelationship;
import com.archimatetool.model.impl.ApplicationComponent;
import com.archimatetool.model.impl.ApplicationFunction;
import com.archimatetool.model.impl.ApplicationService;
import com.archimatetool.model.impl.ArchimateElement;
import com.archimatetool.model.impl.ArchimateFactory;
import com.archimatetool.model.impl.ArchimateRelationship;
import com.archimatetool.model.impl.CompositionRelationship;
import com.archimatetool.model.impl.DataObject;
import com.archimatetool.model.impl.RealizationRelationship;
import com.archimatetool.model.impl.ServingRelationship;
import com.archimatetool.model.impl.SpecializationRelationship;
import com.archimatetool.model.impl.TriggeringRelationship;
import com.mxgraph.canvas.mxGraphics2DCanvas;
import com.mxgraph.layout.mxGraphLayout;
import com.mxgraph.model.mxCell;
import com.mxgraph.util.mxCellRenderer;
import com.mxgraph.util.mxConstants;
import com.mxgraph.view.mxGraph;

import es.alarcos.archirev.logic.connector.ConnectorEnum;
import es.alarcos.archirev.logic.layout.ExtendedHierarchicalLayout;
import es.alarcos.archirev.logic.shape.ShapeEnum;
import es.alarcos.archirev.model.Model;
import es.alarcos.archirev.model.Source;

@Singleton
@Service
public class ExtractionService implements Serializable {

	private static final long serialVersionUID = -4392305100176250199L;

	static Logger LOGGER = LoggerFactory.getLogger(ExtractionService.class);

	private static final String MAPPED_SUPERCLASS_ANNOTATION = "MappedSuperclass";

	private static final String DEFAULT_LANG = "en";
	private static final String SCHEMA_LOCATION = "http://www.opengroup.org/xsd/archimate/3.0/ http://www.opengroup.org/xsd/archimate/3.0/archimate3_Diagram.xsd http://purl.org/dc/elements/1.1/ http://dublincore.org/schemas/xmls/qdc/2008/02/11/dc.xsd";

	private static final String NS_ARCHIMATE = "http://www.opengroup.org/xsd/archimate/3.0/";
	private static final String NS_ELEMENTS = "http://purl.org/dc/elements/1.1/";
	private static final String NS_XSI = "http://www.w3.org/2001/XMLSchema-instance";

	// TODO Load this from a configuration json file
	private static final MultiValueMap<String, ArchimateElementEnum> mapping = new LinkedMultiValueMap<>();
	static {
		mapping.add("ManagedBean", ArchimateElementEnum.APPLICATION);
		mapping.add("Controller", ArchimateElementEnum.APPLICATION);
		mapping.add("Component", ArchimateElementEnum.APPLICATION);
		mapping.add("Service", ArchimateElementEnum.APPLICATION);
		mapping.add("Entity", ArchimateElementEnum.DATA_ENTITY);
		mapping.add("Table", ArchimateElementEnum.DATA_ENTITY);
		mapping.add("MappedSuperclass", ArchimateElementEnum.DATA_ENTITY);
		mapping.add("Repository", ArchimateElementEnum.COMPONENT);
		mapping.add("SpringBootApplication", ArchimateElementEnum.COMPONENT);
	}

	// TODO Move this map to constants and allow parametrize this.
	private static final Map<Class<? extends ArchimateElement>, Map<Class<? extends ArchimateElement>, Class<? extends ArchimateRelationship>>> mapPrioritizedRelationship = new HashMap<>();
	static {
		Map<Class<? extends ArchimateElement>, Class<? extends ArchimateRelationship>> applicationFunctionMap = new HashMap<>();
		applicationFunctionMap.put(ApplicationFunction.class, TriggeringRelationship.class);
		applicationFunctionMap.put(ApplicationComponent.class, ServingRelationship.class);
		applicationFunctionMap.put(ApplicationService.class, RealizationRelationship.class);
		applicationFunctionMap.put(DataObject.class, AccessRelationship.class);
		mapPrioritizedRelationship.put(ApplicationFunction.class, applicationFunctionMap);

		Map<Class<? extends ArchimateElement>, Class<? extends ArchimateRelationship>> applicationComponentMap = new HashMap<>();
		applicationComponentMap.put(ApplicationFunction.class, TriggeringRelationship.class);
		applicationComponentMap.put(ApplicationComponent.class, ServingRelationship.class);
		applicationComponentMap.put(ApplicationService.class, RealizationRelationship.class);
		applicationComponentMap.put(DataObject.class, AccessRelationship.class);
		mapPrioritizedRelationship.put(ApplicationComponent.class, applicationComponentMap);

		Map<Class<? extends ArchimateElement>, Class<? extends ArchimateRelationship>> applicationServiceMap = new HashMap<>();
		applicationServiceMap.put(ApplicationFunction.class, AccessRelationship.class);
		applicationServiceMap.put(ApplicationComponent.class, AccessRelationship.class);
		applicationServiceMap.put(ApplicationService.class, TriggeringRelationship.class);
		applicationServiceMap.put(DataObject.class, AccessRelationship.class);
		mapPrioritizedRelationship.put(ApplicationService.class, applicationServiceMap);

		Map<Class<? extends ArchimateElement>, Class<? extends ArchimateRelationship>> dataObjectMap = new HashMap<>();
		dataObjectMap.put(ApplicationFunction.class, AccessRelationship.class);
		dataObjectMap.put(ApplicationComponent.class, AccessRelationship.class);
		dataObjectMap.put(ApplicationService.class, AccessRelationship.class);
		dataObjectMap.put(DataObject.class, CompositionRelationship.class);
	}

	public ExtractionService() {

	}

	public void extractArchimateModel(Model model) {
		Set<Source> sources = model.getExtraction().getSources();
		Validate.isTrue(sources != null && !sources.isEmpty(), "There is no source as input");
		// TODO Integrate different sources into single one model
		File imageFile = null;
		String modelName = model.getExtraction().getName();
		for (Source source : sources) {
			modelName += ("_" + source.getName());
			switch (source.getType()) {
			case WEB_APP:
				imageFile = extractArchimateModelForWebApp(model, source, false);
				break;
			case JPA:
				break;
			default:
			}
		}
		model.setName(modelName);
		if (imageFile != null) {
			model.setImagePath(imageFile.getAbsolutePath());
			model.setExportedPath(null);
		}
	}

	public void exportArchimateModel(Model model) {
		Set<Source> sources = model.getExtraction().getSources();
		Validate.isTrue(sources != null && !sources.isEmpty(), "There is no source as input");
		// TODO Integrate different sources into single one model
		File exportXmlFile = null;
		for (Source source : sources) {
			switch (source.getType()) {
			case WEB_APP:
				exportXmlFile = extractArchimateModelForWebApp(model, source, true);
				break;
			case JPA:
				break;
			default:
			}
		}
		if (exportXmlFile != null) {
			model.setExportedPath(exportXmlFile.getAbsolutePath());
		}
	}

	private File extractArchimateModelForWebApp(Model model, Source source, boolean export) {

		MultiValueMap<String, ArchimateElement> modelElementsByClassName = new LinkedMultiValueMap<>();
		MultiValueMap<String, ArchimateRelationship> modelRelationshipsByClassName = new LinkedMultiValueMap<>();
		try {
			modelElementsByClassName = computeModelElementsByClassName(source, mapping);
			modelRelationshipsByClassName = computeModelRelationshipsByClassName(source, mapping,
					modelElementsByClassName);
			if (!export) {
				return generateDiagram(model, modelElementsByClassName, modelRelationshipsByClassName);
			} else {
				return exportOpenExchangeFormat(model, modelElementsByClassName, modelRelationshipsByClassName);
			}
		} catch (NoClassDefFoundError | IOException e) {
			e.printStackTrace();
		}
		return null;

	}

	private MultiValueMap<String, ArchimateElement> computeModelElementsByClassName(Source warSource,
			MultiValueMap<String, ArchimateElementEnum> mapping) throws ZipException, IOException {
		MultiValueMap<String, ArchimateElement> modelElementsByClassName = new LinkedMultiValueMap<>();

		File tempWarFile = File.createTempFile("warFile", ".tmp", null);
		FileOutputStream fos = new FileOutputStream(tempWarFile);
		fos.write(warSource.getFile());
		fos.close();

		ZipFile zipFile = getZipFile(tempWarFile);
		ClassParser cp;

		@SuppressWarnings("unchecked")
		Enumeration<ZipEntry> entries = (Enumeration<ZipEntry>) zipFile.entries();
		while (entries.hasMoreElements()) {
			ZipEntry entry = entries.nextElement();
			if (entry.isDirectory())
				continue;

			if (!entry.getName().endsWith(".class"))
				continue;

			cp = new ClassParser(tempWarFile.getAbsolutePath(), entry.getName());
			JavaClass javaClass = null;
			try {
				javaClass = cp.parse();
			} catch (ClassFormatException | IOException e) {
				LOGGER.error(entry.getName() + " cannot be parsed");
				javaClass = null;
				continue;
			}

			Set<ArchimateElementEnum> uniqueElements = new HashSet<>();
			boolean mappedSuperclass = false;
			for (AnnotationEntry annotationEntry : javaClass.getAnnotationEntries()) {
				String annotationType = annotationEntry.getAnnotationType();
				// LOGGER.debug(String.format("[%s]: @%s", javaClass.getClassName(),
				// annotationType));
				String annotation = annotationType.substring(annotationType.lastIndexOf("/") + 1,
						annotationType.length() - 1);
				List<ArchimateElementEnum> elementList = mapping.get(annotation);
				if (elementList != null) {
					uniqueElements.addAll(elementList);
					if (!mappedSuperclass && MAPPED_SUPERCLASS_ANNOTATION.equals(annotation)) {
						mappedSuperclass = true;
					}
				}
			}

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
				String formattedName = getFormattedName(javaClass);
				elementToBeAdded.setName(formattedName);
				modelElementsByClassName.add(javaClass.getClassName(), elementToBeAdded);
			}
		}
		return modelElementsByClassName;
	}

	private File generateDiagram(Model model, MultiValueMap<String, ArchimateElement> modelElementsByClassName,
			MultiValueMap<String, ArchimateRelationship> modelRelationshipsByClassName) {

		mxGraph graph = new mxGraph();
		Object parent = graph.getDefaultParent();
		graph.getModel().beginUpdate();

		loadShapeStyles(graph);

		try {
			Map<ArchimateElement, Object> nodes = new HashMap<>();
			for (Entry<String, List<ArchimateElement>> entry : modelElementsByClassName.entrySet()) {
				// LOGGER.info("");
				// LOGGER.info(entry.getKey());

				Collections.sort(entry.getValue(), new Comparator<ArchimateElement>() {
					@Override
					public int compare(ArchimateElement o1, ArchimateElement o2) {
						return o1.getClass().getSimpleName().compareTo(o2.getClass().getSimpleName());
					}
				});

				parent = graph.getDefaultParent();

				List<ArchimateElement> componentElments = new ArrayList<>();
				for (ArchimateElement element : entry.getValue()) {
					if (element instanceof ApplicationComponent) {
						componentElments.add(element);
					}
				}

				ApplicationComponent component = componentElments.isEmpty() ? null
						: (ApplicationComponent) componentElments.get(0);
				Object componentNode = null;
				if (component != null) {
					ShapeEnum shapeEnum = ShapeEnum.getByModelElement(component.getClass());
					componentNode = graph.insertVertex(parent, null, component.getName(), 0, 0,
							component.getName().length() * 5 + 60 + 30, 40 + 35, shapeEnum.getShape().getSimpleName());
					nodes.put(component, componentNode);
					parent = componentNode;
				}

				for (ArchimateElement archimateElement : entry.getValue()) {
					ShapeEnum shapeEnum = ShapeEnum.getByModelElement(archimateElement.getClass());

					if (archimateElement instanceof ApplicationComponent) {
						continue;
					}
					Object node = graph.insertVertex(parent, null, archimateElement.getName(), 15, 20,
							archimateElement.getName().length() * 5 + 60, 40, shapeEnum.getShape().getSimpleName());

					nodes.put(archimateElement, componentNode != null ? componentNode : node);
					// nodes.put(archimateElement, node);

					// LOGGER.info("\t" + archimateElement.getClass().getSimpleName() + " (\"" +
					// archimateElement.getName()
					// + "\")");
				}

			}

			parent = graph.getDefaultParent();
			List<String> visitedEdges = new ArrayList<>();
			for (Entry<String, List<ArchimateRelationship>> entry : modelRelationshipsByClassName.entrySet()) {
				LOGGER.info("");
				LOGGER.info(entry.getKey());

				for (ArchimateRelationship archimateRelationship : entry.getValue()) {

					mxCell node1 = (mxCell) nodes.get(archimateRelationship.getSource());
					mxCell node2 = (mxCell) nodes.get(archimateRelationship.getTarget());
					String edgeId = node1.getValue() + "--" + archimateRelationship.getClass().getSimpleName() + "-->"
							+ node2.getValue();

					if (visitedEdges.contains(edgeId) || node1.equals(node2) || node1.getParent().equals(node2)
							|| node2.getParent().equals(node1)) {
						continue;
					}

					String simpleName = archimateRelationship.getClass().getSimpleName();
					graph.insertEdge(parent, null, simpleName, node1, node2,
							archimateRelationship.getClass().getSimpleName());

					visitedEdges.add(edgeId);

					LOGGER.info("\t" + archimateRelationship.getId());
				}
			}
		} finally {
			graph.getModel().endUpdate();
		}

		try {
			ExtendedHierarchicalLayout extendedHierarchicalLayout = new ExtendedHierarchicalLayout(graph, 75);

			mxGraphLayout layout = extendedHierarchicalLayout;
			layout.execute(graph.getDefaultParent());

			BufferedImage image = mxCellRenderer.createBufferedImage(graph, null, 1, java.awt.Color.WHITE, true, null);
			File file = new File(model.getImagePath());
			ImageIO.write(image, "PNG", file);

			// TODO remove this local test
			File localFile = new File("C:\\Users\\Alarcos\\git\\ArchiRev\\ArchiRev\\target\\diagrams\\testJgraphX.png");
			ImageIO.write(image, "PNG", localFile);

			return file;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	private File exportOpenExchangeFormat(Model model, MultiValueMap<String, ArchimateElement> modelElementsByClassName,
			MultiValueMap<String, ArchimateRelationship> modelRelationshipsByClassName) {
		mxGraph graph = new mxGraph();
		graph.getModel().beginUpdate();
		try {

			Element eModel = createXmlRootElement(model.getName());
			Document doc = new Document(eModel);

			Object parent = graph.getDefaultParent();
			loadShapeStyles(graph);

			Map<ArchimateElement, Object> nodes = new HashMap<>();
			for (Entry<String, List<ArchimateElement>> entry : modelElementsByClassName.entrySet()) {
				// LOGGER.info("");
				// LOGGER.info(entry.getKey());
				Collections.sort(entry.getValue(), new Comparator<ArchimateElement>() {
					@Override
					public int compare(ArchimateElement o1, ArchimateElement o2) {
						return o1.getClass().getSimpleName().compareTo(o2.getClass().getSimpleName());
					}
				});

				parent = graph.getDefaultParent();

				List<ArchimateElement> componentElments = new ArrayList<>();
				for (ArchimateElement element : entry.getValue()) {
					if (element instanceof ApplicationComponent) {
						componentElments.add(element);
					}
				}

				ApplicationComponent component = componentElments.isEmpty() ? null
						: (ApplicationComponent) componentElments.get(0);
				Object componentNode = null;
				if (component != null) {
					ShapeEnum shapeEnum = ShapeEnum.getByModelElement(component.getClass());
					componentNode = graph.insertVertex(parent, null, component.getName(), 0, 0,
							component.getName().length() * 5 + 60 + 30, 40 + 35, shapeEnum.getShape().getSimpleName());
					nodes.put(component, componentNode);
					parent = componentNode;
				}

				for (ArchimateElement archimateElement : entry.getValue()) {
					ShapeEnum shapeEnum = ShapeEnum.getByModelElement(archimateElement.getClass());

					if (archimateElement instanceof ApplicationComponent) {
						continue;
					}
					Object node = graph.insertVertex(parent, null, archimateElement.getName(), 15, 20,
							archimateElement.getName().length() * 5 + 60, 40, shapeEnum.getShape().getSimpleName());

					nodes.put(archimateElement, componentNode != null ? componentNode : node);

					// LOGGER.info("\t" + archimateElement.getClass().getSimpleName() + " (\"" +
					// archimateElement.getName()
					// + "\")");
				}

			}

			parent = graph.getDefaultParent();
			List<String> visitedEdges = new ArrayList<>();

			Map<ArchimateRelationship, Object> edges = new HashMap<>();
			for (Entry<String, List<ArchimateRelationship>> entry : modelRelationshipsByClassName.entrySet()) {
				LOGGER.info("");
				LOGGER.info(entry.getKey());

				for (ArchimateRelationship archimateRelationship : entry.getValue()) {

					mxCell node1 = (mxCell) nodes.get(archimateRelationship.getSource());
					mxCell node2 = (mxCell) nodes.get(archimateRelationship.getTarget());
					String edgeId = node1.getValue() + "--" + archimateRelationship.getClass().getSimpleName() + "-->"
							+ node2.getValue();

					if (visitedEdges.contains(edgeId) || node1.equals(node2) || node1.getParent().equals(node2)
							|| node2.getParent().equals(node1)) {
						continue;
					}

					String simpleName = archimateRelationship.getClass().getSimpleName();
					Object edge = graph.insertEdge(parent, null, simpleName, node1, node2,
							archimateRelationship.getClass().getSimpleName());

					visitedEdges.add(edgeId);
					edges.put(archimateRelationship, edge);

					LOGGER.info("\t" + archimateRelationship.getId());
				}
			}

			graph.getModel().endUpdate();

			 ExtendedHierarchicalLayout extendedHierarchicalLayout = new
			 ExtendedHierarchicalLayout(graph, 75);
			
			 mxGraphLayout layout = extendedHierarchicalLayout;
			 layout.execute(graph.getDefaultParent());
			
			// BufferedImage image = mxCellRenderer.createBufferedImage(graph, null, 1,
			// java.awt.Color.WHITE, true, null);
			// File file = new
			// File("C:\\Users\\Alarcos\\git\\ArchiRev\\ArchiRev\\target\\diagrams\\testJgraphX.png");
			// ImageIO.write(image, "PNG", file);

			Element eElements = createXmlElements(nodes);
			eModel.addContent(eElements);

			Element eRelationships = createXmlRelationships(edges);
			eModel.addContent(eRelationships);

			Element eViews = createXmlViews(nodes, edges, graph);
			eModel.addContent(eViews);

			XMLOutputter xmlOutput = new XMLOutputter();

			// display nice nice
			xmlOutput.setFormat(Format.getPrettyFormat());
			xmlOutput.output(doc, new FileWriter(model.getExportedPath()));

			// TODO remove this local test
			String xmlFileName = "C:\\Users\\Alarcos\\git\\ArchiRev\\ArchiRev\\target\\diagrams\\" + model.getName()
					+ ".xml";
			xmlOutput.output(doc, new FileWriter(xmlFileName));

			if (validateXmlFile(xmlFileName)) {
				return new File(model.getExportedPath());
			}
		} catch (IOException ex) {
			ex.printStackTrace();
		}
		return null;
	}

	private Element createXmlRootElement(final String name) {
		Namespace nsArchimate = Namespace.getNamespace(NS_ARCHIMATE);
		Namespace nsDc = Namespace.getNamespace("dc", NS_ELEMENTS);
		Namespace nsXsi = Namespace.getNamespace("xsi", NS_XSI);

		Element eModel = new Element("model", nsArchimate);
		eModel.addNamespaceDeclaration(nsDc);
		eModel.addNamespaceDeclaration(nsXsi);

		eModel.setAttribute("schemaLocation", SCHEMA_LOCATION, nsXsi);
		addIdentifier(eModel);

		Element eName = new Element("name", nsArchimate).addContent(name);
		addLang(eName);
		eModel.addContent(eName);

		Element eMetadata = new Element("metadata", nsArchimate);
		eMetadata.addContent(new Element("schema", nsArchimate).addContent("Dublin core"));
		eMetadata.addContent(new Element("schemaversion", nsArchimate).addContent("1.1"));
		// eMetadata.addContent(new Element("title", nsDc).addContent("Diagram - " +
		// name));
		// eMetadata.addContent(new Element("creator",
		// nsDc).addContent(System.getProperty("user.name")));
		eModel.addContent(eMetadata);

		return eModel;
	}

	private Element createXmlElements(Map<ArchimateElement, Object> nodes) {
		Namespace nsArchimate = Namespace.getNamespace(NS_ARCHIMATE);
		Namespace nsXsi = Namespace.getNamespace("xsi", NS_XSI);
		Element eElements = new Element("elements", nsArchimate);
		for (ArchimateElement archimateElement : nodes.keySet()) {
			Element eElement = new Element("element", nsArchimate);
			addIdentifier(eElement);
			archimateElement.setId(eElement.getAttributeValue("identifier"));
			eElement.setAttribute("type", archimateElement.getClass().getSimpleName(), nsXsi);
			Element eName = new Element("name", nsArchimate).addContent(archimateElement.getName());
			addLang(eName);
			eElement.addContent(eName);
			eElements.addContent(eElement);
		}
		return eElements;
	}

	private Element createXmlRelationships(Map<ArchimateRelationship, Object> relationships) {
		Namespace nsArchimate = Namespace.getNamespace(NS_ARCHIMATE);
		Namespace nsXsi = Namespace.getNamespace("xsi", NS_XSI);
		Element eRelationships = new Element("relationships", nsArchimate);

		for (ArchimateRelationship archimateRelationship : relationships.keySet()) {
			Element eRelationship = new Element("relationship", nsArchimate);
			addIdentifier(eRelationship);
			archimateRelationship.setId(eRelationship.getAttributeValue("identifier"));
			String simpleName = archimateRelationship.getClass().getSimpleName();
			simpleName = simpleName.substring(0, simpleName.lastIndexOf("Relationship"));
			eRelationship.setAttribute("type", simpleName, nsXsi);
			eRelationship.setAttribute("source", archimateRelationship.getSource().getId());
			eRelationship.setAttribute("target", archimateRelationship.getTarget().getId());
			Element eName = new Element("name", nsArchimate).addContent(archimateRelationship.getName());
			addLang(eName);
			eRelationship.addContent(eName);
			eRelationships.addContent(eRelationship);
		}
		return eRelationships;
	}

	private Element createXmlViews(Map<ArchimateElement, Object> nodes, Map<ArchimateRelationship, Object> edges,
			mxGraph graph) {
		Namespace nsArchimate = Namespace.getNamespace(NS_ARCHIMATE);
		Namespace nsXsi = Namespace.getNamespace("xsi", NS_XSI);

		Element eViews = new Element("views", nsArchimate);
		Element eDiagrams = new Element("diagrams", nsArchimate);
		Element eView = new Element("view", nsArchimate);
		addIdentifier(eView);
		eView.setAttribute("type", "Diagram", nsXsi);
		Element eName = new Element("name", nsArchimate).addContent("DefaultView");
		addLang(eName);
		eView.addContent(eName);
		eDiagrams.addContent(eView);
		eViews.addContent(eDiagrams);

		Map<ArchimateElement, String> uuidMap = new HashMap<>();

		for (Entry<ArchimateElement, Object> node : nodes.entrySet()) {
			Element eNode = new Element("node", nsArchimate);
			addIdentifier(eNode);
			uuidMap.put(node.getKey(), eNode.getAttributeValue("identifier"));
			eNode.setAttribute("elementRef", node.getKey().getId());
			eNode.setAttribute("type", "Element", nsXsi);
			mxCell cell = (mxCell) node.getValue();
			addGeometry(eNode, cell);

			Element eStyle = new Element("style", nsArchimate);
			eNode.addContent(eStyle);

			Map<String, Object> cellStyle = graph.getCellStyle(cell);
			Color fillColor = Color.decode("#" + cellStyle.get("fillColor"));
			Color lineColor = Color.decode("#" + cellStyle.get("strokeColor"));

			Element eFillColor = new Element("fillColor", nsArchimate)
					.setAttribute("r", String.valueOf(fillColor.getRed()))
					.setAttribute("g", String.valueOf(fillColor.getGreen()))
					.setAttribute("b", String.valueOf(fillColor.getBlue()));
			Element eLineColor = new Element("lineColor", nsArchimate)
					.setAttribute("r", String.valueOf(lineColor.getRed()))
					.setAttribute("g", String.valueOf(lineColor.getGreen()))
					.setAttribute("b", String.valueOf(lineColor.getBlue()));

			eStyle.addContent(eFillColor);
			eStyle.addContent(eLineColor);

			eView.addContent(eNode);
		}

		for (Entry<ArchimateRelationship, Object> edge : edges.entrySet()) {
			Element eConnection = new Element("connection", nsArchimate);
			addIdentifier(eConnection);
			eConnection.setAttribute("relationshipRef", edge.getKey().getId());
			eConnection.setAttribute("type", "Relationship", nsXsi);
			eConnection.setAttribute("source", uuidMap.get(edge.getKey().getSource()));
			eConnection.setAttribute("target", uuidMap.get(edge.getKey().getTarget()));
			mxCell cell = (mxCell) edge.getValue();

			Element eStyle = new Element("style", nsArchimate);
			eConnection.addContent(eStyle);

			Map<String, Object> cellStyle = graph.getCellStyle(cell);
			Color lineColor = Color.decode("#" + cellStyle.get("strokeColor"));

			Element eLineColor = new Element("lineColor", nsArchimate)
					.setAttribute("r", String.valueOf(lineColor.getRed()))
					.setAttribute("g", String.valueOf(lineColor.getGreen()))
					.setAttribute("b", String.valueOf(lineColor.getBlue()));

			eStyle.addContent(eLineColor);

			eView.addContent(eConnection);
		}

		return eViews;
	}

	private String addIdentifier(Element element) {
		String id = "id-" + UUID.randomUUID();
		element.setAttribute("identifier", id);
		return id;
	}

	private void addLang(Element element) {
		Namespace nsXml = Namespace.getNamespace("xml", "http://www.w3.org/XML/1998/namespace");
		element.setAttribute(new Attribute("lang", DEFAULT_LANG, nsXml));
	}

	private void addGeometry(Element eNode, mxCell cell) {
		eNode.setAttribute("x", String.valueOf((int) cell.getGeometry().getX()));
		eNode.setAttribute("y", String.valueOf((int) cell.getGeometry().getY()));
		eNode.setAttribute("w", String.valueOf((int) cell.getGeometry().getWidth()));
		eNode.setAttribute("h", String.valueOf((int) cell.getGeometry().getHeight()));
	}

	private boolean validateXmlFile(final String xmlFileName) throws MalformedURLException {
		URL schemaFile = new URL("http://www.opengroup.org/xsd/archimate/3.0/archimate3_Model.xsd");
		StreamSource xmlFile = new StreamSource(new File(xmlFileName));
		SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
		try {
			Schema schema = schemaFactory.newSchema(schemaFile);
			Validator validator = schema.newValidator();
			validator.validate(xmlFile);
			LOGGER.debug(xmlFile.getSystemId() + " is valid");
			return true;
		} catch (SAXException e) {
			LOGGER.error(xmlFile.getSystemId() + " is NOT valid reason:" + e);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}

	private ZipFile getZipFile(final File file) throws ZipException, IOException {
		ZipFile zipFile = new ZipFile(file);
		return zipFile;
	}

	private String getFormattedName(JavaClass javaClass) {
		String simpleClassName = javaClass.getClassName().substring(javaClass.getClassName().lastIndexOf(".") + 1);
		simpleClassName = StringUtils.join(StringUtils.splitByCharacterTypeCamelCase(simpleClassName), " ");
		return simpleClassName;
	}

	private void loadShapeStyles(mxGraph graph) {
		for (ShapeEnum shapeEnum : ShapeEnum.values()) {
			String shapeName = shapeEnum.getShape().getSimpleName();
			mxGraphics2DCanvas.putShape(shapeName, shapeEnum.getShapeInstance());
			Map<String, Object> style = new HashMap<String, Object>();
			style.put(mxConstants.STYLE_SHAPE, shapeName);
			style.put(mxConstants.STYLE_FILLCOLOR, shapeEnum.getFillColor());
			style.put(mxConstants.STYLE_STROKECOLOR, shapeEnum.getStrokeColor());
			style.put(mxConstants.STYLE_FONTCOLOR, shapeEnum.getFontColor());
			style.put(mxConstants.STYLE_ROUNDED, shapeEnum.getRounded());
			style.put(mxConstants.STYLE_VERTICAL_ALIGN, shapeEnum.getVerticalAlign());
			graph.getStylesheet().putCellStyle(shapeName, style);
		}
		for (ConnectorEnum connectorEnum : ConnectorEnum.values()) {
			String connectorName = connectorEnum.getModelRelationship().getSimpleName();
			mxGraphics2DCanvas.putShape(connectorName, connectorEnum.getConnectorShapeInstance());
			Map<String, Object> style = new HashMap<String, Object>();
			style.put(mxConstants.STYLE_SHAPE, mxConstants.SHAPE_CONNECTOR);

			style.put(mxConstants.STYLE_ROUNDED, true);
			style.put(mxConstants.STYLE_NOLABEL, "1");

			style.put(mxConstants.STYLE_STROKECOLOR, connectorEnum.getStrokeColor());
			style.put(mxConstants.STYLE_STARTARROW, connectorEnum.getStartArrow());
			style.put(mxConstants.STYLE_ENDARROW, connectorEnum.getEndArrow());
			style.put(mxConstants.STYLE_STARTFILL, connectorEnum.getStartFill());
			style.put(mxConstants.STYLE_ENDFILL, connectorEnum.getEndFill());
			style.put(mxConstants.STYLE_STARTSIZE, connectorEnum.getStartSize());
			style.put(mxConstants.STYLE_ENDSIZE, connectorEnum.getEndSize());
			style.put(mxConstants.STYLE_DASHED, connectorEnum.getDashed());
			graph.getStylesheet().putCellStyle(connectorName, style);

		}
	}

	private MultiValueMap<String, ArchimateRelationship> computeModelRelationshipsByClassName(Source warSource,
			MultiValueMap<String, ArchimateElementEnum> mapping,
			MultiValueMap<String, ArchimateElement> modelElementsByClassName) throws ZipException, IOException {
		MultiValueMap<String, ArchimateRelationship> modelRelationshipsByClassName = new LinkedMultiValueMap<>();

		File tempWarFile = File.createTempFile("warFile", ".tmp", null);
		FileOutputStream fos = new FileOutputStream(tempWarFile);
		fos.write(warSource.getFile());
		fos.close();

		ZipFile zipFile = getZipFile(tempWarFile);
		ClassParser cp;

		Set<String> visitedRelationships = new HashSet<>();

		@SuppressWarnings("unchecked")
		Enumeration<ZipEntry> entries = (Enumeration<ZipEntry>) zipFile.entries();
		while (entries.hasMoreElements()) {
			ZipEntry entry = entries.nextElement();
			if (entry.isDirectory())
				continue;

			if (!entry.getName().endsWith(".class"))
				continue;

			cp = new ClassParser(tempWarFile.getAbsolutePath(), entry.getName());
			JavaClass javaClass = null;
			try {
				javaClass = cp.parse();
			} catch (ClassFormatException | IOException e) {
				LOGGER.error(entry.getName() + " cannot be parsed");
				javaClass = null;
				continue;
			}

			if (modelElementsByClassName.containsKey(javaClass.getClassName())) {

				for (int i = 0; i < javaClass.getConstantPool().getLength(); i++) {
					Constant constant = javaClass.getConstantPool().getConstant(i);
					if (constant == null) {
						continue;
					}
					if (constant.getTag() == 7) {
						String referencedClass = javaClass.getConstantPool().constantToString(constant);
						if (modelElementsByClassName.containsKey(referencedClass)) {
							// LOGGER.debug(String.format("[%s] --> %s", javaClass.getClassName(),
							// referencedClass));

							List<ArchimateElement> sourceElements = modelElementsByClassName
									.get(javaClass.getClassName());
							List<ArchimateElement> targetElemetns = modelElementsByClassName.get(referencedClass);

							for (ArchimateElement source : sourceElements) {
								for (ArchimateElement target : targetElemetns) {
									if (!source.equals(target)) {
										ArchimateElement copySource = source;
										ArchimateElement copyTarget = target;

										ArchimateRelationship relationshipToBeAdded = getPrioritizedRelationship(source,
												target);

										if (relationshipToBeAdded instanceof CompositionRelationship
												&& MAPPED_SUPERCLASS_ANNOTATION.equals(target.getDocumentation())) {
											copySource = target;
											copyTarget = source;
											relationshipToBeAdded = (ArchimateRelationship) ArchimateFactory.eINSTANCE
													.createSpecializationRelationship();
										}

										relationshipToBeAdded.setSource(copySource);
										relationshipToBeAdded.setTarget(copyTarget);
										relationshipToBeAdded
												.setName(copySource.getName() + "-to-" + copyTarget.getName());
										String relationshipId = copySource.getClass().getSimpleName() + "[\""
												+ copySource.getName() + "\"]" + " --("
												+ relationshipToBeAdded.getClass().getSimpleName() + ")--> "
												+ copyTarget.getClass().getSimpleName() + "[\"" + copyTarget.getName()
												+ "\"]";
										relationshipToBeAdded.setId(relationshipId);

										if (visitedRelationships.contains(relationshipId)) {
											continue;
										}
										modelRelationshipsByClassName.add(javaClass.getClassName(),
												relationshipToBeAdded);
										visitedRelationships.add(relationshipId);
									}
								}
							}
						}

					}
				}
			}
		}
		return modelRelationshipsByClassName;
	}

	private ArchimateRelationship getPrioritizedRelationship(ArchimateElement source, ArchimateElement target) {
		ArchimateRelationship DEFAULT = (ArchimateRelationship) ArchimateFactory.eINSTANCE.createAccessRelationship();
		Map<Class<? extends ArchimateElement>, Class<? extends ArchimateRelationship>> relationshipSoruceClass = mapPrioritizedRelationship
				.get(source.getClass());
		if (relationshipSoruceClass == null) {
			return DEFAULT;
		}
		Class<? extends ArchimateRelationship> relationshipClass = relationshipSoruceClass.get(target.getClass());

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

}
