package es.alarcos.archirev.logic.shape;

public class ArchiMateApplicationServiceShape extends AbstractArchiMateRectangleShape {

	private static final String IMAGEN_RESOURCE = ".\\src\\test\\resources\\icon\\application_service.png";
	
	@Override
	public String getCornerImagePath() {
		return IMAGEN_RESOURCE;
	}

}
