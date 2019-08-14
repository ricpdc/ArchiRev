package es.alarcos.archirev.model.enums;

public enum SourceConcernEnum {
	
	APPLICATION(1, "Applications"), SERVICE(2, "Services"), PROCESS(3, "Processes");
	
	private int id;
	private String label;
	
	private SourceConcernEnum(int id, String label) {
		this.id=id;
		this.label=label;
	}

	public int getId() {
		return id;
	}

	public String getLabel() {
		return label;
	}

}
