package es.alarcos.archirev.model.enums;

public enum ModelViewEnum {

	ALL(0, "All elements", ""), INFORMATION_STRUCTURE(1, "Information Structure", ""), APPLICATION_BEHAVIOUR(2,
			"Application Behaviour", ""), APPLICATION_STRUCTURE(3, "Application Structure", "");

	private int id;
	private String label;
	private String description;

	private ModelViewEnum(int id, String label, String description) {
		this.id = id;
		this.label = label;
		this.description = description;
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

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

}
