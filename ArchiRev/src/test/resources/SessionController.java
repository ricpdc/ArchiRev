/*
 * Decompiled with CFR 0_115.
 */
package es.alarcos.archirev.controller;

@javax.faces.bean.ManagedBean(name="sessionController")
@org.springframework.stereotype.Component
@javax.faces.bean.SessionScoped
public class es.alarcos.archirev.controller.SessionController
extends es.alarcos.archirev.controller.AbstractController
 {
    private static final long serialVersionUID = -3842898392480923141L;
    static org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(es.alarcos.archirev.controller.NewProjectDialogController.class);
    @org.springframework.beans.factory.annotation.Autowired
    private es.alarcos.archirev.persistency.ProjectDao projectDao;
    private es.alarcos.archirev.model.Project project = new es.alarcos.archirev.model.Project();

    public void createEmptyProject() {
        this.project = new es.alarcos.archirev.model.Project();
        this.project.setSources(com.google.common.collect.Lists.<es.alarcos.archirev.model.Source>newArrayList());
        this.project.setCreatedBy(this.getLoggedUser());
        this.project.setModifiedBy(this.getLoggedUser());
    }

    public void updateProject() {
        this.setProject(this.getProjectDao().update(this.getProject()));
    }

    public boolean isActiveProject() {
        if (this.project == null) return false;
        if (this.project.getId() == null) return false;
        return true;
    }

    public void refreshProject() {
        this.getProjectDao().refresh(this.project);
    }

    public es.alarcos.archirev.model.Project getProject() {
        return this.project;
    }

    public void setProject(es.alarcos.archirev.model.Project project) {
        this.project = project;
    }

    public es.alarcos.archirev.persistency.ProjectDao getProjectDao() {
        return this.projectDao;
    }

    public void setProjectDao(es.alarcos.archirev.persistency.ProjectDao projectDao) {
        this.projectDao = projectDao;
    }

    public java.lang.String getLoggedUser() {
        javax.faces.context.FacesContext facesContext = javax.faces.context.FacesContext.getCurrentInstance();
        return facesContext.getExternalContext().getSessionMap().get("user").toString();
    }
}
