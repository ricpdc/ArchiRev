package es.alarcos.archirev.logic.bestplan;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.inject.Singleton;

import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import es.alarcos.archirev.model.InputArtifact;
import es.alarcos.archirev.model.Stakeholder;
import es.alarcos.archirev.model.Technique;
import es.alarcos.archirev.model.Viewpoint;
import es.alarcos.archirev.persistency.TechniqueDao;
import es.alarcos.archirev.persistency.ViewpointDao;
import io.jenetics.Chromosome;
import io.jenetics.Genotype;
import io.jenetics.IntegerChromosome;
import io.jenetics.IntegerGene;
import io.jenetics.Mutator;
import io.jenetics.UniformCrossover;
import io.jenetics.engine.Engine;
import io.jenetics.engine.EvolutionResult;
import io.jenetics.util.Factory;

@Singleton
@Service
public class BestPlanService {

	static Logger LOGGER = LoggerFactory.getLogger(BestPlanService.class);

	public static final String PRIO_AUTOMATIC = "PRIO_AUTOMATIC";
	public static final String PRIO_MANUAL = "PRIO_MANUAL";
	public static final String PRIO_BEST = "PRIO_BEST";
	public static final String MAX_PERFORMANCE = "MAX_PERFORMANCE";
	public static final String MAX_COMPLETED_VIEWPOINTS = "MAX_COMPLETED_VIEWPOINTS";

	private List<InputArtifact> artifacts;
	private List<Stakeholder> stakeholders;
	private List<Technique> techniques;
	private List<Viewpoint> selectedViewpoints;

	private String maximizationBestPlan;
	private String priorityBestPlan;
	private List<Pair<InputArtifact, Technique>> artifactTechniquePairs;

	@Autowired
	private ViewpointDao viewpointDao;

	@Autowired
	private TechniqueDao techniqueDao;

	public BestPlanService() {
		super();
	}

	public BestPlanService(List<InputArtifact> artifacts, List<Stakeholder> stakeholders) {
		super();
		this.artifacts = artifacts;
		this.stakeholders = stakeholders;

		this.artifactTechniquePairs = techniqueDao.getTechniqueListByArtifact(artifacts);

	}

	@PostConstruct
	public void init() {
		techniques = techniqueDao.findAll();
	}

	public BestPlan computeBestPlan() {
		BestPlan bestPlan = new BestPlan();
		try {

			Factory<Genotype<IntegerGene>> gtf = Genotype.of(IntegerChromosome.of(0, 1, artifactTechniquePairs.size()),
					IntegerChromosome.of(0, 1, stakeholders.size()));

			// Genotype<IntegerGene> newInstance = gtf.newInstance();
			// eval(newInstance);
			// System.out.println("Before the evolution:\n" + newInstance + "-->" +
			// eval(newInstance));

			Engine<IntegerGene, Double> engine = Engine.builder(this::fitness, gtf).maximizing()
					.alterers(new UniformCrossover<>(0.6), new Mutator<>(0.3)).build();

			Genotype<IntegerGene> result = engine.stream().limit(100).collect(EvolutionResult.toBestGenotype());

			System.out.println("After the evolution:\n" + result + "-->" + fitness(result));

			List<Pair<InputArtifact,Technique>> decodeArtifactTechniquePairs = decodeArtifactTechniquePairs(result);
			List<InputArtifact> includedArtifacts = new ArrayList<>(decodeArtifactTechniquePairs.stream()
					.map(Pair<InputArtifact, Technique>::getLeft).collect(Collectors.toSet()));
			
			bestPlan.setArtifacts(includedArtifacts);
			bestPlan.setStakeholders(decodeStakeholders(result));
		} catch (Exception ex) {
			LOGGER.error("Error computing best plan with genetic algorithm: ", ex);
			bestPlan.setErrorMessage(ex.getMessage());
		}
		return bestPlan;

	}

