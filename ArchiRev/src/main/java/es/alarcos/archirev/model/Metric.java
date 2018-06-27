package es.alarcos.archirev.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "metric")
public class Metric extends AbstractEntity {

	private static final long serialVersionUID = 4652729570226202557L;

	public Metric() {
		
	}
	
	public Metric(Model model, String name, String value) {
		this.model = model;
		this.name = name;
		this.value = value;
	}

	@Column(name = "name")
	private String name;

	@Column(name = "value")
	private String value;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "model_id")
	private Model model;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public Model getModel() {
		return model;
	}

	public void setModel(Model model) {
		this.model = model;
	}

}
