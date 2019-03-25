package es.alarcos.archirev.controller.converter;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;

import org.primefaces.component.picklist.PickList;
import org.primefaces.model.DualListModel;

import es.alarcos.archirev.model.AbstractEntity;

@FacesConverter(value = "entityConverter")
public class PickListEntityConverter<E extends AbstractEntity> implements Converter {

	@SuppressWarnings("unchecked")
	@Override
	public Object getAsObject(FacesContext fc, UIComponent comp, String value) {
		DualListModel<E> model = (DualListModel<E>) ((PickList) comp).getValue();
		for (E entity : model.getSource()) {
			if (String.valueOf(entity.getId()).equals(value)) {
				return entity;
			}
		}
		for (E entity : model.getTarget()) {
			if (String.valueOf(entity.getId()).equals(value)) {
				return entity;
			}
		}
		return null;
	}

	@Override
	public String getAsString(FacesContext fc, UIComponent comp, Object value) {
		if (value == null) {
			return "";
		}
		return String.valueOf(((E) value).getId());
	}
}
