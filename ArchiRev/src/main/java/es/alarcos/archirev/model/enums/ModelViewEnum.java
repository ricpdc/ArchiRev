package es.alarcos.archirev.model.enums;

public enum ModelViewEnum {

	ALL(0, "All elements", "All elements and relationship in a single view"), 
	ALL_WITH_COMPONENTS(1, "All elements divided by graph connected components", "All elements and relationship splited in different views according to the graph connected componenets"),
	INFORMATION_STRUCTURE(2, "Information Structure", "A view with Data Object composistions"), 
	APPLICATION_BEHAVIOUR(3, "Application Behaviour", ""), 
	APPLICATION_STRUCTURE(4, "Application Structure", "");

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
