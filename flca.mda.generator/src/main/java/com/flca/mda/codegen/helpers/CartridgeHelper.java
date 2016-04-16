package com.flca.mda.codegen.helpers;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import mda.type.IRegisterTemplates;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.flca.mda.codegen.engine.data.CartridgeClasspathData;
import com.flca.mda.codegen.engine.data.TemplatesTreeCloner;

import flca.mda.api.util.TypeUtils;
import flca.mda.codegen.CodegenConstants;
import flca.mda.codegen.data.DataStore;
import flca.mda.codegen.data.ITemplate;
import flca.mda.codegen.data.SubsValue;
import flca.mda.codegen.data.SubsValueType;
import flca.mda.codegen.data.TemplatesBranch;
import flca.mda.codegen.data.TemplatesStore;
import flca.mda.codegen.data.TemplatesStoreData;
import flca.mda.codegen.data.TemplatesTree;
import flca.mda.codegen.helpers.FileHelper;

public class CartridgeHelper {
	private static Logger logger = LoggerFactory.getLogger(CartridgeHelper.class);

	private static CartridgeHelper sInstance;

	private Map<String, CartridgeClasspathData> cartridgeClassPaths = null;

	private Set<ITemplate> selectedTemplates = new HashSet<ITemplate>();

	protected TypeUtils tu = new TypeUtils();
	
	public static CartridgeHelper getInstance() {
		if (sInstance == null) {
			sInstance = new CartridgeHelper();
		}
		return sInstance;
	}

	private CartridgeHelper() {
	}

	/**
	 * return all the classpath File(s) of the cartridges that are registed in
	 * the dropins/easymda/cartrdiges folder and are enabled (on the wizzard
	 * page#1) Note the cartridgeClassPaths are filled during the initialize()
	 * method
	 * 
	 * @return
	 */
	public Collection<CartridgeClasspathData> getAllCartridgeClassPaths() {
		if (cartridgeClassPaths == null) {
			cartridgeClassPaths = setupCartridgeClassPaths();
		}
		return cartridgeClassPaths.values();
	}

	public boolean getCartridgeEnableStatus(File aFile) {
		String key = CartridgeClasspathData.getClasspathKey(aFile);
		if (cartridgeClassPaths != null && cartridgeClassPaths.containsKey(key)) {
			return cartridgeClassPaths.get(key).isEnabled();
		} else {
			return false;
		}
	}

	/** 
	 * Here we may enable or disable a cartridge jar-file or bin dir
	 * @param aFile
	 * @param aValue
	 */
	public void setCartridgeEnableStatus(String aPath, Boolean aValue) {
		if (getAllCartridgeClassPaths() != null) {
			if (cartridgeClassPaths.containsKey(aPath)) {
				CartridgeClasspathData data = cartridgeClassPaths.get(aPath);
				data.setEnabled(aValue);
			}
		} else {
			LogHelper.error("cartridgeClassPaths is null while calling setCartridgeEnableStatus");
		}
	}

	/**
	 * return all the classpath File(s) of the cartridges that are registed in
	 * the dropins/easymda/cartrdiges folder and are enabled (on the wizzard
	 * page#1) Note the cartridgeClassPaths are filled during the initialize()
	 * method
	 * 
	 * @return
	 */
	public Collection<CartridgeClasspathData> getEnabledCartridgeClassPaths() {
		Collection<CartridgeClasspathData> result = new ArrayList<CartridgeClasspathData>();
		Collection<CartridgeClasspathData> paths = getAllCartridgeClassPaths();

		for (CartridgeClasspathData data : paths) {
			if (data.isEnabled()) {
				result.add(data);
			}
		}

		return result;
	}

	/**
	 * return all the TemplatesStoreData thate correspond with the enabled
	 * cartridges. Note this Map is filled filled during the initialize() method
	 * 
	 * @return
	 */
	public Map<File, TemplatesStoreData> getTemplatesDescriptors() {
		return TemplatesStore.getInstance().getData();
	}


