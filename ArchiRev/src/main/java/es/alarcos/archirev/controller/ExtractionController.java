package es.alarcos.archirev.controller;

import java.util.ArrayList;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

import org.primefaces.event.TransferEvent;
import org.primefaces.model.DualListModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import com.google.common.collect.Maps;

import es.alarcos.archirev.model.Project;
import es.alarcos.archirev.model.Source;
import es.alarcos.archirev.model.enums.SourceConcernEnum;
import es.alarcos.archirev.model.enums.SourceEnum;

@ManagedBean(name = "extractionController")
@Controller
@ViewScoped
public class ExtractionController extends AbstractController {

	private static final long serialVersionUID = -1637522119751630382L;

	static Logger LOGGER = LoggerFactory.getLogger(ExtractionController.class);

	@Autowired
	private SessionController sessionController;

	private DualListModel<Source> sourcePickerList;
	

	Map<SourceConcernEnum, Set<SourceEnum>> sourcesMap = Maps.newHashMap();

	public ExtractionController() {
		super();
	}

	@PostConstruct
	void init() {
		super.init();
		loadSources();
	}
	
	private void loadSources() {
		sourcePickerList = new DualListModel<Source>(getProject().getSources(), new ArrayList<Source>());
	}

	public void onTransfer(TransferEvent event) {
        StringBuilder builder = new StringBuilder();
        for(Object item : event.getItems()) {
            builder.append(((Source) item).getName()).append(",");
        }
    } 

	
	public Project getProject() {
		return sessionController.getProject();
	}

	public SessionController getSessionController() {
		return sessionController;
	}

	public void setSessionController(SessionController sessionController) {
		this.sessionController = sessionController;
	}

	public DualListModel<Source> getSourcePickerList() {
		return sourcePickerList;
	}

	


}