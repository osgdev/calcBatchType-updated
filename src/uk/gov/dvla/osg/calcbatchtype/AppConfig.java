package uk.gov.dvla.osg.calcbatchtype;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;
/*import java.io.File;*/

/*import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;*/

class AppConfig {

	private String documentReference;
	private String ottField;
	private String appNameField;
	private String fleetField;
	private String titleField;
	private String name1Field;
	private String name2Field;
	private String address1Field;
	private String address2Field;
	private String address3Field;
	private String address4Field;
	private String address5Field;	
	private String postcodeField;
	private String mscField;
	private String langField;
	private String lookupReferenceFieldName;
	private String batchType;
	// OUTPUT FIELDS
	private String groupIdField;
	private String outputBatchType;
	private String eogField;
	// FILES
	private String lookupFile;
	private String presentationPriorityConfigPath;
	private String presentationPriorityFileSuffix;
	private String productionConfigPath;
	private String productionFileSuffix;
	private String postageConfigPath;
	private String postageFileSuffix;
	private String presentationPriorityField;

	public AppConfig(String fileName) throws Exception {
		Properties prop = new Properties();
		InputStream input = new FileInputStream(fileName);
		prop.load(input);
		documentReference = prop.getProperty("documentReference");
		ottField = prop.getProperty("ottField");
		appNameField = prop.getProperty("appNameField");
		fleetField = prop.getProperty("fleetField");
		titleField = prop.getProperty("titleField");
		name1Field = prop.getProperty("name1Field");
		name2Field = prop.getProperty("name2Field");
		address1Field = prop.getProperty("address1Field");
		address2Field = prop.getProperty("address2Field");
		address3Field = prop.getProperty("address3Field");
		address4Field = prop.getProperty("address4Field");
		address5Field = prop.getProperty("address5Field");
		postcodeField = prop.getProperty("postcodeField");
		mscField = prop.getProperty("mscField");
		langField = prop.getProperty("langField");
		lookupReferenceFieldName = prop.getProperty("lookupReferenceFieldName");
		batchType = prop.getProperty("batchType");
		groupIdField = prop.getProperty("groupIdField");
		outputBatchType = prop.getProperty("outputBatchType");
		eogField = prop.getProperty("eogField");
		lookupFile = prop.getProperty("lookupFile");
		presentationPriorityConfigPath = prop.getProperty("presentationPriorityConfigPath");
		presentationPriorityFileSuffix = prop.getProperty("presentationPriorityFileSuffix");
		postageConfigPath = prop.getProperty("postageConfigPath");
		postageFileSuffix = prop.getProperty("postageFileSuffix");
		presentationPriorityField = prop.getProperty("presentationPriorityField");
		productionConfigPath = prop.getProperty("productionConfigPath");
		productionConfigPath = prop.getProperty("productionConfigPath");
		productionFileSuffix = prop.getProperty("productionFileSuffix");
	}
	
	public String EogField() {
		return this.eogField;
	}
	public String LookupFile() {
		return lookupFile;
	}

	public String SelectorRef() {
		return lookupReferenceFieldName;
	}

	public String OttField() {
		return ottField;
	}

	public String AppField() {
		return appNameField;
	}

	public String FleetField() {
		return fleetField;
	}

	public String TitleField() {
		return titleField;
	}

	public String Name1Field() {
		return name1Field;
	}

	public String Name2Field() {
		return name2Field;
	}

	public String Add1Field() {
		return address1Field;
	}

	public String Add2Field() {
		return address2Field;
	}

	public String Add3Field() {
		return address3Field;
	}

	public String Add4Field() {
		return address4Field;
	}

	public String Add5Field() {
		return address5Field;
	}

	public String PcField() {
		return postcodeField;
	}

	public String MscField() {
		return mscField;
	}

	public String BatchType() {
		return batchType;
	}

	public String DocRef() {
		return documentReference;
	}

	public String GroupIdField() {
		return groupIdField;
	}

	public String LangField() {
		return langField;
	}

	public String PresentationPriorityConfigPath() {
		return presentationPriorityConfigPath;
	}

	public String PresentationPriorityFileSuffix() {
		return presentationPriorityFileSuffix;
	}

	public String ProductionConfigPath() {
		return productionConfigPath;
	}

	public String ProductionFileSuffix() {
		return productionFileSuffix;
	}

	public String PostageConfigPath() {
		return postageConfigPath;
	}

	public String PostageFileSuffix() {
		return postageFileSuffix;
	}

	public String OutputBatchType() {
		return outputBatchType;
	}
	
	public String getPresentationPriorityField() {
		return this.presentationPriorityField;
	}
	public String getPresentationPriorityConfigPath() {
		return this.presentationPriorityConfigPath;
	}
	public String getPresentationPriorityFileSuffix() {
		return this.presentationPriorityFileSuffix;
	}
	/*ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
	mapper.setVisibility(PropertyAccessor.ALL, Visibility.ANY);
	return mapper.readValue(new File(fileName), AppConfig.class);*/
}