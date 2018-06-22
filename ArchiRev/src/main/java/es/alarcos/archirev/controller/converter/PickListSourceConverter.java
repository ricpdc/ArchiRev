package es.alarcos.archirev.controller.converter;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;

import org.primefaces.component.picklist.PickList;
import org.primefaces.model.DualListModel;

import es.alarcos.archirev.model.Source;

@FacesConverter(value = "sourceConverter")
public class PickListSourceConverter implements Converter {

	@SuppressWarnings("unchecked")
	@Override
	public Object getAsObject(FacesContext fc, UIComponent comp, String value) {
		DualListModel<Source> model = (DualListModel<Source>) ((PickList) comp).getValue();
		for (Source source : model.getSource()) {
			if (String.valueOf(source.getId()).equals(value)) {
				return source;
			}
		}
		for (Source source : model.getTarget()) {
			if (String.valueOf(source.getId()).equals(value)) {
				return source;
			}
		}
		return null;
	}

	@Override
	public String getAsString(FacesContext fc, UIComponent comp, Object value) {
		if (value == null) {
			return "";
		}
		return String.valueOf(((Source) value).getId());
	}
}
