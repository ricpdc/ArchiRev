package es.alarcos.archirev.model;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;

@Entity
@Table(name = "extraction")
@NamedQueries({
		@NamedQuery(name = "Extraction.getLast", query = "SELECT e FROM Extraction e WHERE e.id = (SELECT max(e2.id) FROM Extraction e2)") })
public class Extraction extends AbstractEntity {

	private static final long serialVersionUID = -8699651777914138510L;

	@Column(name = "name")
	private String name;

	@Column(name = "setup")
	private String setup;

	@ManyToMany(cascade = { CascadeType.PERSIST, CascadeType.MERGE})
	@JoinTable(name = "extraction_source", joinColumns = {
			@JoinColumn(name = "extraction_id", nullable = true) }, inverseJoinColumns = {
					@JoinColumn(name = "source_id") })
	private Set<Source> sources;

	@OneToMany(mappedBy = "extraction", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	private Set<Model> models = new HashSet<>();
	
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "project_id")
	private Project project;

	public Extraction() {
		super();
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getSetup() {
		return setup;
	}

	public void setSetup(String setup) {
		this.setup = setup;
	}

	public Set<Source> getSources() {
		return sources;
	}

	public void setSources(Set<Source> sources) {
		this.sources = sources;
	}

	public Set<Model> getModels() {
		return models;
	}

	public void setModels(Set<Model> models) {
		this.models = models;
	}
	
	public void addModel(Model model) {
		getModels().add(model);
	}

	public Project getProject() {
		return project;
	}

	public void setProject(Project project) {
		this.project = project;
	}

	@Transient
	public String getDisplaySources() {
		String displaySources = "";
		if (!sources.isEmpty()) {
			Iterator<Source> iterator = sources.iterator();
			displaySources = iterator.next().getName();
			iterator.hasNext();

			for (; iterator.hasNext();) {
				displaySources += (", " + iterator.next().getName());
			}
		}

		return displaySources;
	}

}
