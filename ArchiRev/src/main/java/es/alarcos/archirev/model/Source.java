package es.alarcos.archirev.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import es.alarcos.archirev.model.enums.SourceEnum;

@Entity
@Table(name = "source")
public class Source extends AbstractEntity {

	private static final long serialVersionUID = -3796066622416230524L;

	@Enumerated(EnumType.ORDINAL)
	@Column(name = "type")
	private SourceEnum type;

	@Column(name = "uploadpath")
	private String uploadPath;

	@Column(name = "file", length = 500000000)
	private byte[] file;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "project_id")
	private Project project;

	public Source() {
		super();
	}

	public SourceEnum getType() {
		return type;
	}

	public void setType(SourceEnum type) {
		this.type = type;
	}

	public String getUploadPath() {
		return uploadPath;
	}

	public void setUploadPath(String uploadPath) {
		this.uploadPath = uploadPath;
	}

	public byte[] getFile() {
		return file;
	}

	public void setFile(byte[] file) {
		this.file = file;
	}

	public Project getProject() {
		return project;
	}

	public void setProject(Project project) {
		this.project = project;
	}

}
