package es.alarcos.archirev.controller;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.primefaces.context.RequestContext;
import org.primefaces.event.ToggleEvent;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.DualListModel;
import org.primefaces.model.StreamedContent;
import org.primefaces.model.Visibility;
import org.primefaces.model.chart.Axis;
import org.primefaces.model.chart.AxisType;
import org.primefaces.model.chart.BarChartModel;
import org.primefaces.model.chart.ChartSeries;
import org.primefaces.model.chart.HorizontalBarChartModel;
import org.primefaces.model.chart.LineChartModel;
import org.primefaces.model.chart.MeterGaugeChartModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import es.alarcos.archirev.logic.bestplan.BestPlan;
import es.alarcos.archirev.logic.bestplan.BestPlanService;
import es.alarcos.archirev.model.AbstractEntity;
import es.alarcos.archirev.model.InputArtifact;
import es.alarcos.archirev.model.Stakeholder;
import es.alarcos.archirev.model.Viewpoint;
import es.alarcos.archirev.model.ViewpointElement;
import es.alarcos.archirev.model.enums.ViewpointSimulationEnum;
import es.alarcos.archirev.persistency.ConcernDao;
import es.alarcos.archirev.persistency.InputArtifactDao;
import es.alarcos.archirev.persistency.PurposeDao;
import es.alarcos.archirev.persistency.QueriedViewpointDTO;
import es.alarcos.archirev.persistency.ScopeDao;
import es.alarcos.archirev.persistency.StakeholderDao;
import es.alarcos.archirev.persistency.ViewpointDao;
import es.alarcos.archirev.persistency.ViewpointElementDao;
import io.jenetics.IntegerGene;
import io.jenetics.engine.EvolutionDurations;
import io.jenetics.engine.EvolutionResult;

@ManagedBean(name = "viewpointController")
@Controller
@ViewScoped
public class ViewpointController extends AbstractController {

	private static final long serialVersionUID = -7943630748807472984L;

	private static Logger logger = LoggerFactory.getLogger(ViewpointController.class);

	@Autowired
	private SessionController sessionController;

	@Autowired
	private ViewpointDao viewpointDao;

	@Autowired
	private ScopeDao scopeDao;

	@Autowired
	private ConcernDao concernDao;

	@Autowired
	private PurposeDao purposeDao;

	@Autowired
	private StakeholderDao stakeholderDao;

	@Autowired
	private ViewpointElementDao viewpointElementDao;

	@Autowired
	private InputArtifactDao inputArtifactDao;

	@Autowired
	private BestPlanService bestPlanService;

	private Viewpoint selectedViewpoint;

	private boolean[] activeViewpoint;

	private List<Viewpoint> availableViewpoints;
	private List<Viewpoint> selectedViewpoints;
	private List<String> scopeItems;
	private List<String> stakeholdersItems;
	private List<String> concernItems;
	private List<String> purposeItems;
	private List<String> elementItems;
	private List<ViewpointElement> allElements;
	private List<ViewpointElement> selectedBestPlanElements;

	private String priorityBestPlan;
	private String maximizationBestPlan;

	private boolean coloured;

	private DualListModel<InputArtifact> artifactPickerList = new DualListModel<>(new ArrayList<InputArtifact>(),
			new ArrayList<InputArtifact>());
	private DualListModel<Stakeholder> stakeholderPickerList = new DualListModel<>(new ArrayList<Stakeholder>(),
			new ArrayList<Stakeholder>());

	private Map<String, QueriedViewpointDTO> queriedViewpointMap = new HashMap<String, QueriedViewpointDTO>();

	private ArrayList<Pair<String, Integer>> techniquesFromSelectedViewpoint;

	private ArrayList<Pair<String, Integer>> stakeholdersFromSelectedViewpoint;

	private String selectedTechnique;

	private ArrayList<String> elementsFromSelectedTechnique;

	private ViewpointSimulationEnum simulationType = ViewpointSimulationEnum.AUTOMATIC;

	private List<Long> queriedElementIds;

	private BestPlan bestPlan;
	
	private LineChartModel bestPlanEvolutionPlot;

	public ViewpointController() {
		super();
	}

	@PostConstruct
	public void init() {
		super.init();

		reload();
	}

