package es.alarcos.archirev;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.antlr.v4.runtime.ANTLRFileStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.apache.commons.io.FilenameUtils;
import org.junit.jupiter.api.Test;

import es.alarcos.archirev.parser.csharp.CSharpLexer;
import es.alarcos.archirev.parser.csharp.CSharpParser;
import es.alarcos.archirev.parser.csharp.CSharpParser.Class_definitionContext;
import es.alarcos.archirev.parser.csharp.CSharpParser.Compilation_unitContext;
import es.alarcos.archirev.parser.csharp.CSharpParser.Delegate_definitionContext;
import es.alarcos.archirev.parser.csharp.CSharpParser.Enum_definitionContext;
import es.alarcos.archirev.parser.csharp.CSharpParser.Interface_definitionContext;
import es.alarcos.archirev.parser.csharp.CSharpParser.Struct_definitionContext;

@SuppressWarnings("deprecation")
class CsharpParserTest {

	private static final String TEST_FOLDER = "D:\\Universidad\\CASOS DE ESTUDIO\\ArquiRev\\bari";
	private int numberOfCsharpFiles;
	private List<String> wrongFiles;
	private Set<String> classes = new TreeSet<String>();
	private Set<String> enums = new TreeSet<String>();
	private Set<String> interfaces = new TreeSet<String>();
	private Set<String> structs = new TreeSet<String>();
	private Set<String> delegates = new TreeSet<String>();

	@Test
	void test() throws IOException {

		// for (SyntaxError syntaxError : listener.getSyntaxErrors()) {
		// System.out.println(syntaxError.toString());
		// }

		numberOfCsharpFiles = 0;
		wrongFiles = new ArrayList<>();
		parseCsharpFolder(TEST_FOLDER);
		System.out.println("\n\n\nAnlyzed files: " + numberOfCsharpFiles);
		System.out.println("\n\n\nWrong files: " + wrongFiles.size());

		for (String fileName : wrongFiles) {
			System.out.println(fileName);
		}

		System.out.println("\n\n\n" + classes.size() + " Different unique classes:\n");
		for (String clazz : classes) {
			System.out.println(clazz);
		}

		System.out.println("\n\n\n" + enums.size() + " Different unique enums:\n");
		for (String en : enums) {
			System.out.println(en);
		}

		System.out.println("\n\n\n" + interfaces.size() + " Different unique interfaces:\n");
		for (String inter : interfaces) {
			System.out.println(inter);
		}

		System.out.println("\n\n\n" + structs.size() + " Different unique structs:\n");
		for (String st : structs) {
			System.out.println(st);
		}

		System.out.println("\n\n\n" + delegates.size() + " Different unique delegates:\n");
		for (String del : delegates) {
			System.out.println(del);
		}

	}

	private void parseCsharpFolder(String path) {
		File root = new File(path);
		File[] list = root.listFiles();
		if (list == null) {
			return;
		}
		for (File file : list) {
			if (file.isDirectory()) {
				parseCsharpFolder(file.getAbsolutePath());
			} else if ("cs".equals(FilenameUtils.getExtension(file.getAbsolutePath()))) {
				try {
					parserCsharpFile(file);
					numberOfCsharpFiles++;
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		}
	}

	private void parserCsharpFile(File file) throws IOException {
		// System.out.println("Parsing file:" + file.getAbsolutePath());

		// create temp file without directives
		File tempFile = File.createTempFile(file.getAbsolutePath(), "-withoutDirectives.tmp");
		FileWriter fw = new FileWriter(tempFile);

		List<String> lines = Files.readAllLines(file.toPath(), Charset.defaultCharset());
		for (String line : lines) {
			if (!line.trim().startsWith("#") && !line.trim().startsWith("﻿#")) {
				fw.write(line + "\n");
			}
		}

		fw.close();

		if (Files.readAllBytes(tempFile.toPath()).length == 0) {
			return;
		}

		ANTLRFileStream input = new ANTLRFileStream(tempFile.getAbsolutePath());
		CSharpLexer lexer = new CSharpLexer(input);
		CommonTokenStream tokens = new CommonTokenStream(lexer);
		CSharpParser parser = new CSharpParser(tokens);

		SyntaxErrorListener listener = new SyntaxErrorListener();
		parser.addErrorListener(listener);

		Compilation_unitContext compilation_unit = parser.compilation_unit();
		for (int i = 0; i < compilation_unit.getChildCount(); i++) {
			ParseTree tree = compilation_unit.getChild(i);
			getElements(tree);
		}

		System.out.println("Parsing.... " + file.getAbsolutePath());
		if (!listener.getSyntaxErrors().isEmpty()) {
			wrongFiles.add(file.getAbsolutePath());
		}
		for (SyntaxError syntaxError : listener.getSyntaxErrors()) {
			System.out.println("\tERROR: " + syntaxError);
		}

	}

	public void getElements(ParseTree tree) {
		for (int i = 0; i < tree.getChildCount(); i++) {
			ParseTree subTree = tree.getChild(i);
			if (subTree.getClass().equals(Class_definitionContext.class)) {
				classes.add(((Class_definitionContext) subTree).identifier().IDENTIFIER().getText());
			} else if (subTree.getClass().equals(Enum_definitionContext.class)) {
				enums.add(((Enum_definitionContext) subTree).identifier().IDENTIFIER().getText());
			} else if (subTree.getClass().equals(Interface_definitionContext.class)) {
				interfaces.add(((Interface_definitionContext) subTree).identifier().IDENTIFIER().getText());
			} else if (subTree.getClass().equals(Struct_definitionContext.class)) {
				structs.add(((Struct_definitionContext) subTree).identifier().IDENTIFIER().getText());
			} else if (subTree.getClass().equals(Delegate_definitionContext.class)) {
				delegates.add(((Delegate_definitionContext) subTree).identifier().IDENTIFIER().getText());
			}
			getElements(subTree);
		}
	}

}
