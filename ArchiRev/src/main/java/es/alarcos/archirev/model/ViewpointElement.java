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
import javax.persistence.OneToMany;
import javax.persistence.Table;

@Entity
@Table(name = "av_element")
@NamedQueries({ @NamedQuery(name = "ViewpointElement.getNames", query = "SELECT e.name FROM ViewpointElement e"),
	@NamedQuery(name = "ViewpointElement.getElementsById", query = "SELECT e FROM ViewpointElement e WHERE e.id IN (:elementIds)")})
public class ViewpointElement extends AbstractEntity {
	private static final long serialVersionUID = -4082736150716056520L;

	@ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	@JoinTable(name = "av_viewpoint_element", joinColumns = {
			@JoinColumn(name = "element_id", nullable = true) }, inverseJoinColumns = {
					@JoinColumn(name = "viewpoint_id") })
	private List<Viewpoint> viewpoints = new ArrayList<>();
	
	@OneToMany(cascade = CascadeType.ALL, mappedBy = "element", fetch = FetchType.LAZY)
	private List<MiningPoint> miningPoints = new ArrayList<>();

	@Column(name = "icon")
	private byte[] icon;

	public ViewpointElement() {

	}

	public List<Viewpoint> getViewpoints() {
		return viewpoints;
	}

	public void setViewpoints(List<Viewpoint> viewpoints) {
		this.viewpoints = viewpoints;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ViewpointElement other = (ViewpointElement) obj;
		if (id == null) {
			if (other.getId() != null)
				return false;
		} else if (!id.equals(other.getId())) {
			return false;
		}
		return true;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((miningPoints == null) ? 0 : miningPoints.hashCode());
		result = prime * result + ((viewpoints == null) ? 0 : viewpoints.hashCode());
		return result;
	}

	public byte[] getIcon() {
		return icon;
	}

	public void setIcon(byte[] icon) {
		this.icon = icon;
	}

	public List<MiningPoint> getMiningPoints() {
		return miningPoints;
	}

	public void setMiningPoints(List<MiningPoint> miningPoints) {
		this.miningPoints = miningPoints;
	}

}
