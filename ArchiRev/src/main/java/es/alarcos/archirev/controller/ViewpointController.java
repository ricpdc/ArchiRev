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
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;
import org.primefaces.model.Visibility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import es.alarcos.archirev.model.AbstractEntity;
import es.alarcos.archirev.model.Viewpoint;
import es.alarcos.archirev.persistency.ScopeDao;
import es.alarcos.archirev.persistency.StakeholderDao;
import es.alarcos.archirev.persistency.ViewpointDao;

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
	private StakeholderDao stakeholderDao;

	private List<Viewpoint> availableViewpoints;
	private List<String> scopeItems;
	private List<String> stakeholdersItems;

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

	public StreamedContent getExampleImage(byte[] example) throws IOException {
		return new DefaultStreamedContent(new ByteArrayInputStream(example), "image/png");
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
		List<String> filterList = (filter == null) ? null : new ArrayList<String>(Arrays.asList((String[])filter));
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

}