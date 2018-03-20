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

public class BatchTypesCalculator {

	private static final Logger LOGGER = LogManager.getLogger();

	private static ProductionConfiguration prodConfig;
	
	public static void run(ArrayList<DocumentProperties> docProps) {
		LOGGER.info("CalculateBatchTypes initiated");
		prodConfig = ProductionConfiguration.getInstance();
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
			if (isEmpty(docProp.getFleetNo())) {
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
		int maxMulti = prodConfig.getMaxMulti();
		multiCustomers.forEach(docProp -> {
			if (!prodConfig.getSite(FullBatchType.valueOf(CLERICAL+""+docProp.getLang())).equals("X")) {
				int occurrences = Collections.frequency(docProps, docProp);
				if (occurrences > maxMulti) {
					LOGGER.debug("{} {}", docProp.getName1(), occurrences);
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
				} else if (multiCustomers.contains(dp) && isNotIgnore(MULTI, dp.getLang())) {
					dp.setBatchType(MULTI);
					dp.setGroupId(multiMap.get(dp));
				} else if (multiCustomers.contains(dp) && isIgnore(MULTI, dp.getLang())) {
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
			}
			dp.setPresentationPriority(presConfig.lookupRunOrder(dp.getBatchType()));
		});
	}

	private static boolean isIgnore(BatchType batchType, String lang) {
		return !isNotIgnore(batchType, lang);
	}

	private static boolean isNotIgnore(BatchType batchType, String lang) {
		if (batchType.equals(MULTI)) {
			return containsNone(prodConfig.getSite(FullBatchType.valueOf(batchType.name() + lang)), 'x', 'X');
		}
		return !prodConfig.getSite(FullBatchType.valueOf(batchType.name() + lang)).equals("X");
	}
}
