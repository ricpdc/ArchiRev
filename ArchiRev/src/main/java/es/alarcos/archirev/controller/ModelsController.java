package es.alarcos.archirev.controller;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.util.Date;
import java.util.Set;
import java.util.UUID;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

import org.primefaces.context.RequestContext;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import es.alarcos.archirev.logic.ExtractionService;
import es.alarcos.archirev.model.Extraction;
import es.alarcos.archirev.model.Model;
import es.alarcos.archirev.model.Project;
import es.alarcos.archirev.model.View;

@ManagedBean(name = "modelsController")
@Controller
@ViewScoped
public class ModelsController extends AbstractController {

	private static final long serialVersionUID = 1220451072138440791L;

	static Logger LOGGER = LoggerFactory.getLogger(ModelsController.class);

	@Autowired
	private SessionController sessionController;

	@Autowired
	private ExtractionService extractionService;

	private Model selectedModel;
	private View selectedView;
	
	private StreamedContent exportedFile;

	public ModelsController() {
		super();
	}

	@PostConstruct
	public void init() {
		super.init();
		reload();
	}

	public void reload() {
		if (!getProject().getModels().isEmpty()) {
			selectedModel = getProject().getModels().iterator().next();
		}
		RequestContext.getCurrentInstance().update("mainForm:mainTabs:eaModelsPanel");
	}

	public StreamedContent getSelectedDiagram() {
		byte[] imageBytes = null;
		if (selectedModel == null || selectedModel.getDefaultView() == null) {
			return null;
		}
		try {
			if(selectedView==null) {
				selectedView=selectedModel.getDefaultView();
			}
			imageBytes = Files.readAllBytes(new File(selectedView.getSanitizedImagePath()).toPath());
			return new DefaultStreamedContent(new ByteArrayInputStream(imageBytes), "image/png");
		} catch (IOException e) {
			LOGGER.error("Error rendering the model diagram");
		}
		return null;
	}

	public void exportModel() {
		if(selectedModel.getExportedPath()==null) {
			final Timestamp now = new Timestamp(new Date().getTime());
			final String loggedUser = sessionController.getLoggedUser();
			selectedModel.setModifiedAt(now);
			selectedModel.setModifiedBy(loggedUser);
	
			selectedModel.setExportedPath(createExportedFile(selectedModel));
	
			extractionService.exportArchimateModel(selectedModel);
	
			getProject().setModifiedAt(now);
			getProject().setModifiedBy(loggedUser);
	
			sessionController.updateProject();
		}
		
		byte[] xmlFileBytes= null;
		if (selectedModel == null) {
			exportedFile = null;
		}
		try {
			xmlFileBytes = Files.readAllBytes(new File(selectedModel.getSanitizedExportedPath()).toPath());
		} catch (IOException e) {
			LOGGER.error(e.getMessage());
		}
		exportedFile = new DefaultStreamedContent(new ByteArrayInputStream(xmlFileBytes), "text/xml", selectedModel.getName()+".xml");
	}
	
	public void openMetricsDialog() {
		RequestContext context = RequestContext.getCurrentInstance();
    	context.update("mainForm:metricsDialog");
    	context.execute("PF('metricsDialog').show()");
	}
	
	

	private String createExportedFile(final Model model) {
		File folder = new File(getSessionController().getProperty("location.export"));
		if (!folder.exists()) {
			folder.mkdir();
		}
		Path filePath = null;
		try {
			filePath = Files.createFile(Paths.get(folder.getAbsolutePath() + File.separator + "p_"
					+ getProject().getId() + "_e_" + model.getId() + "_" + UUID.randomUUID() + ".xml"));
		} catch (IOException e) {
			LOGGER.error(e.getMessage());
			return null;
		}
		return filePath.toString();
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

	public Model getSelectedModel() {
		return selectedModel;
	}

	public void setSelectedModel(Model selectedModel) {
		this.selectedModel = selectedModel;
	}

	public StreamedContent getExportedFile() {
		return exportedFile;
	}

	public void setExportedFile(StreamedContent exportedFile) {
		this.exportedFile = exportedFile;
	}

	public View getSelectedView() {
		return selectedView;
	}

	public void setSelectedView(View selectedView) {
		this.selectedView = selectedView;
	}

}