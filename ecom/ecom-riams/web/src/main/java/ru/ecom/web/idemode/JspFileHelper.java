package ru.ecom.web.idemode;

import org.apache.log4j.Logger;
import org.jdom.*;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import ru.ecom.web.idemode.tagext.TagLibraryManager;
import ru.nuzmsh.util.StringUtil;

import javax.servlet.jsp.tagext.TagAttributeInfo;
import javax.servlet.jsp.tagext.TagInfo;
import java.io.*;
import java.util.*;

public class JspFileHelper {

	private static final String TILES_INSERT = "<tiles:insert page        = '/WEB-INF/tiles/mainLayout.jsp' flush       = 'true'"
			+"  xmlns:tiles = 'http://struts.apache.org/tags-tiles'"
			+"  xmlns:msh   = 'http://www.nuzmsh.ru/tags/msh'"
			+"  xmlns:ecom  = 'http://www.ecom-ast.ru/tags/ecom'"
			+"  xmlns:tags  = 'http://www.ecom-ast.ru/tags/riams/tags'"
			+"  >" ;
	private static final Logger LOG = Logger.getLogger(JspFileHelper.class);

	/** Рабочий каталог */
	public File getWorkspaceDir() {
		return theWorkspaceDir;
	}

	public void setWorkspaceDir(File aWorkspaceDir) {
		theWorkspaceDir = aWorkspaceDir;
	}

	private List<File> findTargetJspFiles(String aJspPath) {
		ArrayList<File> ret = new ArrayList<>(10);
		File WORKSPACE_DIR = theWorkspaceDir; 
		File[] dirs = WORKSPACE_DIR.listFiles() ;
		for(File dir : dirs) {
			if(dir.isDirectory()) {
				File jspFile = new File(dir, "web/target/webapp"+aJspPath) ;
				if(jspFile.exists()) {
					ret.add(jspFile);
				}
			}
		}
		return ret ;
	}
	
	private void copyFile(File aSource, File aDest) throws IOException {
		try (FileInputStream in = new FileInputStream(aSource) ;
			 FileOutputStream out = new FileOutputStream(aDest)){
				byte[] buf = new byte[4048];
				int result ;
				while ( (result=in.read(buf))>0) {
					out.write(buf, 0, result);
				}
		}
	}
	
	
	private File findJspFile(String aJspPath) {
		File WORKSPACE_DIR = theWorkspaceDir; 
		File[] dirs = WORKSPACE_DIR.listFiles() ;
		File ret = null ;
		for(File dir : dirs) {
			if(dir.isDirectory()) {
				File jspFile = new File(dir, "web/src/webapp"+aJspPath) ;
				if(jspFile.exists()) {
					if(ret!=null) {
						throw new IllegalStateException("Jsp-файл "+aJspPath
								+" существует в двух каталогах "
								+ ret.getAbsolutePath()
								+" и в "+jspFile.getAbsolutePath()) ;
					}
					ret = jspFile ;
				}
			}
		}
		if(ret==null) {
			throw new IllegalStateException("Не найден JSP-файл "+aJspPath+ 
					" в каталоге "+WORKSPACE_DIR) ;
		}
		return ret ;
		
	}
	
	private void findElementsByGuid(Element aElement, String aGuid, List<Element> aList) {
		if(aGuid.equals(aElement.getAttributeValue("guid"))) {
			aList.add(aElement);
		}
		for(Object obj : aElement.getChildren()) {
			Element elm = (Element) obj ;
			findElementsByGuid(elm, aGuid, aList);
		}
	}
	
	private String createCleanXml(String aJspPath) throws IOException {
		FileInputStream in = new FileInputStream(findJspFile(aJspPath)) ;
		try (LineNumberReader lin = new LineNumberReader(new InputStreamReader(in, "utf-8"))){
			
			StringBuilder sb = new StringBuilder(8000) ;
			sb.append("<?xml version='1.0' encoding='utf-8'?>") ;
			sb.append("<!DOCTYPE mydtd [<!ENTITY nbsp ' '>]>") ;

			String line ;
			while ( (line=lin.readLine())!=null) {
				String trimmedLine = line.trim();
				if(trimmedLine.startsWith("<tiles:insert")) {
					line = TILES_INSERT ;
				}
				if(!trimmedLine.startsWith("<%@")) {
					sb.append(line);
					sb.append('\n');
				}
			}
			return sb.toString() ;
		}
	}

