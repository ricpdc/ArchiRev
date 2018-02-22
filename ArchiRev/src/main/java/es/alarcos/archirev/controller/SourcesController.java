package es.alarcos.archirev.controller;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.component.UIOutput;
import javax.faces.context.FacesContext;
import javax.faces.event.AjaxBehaviorEvent;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.Validate;
import org.primefaces.context.RequestContext;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.UploadedFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import es.alarcos.archirev.model.Project;
import es.alarcos.archirev.model.Source;
import es.alarcos.archirev.model.enums.SourceConcernEnum;
import es.alarcos.archirev.model.enums.SourceEnum;

@ManagedBean(name = "sourcesController")
@Controller
@ViewScoped
public class SourcesController extends AbstractController {

	private static final long serialVersionUID = -1637522119751630382L;

	static Logger LOGGER = LoggerFactory.getLogger(SourcesController.class);

	@Autowired
	private SessionController sessionController;
	
	private SourceConcernEnum sourceConcern;
	private SourceEnum sourceType;

	Map<SourceConcernEnum, Set<SourceEnum>> sourcesMap = Maps.newHashMap();

	public SourcesController() {
		super();
	}

	@PostConstruct
	void init() {
		super.init();

		loadSourceTypes();

	}

	private void loadSourceTypes() {
		sourcesMap = Maps.newHashMap();
		if (sourcesMap.isEmpty()) {
			for (SourceConcernEnum concern : SourceConcernEnum.values()) {
				Set<SourceEnum> types = Sets.newTreeSet();
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
		sourceType=null;
		SourceConcernEnum concern = (SourceConcernEnum) ((UIOutput) event.getSource()).getValue();
		LOGGER.info("Selected concern: " + concern);
	}
	
	public void onSelectSourceType(final AjaxBehaviorEvent event) {
		SourceEnum type = (SourceEnum) ((UIOutput) event.getSource()).getValue();
		LOGGER.info("Selected type: " + type);
	}

	public void handleWebAppFileUpload(FileUploadEvent event) {
		Validate.notNull(sourceConcern, "Source concern cannot be null");
		Validate.notNull(sourceType, "Soruce type cannot be null");
		
		UploadedFile uploadedFile = event.getFile();
		Validate.notNull(uploadedFile, "corrupt uploaded file");

		Source source = new Source();
		source.setConcern(sourceConcern);
		source.setType(sourceType);
		source.setName(uploadedFile.getFileName());
		
		String extension = FilenameUtils.getExtension(uploadedFile.getFileName());
		Validate.isTrue(sourceType.getExtensions().contains(extension));
		source.setFileExtension(extension);
		
		source.setFile(uploadedFile.getContents());
		source.setProject(getProject());
		Timestamp now = new Timestamp(new Date().getTime());
		source.setCreatedAt(now);
		source.setModifiedAt(now);
		
		getProject().getSources().add(source);
		sessionController.setProject(sessionController.getProjectDao().update(getProject()));

		RequestContext.getCurrentInstance().update("mainForm:mainTabs:sourcesTable");

		FacesMessage message = new FacesMessage("Succesful", uploadedFile.getFileName() + " is uploaded.");
		FacesContext.getCurrentInstance().addMessage(null, message);
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

	public List<Source> getSources() {
		return getProject().getSources();
	}

	public Set<SourceEnum> getSourceTypeOptions() {
		return sourceConcern != null ? sourcesMap.get(sourceConcern) : Sets.newTreeSet();
	}

	public Set<SourceConcernEnum> getSourceConcernOptions() {
		return sourcesMap.keySet();
	}

	public SourceEnum getSourceType() {
		return sourceType;
	}
	
	public String getSelectFileMessage() {
		return sourceType!=null ? "Select " + sourceType.getLabel() + " " + sourceType.getFormattedExtensions() : "";
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

}