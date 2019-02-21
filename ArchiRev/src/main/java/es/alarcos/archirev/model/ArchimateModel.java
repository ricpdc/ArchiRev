package es.alarcos.archirev.model;

import java.util.Set;
import java.util.TreeSet;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;

@Entity
@Table(name = "archimate_model")
public class ArchimateModel extends Model {

	private static final long serialVersionUID = -7476543110947979418L;

	@OneToMany(cascade = CascadeType.ALL, mappedBy = "model", orphanRemoval = true, fetch = FetchType.LAZY)
	private Set<View> views = new TreeSet<>();

	@OneToMany(cascade = CascadeType.ALL, mappedBy = "model", orphanRemoval = true, fetch = FetchType.LAZY)
	private Set<Element> elements = new TreeSet<>();

	@OneToMany(cascade = CascadeType.ALL, mappedBy = "model", orphanRemoval = true, fetch = FetchType.LAZY)
	private Set<Relationship> relationships = new TreeSet<>();

	

	@Transient
	private String rootDiagramPath;

	
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
