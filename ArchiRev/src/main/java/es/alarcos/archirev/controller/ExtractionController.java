package es.alarcos.archirev.controller;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.servlet.ServletContext;

import org.primefaces.context.RequestContext;
import org.primefaces.event.TransferEvent;
import org.primefaces.model.DualListModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import com.google.common.collect.Maps;

import es.alarcos.archirev.logic.ArchimateExtractionService;
import es.alarcos.archirev.logic.KdmExtractionService;
import es.alarcos.archirev.model.ArchimateModel;
import es.alarcos.archirev.model.Extraction;
import es.alarcos.archirev.model.KdmModel;
import es.alarcos.archirev.model.Project;
import es.alarcos.archirev.model.Source;
import es.alarcos.archirev.model.enums.ModelViewEnum;
import es.alarcos.archirev.model.enums.SourceConcernEnum;
import es.alarcos.archirev.model.enums.SourceEnum;
import es.alarcos.archirev.persistency.ArchimateModelDao;
import es.alarcos.archirev.persistency.ExtractionDao;
import es.alarcos.archirev.persistency.ViewDao;

@ManagedBean(name = "extractionController")
@Controller
@ViewScoped
public class ExtractionController extends AbstractController {

	private static final long serialVersionUID = -1637522119751630382L;

	static Logger logger = LoggerFactory.getLogger(ExtractionController.class);

	private static final String DEFAULT_SETUP_PATH = "/json/default_setup.json";

	@Autowired
	private SessionController sessionController;

	@Autowired
	private ExtractionDao extractionDao;

	@SuppressWarnings("unused")
	@Autowired
	private ViewDao modelDao;
	
	@Autowired
	private ArchimateModelDao archimateModelDao;

	@Autowired
	private ArchimateExtractionService archimateExtractionService;
	
	@Autowired
	private KdmExtractionService kdmExtractionService;

	private DualListModel<Source> sourcePickerList;

	private String extractionName;

	private String setupText;
	private String setupTextBackup;

	private List<ModelViewEnum> possibleViews = new ArrayList<>();
	private List<ModelViewEnum> selectedViews = new ArrayList<>();

	Map<SourceConcernEnum, Set<SourceEnum>> sourcesMap = Maps.newHashMap();

	public ExtractionController() {
		super();
	}

	@PostConstruct
	public void init() {
		super.init();
		reload();
	}

	public void reload() {
		if (getSessionController().isActiveProject()) {
			sourcePickerList = new DualListModel<Source>(new ArrayList<Source>(getProject().getSources()),
					new ArrayList<Source>());
			setExtractionName("Extraction Job " + (getProject().getExtractions().size() + 1));
		} else {
			sourcePickerList = new DualListModel<Source>(new ArrayList<Source>(), new ArrayList<Source>());
			setExtractionName("");
		}
		loadPossibleViews();
		loadDefaultSetup();

		RequestContext.getCurrentInstance().update("mainForm:mainTabs:extractionTable");
	}

	private void loadPossibleViews() {
		possibleViews = Arrays.asList(ModelViewEnum.values());
		selectedViews.add(possibleViews.get(0));
	}

	private void loadDefaultSetup() {
		ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
		String relativeWebPath = DEFAULT_SETUP_PATH;
		ServletContext servletContext = (ServletContext) externalContext.getContext();
		String absoluteDiskPath = servletContext.getRealPath(relativeWebPath);
		try {
			byte[] encoded = Files.readAllBytes(Paths.get(absoluteDiskPath));
			setupText = new String(encoded, StandardCharsets.UTF_8);
		} catch (IOException e) {
			logger.error("Error loading default setup for model extraction");
		}
	}

	public void onTransfer(TransferEvent event) {
		StringBuilder builder = new StringBuilder();
		for (Object item : event.getItems()) {
			builder.append(((Source) item).getName()).append(",");
		}
	}