	public void reload() {
		if (sessionController.isActiveViewpoints()) {
			availableViewpoints = viewpointDao.findAllOrderById();
			scopeItems = scopeDao.getScopeNames();
			stakeholdersItems = stakeholderDao.getStakeholdersNames();
			concernItems = concernDao.getConcernNames();
			purposeItems = purposeDao.getPurposeNames();
			elementItems = viewpointElementDao.getElementNames();
			allElements = viewpointElementDao.findAll();

			artifactPickerList = new DualListModel<>(inputArtifactDao.findAll(), new ArrayList<InputArtifact>());
			stakeholderPickerList = new DualListModel<>(stakeholderDao.findAll(), new ArrayList<Stakeholder>());

			initializeActiveViewpoints();

			RequestContext.getCurrentInstance().update("mainForm:viewpointsTabs");

			coloured = false;
			queriedViewpointMap = new HashMap<String, QueriedViewpointDTO>();
			queriedElementIds = null;

			loadDefaultSetup();
			updateBestPlanEvolutionPlot();
		}
	}

	private void initializeActiveViewpoints() {
		selectedViewpoints = new ArrayList<>();
		activeViewpoint = new boolean[availableViewpoints.size() + 1];

		for (Viewpoint viewpoint : availableViewpoints) {
			activeViewpoint[viewpoint.getId().intValue()] = true;
			selectedViewpoints.add(viewpoint);
		}
	}

	public void toggleActiveValue(String viewpointName) {
		for (Viewpoint viewpoint : availableViewpoints) {
			if (viewpointName.equals(viewpoint.getName())) {
				if (activeViewpoint[viewpoint.getId().intValue()]) {
					if (!selectedViewpoints.contains(viewpoint)) {
						selectedViewpoints.add(viewpoint);
					}
				} else {
					selectedViewpoints.remove(viewpoint);
				}
				break;
			}
		}
	}

	public void onRowToggle(ToggleEvent event) {
		if (event.getVisibility().equals(Visibility.VISIBLE)) {
			logger.debug("Expanded: " + ((Viewpoint) event.getData()).getName());
		}
	}

	public StreamedContent getImageFromBytes(byte[] bytes) throws IOException {
		if (bytes == null) {
			return null;
		}
		return new DefaultStreamedContent(new ByteArrayInputStream(bytes), "image/png");
	}

	public List<String> getScopeItems() {
		return scopeItems;
	}

	public void setScopeItems(List<String> scopeItems) {
		this.scopeItems = scopeItems;
	}

	public List<String> getStakeholdersItems() {
		return stakeholdersItems;
	}

	public void setStakeholdersItems(List<String> stakeholdersItems) {
		this.stakeholdersItems = stakeholdersItems;
	}

	@SuppressWarnings("unchecked")
	public boolean filterByName(Object value, Object filter, Locale locale) {
		List<String> filterList = (filter == null) ? null : new ArrayList<String>(Arrays.asList((String[]) filter));
		if (filterList == null || filterList.isEmpty()) {
			return true;
		}
		if (value == null) {
			return false;
		}
		List<AbstractEntity> valueList = (List<AbstractEntity>) value;
		for (AbstractEntity entity : valueList) {
			if (filterList.contains(entity.getName())) {
				return true;
			}
		}
		return false;
	}

	public List<String> getPurposeItems() {
		return purposeItems;
	}

	public void setPurposeItems(List<String> purposeItems) {
		this.purposeItems = purposeItems;
	}

	public List<String> getConcernItems() {
		return concernItems;
	}

	public void setConcernItems(List<String> concernItems) {
		this.concernItems = concernItems;
	}

	public List<String> getElementItems() {
		return elementItems;
	}

	public void setElementItems(List<String> elementItems) {
		this.elementItems = elementItems;
	}

	public Viewpoint getSelectedViewpoint() {
		return selectedViewpoint;
	}

	public void setSelectedViewpoint(Viewpoint selectedViewpoint) {
		this.selectedViewpoint = selectedViewpoint;
	}

	public QueriedViewpointDTO getSelectedViewpointDTO() {
		if (selectedViewpoint == null || queriedViewpointMap == null) {
			return null;
		}
		return queriedViewpointMap.get(selectedViewpoint.getName());
	}

	public List<ViewpointElement> getAllElements() {
		return allElements;
	}

	public void setAllElements(List<ViewpointElement> allElements) {
		this.allElements = allElements;
	}

