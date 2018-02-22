package es.alarcos.archirev.model;

import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.persistence.Table;

@Entity
@Table(name = "project")
public class Project extends AbstractEntity {

	private static final long serialVersionUID = 2570099621580899220L;

	@Column(name = "name")
	private String name;

	@Column(name = "description")
	private String description;

	@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "project", fetch=FetchType.EAGER)
	private List<Source> sources;

	public Project() {
		super();
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String toString() {
		return name + " (" + description + ")";
	}

	public List<Source> getSources() {
		return sources;
	}

	public void setSources(List<Source> sources) {
		this.sources = sources;
	}

}
