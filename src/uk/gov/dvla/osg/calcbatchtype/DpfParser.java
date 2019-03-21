package uk.gov.dvla.osg.calcbatchtype;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.univocity.parsers.common.processor.ConcurrentRowProcessor;
import com.univocity.parsers.common.processor.RowListProcessor;
import com.univocity.parsers.common.record.Record;
import com.univocity.parsers.tsv.*;

import uk.gov.dvla.osg.common.utils.Utils;

public class DpfParser {
    
    private static final Logger LOGGER = LogManager.getLogger();
    
    private String[] headers;
    private String inputFile;
    private String outputFile;
    private AppConfig appConfig;

    /**
     * Extracts DocumentProperties from a dpf data file.
     * 
     * @param inputFile dpf input file
     */
    public DpfParser(String inputFile, String outputFile, AppConfig appConfig) {
        this.inputFile = inputFile;
        this.outputFile = outputFile;
        this.appConfig = appConfig;
    }

    /**
     * Extracts records from the .dpf file.
     * 
     * @return a list of DocProps
     */
    public ArrayList<DocumentProperties> Load() {
        ArrayList<DocumentProperties> docProps = new ArrayList<>();
        TsvParser parser = createParser();
        List<Record> allRecords = parser.parseAllRecords(new File(inputFile));

        for (Record record : allRecords) {
            DocumentProperties dp = new DocumentProperties(record.getString(appConfig.SelectorRef()), 
                    record.getString(appConfig.DocRef()), 
                    record.getString(appConfig.OttField()), 
                    record.getString(appConfig.AppField()), 
                    record.getString(appConfig.FleetField()), 
                    record.getString(appConfig.TitleField()), 
                    record.getString(appConfig.Name1Field()), 
                    record.getString(appConfig.Name2Field()), 
                    record.getString(appConfig.Add1Field()), 
                    record.getString(appConfig.Add2Field()), 
                    record.getString(appConfig.Add3Field()), 
                    record.getString(appConfig.Add4Field()), 
                    record.getString(appConfig.Add5Field()), 
                    record.getString(appConfig.PcField()), record.getString(appConfig.MscField()), record.getString(appConfig.LangField()));
            dp.setBatchType(record.getString(appConfig.BatchType()));
            docProps.add(dp);
        }

        headers = parser.getRecordMetadata().headers();

        Utils.archiveDpf(inputFile, "Original.bak", LOGGER);

        return docProps;
    }

    /**
     * Saves the dpf file with the amended document properties.
     * 
     * @param outputFile path of the file to write out to
     * @throws IOException unable to write output file to the supplied path
     */
    public void Save(ArrayList<DocumentProperties> docProps) {
        try (FileWriter fw = new FileWriter(new File(outputFile))) {
            // Create an instance of TsvWriter with the default settings
            TsvWriter writer = new TsvWriter(fw, new TsvWriterSettings());
            // Writes the file headers
            writer.writeHeaders(headers);
            // Keep track of which row is being processed
            int counter = 0;
            // Build a parser that loops through the original dpf file
            TsvParser parser = createParser();
            // Extract all records from the input file
            List<String[]> allRecords = parser.parseAll(new File(inputFile));
            // Write each record to the output file
            for (String[] record : allRecords) {
                // Write out the original row of data
                writer.addValues((Object[]) record);
                // Replace changed values
                writer.addValue(appConfig.OutputBatchType(), docProps.get(counter).getBatchType());
                writer.addValue(appConfig.MscField(), docProps.get(counter).getMsc());
                writer.addValue(appConfig.EogField(), docProps.get(counter).getEog());
                writer.addValue(appConfig.GroupIdField(), docProps.get(counter++).getGroupId());
                writer.writeValuesToRow();
            }
            // Flushes and closes the writer
            writer.close();
            Utils.archiveDpf(outputFile, "CalcBatchType.bak", LOGGER);
        } catch (IOException ex) {
            LOGGER.fatal("Unable to write to {} : {}", outputFile, ex.getMessage());
            System.exit(1);
        }
    }

    /**
     * Create a new instance of a TsvParser
     * 
     * @return A TsvParser set to handle header rows
     */
    private TsvParser createParser() {
        TsvParserSettings parserSettings = new TsvParserSettings();
        parserSettings.setNullValue("");
        parserSettings.setProcessor(new ConcurrentRowProcessor(new RowListProcessor()));
        parserSettings.setLineSeparatorDetectionEnabled(true);
        parserSettings.setHeaderExtractionEnabled(true);
        return new TsvParser(parserSettings);
    }

}
