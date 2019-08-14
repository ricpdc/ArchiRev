package es.alarcos.archirev.model;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

@Entity
@Table(name = "av_purpose")
@NamedQueries({ @NamedQuery(name = "Purpose.getNames", query = "SELECT p.name FROM Purpose p") })
public class Purpose extends AbstractEntity {

	private static final long serialVersionUID = -8899802460918251152L;

	@ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	@JoinTable(name = "av_viewpoint_purpose", joinColumns = {
			@JoinColumn(name = "purpose_id", nullable = true) }, inverseJoinColumns = {
					@JoinColumn(name = "viewpoint_id") })
	private List<Viewpoint> viewpoints = new ArrayList<>();

	public Purpose() {
		super();
	}

	public List<Viewpoint> getViewpoints() {
		return viewpoints;
	}

	public void setViewpoints(List<Viewpoint> viewpoints) {
		this.viewpoints = viewpoints;
	}

}
