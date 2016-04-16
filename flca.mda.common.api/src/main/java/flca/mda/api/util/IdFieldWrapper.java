package flca.mda.api.util;


public class IdFieldWrapper extends Fw {

	public IdFieldWrapper(TypeUtils tu, Class<?> ownerClass) {
		super(tu, new SpecialField(Long.class, TypeUtils.ID, true, null, FwSelectType.ID), ownerClass);
	}

	@Override
	public boolean isRequired() {
		return false;
	}

	@Override 
	public boolean isVal() {
		return true;
	}
	
	@Override 
	public boolean isId() {
		return true;
	}
	
	@Override
	public boolean needsOption() {
		return true;
	}
	
}
