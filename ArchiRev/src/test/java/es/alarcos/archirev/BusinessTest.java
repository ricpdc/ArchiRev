package es.alarcos.archirev;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import javax.imageio.ImageIO;
import javax.swing.SwingConstants;

import org.apache.bcel.classfile.AnnotationEntry;
import org.apache.bcel.classfile.ClassFormatException;
import org.apache.bcel.classfile.ClassParser;
import org.apache.bcel.classfile.Constant;
import org.apache.bcel.classfile.JavaClass;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.commons.lang3.tuple.Triple;
import org.benf.cfr.reader.entities.ClassFile;
import org.benf.cfr.reader.relationship.MemberNameResolver;
import org.benf.cfr.reader.state.ClassFileSourceImpl;
import org.benf.cfr.reader.state.DCCommonState;
import org.benf.cfr.reader.state.TypeUsageCollector;
import org.benf.cfr.reader.util.CannotLoadClassException;
import org.benf.cfr.reader.util.ListFactory;
import org.benf.cfr.reader.util.bytestream.BaseByteData;
import org.benf.cfr.reader.util.getopt.GetOptParser;
import org.benf.cfr.reader.util.getopt.Options;
import org.benf.cfr.reader.util.getopt.OptionsImpl;
import org.benf.cfr.reader.util.output.Dumper;
import org.benf.cfr.reader.util.output.ToStringDumper;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.zeroturnaround.zip.ZipUtil;

import com.archimatetool.model.IArchimateConcept;
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
import com.mxgraph.util.mxCellRenderer;
import com.mxgraph.util.mxConstants;
import com.mxgraph.view.mxGraph;

import es.alarcos.archirev.connector.ConnectorEnum;
import es.alarcos.archirev.layout.ExtendedHierarchicalLayout;
import es.alarcos.archirev.logic.ArchimateElementEnum;
import es.alarcos.archirev.shape.ShapeEnum;
import the.bytecode.club.bytecodeviewer.DecompilerSettings;
import the.bytecode.club.bytecodeviewer.decompilers.CFRDecompiler.Settings;
import the.bytecode.club.bytecodeviewer.decompilers.Decompiler;

class BusinessTest {

	static Logger LOGGER = LoggerFactory.getLogger(BusinessTest.class);

	private final Decompiler decompiler = Decompiler.CFR;
	private final DecompilerSettings settings = new DecompilerSettings(decompiler);

	private final String warPath = ".\\src\\test\\resources\\ArchiRev.war";

