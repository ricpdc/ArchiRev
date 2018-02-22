package es.alarcos.archirev.model.enums;

import static java.util.stream.Collectors.joining;

import java.util.Arrays;
import java.util.List;

import org.springframework.util.CollectionUtils;

public enum SourceEnum {

	WEB_APP(1, SourceConcernEnum.APPLICATION, "Web application", Arrays.asList("ear", "war", "jar")), JPA(1,
			SourceConcernEnum.APPLICATION, "JPA config");

	private int id;
	private SourceConcernEnum sourceConcern;
	private String label;
	private List<String> extensions;

	private SourceEnum(int id, SourceConcernEnum sourceConcern, String label) {
		this(id, sourceConcern, label, null);
	}

	private SourceEnum(int id, SourceConcernEnum sourceConcern, String label, List<String> extensions) {
		this.id = id;
		this.sourceConcern = sourceConcern;
		this.label = label;
		this.setExtensions(extensions);
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public SourceConcernEnum getSourceConcern() {
		return sourceConcern;
	}

	public void setSourceConcern(SourceConcernEnum sourceConcern) {
		this.sourceConcern = sourceConcern;
	}

	public List<String> getExtensions() {
		return extensions;
	}

	public String getFormattedExtensions() {
		return CollectionUtils.isEmpty(extensions) ? "" : extensions.stream().collect(joining(", *.", "(*.", ")"));
	}

	public void setExtensions(List<String> extensions) {
		this.extensions = extensions;
	}

}
