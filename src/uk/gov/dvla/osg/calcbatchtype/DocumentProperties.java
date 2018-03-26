package uk.gov.dvla.osg.calcbatchtype;

import org.apache.commons.lang3.StringUtils;

import uk.gov.dvla.osg.common.classes.BatchType;

/**
 * Subset of the Doc Props (.dpf) file.
 */
public class DocumentProperties {

	private String selectorRef, docRef, ott, fleetNo, title, name1, name2, add1, add2, add3, add4, add5, pc, msc, dps,
			appName, outputMedia, lang, eog;
	private BatchType batchType;
	private Integer groupId;
	private int presentationPriority;

	public DocumentProperties(String selectorRef, String docRef, String ott, String appName, String fleetNo,
			String title, String name1, String name2, String add1, String add2, String add3, String add4, String add5,
			String pc, String msc, String lang) {

		this.selectorRef = selectorRef;
		this.docRef = docRef;
		this.ott = ott;
		this.appName = appName;
		this.fleetNo = fleetNo;
		this.title = title;
		this.name1 = name1;
		this.name2 = name2;
		this.add1 = add1;
		this.add2 = add2;
		this.add3 = add3;
		this.add4 = add4;
		this.add5 = add5;
		this.pc = pc;
		this.msc = msc;
		this.lang = lang;

	}

	public void setEog() {
		this.eog = "X";
	}

	public String getEog() {
		return this.eog;
	}

	public String getSelectorRef() {
		return selectorRef;
	}

	public void setSelectorRef(String selectorRef) {
		this.selectorRef = selectorRef;
	}

	public String getLang() {
		return lang;
	}

	public void setLang(String lang) {
		this.lang = lang;
	}

	public Integer getGroupId() {
		return groupId;
	}

	public void setGroupId(int groupId) {
		this.groupId = groupId;
	}

	public String getDocRef() {
		return docRef;
	}

	public void setDocRef(String docRef) {
		this.docRef = docRef;
	}

	public String getOtt() {
		return ott;
	}

	public void setOtt(String ott) {
		this.ott = ott;
	}

	public String getFleetNo() {
		return fleetNo;
	}

	public void setFleetNo(String fleetNo) {
		this.fleetNo = fleetNo;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getName1() {
		return name1;
	}

	public void setName1(String name1) {
		this.name1 = name1;
	}

	public String getName2() {
		return name2;
	}

	public void setName2(String name2) {
		this.name2 = name2;
	}

	public String getAdd1() {
		return add1;
	}

	public void setAdd1(String add1) {
		this.add1 = add1;
	}

	public String getAdd2() {
		return add2;
	}

	public void setAdd2(String add2) {
		this.add2 = add2;
	}

	public String getAdd3() {
		return add3;
	}

	public void setAdd3(String add3) {
		this.add3 = add3;
	}

	public String getAdd4() {
		return add4;
	}

	public void setAdd4(String add4) {
		this.add4 = add4;
	}

	public String getAdd5() {
		return add5;
	}

	public void setAdd5(String add5) {
		this.add5 = add5;
	}

	public String getPc() {
		return pc;
	}

	public void setPc(String pc) {
		this.pc = pc;
	}

	public String getMsc() {
		return msc;
	}

	public void setMsc(String msc) {
		this.msc = msc;
	}

	public String getDps() {
		return dps;
	}

	public void setDps(String dps) {
		this.dps = dps;
	}

	public String getAppName() {
		return appName;
	}

	public void setAppName(String appName) {
		this.appName = appName;
	}

	public String getOutputMedia() {
		return outputMedia;
	}

	public void setOutputMedia(String outputMedia) {
		this.outputMedia = outputMedia;
	}

	public BatchType getBatchType() {
		return this.batchType;
	}

	public void setBatchType(String batchType) {
		if (StringUtils.isNotBlank(batchType)) {
			this.batchType = BatchType.valueOf(batchType);
		}
	}

	public void setBatchType(BatchType bt) {
		this.batchType = bt;
	}

	public String getFullBatchType() {
		return batchType + lang;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((add1 == null) ? 0 : add1.hashCode());
		result = prime * result + ((add2 == null) ? 0 : add2.hashCode());
		result = prime * result + ((add3 == null) ? 0 : add3.hashCode());
		result = prime * result + ((add4 == null) ? 0 : add4.hashCode());
		result = prime * result + ((add5 == null) ? 0 : add5.hashCode());
		result = prime * result + ((appName == null) ? 0 : appName.hashCode());
		result = prime * result + ((fleetNo == null) ? 0 : fleetNo.hashCode());
		result = prime * result + ((name1 == null) ? 0 : name1.hashCode());
		result = prime * result + ((name2 == null) ? 0 : name2.hashCode());
		result = prime * result + ((ott == null) ? 0 : ott.hashCode());
		result = prime * result + ((pc == null) ? 0 : pc.hashCode());
		result = prime * result + ((title == null) ? 0 : title.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		DocumentProperties other = (DocumentProperties) obj;
		if (add1 == null) {
			if (other.add1 != null) return false;
		} else if (!add1.equals(other.add1)) return false;
		if (add2 == null) {
			if (other.add2 != null) return false;
		} else if (!add2.equals(other.add2)) return false;
		if (add3 == null) {
			if (other.add3 != null) return false;
		} else if (!add3.equals(other.add3)) return false;
		if (add4 == null) {
			if (other.add4 != null) return false;
		} else if (!add4.equals(other.add4)) return false;
		if (add5 == null) {
			if (other.add5 != null) return false;
		} else if (!add5.equals(other.add5)) return false;
		if (appName == null) {
			if (other.appName != null) return false;
		} else if (!appName.equals(other.appName)) return false;
		if (fleetNo == null) {
			if (other.fleetNo != null) return false;
		} else if (!fleetNo.equals(other.fleetNo)) return false;
		if (name1 == null) {
			if (other.name1 != null) return false;
		} else if (!name1.equals(other.name1)) return false;
		if (name2 == null) {
			if (other.name2 != null) return false;
		} else if (!name2.equals(other.name2)) return false;
		if (ott == null) {
			if (other.ott != null) return false;
		} else if (!ott.equals(other.ott)) return false;
		if (pc == null) {
			if (other.pc != null) return false;
		} else if (!pc.equals(other.pc)) return false;
		if (title == null) {
			if (other.title != null) return false;
		} else if (!title.equals(other.title)) return false;
		return true;
	}

	@Override
	public String toString() {
		return this.ott + "," + this.fleetNo + "," + this.title + "," + this.name1 + "," + this.name2 + "," + this.add1
				+ "," + this.add2 + "," + this.add3 + "," + this.add4 + "," + this.add5 + "," + this.pc + "," + this.msc
				+ "," + this.dps + "," + this.appName + "," + this.outputMedia + "," + this.batchType;
	}

	public int getPresentationPriority() {
		return presentationPriority;
	}

	public void setPresentationPriority(int presentationPriority) {
		this.presentationPriority = presentationPriority;
	}
}
