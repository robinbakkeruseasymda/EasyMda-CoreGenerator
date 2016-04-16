package flca.mda.api.util;

import java.util.ArrayList;
import java.util.List;

/**
 * Helper class to keep track of (deeply) nested objects
 * 
 * @author robin
 *
 */
public class NestedObject {

	private List<Class<?>> parents = new ArrayList<Class<?>>();
	private List<Fw> fields = new ArrayList<Fw>();
	private Class<?> nestedType;
	
	public NestedObject(Fw aField, Class<?> currentClass, NestedObject previous) {
		if (previous != null) {
			this.parents.addAll(previous.parents);
			this.fields.addAll(previous.fields);
		}
		
		this.fields.add(aField);
		this.parents.add(currentClass);
		this.nestedType = aField.genericType();
	}

	public Class<?> getRootClass() {
		return (getParents().isEmpty()) ? null : getParents().get(0);
	}
	
	public Class<?> lastParentClass() {
		return (getParents().isEmpty()) ? null : getParents().get(getParents().size()-1);
	}

	public Fw lastField() {
		return (getFieldsExc().isEmpty()) ? null : getFieldsExc().get(getFieldsExc().size()-1);
	}
	

 	public List<Class<?>> getParents() {
		return parents;
	}

	public List<Fw> getFieldsExc() {
		return fields;
	}

	public Class<?> getNestedType() {
		return nestedType;
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("[");
		for (Class<?> p : parents) {
			sb.append(p.getSimpleName() + ",");
		}
		sb.append("], fields=[");
		for (Fw f : fields) {
			sb.append(f.name() + ",");
		}
		sb.append("], nested=" + nestedType.getSimpleName());
		return sb.toString();
	}
}