package uk.gov.dvla.osg.calcbatchtype;

import static org.apache.commons.lang3.StringUtils.*;
import static uk.gov.dvla.osg.common.classes.BatchType.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import uk.gov.dvla.osg.common.classes.BatchType;
import uk.gov.dvla.osg.common.classes.FullBatchType;
import uk.gov.dvla.osg.common.config.PresentationConfiguration;
import uk.gov.dvla.osg.common.config.ProductionConfiguration;

/**
 * Calculates the batch type for each record, if not already set.
 * Current batch types are CLERICAL, FLEET, MULTI, SORTED & UNSORTED.
 */
public class BatchTypesCalculator {

	private static final Logger LOGGER = LogManager.getLogger();
	
	public static void run(ArrayList<DocumentProperties> docProps) {
		LOGGER.info("CalculateBatchTypes initiated");
		PresentationConfiguration presConfig = PresentationConfiguration.getInstance();
		// Sets ensure that lists only contain unique customers
		Set<DocumentProperties> uniqueCustomers = new HashSet<DocumentProperties>();
		Set<DocumentProperties> multiCustomers = new HashSet<DocumentProperties>();
		Set<DocumentProperties> clericalCustomers = new HashSet<DocumentProperties>();
		Map<DocumentProperties, Integer> multiMap = new HashMap<DocumentProperties, Integer>();
		Map<String, Integer> fleetMap = new HashMap<String, Integer>();
		Set<String> uniqueFleets = new HashSet<String>();

		// Group Fleets together & determine if non-fleets are unique or multis
		docProps.forEach(docProp -> {
			if (isBlank(docProp.getFleetNo())) {
				if (!(uniqueCustomers.add(docProp))) {
					multiCustomers.add(docProp);
				}
			} else {
				uniqueFleets.add(docProp.getFleetNo() + docProp.getLang());
			}
		});

		// Counter is used to set the group ID for multis and fleets
		AtomicInteger i = new AtomicInteger(1);
		multiCustomers.forEach(prop -> multiMap.put(prop, i.getAndIncrement()));
		uniqueFleets.forEach(fleet -> fleetMap.put(fleet, i.getAndIncrement()));

		// Batch Type changed to clerical when over the maxMulti limit
		int maxMulti = ProductionConfiguration.getInstance().getMaxMulti();
		
		multiCustomers.forEach(docProp -> {
			if (isNotIgnore(CLERICAL, docProp.getLang())) {
				int occurrences = Collections.frequency(docProps, docProp);
				if (occurrences > maxMulti) {
					//Change batch type to CLERICAL
					clericalCustomers.add(docProp);
				}
			}
		});

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
				} else if (multiCustomers.contains(dp) && isAllowMulti(dp.getLang())) {
					dp.setBatchType(MULTI);
					dp.setGroupId(multiMap.get(dp));
				} else if (multiCustomers.contains(dp) && isGroup(MULTI, dp.getLang())) {
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
			dp.setPresentationPriority(presConfig.lookupRunOrder(dp.getBatchType()));
		});
	}

	
	/**
	 * If site is set to X then AllowMulti is false but isGroup is true. Records are processed as 
	 * singles with groupId set. 
	 * @param multi
	 * @param lang
	 * @return
	 */
	private static boolean isGroup(BatchType multi, String lang) {
		return !isNotIgnore(multi, lang);
	}


	/**
	 * Checks the site property in the production configuration file to see 
	 * if the batch type should be ignored.
	 * @param batchType
	 * @param lang
	 * @return true if site is not set as 'X' in config file
	 */
	private static boolean isNotIgnore(BatchType batchType, String lang) {
		return !ProductionConfiguration.getInstance().getSite(FullBatchType.valueOf(batchType.name() + lang)).equals("X");
	}
	
	/**
	 * Checks the site property in the production configuration file to see if Multis are to be processed.
	 * @param batchType
	 * @param lang
	 * @return true if site is not 'X' or 'XX' in config file
	 */
	private static boolean isAllowMulti(String lang) {
		String site = ProductionConfiguration.getInstance().getSite(FullBatchType.valueOf("MULTI" + lang));
			return !(site.equals("XX") || site.equals("X"));
	}
}
