package es.alarcos.archirev.model;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import es.alarcos.archirev.model.enums.SourceConcernEnum;
import es.alarcos.archirev.model.enums.SourceEnum;

@Entity
@Table(name = "source")
public class Source extends AbstractEntity {

	private static final long serialVersionUID = -3796066622416230524L;

	static Logger logger = LoggerFactory.getLogger(Source.class);
	
	@Enumerated(EnumType.ORDINAL)
	@Column(name = "concern")
	private SourceConcernEnum concern;

	@Enumerated(EnumType.ORDINAL)
	@Column(name = "type")
	private SourceEnum type;
	
	@Column(name = "file_extension")
	private String fileExtension;

	@Column(name = "file_path")
	private String filePath;

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
	
	public String getFilePath() {
		return filePath;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}

	public String getFileExtension() {
		return fileExtension;
	}

	public void setFileExtension(String fileExtension) {
		this.fileExtension = fileExtension;
	}

	@Transient
	public String getDisplayName() {
		return String.format("%s [%s]", name, type.getLabel());
	}
	
	@Transient
	public byte[] getFile() {
		try {
			return Files.readAllBytes(Paths.get(getFilePath()));
		} catch (IOException e) {
			logger.error(e.getMessage());
			return null;
		}
	}
	

}
