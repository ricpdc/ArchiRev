package es.alarcos.archirev.controller;

import java.util.ArrayList;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.collect.Lists;

import es.alarcos.archirev.model.Project;
import es.alarcos.archirev.model.Source;
import es.alarcos.archirev.persistency.ProjectDao;

@ManagedBean(name = "sessionController")
@Component
@SessionScoped
public class SessionController extends AbstractController {

	private static final long serialVersionUID = -3842898392480923141L;

	static Logger LOGGER = LoggerFactory.getLogger(NewProjectDialogController.class);

	@Autowired
	private ProjectDao projectDao;
	
	private Project project;

	public SessionController() {
		super();
		project = new Project();
	}
	
	public void createEmptyProject() {
		project = new Project();
		project.setSources(new ArrayList<Source>());
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

}
