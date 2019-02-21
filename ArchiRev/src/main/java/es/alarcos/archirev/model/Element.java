package es.alarcos.archirev.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.archimatetool.model.impl.ArchimateElement;

@Entity
@Table(name = "element")
public class Element extends AbstractEntity implements Serializable {

	private static final long serialVersionUID = 8422344555319120259L;

	public Element() {
		
	}
	
	public Element(ArchimateModel model, String elementId, String name, String documentation, String type) {
		this.model = model;
		this.elementId = elementId;
		this.name = name;
		this.documentation = documentation;
		this.type = type;
	}

	public Element(ArchimateModel model, ArchimateElement archimateElement) {
		this(model, archimateElement.getId(), archimateElement.getName(), archimateElement.getDocumentation(),
				archimateElement.getClass().getSimpleName());
	}

	@Column(name = "elementId")
	private String elementId;

	@Column(name = "documentation")
	private String documentation;

	@Column(name = "type")
	private String type;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "model_id")
	private ArchimateModel model;

	public String getDocumentation() {
		return documentation;
	}

	public void setDocumentation(String documentation) {
		this.documentation = documentation;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public Model getModel() {
		return model;
	}

	public void setModel(ArchimateModel model) {
		this.model = model;
	}

	public String getElementId() {
		return elementId;
	}

	public void setElementId(String elementId) {
		this.elementId = elementId;
	}

}
