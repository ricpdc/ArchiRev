package es.alarcos.archirev.model.enums;

public enum SourceEnum {

	WEB_APP(1, SourceConcernEnum.APPLICATION, "Web application");
	
	private int id;
	private SourceConcernEnum sourceConcern;
	private String label;
	
	private SourceEnum(int id, SourceConcernEnum sourceConcern, String label) {
		this.id=id;
		this.setSourceConcern(sourceConcern);
		this.label=label;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public SourceConcernEnum getSourceConcern() {
		return sourceConcern;
	}

	public void setSourceConcern(SourceConcernEnum sourceConcern) {
		this.sourceConcern = sourceConcern;
	}
	
	
	
}
