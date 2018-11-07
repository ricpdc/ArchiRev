package es.alarcos.archirev.model;

import java.io.File;
import java.io.IOException;
import java.util.Set;
import java.util.TreeSet;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

@Entity
@Table(name = "model")
public class Model extends AbstractEntity {

	private static final long serialVersionUID = -7476543110947979418L;

	@OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	@JoinColumn(name = "extraction_id")
	private Extraction extraction;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "project_id")
	private Project project;

	@Column(name = "exported_path", nullable = true)
	private String exportedPath;

	@OneToMany(cascade = CascadeType.ALL, mappedBy = "model", orphanRemoval = true, fetch = FetchType.LAZY)
	private Set<View> views = new TreeSet<>();

	@OneToMany(cascade = CascadeType.ALL, mappedBy = "model", orphanRemoval = true, fetch = FetchType.LAZY)
	private Set<Element> elements = new TreeSet<>();

	@OneToMany(cascade = CascadeType.ALL, mappedBy = "model", orphanRemoval = true, fetch = FetchType.LAZY)
	private Set<Relationship> relationships = new TreeSet<>();

	

	@Transient
	private String rootDiagramPath;

	
	public Extraction getExtraction() {
		return extraction;
	}

	public void setExtraction(Extraction extraction) {
		this.extraction = extraction;
	}

	public Project getProject() {
		return project;
	}

	public void setProject(Project project) {
		this.project = project;
	}

	public Set<View> getViews() {
		return views;
	}

	public void setViews(Set<View> views) {
		this.views = views;
	}

	public Set<Element> getElements() {
		return elements;
	}

	public void setElements(Set<Element> elements) {
		this.elements = elements;
	}

	public Set<Relationship> getRelationships() {
		return relationships;
	}

	public void setRelationships(Set<Relationship> relationships) {
		this.relationships = relationships;
	}

	public String getExportedPath() {
		return exportedPath;
	}

	public void setExportedPath(String exportedPath) {
		this.exportedPath = exportedPath;
	}

	@Transient
	public String getSanitizedExportedPath() {
		try {
			if (exportedPath != null) {
				return new File(exportedPath).getCanonicalPath();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	public String getRootDiagramPath() {
		return rootDiagramPath;
	}

	public void setRootDiagramPath(String rootDiagramPath) {
		this.rootDiagramPath = rootDiagramPath;
	}

	
	public View getDefaultView() {
		if(getViews()!=null && !getViews().isEmpty()) {
			return getViews().iterator().next();
		}
		return null;
	}

}
