package es.alarcos.archirev.controller;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.Timestamp;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.component.UIOutput;
import javax.faces.context.FacesContext;
import javax.faces.event.AjaxBehaviorEvent;

import org.apache.bcel.classfile.ClassFormatException;
import org.apache.bcel.classfile.ClassParser;
import org.apache.bcel.classfile.JavaClass;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.primefaces.context.RequestContext;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.event.ToggleEvent;
import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.TreeNode;
import org.primefaces.model.UploadedFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import com.google.common.collect.Maps;

import es.alarcos.archirev.model.Project;
import es.alarcos.archirev.model.Source;
import es.alarcos.archirev.model.enums.SourceConcernEnum;
import es.alarcos.archirev.model.enums.SourceEnum;

@ManagedBean(name = "sourcesController")
@Controller
@ViewScoped
public class SourcesController extends AbstractController {

	private static final long serialVersionUID = -1637522119751630382L;

	private static Logger logger = LoggerFactory.getLogger(SourcesController.class);

	@Autowired
	private SessionController sessionController;

	private SourceConcernEnum sourceConcern;
	private SourceEnum sourceType;
	private String allowTypes="/(\\.|\\/)(zip|rar|7z)$/";

	private Source selectedSource;
	private transient ZipFile zipFile;
	private transient TreeNode sourceRoot;
	private transient ZipEntry selectedNode;
	private String documentText;
	private String documentTextType;
	private transient Hashtable<String, TreeNode> directories = new Hashtable<>();

	private transient Map<SourceConcernEnum, Set<SourceEnum>> sourcesMap = Maps.newHashMap();


	public SourcesController() {
		super();
	}

	@PostConstruct
	public void init() {
		super.init();

		loadSourceTypes();

	}

	private void loadSourceTypes() {
		sourcesMap = Maps.newHashMap();
		if (sourcesMap.isEmpty()) {
			for (SourceConcernEnum concern : SourceConcernEnum.values()) {
				Set<SourceEnum> types = new TreeSet<SourceEnum>();
				for (SourceEnum type : SourceEnum.values()) {
					if (type.getSourceConcern().equals(concern)) {
						types.add(type);
					}
				}
				sourcesMap.put(concern, types);
			}
		}
	}

	public void onSelectSourceConcern(final AjaxBehaviorEvent event) {
		sourceType = null;
		SourceConcernEnum concern = (SourceConcernEnum) ((UIOutput) event.getSource()).getValue();
		logger.info("Selected concern: {0}", concern);
	}

	public void onSelectSourceType(final AjaxBehaviorEvent event) {
		SourceEnum type = (SourceEnum) ((UIOutput) event.getSource()).getValue();
		logger.info("Selected type: {0}", type);
		allowTypes = "/(\\.|\\/)("+StringUtils.join(type.getExtensions(), "|")+")$/";
	}

	public void addSource(FileUploadEvent event) {
		Validate.notNull(sourceConcern, "Source concern cannot be null");
		Validate.notNull(sourceType, "Source type cannot be null");

		UploadedFile uploadedFile = event.getFile();
		Validate.notNull(uploadedFile, "corrupt uploaded file");

		Source source = new Source();
		source.setConcern(sourceConcern);
		source.setType(sourceType);
		source.setName(uploadedFile.getFileName());

		String extension = FilenameUtils.getExtension(uploadedFile.getFileName());
		Validate.isTrue(sourceType.getExtensions().contains(extension));
		source.setFileExtension(extension);

		String filePath = saveFileInServer(uploadedFile);

		source.setFilePath(filePath);

		source.setProject(getProject());
		final Timestamp now = new Timestamp(new Date().getTime());
		source.setCreatedAt(now);
		source.setModifiedAt(now);
		final String loggedUser = sessionController.getLoggedUser();
		source.setCreatedBy(loggedUser);
		source.setModifiedBy(loggedUser);

		getProject().getSources().add(source);
		getProject().setModifiedAt(now);
		getProject().setModifiedBy(loggedUser);

		sessionController.updateProject();

		// Now store it in DB.

		RequestContext.getCurrentInstance().update("mainForm:mainTabs:sourcesTable");

		FacesMessage message = new FacesMessage("Succesful", uploadedFile.getFileName() + " is uploaded.");
		FacesContext.getCurrentInstance().addMessage(null, message);
	}

