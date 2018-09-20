package es.alarcos.archirev.model;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

import es.alarcos.archirev.model.enums.ModelViewEnum;

@Entity
@Table(name = "extraction")
@NamedQueries({
		@NamedQuery(name = "Extraction.getLast", query = "SELECT e FROM Extraction e WHERE e.id = (SELECT max(e2.id) FROM Extraction e2)") })
public class Extraction extends AbstractEntity {

	private static final long serialVersionUID = -8699651777914138510L;

	@Column(name = "setup")
	private String setup;

	@ManyToMany(cascade = { CascadeType.PERSIST, CascadeType.MERGE})
	@JoinTable(name = "extraction_source", joinColumns = {
			@JoinColumn(name = "extraction_id", nullable = true) }, inverseJoinColumns = {
					@JoinColumn(name = "source_id") })
	private Set<Source> sources;

	
	@OneToOne(mappedBy = "extraction", cascade = CascadeType.ALL, fetch = FetchType.LAZY, optional = true)
	private Model model;
	
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "project_id")
	private Project project;
	
	@ElementCollection(targetClass=ModelViewEnum.class)
    @Enumerated(EnumType.ORDINAL)
    @CollectionTable(name="extraction_selected_views")
	@Column(name="selected_views")
	private Set<ModelViewEnum> selectedViews = new HashSet<>();

	public Extraction() {
		super();
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

	public Model getModel() {
		return model;
	}

	public void setModel(Model model) {
		this.model = model;
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
	
	@Transient
	public String getDisplayViews() {
		String displayViews = "";
		if (!selectedViews.isEmpty()) {
			Iterator<ModelViewEnum> iterator = selectedViews.iterator();
			displayViews = iterator.next().getLabel();
			iterator.hasNext();

			for (; iterator.hasNext();) {
				displayViews += (", " + iterator.next().getLabel());
			}
		}

		return displayViews;
	}

	public Set<ModelViewEnum> getSelectedViews() {
		return selectedViews;
	}

	public void setSelectedViews(Set<ModelViewEnum> selectedViews) {
		this.selectedViews = selectedViews;
	}

}