	@Test
	void testDecompileCFRSingleDocument() {

		final String inPath = ".\\src\\test\\resources\\SessionController.class";
		final String outPath = ".\\src\\test\\resources\\SessionController.java";

		try {
			Path path = Paths.get(inPath);
			byte[] bytes = Files.readAllBytes(path);
			Options options = new GetOptParser().parse(generateMainMethod(), OptionsImpl.getFactory());
			ClassFileSourceImpl classFileSource = new ClassFileSourceImpl(options);
			DCCommonState dcCommonState = new DCCommonState(options, classFileSource);
			String contents = doClass(dcCommonState, bytes);
			FileUtils.write(new File(outPath), contents, "UTF-8", false);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Test
	void testDecompileCFRWarFile() {
		extractAndDecompileJavaFromWar();

	}

	@Test
	@SuppressWarnings({ "rawtypes", "resource" })
	public void testAnnotationParsing() {
		try {
			File classFolder = extractClassesFromWar();

			URLClassLoader classLoader = new URLClassLoader(
					new URL[] { new URL("file:///" + classFolder.getAbsolutePath()) });

			Path classFolderPath = Paths.get(classFolder.getAbsolutePath());
			Iterator<File> it = FileUtils.iterateFiles(classFolder, new String[] { "class" }, true);
			while (it.hasNext()) {
				File file = (File) it.next();
				Path classFilePath = Paths.get(file.getAbsolutePath());

				Path relativeClassPath = classFolderPath.relativize(classFilePath);
				String classQualifiedName = FilenameUtils.removeExtension(relativeClassPath.toString())
						.replaceAll("\\\\", ".");
				Class c = classLoader.loadClass(classQualifiedName);
				System.out.println("\n" + classQualifiedName);
				for (Annotation annotation : c.getAnnotations()) {
					System.out.println("C\t" + annotation.annotationType().getSimpleName());
				}
				for (Field field : c.getDeclaredFields()) {
					for (Annotation annotation : field.getDeclaredAnnotations()) {
						System.out.println("F\t" + annotation.annotationType().getSimpleName());
					}
				}
				for (Constructor constructor : c.getDeclaredConstructors()) {
					for (Annotation annotation : constructor.getAnnotations()) {
						System.out.println("M\t" + annotation.annotationType().getSimpleName());
					}
				}
				for (Method method : c.getDeclaredMethods()) {
					for (Annotation annotation : method.getAnnotations()) {
						System.out.println("M\t" + annotation.annotationType().getSimpleName());
					}
				}
			}

		} catch (NoClassDefFoundError | ClassNotFoundException | MalformedURLException e) {
			e.printStackTrace();
		}
	}

	@Test
	@SuppressWarnings({ "rawtypes", "resource" })
	public void testAnnotationParsingAndMapGeneration() {
		try {
			// TODO Load this from a configuration json file
			MultiValueMap<String, ArchimateElementEnum> mapping = new LinkedMultiValueMap<>();
			mapping.add("ManagedBean", ArchimateElementEnum.APPLICATION);
			mapping.add("Controller", ArchimateElementEnum.APPLICATION);
			mapping.add("Service", ArchimateElementEnum.APPLICATION);
			mapping.add("Service", ArchimateElementEnum.SERVICE);
			mapping.add("Entity", ArchimateElementEnum.DATA_ENTITY);

			MultiValueMap<Class, ArchimateElement> modelElements = new LinkedMultiValueMap<>();
			MultiValueMap<Class, ArchimateRelationship> modelRelationships = new LinkedMultiValueMap<>();

			File classFolder = extractClassesFromWar();

			URLClassLoader classLoader = new URLClassLoader(
					new URL[] { new URL("file:///" + classFolder.getAbsolutePath()) });

			Iterator<File> it = FileUtils.iterateFiles(classFolder, new String[] { "class" }, true);
			Path classFolderPath = Paths.get(classFolder.getAbsolutePath());
			while (it.hasNext()) {
				File file = (File) it.next();
				Path classFilePath = Paths.get(file.getAbsolutePath());

				Path relativeClassPath = classFolderPath.relativize(classFilePath);
				String classQualifiedName = FilenameUtils.removeExtension(relativeClassPath.toString())
						.replaceAll("\\\\", ".");
				Class c = classLoader.loadClass(classQualifiedName);

				for (Annotation annotation : c.getAnnotations()) {

					String annotationSimpleName = annotation.annotationType().getSimpleName();
					List<ArchimateElementEnum> elementList = mapping.get(annotationSimpleName);
					if (elementList != null) {
						for (ArchimateElementEnum archimateElementEnum : elementList) {
							ArchimateElement elementToBeAdded = null;
							switch (archimateElementEnum) {
							case APPLICATION:
								elementToBeAdded = (ArchimateElement) ArchimateFactory.eINSTANCE
										.createApplicationFunction();
								break;
							case SERVICE:
								elementToBeAdded = (ArchimateElement) ArchimateFactory.eINSTANCE
										.createApplicationService();
								break;
							case DATA_ENTITY:
								elementToBeAdded = (ArchimateElement) ArchimateFactory.eINSTANCE.createDataObject();
								break;
							default:
								break;
							}
							elementToBeAdded.setName(c.getSimpleName());
							modelElements.add(c, elementToBeAdded);
						}
					}
				}
			}

			// for (Entry<Class, List<ArchimateElement>> entry : modelElements.entrySet()) {
			// Class clazz = entry.getKey();
			// LOGGER.info("");
			// LOGGER.info(clazz.getName());
			// for (ArchimateElement archimateElement : entry.getValue()) {
			// LOGGER.info("\t" + archimateElement.getClass().getSimpleName() + ": " +
			// archimateElement.getName());
			// }
			// }

		} catch (NoClassDefFoundError | ClassNotFoundException |

				MalformedURLException e) {
			e.printStackTrace();
		}
	}

	@Test
	@SuppressWarnings({ "resource", "unchecked" })
	public void testClassGraph() {
		// TODO Load this from a configuration json file
		MultiValueMap<String, ArchimateElementEnum> mapping = new LinkedMultiValueMap<>();
		mapping.add("ManagedBean", ArchimateElementEnum.APPLICATION);
		mapping.add("Controller", ArchimateElementEnum.APPLICATION);
		mapping.add("Component", ArchimateElementEnum.APPLICATION);
		mapping.add("Service", ArchimateElementEnum.APPLICATION);
		mapping.add("Entity", ArchimateElementEnum.DATA_ENTITY);
		mapping.add("Table", ArchimateElementEnum.DATA_ENTITY);
		mapping.add("MappedSuperclass", ArchimateElementEnum.DATA_ENTITY);
		mapping.add("Repository", ArchimateElementEnum.COMPONENT);
		mapping.add("SpringBootApplication", ArchimateElementEnum.COMPONENT);

		MultiValueMap<String, ArchimateElement> modelElementsByClassName = new LinkedMultiValueMap<>();
		MultiValueMap<String, ArchimateRelationship> modelRelationshipsByClassName = new LinkedMultiValueMap<>();

		try {
			modelElementsByClassName = computeModelElementsByClassName(warPath, mapping);

			modelRelationshipsByClassName = computeModelRelationshipsByClassName(warPath, mapping,
					modelElementsByClassName);

			generateJgraphxDiagram(modelElementsByClassName, modelRelationshipsByClassName);

		} catch (NoClassDefFoundError | IOException e) {
			e.printStackTrace();
		}
	}

	private MultiValueMap<String, ArchimateElement> computeModelElementsByClassName(String warPath,
			MultiValueMap<String, ArchimateElementEnum> mapping) throws ZipException, IOException {
		MultiValueMap<String, ArchimateElement> modelElementsByClassName = new LinkedMultiValueMap<>();
		ZipFile zipFile = getZipFile(warPath);
		ClassParser cp;

		@SuppressWarnings("unchecked")
		Enumeration<ZipEntry> entries = (Enumeration<ZipEntry>) zipFile.entries();
		while (entries.hasMoreElements()) {
			ZipEntry entry = entries.nextElement();
			if (entry.isDirectory())
				continue;

			if (!entry.getName().endsWith(".class"))
				continue;

			cp = new ClassParser(warPath, entry.getName());
			JavaClass javaClass = null;
			try {
				javaClass = cp.parse();
			} catch (ClassFormatException | IOException e) {
				LOGGER.error(entry.getName() + " cannot be parsed");
				javaClass = null;
				continue;
			}

			Set<ArchimateElementEnum> uniqueElements = new HashSet<>();
			for (AnnotationEntry annotationEntry : javaClass.getAnnotationEntries()) {
				String annotationType = annotationEntry.getAnnotationType();
				// LOGGER.debug(String.format("[%s]: @%s", javaClass.getClassName(),
				// annotationType));
				String annotation = annotationType.substring(annotationType.lastIndexOf("/") + 1,
						annotationType.length() - 1);
				List<ArchimateElementEnum> elementList = mapping.get(annotation);
				if (elementList != null) {
					uniqueElements.addAll(elementList);
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
					break;
				case COMPONENT:
					elementToBeAdded = (ArchimateElement) ArchimateFactory.eINSTANCE.createApplicationComponent();
					break;
				default:
					break;
				}
				elementToBeAdded
						.setName(javaClass.getClassName().substring(javaClass.getClassName().lastIndexOf(".") + 1));
				modelElementsByClassName.add(javaClass.getClassName(), elementToBeAdded);
			}
		}
		return modelElementsByClassName;
	}

	private MultiValueMap<String, ArchimateRelationship> computeModelRelationshipsByClassName(String warPath,
			MultiValueMap<String, ArchimateElementEnum> mapping,
			MultiValueMap<String, ArchimateElement> modelElementsByClassName) throws ZipException, IOException {
		MultiValueMap<String, ArchimateRelationship> modelRelationshipsByClassName = new LinkedMultiValueMap<>();
		ZipFile zipFile = getZipFile(warPath);
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

			cp = new ClassParser(warPath, entry.getName());
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

										ArchimateRelationship relationshipToBeAdded = getPrioritizedRelationship(source,
												target);

										relationshipToBeAdded.setSource(source);
										relationshipToBeAdded.setTarget(target);
										relationshipToBeAdded.setName(source.getName() + "-to-" + target.getName());
										String relationshipId = source.getName() + "--("
												+ relationshipToBeAdded.getClass().getSimpleName() + ")-->"
												+ target.getName();
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

		// TODO Move this map to constants and allow parametrize this.
		Map<Class<? extends ArchimateElement>, Map<Class<? extends ArchimateElement>, Class<? extends ArchimateRelationship>>> mapPrioritizedRelationship = new HashMap<>();

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
		mapPrioritizedRelationship.put(DataObject.class, dataObjectMap);

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
		}
		else if (relationshipClass.equals(SpecializationRelationship.class)) {
			return (ArchimateRelationship) ArchimateFactory.eINSTANCE.createSpecializationRelationship();
		} else if (relationshipClass.equals(TriggeringRelationship.class)) {
			return (ArchimateRelationship) ArchimateFactory.eINSTANCE.createTriggeringRelationship();
		} else {
			// default
			return (ArchimateRelationship) ArchimateFactory.eINSTANCE.createAccessRelationship();
		}
	}

	private ZipFile getZipFile(final String warPath) throws ZipException, IOException {
		File warFile = new File(warPath);

		if (!warFile.exists()) {
			LOGGER.error("War file " + warPath + " does not exist");
		}

		ZipFile zipFile = new ZipFile(warFile);
		return zipFile;
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

					// LOGGER.info("\t" + archimateElement.getClass().getSimpleName() + " (\"" +
					// archimateElement.getName()
					// + "\")");
				}

			}

