package es.alarcos.archirev.controller;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Set;

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

import es.alarcos.archirev.model.Extraction;
import es.alarcos.archirev.model.Model;
import es.alarcos.archirev.model.Project;

@ManagedBean(name = "modelsController")
@Controller
@ViewScoped
public class ModelsController extends AbstractController {

	private static final long serialVersionUID = 1220451072138440791L;

	static Logger LOGGER = LoggerFactory.getLogger(ModelsController.class);

	@Autowired
	private SessionController sessionController;
	
	private Model selectedModel;

	public ModelsController() {
		super();
	}

	@PostConstruct
	public void init() {
		super.init();
		reload();
	}

	public void reload() {
		if(!getProject().getModels().isEmpty()) {
			selectedModel = getProject().getModels().iterator().next();
		}
		RequestContext.getCurrentInstance().update("mainForm:mainTabs:eaModelsPanel");
	}
	
	public StreamedContent getSelectedDiagram() {
		byte[] imageBytes=null;
        if(selectedModel==null) {
        	return null;
        }
		try {
			imageBytes = Files.readAllBytes(new File(selectedModel.getSanitizedImagePath()).toPath());
			return new DefaultStreamedContent(new ByteArrayInputStream(imageBytes), "image/png");
		} catch (IOException e) {
			LOGGER.error("Error rendering the model diagram");
		}
		return null;
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

}