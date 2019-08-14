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
		super();
	}

	public Metric(View view, String name, String value) {
		this.view = view;
		this.setName(name);
		this.value = value;
	}

	@Column(name = "value")
	private String value;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "view_id")
	private View view;

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public View getView() {
		return view;
	}

	public void setView(View view) {
		this.view = view;
	}

	

}
