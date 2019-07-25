package es.alarcos.archirev.logic.bestplan;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
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
import es.alarcos.archirev.model.ViewpointElement;
import es.alarcos.archirev.persistency.TechniqueDao;
import es.alarcos.archirev.persistency.ViewpointDao;
import es.alarcos.archirev.persistency.ViewpointElementDao;
import io.jenetics.Chromosome;
import io.jenetics.Genotype;
import io.jenetics.IntegerChromosome;
import io.jenetics.IntegerGene;
import io.jenetics.Mutator;
import io.jenetics.RouletteWheelSelector;
import io.jenetics.SinglePointCrossover;
import io.jenetics.TournamentSelector;
import io.jenetics.engine.Engine;
import io.jenetics.engine.EvolutionResult;
import io.jenetics.engine.EvolutionStatistics;
import io.jenetics.engine.Limits;
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
	private Map<Viewpoint, Integer> viewpointSizeMap;

	@Autowired
	private ViewpointDao viewpointDao;

	@Autowired
	private ViewpointElementDao viewpointElementDao;

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

			loadCachedData();

			Factory<Genotype<IntegerGene>> gtf = Genotype.of(IntegerChromosome.of(0, 1, artifactTechniquePairs.size()),
					IntegerChromosome.of(0, 1, stakeholders.size()));

			// Genotype<IntegerGene> newInstance = gtf.newInstance();
			// eval(newInstance);
			// System.out.println("Before the evolution:\n" + newInstance + "-->" +
			// eval(newInstance));

			Engine<IntegerGene, Double> engine = Engine.builder(this::fitness, gtf).populationSize(50)
					.survivorsSelector(new TournamentSelector<>(5)).offspringSelector(new RouletteWheelSelector<>())
					.alterers(new Mutator<>(0.115), new SinglePointCrossover<>(0.16)).maximizing().build();

			EvolutionStatistics<Double, ?> statistics = EvolutionStatistics.ofNumber();

			Genotype<IntegerGene> result = engine.stream().limit(Limits.bySteadyFitness(5)).limit(1).peek(statistics)
					.peek(er -> System.out.println("BEST>> " + er.getBestPhenotype()))
					.collect(EvolutionResult.toBestGenotype());

			System.out.println(statistics);
			System.out.println("After the evolution:\n" + result + "-->" + fitness(result));

			bestPlan = handleResult(result, statistics);
		} catch (Exception ex) {
			LOGGER.error("Error computing best plan with genetic algorithm: ", ex);
			bestPlan.setErrorMessage(ex.getMessage());
		}
		return bestPlan;

	}

	private BestPlan handleResult(Genotype<IntegerGene> result, EvolutionStatistics<Double, ?> statistics) {
		BestPlan bestPlan = new BestPlan();

		// set bestPlan data
		final List<Pair<InputArtifact, Technique>> includedArtifactTechniquePairs = decodeArtifactTechniquePairs(
				result);
		final List<InputArtifact> includedArtifacts = new ArrayList<>(includedArtifactTechniquePairs.stream()
				.map(Pair<InputArtifact, Technique>::getLeft).collect(Collectors.toSet()));
		final List<Stakeholder> includedStakeholders = decodeStakeholders(result);

		bestPlan.setArtifacts(includedArtifacts);
		bestPlan.setStakeholders(includedStakeholders);
		bestPlan.setArtifactTechniques(includedArtifactTechniquePairs);
		bestPlan.setStatistics(statistics);

		// compute maps for best plan data

		Map<Pair<InputArtifact, Technique>, List<Integer>> coveredElementsAutomatic = new HashMap<>();

		for (Pair<InputArtifact, Technique> pair : includedArtifactTechniquePairs) {
			coveredElementsAutomatic.put(pair, viewpointDao.getCoveredElements(null, pair, null));
		}

		Map<Stakeholder, List<Integer>> coveredElementsManual = new HashMap<>();

		for (Stakeholder stakeholder : includedStakeholders) {
			coveredElementsManual.put(stakeholder, viewpointDao.getCoveredElements(null, stakeholder, null));
		}

		Comparator<? super Entry<Pair<InputArtifact, Technique>, List<Integer>>> pairComparator = new Comparator<Entry<Pair<InputArtifact, Technique>, List<Integer>>>() {
			public int compare(Entry<Pair<InputArtifact, Technique>, List<Integer>> obj1,
					Entry<Pair<InputArtifact, Technique>, List<Integer>> obj2) {
				if (obj1.getValue().size() < obj2.getValue().size()) {
					return 1;
				} else if (obj1.getValue().size() > obj2.getValue().size()) {
					return -1;
				} else {
					return 0;
				}
			}
		};
		Map<Pair<InputArtifact, Technique>, List<Integer>> sortedMapAutomatic = coveredElementsAutomatic.entrySet()
				.stream().sorted(pairComparator)
				.collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue(), (e1, e2) -> e2, LinkedHashMap::new));

		Comparator<? super Entry<Stakeholder, List<Integer>>> stakeholderComparator = new Comparator<Entry<Stakeholder, List<Integer>>>() {
			public int compare(Entry<Stakeholder, List<Integer>> obj1, Entry<Stakeholder, List<Integer>> obj2) {
				if (obj1.getValue().size() < obj2.getValue().size()) {
					return 1;
				} else if (obj1.getValue().size() > obj2.getValue().size()) {
					return -1;
				} else {
					return 0;
				}
			}
		};
		Map<Stakeholder, List<Integer>> sortedMapManual = coveredElementsManual.entrySet().stream()
				.sorted(stakeholderComparator)
				.collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue(), (e1, e2) -> e2, LinkedHashMap::new));

		Set<Integer> visitedElements = new HashSet<>();

		boolean stepsStop = false;
		while (!stepsStop) {

			Entry<Pair<InputArtifact, Technique>, List<Integer>> localOptimalAutomatic = null;
			if (!sortedMapAutomatic.isEmpty()) {
				localOptimalAutomatic = sortedMapAutomatic.entrySet().iterator().next();
			}

			Entry<Stakeholder, List<Integer>> localOptimalManual = null;
			if (!sortedMapManual.isEmpty()) {
				localOptimalManual = sortedMapManual.entrySet().iterator().next();
			}

			PlanStep step = new PlanStep();

			int uniqueAutomaticElementIds = 0;
			if(localOptimalAutomatic!=null && !localOptimalAutomatic.getValue().isEmpty()) {
				uniqueAutomaticElementIds = localOptimalAutomatic.getValue().stream().filter(e -> {
					return !visitedElements.contains(e);
				}).collect(Collectors.toList()).size();
			}

			int uniqueManualElementIds = 0;
			if(localOptimalManual!=null && !localOptimalManual.getValue().isEmpty()) {
				uniqueManualElementIds = localOptimalManual.getValue().stream().filter(e -> {
					return !visitedElements.contains(e);
				}).collect(Collectors.toList()).size();
			}

			if ((localOptimalAutomatic != null && localOptimalManual == null)
					|| (localOptimalAutomatic != null && localOptimalManual != null
							&& uniqueAutomaticElementIds >= uniqueManualElementIds)) {
				step.setArtifact(localOptimalAutomatic.getKey().getLeft());
				step.setTechnique(localOptimalAutomatic.getKey().getRight());
				localOptimalAutomatic.getValue().removeAll(visitedElements);
				visitedElements.addAll(localOptimalAutomatic.getValue());
				List<ViewpointElement> elements = viewpointElementDao.getElementsById(localOptimalAutomatic.getValue());
				step.setElements(elements);

				sortedMapAutomatic.remove(localOptimalAutomatic.getKey());
			} else if ((localOptimalManual != null && localOptimalAutomatic == null)
					|| (localOptimalManual != null && localOptimalAutomatic != null
							&& uniqueManualElementIds > uniqueAutomaticElementIds)) {
				step.setStakeholder(localOptimalManual.getKey());

				List<Integer> allElements = localOptimalManual.getValue();
				List<Integer> refinedElementIds = allElements.stream().filter(e -> {
					return visitedElements.contains(e);
				}).collect(Collectors.toList());

				localOptimalManual.getValue().removeAll(visitedElements);
				visitedElements.addAll(localOptimalManual.getValue());
				List<ViewpointElement> elements = viewpointElementDao.getElementsById(localOptimalManual.getValue());
				step.setElements(elements);

				localOptimalManual.getValue().removeAll(visitedElements);
				List<ViewpointElement> refinedElements = viewpointElementDao.getElementsById(refinedElementIds);
				step.setRefinedElements(refinedElements);

				sortedMapManual.remove(localOptimalManual.getKey());
			}

			bestPlan.addStep(step);

			if (sortedMapAutomatic.isEmpty() && sortedMapManual.isEmpty()) {
				stepsStop = true;
			}
		}

		return bestPlan;
	}

	private void loadCachedData() {
		viewpointSizeMap = new HashMap<>();
		for (Viewpoint viewpoint : selectedViewpoints) {
			viewpointSizeMap.put(viewpoint, viewpoint.getElements().size());
		}
	}

	private Double fitness(Genotype<IntegerGene> genotype) {
		long time = System.currentTimeMillis();
		double fitness = 0;

		List<Pair<InputArtifact, Technique>> includedArtifactTechniquePairs = decodeArtifactTechniquePairs(genotype);
		List<Stakeholder> includedStakeholders = decodeStakeholders(genotype);

		Set<InputArtifact> includedArtifacts = includedArtifactTechniquePairs.stream()
				.map(Pair<InputArtifact, Technique>::getLeft).collect(Collectors.toSet());
		Set<Integer> totalCoveredElements;

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

		// compute weigths
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

		// compute only automatic
		Map<Viewpoint, Set<Integer>> elementsCoveredByViewpointArtifact = new HashMap<>();
		if (w1 > 0 || w7 > 0 || w3 > 0 || w9 > 0) {
			for (Viewpoint viewpoint : selectedViewpoints) {
				totalCoveredElements = new HashSet<>();

				for (Pair<InputArtifact, Technique> pair : includedArtifactTechniquePairs) {
					totalCoveredElements.addAll(viewpointDao.getCoveredElements(viewpoint, pair, null));
				}

				elementsCoveredByViewpointArtifact.put(viewpoint, totalCoveredElements);
				final long totalElementsByViewpoint = viewpointSizeMap.get(viewpoint).longValue();
				final double percentage = (double) totalCoveredElements.size() / totalElementsByViewpoint;
				percentageTotalArtifacts += percentage;
				if (totalCoveredElements.size() == totalElementsByViewpoint) {
					percentageCompletedViewpointArtifacts++;
				}
			}
			percentageCompletedViewpointArtifacts = percentageCompletedViewpointArtifacts / selectedViewpoints.size();
			percentageMeanArtifacts = percentageTotalArtifacts / includedArtifactTechniquePairs.size();
		}

		// compute only manual
		if (w2 > 0 || w8 > 0) {
			for (Viewpoint viewpoint : selectedViewpoints) {
				totalCoveredElements = new HashSet<>();

				for (Stakeholder stakeholder : includedStakeholders) {
					totalCoveredElements.addAll(viewpointDao.getCoveredElements(viewpoint, stakeholder, null));
				}

				final long totalElementsByViewpoint = viewpointSizeMap.get(viewpoint).longValue();
				final double percentage = (double) totalCoveredElements.size() / totalElementsByViewpoint;
				percentageTotalStakeholders += percentage;
				if (totalCoveredElements.size() == totalElementsByViewpoint) {
					percentageCompletedViewpointStakeholders++;
				}
			}

			percentageCompletedViewpointStakeholders = percentageCompletedViewpointStakeholders
					/ selectedViewpoints.size();
			percentageMeanStakeholders = percentageTotalStakeholders / includedStakeholders.size();
		}

		// compute for combined (automatic and then manual)
		if (w3 > 0 || w9 > 0) {
			Set<Integer> alreadyCovered;
			for (Viewpoint viewpoint : selectedViewpoints) {
				alreadyCovered = elementsCoveredByViewpointArtifact.get(viewpoint);
				totalCoveredElements = new HashSet<>();
				totalCoveredElements.addAll(alreadyCovered);

				for (Stakeholder stakeholder : includedStakeholders) {
					totalCoveredElements
							.addAll(viewpointDao.getCoveredElements(viewpoint, stakeholder, alreadyCovered));
				}

				final long totalElementsByViewpoint = viewpointSizeMap.get(viewpoint).longValue();
				final double percentage = (double) totalCoveredElements.size() / totalElementsByViewpoint;
				percentageTotalCombined += percentage;
				if (totalCoveredElements.size() == totalElementsByViewpoint) {
					percentageCompletedViewpointCombined++;
				}
			}

			percentageCompletedViewpointCombined = percentageCompletedViewpointCombined / selectedViewpoints.size();
			percentageMeanCombined = percentageTotalCombined
					/ (includedArtifactTechniquePairs.size() + includedStakeholders.size());
		}

		// aggregate values into a single fitness value
		fitness = w1 * percentageMeanArtifacts + w2 * percentageMeanStakeholders + w3 * percentageMeanCombined
				+ w4 * percentageNotUsedArtefacts + w5 * percentageNotUsedStakeholders + w6 * percentageNotUsedCombined
				+ w7 * percentageCompletedViewpointArtifacts + w8 * percentageCompletedViewpointStakeholders
				+ w9 * percentageCompletedViewpointCombined;

		time = System.currentTimeMillis() - time;

		LOGGER.info("Fitness (in " + time + " ms): " + fitness);

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
