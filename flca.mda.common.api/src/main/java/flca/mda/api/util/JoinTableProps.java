package flca.mda.api.util;

/**
 * Structure to store several values that can be used to handle JoinTables
 * example a join table JoinAD AS3Helper.getJoinTableProps(class) returns 2 JoinTableManyToOneProps
 * with 
 * manyToOnePropName = a, manyToOnePropType = A, masterPropName = d, masterPropType = D
 * manyToOnePropName = d, manyToOnePropType = D, masterPropName = a, masterPropType = a
 * @author rbakkerus
 * 
 */
public class JoinTableProps
{
	public String manyToOnePropName;
	public String manyToOneCapPropName;
	public String manyToOnePropType;
	public String masterPropName;
	public String masterCapPropName;
	public String masterPropType;
}
