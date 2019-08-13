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
import org.primefaces.event.SelectEvent;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import es.alarcos.archirev.logic.ArchimateExtractionService;
import es.alarcos.archirev.logic.IconService;
import es.alarcos.archirev.model.ArchimateModel;
import es.alarcos.archirev.model.Element;
import es.alarcos.archirev.model.Extraction;
import es.alarcos.archirev.model.Project;
import es.alarcos.archirev.model.Relationship;
import es.alarcos.archirev.model.View;

@ManagedBean(name = "modelsController")
@Controller
@ViewScoped
public class ModelsController extends AbstractController {

	private static final String IMAGES_FAV_PNG = "images/fav.png";

	private static final long serialVersionUID = 1220451072138440791L;

	static Logger LOGGER = LoggerFactory.getLogger(ModelsController.class);

	@Autowired
	private SessionController sessionController;

	@Autowired
	private ArchimateExtractionService extractionService;

	private ArchimateModel selectedModel;
	private View selectedView;
	private View selectedView2;

	private transient StreamedContent exportedFile;

	public ModelsController() {
		super();
	}

	@PostConstruct
	public void init() {
		super.init();
		reload();
	}

	public void reload() {
		if (!getProject().getArchimateModels().isEmpty()) {
			selectedModel = getProject().getArchimateModels().iterator().next();
		}
		RequestContext.getCurrentInstance().update("mainForm:mainTabs:eaModelsPanel");
	}

	public StreamedContent getSelectedDiagram() {
		byte[] imageBytes = null;
		if (selectedModel == null || selectedModel.getDefaultView() == null) {
			return null;
		}
		try {
			if (selectedView == null) {
				selectedView = selectedModel.getDefaultView();
			}
			imageBytes = Files.readAllBytes(new File(selectedView.getSanitizedImagePath()).toPath());
			return new DefaultStreamedContent(new ByteArrayInputStream(imageBytes), "image/png");
		} catch (IOException e) {
			LOGGER.error("Error rendering the model diagram");
		}
		return null;
	}

	public void exportModel() {
		if (selectedModel.getExportedPath() == null) {
			final Timestamp now = new Timestamp(new Date().getTime());
			final String loggedUser = sessionController.getLoggedUser();
			selectedModel.setModifiedAt(now);
			selectedModel.setModifiedBy(loggedUser);

			selectedModel.setExportedPath(createExportedFile(selectedModel));

			selectedModel.setRootDiagramPath(getSessionController().getProperty("location.diagram"));
			
			extractionService.exportArchimateModel(selectedModel);

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
				selectedModel.getName() + ".xml");
		}
	}

	public void openMetricsDialog() {
		RequestContext context = RequestContext.getCurrentInstance();
		context.update("mainForm:metricsDialog");
		context.update("mainForm:metricsTable");
		context.execute("PF('metricsDialog').show()");
	}
	
	public void onSelectModel(SelectEvent event) {
		ArchimateModel model = (ArchimateModel) event.getObject();
		LOGGER.info("Selected model: " + model);
		selectedModel = model;
		if(selectedModel!=null) {
			selectedView = selectedModel.getDefaultView();
		}
	}
	
	public void onSelectView(SelectEvent event) {
		View view = (View) event.getObject();
		LOGGER.info("Selected view: " + view);
		setSelectedView(view);
	}

	private String createExportedFile(final ArchimateModel model) {
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

	public String getElementIconPath(Element element) {
		if (element == null) {
			return IMAGES_FAV_PNG;
		}
		String icon = IconService.getIcon(element.getType());
		return icon != null ? icon : IMAGES_FAV_PNG;
	}

	public String getRelationshipIconPath(Relationship relationship) {
		if (relationship == null) {
			return IMAGES_FAV_PNG;
		}
		String icon = IconService.getIcon(relationship.getType());
		return icon != null ? icon : IMAGES_FAV_PNG;
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

	public ArchimateModel getSelectedModel() {
		return selectedModel;
	}

	public void setSelectedModel(ArchimateModel selectedModel) {
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

	public View getSelectedView2() {
		return selectedView2;
	}

	public void setSelectedView2(View selectedView2) {
		this.selectedView2 = selectedView2;
	}

}