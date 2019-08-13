package es.alarcos.archirev.controller;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;

import org.primefaces.context.RequestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import es.alarcos.archirev.model.Project;
import es.alarcos.archirev.persistency.ProjectDao;

@ManagedBean(name = "newProjectDialogController")
@Controller
@ViewScoped
public class NewProjectDialogController extends AbstractDialogController {

	private static final long serialVersionUID = 3286211947397473903L;
	static Logger logger = LoggerFactory.getLogger(NewProjectDialogController.class);

	@Autowired
	private SessionController sessionController;
	
	@Autowired
	private ProjectDao projectDao;

	@PostConstruct
	void init() {
		super.init();
	}

	public NewProjectDialogController() {
		super();
	}

	public void onOpen() {
		sessionController.createEmptyProject();
		sessionController.setActiveViewpoints(false);
		RequestContext context = RequestContext.getCurrentInstance();
		context.update("mainForm:newProjectDialog");
		context.execute("PF('newProjectDialog').show()");
	}

	public void onSave() {
		projectDao.persist(getProject());
		RequestContext context = RequestContext.getCurrentInstance();
		context.execute("PF('newProjectDialog').hide()");
		FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_INFO, "New project created", getProject().toString());
		FacesContext.getCurrentInstance().addMessage(null, message);
	}

	public Project getProject() {
		return sessionController.getProject();
	}

	public void setProject(Project project) {
		sessionController.setProject(project);
	}

	public ProjectDao getProjectDao() {
		return projectDao;
	}

	public void setProjectDao(ProjectDao projectDao) {
		this.projectDao = projectDao;
	}

	public SessionController getSessionController() {
		return sessionController;
	}

	public void setSessionController(SessionController sessionController) {
		this.sessionController = sessionController;
	}

}