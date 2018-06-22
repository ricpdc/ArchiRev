package es.alarcos.archirev.model;

import java.io.File;
import java.io.IOException;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

@Entity
@Table(name = "model")
public class Model extends AbstractEntity {

	private static final long serialVersionUID = -7476543110947979418L;

	@Column(name = "name")
	private String name;

	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "extraction_id")
	private Extraction extraction;

	@Column(name = "image_path", nullable = true)
	private String imagePath;

	@Column(name = "exported_path", nullable = true)
	private String exportedPath;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "project_id")
	private Project project;

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

}
