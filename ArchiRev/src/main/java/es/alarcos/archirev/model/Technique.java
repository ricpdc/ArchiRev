package es.alarcos.archirev.model;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;

@Entity
@Table(name = "av_technique")
@NamedQueries({
	@NamedQuery(name = "Technique.getNames", query = "SELECT t.name FROM Technique t") })
public class Technique extends AbstractEntity {

	private static final long serialVersionUID = 8114299468584843872L;

	@Column(name = "description")
	private String description;
	
	@OneToMany(cascade = CascadeType.ALL, mappedBy = "technique", fetch = FetchType.LAZY)
	private List<MiningPoint> miningPoints = new ArrayList<>();

	public Technique() {

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

	public List<MiningPoint> getMiningPoints() {
		return miningPoints;
	}

	public void setMiningPoints(List<MiningPoint> miningPoints) {
		this.miningPoints = miningPoints;
	}

	

}