	public boolean isElementValid(ViewpointElement element) {

		return selectedViewpoint != null && selectedViewpoint.getElements().contains(element);
	}

	public DualListModel<InputArtifact> getArtifactPickerList() {
		return artifactPickerList;
	}

	public void setArtifactPickerList(DualListModel<InputArtifact> artifactPickerList) {
		this.artifactPickerList = artifactPickerList;
	}

	public DualListModel<Stakeholder> getStakeholderPickerList() {
		return stakeholderPickerList;
	}

	public void setStakeholderPickerList(DualListModel<Stakeholder> stakeholderPickerList) {
		this.stakeholderPickerList = stakeholderPickerList;
	}

	public void showViewpointInfoAutomatic(String viewpointName) {
		logger.info("Showing " + viewpointName);
		if (getPercentageAutomatic(viewpointName) == 0.0) {
			return;
		}
		for (Viewpoint viewpoint : selectedViewpoints) {
			if (viewpoint.getName().equals(viewpointName)) {
				selectedViewpoint = viewpoint;
				break;
			}
		}

		queriedViewpointMap.put(viewpointName,
				viewpointDao.getViewpointPercentagesByArtefacts(
						bestPlan != null ? bestPlan.getArtifacts() : artifactPickerList.getTarget(),
						getSelectedViewpointDTO()));
		loadTechniquesFromSelectedViewpointDTO();

		if (selectedViewpoint != null) {
			RequestContext context = RequestContext.getCurrentInstance();
			context.update("mainForm:viewpointDialogAutomatic");
			context.update("mainForm:stakeholderList_elementsTechniqueTable");
			context.execute("PF('viewpointDialogAutomatic').show()");
		}

	}

	public void showViewpointInfoManual(String viewpointName) {
		logger.info("Showing " + viewpointName);
		if (getPercentageManual(viewpointName) == 0.0) {
			return;
		}
		for (Viewpoint viewpoint : selectedViewpoints) {
			if (viewpoint.getName().equals(viewpointName)) {
				selectedViewpoint = viewpoint;
				break;
			}
		}

		queriedViewpointMap.put(viewpointName,
				viewpointDao.getViewpointPercentagesByStakeholders(
						bestPlan != null ? bestPlan.getStakeholders() : stakeholderPickerList.getTarget(),
						getSelectedViewpointDTO(), (isHybridSimulation() ? queriedElementIds : null)));
		loadStakeholdersFromSelectedViewpointDTO();

		if (selectedViewpoint != null) {
			RequestContext context = RequestContext.getCurrentInstance();
			context.update("mainForm:viewpointDialogManual");
			context.update("mainForm:stakeholderList_elementsTechniqueTable");
			context.execute("PF('viewpointDialogManual').show()");
		}

	}

	public void showElementsForTechnique(String techniqueName) {
		selectedTechnique = techniqueName;
		setElementsFromSelectedTechnique(
				new ArrayList<String>(getSelectedViewpointDTO().getElementsByTechnique().get(selectedTechnique)));

		if (selectedViewpoint != null) {
			RequestContext context = RequestContext.getCurrentInstance();
			context.update("mainForm:elementsTechniqueDialog");
			context.execute("PF('elementsTechniqueDialog').show()");
		}

	}

	public boolean isElementCovered(ViewpointElement element) {
		return getElementsFromSelectedTechnique() != null
				&& getElementsFromSelectedTechnique().contains(element.getName());
	}

	public boolean isElementCoveredByStakeholder(ViewpointElement element, Stakeholder stakeholder) {
		if (element != null && getSelectedViewpointDTO() != null
				&& getSelectedViewpointDTO().getElementsByStakeholder() != null
				&& getSelectedViewpointDTO().getElementsByStakeholder().get(stakeholder.getName()) != null) {
			return getSelectedViewpointDTO().getElementsByStakeholder().get(stakeholder.getName())
					.contains(element.getName());
		}
		return false;
	}

	private List<Pair<String, Integer>> loadStakeholdersFromSelectedViewpointDTO() {
		stakeholdersFromSelectedViewpoint = new ArrayList<Pair<String, Integer>>();
		if (getSelectedViewpointDTO() == null) {
			return stakeholdersFromSelectedViewpoint;
		}
		Set<String> keySet = getSelectedViewpointDTO().getElementsByStakeholder().keySet();
		if (keySet == null) {
			return stakeholdersFromSelectedViewpoint;
		}
		for (String key : keySet) {
			stakeholdersFromSelectedViewpoint.add(new ImmutablePair<String, Integer>(key,
					getSelectedViewpointDTO().getElementsByStakeholder().get(key).size()));
		}
		return stakeholdersFromSelectedViewpoint;
	}

