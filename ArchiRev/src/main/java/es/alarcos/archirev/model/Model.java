package es.alarcos.archirev.model;

import java.io.File;
import java.io.IOException;
import java.util.Set;
import java.util.TreeSet;

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
@Table(name = "model")
public class Model extends AbstractEntity {

	private static final long serialVersionUID = -7476543110947979418L;

	@Column(name = "name")
	private String name;
	
	@Enumerated(EnumType.STRING)
	@Column(name = "view")
	private ModelViewEnum view;
	
	@ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	@JoinColumn(name = "extraction_id")
	private Extraction extraction;

	@Column(name = "image_path", nullable = true)
	private String imagePath;

	@Column(name = "exported_path", nullable = true)
	private String exportedPath;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "project_id")
	private Project project;
	
	@OneToMany(cascade = CascadeType.ALL, mappedBy = "model", orphanRemoval = true, fetch=FetchType.LAZY)
	private Set<Metric> metrics = new TreeSet<>();

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Extraction getExtraction() {
		return extraction;
	}

	public void setExtraction(Extraction extraction) {
		this.extraction = extraction;
	}

	public Project getProject() {
		return project;
	}

	public void setProject(Project project) {
		this.project = project;
	}

	public String getImagePath() {
		return imagePath;
	}

	public void setImagePath(String imagePath) {
		this.imagePath = imagePath;
	}

	public String getExportedPath() {
		return exportedPath;
	}

	public void setExportedPath(String exportedPath) {
		this.exportedPath = exportedPath;
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
	
	@Transient
	public String getSanitizedExportedPath() {
		try {
			if (exportedPath != null) {
				return new File(exportedPath).getCanonicalPath();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
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

	public ModelViewEnum getView() {
		return view;
	}

	public void setView(ModelViewEnum view) {
		this.view = view;
	}

}
