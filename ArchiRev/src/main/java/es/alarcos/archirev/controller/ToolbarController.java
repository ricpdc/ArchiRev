package es.alarcos.archirev.controller;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.context.FacesContext;

@ManagedBean
public class ToolbarController {

	private static final String SUCCESS = "Success";

	public void save() {
		addMessage(SUCCESS, "Data saved");
	}

	public void update() {
		addMessage(SUCCESS, "Data updated");
	}

	public void delete() {
		addMessage(SUCCESS, "Data deleted");
	}

	public void addMessage(String summary, String detail) {
		FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_INFO, summary, detail);
		FacesContext.getCurrentInstance().addMessage(null, message);
	}
}