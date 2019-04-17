package es.alarcos.archirev.controller;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

import org.primefaces.context.RequestContext;
import org.primefaces.event.ToggleEvent;
import org.primefaces.event.TransferEvent;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.DualListModel;
import org.primefaces.model.StreamedContent;
import org.primefaces.model.Visibility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import es.alarcos.archirev.model.AbstractEntity;
import es.alarcos.archirev.model.InputArtifact;
import es.alarcos.archirev.model.Source;
import es.alarcos.archirev.model.Stakeholder;
import es.alarcos.archirev.model.Viewpoint;
import es.alarcos.archirev.model.ViewpointElement;
import es.alarcos.archirev.persistency.ConcernDao;
import es.alarcos.archirev.persistency.InputArtifactDao;
import es.alarcos.archirev.persistency.PurposeDao;
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
	
	
	private DualListModel<InputArtifact> artifactPickerList = new DualListModel<>(new ArrayList<InputArtifact>(), new ArrayList<InputArtifact>());
	private DualListModel<Stakeholder> stakeholderPickerList = new DualListModel<>(new ArrayList<Stakeholder>(), new ArrayList<Stakeholder>());

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
		if(bytes == null) {
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
		
		for (Viewpoint viewpoint : availableViewpoints) {
			if(viewpoint.getName().equals(viewpointName)) {
				selectedViewpoint = viewpoint;
				break;
			}
		}
		
		if(selectedViewpoint!=null) {
			RequestContext context = RequestContext.getCurrentInstance();
			context.update("mainForm:viewpointDialog");
			context.execute("PF('viewpointDialog').show()");
		}
		
	}
	
}