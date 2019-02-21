package es.alarcos.archirev.model;

import java.io.File;
import java.io.IOException;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;
import javax.persistence.OneToOne;
import javax.persistence.Transient;

@MappedSuperclass
public abstract class Model extends AbstractEntity {

	private static final long serialVersionUID = 8268751860712719602L;

	@OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	@JoinColumn(name = "extraction_id")
	private Extraction extraction;
	
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "project_id")
	private Project project;
	
	@Column(name = "exported_path", nullable = true)
	protected String exportedPath;

	public Model() {
		super();
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

	public String getExportedPath() {
		return exportedPath;
	}

	public void setExportedPath(String exportedPath) {
		this.exportedPath = exportedPath;
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

}