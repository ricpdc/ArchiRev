package es.alarcos.archirev.model;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToOne;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.SecondaryTable;
import javax.persistence.SecondaryTables;
import javax.persistence.Table;
import javax.persistence.Transient;

@Entity
@Table(name = "av_viewpoint")
@SecondaryTables({
		@SecondaryTable(name = "av_viewpoint_basic", pkJoinColumns = @PrimaryKeyJoinColumn(name = "viewpoint_id", referencedColumnName = "id")) })
@NamedQueries({ @NamedQuery(name = "Viewpoint.getTotalElements", query = "SELECT count(e) FROM Viewpoint v JOIN v.elements e WHERE v.id=:viewpointId ") })
public class Viewpoint extends AbstractEntity {

	private static final long serialVersionUID = 326421301169883035L;

	@Column(name = "explanation")
	private String explanation;

	@Column(name = "example")
	private byte[] example;

	@Column(name = "perspective", table = "av_viewpoint_basic")
	private String perspective;

	@Column(name = "multiple_layers", table = "av_viewpoint_basic")
	private String multipleLayers;

	@Column(name = "multiple_aspects", table = "av_viewpoint_basic")
	private String multipleAspects;

	@OneToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "scope")
	private Scope scope;

	@ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	@JoinTable(name = "av_viewpoint_stakeholder", joinColumns = {
			@JoinColumn(name = "viewpoint_id", nullable = true) }, inverseJoinColumns = {
					@JoinColumn(name = "stakeholder_id") })
	private List<Stakeholder> stakeholders = new ArrayList<>();
	
	@ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	@JoinTable(name = "av_viewpoint_concern", joinColumns = {
			@JoinColumn(name = "viewpoint_id", nullable = true) }, inverseJoinColumns = {
					@JoinColumn(name = "concern_id") })
	private List<Concern> concerns = new ArrayList<>();
	
	@ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	@JoinTable(name = "av_viewpoint_purpose", joinColumns = {
			@JoinColumn(name = "viewpoint_id", nullable = true) }, inverseJoinColumns = {
					@JoinColumn(name = "purpose_id") })
	private List<Purpose> purposes = new ArrayList<>();
	
	@ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	@JoinTable(name = "av_viewpoint_element", joinColumns = {
			@JoinColumn(name = "viewpoint_id", nullable = true) }, inverseJoinColumns = {
					@JoinColumn(name = "element_id") })
	private List<ViewpointElement> elements = new ArrayList<>();

	@OneToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "category", table = "av_viewpoint_basic")
	private Category category;
	
	public Viewpoint() {
		super();
	}

	public String toString() {
		return name + " (" + explanation + ")";
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getExplanation() {
		return explanation;
	}

	public void setExplanation(String explanation) {
		this.explanation = explanation;
	}

	public Scope getScope() {
		return scope;
	}

	public void setScope(Scope scope) {
		this.scope = scope;
	}

	public byte[] getExample() {
		return example;
	}

	public void setExample(byte[] example) {
		this.example = example;
	}

	public String getPerspective() {
		return perspective;
	}

	public void setPerspective(String perspective) {
		this.perspective = perspective;
	}

	public String getMultipleLayers() {
		return multipleLayers;
	}

	public void setMultipleLayers(String multipleLayers) {
		this.multipleLayers = multipleLayers;
	}

	public String getMultipleAspects() {
		return multipleAspects;
	}

	@Transient
	public String getLayerAndAspect() {
		if (multipleLayers == null || multipleLayers.isEmpty()) {
			return "";
		}
		return ("t".equals(multipleLayers) ? "Multiple layers" : "Single layer") + " / "
				+ ("t".equals(multipleAspects) ? "Multiple aspects" : "Single aspect");
	}

	@Transient
	public void setLayerAndAspect(String value) {
		return;
	}

	public void setMultipleAspects(String multipleAspects) {
		this.multipleAspects = multipleAspects;
	}

	public Category getCategory() {
		return category;
	}

	public void setCategory(Category category) {
		this.category = category;
	}

	public List<Stakeholder> getStakeholders() {
		return stakeholders;
	}

	public void setStakeholders(List<Stakeholder> stakeholders) {
		this.stakeholders = stakeholders;
	}
	
	@Transient
	public void setDisplayedStakeholders(String value) {
		return;
	}

	@Transient
	public String getDisplayedStakeholders() {
		return getStringList(stakeholders);
	}
	
	
	private String getStringList(List<? extends AbstractEntity> entities) {
		if (entities.isEmpty()) {
			return "";
		}
		String res = entities.get(0).getName();
		
		for (int i = 1; i < entities.size(); i++) {
			res += (", " + entities.get(i).getName());
		}
		return res;
	}

	public List<Concern> getConcerns() {
		return concerns;
	}

	public void setConcerns(List<Concern> concerns) {
		this.concerns = concerns;
	}
	
	@Transient
	public void setDisplayedConcerns(String value) {
		return;
	}

	@Transient
	public String getDisplayedConcerns() {
		return getStringList(concerns);
	}

	public List<Purpose> getPurposes() {
		return purposes;
	}

	public void setPurposes(List<Purpose> purposes) {
		this.purposes = purposes;
	}
	
	@Transient
	public void setDisplayedPurposes(String value) {
		return;
	}

	@Transient
	public String getDisplayedPurposes() {
		return getStringList(purposes);
	}

	public List<ViewpointElement> getElements() {
		return elements;
	}

	public void setElements(List<ViewpointElement> elements) {
		this.elements = elements;
	}	
	
	@Transient
	public void setDisplayedElements(String value) {
		return;
	}

	@Transient
	public String getDisplayedElements() {
		return getStringList(elements);
	}	

}
