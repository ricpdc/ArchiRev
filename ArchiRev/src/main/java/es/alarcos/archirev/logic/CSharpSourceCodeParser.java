package es.alarcos.archirev.logic;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import org.antlr.v4.runtime.ANTLRFileStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.apache.commons.lang3.StringUtils;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.TreeNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import com.archimatetool.model.impl.ArchimateElement;
import com.archimatetool.model.impl.ArchimateRelationship;

import es.alarcos.archirev.model.Source;
import es.alarcos.archirev.parser.csharp.CSharpLexer;
import es.alarcos.archirev.parser.csharp.CSharpParser;
import es.alarcos.archirev.parser.csharp.CSharpParser.Class_definitionContext;
import es.alarcos.archirev.parser.csharp.CSharpParser.Compilation_unitContext;
import es.alarcos.archirev.parser.csharp.CSharpParser.Delegate_definitionContext;
import es.alarcos.archirev.parser.csharp.CSharpParser.Enum_definitionContext;
import es.alarcos.archirev.parser.csharp.CSharpParser.IdentifierContext;
import es.alarcos.archirev.parser.csharp.CSharpParser.Interface_definitionContext;
import es.alarcos.archirev.parser.csharp.CSharpParser.Struct_definitionContext;

@SuppressWarnings("deprecation")
public class CSharpSourceCodeParser extends AbstractSourceCodeParser implements Serializable {

	private static final long serialVersionUID = 4197712088221134990L;

	private static Logger LOGGER = LoggerFactory.getLogger(CSharpSourceCodeParser.class);

	private int numberOfCsharpFiles;
	private Set<String> wrongFiles;
	private Set<String> classes = new TreeSet<String>();
	private Set<String> enums = new TreeSet<String>();
	private Set<String> interfaces = new TreeSet<String>();
	private Set<String> structs = new TreeSet<String>();
	private Set<String> delegates = new TreeSet<String>();
	private Set<String> delcaredTypeNames = new TreeSet<String>();
	private Set<String> callsToCallableUnits = new TreeSet<String>();

	private static File elementsLog;
	private static File relationshipsLog;
	
	
	private Hashtable<String, TreeNode> directories = new Hashtable<>();
	private TreeNode treeRoot;

	private MultiValueMap<String, ArchimateElement> modelElementsByClassName;

	public CSharpSourceCodeParser(final String setup) {
		super(setup);
	}

