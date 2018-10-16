package es.alarcos.archirev.logic;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;

import javax.imageio.ImageIO;
import javax.inject.Singleton;
import javax.xml.XMLConstants;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.apache.commons.lang3.Validate;
import org.jdom2.Attribute;
import org.jdom2.Document;
import org.jdom2.Namespace;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.xml.sax.SAXException;

import com.archimatetool.model.impl.ApplicationComponent;
import com.archimatetool.model.impl.ArchimateConcept;
import com.archimatetool.model.impl.ArchimateElement;
import com.archimatetool.model.impl.ArchimateRelationship;
import com.archimatetool.model.impl.DataObject;
import com.mxgraph.canvas.mxGraphics2DCanvas;
import com.mxgraph.layout.mxGraphLayout;
import com.mxgraph.model.mxCell;
import com.mxgraph.util.mxCellRenderer;
import com.mxgraph.util.mxConstants;
import com.mxgraph.view.mxGraph;

import es.alarcos.archirev.logic.connector.ConnectorEnum;
import es.alarcos.archirev.logic.layout.ExtendedHierarchicalLayout;
import es.alarcos.archirev.logic.shape.ShapeEnum;
import es.alarcos.archirev.model.Element;
import es.alarcos.archirev.model.Metric;
import es.alarcos.archirev.model.Model;
import es.alarcos.archirev.model.Relationship;
import es.alarcos.archirev.model.Source;
import es.alarcos.archirev.model.View;
import es.alarcos.archirev.model.enums.ModelViewEnum;

@Singleton
@Service
public class ExtractionService implements Serializable {

	private static final long serialVersionUID = -4392305100176250199L;

	static Logger LOGGER = LoggerFactory.getLogger(ExtractionService.class);

	
	private static final String DEFAULT_LANG = "en";
	private static final String SCHEMA_LOCATION = "http://www.opengroup.org/xsd/archimate/3.0/ http://www.opengroup.org/xsd/archimate/3.0/archimate3_Diagram.xsd http://purl.org/dc/elements/1.1/ http://dublincore.org/schemas/xmls/qdc/2008/02/11/dc.xsd";

	private static final String NS_ARCHIMATE = "http://www.opengroup.org/xsd/archimate/3.0/";
	private static final String NS_ELEMENTS = "http://purl.org/dc/elements/1.1/";
	private static final String NS_XSI = "http://www.w3.org/2001/XMLSchema-instance";

	
	private AbstractSourceCodeParser sourceCodeParser;

	private MultiValueMap<String, ArchimateElement> modelElementsByClassName = new LinkedMultiValueMap<>();
	private MultiValueMap<String, ArchimateRelationship> modelRelationshipsByClassName = new LinkedMultiValueMap<>();

	public ExtractionService() {

	}

	public void extractArchimateModel(Model model) {
		Set<Source> sources = model.getExtraction().getSources();
		Validate.isTrue(sources != null && !sources.isEmpty(), "There is no source as input");
		// TODO Integrate different sources into single one model
		File imageFile = null;
		String modelName = model.getExtraction().getName();
		
		model.getViews().clear();
		
		for (Source source : sources) {
			modelName += ("_" + source.getName());
			switch (source.getType()) {
			case JAVA_WEB_APP:
				sourceCodeParser = new JavaSourceCodeParser(model.getExtraction().getSetup());
				imageFile = extractArchimateModelFromSourceCode(model, source, false);
				break;
			case CSHARP_APP:
				sourceCodeParser = new CSharpSourceCodeParser(model.getExtraction().getSetup());
				imageFile = extractArchimateModelFromSourceCode(model, source, false);
				break;
			case JPA:
				//TODO complete this
				break;
			default:
			}
		}
		model.setName(modelName);
		for (ModelViewEnum viewType : model.getExtraction().getSelectedViews()) {
			if(viewType.equals(ModelViewEnum.ALL)) {
				continue;
			}
			generateModelView(viewType, model, modelElementsByClassName, modelRelationshipsByClassName);
		}
	}

	

