package es.alarcos.archirev.controller;

import java.io.ByteArrayInputStream;
import java.io.IOException;
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
import org.primefaces.event.TransferEvent;
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

import com.archimatetool.model.impl.ArchimateElement;

import es.alarcos.archirev.model.AbstractEntity;
import es.alarcos.archirev.model.InputArtifact;
import es.alarcos.archirev.model.Source;
import es.alarcos.archirev.model.Stakeholder;
import es.alarcos.archirev.model.Viewpoint;
import es.alarcos.archirev.model.ViewpointElement;
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

	private String selectedTechnique;

	private ArrayList<String> elementsFromSelectedTechnique;

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

			artifactPickerList.setTarget(artifactPickerList.getSource());
			simulateViewpoints();
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

	public void onTransfer(TransferEvent event) {
		StringBuilder builder = new StringBuilder();
		for (Object item : event.getItems()) {
			builder.append(((Source) item).getName()).append(",");
		}
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

	public void showViewpointInfo(String viewpointName) {
		LOGGER.info("Showing " + viewpointName);

		if (getPercentage(viewpointName) == 0.0) {
			return;
		}

		for (Viewpoint viewpoint : availableViewpoints) {
			if (viewpoint.getName().equals(viewpointName)) {
				selectedViewpoint = viewpoint;
				QueriedViewpointDTO updatedViewpointDTO = viewpointDao
						.getViewpointPercentagesByArtefacts(artifactPickerList.getTarget(), getSelectedViewpointDTO());
				queriedViewpointMap.put(viewpointName, updatedViewpointDTO);
				break;
			}
		}
		
		loadTechniquesFromSelectedViewpointDTO();

		if (selectedViewpoint != null) {
			RequestContext context = RequestContext.getCurrentInstance();
			context.update("mainForm:viewpointDialog");
			context.execute("PF('viewpointDialog').show()");
		}

	}
	
	public void showElementsForTechnique (String techniqueName) {
		selectedTechnique = techniqueName;
		setElementsFromSelectedTechnique(new ArrayList<String>(getSelectedViewpointDTO().getElementsByTechnique().get(selectedTechnique)));
		
		if (selectedViewpoint != null) {
			RequestContext context = RequestContext.getCurrentInstance();
			context.update("mainForm:elementsTechniqueDialog");
			context.execute("PF('elementsTechniqueDialog').show()");
		}

	}
	
	public boolean isElementCovered (ViewpointElement element) {
		return getElementsFromSelectedTechnique() != null && getElementsFromSelectedTechnique().contains(element.getName());
	}
	
	private List<Pair<String, Integer>> loadTechniquesFromSelectedViewpointDTO () {
		setTechniquesFromSelectedViewpoint(new ArrayList<Pair<String, Integer>>());
		if(getSelectedViewpointDTO()==null) {
			return getTechniquesFromSelectedViewpoint();
		}
		Set<String> keySet = getSelectedViewpointDTO().getElementsByTechnique().keySet();
		if(keySet == null) {
			return getTechniquesFromSelectedViewpoint();
		}
		for (String key : keySet) {
			getTechniquesFromSelectedViewpoint().add(new ImmutablePair(key, getSelectedViewpointDTO().getElementsByTechnique().get(key).size()));
		}
		return getTechniquesFromSelectedViewpoint();
	}

	public double getPercentage(String viewpointName) {
		QueriedViewpointDTO queriedViewpointDTO = queriedViewpointMap.get(viewpointName);
		if (queriedViewpointDTO != null) {
			return queriedViewpointDTO.getMaxPercentageElements();
		}
		return 0.0;
	}

	public void simulateViewpoints() {
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

	public MeterGaugeChartModel getMeterPlot() {
		if (getSelectedViewpointDTO() == null) {
			return new MeterGaugeChartModel();
		}

		MeterGaugeChartModel meter;

		int totalElements = getSelectedViewpointDTO().getTotalElements();
		int maxCoveredElements = getSelectedViewpointDTO().getMaxNumElements();
		double maxPercentageElements = getSelectedViewpointDTO().getMaxPercentageElements() / 100;

		List<Number> intervals = new ArrayList<Number>() {
			{
				add(Math.max((int) (totalElements * 0.15), 1));
				add((int) (totalElements * 0.50));
				add((int) (totalElements * 0.85));
				add((int) totalElements * 1);
			}
		};

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
    	BarChartModel horizontalBarModel = new HorizontalBarChartModel();
    	
    	if (getSelectedViewpointDTO() == null) {
			return horizontalBarModel;
		}
 
        ChartSeries techniques = new ChartSeries();
        techniques.setLabel("Techniques");
        
        
        Collections.sort(techniquesFromSelectedViewpoint, new Comparator<Pair<String, Integer>>() {
			@Override
			public int compare(Pair<String, Integer> o1, Pair<String, Integer> o2) {
				return o1.getRight().compareTo(o2.getRight());
			}
		}.reversed());
        
        for (int i =  Math.min(6, techniquesFromSelectedViewpoint.size()); i > 0; i--) {
			Pair<String, Integer> pair = techniquesFromSelectedViewpoint.get(i);
			techniques.set(pair.getLeft(), (double)(pair.getRight()) / getSelectedViewpointDTO().getTotalElements() * 100.00);
		}    
 
        horizontalBarModel.addSeries(techniques);
 
        horizontalBarModel.setTitle("Most valuable techniques");
        horizontalBarModel.setStacked(true);
 
        Axis xAxis = horizontalBarModel.getAxis(AxisType.X);
        xAxis.setLabel("Percentage of covered elements");
        xAxis.setMin(0);
        xAxis.setMax(100);
        horizontalBarModel.setAnimate(true);
        horizontalBarModel.setMouseoverHighlight(true);
        horizontalBarModel.setShowDatatip(true);
 
        Axis yAxis = horizontalBarModel.getAxis(AxisType.Y);
        
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



}