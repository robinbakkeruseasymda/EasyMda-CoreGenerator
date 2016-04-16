package flca.mda.codegen.data;

import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SubsValue implements Comparable<SubsValue> {
	private String name;
	private SubsValueType type = SubsValueType.TEXTINPUT;// default
	private String comboList[];
	private String value;
	private String label;
	private int rank = 5; // 1 is at top
	// inidicated for cartridge this subsvalue should be shown. Null mean all cartridges
	private String useForCartridges[];
	private String help;
	private boolean required = true;

	private static Logger logger = LoggerFactory.getLogger(SubsValue.class);

	public SubsValue(String aName) {
		super();
		if (aName == null) {
			logger.error("ctor SubsValue called with null value");
			aName = "";
		}
		this.name = aName;
	}

	public SubsValue(String aName, String aValue) {
		this(aName);
		this.value = aValue;
	}

	public SubsValue(String aName, String aValue, SubsValueType aType) {
		this(aName, aValue);
		this.type = aType;
	}


	public SubsValue(String aName, String aValue, SubsValueType aType, String[] comboList) {
		this(aName, aValue, aType);
		this.comboList = comboList;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public SubsValueType getType() {
		return type;
	}

	public void setType(SubsValueType type) {
		this.type = type;
	}

	public String[] getComboList() {
		return comboList;
	}

	public void setComboList(String[] comboList) {
		this.comboList = comboList;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public String getLabel() {
		if (label != null) {
			return label;
		} else {
			if (name != null) {
				return name;
			} else {
				return "???";
			}
		}
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public int getRank() {
		return rank;
	}


	public String[] getUseForCartridges() {
		return useForCartridges;
	}

	public void setUseForCartridges(String[] useForCartridges) {
		this.useForCartridges = useForCartridges;
	}

	public void setRank(int rank) {
		this.rank = rank;
	}

	public String getHelp() {
		return help;
	}

	public void setHelp(String help) {
		this.help = help;
	}

	/**
	 * @return the required
	 */
	public boolean isRequired() {
		return required;
	}

	/**
	 * @param required
	 *            the required to set
	 */
	public void setRequired(boolean required) {
		this.required = required;
	}

	public void merge(SubsValue aWith) {
		if (aWith != null && aWith.getName().equals(this.getName())) {
			this.setValue(aWith.getValue());
			// this.setDefaultValue(aWith.)
		} else {
			logger.warn("SubsValue.merge() may only be used when it has the same name");
		}
	}

	@Override
	public String toString() {
		return getName() + "=" + getValue() + " [" + getRank() + "]";
	}

	private static SubsValue getSubsValue(String aName, Collection<SubsValue> aSubsValues) {
		if (aName != null) {
			for (SubsValue subsval : aSubsValues) {
				if (aName.equals(subsval.getName())) {
					return subsval;
				}
			}
		}
		return null;
	}

	public static boolean containsSubsFrom(String aName, Collection<SubsValue> aSubsValues) {
		return getSubsValue(aName, aSubsValues) != null;
	}

	@Override
	public int compareTo(SubsValue o) {
		int c = new Integer(this.rank).compareTo(new Integer(o.getRank()));
		if (c == 0) {
			c = this.type.name().compareTo(o.type.name());
		}
		if (c == 0) {
			c = this.name.compareTo(o.name);
		}
		
		return c;
	}

	@Override
	public boolean equals(Object arg0) {
		if (arg0 != null) {
			SubsValue other = (SubsValue) arg0;

			if (name.equals(other.getName())) {
				if (useForCartridges == null) {
					return other.useForCartridges == null; 
				} else {
					return useForCartridges.equals(other.useForCartridges);
				}
			} else {
				return false;
			}
		} else {
			return false;
		}
	}

}
