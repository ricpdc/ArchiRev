package es.alarcos.archirev.model;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
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
	private Model model;

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

	public Model getModel() {
		return model;
	}

	public void setModel(Model model) {
		this.model = model;
	}

}
