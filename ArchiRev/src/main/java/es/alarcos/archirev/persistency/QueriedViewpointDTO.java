package es.alarcos.archirev.persistency;

public class QueriedViewpointDTO {
	
	Long id;
	String name;
	private String techniqueName;
	int numElements;
	int totalElements;
	double percentageElements;
	
	
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public int getNumElements() {
		return numElements;
	}
	public void setNumElements(int numElements) {
		this.numElements = numElements;
	}
	public int getTotalElements() {
		return totalElements;
	}
	public void setTotalElements(int totalElements) {
		this.totalElements = totalElements;
	}
	public double getPercentageElements() {
		return percentageElements;
	}
	public void setPercentageElements(double percentageElements) {
		this.percentageElements = percentageElements;
	}
	public String getTechniqueName() {
		return techniqueName;
	}
	public void setTechniqueName(String techniqueName) {
		this.techniqueName = techniqueName;
	}
	
	
	
	
}
