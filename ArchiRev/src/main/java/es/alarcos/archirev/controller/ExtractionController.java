package es.alarcos.archirev.controller;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;

import org.primefaces.context.RequestContext;
import org.primefaces.event.TransferEvent;
import org.primefaces.model.DualListModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import com.google.common.collect.Maps;

import es.alarcos.archirev.logic.ExtractionService;
import es.alarcos.archirev.model.Extraction;
import es.alarcos.archirev.model.Model;
import es.alarcos.archirev.model.Project;
import es.alarcos.archirev.model.Source;
import es.alarcos.archirev.model.enums.SourceConcernEnum;
import es.alarcos.archirev.model.enums.SourceEnum;
import es.alarcos.archirev.persistency.ExtractionDao;
import es.alarcos.archirev.persistency.ModelDao;

@ManagedBean(name = "extractionController")
@Controller
@ViewScoped
public class ExtractionController extends AbstractController {

	private static final long serialVersionUID = -1637522119751630382L;

	static Logger LOGGER = LoggerFactory.getLogger(ExtractionController.class);

	@Autowired
	private SessionController sessionController;

	@Autowired
	private ExtractionDao extractionDao;

	@Autowired
	private ModelDao modelDao;

	@Autowired
	private ExtractionService extractionService;

	private DualListModel<Source> sourcePickerList;

	private String extractionName;

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
		RequestContext.getCurrentInstance().update("mainForm:mainTabs:extractionTable");
	}

	public void onTransfer(TransferEvent event) {
		StringBuilder builder = new StringBuilder();
		for (Object item : event.getItems()) {
			builder.append(((Source) item).getName()).append(",");
		}
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
		extraction.setSetup("dummy setup");
		extraction.setSources(new HashSet<Source>(sourcePickerList.getTarget()));

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
		Model model = null;
		if(extraction.getModel()==null) {
			model = new Model();
			model.setExtraction(extraction);
			model.setProject(getProject());
			model.setCreatedAt(now);
			model.setCreatedBy(loggedUser);
			model.setModifiedAt(now);
			model.setModifiedBy(loggedUser);
			extraction.setModel(model);
			getProject().getModels().add(model);
		}
		else {
			model = extraction.getModel();
			model.setModifiedAt(now);
			model.setModifiedBy(loggedUser);	
		}
		
		model.setImagePath(createImageFile(extraction));
				
		extractionService.extractArchimateModel(model);
		
		getProject().setModifiedAt(now);
		getProject().setModifiedBy(loggedUser);
		
		sessionController.updateProject();
		
		reload();

		FacesMessage message = new FacesMessage(""+extraction.getName() + " has been completed.");
		FacesContext.getCurrentInstance().addMessage(null, message);
		
	}

	private String createImageFile(final Extraction extraction) {
		File folder = new File(getSessionController().getProperty("location.diagram"));
		if (!folder.exists()) {
			folder.mkdir();
		}
		Path filePath = null;
		try {
			filePath = Files.createFile(Paths.get(folder.getAbsolutePath() + File.separator + "p_"
					+ getProject().getId() + "_e_" + extraction.getId() + "_" + UUID.randomUUID() + ".png"));

		} catch (IOException e) {
			LOGGER.error(e.getMessage());
			return null;
		}
		return filePath.toString();
	}

	public void startAllExtractions() {
		for (Extraction extraction : getProject().getExtractions()) {
			if (extraction.getModel() != null) {
				startExtraction(extraction);
			}
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

}