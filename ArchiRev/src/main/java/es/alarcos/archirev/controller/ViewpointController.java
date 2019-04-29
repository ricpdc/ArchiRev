package es.alarcos.archirev.controller;

import java.io.ByteArrayInputStream;
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
import org.primefaces.model.chart.MeterGaugeChartModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

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

@ManagedBean(name = "viewpointController")
@Controller
@ViewScoped
public class ViewpointController extends AbstractController {

	private static final long serialVersionUID = -7943630748807472984L;

	static Logger LOGGER = LoggerFactory.getLogger(ViewpointController.class);

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

	private Viewpoint selectedViewpoint;

	private List<Viewpoint> availableViewpoints;
	private List<String> scopeItems;
	private List<String> stakeholdersItems;
	private List<String> concernItems;
	private List<String> purposeItems;
	private List<String> elementItems;
	private List<ViewpointElement> allElements;

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
			setAvailableViewpoints(viewpointDao.findAll());
			scopeItems = scopeDao.getScopeNames();
			stakeholdersItems = stakeholderDao.getStakeholdersNames();
			concernItems = concernDao.getConcernNames();
			purposeItems = purposeDao.getPurposeNames();
			elementItems = viewpointElementDao.getElementNames();
			allElements = viewpointElementDao.findAll();

			artifactPickerList = new DualListModel<>(inputArtifactDao.findAll(), new ArrayList<InputArtifact>());
			stakeholderPickerList = new DualListModel<>(stakeholderDao.findAll(), new ArrayList<Stakeholder>());

			RequestContext.getCurrentInstance().update("mainForm:viewpointsTabs");

			coloured = false;
			queriedViewpointMap = new HashMap<String, QueriedViewpointDTO>();
			queriedElementIds = null;
		}
	}

	public List<Viewpoint> getAvailableViewpoints() {
		return availableViewpoints;
	}

	public void setAvailableViewpoints(List<Viewpoint> availableViewpoints) {
		this.availableViewpoints = availableViewpoints;
	}

	public void onRowToggle(ToggleEvent event) {
		if (event.getVisibility().equals(Visibility.VISIBLE)) {
			LOGGER.debug("Expanded: " + ((Viewpoint) event.getData()).getName());
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
		LOGGER.info("Showing " + viewpointName);
		if (getPercentageAutomatic(viewpointName) == 0.0) {
			return;
		}
		for (Viewpoint viewpoint : availableViewpoints) {
			if (viewpoint.getName().equals(viewpointName)) {
				selectedViewpoint = viewpoint;
				break;
			}
		}

		queriedViewpointMap.put(viewpointName, viewpointDao
				.getViewpointPercentagesByArtefacts(artifactPickerList.getTarget(), getSelectedViewpointDTO()));
		loadTechniquesFromSelectedViewpointDTO();

		if (selectedViewpoint != null) {
			RequestContext context = RequestContext.getCurrentInstance();
			context.update("mainForm:viewpointDialogAutomatic");
			context.update("mainForm:stakeholderList_elementsTechniqueTable");
			context.execute("PF('viewpointDialogAutomatic').show()");
		}

	}

	public void showViewpointInfoManual(String viewpointName) {
		LOGGER.info("Showing " + viewpointName);
		if (getPercentageManual(viewpointName) == 0.0) {
			return;
		}
		for (Viewpoint viewpoint : availableViewpoints) {
			if (viewpoint.getName().equals(viewpointName)) {
				selectedViewpoint = viewpoint;
				break;
			}
		}

		queriedViewpointMap.put(viewpointName,
				viewpointDao.getViewpointPercentagesByStakeholders(stakeholderPickerList.getTarget(),
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
		return new BigDecimal(getPercentageAutomatic(viewpointName)).setScale(1, BigDecimal.ROUND_FLOOR).doubleValue();
	}

	public double getFormattedPercentageManual(String viewpointName) {
		return new BigDecimal(getPercentageManual(viewpointName)).setScale(1, BigDecimal.ROUND_FLOOR).doubleValue();
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
			return getPercentageAutomatic(viewpointName) +  getFormattedPercentageManual(viewpointName);
		default:
			return 0.0;
		}
	}

	public void simulateViewpointsByArtifact() {
		simulationType = ViewpointSimulationEnum.AUTOMATIC;
		List<QueriedViewpointDTO> listViewpointsByArtefacts = viewpointDao
				.listViewpointsMaxPercentageByArtefacts(artifactPickerList.getTarget());
		queriedViewpointMap = new HashMap<String, QueriedViewpointDTO>();
		for (Viewpoint viewpoint : availableViewpoints) {
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
		simulationType = ViewpointSimulationEnum.MANUAL;
		queriedElementIds = null;
		List<QueriedViewpointDTO> listViewpointsByStakeholders = viewpointDao
				.listViewpointsMaxPercentageByStakeholder(stakeholderPickerList.getTarget(), null, null);
		queriedViewpointMap = new HashMap<String, QueriedViewpointDTO>();
		for (Viewpoint viewpoint : availableViewpoints) {
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
		simulationType = ViewpointSimulationEnum.HYBRID;
		List<QueriedViewpointDTO> listViewpointsByArtefacts = viewpointDao
				.listViewpointsMaxPercentageByArtefacts(artifactPickerList.getTarget());
		queriedViewpointMap = new HashMap<String, QueriedViewpointDTO>();
		for (Viewpoint viewpoint : availableViewpoints) {
			for (QueriedViewpointDTO viewpointDTO : listViewpointsByArtefacts) {
				if (viewpointDTO.getId().equals(viewpoint.getId())) {
					queriedViewpointMap.put(viewpoint.getName(), viewpointDTO);
					break;
				}
			}
		}

		queriedElementIds = viewpointDao.getCoveredElementsByArtifacts(artifactPickerList.getTarget());

		List<QueriedViewpointDTO> listViewpointsByStakeholders = viewpointDao.listViewpointsMaxPercentageByStakeholder(
				stakeholderPickerList.getTarget(), queriedElementIds, queriedViewpointMap);

		for (Viewpoint viewpoint : availableViewpoints) {
			for (QueriedViewpointDTO viewpointDTO : listViewpointsByStakeholders) {
				if (viewpointDTO.getId().equals(viewpoint.getId())) {
					queriedViewpointMap.put(viewpoint.getName(), viewpointDTO);
					break;
				}
			}
		}
		coloured = true;
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

}