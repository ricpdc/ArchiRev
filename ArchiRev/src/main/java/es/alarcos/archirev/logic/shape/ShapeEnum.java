package es.alarcos.archirev.logic.shape;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.archimatetool.model.impl.ApplicationFunction;
import com.archimatetool.model.impl.ApplicationService;
import com.archimatetool.model.impl.ArchimateElement;
import com.archimatetool.model.impl.BusinessObject;
import com.archimatetool.model.impl.DataObject;
import com.archimatetool.model.impl.ApplicationComponent;
import com.mxgraph.shape.mxBasicShape;
import com.mxgraph.shape.mxRectangleShape;
import com.mxgraph.util.mxConstants;

public enum ShapeEnum {

	// @formatter:off
	DEFAULT(ArchimateElement.class, mxRectangleShape.class, "cce3ff" , "000f84", "000f84", true, mxConstants.ALIGN_MIDDLE), 
	APPLICATION_FUNCTION(ApplicationFunction.class, ArchiMateApplicationFunctionShape.class, "cce3ff" , "000f84", "000f84", true, mxConstants.ALIGN_MIDDLE),
	APPLICATION_SERVICE(ApplicationService.class, ArchiMateApplicationServiceShape.class, "cce3ff" , "000f84", "000f84", true, mxConstants.ALIGN_MIDDLE),
	DATA_OBJECT(DataObject.class, ArchiMateDataObjectShape.class, "adfff8" , "000f84", "000f84", false, mxConstants.ALIGN_MIDDLE),
	BUSINESS_OBJECT(BusinessObject.class, ArchiMateBusinessObjectShape.class, "ffff00" , "000f84", "000f84", false, mxConstants.ALIGN_MIDDLE),
	APPLICATION_COMPONENT(ApplicationComponent.class, ArchiMateApplicationComponentShape.class, "cce3ff" , "000f84", "000f84", false, mxConstants.ALIGN_TOP);
	
	
	// @formatter:on

	static Logger LOGGER = LoggerFactory.getLogger(ShapeEnum.class);

	private Class<? extends ArchimateElement> modelElement;
	private Class<? extends mxBasicShape> shape;
	private String fillColor;
	private String strokeColor;
	private String fontColor;
	private boolean rounded;
	private String verticalAlign;

	private ShapeEnum(final Class<? extends ArchimateElement> modelElement, final Class<? extends mxBasicShape> shape,
			final String fillColor, final String strokeColor, final String fontColor) {
		this(modelElement, shape, fillColor, strokeColor, fontColor, false, mxConstants.ALIGN_MIDDLE);
	}
	
	private ShapeEnum(final Class<? extends ArchimateElement> modelElement, final Class<? extends mxBasicShape> shape,
			final String fillColor, final String strokeColor, final String fontColor, final boolean rounded, final String verticalAlign) {
		this.modelElement = modelElement;
		this.shape = shape;
		this.fillColor = fillColor;
		this.strokeColor = strokeColor;
		this.fontColor = fontColor;
		this.rounded = rounded;
		this.verticalAlign = verticalAlign;
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
			constructor = shape.getConstructor();
			return constructor.newInstance();
		} catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException
				| IllegalArgumentException | InvocationTargetException e) {
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

	public boolean isRounded() {
		return rounded;
	}
	
	public String getRounded() {
		return isRounded() ? "1" : "0";
	}

	public void setRounded(boolean rounded) {
		this.rounded = rounded;
	}

	public String getVerticalAlign() {
		return verticalAlign;
	}

	public void setVerticalAlign(String verticalAlign) {
		this.verticalAlign = verticalAlign;
	}
	
	

}