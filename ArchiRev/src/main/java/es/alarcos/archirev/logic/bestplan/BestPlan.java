package es.alarcos.archirev.logic.bestplan;

import java.util.List;

import es.alarcos.archirev.model.InputArtifact;
import es.alarcos.archirev.model.Stakeholder;

public class BestPlan {
	List<InputArtifact> artifacts;
	List<Stakeholder> stakeholders;

	public BestPlan() {

	}

	public BestPlan(List<InputArtifact> artifacts, List<Stakeholder> stakeholders) {
		super();
		this.artifacts = artifacts;
		this.stakeholders = stakeholders;
	}

	public List<InputArtifact> getArtifacts() {
		return artifacts;
	}

	public void setArtifacts(List<InputArtifact> artifacts) {
		this.artifacts = artifacts;
	}

	public List<Stakeholder> getStakeholders() {
		return stakeholders;
	}

	public void setStakeholders(List<Stakeholder> stakeholders) {
		this.stakeholders = stakeholders;
	}
}