	/**
	 * This method will look for all cartridges in plugin/cartrdiges folder,
	 * either as a jar file or a link file. It will populate the TemplatesStore
	 * dataMap, and also corresponding SubVals
	 */
	public void initialize(Collection<CartridgeClasspathData> aCartridges) {
		selectedTemplates.clear();
		TemplatesStore.getInstance().clear();

		if (cartridgeClassPaths == null) {
			setupCartridgeClassPaths();
		}

		if (cartridgeClassPaths != null) {
			initializeCartridgeClasspaths(aCartridges);
		} else {
			logger.error("cartridgeClassPaths is null??");
			throw new RuntimeException("cartridge classpaths are null");
		}

		storeCartridgeDir();
		ProjectInstanceHelper.getInstance().refresh();
	}

	private void storeCartridgeDir() {
		File cartridgeDir = PluginHelper.getInstance().getCartridgeDir();
		logger.info("setting " + CodegenConstants.DATASTORE_CARTRIDGE_DIR + " -> " + cartridgeDir.getAbsolutePath() );
		SubsValue subsval = new SubsValue(CodegenConstants.DATASTORE_CARTRIDGE_DIR, cartridgeDir.getAbsolutePath());
		subsval.setType(SubsValueType.NONE);
		DataStore.getInstance().addSubsValue(subsval);
	}

	private void initializeCartridgeClasspaths(
			Collection<CartridgeClasspathData> aCartridges) {
		ClassloaderHelper.getInstance().setupCurrentClassloader();

		for (CartridgeClasspathData data : getAllCartridgeClassPaths()) {
			TemplatesStore.getInstance().add(data.getFile());
		}

		findTemplateTrees(aCartridges);
	}
	
	private void findTemplateTrees(Collection<CartridgeClasspathData> aCartridges) {
		for (CartridgeClasspathData data : aCartridges) {
			try {
				findTemplatesTree(data.getFile()); // this will also do a: TemplatesStore.getInstance().add(...)
			} catch (InvocationTargetException e) {
				LogHelper.error("error setup templates: " + e.getCause(), e);
			} catch (Exception e) {
				LogHelper.error("error setup templates: " + e.getMessage(), e);
			}
		}
	}

	public TemplatesTree getTemplatesTree(String aFoldersName) {
		return TemplatesStore.getInstance().getTemplatesTree(aFoldersName);
	}

	/**
	 * Here the TemplateStore will be populated with all TemplatesTree's
	 * 
	 * @param aTemplatesDir
	 */
	private void findTemplatesTree(File aCartridgeCP) throws Exception {
		Class<?> registerTemplatesClz = findRegisterTemplatesClass(aCartridgeCP);

		Object obj = registerTemplatesClz.newInstance();
		TemplatesTree templateTree = new TemplatesTreeCloner().clone(obj);

		if (templateTree != null) {
			TemplatesStore.getInstance().add(aCartridgeCP, registerTemplatesClz, templateTree);
		}
	}

	/**
	 * this returns all classpath's to java project containging jet templates
	 * 
	 * @return
	 */
	private Map<String, CartridgeClasspathData> setupCartridgeClassPaths() {
		cartridgeClassPaths = new HashMap<String, CartridgeClasspathData>();

		File cartridgeDir = PluginHelper.getInstance().getCartridgeDir();

		List<File> jarFiles = FileHelper.findFiles(cartridgeDir.getPath(), new CartridgeJarFilter(), false);
		for (File file : jarFiles) {
			cartridgeClassPaths.put(CartridgeClasspathData.getClasspathKey(file), new CartridgeClasspathData(file, true));
		}

		List<File> linkFiles = FileHelper.findFiles(cartridgeDir.getPath(), new CartridgeLinkFilter(), false);
		for (File file : linkFiles) {
			String path = readLinkFile(file);
			if (path != null) {
				LogHelper.info("using cartridges: " + path + " via linkfile: " + file);
				cartridgeClassPaths.put(CartridgeClasspathData.getClasspathKey(new File(path)), new CartridgeClasspathData(new File(path), true));
			}
		}

		return cartridgeClassPaths;
	}

	
	private String readLinkFile(File file) {
		FileInputStream fis = null;
		try {
			Properties props = new Properties();
			fis = new FileInputStream(file);
			props.load(fis);
			fis.close();
			return props.getProperty("path");
		} catch (Exception e) {
			LogHelper.error("error in readLinkFile " + file, e);
			return null;
		}
	}

