package uk.gov.dvla.osg.calcbatchtype;

import java.io.File;
import java.util.ArrayList;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import uk.gov.dvla.osg.common.config.PresentationConfiguration;
import uk.gov.dvla.osg.common.config.ProductionConfiguration;
import uk.gov.dvla.osg.common.config.SelectorLookup;

public class Main {

	private static final Logger LOGGER = LogManager.getLogger();

	private static final int EXPECTED_NUMBER_OF_ARGS = 3;

	private static String inputFile;
	private static String outputFile;
	private static String propsFile;

	public static long bytesToMegabytes(long bytes) {
		return bytes / (1024L * 1024L);
	}

	public static void main(String[] args) {

		LOGGER.debug("calcBatchType started");
		// Get the Java runtime
		Runtime runtime = Runtime.getRuntime();
		// Calculate the start time
		long start = System.nanoTime();

		try {
			// Process args
			LOGGER.trace("Set Args");
			setArgs(args);
			// Load files
			LOGGER.trace("Load Config");
			AppConfig appConfig = loadPropertiesFile();
	        LOGGER.trace("Load Customers");
			DpfParser dpf = new DpfParser(inputFile, outputFile, appConfig);
			ArrayList<DocumentProperties> docProps = dpf.Load();
			LOGGER.trace("Load Lookup file");
			loadLookupFiles(appConfig, docProps);
			// set batch types
			LOGGER.trace("Run BTC");
			BatchTypesCalculator.run(docProps);
			// save to new file
			LOGGER.trace("Save DPF");
			dpf.Save(docProps);
			LOGGER.trace("Data saved to: {}", outputFile);
		} catch (Exception e) {
			LOGGER.fatal(e);
			System.exit(1);
		}

		double seconds = (System.nanoTime() - start) / 1000000000.0;
		System.out.format("Univocity Parser : %f \n", Double.valueOf(seconds));
		// Run the garbage collector
		runtime.gc();
		// Calculate the used memory
		System.out
				.println("Used memory is megabytes: " + bytesToMegabytes(runtime.totalMemory() - runtime.freeMemory()));

	}

	private static AppConfig loadPropertiesFile() throws Exception{
		ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
		mapper.setVisibility(PropertyAccessor.FIELD, Visibility.ANY);
		return mapper.readValue(new File(propsFile), AppConfig.class);
	}

	/**
	 * Validate and set args
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

	private static void loadLookupFiles(AppConfig appConfig, ArrayList<DocumentProperties> docProps) {
		// set Production Config using the Selector Lookup file
		SelectorLookup lookup = new SelectorLookup(appConfig.LookupFile());
		ProductionConfiguration.init(appConfig.ProductionConfigPath()
				+ lookup.get(docProps.get(0).getSelectorRef()).getProductionConfig() 
				+ appConfig.ProductionFileSuffix());
		PresentationConfiguration.init(appConfig.getPresentationPriorityConfigPath() + lookup.get(docProps.get(0).getSelectorRef()).getPresentationConfig()
				+ appConfig.getPresentationPriorityFileSuffix());
	}
}