	public void openSetupDialog() {
		setupTextBackup = setupText;
		RequestContext context = RequestContext.getCurrentInstance();
		context.update("mainForm:extractionSetupDialog");
		context.execute("PF('extractionSetupDialog').show()");
	}

	public void openViewDialog() {
		setupTextBackup = setupText;
		RequestContext context = RequestContext.getCurrentInstance();
		context.update("mainForm:extractionViewDialog");
		context.execute("PF('extractionViewDialog').show()");
	}

	public void saveSetup() {
		closeSetupDialog();
	}

	public void discardSetup() {
		setupText = setupTextBackup;
		closeSetupDialog();
	}

	public void resetSetup() {
		loadDefaultSetup();
		RequestContext context = RequestContext.getCurrentInstance();
		context.update("mainForm:codeMirror");
	}

	private void closeSetupDialog() {
		RequestContext context = RequestContext.getCurrentInstance();
		context.update("mainForm:extractionSetupDialog");
		context.execute("PF('extractionSetupDialog').hide()");
	}

	public void addExtraction() {
		Extraction extraction = new Extraction();
		extraction.setName(getExtractionName());

		extraction.setProject(getProject());
		final Timestamp now = new Timestamp(new Date().getTime());
		extraction.setCreatedAt(now);
		extraction.setModifiedAt(now);
		final String loggedUser = sessionController.getLoggedUser();
		extraction.setCreatedBy(loggedUser);
		extraction.setModifiedBy(loggedUser);
		extraction.setSetup(setupText);
		extraction.setSources(new HashSet<Source>(sourcePickerList.getTarget()));
		extraction.getSelectedViews().addAll(selectedViews);

		getProject().getExtractions().add(extraction);
		getProject().setModifiedBy(loggedUser);

		sessionController.updateProject();

		reload();

		FacesMessage message = new FacesMessage("Succesful", extraction.getName() + " is added.");
		FacesContext.getCurrentInstance().addMessage(null, message);
	}

	public void startExtraction(Extraction extraction) {
		final Timestamp now = new Timestamp(new Date().getTime());
		final String loggedUser = sessionController.getLoggedUser();
		ArchimateModel archiMateModel = null;
		if (extraction.getArchimateModel() == null) {
			archiMateModel = new ArchimateModel();
			archiMateModel.setExtraction(extraction);
			archiMateModel.setProject(getProject());
			archiMateModel.setCreatedAt(now);
			archiMateModel.setCreatedBy(loggedUser);
			archiMateModel.setModifiedAt(now);
			archiMateModel.setModifiedBy(loggedUser);
			extraction.setArchimateModel(archiMateModel);
			getProject().getArchimateModels().add(archiMateModel);
		} else {
			archiMateModel = extraction.getArchimateModel();
			archiMateModel.setModifiedAt(now);
			archiMateModel.setModifiedBy(loggedUser);
		}
		
		archiMateModel.setRootDiagramPath(getSessionController().getProperty("location.diagram"));

		archimateExtractionService.extractArchimateModel(archiMateModel);

		getProject().setModifiedAt(now);
		getProject().setModifiedBy(loggedUser);

		sessionController.updateProject();

		reload();

		FacesMessage message = new FacesMessage("" + extraction.getName() + " has been completed.");
		FacesContext.getCurrentInstance().addMessage(null, message);

	}
	