	private List<Pair<String, Integer>> loadTechniquesFromSelectedViewpointDTO() {
		techniquesFromSelectedViewpoint = new ArrayList<Pair<String, Integer>>();
		if (getSelectedViewpointDTO() == null) {
			return techniquesFromSelectedViewpoint;
		}
		Set<String> keySet = getSelectedViewpointDTO().getElementsByTechnique().keySet();
		if (keySet == null) {
			return techniquesFromSelectedViewpoint;
		}
		for (String key : keySet) {
			techniquesFromSelectedViewpoint.add(new ImmutablePair<String, Integer>(key,
					getSelectedViewpointDTO().getElementsByTechnique().get(key).size()));
		}
		return techniquesFromSelectedViewpoint;
	}

	public double getFormattedPercentageAutomatic(String viewpointName) {
		return BigDecimal.valueOf(getPercentageAutomatic(viewpointName)).setScale(1, BigDecimal.ROUND_FLOOR).doubleValue();
	}

	public double getFormattedPercentageManual(String viewpointName) {
		return BigDecimal.valueOf(getPercentageManual(viewpointName)).setScale(1, BigDecimal.ROUND_FLOOR).doubleValue();
	}

	private double getPercentageAutomatic(String viewpointName) {
		QueriedViewpointDTO queriedViewpointDTO = queriedViewpointMap.get(viewpointName);
		if (queriedViewpointDTO != null) {
			return queriedViewpointDTO.getMaxPercentageElementsAutomatic();
		}
		return 0.0;
	}

	private double getPercentageManual(String viewpointName) {
		QueriedViewpointDTO queriedViewpointDTO = queriedViewpointMap.get(viewpointName);
		if (queriedViewpointDTO != null) {
			return queriedViewpointDTO.getMaxPercentageElementsManual();
		}
		return 0.0;
	}

	public double getPercentage(String viewpointName) {
		switch (simulationType) {
		case MANUAL:
			return getPercentageManual(viewpointName);
		case AUTOMATIC:
			return getPercentageAutomatic(viewpointName);
		case HYBRID:
			return getPercentageAutomatic(viewpointName) + getFormattedPercentageManual(viewpointName);
		default:
			return 0.0;
		}
	}

	public void simulateViewpointsByArtifact() {
		simulateViewpointsByArtifact(artifactPickerList.getTarget());
	}

	private void simulateViewpointsByArtifact(List<InputArtifact> artifacts) {
		simulationType = ViewpointSimulationEnum.AUTOMATIC;
		List<QueriedViewpointDTO> listViewpointsByArtefacts = viewpointDao
				.listViewpointsMaxPercentageByArtefacts(artifacts, selectedViewpoints);
		queriedViewpointMap = new HashMap<String, QueriedViewpointDTO>();
		for (Viewpoint viewpoint : selectedViewpoints) {
			for (QueriedViewpointDTO viewpointDTO : listViewpointsByArtefacts) {
				if (viewpointDTO.getId().equals(viewpoint.getId())) {
					queriedViewpointMap.put(viewpoint.getName(), viewpointDTO);
					break;
				}
			}
		}
		coloured = true;
	}

	public void simulateViewpointsByStakeholder() {
		simulateViewpointsByStakeholder(stakeholderPickerList.getTarget());
	}

	private void simulateViewpointsByStakeholder(final List<Stakeholder> stakeholders) {
		simulationType = ViewpointSimulationEnum.MANUAL;
		queriedElementIds = null;
		List<QueriedViewpointDTO> listViewpointsByStakeholders = viewpointDao
				.listViewpointsMaxPercentageByStakeholder(stakeholders, selectedViewpoints, null, null);
		queriedViewpointMap = new HashMap<String, QueriedViewpointDTO>();
		for (Viewpoint viewpoint : selectedViewpoints) {
			for (QueriedViewpointDTO viewpointDTO : listViewpointsByStakeholders) {
				if (viewpointDTO.getId().equals(viewpoint.getId())) {
					queriedViewpointMap.put(viewpoint.getName(), viewpointDTO);
					break;
				}
			}
		}
		coloured = true;
	}

