package es.alarcos.archirev.model;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "av_mining_point")
public class MiningPoint extends AbstractEntity {

	private static final long serialVersionUID = -4940730279436575028L;

	@ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	@JoinColumn(name = "element_id")
	private ViewpointElement element;
	
	@ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	@JoinColumn(name = "input_id")
	private InputArtifact artifact;
	
	@ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	@JoinColumn(name = "technique_id")
	private Technique technique;;
	
	
	public MiningPoint() {
		super();
	}


	public ViewpointElement getElement() {
		return element;
	}


	public void setElement(ViewpointElement element) {
		this.element = element;
	}


	public Technique getTechnique() {
		return technique;
	}


	public void setTechnique(Technique technique) {
		this.technique = technique;
	}


	public InputArtifact getArtifact() {
		return artifact;
	}


	public void setArtifact(InputArtifact artifact) {
		this.artifact = artifact;
	}


	@Override
	public String toString() {
		return String.format("MiningPoint [element=%s, artifact=%s, technique=%s]", element, artifact, technique);
	}	

}