	private Double fitness(Genotype<IntegerGene> genotype) {
		long time = System.currentTimeMillis();
		double fitness = 0;

		List<Pair<InputArtifact, Technique>> includedArtifactTechniquePairs = decodeArtifactTechniquePairs(genotype);
		List<Stakeholder> includedStakeholders = decodeStakeholders(genotype);

		Set<InputArtifact> includedArtifacts = includedArtifactTechniquePairs.stream()
				.map(Pair<InputArtifact, Technique>::getLeft).collect(Collectors.toSet());

		// metrics for computing fitness value
		int totalArtifacts = artifacts.size();
		int totalStakeholders = stakeholders.size();
		int totalCombined = totalArtifacts + totalStakeholders;

		double percentageTotalArtifacts = 0;
		double percentageTotalStakeholders = 0;
		double percentageTotalCombined = 0;

		double percentageMeanArtifacts = 0;
		double percentageMeanStakeholders = 0;
		double percentageMeanCombined = 0;

		double percentageCompletedViewpointArtifacts = 0;
		double percentageCompletedViewpointStakeholders = 0;
		double percentageCompletedViewpointCombined = 0;

		double percentageNotUsedArtefacts = (totalArtifacts - includedArtifacts.size()) / totalArtifacts;
		double percentageNotUsedStakeholders = (totalStakeholders - includedStakeholders.size()) / totalCombined;
		double percentageNotUsedCombined = (totalCombined - includedArtifacts.size() - includedStakeholders.size())
				/ totalCombined;

		
		//compute only automatic
		Map<Viewpoint, Set<Long>> elementsCoveredByViewpointArtifact = new HashMap<>();
		Map<Viewpoint, Double> percentageCoveredByViewpointArtifact = new HashMap<>();

		Set<Long> totalCoveredElements;
		for (Viewpoint viewpoint : selectedViewpoints) {
			totalCoveredElements = new HashSet<>();
			
			for (Pair<InputArtifact, Technique> pair : includedArtifactTechniquePairs) {
				totalCoveredElements.addAll(viewpointDao.getCoveredElements(viewpoint, pair, null));
			}

			elementsCoveredByViewpointArtifact.put(viewpoint, totalCoveredElements);
			final long totalElementsByViewpoint = viewpointDao.getTotalElementsByViewpoint(viewpoint);
			final double percentage = (double) totalCoveredElements.size() / totalElementsByViewpoint;
			percentageTotalArtifacts += percentage;
			percentageCoveredByViewpointArtifact.put(viewpoint, percentage);
			if (totalCoveredElements.size() == totalElementsByViewpoint) {
				percentageCompletedViewpointArtifacts++;
			}
		}
		percentageCompletedViewpointArtifacts = percentageCompletedViewpointArtifacts / selectedViewpoints.size();
		percentageMeanArtifacts = percentageTotalArtifacts / includedArtifactTechniquePairs.size();

		
		
		
		//compute only manual
		Map<Viewpoint, Set<Long>> elementsCoveredByViewpointStakeholder = new HashMap<>();
		Map<Viewpoint, Double> percentageCoveredByViewpointStakeholder = new HashMap<>();

		for (Viewpoint viewpoint : selectedViewpoints) {
			totalCoveredElements = new HashSet<>();
			
			for(Stakeholder stakeholder : includedStakeholders) {
				totalCoveredElements.addAll(viewpointDao.getCoveredElements(viewpoint, stakeholder, null));
			}
			
			elementsCoveredByViewpointStakeholder.put(viewpoint, totalCoveredElements);
			final long totalElementsByViewpoint = viewpointDao.getTotalElementsByViewpoint(viewpoint);
			final double percentage = (double) totalCoveredElements.size() / totalElementsByViewpoint;
			percentageTotalStakeholders += percentage;
			percentageCoveredByViewpointStakeholder.put(viewpoint, percentage);
			if (totalCoveredElements.size() == totalElementsByViewpoint) {
				percentageCompletedViewpointStakeholders++;
			}
		}
		
		percentageCompletedViewpointStakeholders = percentageCompletedViewpointStakeholders / selectedViewpoints.size();
		percentageMeanStakeholders = percentageTotalStakeholders / includedStakeholders.size();
		
		
		
		//compute for combined (automatic and then manual)
		Map<Viewpoint, Set<Long>> elementsCoveredByViewpointCombined = new HashMap<>();
		Map<Viewpoint, Double> percentageCoveredByViewpointCombined = new HashMap<>();
		
		Set<Long> alreadyCovered;
		for (Viewpoint viewpoint : selectedViewpoints) {
			alreadyCovered = elementsCoveredByViewpointArtifact.get(viewpoint);	
			totalCoveredElements = new HashSet<>();
			totalCoveredElements.addAll(alreadyCovered);
			
			for(Stakeholder stakeholder : includedStakeholders) {
				totalCoveredElements.addAll(viewpointDao.getCoveredElements(viewpoint, stakeholder, alreadyCovered));
			}
			
			elementsCoveredByViewpointCombined.put(viewpoint, totalCoveredElements);
			final long totalElementsByViewpoint = viewpointDao.getTotalElementsByViewpoint(viewpoint);
			final double percentage = (double) totalCoveredElements.size() / totalElementsByViewpoint;
			percentageTotalCombined += percentage;
			percentageCoveredByViewpointCombined.put(viewpoint, percentage);
			if (totalCoveredElements.size() == totalElementsByViewpoint) {
				percentageCompletedViewpointCombined++;
			}
		}
		
		percentageCompletedViewpointCombined= percentageCompletedViewpointCombined / selectedViewpoints.size();
		percentageMeanCombined = percentageTotalCombined / (includedArtifactTechniquePairs.size() + includedStakeholders.size());

		
		

		// aggregate values into a single fitness value

		// weigths
		double w1 = 0.0;
		double w2 = 0.0;
		double w3 = 0.0;
		double w4 = 0.0;
		double w5 = 0.0;
		double w6 = 0.0;
		double w7 = 0.0;
		double w8 = 0.0;
		double w9 = 0.0;

		final double LOW = 0.15;
		final double MODERATE = 0.35;
		final double MEDIUM = 0.50;

		if (PRIO_AUTOMATIC.equals(priorityBestPlan) && MAX_PERFORMANCE.equals(maximizationBestPlan)) {
			w1 = MODERATE;
			w3 = LOW;
			w4 = LOW;
			w5 = MODERATE;
		} else if (PRIO_AUTOMATIC.equals(priorityBestPlan) && MAX_COMPLETED_VIEWPOINTS.equals(maximizationBestPlan)) {
			w4 = LOW;
			w5 = MODERATE;
			w7 = MODERATE;
			w9 = LOW;
		}
		if (PRIO_MANUAL.equals(priorityBestPlan) && MAX_PERFORMANCE.equals(maximizationBestPlan)) {
			w2 = MODERATE;
			w3 = LOW;
			w4 = MODERATE;
			w5 = LOW;
		} else if (PRIO_MANUAL.equals(priorityBestPlan) && MAX_COMPLETED_VIEWPOINTS.equals(maximizationBestPlan)) {
			w4 = MODERATE;
			w5 = LOW;
			w7 = MODERATE;
			w9 = LOW;
		}
		if (PRIO_BEST.equals(priorityBestPlan) && MAX_PERFORMANCE.equals(maximizationBestPlan)) {
			w3 = MEDIUM;
			w6 = MEDIUM;
		} else if (PRIO_BEST.equals(priorityBestPlan) && MAX_COMPLETED_VIEWPOINTS.equals(maximizationBestPlan)) {
			w6 = MEDIUM;
			w9 = MEDIUM;
		}

		fitness = w1 * percentageMeanArtifacts + w2 * percentageMeanStakeholders + w3 * percentageMeanCombined
				+ w4 * percentageNotUsedArtefacts + w5 * percentageNotUsedStakeholders + w6 * percentageNotUsedCombined
				+ w7 * percentageCompletedViewpointArtifacts + w8 * percentageCompletedViewpointStakeholders
				+ w9 * percentageCompletedViewpointCombined;


		time = System.currentTimeMillis()-time;
		
		LOGGER.info("Fitness (in "+time+" ms): " + fitness);

		return fitness;
	}

