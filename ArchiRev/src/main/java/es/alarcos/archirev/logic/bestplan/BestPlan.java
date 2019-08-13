package es.alarcos.archirev.logic.bestplan;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;

import es.alarcos.archirev.model.InputArtifact;
import es.alarcos.archirev.model.Stakeholder;
import es.alarcos.archirev.model.Technique;
import io.jenetics.engine.EvolutionStatistics;

public class BestPlan implements Serializable {
	private static final long serialVersionUID = 2661719728436420699L;
	
	private List<InputArtifact> artifacts;
	private List<Stakeholder> stakeholders;
	private List<Pair<InputArtifact, Technique>> artifactTechniques;
	private String errorMessage;
	private transient EvolutionStatistics<Double, ?> statistics;
	private List<PlanStep> steps;

	public BestPlan() {
		steps = new ArrayList<>();
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

	public String getErrorMessage() {
		return errorMessage;
	}

	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}

	public EvolutionStatistics<Double, ?> getStatistics() {
		return statistics;
	}
	
	

	public void setStatistics(EvolutionStatistics<Double, ?> statistics) {
		this.statistics = statistics;
	}

	public List<Pair<InputArtifact, Technique>> getArtifactTechniques() {
		return artifactTechniques;
	}

	public void setArtifactTechniques(List<Pair<InputArtifact, Technique>> artifactTechniques) {
		this.artifactTechniques = artifactTechniques;
	}

	public List<PlanStep> getSteps() {
		return steps;
	}

	public void setSteps(List<PlanStep> steps) {
		this.steps = steps;
	}
	
	public void addStep(PlanStep step) {
		this.steps.add(step);
	}
}
