package es.alarcos.archirev;

import java.io.File;
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
import java.util.Iterator;

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
import org.zeroturnaround.zip.ZipUtil;

import the.bytecode.club.bytecodeviewer.DecompilerSettings;
import the.bytecode.club.bytecodeviewer.decompilers.CFRDecompiler.Settings;
import the.bytecode.club.bytecodeviewer.decompilers.Decompiler;

class DecompilerTest {

	private final Decompiler decompiler = Decompiler.CFR;
	private final DecompilerSettings settings = new DecompilerSettings(decompiler);

	private final String warPath = "C:\\Users\\Alarcos\\Downloads\\ArchiRev.war";

	@Test
	void testDecompileCFRSingleDocument() {

		final String inPath = "C:\\Users\\Alarcos\\Downloads\\SessionController.class";
		final String outPath = "C:\\Users\\Alarcos\\Downloads\\SessionController.java";

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

	@Test
	public void testAnnotationParsing() {
		try {
			File zipFile = new File(warPath);
			File rootFolder = zipFile.getParentFile();
			String inputBaseName = FilenameUtils.getBaseName(zipFile.getName());
			File classFolder = new File(rootFolder + File.separator + inputBaseName + "_output_class");
			if (!classFolder.exists()) {
				classFolder.mkdir();
			}
			Path classFolderPath = Paths.get(classFolder.getAbsolutePath());

			ZipUtil.unpack(zipFile, classFolder);

			URLClassLoader classLoader = new URLClassLoader(
					new URL[] { new URL("file:///" + classFolder.getAbsolutePath()) });

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
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public String[] generateMainMethod() {
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

	public String doClass(DCCommonState dcCommonState, byte[] content1) throws Exception {
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

	public DecompilerSettings getSettings() {
		return settings;
	}

}
