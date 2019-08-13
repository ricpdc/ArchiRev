package es.alarcos.archirev.logic.bestplan;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import es.alarcos.archirev.model.InputArtifact;
import es.alarcos.archirev.model.Stakeholder;
import es.alarcos.archirev.model.Technique;
import es.alarcos.archirev.model.ViewpointElement;

public class PlanStep implements Comparable<PlanStep>, Serializable {
	private static final long serialVersionUID = -7864039797888004350L;
	
	private int order;
	private InputArtifact artifact;
	private Technique technique;
	private Stakeholder stakeholder;
	private List<ViewpointElement> elements;
	private List<ViewpointElement> refinedElements;
	private List<PlanStep> alternativeSteps;

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


	public int getOrder() {
		return order;
	}

	public void setOrder(int order) {
		this.order = order;
	}

	public String getDescription() {
		String step = "";
		
		if(isAutomatic()) {
			step = "Apply \'" + technique.getName() + "\' to \'" + artifact.getName() + "\'";
		}
		else {
			step = "Manual modelling by \'" + stakeholder.getName() + "\'";
		}
		return step;
	}
	
	public boolean isAutomatic() {
		return stakeholder == null && artifact != null;
	}
	
	public boolean isManual() {
		return !isAutomatic();
	}
	
	public void setDescription(String description) {
		return;
	}

	@Override
	public int compareTo(PlanStep o) {
		if(o == null) {
			return 1;
		}
		return Integer.compare(this.getOrder(), o.getOrder());
	}

	public List<PlanStep> getAlternativeSteps() {
		return alternativeSteps;
	}

	public void setAlternativeSteps(List<PlanStep> alternativeSteps) {
		this.alternativeSteps = alternativeSteps;
	}
	
	public void addAlternativeStep(PlanStep alternativeStep) {
		if(alternativeSteps==null) {
			alternativeSteps = new ArrayList<PlanStep> ();
		}
		alternativeSteps.add(alternativeStep);		
	}

	@Override
	public String toString() {
		return String.format(
				"PlanStep [order=%s, artifact=%s, technique=%s, stakeholder=%s, elements=%s, refinedElements=%s, alternativeSteps=%s]",
				order, artifact, technique, stakeholder, elements, refinedElements, alternativeSteps);
	}
}
