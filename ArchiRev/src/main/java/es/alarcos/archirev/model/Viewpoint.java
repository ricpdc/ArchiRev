package es.alarcos.archirev.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "av_viewpoint")
public class Viewpoint extends AbstractEntity {

	private static final long serialVersionUID = 326421301169883035L;

	@Column(name = "name")
	private String name;

	@Column(name = "explanation")
	private String explanation;

	public Viewpoint() {
		super();
	}

	public String toString() {
		return name + " (" + explanation + ")";
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getExplanation() {
		return explanation;
	}

	public void setExplanation(String explanation) {
		this.explanation = explanation;
	}

}
