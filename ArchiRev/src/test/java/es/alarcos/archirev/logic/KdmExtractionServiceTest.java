package es.alarcos.archirev.logic;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.TreeSet;

import org.junit.Before;
import org.junit.Test;

import es.alarcos.archirev.model.Extraction;
import es.alarcos.archirev.model.KdmModel;
import es.alarcos.archirev.model.Source;
import es.alarcos.archirev.model.enums.SourceConcernEnum;
import es.alarcos.archirev.model.enums.SourceEnum;

public class KdmExtractionServiceTest {

	private static final String BASE_PATH = "D:\\OneDrive - Universidad de Castilla-La Mancha\\Universidad\\ARTICULOS\\JCR-SoSym-2021\\case_study";
	private static final String DEFAULT_SETUP_PATH = "C:\\Users\\Alarcos\\git\\ArchiRev\\ArchiRev\\src\\main\\webapp\\json\\default_setup.json";

	private KdmExtractionService kdmExtractionService = new KdmExtractionService();;

	private List<String> projects = Arrays.asList("1_Asp.Net-Core-Inventory-Order-Management-System-master",
			"2_HotelManagement-master", "3_Employee-Management-System-master",
			"4_Clinic-Management-System-ASP.NET-master", "5_Flatmate-Management-System-master",
			"6_OrderManagementSystem-master", "7_HotelManagement-master",
			"8_Office-Management-System-master", "9_School-Management-System-master",
			"10_Dental-Clinic-Management-System-master", "11_CafeteriaManagementSystem-master");

	
	@SuppressWarnings("unchecked")
	private KdmModel setInputProject(String projectName) {
		KdmModel kdmModel = new KdmModel();

		Source source = new Source();
		source.setConcern(SourceConcernEnum.APPLICATION);
		source.setType(SourceEnum.CSHARP_APP);
		source.setFilePath(BASE_PATH + "\\source\\" + projectName + ".zip");
		source.setName(projectName);

		Extraction extraction = new Extraction();
		extraction.setSources(new TreeSet(Arrays.asList(source)));
		loadDefaultSetup(extraction);
		extraction.setKdmModel(kdmModel);

		kdmModel.setExtraction(extraction);
		kdmModel.setName(projectName);
		kdmModel.setExportedPath(BASE_PATH + "\\kdm\\" + projectName + ".kdm");

		return kdmModel;
	}

	@Test
	public void test() {
		for (String projectName : projects) {
			KdmModel kdmModel = setInputProject(projectName);
			kdmExtractionService.extractKdmModel(kdmModel);
			assertTrue(new File(kdmModel.getExportedPath()).exists());
		}

	}

	private void loadDefaultSetup(Extraction extraction) {
		try {
			byte[] encoded = Files.readAllBytes(Paths.get(DEFAULT_SETUP_PATH));
			extraction.setSetup(new String(encoded, StandardCharsets.UTF_8));

		} catch (IOException e) {
			System.out.println("Error loading default setup for model extraction");
		}
	}

}
