package es.alarcos.archirev.model;

import java.util.Set;
import java.util.TreeSet;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.persistence.Table;

@Entity
@Table(name = "av_category")
public class Category extends AbstractEntity {

	private static final long serialVersionUID = 4901676425961677103L;

	@Column(name = "name")
	private String name;

	@Column(name = "description")
	private String description;
	
	
	@OneToMany(cascade = CascadeType.ALL, mappedBy = "category", fetch = FetchType.LAZY)
	private Set<Viewpoint> viewpoints = new TreeSet<>();

	public Category() {

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
