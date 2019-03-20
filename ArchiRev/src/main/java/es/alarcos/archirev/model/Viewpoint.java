package es.alarcos.archirev.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

@Entity
@Table(name = "av_viewpoint")
public class Viewpoint extends AbstractEntity {

	private static final long serialVersionUID = 326421301169883035L;

	@Column(name = "name")
	private String name;

	@Column(name = "explanation")
	private String explanation;

	@Column(name = "example")
	private byte[] example;

	@OneToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "scope")
	private Scope scope;
	
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

	public Scope getScope() {
		return scope;
	}

	public void setScope(Scope scope) {
		this.scope = scope;
	}

	public byte[] getExample() {
		return example;
	}

	public void setExample(byte[] example) {
		this.example = example;
	}

}