	public Collection<URL> getCartridgesClasspathUrls() {
		Collection<URL> result = new HashSet<URL>();

		for (CartridgeClasspathData data : getEnabledCartridgeClassPaths()) {
			try {
				result.add(data.getFile().toURI().toURL());
			} catch (MalformedURLException e) {
				LogHelper.error("error in getCartridgesClasspathUrls " + data.getFile(), e);
			}
		}

		return result;
	}

	public Collection<URL> getApilibsClasspathUrls() {
		Collection<URL> result = new HashSet<URL>();

		File apilib = PluginHelper.getInstance().getApilibDir();
		for (File jar : FileHelper.findFiles(apilib.getPath(), new JarFilter(), true)) {
			try {
				result.add(jar.toURI().toURL());
			} catch (MalformedURLException e) {
				LogHelper.error("error in getApilibsClasspathUrls " + jar, e);
			}
		}

		return result;
	}

	public void updateSelectedTemplate(ITemplate aTemplate, boolean aSelected) {
		if (aSelected) {
			if (!selectedTemplates.contains(aTemplate)) {
				selectedTemplates.add(aTemplate);
				return;
			}
		} else {
			if (selectedTemplates.contains(aTemplate)) {
				selectedTemplates.remove(aTemplate);
				return;
			}
		}
		logger.error("updateSelectedTemplate: this should not be possible " + aSelected + "  " + aTemplate);
	}

	public Set<ITemplate> getSelectedTemplates() {
		return selectedTemplates;
	}

	public ITemplate getTemplate(String aTemplateName) {
		for (TemplatesStoreData storeData : TemplatesStore.getInstance().getData().values()) {
			if (storeData != null && storeData.getTemplatesTree() != null && storeData.getTemplatesTree().getBranches() != null) {
				for (TemplatesBranch branch : storeData.getTemplatesTree().getBranches()) {
					for (ITemplate template : branch.getTemplates()) {
						if (aTemplateName.equals(template.getName())) {
							return template;
						}
					}
				}
			}
		}
		return null;
	}

	public IRegisterTemplates findIRegisterTemplatesFile(File aCartridgeCP) throws Exception {
		Class<?> clz = findRegisterTemplatesClass(aCartridgeCP);
		if (clz != null) {
			return (IRegisterTemplates) clz.newInstance();
		} else {
			String msg = "error getting unique class that implement IRegisterTemplates, under " + aCartridgeCP;
			msg += "\nYou should add a jarfile(s) under <eclipse>/dropins/easymda-1.0/cartridges";
			msg += "\nOr a xxx.link file(s) with a path pointing to a java project containg jet templates (a cartridge)";
			LogHelper.error(msg);
			throw new Exception(msg);		
		}
	}
	
	private Class<?> findRegisterTemplatesClass(File aCartridgeCP) throws Exception {
		Class<?> result = null;
		if (aCartridgeCP.getName().toLowerCase().endsWith(".jar")) {
			result = findRegisterClassViaJarfile(aCartridgeCP, IRegisterTemplates.class);
		} else {
			result = findRegisterClassViaBindir(aCartridgeCP, IRegisterTemplates.class);
		}

		if (result == null) {
			String msg = "error getting unique class that implement IRegisterTemplates, under " + aCartridgeCP;
			msg += "\nYou should add a jarfile(s) under <eclipse>/dropins/easymda-1.0/cartridges";
			msg += "\nOr a xxx.link file(s) with a path pointing to a java project containg jet templates (a cartridge)";
			LogHelper.error(msg);
			throw new Exception(msg);
		} else {
			return result;
		}
	}

