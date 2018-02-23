package es.alarcos.archirev.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

@Entity
@Table(name = "archirev_users")
@NamedQueries({
		@NamedQuery(name = "User.findByUsername", query = "SELECT u FROM User u WHERE u.username = :username") })
public class User extends AbstractEntity {

	private static final long serialVersionUID = 3143558875585472176L;

	@Column(name = "username")
	private String username;

	@Column(name = "userpassword")
	private String userPassword;

	public User() {
		super();
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getUserPassword() {
		return userPassword;
	}

	public void setUserPassword(String userPassword) {
		this.userPassword = userPassword;
	}

}
