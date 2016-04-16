package flca.mda.api.util;

import java.util.ArrayList;
import java.util.List;

/**
 * helper class to keep track of all (deeply) nested objects of a given class.
 * In this NestedObjects the tree is flattened into a list of NestedObject's 
 * For, for example: Testa this shoudl result in NestedOnjects like:
 * NestedObject({}, {tstb}, Testb.class)  // first {} are the parent class(es), second {} the fields
 * NestedObject({}, {tstsc}, Testc.class)
 * NestedObject({Testc.class}, {tstsc,tstsd}, Testd.class)
 * NestedObject({Testc.class}, {tstsc,tstse}, Teste.class)
 * @author ed45064.ts1
 *
 */
public class NestedObjects implements TypeConstants {

	private static final TypeUtils tu = new TypeUtils();
	
	private Class<?> root;
	private List<NestedObject> nestedObjects = new ArrayList<NestedObject>();

	public NestedObjects(Class<?> root) {
		super();
		this.root = root;
		nestedObjects = fillNested(root, nestedObjects, -1);
	}

	public Class<?> getRoot() {
		return root;
	}
	
	public List<NestedObject> getNestedObjects() {
		return nestedObjects;
	}

	private List<NestedObject> fillNested(Class<?> aClass, List<NestedObject> aNestedObjects, int aIndex) {
		List<NestedObject> result = aNestedObjects;
		//-- in 4 separate steps so that the order is more predictable 
		List<Fw> fields = tu.getFieldsInc(aClass, O2M_FLD);
		fields.addAll(tu.getFieldsInc(aClass, M2O_FLD));
		fields.addAll(tu.getFieldsInc(aClass, M2M_FLD));
		fields.addAll(tu.getFieldsInc(aClass, O2O_FLD));
		
		for (Fw fw : fields) {
			if (fw.isOwner()) {
				NestedObject lastone = (aIndex < 0) ? null : result.get(aIndex);
				NestedObject nestobj = new NestedObject(fw, aClass, lastone);
				result.add(nestobj);
				
				Class<?> currtype = fw.genericType();
				if (!recursive(currtype, result)) {
					result = fillNested(currtype, result, aIndex + 1);
				}
			}
		}
		return result;
	}
	
	private boolean recursive(Class<?> aCurrType, List<NestedObject> aNestedObjects) {
		for (NestedObject nestobj : aNestedObjects) {
			for (Class<?> clz : nestobj.getParents()) {
				if (clz.equals(aCurrType)) return true;
			}
		}
		
		return false;
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer() ;
		sb.append("root=" + this.root.getSimpleName() + "\n");
		for (NestedObject nested : nestedObjects) {
			sb.append(nested.toString() + "\n");
		}
		return sb.toString();
	}
}



	

