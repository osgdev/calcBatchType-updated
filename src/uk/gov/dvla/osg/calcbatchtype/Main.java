package uk.gov.dvla.osg.calcbatchtype;

import java.util.ArrayList;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import uk.gov.dvla.osg.common.classes.Selector;
import uk.gov.dvla.osg.common.config.PresentationConfiguration;
import uk.gov.dvla.osg.common.config.ProductionConfiguration;
import uk.gov.dvla.osg.common.config.SelectorLookup;

public class Main {

	private static final Logger LOGGER = LogManager.getLogger();

	private static final int EXPECTED_NUMBER_OF_ARGS = 3;

	private static String inputFile;
	private static String outputFile;
	private static String propsFile;

 
	public static void main(String[] args) {

		LOGGER.trace("calcBatchType started");

		try {
			// Process args
			LOGGER.trace("Set Args");
			setArgs(args);
			// Load files
			LOGGER.trace("Load Config");
			AppConfig appConfig = new AppConfig(propsFile);
	        LOGGER.trace("Load Customers");
			DpfParser dpf = new DpfParser(inputFile, outputFile, appConfig);
			ArrayList<DocumentProperties> docProps = dpf.Load();
			LOGGER.trace("Load Lookup file");
			loadLookupFiles(appConfig, docProps);
			// set batch types
			LOGGER.trace("Run Batch Type Calculator");
			BatchTypesCalculator.run(docProps);
			// save to new file
			LOGGER.trace("Save DPF");
			dpf.Save(docProps);
			LOGGER.trace("Data saved to: {}", outputFile);
		} catch (Exception e) {
			LOGGER.fatal(e);
			System.exit(1);
		}
	}

	/**
	 * Validate number of expected arguements and set args
	 * @param args
	 */
	private static void setArgs(String[] args) {

		if (args.length != EXPECTED_NUMBER_OF_ARGS) {
			LOGGER.fatal("Incorrect number of args parsed {} expected {}", args.length, EXPECTED_NUMBER_OF_ARGS);
			System.exit(1);
		}
		inputFile = args[0];
		outputFile = args[1];
		propsFile = args[2];

	}

	/**
	 * Loads selector, Production Configuration and Presentation Configuration files.
	 * @param appConfig Application Configuration file
	 * @param docProps Document Properties file
	 */
	private static void loadLookupFiles(AppConfig appConfig, ArrayList<DocumentProperties> docProps) {
		// Lookup config files based on the selector, format is path+fileName+suffix
		SelectorLookup.init(appConfig.LookupFile());
		Selector selector = SelectorLookup.getInstance().getLookup().get(docProps.get(0).getSelectorRef());
		ProductionConfiguration.init(appConfig.ProductionConfigPath()
				+ selector.getProductionConfig() 
				+ appConfig.ProductionFileSuffix());
		PresentationConfiguration.init(appConfig.getPresentationPriorityConfigPath() 
				+ selector.getPresentationConfig()
				+ appConfig.getPresentationPriorityFileSuffix());
	}
	
	/*	private static AppConfig loadPropertiesFile() throws Exception{
	ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
	mapper.setVisibility(PropertyAccessor.FIELD, Visibility.ANY);
	return mapper.readValue(new File(propsFile), AppConfig.class);
	}*/
}
