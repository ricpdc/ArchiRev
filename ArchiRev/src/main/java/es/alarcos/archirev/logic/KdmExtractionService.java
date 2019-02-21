package es.alarcos.archirev.logic;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;
import java.util.Set;
import java.util.UUID;

import javax.inject.Singleton;

import org.apache.commons.lang3.Validate;
import org.jdom2.Document;
import org.jdom2.Namespace;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import com.archimatetool.model.impl.ArchimateElement;
import com.archimatetool.model.impl.ArchimateRelationship;

import es.alarcos.archirev.model.KdmModel;
import es.alarcos.archirev.model.Source;
import es.alarcos.archirev.model.enums.ModelViewEnum;

@Singleton
@Service
public class KdmExtractionService implements Serializable {

	private static final long serialVersionUID = -4392305100176250199L;

	static Logger LOGGER = LoggerFactory.getLogger(KdmExtractionService.class);
	
	private static final String NS_KDM = "http://kdm.omg.org/kdm";
	private static final String NS_XMI = "http://www.omg.org/XMI";
	private static final String NS_XSI = "http://www.w3.org/2001/XMLSchema-instance";
	private static final String NS_ACTION = "http://kdm.omg.org/action";
	private static final String NS_CODE = "http://kdm.omg.org/code";


	private AbstractSourceCodeParser sourceCodeParser;

	private MultiValueMap<String, ArchimateElement> modelElementsByClassName = new LinkedMultiValueMap<>();
	private MultiValueMap<String, ArchimateRelationship> modelRelationshipsByClassName = new LinkedMultiValueMap<>();
	private File kdmFile;

	private Document kdmDocument;

	public KdmExtractionService() {

	}

	public void extractKdmModel(KdmModel model) {
		Set<Source> sources = model.getExtraction().getSources();
		Validate.isTrue(sources != null && !sources.isEmpty(), "There is no source as input");
		// TODO Integrate different sources into single one model
		String modelName = model.getExtraction().getName();

		for (Source source : sources) {
			modelName += ("_" + source.getName());
			model.setName(modelName);
			switch (source.getType()) {
			case JAVA_WEB_APP:
				sourceCodeParser = new JavaSourceCodeParser(model.getExtraction().getSetup());
				extractKdmModelFromSourceCode(model, source);
				break;
			case CSHARP_APP:
				sourceCodeParser = new CSharpSourceCodeParser(model.getExtraction().getSetup());
				extractKdmModelFromSourceCode(model, source);
				break;
			case JPA:
				// TODO complete this
				break;
			default:
			}
		}
	}

	private void extractKdmModelFromSourceCode(KdmModel kdmModel, Source source) {
		modelElementsByClassName = new LinkedMultiValueMap<>();
		modelRelationshipsByClassName = new LinkedMultiValueMap<>();
		try {
			org.jdom2.Element eModel = createRootElement(kdmModel.getName());
			kdmDocument = new Document(eModel);
			
			kdmDocument = sourceCodeParser.generateCodeElements(source, kdmDocument);
			//modelRelationshipsByClassName = sourceCodeParser.computeModelRelationshipsByClassName(source, modelElementsByClassName);
			kdmFile = generateKdmModel(kdmModel);
			long time = System.nanoTime();
			LOGGER.info(">Time> " + ModelViewEnum.ALL.getLabel() + ": " + (System.nanoTime() - time));
		} catch (NoClassDefFoundError | IOException e) {
			e.printStackTrace();
		}
		// if (kdmFile != null) {
		// model.setExportedPath(kdmFile.getAbsolutePath());
		// }
	}