	public void computeOptimalWorkflow(BestPlan bestPlan) {

		bestPlan.getArtifacts();

		for (InputArtifact artifact : bestPlan.getArtifacts()) {
			for (Viewpoint viewpoint : selectedViewpoints) {
				viewpointDao.getTechniquePercentagesByArtefactAndViewpoint(artifact, viewpoint);
			}

		}

		Map<Pair<InputArtifact, Technique>, Map<Viewpoint, Double>> percentages;

	}

	private List<InputArtifact> decodeArtifacts(Genotype<IntegerGene> genotype) {
		Chromosome<IntegerGene> chromosomeArtifacts = genotype.get(0);
		List<InputArtifact> includedArtifacts = new ArrayList<>();
		for (int a = 0; a < chromosomeArtifacts.length(); a++) {
			if (chromosomeArtifacts.getGene(a).intValue() == 1) {
				includedArtifacts.add(artifacts.get(a));
			}
		}
		return includedArtifacts;
	}

	private List<Pair<InputArtifact, Technique>> decodeArtifactTechniquePairs(Genotype<IntegerGene> genotype) {
		Chromosome<IntegerGene> chromosomeArtifacts = genotype.get(0);
		List<Pair<InputArtifact, Technique>> includedArtifactTechniquePairs = new ArrayList<>();
		for (int a = 0; a < chromosomeArtifacts.length(); a++) {
			if (chromosomeArtifacts.getGene(a).intValue() == 1) {
				includedArtifactTechniquePairs.add(artifactTechniquePairs.get(a));
			}
		}
		return includedArtifactTechniquePairs;
	}

