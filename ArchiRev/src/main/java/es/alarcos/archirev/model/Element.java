package es.alarcos.archirev.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "element")
public class Element extends AbstractEntity implements Serializable {

	private static final long serialVersionUID = 8422344555319120259L;

	public Element() {

	}

	public Element(Model model, String name, String documentation, String type) {
		this.model = model;
		this.name = name;
		this.documentation = documentation;
		this.type = type;
	}

	@Column(name = "documentation")
	private String documentation;
	
	@Column(name = "type")
	private String type;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "model_id")
	private Model model;

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

	public void setModel(Model model) {
		this.model = model;
	}

	
}
