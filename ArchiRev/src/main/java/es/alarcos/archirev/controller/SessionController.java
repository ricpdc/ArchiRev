package es.alarcos.archirev.controller;

import java.io.IOException;
import java.util.HashSet;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.servlet.http.HttpServletRequest;

import org.primefaces.context.RequestContext;
import org.primefaces.event.TabChangeEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import es.alarcos.archirev.model.Project;
import es.alarcos.archirev.model.Source;
import es.alarcos.archirev.persistency.ProjectDao;

@ManagedBean(name = "sessionController")
@Component
@SessionScoped
public class SessionController extends AbstractController {

	private static final long serialVersionUID = -3842898392480923141L;

	private static Logger logger = LoggerFactory.getLogger(SessionController.class);

	@Autowired
	private ProjectDao projectDao;

	@Autowired
	private transient Environment env;

	@Autowired
	private UserLoginController userLoginController;

	private Project project;

	private int activeTab;
	private int activeViewpointsTab;
	
	private boolean activeViewpoints;

	public SessionController() {
		super();
		project = new Project();
	}

	public void createEmptyProject() {
		project = new Project();
		project.setSources(new HashSet<Source>());
		project.setCreatedBy(getLoggedUser());
		project.setModifiedBy(getLoggedUser());
	}

	public void updateProject() {
		setProject(getProjectDao().update(getProject()));
	}

	public boolean isActiveProject() {
		return project != null && project.getId() != null;
	}
	

	public void refreshProject() {
		getProjectDao().refresh(project);
	}

	public void onTabChange(TabChangeEvent event) {
		logger.debug("Tab Changed", "Active Tab: " + event.getTab().getTitle());
		switch (activeTab) {
		case 0:
			break;
		case 1:
			break;
		case 2:
			break;
		}

		RequestContext.getCurrentInstance().update("mainForm");
	}

	public void login(ActionEvent event) {
		userLoginController.login();
	}

	public void logout() throws IOException {
		project = null;
		userLoginController.logout();
		refreshPage();
	}

	private void refreshPage() throws IOException {
		ExternalContext ec = FacesContext.getCurrentInstance().getExternalContext();
	    ec.redirect(((HttpServletRequest) ec.getRequest()).getRequestURI());
	}
	
	public void onToggleViewpoints() throws IOException {
		activeViewpoints = !activeViewpoints;
		refreshPage();
	}
	

	public boolean isLoggedIn() {
		return userLoginController.isLoggedIn();
	}

	public Project getProject() {
		return project;
	}

	public void setProject(Project project) {
		this.project = project;
	}

	public ProjectDao getProjectDao() {
		return projectDao;
	}

	public void setProjectDao(ProjectDao projectDao) {
		this.projectDao = projectDao;
	}

	public String getLoggedUser() {
		FacesContext facesContext = FacesContext.getCurrentInstance();
		return facesContext.getExternalContext().getSessionMap().get("user").toString();
	}

	public int getActiveTab() {
		return activeTab;
	}

	public void setActiveTab(int activeTab) {
		this.activeTab = activeTab;
	}

	public Environment getEnv() {
		return env;
	}

	public void setEnv(Environment env) {
		this.env = env;
	}

	public String getProperty(String key) {
		return env.getProperty(key);
	}

	public int getActiveViewpointsTab() {
		return activeViewpointsTab;
	}

	public void setActiveViewpointsTab(int activeViewpointsTab) {
		this.activeViewpointsTab = activeViewpointsTab;
	}

	public boolean isActiveViewpoints() {
		return activeViewpoints;
	}

	public void setActiveViewpoints(boolean activeViewpoints) {
		this.activeViewpoints = activeViewpoints;
	}

}
