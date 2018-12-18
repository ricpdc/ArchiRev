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
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
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
import com.mxgraph.analysis.mxGraphAnalysis;
import com.mxgraph.analysis.mxUnionFind;
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

	private static final int MIN_NUM_NODES_FOR_GRAPH_COMPONENT_VIEW = 2;

	private AbstractSourceCodeParser sourceCodeParser;

	private MultiValueMap<String, ArchimateElement> modelElementsByClassName = new LinkedMultiValueMap<>();
	private MultiValueMap<String, ArchimateRelationship> modelRelationshipsByClassName = new LinkedMultiValueMap<>();
	private File exportXmlFile;

	private Map<View, mxGraph> viewGraph = new HashMap<View, mxGraph>();
	private View defaultView;

	private Map<ArchimateElement, Object> graphNodesMap;
	private Map<ArchimateRelationship, Object> graphEdgesMap;

	public ExtractionService() {

	}

	public void extractArchimateModel(Model model) {
		Set<Source> sources = model.getExtraction().getSources();
		Validate.isTrue(sources != null && !sources.isEmpty(), "There is no source as input");
		// TODO Integrate different sources into single one model
		String modelName = model.getExtraction().getName();

		model.getViews().clear();

		for (Source source : sources) {
			modelName += ("_" + source.getName());
			model.setName(modelName);
			switch (source.getType()) {
			case JAVA_WEB_APP:
				sourceCodeParser = new JavaSourceCodeParser(model.getExtraction().getSetup());
				extractArchimateModelFromSourceCode(model, source);
				break;
			case CSHARP_APP:
				sourceCodeParser = new CSharpSourceCodeParser(model.getExtraction().getSetup());
				extractArchimateModelFromSourceCode(model, source);
				break;
			case JPA:
				// TODO complete this
				break;
			default:
			}
		}
	}

	private void generateModelViewsForComponents(Model model, ModelViewEnum viewType) {
		mxGraph graph = viewGraph.get(defaultView);
		List<List<mxCell>> components = computeGraphComponents(graph);
		for (int i = 0; i < components.size(); i++) {
			if (components.get(i).size() > MIN_NUM_NODES_FOR_GRAPH_COMPONENT_VIEW) {
				generateModelViewForComponent(viewType, model, graph, components.get(i));
			}
		}
	}

	public void exportArchimateModel(Model model) {
		Set<Source> sources = model.getExtraction().getSources();
		Validate.isTrue(sources != null && !sources.isEmpty(), "There is no source as input");
		// TODO Integrate different sources into single one model
		exportXmlFile = null;
		for (Source source : sources) {
			switch (source.getType()) {
			case JAVA_WEB_APP:
				sourceCodeParser = new JavaSourceCodeParser(model.getExtraction().getSetup());
				extractArchimateModelFromSourceCode(model, source);
				break;
			case CSHARP_APP:
				sourceCodeParser = new CSharpSourceCodeParser(model.getExtraction().getSetup());
				extractArchimateModelFromSourceCode(model, source);
				break;
			case JPA:
				break;
			default:
			}

			exportXmlFile = exportOpenExchangeFormat(model);
		}
		if (exportXmlFile != null) {
			model.setExportedPath(exportXmlFile.getAbsolutePath());
		}
	}

	private void extractArchimateModelFromSourceCode(Model model, Source source) {
		viewGraph = new HashMap<>();
		modelElementsByClassName = new LinkedMultiValueMap<>();
		modelRelationshipsByClassName = new LinkedMultiValueMap<>();
		try {
			modelElementsByClassName = sourceCodeParser.computeModelElementsByClassName(source);
			modelRelationshipsByClassName = sourceCodeParser.computeModelRelationshipsByClassName(source,
					modelElementsByClassName);
			generateModelAndDefaultView(model);
			for (ModelViewEnum viewType : model.getExtraction().getSelectedViews()) {
				if (viewType.equals(ModelViewEnum.ALL)) {
					continue;
				} else if (viewType.equals(ModelViewEnum.ALL_WITH_COMPONENTS)) {
					generateModelViewsForComponents(model, viewType);
				} else {
					generateModelView(viewType, model);
				}
			}
		} catch (NoClassDefFoundError | IOException e) {
			e.printStackTrace();
		}
	}

	private File generateModelAndDefaultView(Model model) {

		mxGraph graph = new mxGraph();
		Object parent = graph.getDefaultParent();
		graph.getModel().beginUpdate();

		loadShapeStyles(graph);

		try {
			graphNodesMap = new HashMap<>();

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
					componentNode = graph.insertVertex(parent, component.getId(), component.getName(), 0, 0,
							component.getName().length() * 5 + 60 + 30, 40 + 35, shapeEnum.getShape().getSimpleName());
					graphNodesMap.put(component, componentNode);
					parent = componentNode;
				}

				for (ArchimateElement archimateElement : entry.getValue()) {
					ShapeEnum shapeEnum = ShapeEnum.getByModelElement(archimateElement.getClass());

					if (archimateElement instanceof ApplicationComponent) {
						continue;
					}

					Object node = graph.insertVertex(parent, archimateElement.getId(), archimateElement.getName(), 15,
							20, archimateElement.getName().length() * 5 + 60, 40, shapeEnum.getShape().getSimpleName());
					graphNodesMap.put(archimateElement, componentNode != null ? componentNode : node);
					elements.add(new Element(model, archimateElement));
					// nodes.put(archimateElement, node);

					// LOGGER.info("\t" + archimateElement.getClass().getSimpleName() + " (\"" +
					// archimateElement.getName()
					// + "\")");
				}

			}

			parent = graph.getDefaultParent();
			List<String> visitedEdges = new ArrayList<>();
			graphEdgesMap = new HashMap<>();
			for (Entry<String, List<ArchimateRelationship>> entry : modelRelationshipsByClassName.entrySet()) {
				LOGGER.info("");
				LOGGER.info(entry.getKey());

				for (ArchimateRelationship archimateRelationship : entry.getValue()) {

					mxCell node1 = (mxCell) graphNodesMap.get(archimateRelationship.getSource());
					mxCell node2 = (mxCell) graphNodesMap.get(archimateRelationship.getTarget());
					if (node1 != null && node2 != null) {
						String edgeId = node1.getValue() + "--" + archimateRelationship.getClass().getSimpleName()
								+ "-->" + node2.getValue();

						if (visitedEdges.contains(edgeId) || node1.equals(node2) || node1.getParent().equals(node2)
								|| node2.getParent().equals(node1)) {
							continue;
						}

						String simpleName = archimateRelationship.getClass().getSimpleName();
						Object edge = graph.insertEdge(parent, archimateRelationship.getId(), simpleName, node1, node2,
								archimateRelationship.getClass().getSimpleName());
						relationships.add(new Relationship(model, archimateRelationship));
						visitedEdges.add(edgeId);
						graphEdgesMap.put(archimateRelationship, edge);

						LOGGER.info("\t" + archimateRelationship.getId());
					}
				}
			}

			// All elements are added to the model
			model.getElements().clear();
			model.getElements().addAll(elements);
			model.getRelationships().clear();
			model.getRelationships().addAll(relationships);

		} finally {
			graph.getModel().endUpdate();
		}

		try {

			ExtendedHierarchicalLayout extendedHierarchicalLayout = new ExtendedHierarchicalLayout(graph, 75);

			mxGraphLayout layout = extendedHierarchicalLayout;
			layout.execute(graph.getDefaultParent());

			defaultView = getDefaultView(model);
			computeMetrics(defaultView, graph);
			viewGraph.put(defaultView, graph);
			File file = new File(defaultView.getImagePath());
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

	private List<List<mxCell>> computeGraphComponents(mxGraph graph) {
		mxGraphAnalysis analyzer = mxGraphAnalysis.getInstance();
		Object[] nodes = graph.getChildVertices(graph.getDefaultParent());
		mxUnionFind connectionComponents = analyzer.getConnectionComponents(graph, nodes,
				graph.getChildEdges(graph.getDefaultParent()));

		List<mxCell> visitedNodes = new ArrayList<>();

		List<List<mxCell>> components = new ArrayList<>();

		// Object[] nodes = graph.getChildVertices(graph.getDefaultParent());
		for (Object objA : nodes) {
			mxCell node = (mxCell) objA;
			if (visitedNodes.contains(node)) {
				continue;
			}
			components.add(getConnectedNodes(nodes, connectionComponents, node, visitedNodes));
		}
		return components;
	}

	private List<mxCell> getConnectedNodes(Object[] nodes, mxUnionFind connectionComponents, mxCell node,
			List<mxCell> visitedNodes) {
		List<mxCell> newComponent = new ArrayList<>();
		newComponent.add(node);
		visitedNodes.add(node);
		List<mxCell> nodesToCheck = new ArrayList<>();
		nodesToCheck.add(node);

		while (!nodesToCheck.isEmpty()) {
			mxCell nodeA = (mxCell) nodesToCheck.get(0);
			nodesToCheck.remove(0);
			for (Object objB : nodes) {
				mxCell nodeB = (mxCell) objB;
				if (visitedNodes.contains(nodeB)) {
					continue;
				}
				if (!connectionComponents.differ(nodeA, nodeB)) {
					newComponent.add(nodeB);
					nodesToCheck.add(nodeB);
					visitedNodes.add(nodeB);
				}
			}
		}
		return newComponent;
	}

	private mxGraph generateGraphComponent(ModelViewEnum viewType, Model model) {
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
					componentNode = graph.insertVertex(parent, component.getId(), component.getName(), 0, 0,
							component.getName().length() * 5 + 60 + 30, 40 + 35, shapeEnum.getShape().getSimpleName());
					nodes.put(component, componentNode);
					parent = componentNode;
				}

				for (ArchimateElement archimateElement : entry.getValue()) {
					ShapeEnum shapeEnum = ShapeEnum.getByModelElement(archimateElement.getClass());

					if (archimateElement instanceof ApplicationComponent) {
						continue;
					}

					Object node = graph.insertVertex(parent, archimateElement.getId(), archimateElement.getName(), 15,
							20, archimateElement.getName().length() * 5 + 60, 40, shapeEnum.getShape().getSimpleName());
					nodes.put(archimateElement, componentNode != null ? componentNode : node);
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
						graph.insertEdge(parent, archimateRelationship.getId(), simpleName, node1, node2,
								archimateRelationship.getClass().getSimpleName());

						visitedEdges.add(edgeId);
					}
				}
			}

		} finally {
			graph.getModel().endUpdate();
		}

		return graph;

	}

	private File generateModelViewForComponent(ModelViewEnum viewType, Model model, mxGraph fullGraph,
			List<mxCell> component) {
		mxGraph graph = new mxGraph();
		Object parent = graph.getDefaultParent();
		graph.getModel().beginUpdate();

		loadShapeStyles(graph);

		HashMap<mxCell, mxCell> nodeMap = new HashMap<mxCell, mxCell>();

		try {
			parent = graph.getDefaultParent();
			for (mxCell node : component) {
				mxCell insertedNode = (mxCell) graph.insertVertex(parent, node.getId(), node.getValue(), 15, 20,
						node.getValue().toString().length() * 5 + 60, 40, node.getStyle());
				nodeMap.put(node, insertedNode);
			}

			Set<Object> affectedEdges = new HashSet<Object>(Arrays.asList(fullGraph.getAllEdges(component.toArray())));
			for (Object edgeObj : affectedEdges) {
				mxCell edge = (mxCell) edgeObj;
				graph.insertEdge(parent, edge.getId(), edge.getValue(), nodeMap.get(edge.getSource()),
						nodeMap.get(edge.getTarget()), edge.getStyle());
			}

		} finally {
			graph.getModel().endUpdate();
		}

		try {
			ExtendedHierarchicalLayout extendedHierarchicalLayout = new ExtendedHierarchicalLayout(graph, 75);

			mxGraphLayout layout = extendedHierarchicalLayout;
			layout.execute(graph.getDefaultParent());

			View view = getView(viewType, model);
			computeMetrics(view, graph);
			viewGraph.put(view, graph);
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

	private File generateModelView(final ModelViewEnum viewType, final Model model) {

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
					if (filterInForView(viewType, component)) {
						componentNode = graph.insertVertex(parent, component.getId(), component.getName(), 0, 0,
								component.getName().length() * 5 + 60 + 30, 40 + 35,
								shapeEnum.getShape().getSimpleName());
						nodes.put(component, componentNode);
						parent = componentNode;
					}
				}

				for (ArchimateElement archimateElement : entry.getValue()) {
					ShapeEnum shapeEnum = ShapeEnum.getByModelElement(archimateElement.getClass());

					if (archimateElement instanceof ApplicationComponent) {
						continue;
					}

					if (filterInForView(viewType, archimateElement)) {

						Object node = graph.insertVertex(parent, archimateElement.getId(), archimateElement.getName(),
								15, 20, archimateElement.getName().length() * 5 + 60, 40,
								shapeEnum.getShape().getSimpleName());
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
						graph.insertEdge(parent, archimateRelationship.getId(), simpleName, node1, node2,
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
			computeMetrics(view, graph);
			viewGraph.put(view, graph);
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
			defaultView.setName(model.getName() + " - " + defaultType.getLabel());
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
		view.setName(model.getName() + " - " + viewType.getLabel());
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

	private void computeMetrics(View view, mxGraph graph) {
		view.getMetrics().clear();
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

		view.addMetric(new Metric(view, "#Nodes", String.valueOf(numVertices)));
		view.addMetric(new Metric(view, "#Edges", String.valueOf(numEdges)));
		view.addMetric(new Metric(view, "Connectivity", df.format(connectivity)));
		view.addMetric(new Metric(view, "Density", df.format(density)));

		view.addMetric(new Metric(view, "#" + ConnectorEnum.ACCESS.getModelRelationship().getSimpleName(),
				String.valueOf(numAccess)));
		view.addMetric(new Metric(view, "#" + ConnectorEnum.AGGREGATION.getModelRelationship().getSimpleName(),
				String.valueOf(numAggregation)));
		view.addMetric(new Metric(view, "#" + ConnectorEnum.COMPOSITION.getModelRelationship().getSimpleName(),
				String.valueOf(numComposition)));
		view.addMetric(new Metric(view, "#" + ConnectorEnum.REALIZATION.getModelRelationship().getSimpleName(),
				String.valueOf(numRealization)));
		view.addMetric(new Metric(view, "#" + ConnectorEnum.SERVING.getModelRelationship().getSimpleName(),
				String.valueOf(numServing)));
		view.addMetric(new Metric(view, "#" + ConnectorEnum.SPECIALIZATION.getModelRelationship().getSimpleName(),
				String.valueOf(numSpecialization)));
		view.addMetric(new Metric(view, "#" + ConnectorEnum.TRIGGERING.getModelRelationship().getSimpleName(),
				String.valueOf(numTriggering)));

	}

	private File exportOpenExchangeFormat(Model model) {

		try {

			org.jdom2.Element eModel = createXmlRootElement(model.getName());
			Document doc = new Document(eModel);

			org.jdom2.Element eElements = createXmlElements();
			eModel.addContent(eElements);

			org.jdom2.Element eRelationships = createXmlRelationships();
			eModel.addContent(eRelationships);

			org.jdom2.Element eDiagrams = createXmlViews(eModel);

			for (Entry<View, mxGraph> entry : viewGraph.entrySet()) {
				org.jdom2.Element eView = createXmlView(entry.getKey(), entry.getValue());
				eDiagrams.addContent(eView);
			}

			XMLOutputter xmlOutput = new XMLOutputter();

			// display nice nice
			xmlOutput.setFormat(Format.getPrettyFormat());
			xmlOutput.output(doc, new FileWriter(model.getExportedPath()));

			// TODO remove this local test
			String xmlFileName = "C:\\Users\\Alarcos\\git\\ArchiRev\\ArchiRev\\temp\\" + model.getName()
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

	private org.jdom2.Element createXmlElements() {
		Namespace nsArchimate = Namespace.getNamespace(NS_ARCHIMATE);
		Namespace nsXsi = Namespace.getNamespace("xsi", NS_XSI);
		org.jdom2.Element eElements = new org.jdom2.Element("elements", nsArchimate);
		for (ArchimateElement archimateElement : graphNodesMap.keySet()) {
			org.jdom2.Element eElement = new org.jdom2.Element("element", nsArchimate);
			eElement.setAttribute("identifier", archimateElement.getId());
			eElement.setAttribute("type", archimateElement.getClass().getSimpleName(), nsXsi);
			org.jdom2.Element eName = new org.jdom2.Element("name", nsArchimate).addContent(archimateElement.getName());
			addLang(eName);
			eElement.addContent(eName);
			eElements.addContent(eElement);
		}
		return eElements;
	}

	private org.jdom2.Element createXmlRelationships() {
		Namespace nsArchimate = Namespace.getNamespace(NS_ARCHIMATE);
		Namespace nsXsi = Namespace.getNamespace("xsi", NS_XSI);
		org.jdom2.Element eRelationships = new org.jdom2.Element("relationships", nsArchimate);

		for (ArchimateRelationship archimateRelationship : graphEdgesMap.keySet()) {
			org.jdom2.Element eRelationship = new org.jdom2.Element("relationship", nsArchimate);
			eRelationship.setAttribute("identifier", archimateRelationship.getId());
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

	private org.jdom2.Element createXmlViews(org.jdom2.Element eModel) {
		Namespace nsArchimate = Namespace.getNamespace(NS_ARCHIMATE);

		org.jdom2.Element eViews = new org.jdom2.Element("views", nsArchimate);
		org.jdom2.Element eDiagrams = new org.jdom2.Element("diagrams", nsArchimate);

		eViews.addContent(eDiagrams);
		eModel.addContent(eViews);

		return eDiagrams;
	}

	private org.jdom2.Element createXmlView(View view, mxGraph graph) {
		Namespace nsArchimate = Namespace.getNamespace(NS_ARCHIMATE);
		Namespace nsXsi = Namespace.getNamespace("xsi", NS_XSI);

		org.jdom2.Element eView = new org.jdom2.Element("view", nsArchimate);
		addIdentifier(eView);
		eView.setAttribute("type", "Diagram", nsXsi);
		org.jdom2.Element eName = new org.jdom2.Element("name", nsArchimate).addContent(view.getName());
		addLang(eName);
		eView.addContent(eName);

		Object[] nodes = graph.getChildVertices(graph.getDefaultParent());

		Map<String, String> viewNodeIds = new HashMap<>();
		
		for (Object node : nodes) {
			org.jdom2.Element eNode = new org.jdom2.Element("node", nsArchimate);
			mxCell nodeCell = (mxCell) node;
			addIdentifier(eNode);
			viewNodeIds.put(nodeCell.getId(), eNode.getAttributeValue("identifier"));
			eNode.setAttribute("elementRef", nodeCell.getId());
			eNode.setAttribute("type", "Element", nsXsi);
			mxCell cell = (mxCell) node;
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

		Object[] edges = graph.getChildEdges(graph.getDefaultParent());

		for (Object edge : edges) {
			org.jdom2.Element eConnection = new org.jdom2.Element("connection", nsArchimate);
			mxCell edgeCell = (mxCell) edge;
			addIdentifier(eConnection);
			
			eConnection.setAttribute("relationshipRef", edgeCell.getId());
			eConnection.setAttribute("type", "Relationship", nsXsi);
			eConnection.setAttribute("source", viewNodeIds.get(edgeCell.getSource().getId()));
			eConnection.setAttribute("target", viewNodeIds.get(edgeCell.getTarget().getId()));
			mxCell cell = (mxCell) edge;

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

		return eView;
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
		eNode.setAttribute("x", String.valueOf((int) Math.max(0.0, cell.getGeometry().getX())));
		eNode.setAttribute("y", String.valueOf((int) Math.max(0.0, cell.getGeometry().getY())));
		eNode.setAttribute("w", String.valueOf((int) Math.max(0.0, cell.getGeometry().getWidth())));
		eNode.setAttribute("h", String.valueOf((int) Math.max(0.0, cell.getGeometry().getHeight())));
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
