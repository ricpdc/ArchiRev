package es.alarcos.archirev.model;

import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;

@Entity
@Table(name = "extraction")
public class Extraction extends AbstractEntity {

	private static final long serialVersionUID = -8699651777914138510L;

	@Column(name = "name")
	private String name;

	@Column(name = "setup")
	private String setup;

	@ManyToMany(cascade = { CascadeType.PERSIST, CascadeType.MERGE })
	@JoinTable(name = "extraction_source", 
		joinColumns = {@JoinColumn(name = "extraction_id")}, 
		inverseJoinColumns = {@JoinColumn(name = "source_id")})
	private List<Source> sources;
	
	@OneToOne(mappedBy = "extraction", cascade = CascadeType.ALL, 
            fetch = FetchType.LAZY, optional = true)
	private Model model;
	
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "project_id")
	private Project project;

	public Extraction() {
		super();
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getSetup() {
		return setup;
	}

	public void setSetup(String setup) {
		this.setup = setup;
	}

	public List<Source> getSources() {
		return sources;
	}

	public void setSources(List<Source> sources) {
		this.sources = sources;
	}

	public Model getModel() {
		return model;
	}

	public void setModel(Model model) {
		this.model = model;
	}

	public Project getProject() {
		return project;
	}

	public void setProject(Project project) {
		this.project = project;
	}
	
	

}