	private File generateKdmModel(KdmModel kdmModel) {
		try {
			XMLOutputter xmlOutput = new XMLOutputter();

			// display nice nice
			xmlOutput.setFormat(Format.getPrettyFormat());
			xmlOutput.output(kdmDocument, new FileWriter(kdmModel.getExportedPath()));

			// TODO remove this local test
			String xmlFileName = "C:\\Users\\Alarcos\\git\\ArchiRev\\ArchiRev\\temp\\" + kdmModel.getName() + ".xml";
			xmlOutput.output(kdmDocument, new FileWriter(xmlFileName));

			// if (validateXmlFile(xmlFileName)) {
			return new File(kdmModel.getExportedPath());
			// }
		} catch (IOException ex) {
			ex.printStackTrace();
		}
		return null;

	}

	private org.jdom2.Element createRootElement(final String name) {
		Namespace nsKdm = Namespace.getNamespace("kdm", NS_KDM);
		Namespace nsXmi = Namespace.getNamespace("xmi", NS_XMI);
		Namespace nsXsi = Namespace.getNamespace("xsi", NS_XSI);
		Namespace nsAction = Namespace.getNamespace("action", NS_ACTION);
		Namespace nsCode = Namespace.getNamespace("code", NS_CODE);


		org.jdom2.Element eModel = new org.jdom2.Element("Segment", nsKdm);
		eModel.addNamespaceDeclaration(nsKdm);
		eModel.addNamespaceDeclaration(nsXmi);
		eModel.addNamespaceDeclaration(nsXsi);
		eModel.addNamespaceDeclaration(nsAction);
		eModel.addNamespaceDeclaration(nsCode);
		
		eModel.setAttribute("version", "2.0", nsXmi);
		//TODO fix with official KDM ecore metamodel
		eModel.setAttribute("schemaLocation", "http://kdm.omg.org/action https://www.omg.org/spec/KDM/20160201/kdm.ecore#//action http://kdm.omg.org/code https://www.omg.org/spec/KDM/20160201/kdm.ecore#//code http://kdm.omg.org/kdm https://www.omg.org/spec/KDM/20160201/kdm.ecore#//kdm", nsXsi);
		eModel.setAttribute("name", name);
	
		org.jdom2.Element eCodeModel = new org.jdom2.Element("model");
		addXmiIdentifier(eCodeModel);
		eCodeModel.setAttribute("type", "code:CodeModel", nsXsi);
		eCodeModel.setAttribute("name", name);
		
		eModel.addContent(eCodeModel);
		

		return eModel;
	}
	
	
	private org.jdom2.Element createKdmElements() {
		Namespace nsKdm = Namespace.getNamespace("kdm", NS_KDM);
		Namespace nsXmi = Namespace.getNamespace("xmi", NS_XMI);
		Namespace nsXsi = Namespace.getNamespace("xsi", NS_XSI);
		Namespace nsAction = Namespace.getNamespace("action", NS_ACTION);
		Namespace nsCode = Namespace.getNamespace("code", NS_CODE);
		
		org.jdom2.Element eElements = new org.jdom2.Element("elements", nsKdm);
//		for (ArchimateElement archimateElement : graphNodesMap.keySet()) {
//			org.jdom2.Element eElement = new org.jdom2.Element("element", nsArchimate);
//			eElement.setAttribute("identifier", archimateElement.getId());
//			eElement.setAttribute("type", archimateElement.getClass().getSimpleName(), nsXsi);
//			org.jdom2.Element eName = new org.jdom2.Element("name", nsArchimate).addContent(archimateElement.getName());
//			addLang(eName);
//			eElement.addContent(eName);
//			eElements.addContent(eElement);
//		}
		return eElements;
	}
	

	private String addIdentifier(org.jdom2.Element element) {
		String id = "id-" + UUID.randomUUID();
		element.setAttribute("identifier", id);
		return id;
	}
	
	private String addXmiIdentifier(org.jdom2.Element element) {
		Namespace nsXmi = Namespace.getNamespace("xmi", NS_XMI);
		String id = "id." + UUID.randomUUID();
		element.setAttribute("identifier", id, nsXmi);
		return id;
	}

	public AbstractSourceCodeParser getSourceCodeParser() {
		return sourceCodeParser;
	}

}
