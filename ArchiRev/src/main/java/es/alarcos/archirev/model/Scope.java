package es.alarcos.archirev.model;

import java.util.Set;
import java.util.TreeSet;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;

@Entity
@Table(name = "av_scope")
@NamedQueries({
	@NamedQuery(name = "Scope.getNames", query = "SELECT s.name FROM Scope s") })
public class Scope extends AbstractEntity {

	private static final long serialVersionUID = 4901676425961677103L;

	@Column(name = "description")
	private String description;
	
	
	@OneToMany(cascade = CascadeType.ALL, mappedBy = "scope", fetch = FetchType.LAZY)
	private Set<Viewpoint> viewpoints = new TreeSet<>();

	public Scope() {

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

	public Set<Viewpoint> getViewpoints() {
		return viewpoints;
	}

	public void setViewpoints(Set<Viewpoint> viewpoints) {
		this.viewpoints = viewpoints;
	}

}