	private List<Stakeholder> decodeStakeholders(Genotype<IntegerGene> genotype) {
		Chromosome<IntegerGene> chromosomeStakeholders = genotype.get(1);
		List<Stakeholder> includedStakeholders = new ArrayList<>();
		for (int s = 0; s < chromosomeStakeholders.length(); s++) {
			if (chromosomeStakeholders.getGene(s).intValue() == 1) {
				includedStakeholders.add(stakeholders.get(s));
			}
		}
		return includedStakeholders;
	}

	public List<InputArtifact> getArtifacts() {
		return artifacts;
	}

	public void setArtifacts(List<InputArtifact> artifacts) {
		this.artifacts = artifacts;
		Collections.sort(this.artifacts);

		artifactTechniquePairs = techniqueDao.getTechniqueListByArtifact(artifacts);
	}

	public List<Stakeholder> getStakeholders() {
		return stakeholders;
	}

	public void setStakeholders(List<Stakeholder> stakeholders) {
		this.stakeholders = stakeholders;
		Collections.sort(this.stakeholders);
	}

	public String getMaximizationBestPlan() {
		return maximizationBestPlan;
	}

	public void setMaximizationBestPlan(String maximizationBestPlan) {
		this.maximizationBestPlan = maximizationBestPlan;
	}

	public String getPriorityBestPlan() {
		return priorityBestPlan;
	}

	public void setPriorityBestPlan(String priorityBestPlan) {
		this.priorityBestPlan = priorityBestPlan;
	}

	public List<Viewpoint> getSelectedViewpoints() {
		return selectedViewpoints;
	}

	public void setSelectedViewpoints(List<Viewpoint> selectedViewpoints) {
		this.selectedViewpoints = selectedViewpoints;
	}

}