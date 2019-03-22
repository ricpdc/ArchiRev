package es.alarcos.archirev.model;

import java.io.ByteArrayInputStream;
import java.io.IOException;
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
import javax.persistence.Transient;

import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;

@Entity
@Table(name = "av_element")
@NamedQueries({ @NamedQuery(name = "ViewpointElement.getNames", query = "SELECT e.name FROM ViewpointElement e") })
public class ViewpointElement extends AbstractEntity {
	private static final long serialVersionUID = -4082736150716056520L;

	@ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	@JoinTable(name = "av_viewpoint_element", joinColumns = {
			@JoinColumn(name = "element_id", nullable = true) }, inverseJoinColumns = {
					@JoinColumn(name = "viewpoint_id") })
	private List<Viewpoint> viewpoints = new ArrayList<>();

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
		} else if (!id.equals(other.getId()))
			return false;
		return true;
	}

	public byte[] getIcon() {
		return icon;
	}

	public void setIcon(byte[] icon) {
		this.icon = icon;
	}

}
