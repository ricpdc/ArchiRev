package es.alarcos.archirev.logic;

import java.io.IOException;
import java.io.Serializable;
import java.util.zip.ZipException;

import org.springframework.util.MultiValueMap;

import com.archimatetool.model.impl.ArchimateElement;
import com.archimatetool.model.impl.ArchimateRelationship;

import es.alarcos.archirev.model.Source;

public class CSharpSourceCodeParser extends AbstractSourceCodeParser implements Serializable {

	private static final long serialVersionUID = 4197712088221134990L;

	public CSharpSourceCodeParser(final String setup) {
		super(setup);
	}

	@Override
	public MultiValueMap<String, ArchimateElement> computeModelElementsByClassName(Source warSource)
			throws ZipException, IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public MultiValueMap<String, ArchimateRelationship> computeModelRelationshipsByClassName(Source warSource,
			MultiValueMap<String, ArchimateElement> modelElementsByClassName) throws ZipException, IOException {
		// TODO Auto-generated method stub
		return null;
	}

}