	private Class<?> findRegisterClassViaBindir(File aCartridgeCP, Class<?> aFindClass) throws Exception {
		Class<?> result = null;
		List<File> files = FileHelper.findFiles(aCartridgeCP.getAbsolutePath(), new JavaFilter(), true);
		ClassLoader loader = ClassloaderHelper.getInstance().getClassLoader();
		for (File file : files) {
			try {
				String clsname = FileHelper.getFqn(file, aCartridgeCP.getAbsolutePath());
				Class<?> clz = loader.loadClass(clsname);
				if (tu.hasType(clz, aFindClass)) {
					result = clz;
					return result;
				}
			} catch (Throwable e) {
				LogHelper.error("error loading " + file + " " + e);
			}
		}
		return null;
	}

	private Class<?> findRegisterClassViaJarfile(File aCartridgeCP, Class<?> aFindClass) throws Exception {
		Class<?> result = null;
		ClassLoader loader = ClassloaderHelper.getInstance().getClassLoader();

		ZipFile zf = null;
		try {
			zf = new ZipFile(aCartridgeCP);
			Enumeration<?> e = zf.entries();
			while (e.hasMoreElements()) {
				ZipEntry ze = (ZipEntry) e.nextElement();
				if (ze.getName().endsWith(".class")) {
					String fqn = FileHelper.getFqn(new File(ze.getName()), null);
					Class<?> clz = loader.loadClass(fqn);
					if (tu.hasType(clz, aFindClass)) {
						result = clz;
						break;
					}
				}
			}
			return result;
		} catch(Throwable t) {
			logger.error("error at findRegisterClassViaJarfile "  + t);
			return null;
		} finally {
			if (zf != null) {zf.close();}
		}
	}

	class JavaFilter implements FileFilter {
		@Override
		public boolean accept(File pathname) {
			return (pathname.isDirectory() || pathname.getName().endsWith(".class"));
		}
	}

	class JarFilter implements FileFilter {
		@Override
		public boolean accept(File pathname) {
			return (pathname.isDirectory() || pathname.getName().toLowerCase().endsWith(".jar"));
		}
	}

	/**
	 * This return a collection of <%=xxx> that are used in all templates
	 * 
	 * @return
	 */
	public Collection<SubsValue> getSubsValues() {
		Collection<SubsValue> result = new HashSet<SubsValue>();

		if (TemplatesStore.getInstance().getData().values() != null) {
			for (TemplatesStoreData data : TemplatesStore.getInstance().getData().values()) {
				if (isSelectedCartridge(data)) {
					Collection<SubsValue> subsvals = data.getSubstitutes();
					if (subsvals != null) {
						DataStore.getInstance().mergeSubsValues(subsvals, false);
					}
				}
			}
		}

		return result;
	}

	private boolean isSelectedCartridge(TemplatesStoreData data) {
		for (CartridgeClasspathData cart : CartridgeHelper.getInstance().getEnabledCartridgeClassPaths()) {
			String path = CartridgeClasspathData.getClasspathKey(data.getTemplatesProjectDir());
			if (cart.getKey().equals(path)) {
				return true;
			}
		}
		return false;
	}
	//---------------------- filters ----------------------
	
	class CartridgeLinkFilter implements FileFilter {
		@Override
		public boolean accept(File pathname) {
			return pathname.getName().toLowerCase().endsWith(".link");
		}
	}

	class CartridgeJarFilter implements FileFilter {
		@Override
		public boolean accept(File pathname) {
			return pathname.getName().toLowerCase().endsWith(".jar");
		}
	}

}
