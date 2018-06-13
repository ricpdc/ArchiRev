package es.alarcos.archirev.model;

import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;

@Entity
@Table(name = "project")
@NamedQueries({
	@NamedQuery(name = "Project.findByUser", query = "SELECT p FROM Project p WHERE p.createdBy = :createdBy") })
public class Project extends AbstractEntity {

	private static final long serialVersionUID = 2570099621580899220L;

	@Column(name = "name")
	private String name;

	@Column(name = "description")
	private String description;

	@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "project", fetch=FetchType.EAGER)
	private List<Source> sources;
	
	@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "project", fetch=FetchType.LAZY)
	private List<Extraction> extractions;
	
	@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "project", fetch=FetchType.LAZY)
	private List<Model> models;

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

	public List<Extraction> getExtractions() {
		return extractions;
	}

	public void setExtractions(List<Extraction> extractions) {
		this.extractions = extractions;
	}

	public List<Model> getModels() {
		return models;
	}

	public void setModels(List<Model> models) {
		this.models = models;
	}

}