	public void startKdmExtraction(Extraction extraction) {
		final Timestamp now = new Timestamp(new Date().getTime());
		final String loggedUser = sessionController.getLoggedUser();
		KdmModel kdmModel = null;
		if (extraction.getKdmModel() == null) {
			kdmModel = new KdmModel();
			kdmModel.setExtraction(extraction);
			kdmModel.setProject(getProject());
			kdmModel.setCreatedAt(now);
			kdmModel.setCreatedBy(loggedUser);
			kdmModel.setModifiedAt(now);
			kdmModel.setModifiedBy(loggedUser);
			kdmModel.setExportedPath(createKdmFile(kdmModel));
			extraction.setKdmModel(kdmModel);
			getProject().getKdmModels().add(kdmModel);
		} else {
			kdmModel = extraction.getKdmModel();
			kdmModel.setModifiedAt(now);
			kdmModel.setModifiedBy(loggedUser);
		}

		kdmExtractionService.extractKdmModel(kdmModel);

		getProject().setModifiedAt(now);
		getProject().setModifiedBy(loggedUser);

		sessionController.updateProject();

		reload();

		FacesMessage message = new FacesMessage("KDM Extractioin: " + extraction.getName() + " has been completed.");
		FacesContext.getCurrentInstance().addMessage(null, message);

	}
	
	
	private String createKdmFile(final KdmModel model) {
		File folder = new File(getSessionController().getProperty("location.kdm"));
		if (!folder.exists()) {
			folder.mkdir();
		}
		Path filePath = null;
		try {
			filePath = Files.createFile(Paths.get(folder.getAbsolutePath() + File.separator + "p_"
					+ getProject().getId() + "_kdm_" + model.getExtraction().getName() + "_" + UUID.randomUUID() + ".kdm"));
		} catch (IOException e) {
			logger.error(e.getMessage());
			return null;
		}
		return filePath.toString();
	}

	public void startAllExtractions() {
		for (Extraction extraction : getProject().getExtractions()) {
			if (extraction.getArchimateModel() != null) {
				startExtraction(extraction);
			}
		}
	}
	
	public void startAllKdmExtractions() {
		for (Extraction extraction : getProject().getExtractions()) {
			//if (extraction.getKdmModel() != null) {
				startKdmExtraction(extraction);
			//}
		}
	}

	public boolean hasSourceSelected() {
		return !sourcePickerList.getTarget().isEmpty();
	}

	public void removeExtraction(final Extraction extraction) {
		getProject().getExtractions().remove(extraction);
		sessionController.updateProject();
	}

	public Project getProject() {
		return sessionController.getProject();
	}

	public Set<Extraction> getExtractions() {
		return getProject().getExtractions();
	}

	public SessionController getSessionController() {
		return sessionController;
	}

	public void setSessionController(SessionController sessionController) {
		this.sessionController = sessionController;
	}

	public DualListModel<Source> getSourcePickerList() {
		if (getSessionController().isActiveProject()) {
			sourcePickerList = new DualListModel<Source>(new ArrayList<Source>(getProject().getSources()),
					new ArrayList<Source>());
		} else {
			sourcePickerList = new DualListModel<Source>(new ArrayList<Source>(), new ArrayList<Source>());
		}
		return sourcePickerList;
	}

	public void setSourcePickerList(DualListModel<Source> sourcePickerList) {
		this.sourcePickerList = sourcePickerList;
	}

	public String getExtractionName() {
		return extractionName;
	}

	public void setExtractionName(String extractionName) {
		this.extractionName = extractionName;
	}

	public ExtractionDao getExtractionDao() {
		return extractionDao;
	}

	public String getSetupTextBackup() {
		return setupTextBackup;
	}

	public void setSetupTextBackup(String setupTextBackup) {
		this.setupTextBackup = setupTextBackup;
	}

	public String getSetupText() {
		return setupText;
	}

	public void setSetupText(String setupText) {
		this.setupText = setupText;
	}

	public List<ModelViewEnum> getPossibleViews() {
		return possibleViews;
	}

	public void setPossibleViews(List<ModelViewEnum> possibleViews) {
		this.possibleViews = possibleViews;
	}

	public List<ModelViewEnum> getSelectedViews() {
		return selectedViews;
	}

	public void setSelectedViews(List<ModelViewEnum> selectedViews) {
		this.selectedViews = selectedViews;
	}

	public ArchimateModelDao getArchimateModelDao() {
		return archimateModelDao;
	}

	public void setArchimateModelDao(ArchimateModelDao archimateModelDao) {
		this.archimateModelDao = archimateModelDao;
	}

	

}