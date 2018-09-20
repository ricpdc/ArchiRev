package es.alarcos.archirev.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "metric")
public class Metric extends AbstractEntity implements Serializable {

	private static final long serialVersionUID = 4652729570226202557L;

	public Metric() {

	}

	public Metric(Model model, String name, String value) {
		this.model = model;
		this.setName(name);
		this.value = value;
	}

	@Column(name = "value")
	private String value;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "model_id")
	private Model model;

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
