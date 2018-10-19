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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashSet;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import com.archimatetool.model.impl.ArchimateElement;
import com.archimatetool.model.impl.ArchimateFactory;
import com.archimatetool.model.impl.ArchimateRelationship;
import com.archimatetool.model.impl.CompositionRelationship;

import es.alarcos.archirev.model.Source;
import es.alarcos.archirev.parser.csharp.CSharpLexer;
import es.alarcos.archirev.parser.csharp.CSharpParser;
import es.alarcos.archirev.parser.csharp.CSharpParser.Class_definitionContext;
import es.alarcos.archirev.parser.csharp.CSharpParser.Class_typeContext;
import es.alarcos.archirev.parser.csharp.CSharpParser.Compilation_unitContext;
import es.alarcos.archirev.parser.csharp.CSharpParser.Delegate_definitionContext;
import es.alarcos.archirev.parser.csharp.CSharpParser.Enum_definitionContext;
import es.alarcos.archirev.parser.csharp.CSharpParser.IdentifierContext;
import es.alarcos.archirev.parser.csharp.CSharpParser.Interface_definitionContext;
import es.alarcos.archirev.parser.csharp.CSharpParser.Qualified_identifierContext;
import es.alarcos.archirev.parser.csharp.CSharpParser.Struct_definitionContext;
import es.alarcos.archirev.parser.csharp.CSharpParser.Variable_declaratorContext;

@SuppressWarnings("deprecation")
public class CSharpSourceCodeParser extends AbstractSourceCodeParser implements Serializable {

	private static final long serialVersionUID = 4197712088221134990L;

	private static Logger LOGGER = LoggerFactory.getLogger(CSharpSourceCodeParser.class);

	private int numberOfCsharpFiles;
	private List<String> wrongFiles;
	private Set<String> classes = new TreeSet<String>();
	private Set<String> enums = new TreeSet<String>();
	private Set<String> interfaces = new TreeSet<String>();
	private Set<String> structs = new TreeSet<String>();
	private Set<String> delegates = new TreeSet<String>();
	private Set<String> delcaredTypeNames = new TreeSet<String>();
	private Set<String> callsToCallableUnits = new TreeSet<String>();

	public CSharpSourceCodeParser(final String setup) {
		super(setup);
	}

	@Override
	public MultiValueMap<String, ArchimateElement> computeModelElementsByClassName(Source zipSource)
			throws ZipException, IOException {
		numberOfCsharpFiles = 0;
		wrongFiles = new ArrayList<>();
		MultiValueMap<String, ArchimateElement> modelElementsByClassName = new LinkedMultiValueMap<>();

		File tempZipFile = File.createTempFile("zipFile", ".tmp", null);
		FileOutputStream fos = new FileOutputStream(tempZipFile);
		fos.write(zipSource.getFile());
		fos.close();

		ZipFile zipFile = getZipFile(tempZipFile);

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
				parserCsharpFile(zipFile, zipEntry, modelElementsByClassName);
				numberOfCsharpFiles++;
			} catch (Exception ex) {
				LOGGER.error(zipEntry.getName() + " cannot be parsed");
			}

		}

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

		return modelElementsByClassName;
	}

	private void parserCsharpFile(final ZipFile zipFile, final ZipEntry zipEntry,
			final MultiValueMap<String, ArchimateElement> modelElementsByClassName) throws IOException {
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

		String zipEntryName = zipEntry.getName();
		//LOGGER.info("Parsing.... " + zipEntryName);
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

			createModelElements(modelElementsByClassName, getSimpleClassName(zipEntryName), uniqueElements,
					mappedSuperclass);

		} else {
			wrongFiles.add(zipEntryName);
			for (SyntaxError syntaxError : listener.getSyntaxErrors()) {
				LOGGER.error("\tERROR: " + syntaxError);
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
		wrongFiles = new ArrayList<>();

		File tempZipFile = File.createTempFile("zipFile", ".tmp", null);
		FileOutputStream fos = new FileOutputStream(tempZipFile);
		fos.write(zipSource.getFile());
		fos.close();

		ZipFile zipFile = getZipFile(tempZipFile);

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
			for (String referencedClass : callsToCallableUnits) {

				List<ArchimateElement> sourceElements = modelElementsByClassName.get(zipEntrySimpleName);
				List<ArchimateElement> targetElements = modelElementsByClassName.get(referencedClass);

				createModelRelationships(modelRelationshipsByClassName, visitedRelationships, zipEntrySimpleName,
						sourceElements, targetElements);

			}
		} else {
			wrongFiles.add(zipEntry.getName());
			for (SyntaxError syntaxError : listener.getSyntaxErrors()) {
				LOGGER.error("\tERROR: " + syntaxError);
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
				//LOGGER.info(name);
			}
			getCallToCallableUnits(subTree);
		}
	}

}
