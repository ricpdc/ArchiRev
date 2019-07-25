package es.alarcos.archirev.logic.bestplan;

import java.util.List;

import es.alarcos.archirev.model.InputArtifact;
import es.alarcos.archirev.model.Stakeholder;
import es.alarcos.archirev.model.Technique;
import es.alarcos.archirev.model.ViewpointElement;

public class PlanStep {

	private InputArtifact artifact;
	private Technique technique;
	private Stakeholder stakeholder;
	private List<ViewpointElement> elements;
	private List<ViewpointElement> refinedElements;

	public InputArtifact getArtifact() {
		return artifact;
	}

	public void setArtifact(InputArtifact artifact) {
		this.artifact = artifact;
	}

	public Technique getTechnique() {
		return technique;
	}

	public void setTechnique(Technique technique) {
		this.technique = technique;
	}

	public Stakeholder getStakeholder() {
		return stakeholder;
	}

	public void setStakeholder(Stakeholder stakeholder) {
		this.stakeholder = stakeholder;
	}

	public List<ViewpointElement> getElements() {
		return elements;
	}

	public void setElements(List<ViewpointElement> elements) {
		this.elements = elements;
	}

	public List<ViewpointElement> getRefinedElements() {
		return refinedElements;
	}

	public void setRefinedElements(List<ViewpointElement> refinedElements) {
		this.refinedElements = refinedElements;
	}

	@Override
	public String toString() {
		return String.format("PlanStep [artifact=%s, technique=%s, stakeholder=%s, elements=%s, refinedElements=%s]",
				artifact, technique, stakeholder, elements, refinedElements);
	}

}
