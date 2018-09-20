package es.alarcos.archirev.model.enums;

public enum ModelViewEnum {

	ALL(0, "All elements"), INFORMATION_STRUCTURE(1, "Information Structure"), APPLICATION_BEHAVIOUR(2,
			"Application Behaviour"), APPLICATION_STRUCTURE(3, "Application Structure");

	private int id;
	private String label;

	private ModelViewEnum(int id, String label) {
		this.id = id;
		this.label = label;
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

}
