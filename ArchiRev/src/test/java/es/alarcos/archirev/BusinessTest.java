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
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
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

import com.archimatetool.model.impl.ArchimateElement;
import com.archimatetool.model.impl.ArchimateFactory;
import com.archimatetool.model.impl.ArchimateRelationship;
import com.mxgraph.canvas.mxGraphics2DCanvas;
import com.mxgraph.layout.hierarchical.mxHierarchicalLayout;
import com.mxgraph.util.mxCellRenderer;
import com.mxgraph.util.mxConstants;
import com.mxgraph.view.mxGraph;
import com.mxgraph.view.mxStylesheet;

import es.alarcos.archirev.logic.ArchimateElementEnum;
import es.alarcos.archirev.shape.ArchiMateApplicationFunctionShape;
import guru.nidi.graphviz.attribute.Attributes;
import guru.nidi.graphviz.attribute.Color;
import guru.nidi.graphviz.attribute.Shape;
import guru.nidi.graphviz.attribute.Style;
import guru.nidi.graphviz.engine.Format;
import guru.nidi.graphviz.engine.Graphviz;
import guru.nidi.graphviz.model.Factory;
import guru.nidi.graphviz.model.Graph;
import guru.nidi.graphviz.model.Node;
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

			for (Entry<Class, List<ArchimateElement>> entry : modelElements.entrySet()) {
				Class clazz = entry.getKey();
				LOGGER.info("");
				LOGGER.info(clazz.getName());
				for (ArchimateElement archimateElement : entry.getValue()) {
					LOGGER.info("\t" + archimateElement.getClass().getSimpleName() + ": " + archimateElement.getName());
				}
			}

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
		mapping.add("Service", ArchimateElementEnum.APPLICATION);
		mapping.add("Service", ArchimateElementEnum.SERVICE);
		mapping.add("Entity", ArchimateElementEnum.DATA_ENTITY);

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
										ArchimateRelationship relationshipToBeAdded = (ArchimateRelationship) ArchimateFactory.eINSTANCE
												.createAssociationRelationship();
										relationshipToBeAdded.setSource(source);
										relationshipToBeAdded.setName(source.getName() + "-to-" + target.getName());
										relationshipToBeAdded.setTarget(target);
										modelRelationshipsByClassName.add(javaClass.getClassName(),
												relationshipToBeAdded);
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

		try {
			Map<ArchimateElement, Object> nodes = new HashMap<>();

			mxGraphics2DCanvas.putShape("applicationFunction", new ArchiMateApplicationFunctionShape());
			Map<String, Object> style = new HashMap<String, Object>();
			style.put(mxConstants.STYLE_SHAPE, "applicationFunction");
			graph.getStylesheet().putCellStyle("applicationFunction", style);

			for (Entry<String, List<ArchimateElement>> entry : modelElementsByClassName.entrySet()) {
				LOGGER.info("");
				LOGGER.info(entry.getKey());
				for (ArchimateElement archimateElement : entry.getValue()) {
					Object node = graph.insertVertex(parent, null, archimateElement.getName(), 0, 0,
							archimateElement.getName().length() * 5 + 60, 40, "applicationFunction");

					// "fontColor=000f84;shape=rectangle;strokeColor=000f84;fillColor=cce3ff"
					nodes.put(archimateElement, node);
					LOGGER.info("\t" + archimateElement.getClass().getSimpleName() + " (\"" + archimateElement.getName()
							+ "\")");
				}
			}

			for (Entry<String, List<ArchimateRelationship>> entry : modelRelationshipsByClassName.entrySet()) {
				LOGGER.info("");
				LOGGER.info(entry.getKey());
				for (ArchimateRelationship archimateRelationship : entry.getValue()) {

					Object node1 = nodes.get(archimateRelationship.getSource());
					Object node2 = nodes.get(archimateRelationship.getTarget());

					String simpleName = archimateRelationship.getClass().getSimpleName();
					graph.insertEdge(parent, null, simpleName, node1, node2, "endArrow=open;");

					LOGGER.info("\t" + archimateRelationship.getSource().getClass().getSimpleName() + " (\""
							+ archimateRelationship.getSource().getName() + "\") --> "
							+ archimateRelationship.getTarget().getClass().getSimpleName() + " (\""
							+ archimateRelationship.getTarget().getName() + "\")");
				}
			}
		} finally {
			graph.getModel().endUpdate();
		}

		try {

			mxHierarchicalLayout layout = new mxHierarchicalLayout(graph);
			layout.setOrientation(SwingConstants.NORTH);
			layout.execute(graph.getDefaultParent());

			BufferedImage image = mxCellRenderer.createBufferedImage(graph, null, 1, java.awt.Color.WHITE, true, null);
			File file = new File("C:\\Users\\Alarcos\\git\\ArchiRev\\ArchiRev\\target\\diagrams\\testJgraphX.png");
			ImageIO.write(image, "PNG", file);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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