	private Element getRootElement(String aJspPath) throws JDOMException, IOException {
		Document doc = new SAXBuilder().build(
				new StringReader(createCleanXml(aJspPath))
		) ;
		return doc.getRootElement() ;
	}
	
	private Element findSingleElementByGuid(String aJspPath, String aGuid) throws JDOMException, IOException {
		Element root = getRootElement(aJspPath);
		ArrayList<Element> list = new ArrayList<>();
		findElementsByGuid(root, aGuid, list);
		if(list.isEmpty()) {
			throw new IllegalStateException("Не найден тэг с guid "+aGuid+" в файле "+aJspPath) ;
		} else if(list.size()>1) {
			throw new IllegalStateException("Найдено "+list.size()+" элементов с guid "+aGuid+" в файле "+aJspPath) ;
		} else {
			return list.get(0);
		}		
	}
	/**
	 * __tag __prefix
	 * @param aJspPath
	 * @param aGuid
	 * @return
	 * @throws Exception
	 */
	public Map<String,String> getAttributeValues(String aJspPath, String aGuid) throws Exception {
			Element elm = findSingleElementByGuid(aJspPath, aGuid);
			HashMap<String,String> map = new HashMap<>();
			// FIXME __TAG & __PREFIX
			map.put("__tag", elm.getName());
			map.put("__prefix", elm.getNamespacePrefix());
			for(Object obj : elm.getAttributes()) {
				Attribute attr = (Attribute) obj ;
				map.put(attr.getName(), elm.getAttributeValue(attr.getName()));
			}
			return map ;
	}
	
	private String getHeaders(String aJspPath) throws IOException {
		StringBuilder sb = new StringBuilder(8000) ;
		try (LineNumberReader in = new LineNumberReader(
				new InputStreamReader(new FileInputStream(findJspFile(aJspPath))
						, "utf-8"))) {
			String line;
			while ((line = in.readLine()) != null) {
				sb.append(line);
				sb.append('\n');
				if (line.trim().startsWith("<tiles:insert")) {
					break;
				}
			}
			return sb.toString();
		}
	}
	private static class BooleanHolder {
		boolean value = false ;
	}
	private void addGuid(Element aElement, TagLibraryManager aManager, BooleanHolder aBooleanHolder) {
		if(aElement==null) return ;
		String prefix = aElement.getNamespacePrefix() ;
		
		if(aElement.getAttribute("guid")==null 
				&& !"tiles".equals(prefix) 
				&& !StringUtil.isNullOrEmpty(prefix)) {
			String tagName = aElement.getName() ;
			
			if(aManager.isGuidSupported(prefix, tagName)) {
				String uuid = createGuid() ;
				aElement.setAttribute("guid", uuid);
				aBooleanHolder.value = true ;
			}
		}
		for(Object obj: aElement.getChildren()) {
			addGuid((Element)obj, aManager, aBooleanHolder);
		}
	}
	
	private static String createGuid() {
		return UUID.randomUUID().toString() ;
	}

	public void deleteTag(String aJspPath, String aGuid) throws Exception {
		Element tag = findSingleElementByGuid(aJspPath, aGuid);
		Document doc = tag.getDocument() ; 
		tag.getParentElement().getChildren().remove(tag);
		saveJsp(doc, aJspPath);
	}	
	
