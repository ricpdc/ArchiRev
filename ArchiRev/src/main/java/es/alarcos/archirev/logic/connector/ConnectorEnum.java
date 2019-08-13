package es.alarcos.archirev.logic.connector;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.archimatetool.model.impl.AccessRelationship;
import com.archimatetool.model.impl.AssociationRelationship;
import com.archimatetool.model.impl.AggregationRelationship;
import com.archimatetool.model.impl.ArchimateRelationship;
import com.archimatetool.model.impl.CompositionRelationship;
import com.archimatetool.model.impl.RealizationRelationship;
import com.archimatetool.model.impl.ServingRelationship;
import com.archimatetool.model.impl.SpecializationRelationship;
import com.archimatetool.model.impl.TriggeringRelationship;
import com.mxgraph.shape.mxBasicShape;
import com.mxgraph.shape.mxConnectorShape;
import com.mxgraph.shape.mxRectangleShape;

public enum ConnectorEnum {

	// @formatter:off
	DEFAULT(ArchimateRelationship.class, AbstractArchiMateConnector.class, "none", "none", true, true, 10, 10, false),
	SPECIALIZATION(SpecializationRelationship.class, ArchiMateSpecializationConnector.class, "block", "dash", false, true, 10, 0, false),
	TRIGGERING(TriggeringRelationship.class, ArchiMateTriggeringConnector.class, "dash", "block", true, true, 10, 8, false),
	COMPOSITION(CompositionRelationship.class, ArchiMateCompositionConnector.class, "diamond", "dash", true, true, 12, 0, false),
	AGGREGATION(AggregationRelationship.class, ArchiMateAggregationConnector.class, "diamond", "dash", false, true, 12, 0, false),
	REALIZATION(RealizationRelationship.class, ArchiMateRealizationConnector.class, "none", "block", true, false, 0, 10, true),
	ACCESS(AccessRelationship.class, ArchiMateAccessConnector.class, "none", "open", true, true, 0, 8, true),
	ASSOCIATION(AssociationRelationship.class, ArchiMateAssociationConnector.class, "none", "none", true, true, 0, 0, false),
	SERVING(ServingRelationship.class, ArchiMateServingConnector.class, "none", "open", true, true, 0, 6, false);
	
	// @formatter:on

	static Logger logger = LoggerFactory.getLogger(ConnectorEnum.class);

	private Class<? extends ArchimateRelationship> modelRelationship;
	private Class<? extends AbstractArchiMateConnector> connectorShape;
	private String strokeColor;
	private String startArrow;
	private String endArrow;
	private boolean startFill;
	private boolean endFill;
	private int startSize;
	private int endSize;
	private boolean dashed;

	private ConnectorEnum(Class<? extends ArchimateRelationship> modelRelationship,
			Class<? extends AbstractArchiMateConnector> connectorShape, String startArrow, String endArrow,
			boolean startFill, boolean endFill, int startSize, int endSize, boolean dashed) {
		this.modelRelationship = modelRelationship;
		this.connectorShape = connectorShape;
		this.strokeColor = "000000";
		this.startArrow = startArrow;
		this.endArrow = endArrow;
		this.startFill = startFill;
		this.endFill = endFill;
		this.startSize = startSize;
		this.endSize = endSize;
		this.dashed = dashed;
	}
	
	public static ConnectorEnum getEnumByName(String name) {
		for (ConnectorEnum connectorEnum : values()) {
			if(connectorEnum.getModelRelationship().getSimpleName().equals(name)) {
				return connectorEnum;
			}
		}
		return DEFAULT;
	}

	public static ConnectorEnum getByModelRelationship(final Class<? extends ArchimateRelationship> modelRelationship) {
		for (ConnectorEnum shapeEnum : values()) {
			if (shapeEnum.getModelRelationship().equals(modelRelationship)) {
				return shapeEnum;
			}
		}
		return DEFAULT;
	}

	public mxBasicShape getConnectorShapeInstance() {
		Constructor<? extends mxConnectorShape> constructor;
		try {
			constructor = connectorShape.getConstructor();
			return constructor.newInstance();
		} catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException
				| IllegalArgumentException | InvocationTargetException e) {
			logger.error("The instance for " + connectorShape.getSimpleName() + " cannot be created");
			return new mxRectangleShape();
		}
	}

	public Class<? extends ArchimateRelationship> getModelRelationship() {
		return modelRelationship;
	}

	public void setModelRelationship(Class<? extends ArchimateRelationship> modelRelationship) {
		this.modelRelationship = modelRelationship;
	}

	public String getStrokeColor() {
		return strokeColor;
	}

	public void setStrokeColor(String strokeColor) {
		this.strokeColor = strokeColor;
	}

	public String getStartArrow() {
		return startArrow;
	}

	public void setStartArrow(String startArrow) {
		this.startArrow = startArrow;
	}

	public String getEndArrow() {
		return endArrow;
	}

	public void setEndArrow(String endArrow) {
		this.endArrow = endArrow;
	}

	public String getStartFill() {
		return isStartFill() ? "1" : "0";
	}

	public void setStartFill(boolean startFill) {
		this.startFill = startFill;
	}

	public boolean isStartFill() {
		return this.startFill;
	}

	public String getEndFill() {
		return isEndFill() ? "1" : "0";
	}

	public void setEndFill(boolean endFill) {
		this.endFill = endFill;
	}

	public boolean isEndFill() {
		return this.endFill;
	}

	public int getStartSize() {
		return startSize;
	}

	public void setStartSize(int startSize) {
		this.startSize = startSize;
	}

	public int getEndSize() {
		return endSize;
	}

	public void setEndSize(int endSize) {
		this.endSize = endSize;
	}

	public boolean isDashed() {
		return dashed;
	}

	public void setDashed(boolean dashed) {
		this.dashed = dashed;
	}

	public String getDashed() {
		return isDashed() ? "1" : "0";
	}

	public Class<? extends mxConnectorShape> getConnectorShape() {
		return connectorShape;
	}

	public void setConnectorShape(Class<? extends AbstractArchiMateConnector> connectorShape) {
		this.connectorShape = connectorShape;
	}

}
