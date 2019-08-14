package es.alarcos.archirev.controller;

import java.io.Serializable;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.context.FacesContext;

import org.apache.commons.lang3.StringUtils;
import org.primefaces.context.RequestContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import es.alarcos.archirev.model.User;
import es.alarcos.archirev.persistency.UserDao;

@ManagedBean(name = "signinController")
@Controller
public class SigninController implements Serializable {


	private static final long serialVersionUID = 7590582575251355602L;
	
	private static final String SIGN_IN_ERROR = "Sign In Error";

	private String username;
	private String password;
	private String repeatedPassword;
	private String affiliation;
	private String email;

	
	@Autowired
	private UserDao userDao;
	
	@Autowired
	private UserLoginController userLoginController;

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
	
	public void showDialog() {
		username="";
		password="";
		repeatedPassword="";
		affiliation="";
		email="";
		RequestContext context = RequestContext.getCurrentInstance();
		context.update("mainForm:signinDialog");
		context.execute("PF('signinDialog').show()");
	}

	public void signin() {
		FacesMessage message = null;
		boolean success = false;
		if (StringUtils.isNotBlank(username) && StringUtils.isNotBlank(password)
				&& StringUtils.isNotBlank(repeatedPassword)) {
			User findUser = getUserDao().getUserByUsername(username);
			if (findUser != null) {
				message = new FacesMessage(FacesMessage.SEVERITY_ERROR, SIGN_IN_ERROR,
						"Username \"" + username + "\" already exist!");
			}
			else if(!password.equals(repeatedPassword)) {
				message = new FacesMessage(FacesMessage.SEVERITY_ERROR, SIGN_IN_ERROR,
						"Passwords do not match");
			}
			else if(password.length() < 6) {
				message = new FacesMessage(FacesMessage.SEVERITY_ERROR, SIGN_IN_ERROR,
						"Password must be 6 digit length at least");
			}
			else {
				User user = new User();
				user.setName(username);
				user.setPassword(password);
				user.setAffiliation(affiliation);
				user.setEmail(email);
				userDao.persist(user);
				success=true;
				message = new FacesMessage(FacesMessage.SEVERITY_INFO, "Sign In Correct", "Welcome \"" + username + "\"");
			}
		} else {
			message = new FacesMessage(FacesMessage.SEVERITY_WARN, SIGN_IN_ERROR, "Empty fields");
		}

		FacesContext facesContext = FacesContext.getCurrentInstance();
		facesContext.addMessage(null, message);
		
		if(success) {
			RequestContext context = RequestContext.getCurrentInstance();
			context.execute("PF('signinDialog').hide()");
			
			userLoginController.setUsername(username);
			userLoginController.setPassword(password);
			userLoginController.login();
		}
		
	}

	public UserDao getUserDao() {
		return userDao;
	}

	public void setUserDao(UserDao userDao) {
		this.userDao = userDao;
	}

	public String getRepeatedPassword() {
		return repeatedPassword;
	}

	public void setRepeatedPassword(String repeatedPassword) {
		this.repeatedPassword = repeatedPassword;
	}

	public String getAffiliation() {
		return affiliation;
	}

	public void setAffiliation(String affiliation) {
		this.affiliation = affiliation;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public UserLoginController getUserLoginController() {
		return userLoginController;
	}

	public void setUserLoginController(UserLoginController userLoginController) {
		this.userLoginController = userLoginController;
	}

}