	@Override
	public MultiValueMap<String, ArchimateElement> computeModelElementsByClassName(Source zipSource)
			throws ZipException, IOException {
		numberOfCsharpFiles = 0;
		wrongFiles = new HashSet<>();
		MultiValueMap<String, ArchimateElement> modelElementsByClassName = new LinkedMultiValueMap<>();

		File tempZipFile = File.createTempFile("zipFile", ".tmp", null);
		FileOutputStream fos = new FileOutputStream(tempZipFile);
		fos.write(zipSource.getFile());
		fos.close();

		ZipFile zipFile = getZipFile(tempZipFile);

		@SuppressWarnings("unchecked")
		Enumeration<ZipEntry> entries = (Enumeration<ZipEntry>) zipFile.entries();

		elementsLog = new File("C:\\Temp\\elements.txt");
		if (elementsLog.exists()) {
			elementsLog.delete();
		}
		elementsLog.createNewFile();
		try {
			Files.write(elementsLog.toPath(),
					"path;name;linesOfCode;parsingTime;numberOfElements;elementsGeneratingTime\n".getBytes(),
					StandardOpenOption.APPEND);
		} catch (IOException e) {
			e.printStackTrace();
		}

		while (entries.hasMoreElements()) {
			ZipEntry zipEntry = entries.nextElement();
			if (zipEntry.isDirectory())
				continue;

			if (!zipEntry.getName().endsWith(".cs")) {
				continue;
			}

			try {
				parseCsharpFile(zipFile, zipEntry, modelElementsByClassName);
				numberOfCsharpFiles++;
			} catch (Exception ex) {
				LOGGER.error(zipEntry.getName() + " cannot be parsed");
				// wrongFiles.add(zipEntry.getName());
			}

		}

		logStats();

		return modelElementsByClassName;
	}
	
	
	@Override
	public Document generateKdmCodeElements(Source zipSource, Document document)
			throws ZipException, IOException {
		numberOfCsharpFiles = 0;
		wrongFiles = new HashSet<>();
		modelElementsByClassName = new LinkedMultiValueMap<>();

		File tempZipFile = File.createTempFile("zipFile", ".tmp", null);
		FileOutputStream fos = new FileOutputStream(tempZipFile);
		fos.write(zipSource.getFile());
		fos.close();

		ZipFile zipFile = getZipFile(tempZipFile);

		@SuppressWarnings("unchecked")
		Enumeration<ZipEntry> entries = (Enumeration<ZipEntry>) zipFile.entries();

		elementsLog = new File("C:\\Temp\\elements-kdm.txt");
		if (elementsLog.exists()) {
			elementsLog.delete();
		}
		elementsLog.createNewFile();
		try {
			Files.write(elementsLog.toPath(),
					"path;name;linesOfCode;parsingTime;numberOfElements;elementsGeneratingTime\n".getBytes(),
					StandardOpenOption.APPEND);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		Element model = document.getRootElement().getChild("model");

		
		directories = new Hashtable<String, TreeNode>();
		treeRoot = new DefaultTreeNode(new ZipEntry(model.getAttributeValue("name")), null);
		while (entries.hasMoreElements()) {
			ZipEntry entry = entries.nextElement();
			if (entry.isDirectory()) {
				directories.put(entry.getName(), new DefaultTreeNode(entry, getParentDirectory(entry)));
			}
		}
		
		entries = (Enumeration<ZipEntry>) zipFile.entries();
		while (entries.hasMoreElements()) {
			ZipEntry entry = entries.nextElement();
			if (!entry.isDirectory() && entry.getName().endsWith(".cs")) {
				new DefaultTreeNode(entry, getParentDirectory(entry));
			}
		}
		
		processEntryToKdmCodeElement(treeRoot, model, zipFile);
		
//		while (entries.hasMoreElements()) {
//			ZipEntry zipEntry = entries.nextElement();
//			if (zipEntry.isDirectory()) {
//				Element packageEntry = new Element("package");
//				packageEntry.setText(zipEntry.getName());
//				model.addContent(packageEntry);
//			}
//
//			if (!zipEntry.getName().endsWith(".cs")) {
//				continue;
//			}
//
//			try {
//				Element fileEntry = new Element("csharp-file");
//				fileEntry.setText(zipEntry.getName());
//				model.addContent(fileEntry);
//				parseCsharpFileAndGenerateKdm(zipFile, zipEntry, modelElementsByClassName, document);
//				numberOfCsharpFiles++;
//			} catch (Exception ex) {
//				LOGGER.error(zipEntry.getName() + " cannot be parsed");
//				// wrongFiles.add(zipEntry.getName());
//			}
//
//		}

		logStats();

		return document;
	}

	private void processEntryToKdmCodeElement(TreeNode treeNode, Element element, ZipFile zipFile) throws IOException {
		Namespace nsXsi = Namespace.getNamespace("xsi", NS_XSI);
		ZipEntry zipEntry = (ZipEntry)treeNode.getData();
		if(!treeNode.isLeaf()) {
			for (TreeNode childNode : treeNode.getChildren()) {
				Element packageE = new Element("codeElement");
				packageE.setAttribute("type", "code:Package", nsXsi);
				addXmiIdentifier(packageE);
				packageE.setAttribute("name", zipEntry.getName());
				element.addContent(packageE);
				processEntryToKdmCodeElement(childNode, packageE, zipFile);
			}
		}
		else if(zipEntry.getName().endsWith(".cs")) {
			//process c# file
			Element compilationUnitE = new Element("codeElement");
			compilationUnitE.setAttribute("type", "code:CompilationUnit", nsXsi);
			addXmiIdentifier(compilationUnitE);
			compilationUnitE.setAttribute("name", zipEntry.getName());
			element.addContent(compilationUnitE);
			parseCsharpFileForKdmGeneration(zipFile, zipEntry, compilationUnitE, modelElementsByClassName);
		}
	}
	
	private TreeNode getParentDirectory(ZipEntry entry) {
		String entryName = entry.getName();
		String parentName = new File(entryName).getParent();
		if (parentName == null) {
			return treeRoot;
		}
		parentName = parentName.replaceAll("\\\\", "/") + "/";
		if (directories.get(parentName) == null) {
			return treeRoot;
		}
		return directories.get(parentName);
	}
	

	private void logStats() {
		LOGGER.info("\n\n\nAnlyzed files: " + numberOfCsharpFiles);
		LOGGER.info("\n\n\nWrong files: " + wrongFiles.size());

		for (String fileName : wrongFiles) {
			LOGGER.info(fileName);
		}

		LOGGER.info("\n\n\n" + classes.size() + " Different unique classes:\n");
		for (String clazz : classes) {
			LOGGER.info(clazz);
		}

		LOGGER.info("\n\n\n" + enums.size() + " Different unique enums:\n");
		for (String en : enums) {
			LOGGER.info(en);
		}

		LOGGER.info("\n\n\n" + interfaces.size() + " Different unique interfaces:\n");
		for (String inter : interfaces) {
			LOGGER.info(inter);
		}

		LOGGER.info("\n\n\n" + structs.size() + " Different unique structs:\n");
		for (String st : structs) {
			LOGGER.info(st);
		}

		System.out.println("\n\n\n" + delegates.size() + " Different unique delegates:\n");
		for (String del : delegates) {
			System.out.println(del);
		}
	}

	private void parseCsharpFile(final ZipFile zipFile, final ZipEntry zipEntry,
			final MultiValueMap<String, ArchimateElement> modelElementsByClassName) throws IOException {
		long time = System.nanoTime();
		String zipEntryName = zipEntry.getName();
		String simpleClassName = getSimpleClassName(zipEntryName);

		String msg = "" + zipEntryName + ";" + simpleClassName + ";";

		for (String exclusion_tag : exclusions) {
			if (simpleClassName.contains(exclusion_tag)) {
				return;
			}
		}

		File tempFile = getTempFileWithoutDirectives(zipFile, zipEntry);
		if (Files.readAllBytes(tempFile.toPath()).length == 0) {
			return;
		}

		ANTLRFileStream grammarInput = new ANTLRFileStream(tempFile.getAbsolutePath());
		CSharpLexer lexer = new CSharpLexer(grammarInput);
		CommonTokenStream tokens = new CommonTokenStream(lexer);
		CSharpParser parser = new CSharpParser(tokens);

		SyntaxErrorListener listener = new SyntaxErrorListener();
		parser.addErrorListener(listener);

		delcaredTypeNames = new TreeSet<String>();

		Compilation_unitContext compilation_unit = parser.compilation_unit();
		for (int i = 0; i < compilation_unit.getChildCount(); i++) {
			ParseTree tree = compilation_unit.getChild(i);
			getDeclaredTypes(tree);
		}

		// LOGGER.info("Parsing.... " + zipEntryName);
		if (listener.getSyntaxErrors().isEmpty()) {
			Set<ArchimateElementEnum> uniqueElements = new HashSet<>();
			boolean mappedSuperclass = false;

			List<String> declaredTypeTags = new ArrayList<>();
			for (String className : delcaredTypeNames) {
				declaredTypeTags.addAll(Arrays.asList(StringUtils.splitByCharacterTypeCamelCase(className)));
			}

			for (String classTag : declaredTypeTags) {
				List<ArchimateElementEnum> elementList = mapping.get(classTag);
				if (elementList != null) {
					uniqueElements.addAll(elementList);
					if (!mappedSuperclass && MAPPED_SUPERCLASS_ANNOTATION.equals(classTag)) {
						mappedSuperclass = true;
					}
				} else {
					LOGGER.debug(String.format("Not mapped annotation \"%s\" in class %s", classTag, zipEntryName));
				}
			}

			msg += (Files.lines(tempFile.toPath()).count() + ";");
			msg += ((System.nanoTime() - time) + ";");
			time = System.nanoTime();

			createModelElements(modelElementsByClassName, simpleClassName, uniqueElements, mappedSuperclass);

			List<ArchimateElement> elementsCreated = modelElementsByClassName
					.get(getSimpleClassName(zipEntry.getName()));
			int numberOfElements = elementsCreated != null ? elementsCreated.size() : 0;
			msg += (numberOfElements + ";");
			msg += ((System.nanoTime() - time) + ";\n");
			try {
				Files.write(elementsLog.toPath(), msg.getBytes(), StandardOpenOption.APPEND);
			} catch (IOException e) {
				e.printStackTrace();
			}

		} else {
			wrongFiles.add(zipEntryName);
			for (SyntaxError syntaxError : listener.getSyntaxErrors()) {
				LOGGER.error("\tSYNTACTIC ERROR: " + syntaxError);
			}
		}

	}
	
	
	private void parseCsharpFileForKdmGeneration(final ZipFile zipFile, final ZipEntry zipEntry,
			Element element, final MultiValueMap<String, ArchimateElement> modelElementsByClassName) throws IOException {
		long time = System.nanoTime();
		String zipEntryName = zipEntry.getName();
		String simpleClassName = getSimpleClassName(zipEntryName);

		String msg = "" + zipEntryName + ";" + simpleClassName + ";";

		for (String exclusion_tag : exclusions) {
			if (simpleClassName.contains(exclusion_tag)) {
				return;
			}
		}

		File tempFile = getTempFileWithoutDirectives(zipFile, zipEntry);
		if (Files.readAllBytes(tempFile.toPath()).length == 0) {
			return;
		}

		ANTLRFileStream grammarInput = new ANTLRFileStream(tempFile.getAbsolutePath());
		CSharpLexer lexer = new CSharpLexer(grammarInput);
		CommonTokenStream tokens = new CommonTokenStream(lexer);
		CSharpParser parser = new CSharpParser(tokens);

		SyntaxErrorListener listener = new SyntaxErrorListener();
		parser.addErrorListener(listener);

		delcaredTypeNames = new TreeSet<String>();

		Compilation_unitContext compilation_unit = parser.compilation_unit();
		for (int i = 0; i < compilation_unit.getChildCount(); i++) {
			ParseTree tree = compilation_unit.getChild(i);
			getDeclaredTypes(tree);
		}

		// LOGGER.info("Parsing.... " + zipEntryName);
		if (listener.getSyntaxErrors().isEmpty()) {
			Set<ArchimateElementEnum> uniqueElements = new HashSet<>();
			boolean mappedSuperclass = false;
			
			Namespace nsXsi = Namespace.getNamespace("xsi", NS_XSI);
			
			List<String> declaredTypeTags = new ArrayList<>();
			for (String className : delcaredTypeNames) {
				declaredTypeTags.addAll(Arrays.asList(StringUtils.splitByCharacterTypeCamelCase(className)));
				//<codeElement xsi:type="code:ClassUnit" name="class A" exportKind="unknown"/>
				Element classE = new Element("codeElement");
				classE.setAttribute("type", "code:ClassUnit", nsXsi);
				classE.setAttribute("name", className);
				classE.setAttribute("exportKind", "unknown");
				//addXmiIdentifier(classE);
				element.addContent(classE);
			}

			for (String classTag : declaredTypeTags) {
//				List<ArchimateElementEnum> elementList = mapping.get(classTag);
//				if (elementList != null) {
//					uniqueElements.addAll(elementList);
//					if (!mappedSuperclass && MAPPED_SUPERCLASS_ANNOTATION.equals(classTag)) {
//						mappedSuperclass = true;
//					}
//				} else {
//					LOGGER.debug(String.format("Not mapped annotation \"%s\" in class %s", classTag, zipEntryName));
//				}
				//<annotation text="dsafasfasfasdf"/>
				
				Element annotation = new Element("annotation");
				annotation.setAttribute("text", classTag);
				element.addContent(annotation);
			}

			msg += (Files.lines(tempFile.toPath()).count() + ";");
			msg += ((System.nanoTime() - time) + ";");
			time = System.nanoTime();

			createModelElements(modelElementsByClassName, simpleClassName, uniqueElements, mappedSuperclass);

			List<ArchimateElement> elementsCreated = modelElementsByClassName
					.get(getSimpleClassName(zipEntry.getName()));
			int numberOfElements = elementsCreated != null ? elementsCreated.size() : 0;
			msg += (numberOfElements + ";");
			msg += ((System.nanoTime() - time) + ";\n");
			try {
				Files.write(elementsLog.toPath(), msg.getBytes(), StandardOpenOption.APPEND);
			} catch (IOException e) {
				e.printStackTrace();
			}

		} else {
			wrongFiles.add(zipEntryName);
			for (SyntaxError syntaxError : listener.getSyntaxErrors()) {
				LOGGER.error("\tSYNTACTIC ERROR: " + syntaxError);
			}
		}

	}

	
	
	private void parseCsharpFileAndGenerateKdm(final ZipFile zipFile, final ZipEntry zipEntry,
			final MultiValueMap<String, ArchimateElement> modelElementsByClassName, Document document) throws IOException {
		long time = System.nanoTime();
		String zipEntryName = zipEntry.getName();
		String simpleClassName = getSimpleClassName(zipEntryName);

		String msg = "" + zipEntryName + ";" + simpleClassName + ";";

//		for (String exclusion_tag : exclusions) {
//			if (simpleClassName.contains(exclusion_tag)) {
//				return;
//			}
//		}

		File tempFile = getTempFileWithoutDirectives(zipFile, zipEntry);
		if (Files.readAllBytes(tempFile.toPath()).length == 0) {
			return;
		}

		ANTLRFileStream grammarInput = new ANTLRFileStream(tempFile.getAbsolutePath());
		CSharpLexer lexer = new CSharpLexer(grammarInput);
		CommonTokenStream tokens = new CommonTokenStream(lexer);
		CSharpParser parser = new CSharpParser(tokens);

		SyntaxErrorListener listener = new SyntaxErrorListener();
		parser.addErrorListener(listener);

		delcaredTypeNames = new TreeSet<String>();

		Compilation_unitContext compilation_unit = parser.compilation_unit();
		for (int i = 0; i < compilation_unit.getChildCount(); i++) {
			ParseTree tree = compilation_unit.getChild(i);
			getDeclaredTypes(tree);
		}

		// LOGGER.info("Parsing.... " + zipEntryName);
		if (listener.getSyntaxErrors().isEmpty()) {
			Set<ArchimateElementEnum> uniqueElements = new HashSet<>();
			boolean mappedSuperclass = false;

			List<String> declaredTypeTags = new ArrayList<>();
			for (String className : delcaredTypeNames) {
				declaredTypeTags.addAll(Arrays.asList(StringUtils.splitByCharacterTypeCamelCase(className)));
			}

			for (String classTag : declaredTypeTags) {
				List<ArchimateElementEnum> elementList = mapping.get(classTag);
				if (elementList != null) {
					uniqueElements.addAll(elementList);
					if (!mappedSuperclass && MAPPED_SUPERCLASS_ANNOTATION.equals(classTag)) {
						mappedSuperclass = true;
					}
				} else {
					LOGGER.debug(String.format("Not mapped annotation \"%s\" in class %s", classTag, zipEntryName));
				}
			}

			msg += (Files.lines(tempFile.toPath()).count() + ";");
			msg += ((System.nanoTime() - time) + ";");
			time = System.nanoTime();

			createModelElements(modelElementsByClassName, simpleClassName, uniqueElements, mappedSuperclass);

			List<ArchimateElement> elementsCreated = modelElementsByClassName
					.get(getSimpleClassName(zipEntry.getName()));
			int numberOfElements = elementsCreated != null ? elementsCreated.size() : 0;
			msg += (numberOfElements + ";");
			msg += ((System.nanoTime() - time) + ";\n");
			try {
				Files.write(elementsLog.toPath(), msg.getBytes(), StandardOpenOption.APPEND);
			} catch (IOException e) {
				e.printStackTrace();
			}

		} else {
			wrongFiles.add(zipEntryName);
			for (SyntaxError syntaxError : listener.getSyntaxErrors()) {
				LOGGER.error("\tSYNTACTIC ERROR: " + syntaxError);
			}
		}

	}
	
	private File getTempFileWithoutDirectives(final ZipFile zipFile, final ZipEntry zipEntry) throws IOException {
		File tempFile = File.createTempFile(zipFile.getName(), "-withoutDirectives.tmp");
		FileWriter fw = new FileWriter(tempFile);

		InputStream is = null;
		BufferedReader br = null;

		try {
			is = zipFile.getInputStream(zipEntry);
			br = new BufferedReader(new InputStreamReader(is));
			String line = null;
			while ((line = br.readLine()) != null) {
				if (!line.trim().startsWith("#") && !line.trim().startsWith("﻿#")) {
					fw.write(line + "\n");
				}
			}
		} catch (IOException ioe) {
			LOGGER.error("Exception while reading input " + ioe);
		} finally {
			// close the streams using close method
			try {
				if (br != null) {
					br.close();
				}
				if (fw != null) {
					fw.close();
				}
			} catch (IOException ioe) {
				LOGGER.error("Error while closing stream: " + ioe);
			}
		}
		return tempFile;
	}

	private void getDeclaredTypes(ParseTree tree) {
		for (int i = 0; i < tree.getChildCount(); i++) {
			ParseTree subTree = tree.getChild(i);
			if (subTree.getClass().equals(Class_definitionContext.class)) {
				String name = ((Class_definitionContext) subTree).identifier().IDENTIFIER().getText();
				classes.add(name);
				delcaredTypeNames.add(name);
			} else if (subTree.getClass().equals(Enum_definitionContext.class)) {
				String name = ((Enum_definitionContext) subTree).identifier().IDENTIFIER().getText();
				enums.add(name);
				delcaredTypeNames.add(name);
			} else if (subTree.getClass().equals(Interface_definitionContext.class)) {
				String name = ((Interface_definitionContext) subTree).identifier().IDENTIFIER().getText();
				interfaces.add(name);
				delcaredTypeNames.add(name);
			} else if (subTree.getClass().equals(Struct_definitionContext.class)) {
				String name = ((Struct_definitionContext) subTree).identifier().IDENTIFIER().getText();
				structs.add(name);
				delcaredTypeNames.add(name);
			} else if (subTree.getClass().equals(Delegate_definitionContext.class)) {
				String name = ((Delegate_definitionContext) subTree).identifier().IDENTIFIER().getText();
				delegates.add(name);
				delcaredTypeNames.add(name);
			}
			getDeclaredTypes(subTree);
		}
	}

	@Override
	protected String getFormattedName(String longZipEntryName) {
		String simpleClassName = StringUtils.join(StringUtils.splitByCharacterTypeCamelCase(longZipEntryName), " ");
		return simpleClassName;
	}

	private String getSimpleClassName(String longZipEntryName) {
		String withoutExtenstion = longZipEntryName.substring(0, longZipEntryName.lastIndexOf("."));
		String simpleClassName = withoutExtenstion.substring(withoutExtenstion.lastIndexOf("/") + 1);
		return simpleClassName;
	}

	@Override
	public MultiValueMap<String, ArchimateRelationship> computeModelRelationshipsByClassName(Source zipSource,
			MultiValueMap<String, ArchimateElement> modelElementsByClassName) throws ZipException, IOException {
		MultiValueMap<String, ArchimateRelationship> modelRelationshipsByClassName = new LinkedMultiValueMap<>();

		numberOfCsharpFiles = 0;
		wrongFiles = new HashSet<>();

		File tempZipFile = File.createTempFile("zipFile", ".tmp", null);
		FileOutputStream fos = new FileOutputStream(tempZipFile);
		fos.write(zipSource.getFile());
		fos.close();

		ZipFile zipFile = getZipFile(tempZipFile);

		relationshipsLog = new File("C:\\Temp\\relationships.txt");
		if (relationshipsLog.exists()) {
			relationshipsLog.delete();
		}
		relationshipsLog.createNewFile();
		try {
			Files.write(relationshipsLog.toPath(),
					"path;name;linesOfCode;parsingTime;numberOfRelationships;relationshipsGeneratingTime\n".getBytes(),
					StandardOpenOption.APPEND);
		} catch (IOException e) {
			e.printStackTrace();
		}

		@SuppressWarnings("unchecked")
		Enumeration<ZipEntry> entries = (Enumeration<ZipEntry>) zipFile.entries();
		while (entries.hasMoreElements()) {
			ZipEntry zipEntry = entries.nextElement();
			if (zipEntry.isDirectory())
				continue;

			if (!zipEntry.getName().endsWith(".cs")) {
				continue;
			}

			try {

				parserCsharpFileForRelationships(zipFile, zipEntry, modelElementsByClassName,
						modelRelationshipsByClassName);
				numberOfCsharpFiles++;
			} catch (Exception ex) {
				LOGGER.error(zipEntry.getName() + " cannot be parsed");
				ex.printStackTrace();
			}

		}

		return modelRelationshipsByClassName;
	}

	private void parserCsharpFileForRelationships(final ZipFile zipFile, final ZipEntry zipEntry,
			final MultiValueMap<String, ArchimateElement> modelElementsByClassName,
			final MultiValueMap<String, ArchimateRelationship> modelRelationshipsByClassName) throws IOException {
		long time = System.nanoTime();
		String msg = "" + zipEntry.getName() + ";" + getSimpleClassName(zipEntry.getName()) + ";";
		File tempFile = getTempFileWithoutDirectives(zipFile, zipEntry);
		if (Files.readAllBytes(tempFile.toPath()).length == 0) {
			return;
		}

		ANTLRFileStream grammarInput = new ANTLRFileStream(tempFile.getAbsolutePath());
		CSharpLexer lexer = new CSharpLexer(grammarInput);
		CommonTokenStream tokens = new CommonTokenStream(lexer);
		CSharpParser parser = new CSharpParser(tokens);

		SyntaxErrorListener listener = new SyntaxErrorListener();
		parser.addErrorListener(listener);

		callsToCallableUnits = new TreeSet<String>();
		Compilation_unitContext compilation_unit = parser.compilation_unit();
		for (int i = 0; i < compilation_unit.getChildCount(); i++) {
			ParseTree tree = compilation_unit.getChild(i);
			getCallToCallableUnits(tree);
		}

		String zipEntrySimpleName = getSimpleClassName(zipEntry.getName());
		// LOGGER.info("Parsing.... " + zipEntryName);
		if (listener.getSyntaxErrors().isEmpty()) {
			Set<String> visitedRelationships = new HashSet<>();
			msg += (Files.lines(tempFile.toPath()).count() + ";");
			msg += ((System.nanoTime() - time) + ";");
			time = System.nanoTime();
			for (String referencedClass : callsToCallableUnits) {

				List<ArchimateElement> sourceElements = modelElementsByClassName.get(zipEntrySimpleName);
				List<ArchimateElement> targetElements = modelElementsByClassName.get(referencedClass);

				createModelRelationships(modelRelationshipsByClassName, visitedRelationships, zipEntrySimpleName,
						sourceElements, targetElements);

			}
			List<ArchimateRelationship> relationshipsCreated = modelRelationshipsByClassName
					.get(getSimpleClassName(zipEntry.getName()));
			int numberOfRelationships = relationshipsCreated != null ? relationshipsCreated.size() : 0;
			msg += (numberOfRelationships + ";");
			msg += ((System.nanoTime() - time) + ";\n");
			try {
				Files.write(relationshipsLog.toPath(), msg.getBytes(), StandardOpenOption.APPEND);
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			wrongFiles.add(zipEntry.getName());
			for (SyntaxError syntaxError : listener.getSyntaxErrors()) {
				LOGGER.error("\tSYNTACTIC ERROR: " + syntaxError);
			}
		}
	}

	private void getCallToCallableUnits(ParseTree tree) {
		for (int i = 0; i < tree.getChildCount(); i++) {
			ParseTree subTree = tree.getChild(i);
			if (subTree.getClass().equals(IdentifierContext.class)) {
				IdentifierContext idContext = (IdentifierContext) subTree;
				String name = idContext.IDENTIFIER() != null ? idContext.IDENTIFIER().getText() : idContext.getText();
				callsToCallableUnits.add(name);
				// LOGGER.info(name);
			}
			getCallToCallableUnits(subTree);
		}
	}

}
