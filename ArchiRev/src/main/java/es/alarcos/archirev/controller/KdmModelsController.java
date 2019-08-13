package es.alarcos.archirev.controller;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
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
import org.primefaces.event.SelectEvent;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import es.alarcos.archirev.model.Extraction;
import es.alarcos.archirev.model.KdmModel;
import es.alarcos.archirev.model.Project;

@ManagedBean(name = "modelsController")
@Controller
@ViewScoped
public class KdmModelsController extends AbstractController {

	private static final long serialVersionUID = 1220451072138440791L;

	static Logger LOGGER = LoggerFactory.getLogger(KdmModelsController.class);

	@Autowired
	private SessionController sessionController;

	private KdmModel selectedModel;

	private transient StreamedContent exportedFile;

	public KdmModelsController() {
		super();
	}

	@PostConstruct
	public void init() {
		super.init();
		reload();
	}

	public void reload() {
		if (!getProject().getKdmModels().isEmpty()) {
			selectedModel = getProject().getKdmModels().iterator().next();
		}
		RequestContext.getCurrentInstance().update("mainForm:mainTabs:eaModelsPanel");
	}

	public void exportModel() {
		if (selectedModel.getExportedPath() == null) {
			final Timestamp now = new Timestamp(new Date().getTime());
			final String loggedUser = sessionController.getLoggedUser();
			selectedModel.setModifiedAt(now);
			selectedModel.setModifiedBy(loggedUser);

			selectedModel.setExportedPath(createExportedFile(selectedModel));
			
			getProject().setModifiedAt(now);
			getProject().setModifiedBy(loggedUser);

			sessionController.updateProject();
		}

		byte[] xmlFileBytes = null;
		if (selectedModel == null) {
			exportedFile = null;
		}
		else {
			try {
				xmlFileBytes = Files.readAllBytes(new File(selectedModel.getSanitizedExportedPath()).toPath());
			} catch (IOException e) {
				LOGGER.error(e.getMessage());
			}
			exportedFile = new DefaultStreamedContent(new ByteArrayInputStream(xmlFileBytes), "text/xml",
					new File(selectedModel.getSanitizedExportedPath()).getName());
		}
	}

	public void onSelectModel(SelectEvent event) {
		KdmModel model = (KdmModel) event.getObject();
		LOGGER.info("Selected model: " + model);
		selectedModel = model;
	}

	private String createExportedFile(final KdmModel model) {
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

	public KdmModel getSelectedModel() {
		return selectedModel;
	}

	public void setSelectedModel(KdmModel selectedModel) {
		this.selectedModel = selectedModel;
	}

	public StreamedContent getExportedFile() {
		return exportedFile;
		
	}

	public void setExportedFile(StreamedContent exportedFile) {
		this.exportedFile = exportedFile;
	}
	
	public String getContentForSelectedModel() {
		if(selectedModel==null) {
			return "";
		}
		byte[] encoded = null;
		try {
			encoded = Files.readAllBytes(Paths.get(selectedModel.getExportedPath()));
		} catch (IOException e) {
			e.printStackTrace();
			LOGGER.error("Error loading kdm file");
		}
		return encoded == null ? "" : new String(encoded, Charset.defaultCharset());
	}
	
	public void setContentForSelectedModel(String value) {
		return;
	}

}