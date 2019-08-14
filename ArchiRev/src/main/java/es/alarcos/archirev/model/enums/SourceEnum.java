package es.alarcos.archirev.model.enums;

import static java.util.stream.Collectors.joining;

import java.util.Arrays;
import java.util.List;

import org.springframework.util.CollectionUtils;

public enum SourceEnum {

	JAVA_WEB_APP(0, SourceConcernEnum.APPLICATION, "Java Web application", Arrays.asList("ear", "war", "jar")),
	CSHARP_APP(1, SourceConcernEnum.APPLICATION, "C# application", Arrays.asList("zip", "rar", "7z")),
	JPA(2, SourceConcernEnum.APPLICATION, "JPA config");

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
		this.extensions = extensions;
	}

	public int getId() {
		return id;
	}

	public String getLabel() {
		return label;
	}

	public SourceConcernEnum getSourceConcern() {
		return sourceConcern;
	}

	public List<String> getExtensions() {
		return extensions;
	}

	public String getFormattedExtensions() {
		return CollectionUtils.isEmpty(extensions) ? "" : extensions.stream().collect(joining(", *.", "(*.", ")"));
	}

}
