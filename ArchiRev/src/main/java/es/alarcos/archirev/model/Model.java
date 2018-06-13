package es.alarcos.archirev.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;

@Entity
@Table(name = "model")
public class Model extends AbstractEntity {


	private static final long serialVersionUID = -7476543110947979418L;

	@Column(name = "name")
	private String name;

	@OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "extraction_id")
    private Extraction extraction;
		
	@Lob
	@Column(name = "image")
	private byte[] image;
	
	@Lob
	@Column(name = "exported_file")
	private byte[] exported_file;
	
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

	public byte[] getImage() {
		return image;
	}

	public void setImage(byte[] image) {
		this.image = image;
	}

	public byte[] getExported_file() {
		return exported_file;
	}

	public void setExported_file(byte[] exported_file) {
		this.exported_file = exported_file;
	}

	public Project getProject() {
		return project;
	}

	public void setProject(Project project) {
		this.project = project;
	}
	

}
