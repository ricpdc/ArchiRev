package es.alarcos.archirev.logic.bestplan;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import es.alarcos.archirev.controller.ViewpointController;
import es.alarcos.archirev.model.InputArtifact;
import es.alarcos.archirev.model.Stakeholder;
import es.alarcos.archirev.model.Viewpoint;
import es.alarcos.archirev.persistency.InputArtifactDao;
import es.alarcos.archirev.persistency.QueriedViewpointDTO;
import es.alarcos.archirev.persistency.StakeholderDao;
import es.alarcos.archirev.persistency.ViewpointDao;
import es.alarcos.archirev.persistency.ViewpointElementDao;
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
public class GeneticAlgorithmBestPlanService {
	
	static Logger LOGGER = LoggerFactory.getLogger(GeneticAlgorithmBestPlanService.class);

	private static final int NUM_CHROMOSOMES = 2;
	private static final int[] NUM_GENES_BY_CHROMOSOME = { 5, 8 };
	
	private List<InputArtifact> artifacts;
	private List<Stakeholder> stakeholders;
	private List<Viewpoint> availableViewpoints;

	
	@Autowired
	private StakeholderDao stakeholderDao;

	@Autowired
	private ViewpointDao viewpointDao;

	@Autowired
	private InputArtifactDao inputArtifactDao;

	
	public GeneticAlgorithmBestPlanService() {
		super();
	}	
	
	public GeneticAlgorithmBestPlanService(List<InputArtifact> artifacts, List<Stakeholder> stakeholders) {
		super();
		this.artifacts = artifacts;
		this.stakeholders = stakeholders;
	}
	
	@PostConstruct
	public void init() {
		availableViewpoints = viewpointDao.findAll();
	}

	

	
	public BestPlan computeBestPlan() {
		BestPlan bestPlan = new BestPlan();
		
		Factory<Genotype<IntegerGene>> gtf = Genotype.of(
				IntegerChromosome.of(0, 1, artifacts.size()),
				IntegerChromosome.of(0, 1, stakeholders.size()));
		
//		Genotype<IntegerGene> newInstance = gtf.newInstance();
//		eval(newInstance);
//		System.out.println("Before the evolution:\n" + newInstance + "-->" + eval(newInstance));

		Engine<IntegerGene, Double> engine = Engine.builder(this::fitness, gtf)
				.maximizing()
				.alterers(new UniformCrossover<>(0.6), new Mutator<>(0.3))
				.build();

		Genotype<IntegerGene> result = engine.stream().limit(100).collect(EvolutionResult.toBestGenotype());

		System.out.println("After the evolution:\n" + result + "-->" + fitness(result));
		
		bestPlan.setArtifacts(decodeArtifacts(result));
		bestPlan.setStakeholders(decodeStakeholders(result));
		return bestPlan;

	}
	
	private Double fitness(Genotype<IntegerGene> genotype) {
		double fitness = 0;
		
		List<InputArtifact> includedArtifacts = decodeArtifacts(genotype);
		List<Stakeholder> includeStakeholders = decodeStakeholders(genotype);
		
		
		//compute viewpoint values
		List<QueriedViewpointDTO> maxPercentageByArtefacts = viewpointDao.listViewpointsMaxPercentageByArtefacts(includedArtifacts);

		
		HashMap<String, QueriedViewpointDTO> queriedViewpointMap = new HashMap<String, QueriedViewpointDTO>();
		for (Viewpoint viewpoint : availableViewpoints) {
			for (QueriedViewpointDTO viewpointDTO : maxPercentageByArtefacts) {
				if (viewpointDTO.getId().equals(viewpoint.getId())) {
					queriedViewpointMap.put(viewpoint.getName(), viewpointDTO);
					break;
				}
			}
		}
		
		List<Long> queriedElementIds = viewpointDao.getCoveredElementsByArtifacts(includedArtifacts);

		List<QueriedViewpointDTO> maxPercentageByStakeholders = viewpointDao.listViewpointsMaxPercentageByStakeholder(
				includeStakeholders, queriedElementIds, queriedViewpointMap);
		
		
		//aggregate values into a single fitness value
		for (QueriedViewpointDTO dto : maxPercentageByStakeholders) {
			fitness += Math.max(dto.getMaxPercentageElementsManual(), dto.getMaxPercentageElementsAutomatic());
		}
		
		fitness = fitness / (includedArtifacts.size() + includeStakeholders.size());
		
		LOGGER.info("Fitness: " + fitness);
		
		return fitness;
	}

	private List<InputArtifact> decodeArtifacts(Genotype<IntegerGene> genotype) {
		Chromosome<IntegerGene> chromosomeArtifacts = genotype.get(0);
		List<InputArtifact> includedArtifacts = new ArrayList<>();
		for(int a = 0; a<chromosomeArtifacts.length(); a++) {
			if(chromosomeArtifacts.getGene(a).intValue()==1){
				includedArtifacts.add(artifacts.get(a));
			}
		}
		return includedArtifacts;
	}
	
	private List<Stakeholder> decodeStakeholders(Genotype<IntegerGene> genotype) {
		Chromosome<IntegerGene> chromosomeStakeholders = genotype.get(1);
		List<Stakeholder> includedStakeholders = new ArrayList<>();
		for(int s = 0; s<chromosomeStakeholders.length();s++) {
			if(chromosomeStakeholders.getGene(s).intValue()==1){
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
	}

	public List<Stakeholder> getStakeholders() {
		return stakeholders;
	}

	public void setStakeholders(List<Stakeholder> stakeholders) {
		this.stakeholders = stakeholders;
		Collections.sort(this.stakeholders);
	}

	

}
