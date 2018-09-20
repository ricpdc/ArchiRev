package es.alarcos.archirev.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

@Entity
@Table(name = "archirev_users")
@NamedQueries({
		@NamedQuery(name = "User.findByUsername", query = "SELECT u FROM User u WHERE u.name = :username") })
public class User extends AbstractEntity {

	private static final long serialVersionUID = 3143558875585472176L;

	@Column(name = "name")
	private String name;

	@Column(name = "password")
	private String password;

	public User() {
		super();
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	

}
