package uk.gov.dvla.osg.calcbatchtype;

import static org.apache.commons.lang3.StringUtils.*;
import static uk.gov.dvla.osg.common.classes.BatchType.*;
import static uk.gov.dvla.osg.common.classes.FullBatchType.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import uk.gov.dvla.osg.common.config.PresentationConfiguration;
import uk.gov.dvla.osg.common.config.ProductionConfiguration;
public class Backup {

	private static final Logger LOGGER = LogManager.getLogger();

	public static void run(ArrayList<DocumentProperties> docProps) {
		LOGGER.info("CalculateBatchTypes initiated");
		ProductionConfiguration pc = ProductionConfiguration.getInstance();
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
		int maxMulti = pc.getMaxMulti();
		multiCustomers.forEach(docProp -> {
			if ( (docProp.getLang().equals("E") && !pc.getSite(CLERICALE).equals("X"))
				|| (docProp.getLang().equals("W") && !pc.getSite(CLERICALW).equals("X")) ) {
				int occurrences = Collections.frequency(docProps, docProp);
				if (occurrences > maxMulti) {
					//Change batch type to CLERICAL
					LOGGER.debug("Changed to clerical {} NO. {}", docProp.getName1(), occurrences);
					clericalCustomers.add(docProp);
				}
			}
		});

		// Use sets above plus PC file to determine batch type for each record
		docProps.forEach(dp -> {
			if (dp.getBatchType() == null) {
				if (dp.getLang().equals("E")) {
					if (isNotEmpty(dp.getFleetNo()) && !pc.getSite(FLEETE).equals("X")) {
						dp.setBatchType(FLEET);
						dp.setGroupId(fleetMap.get(dp.getFleetNo() + dp.getLang()));
					} else if (isEmpty(dp.getMsc())) {
						dp.setBatchType(UNSORTED);
						dp.setEog();
					} else if (clericalCustomers.contains(dp) && !pc.getSite(CLERICALE).equals("X")) {
						dp.setBatchType(CLERICAL);
						dp.setGroupId(multiMap.get(dp));
					} else if (multiCustomers.contains(dp) && containsNone(pc.getSite(MULTIE), 'x','X')) {
						dp.setBatchType(MULTI);
						dp.setGroupId(multiMap.get(dp));
					} else if (multiCustomers.contains(dp) && pc.getSite(MULTIE).equals("X")) {
						dp.setGroupId(multiMap.get(dp));
						if (!pc.getSite(SORTEDE).equals("X")) {
							dp.setBatchType(SORTED);
							dp.setEog();
						} else {
							dp.setBatchType(UNSORTED);
							dp.setEog();
						}
					} else if (!pc.getSite(SORTEDE).equals("X")) {
						dp.setBatchType(SORTED);
						dp.setEog();
					} else {
						dp.setBatchType(UNSORTED);
						dp.setEog();
					}
				} else {
					if (isNotEmpty(dp.getFleetNo()) && !pc.getSite(FLEETW).equals("X")) {
						dp.setBatchType(FLEET);
						dp.setGroupId(fleetMap.get(dp.getFleetNo() + dp.getLang()));
					} else if (isEmpty(dp.getMsc())) {
						dp.setBatchType(UNSORTED);
						dp.setEog();
					} else if (clericalCustomers.contains(dp) && !pc.getSite(CLERICALW).equals("X")) {
						dp.setBatchType(CLERICAL);
						dp.setGroupId(multiMap.get(dp));
					} else if (multiCustomers.contains(dp) && containsNone(pc.getSite(MULTIW), 'x','X')) {
						dp.setBatchType(MULTI);
						dp.setGroupId(multiMap.get(dp));
					} else if (multiCustomers.contains(dp) && pc.getSite(MULTIW).equals("X")) {
						dp.setGroupId(multiMap.get(dp));
						if (!pc.getSite(UNSORTEDW).equals("X")) {
							dp.setBatchType(SORTED);
							dp.setEog();
						} else {
							dp.setBatchType(UNSORTED);
							dp.setEog();
						}
					} else if (!pc.getSite(SORTEDW).equals("X")) {
						dp.setBatchType(SORTED);
						dp.setEog();
					} else {
						dp.setBatchType(UNSORTED);
						dp.setEog();
					}
				}
			}
			dp.setPresentationPriority(PresentationConfiguration.getInstance().lookupRunOrder(dp.getBatchType().name()));
		});
	}
}
