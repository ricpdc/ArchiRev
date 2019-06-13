package es.alarcos.archirev.model;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

@Entity
@Table(name = "av_stakeholder")
@NamedQueries({ @NamedQuery(name = "Stakeholder.getNames", query = "SELECT s.name FROM Stakeholder s") })
public class Stakeholder extends AbstractEntity {

	private static final long serialVersionUID = 3521764823648498872L;

	@Column(name = "name")
	private String name;

	@Column(name = "description")
	private String description;

	@ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	@JoinTable(name = "av_viewpoint_stakeholder", joinColumns = {
			@JoinColumn(name = "stakeholder_id", nullable = true) }, inverseJoinColumns = {
					@JoinColumn(name = "viewpoint_id") })
	private List<Viewpoint> viewpoints = new ArrayList<>();

	public Stakeholder() {

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

	public List<Viewpoint> getViewpoints() {
		return viewpoints;
	}

	public void setViewpoints(List<Viewpoint> viewpoints) {
		this.viewpoints = viewpoints;
	}
	
	@Override
	public int compareTo(AbstractEntity o) {
		if (o == null || o.getId() == null) {
			return 1;
		} else if (getId() == null) {
			return -1;
		}
		return getId().compareTo(o.getId());
	}

}
