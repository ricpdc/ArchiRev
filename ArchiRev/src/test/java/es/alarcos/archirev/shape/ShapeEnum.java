package es.alarcos.archirev.shape;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.archimatetool.model.impl.ApplicationFunction;
import com.archimatetool.model.impl.ApplicationService;
import com.archimatetool.model.impl.ArchimateElement;
import com.mxgraph.shape.mxBasicShape;
import com.mxgraph.shape.mxRectangleShape;

public enum ShapeEnum {

	// @formatter:off
	DEFAULT(ArchimateElement.class, mxRectangleShape.class, "cce3ff" , "000f84", "000f84"), 
	APPLICATION_FUNCTION(ApplicationFunction.class, ArchiMateApplicationFunctionShape.class, "cce3ff" , "000f84", "000f84"),
	APPLICATION_SERVICE(ApplicationService.class, ArchiMateApplicationServiceShape.class, "cce3ff" , "000f84", "000f84");
	// @formatter:on

	static Logger LOGGER = LoggerFactory.getLogger(ShapeEnum.class);

	private Class<? extends ArchimateElement> modelElement;
	private Class<? extends mxBasicShape> shape;
	private String fillColor;
	private String strokeColor;
	private String fontColor;

	private ShapeEnum(final Class<? extends ArchimateElement> modelElement, final Class<? extends mxBasicShape> shape,
			final String fillColor, final String strokeColor, final String fontColor) {
		this.modelElement = modelElement;
		this.shape = shape;
		this.fillColor = fillColor;
		this.strokeColor = strokeColor;
		this.fontColor = fontColor;
	}

	public static ShapeEnum getByModelElement(final Class<? extends ArchimateElement> modelElement) {
		for (ShapeEnum shapeEnum : values()) {
			if (shapeEnum.getModelElement().equals(modelElement)) {
				return shapeEnum;
			}
		}
		return DEFAULT;
	}

	public mxBasicShape getShapeInstance() {
		Constructor<? extends mxBasicShape> constructor;
		try {
			Class<?> c = Class.forName(shape.getCanonicalName());
			constructor = shape.getConstructor();
			return constructor.newInstance();
		} catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException
				| IllegalArgumentException | InvocationTargetException | ClassNotFoundException e) {
			LOGGER.error("The instance for " + shape.getSimpleName() + " cannot be created");
			return new mxRectangleShape();
		}
	}

	public Class<? extends ArchimateElement> getModelElement() {
		return modelElement;
	}

	public Class<? extends mxBasicShape> getShape() {
		return shape;
	}

	public String getFillColor() {
		return fillColor;
	}

	public void setFillColor(String fillColor) {
		this.fillColor = fillColor;
	}

	public String getStrokeColor() {
		return strokeColor;
	}

	public void setStrokeColor(String strokeColor) {
		this.strokeColor = strokeColor;
	}

	public String getFontColor() {
		return fontColor;
	}

	public void setFontColor(String fontColor) {
		this.fontColor = fontColor;
	}

}
