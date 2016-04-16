package flca.mda.api.util;

public class SpecialField {

	private Class<?> type;
	private String name;
	private boolean isId;
	private String defaultValue;
	private FwSelectType specialFieldType;
	
	public SpecialField(Class<?> type, String name, boolean isId, String defValue, FwSelectType fieldType) {
		super();
		this.type = type;
		this.name = name;
		this.isId = isId;
		this.defaultValue = defValue;
		this.specialFieldType = fieldType;
	}
	
	public Class<?> getType() {
		return type;
	}
	public void setType(Class<?> type) {
		this.type = type;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}

	public boolean isId() {
		return isId;
	}

	public String getDefaultValue() {
		return defaultValue;
	}

	public void setDefaultValue(String defaultValue) {
		this.defaultValue = defaultValue;
	}

	public FwSelectType getSpecialFieldType() {
		return specialFieldType;
	}

	public void setSpecialFieldType(FwSelectType specialFieldType) {
		this.specialFieldType = specialFieldType;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((specialFieldType == null) ? 0 : specialFieldType.hashCode());
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SpecialField other = (SpecialField) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (specialFieldType != other.specialFieldType)
			return false;
		if (type == null) {
			if (other.type != null)
				return false;
		} else if (!type.equals(other.type))
			return false;
		return true;
	}
	
	
	
}
