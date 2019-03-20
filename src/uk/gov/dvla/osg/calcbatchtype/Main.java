package uk.gov.dvla.osg.calcbatchtype;

import java.io.File;
import java.util.ArrayList;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import uk.gov.dvla.osg.common.classes.Selector;
import uk.gov.dvla.osg.common.config.PresentationPriorityLookup;
import uk.gov.dvla.osg.common.config.ProductionConfiguration;
import uk.gov.dvla.osg.common.config.SelectorLookup;

public class Main {

    private static final Logger LOGGER = LogManager.getLogger();

    private static final int EXPECTED_NUMBER_OF_ARGS = 3;

    private static String inputFile;
    private static String outputFile;
    private static String propsFile;

    private static ProductionConfiguration prodConfig;

    private static PresentationPriorityLookup presLookup;

    public static void main(String[] args) {
        LOGGER.trace("--- calcBatchType Started ---");

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
            BatchTypesCalculator btc = new BatchTypesCalculator(prodConfig, presLookup);
            btc.run(docProps);
            LOGGER.trace("Printing Summary");
            // save to new file
            LOGGER.trace("Save DPF");
            dpf.Save(docProps);
            LOGGER.trace("Data saved to: {}", outputFile);
            summaryPrint(docProps);
        } catch (Exception ex) {
            LOGGER.fatal(ExceptionUtils.getStackTrace(ex));
            System.exit(1);
        }

        LOGGER.trace("--- calcBatchType Finished ---");
    }

    /**
     * Validate number of expected arguements and set args
     * 
     * @param args
     */
    private static void setArgs(String[] args) {

        if (args.length != EXPECTED_NUMBER_OF_ARGS) {
            LOGGER.fatal("Incorrect number of args parsed '{}' expecting '{}'. "
                    + "Args are "
                    + "1. Input file, "
                    + "2. Output file, "
                    + "3. Props file.", args.length, EXPECTED_NUMBER_OF_ARGS);
            System.exit(1);
        }

        inputFile = args[0];
        boolean inputFileExists = new File(inputFile).exists();
        if (!inputFileExists) {
            LOGGER.fatal("Input File '{}' doesn't exist", inputFile);
            System.exit(1);
        }

        outputFile = args[1];

        propsFile = args[2];
        if (!new File(propsFile).exists()) {
            LOGGER.fatal("Properties File '{}' doesn't exist", propsFile);
            System.exit(1);
        }

    }

    /**
     * Loads selector, Production Configuration and Presentation Configuration
     * files.
     * 
     * @param appConfig Application Configuration file
     * @param docProps Document Properties file
     */
    private static void loadLookupFiles(AppConfig appConfig, ArrayList<DocumentProperties> docProps) {
        // Lookup config files based on the selector, format is path+fileName+suffix
        SelectorLookup selLookup = SelectorLookup.getInstance(appConfig.LookupFile());
        String selRef = docProps.get(0).getSelectorRef();

        if (!selLookup.isPresent(selRef)) {
            LOGGER.fatal("Selector [{}] is not present in the lookupFile.", selRef);
            System.exit(1);
        }

        Selector selector = selLookup.getSelector(selRef);

        prodConfig = ProductionConfiguration.getInstance(appConfig.ProductionConfigPath()
                + selector.getProductionConfig()
                + appConfig.ProductionFileSuffix());

        presLookup = PresentationPriorityLookup.getInstance(appConfig.getPresentationPriorityConfigPath()
                + selector.getPresentationConfig()
                + appConfig.getPresentationPriorityFileSuffix());
    }

    /**
     * Prints a summary of the number of items for each batch type.
     * 
     * @param docProps
     */
    private static void summaryPrint(ArrayList<DocumentProperties> docProps) {
        Map<String, Long> counting = docProps.stream().collect(Collectors.groupingBy(DocumentProperties::getFullBatchType, Collectors.counting()));

        LOGGER.debug(counting);

    }
}
