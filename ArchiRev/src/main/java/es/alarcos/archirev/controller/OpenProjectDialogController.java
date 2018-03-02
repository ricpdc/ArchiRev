package es.alarcos.archirev.controller;

import java.util.List;

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
 
@ManagedBean(name = "openProjectDialogController")
@Controller
@ViewScoped
public class OpenProjectDialogController extends AbstractDialogController {

	private static final long serialVersionUID = -8824006940847003401L;

	static Logger LOGGER = LoggerFactory.getLogger(OpenProjectDialogController.class);

	@Autowired
	private SessionController sessionController;
	
	@Autowired
	private ProjectDao projectDao;
	
	private List<Project> projects;

	@PostConstruct
	void init() {
		super.init();
	}

	public OpenProjectDialogController() {
		super();
	}
     
    public void onOpen() {
    	projects = projectDao.findProjectsByUser(sessionController.getLoggedUser());
    	RequestContext context = RequestContext.getCurrentInstance();
    	context.update("mainForm:openProjectDialog");
    	context.execute("PF('openProjectDialog').show()");
    }
    
    public void openProject(final Project project) {
    	getSessionController().setProject(project);
		RequestContext context = RequestContext.getCurrentInstance();
		context.execute("PF('openProjectDialog').hide()");
		FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_INFO, "Project loaded", getSessionController().getProject().toString());
		FacesContext.getCurrentInstance().addMessage(null, message);
    }
    
    public void removeProject(final Project project) {
    	if(getSessionController().getProject().equals(project)) {
    		getSessionController().setProject(null);
    	}    	
    	projectDao.remove(project);
    	projects.remove(project);
    	FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_INFO, "Project deleted: ", project.toString());
		FacesContext.getCurrentInstance().addMessage(null, message);
    }
    
    public boolean isOpenProject(final Project project) {
    	return project.getId().equals(sessionController.getProject().getId());
    }

	public List<Project> getProjects() {
		return projects;
	}

	public void setProjects(List<Project> projects) {
		this.projects = projects;
	}

	public SessionController getSessionController() {
		return sessionController;
	}

	public void setSessionController(SessionController sessionController) {
		this.sessionController = sessionController;
	}

	public ProjectDao getProjectDao() {
		return projectDao;
	}

	public void setProjectDao(ProjectDao projectDao) {
		this.projectDao = projectDao;
	}
     
   
}