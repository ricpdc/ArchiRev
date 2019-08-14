package es.alarcos.archirev.controller;

import java.io.Serializable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractController implements Serializable {

	private static final long serialVersionUID = -6866990404513164660L;

	private static Logger logger = LoggerFactory.getLogger(AbstractController.class);

	void init() {
		logger.info(String.format("Initializing... %s", getClass().getSimpleName()));
	}
}
