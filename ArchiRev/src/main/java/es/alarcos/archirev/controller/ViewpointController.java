package es.alarcos.archirev.controller;

import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

import org.primefaces.context.RequestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import es.alarcos.archirev.model.Viewpoint;
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

	private List<Viewpoint> availableViewpoints;

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
			RequestContext.getCurrentInstance().update("mainForm:viewpointsTabs");
		}
	}

	public List<Viewpoint> getAvailableViewpoints() {
		return availableViewpoints;
	}

	public void setAvailableViewpoints(List<Viewpoint> availableViewpoints) {
		this.availableViewpoints = availableViewpoints;
	}

	
}