	private String saveFileInServer(final UploadedFile uploadedFile) {
		File folder = new File(getSessionController().getProperty("location.upload"));
		String fileName = FilenameUtils.getBaseName(uploadedFile.getFileName());
		String fileExtension = FilenameUtils.getExtension(uploadedFile.getFileName());

		if (!folder.exists()) {
			folder.mkdir();
		}

		Path filePath = null;
		try (InputStream input = uploadedFile.getInputstream()) {
			filePath = Files.createFile(Paths.get(folder.getAbsolutePath() + File.separator + "p_"
					+ getProject().getId() + "_" + fileName + "_" + UUID.randomUUID() + "." + fileExtension));
			Files.copy(input, filePath, StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException e) {
			logger.error(e.getMessage());
			return null;
		}

		return filePath.toString();
	}

	public void onRowToggle(ToggleEvent event) {
		selectedSource = (Source) event.getData();
		logger.info("Source expanded: {0}", selectedSource);
		computeSourceRoot();
	}

	@SuppressWarnings({ "resource", "unchecked" })
	private void computeSourceRoot() {
		sourceRoot = new DefaultTreeNode(new ZipEntry(selectedSource.getName()), null);
		FileOutputStream fos = null;
		try {
			File tempWarFile = File.createTempFile("warFile", ".tmp", null);
			fos = new FileOutputStream(tempWarFile);
			fos.write(selectedSource.getFile());
			fos.close();
			zipFile = new ZipFile(tempWarFile);

		} catch (IOException e) {
			logger.error(e.getMessage());
			return;
		}
		finally {
			if(fos!=null) {
				try {
					fos.close();
				} catch (IOException e) {
					logger.error(e.getMessage());
				}
			}
		}

		directories = new Hashtable<String, TreeNode>();
		Enumeration<ZipEntry> entries = (Enumeration<ZipEntry>) zipFile.entries();
		while (entries.hasMoreElements()) {
			ZipEntry entry = entries.nextElement();
			if (entry.isDirectory()) {
				directories.put(entry.getName(), new DefaultTreeNode(entry, getParentDirectory(entry)));
			}
		}

		entries = (Enumeration<ZipEntry>) zipFile.entries();
		while (entries.hasMoreElements()) {
			ZipEntry entry = entries.nextElement();
			if (!entry.isDirectory()) {
				new DefaultTreeNode(entry, getParentDirectory(entry));
			}
		}

	}

	private TreeNode getParentDirectory(ZipEntry entry) {
		String entryName = entry.getName();
		String parentName = new File(entryName).getParent();
		if (parentName == null) {
			return sourceRoot;
		}
		parentName = parentName.replaceAll("\\\\", "/") + "/";
		if (directories.get(parentName) == null) {
			return sourceRoot;
		}
		return directories.get(parentName);
	}

	public void selectNode(ZipEntry node) {
		logger.info(node.getName() + " selected!");
		selectedNode = node;
		computeDocumentText();
		RequestContext.getCurrentInstance().update("mainForm:mainTabs:documentViewerDialogId");
		RequestContext.getCurrentInstance().update("mainForm:mainTabs:codeMirror");
	}

	private void computeDocumentText() {
		documentText = StringUtils.EMPTY;
		if (selectedNode == null || zipFile == null) {
			return;
		}
		try {
			documentText = IOUtils.toString(zipFile.getInputStream(selectedNode));
			String extension = FilenameUtils.getExtension(selectedNode.getName());
			switch (extension) {
			case "class":
				documentText = decompile(selectedNode);
				documentTextType = "groovy";
				break;
			case "java":
				documentTextType = "javascript";
				break;
			case "xhtml":
				documentTextType = "htmlmixed";
				break;
			case "xml":
				documentTextType = "xml";
				break;
			default:
				documentTextType = "htmlmixed";
				break;
			}
		} catch (IOException e) {
			logger.error(e.getMessage());
			logger.error(e.getMessage());
			return;
		}
	}

	private String decompile(ZipEntry entry) {
		
		if (entry.isDirectory() || !entry.getName().endsWith(".class")) {
			return "It is not a valid class file";
		}

		ClassParser cp = new ClassParser(selectedSource.getFilePath(), entry.getName());
		JavaClass javaClass = null;
		try {
			javaClass = cp.parse();
		} catch (ClassFormatException | IOException e) {
			logger.error(entry.getName() + " cannot be parsed");
			javaClass = null;
			return "Error parsing file!";
		}
		
		return javaClass.toString();
	}

	public void removeSource(final Source source) {
		getProject().getSources().remove(source);
		sessionController.updateProject();
	}
	
	public String getAllowTypes() {
		return allowTypes;
	}

	private Project getProject() {
		return sessionController.getProject();
	}

	public SessionController getSessionController() {
		return sessionController;
	}

	public void setSessionController(SessionController sessionController) {
		this.sessionController = sessionController;
	}

	public Set<Source> getSources() {
		return getProject().getSources();
	}

	public Set<SourceEnum> getSourceTypeOptions() {
		return sourceConcern != null ? sourcesMap.get(sourceConcern) : new TreeSet<SourceEnum>();
	}

	public Set<SourceConcernEnum> getSourceConcernOptions() {
		return sourcesMap.keySet();
	}

	public SourceEnum getSourceType() {
		return sourceType;
	}

	public String getSelectFileMessage() {
		return sourceType != null ? "Select " + sourceType.getLabel() + " " + sourceType.getFormattedExtensions() : "";
	}

	public void setSourceType(SourceEnum sourceType) {
		this.sourceType = sourceType;
	}

	public SourceConcernEnum getSourceConcern() {
		return sourceConcern;
	}

	public void setSourceConcern(SourceConcernEnum sourceConcern) {
		this.sourceConcern = sourceConcern;
	}

	public TreeNode getSourceRoot() {
		return sourceRoot;
	}

	public void setSourceRoot(TreeNode sourceRoot) {
		this.sourceRoot = sourceRoot;
	}

	public ZipEntry getSelectedNode() {
		return selectedNode;
	}

	public void setSelectedNode(ZipEntry selectedNode) {
		this.selectedNode = selectedNode;
	}

	public String getDocumentText() {
		return documentText;
	}

	public void setDocumentText(String documentText) {
		this.documentText = documentText;
	}

	public String getDocumentTextType() {
		return documentTextType;
	}

	public void setDocumentTextType(String documentTextType) {
		this.documentTextType = documentTextType;
	}

	public Source getSelectedSource() {
		return selectedSource;
	}

	public void setSelectedSource(Source selectedSource) {
		this.selectedSource = selectedSource;
	}

	public void setAllowTypes(String allowTypes) {
		this.allowTypes = allowTypes;
	}

}