package flca.mda.codegen.data;

import java.io.File;
import java.lang.reflect.Method;
import java.util.Collection;

/**
 * Object that is stored in TemplatesStore
 * @author nly36776
 *
 */
public class TemplatesStoreData
{
	private File templatesProjectDir;
	private Class<?> registerClass;
	private TemplatesTree templatesTree;

	public TemplatesStoreData() {
	}

	
	public TemplatesStoreData(File templatesProjectDir, Class<?> registerClass,
			TemplatesTree templatesTree)
	{
		super();
		this.templatesProjectDir = templatesProjectDir;
		this.registerClass = registerClass;
		this.templatesTree = templatesTree;
	}

	public Class<?> getRegisterClass()
	{
		return registerClass;
	}
	public void setRegisterClass(Class<?> registerClass)
	{
		this.registerClass = registerClass;
	}
	public TemplatesTree getTemplatesTree()
	{
		return templatesTree;
	}
	public void setTemplatesTree(TemplatesTree templatesTree)
	{
		this.templatesTree = templatesTree;
	}

	public File getTemplatesProjectDir()
	{
		return templatesProjectDir;
	}

	public void setTemplatesProjectDir(File templatesProjectDir)
	{
		this.templatesProjectDir = templatesProjectDir;
	}
	
	@SuppressWarnings("unchecked")
	public Collection<SubsValue> getSubstitutes()
	{
		try {
			if (this.registerClass != null) {
				Object obj = registerClass.newInstance();
				Method m = registerClass.getMethod("getSubstituteValues", new Class<?>[] {});
				Object result = m.invoke(obj, new Object[] {});
				return (Collection<SubsValue>) result;
			}
		} catch (Exception e) {
			e.printStackTrace();
		} 
		return null;
	}
	
}