	public void exportArchimateModel(Model model) {
		Set<Source> sources = model.getExtraction().getSources();
		Validate.isTrue(sources != null && !sources.isEmpty(), "There is no source as input");
		// TODO Integrate different sources into single one model
		File exportXmlFile = null;
		for (Source source : sources) {
			switch (source.getType()) {
			case JAVA_WEB_APP:
				exportXmlFile = extractArchimateModelFromSourceCode(model, source, true);
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

	private File extractArchimateModelFromSourceCode(Model model, Source source, boolean export) {

		modelElementsByClassName = new LinkedMultiValueMap<>();
		modelRelationshipsByClassName = new LinkedMultiValueMap<>();
		try {
			modelElementsByClassName = sourceCodeParser.computeModelElementsByClassName(source);
			modelRelationshipsByClassName = sourceCodeParser.computeModelRelationshipsByClassName(source,
					modelElementsByClassName);
			if (!export) {
				return generateModelAndDefaultView(model, modelElementsByClassName, modelRelationshipsByClassName);
			} else {
				return exportOpenExchangeFormat(model, modelElementsByClassName, modelRelationshipsByClassName);
			}
		} catch (NoClassDefFoundError | IOException e) {
			e.printStackTrace();
		}
		return null;

	}

	

	private File generateModelAndDefaultView(Model model,
			MultiValueMap<String, ArchimateElement> modelElementsByClassName,
			MultiValueMap<String, ArchimateRelationship> modelRelationshipsByClassName) {

		mxGraph graph = new mxGraph();
		Object parent = graph.getDefaultParent();
		graph.getModel().beginUpdate();

		loadShapeStyles(graph);

		try {
			Map<ArchimateElement, Object> nodes = new HashMap<>();

			Set<Element> elements = new TreeSet<>();
			Set<Relationship> relationships = new TreeSet<>();

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
					elements.add(new Element(model, archimateElement.getName(), archimateElement.getDocumentation(),
							archimateElement.getClass().getSimpleName()));
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
					if (node1 != null && node2 != null) {
						String edgeId = node1.getValue() + "--" + archimateRelationship.getClass().getSimpleName()
								+ "-->" + node2.getValue();

						if (visitedEdges.contains(edgeId) || node1.equals(node2) || node1.getParent().equals(node2)
								|| node2.getParent().equals(node1)) {
							continue;
						}

						String simpleName = archimateRelationship.getClass().getSimpleName();
						graph.insertEdge(parent, null, simpleName, node1, node2,
								archimateRelationship.getClass().getSimpleName());
						relationships.add(new Relationship(model, archimateRelationship.getName(),
								archimateRelationship.getDocumentation(),
								archimateRelationship.getClass().getSimpleName()));
						visitedEdges.add(edgeId);

						LOGGER.info("\t" + archimateRelationship.getId());
					}
				}
			}

			// All elements are added to the model
			model.getElements().clear();
			model.getElements().addAll(elements);
			model.getRelationships().clear();
			model.getRelationships().addAll(relationships);
			computeMetrics(model, graph);

		} finally {
			graph.getModel().endUpdate();
		}

		try {

			ExtendedHierarchicalLayout extendedHierarchicalLayout = new ExtendedHierarchicalLayout(graph, 75);

			mxGraphLayout layout = extendedHierarchicalLayout;
			layout.execute(graph.getDefaultParent());

			View defaultView = getDefaultView(model);
			File file = new File(defaultView.getImagePath());
			BufferedImage image = mxCellRenderer.createBufferedImage(graph, null, 1, java.awt.Color.WHITE, true, null);
			if (image != null) {
				ImageIO.write(image, "PNG", file);

				// TODO remove this local test
				 File localFile = new File(
				 "C:\\Users\\Alarcos\\git\\ArchiRev\\ArchiRev\\target\\diagrams\\testJgraphX.png");
				 ImageIO.write(image, "PNG", localFile);
			}

			return file;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	private File generateModelView(final ModelViewEnum viewType, final Model model,
			MultiValueMap<String, ArchimateElement> modelElementsByClassName,
			MultiValueMap<String, ArchimateRelationship> modelRelationshipsByClassName) {

		mxGraph graph = new mxGraph();
		Object parent = graph.getDefaultParent();
		graph.getModel().beginUpdate();

		loadShapeStyles(graph);

		try {
			Map<ArchimateElement, Object> nodes = new HashMap<>();

			for (Entry<String, List<ArchimateElement>> entry : modelElementsByClassName.entrySet()) {

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
					if(filterInForView(viewType, component)){
						componentNode = graph.insertVertex(parent, null, component.getName(), 0, 0,
							component.getName().length() * 5 + 60 + 30, 40 + 35, shapeEnum.getShape().getSimpleName());
						nodes.put(component, componentNode);
						parent = componentNode;
					}
				}

				for (ArchimateElement archimateElement : entry.getValue()) {
					ShapeEnum shapeEnum = ShapeEnum.getByModelElement(archimateElement.getClass());

					if (archimateElement instanceof ApplicationComponent) {
						continue;
					}
					
					if(filterInForView(viewType, archimateElement)){

						Object node = graph.insertVertex(parent, null, archimateElement.getName(), 15, 20,
								archimateElement.getName().length() * 5 + 60, 40, shapeEnum.getShape().getSimpleName());
						nodes.put(archimateElement, componentNode != null ? componentNode : node);
					}
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
					if (node1 != null && node2 != null) {
						String edgeId = node1.getValue() + "--" + archimateRelationship.getClass().getSimpleName()
								+ "-->" + node2.getValue();

						if (visitedEdges.contains(edgeId) || node1.equals(node2) || node1.getParent().equals(node2)
								|| node2.getParent().equals(node1)) {
							continue;
						}

						String simpleName = archimateRelationship.getClass().getSimpleName();
						graph.insertEdge(parent, null, simpleName, node1, node2,
								archimateRelationship.getClass().getSimpleName());

						visitedEdges.add(edgeId);
					}
				}
			}

		} finally {
			graph.getModel().endUpdate();
		}

		try {
			ExtendedHierarchicalLayout extendedHierarchicalLayout = new ExtendedHierarchicalLayout(graph, 75);

			mxGraphLayout layout = extendedHierarchicalLayout;
			layout.execute(graph.getDefaultParent());

			View view = getView(viewType, model);
			File file = new File(view.getImagePath());
			BufferedImage image = mxCellRenderer.createBufferedImage(graph, null, 1, java.awt.Color.WHITE, true, null);
			if (image != null) {
				ImageIO.write(image, "PNG", file);
			}

			return file;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	private View getDefaultView(Model model) {
		final Timestamp now = new Timestamp(new Date().getTime());
		ModelViewEnum defaultType = ModelViewEnum.ALL;
		View defaultView = null;
		if (model.getViews() == null || model.getViews().isEmpty()) {
			defaultView = new View();
			defaultView.setModel(model);
			defaultView.setType(defaultType);
			defaultView.setCreatedAt(now);
			defaultView.setCreatedBy(model.getModifiedBy());
			defaultView.setModifiedAt(now);
			defaultView.setModifiedBy(model.getModifiedBy());
			model.getViews().add(defaultView);
		} else {
			defaultView = model.getViews().iterator().next(); // default view
			model.setModifiedAt(now);
			model.setModifiedBy(model.getModifiedBy());
		}
		defaultView.setImagePath(createImageFile(model, defaultType));
		return defaultView;
	}

	private View getView(ModelViewEnum viewType, Model model) {
		final Timestamp now = new Timestamp(new Date().getTime());

		View view = new View();
		view.setModel(model);
		view.setType(viewType);
		view.setCreatedAt(now);
		view.setCreatedBy(model.getModifiedBy());
		view.setModifiedAt(now);
		view.setModifiedBy(model.getModifiedBy());
		model.getViews().add(view);
		view.setImagePath(createImageFile(model, viewType));
		return view;
	}

	private boolean filterInForView(ModelViewEnum modelViewEnum, ArchimateConcept archimateElement) {
		boolean filterInForView = false;
		if (modelViewEnum.equals(ModelViewEnum.APPLICATION_BEHAVIOUR)) {
			filterInForView = true;
		} else if (modelViewEnum.equals(ModelViewEnum.APPLICATION_STRUCTURE)) {
			filterInForView = true;
		} else if (modelViewEnum.equals(ModelViewEnum.INFORMATION_STRUCTURE)) {
			filterInForView = archimateElement instanceof DataObject;
		}
		return filterInForView;
	}

	private void computeMetrics(Model model, mxGraph graph) {
		model.getMetrics().clear();
		DecimalFormat df = new DecimalFormat("###.###");

		// TODO compute further metrics from graph.
		Object[] edges = graph.getChildEdges(graph.getDefaultParent());
		Object[] vertices = graph.getChildVertices(graph.getDefaultParent());
		int numEdges = edges.length;
		int numVertices = vertices.length;

		double connectivity = (double) numEdges / (double) numVertices;
		double density = (double) numEdges / ((double) (numVertices * (numVertices - 1)) / 2.0);

		// Separability is the cut(articulation) vertex divided into total number of
		// vertices

		int numAccess = 0;
		int numAggregation = 0;
		int numComposition = 0;
		int numRealization = 0;
		int numServing = 0;
		int numSpecialization = 0;
		int numTriggering = 0;

		for (Object e : edges) {
			mxCell edge = (mxCell) e;
			edge.getValue();
			ConnectorEnum edgeType = ConnectorEnum.getEnumByName(edge.getValue().toString());
			switch (edgeType) {
			case ACCESS:
				numAccess++;
				break;
			case AGGREGATION:
				numAggregation++;
				break;
			case COMPOSITION:
				numComposition++;
				break;
			case REALIZATION:
				numRealization++;
				break;
			case SERVING:
				numServing++;
				break;
			case SPECIALIZATION:
				numSpecialization++;
				break;
			case TRIGGERING:
				numTriggering++;
				break;
			case DEFAULT:
			default:
			}
		}

		model.addMetric(new Metric(model, "#Nodes", String.valueOf(numVertices)));
		model.addMetric(new Metric(model, "#Edges", String.valueOf(numEdges)));
		model.addMetric(new Metric(model, "Connectivity", df.format(connectivity)));
		model.addMetric(new Metric(model, "Density", df.format(density)));

		model.addMetric(new Metric(model, "#" + ConnectorEnum.ACCESS.getModelRelationship().getSimpleName(),
				String.valueOf(numAccess)));
		model.addMetric(new Metric(model, "#" + ConnectorEnum.AGGREGATION.getModelRelationship().getSimpleName(),
				String.valueOf(numAggregation)));
		model.addMetric(new Metric(model, "#" + ConnectorEnum.COMPOSITION.getModelRelationship().getSimpleName(),
				String.valueOf(numComposition)));
		model.addMetric(new Metric(model, "#" + ConnectorEnum.REALIZATION.getModelRelationship().getSimpleName(),
				String.valueOf(numRealization)));
		model.addMetric(new Metric(model, "#" + ConnectorEnum.SERVING.getModelRelationship().getSimpleName(),
				String.valueOf(numServing)));
		model.addMetric(new Metric(model, "#" + ConnectorEnum.SPECIALIZATION.getModelRelationship().getSimpleName(),
				String.valueOf(numSpecialization)));
		model.addMetric(new Metric(model, "#" + ConnectorEnum.TRIGGERING.getModelRelationship().getSimpleName(),
				String.valueOf(numTriggering)));

	}

	private File exportOpenExchangeFormat(Model model, MultiValueMap<String, ArchimateElement> modelElementsByClassName,
			MultiValueMap<String, ArchimateRelationship> modelRelationshipsByClassName) {
		mxGraph graph = new mxGraph();
		graph.getModel().beginUpdate();
		try {

			org.jdom2.Element eModel = createXmlRootElement(model.getName());
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
					if (node1 != null && node2 != null) {
						String edgeId = node1.getValue() + "--" + archimateRelationship.getClass().getSimpleName()
								+ "-->" + node2.getValue();

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
			}

			graph.getModel().endUpdate();

			ExtendedHierarchicalLayout extendedHierarchicalLayout = new ExtendedHierarchicalLayout(graph, 75);

			mxGraphLayout layout = extendedHierarchicalLayout;
			layout.execute(graph.getDefaultParent());

			// BufferedImage image = mxCellRenderer.createBufferedImage(graph, null, 1,
			// java.awt.Color.WHITE, true, null);
			// File file = new
			// File("C:\\Users\\Alarcos\\git\\ArchiRev\\ArchiRev\\target\\diagrams\\testJgraphX.png");
			// ImageIO.write(image, "PNG", file);

			org.jdom2.Element eElements = createXmlElements(nodes);
			eModel.addContent(eElements);

			org.jdom2.Element eRelationships = createXmlRelationships(edges);
			eModel.addContent(eRelationships);

			org.jdom2.Element eViews = createXmlViews(nodes, edges, graph);
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

	private org.jdom2.Element createXmlRootElement(final String name) {
		Namespace nsArchimate = Namespace.getNamespace(NS_ARCHIMATE);
		Namespace nsDc = Namespace.getNamespace("dc", NS_ELEMENTS);
		Namespace nsXsi = Namespace.getNamespace("xsi", NS_XSI);

		org.jdom2.Element eModel = new org.jdom2.Element("model", nsArchimate);
		eModel.addNamespaceDeclaration(nsDc);
		eModel.addNamespaceDeclaration(nsXsi);

		eModel.setAttribute("schemaLocation", SCHEMA_LOCATION, nsXsi);
		addIdentifier(eModel);

		org.jdom2.Element eName = new org.jdom2.Element("name", nsArchimate).addContent(name);
		addLang(eName);
		eModel.addContent(eName);

		org.jdom2.Element eMetadata = new org.jdom2.Element("metadata", nsArchimate);
		eMetadata.addContent(new org.jdom2.Element("schema", nsArchimate).addContent("Dublin core"));
		eMetadata.addContent(new org.jdom2.Element("schemaversion", nsArchimate).addContent("1.1"));
		// eMetadata.addContent(new org.jdom2.Element("title", nsDc).addContent("Diagram
		// - " +
		// name));
		// eMetadata.addContent(new org.jdom2.Element("creator",
		// nsDc).addContent(System.getProperty("user.name")));
		eModel.addContent(eMetadata);

		return eModel;
	}

	private org.jdom2.Element createXmlElements(Map<ArchimateElement, Object> nodes) {
		Namespace nsArchimate = Namespace.getNamespace(NS_ARCHIMATE);
		Namespace nsXsi = Namespace.getNamespace("xsi", NS_XSI);
		org.jdom2.Element eElements = new org.jdom2.Element("elements", nsArchimate);
		for (ArchimateElement archimateElement : nodes.keySet()) {
			org.jdom2.Element eElement = new org.jdom2.Element("element", nsArchimate);
			addIdentifier(eElement);
			archimateElement.setId(eElement.getAttributeValue("identifier"));
			eElement.setAttribute("type", archimateElement.getClass().getSimpleName(), nsXsi);
			org.jdom2.Element eName = new org.jdom2.Element("name", nsArchimate).addContent(archimateElement.getName());
			addLang(eName);
			eElement.addContent(eName);
			eElements.addContent(eElement);
		}
		return eElements;
	}

	private org.jdom2.Element createXmlRelationships(Map<ArchimateRelationship, Object> relationships) {
		Namespace nsArchimate = Namespace.getNamespace(NS_ARCHIMATE);
		Namespace nsXsi = Namespace.getNamespace("xsi", NS_XSI);
		org.jdom2.Element eRelationships = new org.jdom2.Element("relationships", nsArchimate);

		for (ArchimateRelationship archimateRelationship : relationships.keySet()) {
			org.jdom2.Element eRelationship = new org.jdom2.Element("relationship", nsArchimate);
			addIdentifier(eRelationship);
			archimateRelationship.setId(eRelationship.getAttributeValue("identifier"));
			String simpleName = archimateRelationship.getClass().getSimpleName();
			simpleName = simpleName.substring(0, simpleName.lastIndexOf("Relationship"));
			eRelationship.setAttribute("type", simpleName, nsXsi);
			eRelationship.setAttribute("source", archimateRelationship.getSource().getId());
			eRelationship.setAttribute("target", archimateRelationship.getTarget().getId());
			org.jdom2.Element eName = new org.jdom2.Element("name", nsArchimate)
					.addContent(archimateRelationship.getName());
			addLang(eName);
			eRelationship.addContent(eName);
			eRelationships.addContent(eRelationship);
		}
		return eRelationships;
	}

	private org.jdom2.Element createXmlViews(Map<ArchimateElement, Object> nodes,
			Map<ArchimateRelationship, Object> edges, mxGraph graph) {
		Namespace nsArchimate = Namespace.getNamespace(NS_ARCHIMATE);
		Namespace nsXsi = Namespace.getNamespace("xsi", NS_XSI);

		org.jdom2.Element eViews = new org.jdom2.Element("views", nsArchimate);
		org.jdom2.Element eDiagrams = new org.jdom2.Element("diagrams", nsArchimate);
		org.jdom2.Element eView = new org.jdom2.Element("view", nsArchimate);
		addIdentifier(eView);
		eView.setAttribute("type", "Diagram", nsXsi);
		org.jdom2.Element eName = new org.jdom2.Element("name", nsArchimate).addContent("DefaultView");
		addLang(eName);
		eView.addContent(eName);
		eDiagrams.addContent(eView);
		eViews.addContent(eDiagrams);

		Map<ArchimateElement, String> uuidMap = new HashMap<>();

		for (Entry<ArchimateElement, Object> node : nodes.entrySet()) {
			org.jdom2.Element eNode = new org.jdom2.Element("node", nsArchimate);
			addIdentifier(eNode);
			uuidMap.put(node.getKey(), eNode.getAttributeValue("identifier"));
			eNode.setAttribute("elementRef", node.getKey().getId());
			eNode.setAttribute("type", "org.jdom2.Element", nsXsi);
			mxCell cell = (mxCell) node.getValue();
			addGeometry(eNode, cell);

			org.jdom2.Element eStyle = new org.jdom2.Element("style", nsArchimate);
			eNode.addContent(eStyle);

			Map<String, Object> cellStyle = graph.getCellStyle(cell);
			Color fillColor = Color.decode("#" + cellStyle.get("fillColor"));
			Color lineColor = Color.decode("#" + cellStyle.get("strokeColor"));

			org.jdom2.Element eFillColor = new org.jdom2.Element("fillColor", nsArchimate)
					.setAttribute("r", String.valueOf(fillColor.getRed()))
					.setAttribute("g", String.valueOf(fillColor.getGreen()))
					.setAttribute("b", String.valueOf(fillColor.getBlue()));
			org.jdom2.Element eLineColor = new org.jdom2.Element("lineColor", nsArchimate)
					.setAttribute("r", String.valueOf(lineColor.getRed()))
					.setAttribute("g", String.valueOf(lineColor.getGreen()))
					.setAttribute("b", String.valueOf(lineColor.getBlue()));

			eStyle.addContent(eFillColor);
			eStyle.addContent(eLineColor);

			eView.addContent(eNode);
		}

		for (Entry<ArchimateRelationship, Object> edge : edges.entrySet()) {
			org.jdom2.Element eConnection = new org.jdom2.Element("connection", nsArchimate);
			addIdentifier(eConnection);
			eConnection.setAttribute("relationshipRef", edge.getKey().getId());
			eConnection.setAttribute("type", "Relationship", nsXsi);
			eConnection.setAttribute("source", uuidMap.get(edge.getKey().getSource()));
			eConnection.setAttribute("target", uuidMap.get(edge.getKey().getTarget()));
			mxCell cell = (mxCell) edge.getValue();

			org.jdom2.Element eStyle = new org.jdom2.Element("style", nsArchimate);
			eConnection.addContent(eStyle);

			Map<String, Object> cellStyle = graph.getCellStyle(cell);
			Color lineColor = Color.decode("#" + cellStyle.get("strokeColor"));

			org.jdom2.Element eLineColor = new org.jdom2.Element("lineColor", nsArchimate)
					.setAttribute("r", String.valueOf(lineColor.getRed()))
					.setAttribute("g", String.valueOf(lineColor.getGreen()))
					.setAttribute("b", String.valueOf(lineColor.getBlue()));

			eStyle.addContent(eLineColor);

			eView.addContent(eConnection);
		}

		return eViews;
	}

	private String addIdentifier(org.jdom2.Element element) {
		String id = "id-" + UUID.randomUUID();
		element.setAttribute("identifier", id);
		return id;
	}

	private void addLang(org.jdom2.Element element) {
		Namespace nsXml = Namespace.getNamespace("xml", "http://www.w3.org/XML/1998/namespace");
		element.setAttribute(new Attribute("lang", DEFAULT_LANG, nsXml));
	}

	private void addGeometry(org.jdom2.Element eNode, mxCell cell) {
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

	private String createImageFile(final Model model, final ModelViewEnum type) {
		File folder = new File(model.getRootDiagramPath());
		if (!folder.exists()) {
			folder.mkdir();
		}
		Path filePath = null;
		try {
			filePath = Files.createFile(Paths.get(folder.getAbsolutePath() + File.separator + "p_"
					+ model.getProject().getId() + "_e_" + model.getExtraction().getId() + "_" + type.getLabel() + "_"
					+ UUID.randomUUID() + ".png"));

		} catch (IOException e) {
			LOGGER.error(e.getMessage());
			return null;
		}
		return filePath.toString();
	}

	public AbstractSourceCodeParser getSourceCodeParser() {
		return sourceCodeParser;
	}

}
