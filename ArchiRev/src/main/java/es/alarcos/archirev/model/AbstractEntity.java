package es.alarcos.archirev.model;

import java.io.Serializable;
import java.sql.Timestamp;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import javax.persistence.Version;

@MappedSuperclass
public abstract class AbstractEntity implements Serializable {

	private static final long serialVersionUID = -1728118424063172773L;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name="id")
	protected Long id;
	
	@Column(name="createdat")
	protected Timestamp createdAt;
	
	@Column(name="modifiedat")
	protected Timestamp modifiedAt;
	
	@Column(name="createdby")
	protected String createdBy;
	
	@Column(name="modifiedby")
	protected String modifiedBy;
	
	@Version
	protected Long version;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Timestamp getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(Timestamp createdAt) {
		this.createdAt = createdAt;
	}

	public Timestamp getModifiedAt() {
		return modifiedAt;
	}

	public void setModifiedAt(Timestamp modifiedAt) {
		this.modifiedAt = modifiedAt;
	}

	public String getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}

	public String getModifiedBy() {
		return modifiedBy;
	}

	public void setModifiedBy(String modifiedBy) {
		this.modifiedBy = modifiedBy;
	}

	public Long getVersion() {
		return version;
	}

	public void setVersion(Long version) {
		this.version = version;
	}

	@Override
	public String toString() {
		return String.format(
				"AbstractEntity [id=%s, createdAt=%s, modifiedAt=%s, createdBy=%s, modifiedBy=%s, version=%s]", id,
				createdAt, modifiedAt, createdBy, modifiedBy, version);
	}

	
	
	
}
