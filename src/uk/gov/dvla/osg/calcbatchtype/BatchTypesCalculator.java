package uk.gov.dvla.osg.calcbatchtype;

import static org.apache.commons.lang3.StringUtils.*;
import static uk.gov.dvla.osg.common.enums.BatchType.*;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import uk.gov.dvla.osg.common.config.ProductionConfiguration;
import uk.gov.dvla.osg.common.enums.BatchType;
import uk.gov.dvla.osg.common.enums.FullBatchType;

/**
 * Calculates the batch type for each record, if not already set.
 * Current batch types are CLERICAL, FLEET, MULTI, SORTED, UNSORTED & UNSORTED
 */
public class BatchTypesCalculator {

	private static final int MIN_CUSTOMERS_FOR_MULTI = 2; // MultiCustomer + Unique Customer
    private static final Logger LOGGER = LogManager.getLogger();
    private final ProductionConfiguration prodConfig;
	
    public BatchTypesCalculator(ProductionConfiguration prodConfig) {
        this.prodConfig = prodConfig;
    }
    
	public void run(ArrayList<DocumentProperties> docProps) {
		LOGGER.info("CalculateBatchTypes initiated");
		// Sets ensure that lists only contain unique customers
		Set<DocumentProperties> uniqueCustomers = new HashSet<DocumentProperties>();
		Map<DocumentProperties, Integer> multiCustomers = new HashMap<>();
		Set<DocumentProperties> clericalCustomers = new HashSet<DocumentProperties>();
		Map<DocumentProperties, Integer> multiMap = new HashMap<DocumentProperties, Integer>();
		Map<String, Integer> fleetMap = new HashMap<String, Integer>();
		Set<String> uniqueFleets = new HashSet<String>();
        // Batch Type changed to clerical when over the maxMulti limit
        int maxMulti = prodConfig.getMaxMulti();
		
        // Group Fleets together & determine if non-fleets are unique or multis
		for (DocumentProperties docProp : docProps) {
	          if (isBlank(docProp.getFleetNo())) {
	                if (!(uniqueCustomers.add(docProp))) {
	                    if (!multiCustomers.containsKey(docProp)) {
	                        multiCustomers.put(docProp, MIN_CUSTOMERS_FOR_MULTI);  
	                    } else if (multiCustomers.get(docProp) < maxMulti) {
	                        multiCustomers.put(docProp, multiCustomers.get(docProp) + 1);
	                    } else {
	                        // Max Multi level hit so change batch type to CLERICAL
	                        clericalCustomers.add(docProp);
	                    }
	                }
	            } else {
	                uniqueFleets.add(docProp.getFleetNo() + docProp.getLang());
	            }
		}

		// Counter is used to set the group ID for multis and fleets
		AtomicInteger i = new AtomicInteger(1);
		multiCustomers.forEach((k,v) -> multiMap.put(k, i.getAndIncrement()));
		uniqueFleets.forEach(fleet -> fleetMap.put(fleet, i.getAndIncrement()));
		
		// Use sets above plus PC file to determine batch type for each record
		docProps.forEach(dp -> {
			if (dp.getBatchType() == null) {
				if (isNotEmpty(dp.getFleetNo()) && isNotIgnore(FLEET, dp.getLang())) {
					dp.setBatchType(FLEET);
					dp.setGroupId(fleetMap.get(dp.getFleetNo() + dp.getLang()));
				} else if (isEmpty(dp.getMsc())) {
					dp.setBatchType(UNSORTED);
					dp.setEog();
				} else if (clericalCustomers.contains(dp) && isNotIgnore(CLERICAL, dp.getLang())) {
					dp.setBatchType(CLERICAL);
					dp.setGroupId(multiMap.get(dp));
				} else if (multiCustomers.containsKey(dp) && isAllowMulti(dp.getLang())) {
					dp.setBatchType(MULTI);
					dp.setGroupId(multiMap.get(dp));
				} else if (multiCustomers.containsKey(dp) && isGroup(MULTI, dp.getLang())) {
					dp.setGroupId(multiMap.get(dp));
					if (isNotIgnore(SORTED, dp.getLang())) {
						dp.setBatchType(SORTED);
						dp.setEog();
					} else {
						dp.setBatchType(UNSORTED);
						dp.setEog();
					}
				} else if (isNotIgnore(SORTED, dp.getLang())) {
					dp.setBatchType(SORTED);
					dp.setEog();
				} else {
					dp.setBatchType(UNSORTED);
					dp.setEog();
				}
			} else {
				// Single so set EOG
				dp.setEog();
			}
		});
	}

	
	/**
	 * If site is set to X then AllowMulti is false but isGroup is true. Records are processed as 
	 * singles with groupId set. 
	 * @param multi
	 * @param lang
	 * @return
	 */
	private boolean isGroup(BatchType multi, String lang) {
		return !isNotIgnore(multi, lang);
	}


	/**
	 * Checks the site property in the production configuration file to see 
	 * if the batch type should be ignored.
	 * @param batchType
	 * @param lang
	 * @return true if site is not set as 'X' in config file
	 */
	private boolean isNotIgnore(BatchType batchType, String lang) {
		return !prodConfig.getSite(FullBatchType.valueOf(batchType.name() + lang)).equals("X");
	}
	
	/**
	 * Checks the site property in the production configuration file to see if Multis are to be processed.
	 * @param batchType
	 * @param lang
	 * @return true if site is not 'X' or 'XX' in config file
	 */
	private boolean isAllowMulti(String lang) {
		String site = prodConfig.getSite(FullBatchType.valueOf("MULTI" + lang));
			return !(site.equals("XX") || site.equals("X"));
	}
}