	public void simulateViewpointsByArtifactAndStakeholder() {
		simulateViewpointsByArtifactAndStakeholder(artifactPickerList.getTarget(), stakeholderPickerList.getTarget());
	}

	public void simulateViewpointsByArtifactAndStakeholder(List<InputArtifact> artifacts,
			List<Stakeholder> stakeholders) {
		simulationType = ViewpointSimulationEnum.HYBRID;
		List<QueriedViewpointDTO> listViewpointsByArtefacts = viewpointDao
				.listViewpointsMaxPercentageByArtefacts(artifacts, selectedViewpoints);
		queriedViewpointMap = new HashMap<String, QueriedViewpointDTO>();
		for (Viewpoint viewpoint : selectedViewpoints) {
			for (QueriedViewpointDTO viewpointDTO : listViewpointsByArtefacts) {
				if (viewpointDTO.getId().equals(viewpoint.getId())) {
					queriedViewpointMap.put(viewpoint.getName(), viewpointDTO);
					break;
				}
			}
		}

		queriedElementIds = viewpointDao.getCoveredElementsByArtifacts(
				bestPlan != null ? bestPlan.getArtifacts() : artifactPickerList.getTarget());

		List<QueriedViewpointDTO> listViewpointsByStakeholders = viewpointDao.listViewpointsMaxPercentageByStakeholder(
				stakeholders, selectedViewpoints, queriedElementIds, queriedViewpointMap);

		for (Viewpoint viewpoint : selectedViewpoints) {
			for (QueriedViewpointDTO viewpointDTO : listViewpointsByStakeholders) {
				if (viewpointDTO.getId().equals(viewpoint.getId())) {
					queriedViewpointMap.put(viewpoint.getName(), viewpointDTO);
					break;
				}
			}
		}
		coloured = true;
	}

	public void computeBestPlan() {
		// Input
		bestPlanService.setArtifacts(artifactPickerList.getTarget());
		bestPlanService.setStakeholders(stakeholderPickerList.getTarget());
		bestPlanService.setSelectedViewpoints(selectedViewpoints);
		// Parameters
		bestPlanService.setMaximizationBestPlan(maximizationBestPlan);
		bestPlanService.setPriorityBestPlan(priorityBestPlan);
		// Run genetic algorithm
		bestPlan = bestPlanService.computeBestPlan();

		if(bestPlan != null) {
			generateEvolutionStats();

			computeHeatMapForBestPlan();
			updateBestPlanEvolutionPlot();
		}
	}

	private void generateEvolutionStats() {
		// TODO generate a CSV or similar from evolution data
		List<EvolutionResult<IntegerGene,Double>> evolution = bestPlanService.getEvolution();
		File file = new File("C:\\Temp\\genetic\\genetic_stats_"+System.currentTimeMillis()+".csv");
		
		FileWriter csvWriter = null;
		try{
			file.createNewFile();
			csvWriter = new FileWriter(file);
			String header = "#Gen;Fitness Avg.;Fitness Min;Fitness Max;Individuals;Altered;Killed;Invalids;Offspring Alter Time;Offspring Filter Time;"+
					"Offspring Selection Time;Survivor Filter Time;Survivor Selection Time;Evaluation Time;Evolve Time\n";
			csvWriter.append(header);
			String line="";
			for (int i = 0; i < evolution.size(); i++) {
				EvolutionResult<IntegerGene, Double> generation = evolution.get(i);
				
				line = "" + generation.getGeneration();
				
				line += (";" + generation.getPopulation().stream().mapToDouble(g-> g.getFitness()).average().getAsDouble()).replace(".", ",");
				line += (";" + generation.getPopulation().stream().mapToDouble(g-> g.getFitness()).min().getAsDouble()).replace(".", ",");
				line += (";" + generation.getPopulation().stream().mapToDouble(g-> g.getFitness()).max().getAsDouble()).replace(".", ",");
							
				line += (";" + generation.getPopulation().size());
	
				line += (";" + generation.getAlterCount());
				line += (";" + generation.getKillCount());
				line += (";" + generation.getInvalidCount());
				
				final EvolutionDurations durations = generation.getDurations();
				line += (";" + (durations.getOffspringAlterDuration().getSeconds() * 1000000000 + durations.getOffspringAlterDuration().getNano()));
				line += (";" + (durations.getOffspringFilterDuration().getSeconds() * 1000000000 + durations.getOffspringFilterDuration().getNano()));
				line += (";" + (durations.getOffspringSelectionDuration().getSeconds() * 1000000000 + durations.getOffspringSelectionDuration().getNano()));
				line += (";" + (durations.getSurvivorFilterDuration().getSeconds() * 1000000000 + durations.getSurvivorFilterDuration().getNano()));
				line += (";" + (durations.getSurvivorsSelectionDuration().getSeconds() * 1000000000 + durations.getSurvivorsSelectionDuration().getNano()));
				line += (";" + (durations.getEvaluationDuration().getSeconds() * 1000000000 + durations.getEvaluationDuration().getNano())); // the duration needed for evaluating the fitness function of the new individuals
				line += (";" + (durations.getEvolveDuration().getSeconds() * 1000000000 + durations.getEvolveDuration().getNano()));
				
				line += "\n";
				
				csvWriter.append(line);
			}
			csvWriter.flush();
			csvWriter.close();
		}catch(IOException ioe) {
			logger.error("Error generating genetic stats");
			ioe.printStackTrace();
		}
		finally {
			try {
				if(csvWriter!=null) {
					csvWriter.close();
				}
			} catch (IOException e) {
				logger.error(e.getMessage());
			}
		}
		
		
	}

