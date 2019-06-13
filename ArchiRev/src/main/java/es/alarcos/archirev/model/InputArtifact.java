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
@Table(name = "av_input_artifact")
@NamedQueries({
	@NamedQuery(name = "InputArtifact.getNames", query = "SELECT a.name FROM InputArtifact a") })
public class InputArtifact extends AbstractEntity {

	private static final long serialVersionUID = -7335335746169001436L;

	@Column(name = "name")
	private String name;

	@Column(name = "description")
	private String description;
	
	@OneToMany(cascade = CascadeType.ALL, mappedBy = "artifact", fetch = FetchType.LAZY)
	private List<MiningPoint> miningPoints = new ArrayList<>();

	public InputArtifact() {

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
