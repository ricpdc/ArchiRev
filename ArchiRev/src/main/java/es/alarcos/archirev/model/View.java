package es.alarcos.archirev.model;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;

import es.alarcos.archirev.model.enums.ModelViewEnum;

@Entity
@Table(name = "view")
public class View extends AbstractEntity implements Serializable {

	private static final long serialVersionUID = -7476543110947979418L;

	@Enumerated(EnumType.STRING)
	@Column(name = "type")
	private ModelViewEnum type;

	@Column(name = "image_path", nullable = true)
	private String imagePath;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "model_id")
	private ArchimateModel model;
	
	@OneToMany(cascade = CascadeType.ALL, mappedBy = "view", orphanRemoval = true, fetch = FetchType.LAZY)
	private Set<Metric> metrics = new HashSet<>();

	public String getImagePath() {
		return imagePath;
	}

	public void setImagePath(String imagePath) {
		this.imagePath = imagePath;
	}

	@Transient
	public String getSanitizedImagePath() {
		try {
			if (imagePath != null) {
				return new File(imagePath).getCanonicalPath();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	public ModelViewEnum getType() {
		return type;
	}

	public void setType(ModelViewEnum type) {
		this.type = type;
	}

	public ArchimateModel getModel() {
		return model;
	}

	public void setModel(ArchimateModel model) {
		this.model = model;
	}

	public Set<Metric> getMetrics() {
		return metrics;
	}

	public void setMetrics(Set<Metric> metrics) {
		this.metrics = metrics;
	}
	
	public void addMetric(Metric metric) {
		metrics.add(metric);
	}

}