	public void setAttributes(String aJspPath, String aGuid, Map<String,String> aValues, TagLibraryManager aManager) throws Exception {
		Element tag = findSingleElementByGuid(aJspPath, aGuid);
		BooleanHolder booleanHolder = new BooleanHolder() ;
		booleanHolder.value = false ;
		addGuid(tag.getDocument().getRootElement(), aManager, booleanHolder);
		Map<String,String> oldValues = getAttributeValues(aJspPath, aGuid);
		
		System.out.println("newValues = "+aValues);
		System.out.println("oldValues = "+oldValues);
		
        // удаляем атрубуты, которых нет в TagInfo
        TagInfo tagInfo = aManager.getTagInfo(tag.getNamespacePrefix(), tag.getName());
        Map<String,String> values = new HashMap<>() ;
        for(TagAttributeInfo attr:tagInfo.getAttributes()) {
        	String value = aValues.get(attr.getName());
        	if(attr.getTypeName().equals("boolean") 
        			&& ("on".equals(value) 
        					|| "off".equals(value)
        					|| StringUtil.isNullOrEmpty(value)
        					)) {
        		value = "on".equals(value) ? "true" : "false";
        	}
        	if(attr.getName().equals("guid") && StringUtil.isNullOrEmpty(value)) {
        		value = aGuid;
        	}
        	//if(!StringUtil.isNullOrEmpty(value)) {
        	values.put(attr.getName(), value) ;
        	//}
        	
        }
		System.out.println("values = "+values);
        
		// удаляем равные старым значениям и пустые
		Set<String> keys = new TreeSet<>();
		keys.addAll(values.keySet());
		Map<String,String> defaultValues = aManager.getDefaultValues(tagInfo);
		System.out.println("defaultValues = "+defaultValues);
		
		for(String key : keys) {
			String oldValue = oldValues.get(key);
			String newValue = values.get(key);
			if(oldValue!=null && oldValue.equals(newValue)) {
				values.remove(key);
//			} else {
//				String defaultValue = defaultValues.get(key);
//				if(StringUtil.isNullOrEmpty(newValue) && StringUtil.isNullOrEmpty(defaultValue)) {
//					values.remove(key);
//				} else if(newValue!=null && newValue.equals(defaultValue)){
//					values.remove(key);
//				}
			}
			//if(StringUtil.isNullOrEmpty(newValue)) {
			//	values.remove(key);
			//}
		}
		if(values.isEmpty() && !booleanHolder.value) { // нечего сохранять
			return ; 
		}
		
		// устанавливаем атрибуты в элементе
		for(Map.Entry<String, String> entry: values.entrySet()) {
			String newValue = entry.getValue();
			String key = entry.getKey();
			String defaultValue = defaultValues.get(key);
			if(StringUtil.isNullOrEmpty(newValue) && StringUtil.isNullOrEmpty(defaultValue)) {
				LOG.info("   remove "+key);
				tag.removeAttribute(key); 
			} else if(newValue!=null && newValue.equals(defaultValue)){
				tag.removeAttribute(key);
				LOG.info("   remove "+key);
			} else if(newValue!=null ) {
				tag.setAttribute(key, newValue);
				LOG.info("  SET  "+key);
			}
		}
		
		// убираем пустые атрибуты
		Iterator<Attribute> it = tag.getAttributes().iterator();
		while(it.hasNext()) {
			Attribute attr = it.next() ;
			if(StringUtil.isNullOrEmpty(attr.getValue())) {
				it.remove();
			}
		}
		
		saveJsp(tag.getDocument(), aJspPath);
		
	}
	

