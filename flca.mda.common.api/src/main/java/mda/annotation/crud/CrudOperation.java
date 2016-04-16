package mda.annotation.crud;

/**
 * All the possible crud operation that are currently supported
 * @author nly36776
 *
 */
public enum CrudOperation {
	
	// these are backed up via @Crud annotation
	ALL,
	SAVE,
	RETRIEVE,
	FIND,
	FIND_ALL,
	REMOVE,
	
	// this is backed up via @Search annotation
	SEARCH; 

	public String[] methodPrefix() 
	{
		if ("ALL".equals(this.name())) {
			return allPrefix();
		} else if ("FIND_ALL".equals(this.name())) {
			return new String[] {"findAll"};
		} else {
			return new String[] {this.name().toLowerCase()};
		}
	}
	
	public static String[] allPrefix() {
		return new String[] {"save", "retrieve", "find", "findAll", "remove"};
	}
}
