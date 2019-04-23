package es.alarcos.archirev.persistency;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

public class QueriedViewpointDTO {

	Long id;
	String name;
	int maxNumElements;
	int totalElements;
	double maxPercentageElements;
	private Map<String, Set<String>> elementsByTechnique = new HashMap<String, Set<String>>();

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

	public int getMaxNumElements() {
		return maxNumElements;
	}

	public void setMaxNumElements(int maxNumElements) {
		this.maxNumElements = maxNumElements;
	}

	public int getTotalElements() {
		return totalElements;
	}

	public void setTotalElements(int totalElements) {
		this.totalElements = totalElements;
	}

	public double getMaxPercentageElements() {
		return maxPercentageElements;
	}

	public void setMaxPercentageElements(double maxPercentageElements) {
		this.maxPercentageElements = maxPercentageElements;
	}

	public void addElementByTechnique(String technique, String element) {
		if (elementsByTechnique == null) {
			elementsByTechnique = new HashMap<String, Set<String>>();
		}
		if (!elementsByTechnique.containsKey(technique)) {
			elementsByTechnique.put(technique, new TreeSet<String>());
		}
		elementsByTechnique.get(technique).add(element);
	}

	public Map<String, Set<String>> getElementsByTechnique() {
		return elementsByTechnique;
	}

	public void setElementsByTechnique(Map<String, Set<String>> elementsByTechnique) {
		this.elementsByTechnique = elementsByTechnique;
	}

}
