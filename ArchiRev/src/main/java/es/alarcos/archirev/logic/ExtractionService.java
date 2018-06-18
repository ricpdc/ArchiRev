package es.alarcos.archirev.logic;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import javax.imageio.ImageIO;
import javax.inject.Singleton;
import javax.swing.SwingConstants;

import org.apache.bcel.classfile.AnnotationEntry;
import org.apache.bcel.classfile.ClassFormatException;
import org.apache.bcel.classfile.ClassParser;
import org.apache.bcel.classfile.Constant;
import org.apache.bcel.classfile.JavaClass;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

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
import com.mxgraph.layout.mxCircleLayout;
import com.mxgraph.layout.mxCompactTreeLayout;
import com.mxgraph.layout.mxFastOrganicLayout;
import com.mxgraph.layout.mxGraphLayout;
import com.mxgraph.layout.mxOrganicLayout;
import com.mxgraph.layout.hierarchical.mxHierarchicalLayout;
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
		Validate.isTrue(sources!=null && !sources.isEmpty(), "There is no source as input");
		//TODO Integrate different sources into single one model
		for (Source source : sources) {
			switch(source.getType()) {
			case WEB_APP:
				extractArchimateModelForWebApp(model, source);
				break;
			case JPA:
				break;
			default:
			}
		}
		
		
	}

	private void extractArchimateModelForWebApp(Model model, Source source) {
		byte[] inputFile = source.getFile();

		MultiValueMap<String, ArchimateElement> modelElementsByClassName = new LinkedMultiValueMap<>();
		MultiValueMap<String, ArchimateRelationship> modelRelationshipsByClassName = new LinkedMultiValueMap<>();
		try {
			modelElementsByClassName = computeModelElementsByClassName(source, mapping);
			modelRelationshipsByClassName = computeModelRelationshipsByClassName(source, mapping,
					modelElementsByClassName);
			generateJgraphxDiagram(modelElementsByClassName, modelRelationshipsByClassName);
		} catch (NoClassDefFoundError | IOException e) {
			e.printStackTrace();
		}

	}

	private MultiValueMap<String, ArchimateElement> computeModelElementsByClassName(Source warSource,
			MultiValueMap<String, ArchimateElementEnum> mapping) throws ZipException, IOException {
		MultiValueMap<String, ArchimateElement> modelElementsByClassName = new LinkedMultiValueMap<>();
		ZipFile zipFile = getZipFile(warSource);
		ClassParser cp;

		@SuppressWarnings("unchecked")
		Enumeration<ZipEntry> entries = (Enumeration<ZipEntry>) zipFile.entries();
		while (entries.hasMoreElements()) {
			ZipEntry entry = entries.nextElement();
			if (entry.isDirectory())
				continue;

			if (!entry.getName().endsWith(".class"))
				continue;

			cp = new ClassParser(new ByteArrayInputStream(warSource.getFile()), entry.getName());
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
	
	private void generateJgraphxDiagram(MultiValueMap<String, ArchimateElement> modelElementsByClassName,
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
				entry.getValue()
						.sort((o1, o2) -> o1.getClass().getSimpleName().compareTo(o2.getClass().getSimpleName()));
				parent = graph.getDefaultParent();

				//TODO hacer primero, stream a 1.7, para poder analizar.
				List<ArchimateElement> componentElments = entry.getValue().stream()
						.filter(e -> e instanceof ApplicationComponent).collect(Collectors.toList());

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

			mxHierarchicalLayout hierarchicalLayout = new mxHierarchicalLayout(graph);
			hierarchicalLayout.setOrientation(SwingConstants.VERTICAL);
			// mxOrthogonalLayout orthogonalLayout = new mxOrthogonalLayout(graph);
			mxCompactTreeLayout compactTreeLayout = new mxCompactTreeLayout(graph, false, true);
			mxOrganicLayout organicLayout = new mxOrganicLayout(graph);
			mxFastOrganicLayout fastOrganicLayout = new mxFastOrganicLayout(graph);
			// mxEdgeLabelLayout edgeLabelLayout = new mxEdgeLabelLayout(graph);
			mxCircleLayout circleLayout = new mxCircleLayout(graph, 100);
			// mxPartitionLayout partitionLayout = new mxPartitionLayout(graph);
			// mxParallelEdgeLayout parallelEdgeLayout = new mxParallelEdgeLayout(graph,
			// 1000);
			// mxStackLayout stackLayout = new mxStackLayout(graph, false, 1000);

			ExtendedHierarchicalLayout extendedHierarchicalLayout = new ExtendedHierarchicalLayout(graph, 75);

			mxGraphLayout layout = extendedHierarchicalLayout;
			layout.execute(graph.getDefaultParent());

			BufferedImage image = mxCellRenderer.createBufferedImage(graph, null, 1, java.awt.Color.WHITE, true, null);
			File file = new File("C:\\Users\\Alarcos\\git\\ArchiRev\\ArchiRev\\target\\diagrams\\testJgraphX.png");
			ImageIO.write(image, "PNG", file);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private ZipFile getZipFile(final Source warSource) throws ZipException, IOException {
		File tempWarFile = File.createTempFile("warFile", ".tmp", null);
		FileOutputStream fos = new FileOutputStream(tempWarFile);
		fos.write(warSource.getFile());
		fos.close();
		
		ZipFile zipFile = new ZipFile(tempWarFile);
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
		ZipFile zipFile = getZipFile(warSource);
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

			cp = new ClassParser(new ByteArrayInputStream(warSource.getFile()), entry.getName());
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
		Class<? extends ArchimateRelationship> relationshipClass = mapPrioritizedRelationship.get(source.getClass())
				.get(target.getClass());

		if (relationshipClass.equals(AccessRelationship.class)) {
			return (ArchimateRelationship) ArchimateFactory.eINSTANCE.createAccessRelationship();
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
			// default
			return (ArchimateRelationship) ArchimateFactory.eINSTANCE.createAccessRelationship();
		}
	}

}
