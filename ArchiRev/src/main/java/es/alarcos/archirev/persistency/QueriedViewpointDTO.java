package es.alarcos.archirev.persistency;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

public class QueriedViewpointDTO {

	Long id;
	String name;
	int maxNumElementsAutomatic;
	int totalElementsAutomatic;
	double maxPercentageElementsAutomatic;
	int maxNumElementsManual;
	int totalElementsManual;
	double maxPercentageElementsManual;
	private Map<String, Set<String>> elementsByTechnique = new HashMap<String, Set<String>>();
	private Map<String, Set<String>> elementsByStakeholder = new HashMap<String, Set<String>>();

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

	public int getMaxNumElementsAutomatic() {
		return maxNumElementsAutomatic;
	}

	public void setMaxNumElementsAutomatic(int maxNumElementsAutomatic) {
		this.maxNumElementsAutomatic = maxNumElementsAutomatic;
	}

	public int getTotalElementsAutomatic() {
		return totalElementsAutomatic;
	}

	public void setTotalElementsAutomatic(int totalElements) {
		this.totalElementsAutomatic = totalElements;
	}

	public double getMaxPercentageElementsAutomatic() {
		return maxPercentageElementsAutomatic;
	}

	public void setMaxPercentageElementsAutomatic(double maxPercentageElementsAutomatic) {
		this.maxPercentageElementsAutomatic = maxPercentageElementsAutomatic;
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
	
	public void addElementByStakeholder(String stakeholder, String element) {
		if (elementsByStakeholder == null) {
			elementsByStakeholder = new HashMap<String, Set<String>>();
		}
		if (!elementsByStakeholder.containsKey(stakeholder)) {
			elementsByStakeholder.put(stakeholder, new TreeSet<String>());
		}
		elementsByStakeholder.get(stakeholder).add(element);
	}

	public Map<String, Set<String>> getElementsByStakeholder() {
		return elementsByStakeholder;
	}

	public void setElementsByStakeholder(Map<String, Set<String>> elementsByStakeholder) {
		this.elementsByStakeholder = elementsByStakeholder;
	}

	public int getMaxNumElementsManual() {
		return maxNumElementsManual;
	}

	public void setMaxNumElementsManual(int maxNumElementsManual) {
		this.maxNumElementsManual = maxNumElementsManual;
	}

	public int getTotalElementsManual() {
		return totalElementsManual;
	}

	public void setTotalElementsManual(int totalElementsManual) {
		this.totalElementsManual = totalElementsManual;
	}

	public double getMaxPercentageElementsManual() {
		return maxPercentageElementsManual;
	}

	public void setMaxPercentageElementsManual(double maxPercentageElementsManual) {
		this.maxPercentageElementsManual = maxPercentageElementsManual;
	}

}