	public Integer getProgressForBestPlan() {
		int progress = bestPlanService.getProgress();
		if (progress > 100) {
			progress = 100;
		}
		return new Integer(progress);
	}

	public void setProgressForBestPlan(Integer progress) {
		return;
	}
	
	public double getBestPhenotypeValue () {
		double value = bestPlanService.getBestPhenotype() != null ? bestPlanService.getBestPhenotype().getFitness() : 0.0;
		return BigDecimal.valueOf(value).setScale(4, BigDecimal.ROUND_FLOOR).doubleValue();
	}
	
	public void updateBestPlanEvolutionPlot() {
		bestPlanEvolutionPlot = new LineChartModel();
		ChartSeries max = new ChartSeries();
		ChartSeries min = new ChartSeries();
		bestPlanEvolutionPlot.addSeries(max);
		bestPlanEvolutionPlot.addSeries(min);
		
		max.setLabel("max");
		min.setLabel("min");

		bestPlanEvolutionPlot.setTitle("Genetic Algorithm Evolution");
		bestPlanEvolutionPlot.getAxis(AxisType.X).setLabel("Generations");
		bestPlanEvolutionPlot.getAxis(AxisType.Y).setLabel("Fitness");
		
		bestPlanEvolutionPlot.setShowPointLabels(true);
		bestPlanEvolutionPlot.setLegendPosition("e");

		if(bestPlanService.getEvolution() == null) {
			return;
		}
		
		for (int i = 0; i < bestPlanService.getEvolution().size(); i++) {
			EvolutionResult <IntegerGene, Double> generation = bestPlanService.getEvolution().get(i);
			max.set(i+1, generation.getBestFitness());
			min.set(i+1, generation.getWorstFitness());
		}
	}

	private void computeHeatMapForBestPlan() {
		// TODO after sort and filtering by technique is computed it should be fixed by
		// adding such filters to the db query to improve accuracy
		simulateViewpointsByArtifactAndStakeholder(bestPlan.getArtifacts(), bestPlan.getStakeholders());
	}

	public boolean isColoured() {
		return coloured;
	}

	public void setColoured(boolean coloured) {
		this.coloured = coloured;
	}

	public Map<String, QueriedViewpointDTO> getQueriedViewpointMap() {
		return queriedViewpointMap;
	}

	public void setQueriedViewpointMap(Map<String, QueriedViewpointDTO> queriedViewpointMap) {
		this.queriedViewpointMap = queriedViewpointMap;
	}

	public MeterGaugeChartModel getMeterPlotManual() {
		return getMeterPlot(ViewpointSimulationEnum.MANUAL);
	}

	public MeterGaugeChartModel getMeterPlotAutomatic() {
		return getMeterPlot(ViewpointSimulationEnum.AUTOMATIC);
	}