			for (Entry<String, List<ArchimateRelationship>> entry : modelRelationshipsByClassName.entrySet()) {
				LOGGER.info("");
				LOGGER.info(entry.getKey());
				Set<Triple<IArchimateConcept, IArchimateConcept, Class<ArchimateRelationship>>> visitedEdges = new HashSet<>();
				for (ArchimateRelationship archimateRelationship : entry.getValue()) {
					Object node1 = nodes.get(archimateRelationship.getSource());
					Object node2 = nodes.get(archimateRelationship.getTarget());
					if(node1.equals(node2)) {
						continue;
					}

					String simpleName = archimateRelationship.getClass().getSimpleName();
					graph.insertEdge(parent, null, simpleName, node1, node2,
							archimateRelationship.getClass().getSimpleName());

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

	private void extractAndDecompileJavaFromWar() {
		try {
			File zipFile = new File(warPath);
			File rootFolder = zipFile.getParentFile();
			String inputBaseName = FilenameUtils.getBaseName(zipFile.getName());
			File classFolder = new File(rootFolder + File.separator + inputBaseName + "_output_class");
			if (!classFolder.exists()) {
				classFolder.mkdir();
			}

			ZipUtil.unpack(zipFile, classFolder);

			File javaFolder = new File(rootFolder + File.separator + inputBaseName + "_output_java");
			if (!javaFolder.exists()) {
				javaFolder.mkdir();
			}

			Options options = new GetOptParser().parse(generateMainMethod(), OptionsImpl.getFactory());
			ClassFileSourceImpl classFileSource = new ClassFileSourceImpl(options);
			DCCommonState dcCommonState = new DCCommonState(options, classFileSource);

			Path classFolderPath = Paths.get(classFolder.getAbsolutePath());
			Path javaFolderPath = Paths.get(javaFolder.getAbsolutePath());

			Iterator<File> it = FileUtils.iterateFiles(classFolder, new String[] { "class" }, true);
			while (it.hasNext()) {
				File file = (File) it.next();
				System.out.println(file);
				Path classFilePath = Paths.get(file.getAbsolutePath());

				Path relativeClassPath = classFolderPath.relativize(classFilePath);
				Path relativeJavaPath = Paths
						.get(FilenameUtils.removeExtension(relativeClassPath.toString()) + ".java");
				Path outPath = javaFolderPath.resolve(relativeJavaPath);

				byte[] bytes = Files.readAllBytes(classFilePath);
				String contents = doClass(dcCommonState, bytes);

				FileUtils.write(outPath.toFile(), contents, "UTF-8", false);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private File extractClassesFromWar() {
		File zipFile = new File(warPath);
		File rootFolder = zipFile.getParentFile();
		String inputBaseName = FilenameUtils.getBaseName(zipFile.getName());
		File classFolder = new File(rootFolder + File.separator + inputBaseName + "_output_class");
		if (!classFolder.exists()) {
			classFolder.mkdir();
		}

		ZipUtil.unpack(zipFile, classFolder);
		return classFolder;
	}

	private String[] generateMainMethod() {
		for (Settings setting : Settings.values()) {
			getSettings().registerSetting(setting);
		}

		String[] result = new String[getSettings().size() * 2 + 1];
		result[0] = "bytecodeviewer";
		int index = 1;
		for (Settings setting : Settings.values()) {
			result[index++] = "--" + setting.getParam();
			result[index++] = String.valueOf(getSettings().isSelected(setting));
		}
		return result;
	}

	private String doClass(DCCommonState dcCommonState, byte[] content1) throws Exception {
		Options options = dcCommonState.getOptions();
		Dumper d = new ToStringDumper();
		BaseByteData data = new BaseByteData(content1);
		ClassFile var24 = new ClassFile(data, "", dcCommonState);
		dcCommonState.configureWith(var24);

		try {
			var24 = dcCommonState.getClassFile(var24.getClassType());
		} catch (CannotLoadClassException var18) {
		}

		if (options.getOption(OptionsImpl.DECOMPILE_INNER_CLASSES)) {
			var24.loadInnerClasses(dcCommonState);
		}

		if (options.getOption(OptionsImpl.RENAME_MEMBERS)) {
			MemberNameResolver.resolveNames(dcCommonState,
					ListFactory.newList(dcCommonState.getClassCache().getLoadedTypes()));
		}

		var24.analyseTop(dcCommonState);
		TypeUsageCollector var25 = new TypeUsageCollector(var24);
		var24.collectTypeUsages(var25);
		String var26 = options.getOption(OptionsImpl.METHODNAME);
		if (var26 == null) {
			var24.dump(d);
		} else {
			try {
				for (org.benf.cfr.reader.entities.Method method : var24.getMethodByName(var26)) {
					method.dump(d, true);
				}
			} catch (NoSuchMethodException var19) {
				throw new IllegalArgumentException("No such method \'" + var26 + "\'.");
			}
		}
		d.print("");
		return d.toString();
	}

	private DecompilerSettings getSettings() {
		return settings;
	}

}
