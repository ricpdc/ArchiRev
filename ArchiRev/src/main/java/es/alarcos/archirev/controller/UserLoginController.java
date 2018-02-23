package es.alarcos.archirev.controller;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;

import org.apache.commons.lang3.StringUtils;
import org.primefaces.context.RequestContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import es.alarcos.archirev.model.User;
import es.alarcos.archirev.persistency.UserDao;

@ManagedBean(name = "userLoginController")
@Controller
public class UserLoginController {

	private String username;

	private String password;

	private boolean loggedIn;
	
	@Autowired
	private UserDao userDao;

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public void login(ActionEvent event) {
		FacesMessage message = null;
		setLoggedIn(false);
		if(StringUtils.isNotBlank(username) && StringUtils.isNotBlank(password)) {
			User findUser = getUserDao().getUserByUsername(username);
			if(password.equals(findUser.getUserPassword())) {
				setLoggedIn(true);
			}
			else {
				setLoggedIn(false);
			}
		}
		else {
			setLoggedIn(false);
		}
		
		if(isLoggedIn()) {
			message = new FacesMessage(FacesMessage.SEVERITY_INFO, "Welcome", username);			
		}
		else {
			message = new FacesMessage(FacesMessage.SEVERITY_WARN, "Loggin Error", "Invalid credentials");
		}
		
		FacesContext facesContext = FacesContext.getCurrentInstance();
		facesContext.addMessage(null, message);
		RequestContext context = RequestContext.getCurrentInstance();
		context.addCallbackParam("loggedIn", isLoggedIn());
		context.addCallbackParam("user", username);
		
		facesContext.getExternalContext().getSessionMap().put("user", username);
	}
	
	public void logout () {
		username=null;
		password=null;
		loggedIn=false;
		FacesContext.getCurrentInstance().getExternalContext().invalidateSession();
	}
	
	
	public String getLoginButtonText () {
		return isLoggedIn() ? "Login" : "Logout";
	}

	public boolean isLoggedIn() {
		return loggedIn;
	}

	public void setLoggedIn(boolean loggedIn) {
		this.loggedIn = loggedIn;
	}

	public UserDao getUserDao() {
		return userDao;
	}

	public void setUserDao(UserDao userDao) {
		this.userDao = userDao;
	}
}