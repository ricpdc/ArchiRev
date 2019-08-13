package es.alarcos.archirev.controller.converter;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;

import org.apache.commons.lang3.StringUtils;

@FacesConverter(value = "timestampConverter")
public class TimestampConverter implements Converter {

	private final SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy [HH:mm:ss]");

	@Override
	public Object getAsObject(FacesContext context, UIComponent component, String value) {
		if (StringUtils.isNotBlank(value)) {
			Date parsedDate;
			try {
				parsedDate = sdf.parse(value);
			} catch (ParseException e) {
				parsedDate = null;
			}
			if (parsedDate != null) {
				return new Timestamp(parsedDate.getTime());
			}
		}
		return null;
	}

	@Override
	public String getAsString(FacesContext context, UIComponent component, Object value) {
		if (value != null && value instanceof Timestamp) {
			return StringUtils.defaultString(sdf.format(new Date(((Timestamp) value).getTime())));
		}
		return "";
	}

}