	private void saveJsp(Document aDocument, String aJspPath) throws IOException {
        File jspFile = findJspFile(aJspPath);
		// создаем xml
		File tmpFile = File.createTempFile("jspxhtml", ".xml");
		try (FileOutputStream tmpOut = new FileOutputStream(tmpFile)) {
	        XMLOutputter xmlOut = new XMLOutputter(Format.getPrettyFormat());
	        try (OutputStreamWriter fileOut = new OutputStreamWriter(tmpOut, "utf-8")){
	            xmlOut.output(aDocument, fileOut);
	        }
	        
	        // пишем JSP файл
        	String headers = getHeaders(aJspPath);
			try (PrintWriter out = new PrintWriter(new OutputStreamWriter(new FileOutputStream(jspFile), "utf-8"));
				 LineNumberReader in = new LineNumberReader(
					new InputStreamReader(new FileInputStream(tmpFile)
							, "utf-8"))) {
				out.println(headers);
				String line;
				boolean canWrite = false;
				while ((line = in.readLine()) != null) {
					if (canWrite) {
						out.println(line);
					}
					if (line.trim().startsWith("<tiles:insert")) {
						canWrite = true;
					}
				}

			}
		} finally {
	        tmpFile.delete() ;
		}
		
		// копируем в targets
		List<File> targets = findTargetJspFiles(aJspPath);
		for(File dest : targets) {
			copyFile(jspFile, dest);
		}
		
	}
	public enum InsertOption {AFTER, BEFORE, INTO, OVER}

	
	public void addGuids(String aJspPath, TagLibraryManager aManager) throws Exception {
		Element root = getRootElement(aJspPath);
		addGuid(root, aManager, new BooleanHolder());
		saveJsp(root.getDocument(), aJspPath);
	}
	
	public void insertNewTag(
			InsertOption aInsertOption
			, String aTagLibPrefix
			, TagInfo aTagInfo
			, Map<String,String> aValues
			, String aJspPath
			, String aGuid
		) throws Exception {
		
		Element targetTag = findSingleElementByGuid(aJspPath, aGuid);
		Namespace ns = targetTag.getDocument().getRootElement().getNamespace(aTagLibPrefix);
		Element newTag = createTag(aTagInfo, ns, aValues);

		Element parent = (Element) targetTag.getParent() ;
		int index = parent.getChildren().indexOf(targetTag);
		
		switch(aInsertOption) {
			case AFTER:
				parent.getChildren().add(index+1, newTag) ;
				break ;
			case BEFORE:
				parent.getChildren().add(index, newTag) ;
				break ;
			case INTO:
				targetTag.getChildren().add(newTag) ;
				break ;
			case OVER:
				
			//	int oldPosition = parent.getChildren().indexOf(targetTag);
				targetTag.detach() ;
				newTag.addContent(targetTag);
				if(index>=0) {
					parent.getChildren().add(index, newTag) ;
				} else {
					parent.getChildren().add(newTag) ; // fixme скорее всего лишнее
				}
				break ;
			default: 
				throw new RuntimeException("Пока не поддерживается") ;
		}
		saveJsp(targetTag.getDocument(), aJspPath);
	}
	
	private Element createTag(TagInfo aTagInfo, Namespace aNamespace, Map<String, String> aValues) {
		Element elm = new Element(aTagInfo.getTagName(), aNamespace) ;
		for(TagAttributeInfo attr : aTagInfo.getAttributes()) {
			String key  = attr.getName() ;
			String value = aValues.get(key);
			if(!StringUtil.isNullOrEmpty(value)) {
				elm.setAttribute(key, value);
			}
			if("guid".equals(key)) {
				elm.setAttribute(key, createGuid());
			}
		}
		return elm ;
	}
	
