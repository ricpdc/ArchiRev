package es.alarcos.archirev.logic;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import org.apache.bcel.classfile.AnnotationEntry;
import org.apache.bcel.classfile.ClassFormatException;
import org.apache.bcel.classfile.ClassParser;
import org.apache.bcel.classfile.Constant;
import org.apache.bcel.classfile.JavaClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import com.archimatetool.model.impl.ArchimateElement;
import com.archimatetool.model.impl.ArchimateFactory;
import com.archimatetool.model.impl.ArchimateRelationship;
import com.archimatetool.model.impl.CompositionRelationship;

import es.alarcos.archirev.model.Source;

public class JavaSourceCodeParser extends AbstractSourceCodeParser implements Serializable {

	private static final long serialVersionUID = -7546016897450591834L;

	private static Logger LOGGER = LoggerFactory.getLogger(JavaSourceCodeParser.class);

	public JavaSourceCodeParser(final String setup) {
		super(setup);
	}

	@Override
	public MultiValueMap<String, ArchimateElement> computeModelElementsByClassName(Source warSource)
			throws ZipException, IOException {
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
				LOGGER.debug("Entry: " + entry.getName());
				// LOGGER.debug("javaClass: " + javaClass);
			} catch (ClassFormatException | IOException e) {
				LOGGER.error(entry.getName() + " cannot be parsed");
				javaClass = null;
				continue;
			}

			Set<ArchimateElementEnum> uniqueElements = new HashSet<>();
			boolean mappedSuperclass = false;
			for (AnnotationEntry annotationEntry : javaClass.getAnnotationEntries()) {
				String annotationType = annotationEntry.getAnnotationType();

				String annotation = annotationType.substring(annotationType.lastIndexOf("/") + 1,
						annotationType.length() - 1);
				List<ArchimateElementEnum> elementList = mapping.get(annotation);
				if (elementList != null) {
					uniqueElements.addAll(elementList);
					if (!mappedSuperclass && MAPPED_SUPERCLASS_ANNOTATION.equals(annotation)) {
						mappedSuperclass = true;
					}
				} else {
					LOGGER.debug(String.format("Not mapped annotation \"%s\" in class %s", annotationType,
							javaClass.getClassName()));
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
				boolean existent = false;
				List<ArchimateElement> archimateElements = modelElementsByClassName.get(javaClass.getClassName());
				for (int i = 0; archimateElements != null && i < archimateElements.size(); i++) {
					if (archimateElements.get(i).getClass().equals(elementToBeAdded.getClass())) {
						existent = true;
						break;
					}

				}
				if (!existent) {
					modelElementsByClassName.add(javaClass.getClassName(), elementToBeAdded);
				}
			}
		}
		return modelElementsByClassName;
	}

	@Override
	public MultiValueMap<String, ArchimateRelationship> computeModelRelationshipsByClassName(Source warSource,
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
			if (entry.isDirectory()) {
				continue;
			}

			if (!entry.getName().endsWith(".class")) {
				continue;
			}

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

}