	public MeterGaugeChartModel getMeterPlot(ViewpointSimulationEnum type) {
		QueriedViewpointDTO selectedViewpointDTO = getSelectedViewpointDTO();
		if (selectedViewpointDTO == null) {
			return new MeterGaugeChartModel();
		}

		MeterGaugeChartModel meter;

		int totalElements = 0;
		int maxCoveredElements = 0;
		double maxPercentageElements = 0;
		if (ViewpointSimulationEnum.MANUAL.equals(type)) {
			totalElements = selectedViewpointDTO.getTotalElementsManual();
			maxCoveredElements = selectedViewpointDTO.getMaxNumElementsManual();
			maxPercentageElements = selectedViewpointDTO.getMaxPercentageElementsManual() / 100;
		} else if (ViewpointSimulationEnum.AUTOMATIC.equals(type)) {
			totalElements = selectedViewpointDTO.getTotalElementsAutomatic();
			maxCoveredElements = selectedViewpointDTO.getMaxNumElementsAutomatic();
			maxPercentageElements = selectedViewpointDTO.getMaxPercentageElementsAutomatic() / 100;
		}

		List<Number> intervals = new ArrayList<Number>();
		intervals.add(Math.max((int) (totalElements * 0.15), 1));
		intervals.add((int) (totalElements * 0.50));
		intervals.add((int) (totalElements * 0.85));
		intervals.add((int) totalElements * 1);
		meter = new MeterGaugeChartModel(maxCoveredElements, intervals);

		meter.setTitle("Max. " + maxCoveredElements + " covered elements from " + totalElements);
		NumberFormat percentageFormat = NumberFormat.getPercentInstance();
		meter.setGaugeLabel(percentageFormat.format(maxPercentageElements));
		meter.setGaugeLabelPosition("bottom");

		List<Number> ticks = new ArrayList<>();
		int increment = totalElements > 20 ? 5 : 2;
		for (int i = 0; i < totalElements; i = i + increment) {
			ticks.add(i);
		}
		ticks.add(totalElements);
		meter.setTicks(ticks);

		meter.setSeriesColors("cc6666,E7E658,93b75f,66cc66");

		return meter;
	}

	public BarChartModel getBestTechniquesPlot() {
		if (getSelectedViewpointDTO() == null) {
			return new HorizontalBarChartModel();
		}
		return generateBestBarPlot(techniquesFromSelectedViewpoint, "techniques",
				getSelectedViewpointDTO().getTotalElementsAutomatic());
	}

	public BarChartModel getBestStakeholderPlot() {
		if (getSelectedViewpointDTO() == null) {
			return new HorizontalBarChartModel();
		}
		return generateBestBarPlot(stakeholdersFromSelectedViewpoint, "stakeholders",
				getSelectedViewpointDTO().getTotalElementsManual());
	}

	private BarChartModel generateBestBarPlot(ArrayList<Pair<String, Integer>> listElements, String label,
			int totalElements) {
		BarChartModel horizontalBarModel = new HorizontalBarChartModel();

		ChartSeries techniques = new ChartSeries();
		techniques.setLabel(label);

		Collections.sort(listElements, new Comparator<Pair<String, Integer>>() {
			@Override
			public int compare(Pair<String, Integer> o1, Pair<String, Integer> o2) {
				return o1.getRight().compareTo(o2.getRight());
			}
		}.reversed());

		for (int i = Math.min(6, listElements.size() - 1); i >= 0; i--) {
			Pair<String, Integer> pair = listElements.get(i);
			techniques.set(pair.getLeft(), (double) (pair.getRight()) / totalElements * 100.00);
		}

		horizontalBarModel.addSeries(techniques);

		horizontalBarModel.setTitle("Most valuable " + label);
		horizontalBarModel.setStacked(true);

		Axis xAxis = horizontalBarModel.getAxis(AxisType.X);
		xAxis.setLabel("Percentage of covered elements");
		xAxis.setMin(0);
		xAxis.setMax(100);
		horizontalBarModel.setAnimate(true);
		horizontalBarModel.setMouseoverHighlight(true);
		horizontalBarModel.setShowDatatip(true);

		return horizontalBarModel;
	}

	public void saveSetup() {
		closeSetupDialog();
	}

	public void resetSetup() {
		loadDefaultSetup();
		RequestContext context = RequestContext.getCurrentInstance();
		context.update("mainForm:setupBestPlanDialog");
	}

