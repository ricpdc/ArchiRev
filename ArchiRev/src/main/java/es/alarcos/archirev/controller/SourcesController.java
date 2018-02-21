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
import javax.faces.context.FacesContext;

import org.apache.commons.lang3.Validate;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.UploadedFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import es.alarcos.archirev.model.Source;
import es.alarcos.archirev.model.enums.SourceConcernEnum;
import es.alarcos.archirev.model.enums.SourceEnum;
import es.alarcos.archirev.persistency.SourceDao;

@ManagedBean(name = "sourcesController")
@Controller
@ViewScoped
public class SourcesController extends AbstractController {

	private static final long serialVersionUID = -1637522119751630382L;

	static Logger LOGGER = LoggerFactory.getLogger(SourcesController.class);

	@Autowired
	private SessionController sessionController;

	@Autowired
	private SourceDao sourceDao;

	private SourceConcernEnum sourceConcern;
	private SourceEnum sourceType;
	
	Map<SourceConcernEnum, Set<SourceEnum>> sourcesMap = Maps.newHashMap();

	@PostConstruct
	void init() {
		super.init();

		loadSourceTypes();
		
	}

	private void loadSourceTypes() {
		sourcesMap = Maps.newHashMap();
		if(sourcesMap.isEmpty()) {
			for (SourceConcernEnum concern : SourceConcernEnum.values()) {
				Set<SourceEnum> types = Sets.newTreeSet();
				for (SourceEnum type : SourceEnum.values()) {
					if(type.getSourceConcern().equals(concern)) {
						types.add(type);
					}
				}
				sourcesMap.put(concern, types);
			}
		}
	}

	public SourcesController() {
		super();
	}

	public void handleWebAppFileUpload(FileUploadEvent event) {
		UploadedFile uploadedFile = event.getFile();
		Validate.notNull(uploadedFile, "corrupt uploaded file");
		Source source = new Source();
		source.setType(SourceEnum.WEB_APP);
		source.setUploadPath(uploadedFile.getFileName());
		source.setFile(uploadedFile.getContents());
		source.setProject(sessionController.getProject());
		Timestamp now = new Timestamp(new Date().getTime());
		source.setCreatedAt(now);
		source.setModifiedAt(now);
		sourceDao.create(source);

		FacesMessage message = new FacesMessage("Succesful", uploadedFile.getFileName() + " is uploaded.");
		FacesContext.getCurrentInstance().addMessage(null, message);
	}

	public SessionController getSessionController() {
		return sessionController;
	}

	public void setSessionController(SessionController sessionController) {
		this.sessionController = sessionController;
	}

	public SourceDao getSourceDao() {
		return sourceDao;
	}

	public void setSourceDao(SourceDao sourceDao) {
		this.sourceDao = sourceDao;
	}

	public List<Source> getSources() {
		return sessionController.getProject().getSources();
	}

	public Set<SourceEnum> getSourceTypeOptions() {
		return sourceConcern!=null ? sourcesMap.get(sourceConcern) : Sets.newTreeSet();
	}

	public Set<SourceConcernEnum> getSourceConcernOptions() {
		return sourcesMap.keySet();
	}

	public SourceEnum getSourceType() {
		return sourceType;
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