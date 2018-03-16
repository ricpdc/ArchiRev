package es.alarcos.archirev.shape;

public class ArchiMateApplicationFunctionShape extends AbstractArchiMateRectangleShape {

	private static final String IMAGEN_RESOURCE = ".\\src\\test\\resources\\icon\\application_function.png";
	
	@Override
	public String getCornerImagePath() {
		return IMAGEN_RESOURCE;
	}

}
