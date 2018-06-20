package es.alarcos.archirev.model;

import java.util.HashSet;
import java.util.Set;

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

	@OneToMany(cascade = CascadeType.ALL, mappedBy = "project", orphanRemoval = true, fetch=FetchType.LAZY)
	private Set<Source> sources = new HashSet<>();
	
	@OneToMany(cascade = CascadeType.ALL, mappedBy = "project", orphanRemoval = true, fetch=FetchType.LAZY)
	private Set<Extraction> extractions = new HashSet<>();
	
	@OneToMany(cascade = CascadeType.ALL, mappedBy = "project", orphanRemoval = true, fetch=FetchType.LAZY)
	private Set<Model> models = new HashSet<>();

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

	public Set<Source> getSources() {
		return sources;
	}

	public void setSources(Set<Source> sources) {
		this.sources = sources;
	}

	public Set<Extraction> getExtractions() {
		return extractions;
	}

	public void setExtractions(Set<Extraction> extractions) {
		this.extractions = extractions;
	}

	public Set<Model> getModels() {
		return models;
	}

	public void setModels(Set<Model> models) {
		this.models = models;
	}

}