	private void loadDefaultSetup() {
		priorityBestPlan = BestPlanService.PRIO_AUTOMATIC;
		maximizationBestPlan = BestPlanService.MAX_PERFORMANCE;
		
		if(!artifactPickerList.getTarget().isEmpty() && stakeholderPickerList.getTarget().isEmpty()) {
			priorityBestPlan = BestPlanService.PRIO_AUTOMATIC;
		}
		else if (artifactPickerList.getTarget().isEmpty() && !stakeholderPickerList.getTarget().isEmpty()) {
			priorityBestPlan = BestPlanService.PRIO_MANUAL;
		}
	}

	private void closeSetupDialog() {
		RequestContext context = RequestContext.getCurrentInstance();
		context.update("mainForm:setupBestPlanDialog");
		context.execute("PF('setupBestPlanDialog').hide()");
	}

	public ArrayList<Pair<String, Integer>> getTechniquesFromSelectedViewpoint() {
		return techniquesFromSelectedViewpoint;
	}

	public void setTechniquesFromSelectedViewpoint(ArrayList<Pair<String, Integer>> techniquesFromSelectedViewpoint) {
		this.techniquesFromSelectedViewpoint = techniquesFromSelectedViewpoint;
	}

	public String getSelectedTechnique() {
		return selectedTechnique;
	}

	public void setSelectedTechnique(String selectedTechnique) {
		this.selectedTechnique = selectedTechnique;
	}

	public ArrayList<String> getElementsFromSelectedTechnique() {
		return elementsFromSelectedTechnique;
	}

	public void setElementsFromSelectedTechnique(ArrayList<String> elementsFromSelectedTechnique) {
		this.elementsFromSelectedTechnique = elementsFromSelectedTechnique;
	}

	public ViewpointSimulationEnum getSimulationType() {
		return simulationType;
	}

	public void setSimulationType(ViewpointSimulationEnum simulationType) {
		this.simulationType = simulationType;
	}

	public boolean isManualSimulation() {
		return ViewpointSimulationEnum.MANUAL.equals(simulationType);
	}

	public boolean isAutomaticSimulation() {
		return ViewpointSimulationEnum.AUTOMATIC.equals(simulationType);
	}

	public boolean isHybridSimulation() {
		return ViewpointSimulationEnum.HYBRID.equals(simulationType);
	}

	public ArrayList<Pair<String, Integer>> getStakeholdersFromSelectedViewpoint() {
		return stakeholdersFromSelectedViewpoint;
	}

	public void setStakeholdersFromSelectedViewpoint(
			ArrayList<Pair<String, Integer>> stakeholdersFromSelectedViewpoint) {
		this.stakeholdersFromSelectedViewpoint = stakeholdersFromSelectedViewpoint;
	}

	public List<Long> getQueriedElementIds() {
		return queriedElementIds;
	}

	public void setQueriedElementIds(List<Long> queriedElementIds) {
		this.queriedElementIds = queriedElementIds;
	}

	public BestPlan getBestPlan() {
		return bestPlan;
	}

	public void setBestPlan(BestPlan bestPlan) {
		this.bestPlan = bestPlan;
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

	public List<Viewpoint> getAvailableViewpoints() {
		return availableViewpoints;
	}

	public void setAvailableViewpoints(List<Viewpoint> availableViewpoints) {
		this.availableViewpoints = availableViewpoints;
	}

	public boolean[] getActiveViewpoint() {
		return activeViewpoint;
	}

	public void setActiveViewpoint(boolean[] activeViewpoint) {
		this.activeViewpoint = activeViewpoint;
	}

	public List<ViewpointElement> getSelectedBestPlanElements() {
		return selectedBestPlanElements;
	}

	public void setSelectedBestPlanElements(List<ViewpointElement> selectedBestPlanElements) {
		this.selectedBestPlanElements = selectedBestPlanElements;
	}

	public LineChartModel getBestPlanEvolutionPlot() {
		return bestPlanEvolutionPlot;
	}

	public void setBestPlanEvolutionPlot(LineChartModel bestPlanEvolutionPlot) {
		this.bestPlanEvolutionPlot = bestPlanEvolutionPlot;
	}

	public BestPlanService getBestPlanService() {
		return bestPlanService;
	}

	public void setBestPlanService(BestPlanService bestPlanService) {
		this.bestPlanService = bestPlanService;
	}

}