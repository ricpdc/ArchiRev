package es.alarcos.archirev.logic;

import java.io.Serializable;

import javax.inject.Singleton;

import org.springframework.stereotype.Service;

import com.archimatetool.model.impl.AccessRelationship;
import com.archimatetool.model.impl.AggregationRelationship;
import com.archimatetool.model.impl.ApplicationComponent;
import com.archimatetool.model.impl.ApplicationFunction;
import com.archimatetool.model.impl.ApplicationProcess;
import com.archimatetool.model.impl.ApplicationService;
import com.archimatetool.model.impl.ArchimateConcept;
import com.archimatetool.model.impl.AssociationRelationship;
import com.archimatetool.model.impl.CompositionRelationship;
import com.archimatetool.model.impl.DataObject;
import com.archimatetool.model.impl.RealizationRelationship;
import com.archimatetool.model.impl.ServingRelationship;
import com.archimatetool.model.impl.SpecializationRelationship;
import com.archimatetool.model.impl.TriggeringRelationship;

@Singleton
@Service
public class IconService implements Serializable {

	private static final long serialVersionUID = 8596009485095134469L;

	public static String getIcon(ArchimateConcept archimateConcept) {
		return getIcon(archimateConcept.getClass().getSimpleName());
	}
	
	public static String getIcon(String archimateConcept) {
		String icon = "images/fav"; // default
		String root = "images/archimate/";
		if (archimateConcept.equals(ApplicationComponent.class.getSimpleName())) {
			icon = root + "application-component";
		} else if (archimateConcept.equals(ApplicationService.class.getSimpleName())) {
			icon = root + "application-service";
		} else if (archimateConcept.equals(ApplicationFunction.class.getSimpleName())) {
			icon = root + "application-function";
		} else if (archimateConcept.equals(ApplicationProcess.class.getSimpleName())) {
			icon = root + "application-process";
		} else if (archimateConcept.equals(DataObject.class.getSimpleName())) {
			icon = root + "data-object";
		} else if (archimateConcept.equals(ServingRelationship.class.getSimpleName())) {
			icon = root + "serving";
		} else if (archimateConcept.equals(AssociationRelationship.class.getSimpleName())) {
			icon = root + "association";
		} else if (archimateConcept.equals(AccessRelationship.class.getSimpleName())) {
			icon = root + "assignment";
		} else if (archimateConcept.equals(AggregationRelationship.class.getSimpleName())) {
			icon = root + "aggregation";
		} else if (archimateConcept.equals(CompositionRelationship.class.getSimpleName())) {
			icon = root + "composition";
		} else if (archimateConcept.equals(RealizationRelationship.class.getSimpleName())) {
			icon = root + "realization";
		} else if (archimateConcept.equals(SpecializationRelationship.class.getSimpleName())) {
			icon = root + "specialization";
		} else if (archimateConcept.equals(TriggeringRelationship.class.getSimpleName())) {
			icon = root + "triggering";
		}
		icon += ".png";

		return icon;
	}
}