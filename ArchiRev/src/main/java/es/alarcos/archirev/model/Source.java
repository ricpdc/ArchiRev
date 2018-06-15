package es.alarcos.archirev.model;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

import es.alarcos.archirev.model.enums.SourceConcernEnum;
import es.alarcos.archirev.model.enums.SourceEnum;

@Entity
@Table(name = "source")
public class Source extends AbstractEntity {

	private static final long serialVersionUID = -3796066622416230524L;

	@Enumerated(EnumType.ORDINAL)
	@Column(name = "concern")
	private SourceConcernEnum concern;

	@Enumerated(EnumType.ORDINAL)
	@Column(name = "type")
	private SourceEnum type;
	
	@Column(name = "name")
	private String name;


	@Column(name = "file_extension")
	private String fileExtension;

	@Column(name = "file", length = 500000000)
	private byte[] file;

	@ManyToOne(fetch = FetchType.EAGER)
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

	public SourceConcernEnum getConcern() {
		return concern;
	}

	public void setConcern(SourceConcernEnum concern) {
		this.concern = concern;
	}

	public String getFileExtension() {
		return fileExtension;
	}

	public void setFileExtension(String fileExtension) {
		this.fileExtension = fileExtension;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	@Transient
	public String getDisplayName() {
		return String.format("%s [%s]", name, type.getLabel());
	}

}