	public void addFromTemplate(String aNewFormName) throws Exception {
		// check not empty
		if(StringUtil.isNullOrEmpty(aNewFormName)) 
			throw new IllegalArgumentException("Нет названия формы") ;
		
		// check for last Form
		if(aNewFormName.endsWith("Form")) 
			throw new IllegalArgumentException("Название формы не должно оканчиваться на 'Form'") ;
		
		String jspEdit = "/WEB-INF/actions/"+aNewFormName+"/edit.jsp" ;
		String jspList = "/WEB-INF/actions/"+aNewFormName+"/list.jsp" ;

		try {
			findJspFile(jspEdit);
			throw new IllegalArgumentException("Файл "+jspEdit+" уже существует") ;
		} catch (IllegalStateException e) {
			try {
				findJspFile(jspList);
				throw new IllegalArgumentException("Файл "+jspList+" уже существует") ;
			} catch (IllegalStateException e2) {
				// OK
			}
		}

		// checks in struts config (FIXME)
		
		// add form to config-hello.xml
		addNewForm(aNewFormName);
		
		// copy ecom_hello/edit.jsp and ecom_hello/list.jsp to aNewFormName
		File dir = new File(theWorkspaceDir, "ecom-riams/web/src/webapp/WEB-INF/actions/"+aNewFormName) ;
		File editSourceFile = new File(dir, "edit.jsp") ;
		File listSourceFile = new File(dir, "list.jsp") ;
		
		// copy to source with replace
		dir.mkdir();
		copyWithReplace(findJspFile("/WEB-INF/actions/ecom_hello/edit.jsp")
				, editSourceFile, "ecom_hello", aNewFormName);
		copyWithReplace(findJspFile("/WEB-INF/actions/ecom_hello/list.jsp")
				, listSourceFile, "ecom_hello", aNewFormName);
		
		// copy to target
		File destDir = new File(theWorkspaceDir, "ecom-riams/web/target/webapp/WEB-INF/actions/"+aNewFormName);
		destDir.mkdir();
		copyFile(editSourceFile, new File(destDir, "edit.jsp")) ;
		copyFile(listSourceFile, new File(destDir, "list.jsp")) ;
		
		// touch ecom-web.jar
		new File(theWorkspaceDir, "ecom-riams/web/target/webapp/WEB-INF/lib/ecom-web.jar").setLastModified(System.currentTimeMillis());
	}
	
	private void copyWithReplace(File aSource, File aDest, String aFrom, String aTo) throws Exception {
		try (LineNumberReader in = new LineNumberReader(new InputStreamReader(new FileInputStream(aSource), "utf-8"));
			 PrintWriter out = new PrintWriter(new OutputStreamWriter(new FileOutputStream(aDest), "utf-8"))){
				String line ;
				while ( (line=in.readLine())!=null) {
					line = line.replace(aFrom, aTo);
					out.println(line);
				}
		}
	}
	
	private void addNewForm(String aFormName) throws Exception {
		File configHelloForm = new File(theWorkspaceDir, "ecom-riams/web/src/webapp/WEB-INF/config-hello.xml") ;
		File configHelloFormTarget = new File(theWorkspaceDir, "ecom-riams/web/target/webapp/WEB-INF/config-hello.xml") ;
		final String formClass = "ru.ecom.ejb.form.hello.HelloForm" ;

		try ( InputStreamReader in = new InputStreamReader(new FileInputStream(configHelloForm), "utf-8") ;
			  OutputStreamWriter out = new OutputStreamWriter(new FileOutputStream(configHelloForm), "utf-8")) {
			Document doc = new SAXBuilder().build(in) ;
			Element root = doc.getRootElement() ;
			Element formBeansElement = root.getChild("form-beans");
			Element elm = new Element("form-bean") ;
			elm.setAttribute("name", aFormName+"Form");
			elm.setAttribute("type", formClass);
			formBeansElement.addContent(elm);
			
			// saving...
	        XMLOutputter xmlOut = new XMLOutputter(Format.getPrettyFormat());

	        xmlOut.output(doc, out);
	        // copy to target
	        copyFile(configHelloForm, configHelloFormTarget);
		}
	}

	public static void main(String[] args) throws Exception {
		TagLibraryManager manager = new TagLibraryManager() ;
		manager.addTld(ru.nuzmsh.web.tags.AbstractFieldTag.class, "msh");

		JspFileHelper h = new JspFileHelper() ;
		
		System.out.println(h.getAttributeValues("/WEB-INF/actions/mis_lpuArea/edit.jsp"
				, "number123"));
		Map<String,String> values = h.getAttributeValues("/WEB-INF/actions/mis_lpuArea/edit.jsp"
				, "number123") ;
		values.put("label", "Номер участка3");
		values.put("size", "");
		h.setAttributes("/WEB-INF/actions/mis_lpuArea/edit.jsp"
				, "number123"
				, values
				, manager);
	}
	
	/** Рабочий каталог */
	private File theWorkspaceDir = new File("/home/esinev/workspace/ecom") ;
}