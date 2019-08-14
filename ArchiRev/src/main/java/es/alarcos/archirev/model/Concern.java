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
@Table(name = "av_concern")
@NamedQueries({ @NamedQuery(name = "Concern.getNames", query = "SELECT c.name FROM Concern c") })
public class Concern extends AbstractEntity {

	private static final long serialVersionUID = -7373072327641976430L;
	
	@ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	@JoinTable(name = "av_viewpoint_concern", joinColumns = {
			@JoinColumn(name = "concern_id", nullable = true) }, inverseJoinColumns = {
					@JoinColumn(name = "viewpoint_id") })
	private List<Viewpoint> viewpoints = new ArrayList<>();

	public Concern() {
		super();
	}

	public List<Viewpoint> getViewpoints() {
		return viewpoints;
	}

	public void setViewpoints(List<Viewpoint> viewpoints) {
		this.viewpoints = viewpoints;
	}

}
