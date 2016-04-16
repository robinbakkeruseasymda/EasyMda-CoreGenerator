package flca.mda.codegen.helpers;

import java.util.HashMap;
import java.util.Map;

public class IniFileSection {

	private String sectionName;
	
	private Map<String, String> map = new HashMap<String, String>();

	public IniFileSection(String section)
	{
		super();
		this.sectionName = section;
	}

	public String getSectionName()
	{
		return sectionName;
	}

	public void setSectionName(String section)
	{
		this.sectionName = section;
	}

	public Map<String, String> getMap() {
		return map;
	}

	public void setMap(Map<String, String> map) {
		this.map = map;
	}
	
	public void add(String aKey, String aValue)
	{
		map.put(aKey, aValue);
	}
	
	public String getValue(String aKey)
	{
		if (map.containsKey(aKey)) {
			return map.get(aKey);
		} else if (map.containsKey("*")) {
			return map.get("*");
		} else {
			return "";
		}
	}
	
	public IniFileSection clone()
	{
		IniFileSection r = new IniFileSection(this.getSectionName());
		for (String key : this.map.keySet()) {
			String value = this.map.get(key);
			r.add(key, value);
		}
		return r;
	}
	
	public void merge(IniFileSection aWith)
	{
		for (String key : aWith.getMap().keySet()) {
			if (aWith.getMap().get(key) != null) {
				this.getMap().put(key, aWith.getMap().get(key));
			}
		}
	}
	
	@Override
	public String toString()
	{
		StringBuffer sb = new StringBuffer();
		
		sb.append("[" + sectionName + "]:");
		
		for (String key : map.keySet()) {
			sb.append(key + "=" + map.get(key) + ",");
		}
		return sb.toString();
	}
}
