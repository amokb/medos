package ru.ecom.ejb.services.voc;

import org.jboss.annotation.security.SecurityDomain;
import ru.ecom.ejb.services.voc.helper.EntityVocService;
import ru.nuzmsh.util.PropertyUtil;
import ru.nuzmsh.util.voc.VocAdditional;
import ru.nuzmsh.util.voc.VocValue;

import javax.annotation.EJB;
import javax.annotation.Resource;
import javax.ejb.Remote;
import javax.ejb.SessionContext;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceUnit;
import java.lang.reflect.Method;

@Stateless
@Remote(IVocEditService.class)
@SecurityDomain("other")
public class VocEditServiceBean implements IVocEditService {

	public Object createVocValue(String aVocKey, VocValue aVocValue, VocAdditional aAdditional) {
		IVocContextService vocService = infoService.getVocService(aVocKey) ;
		if(vocService==null) throw new IllegalArgumentException("Нет справочника "+aVocKey) ;
		if(vocService instanceof EntityVocService) {
			EntityVocService entityVocService = (EntityVocService) vocService ;
			String name = entityVocService.getNames()[0] ;
			return  createEntity(entityVocService.getEntityClass(), name, aVocValue.getName()) ;
		}
		throw new IllegalStateException("Справочник "+aVocKey+" не поддерживает функцию добавления значений") ;
	}
	
	/**
	 * 
	 * @param aEntitClass
	 * @param aNameField
	 * @param aValue
	 * @return идентификатор нового объекта
	 * @throws Exception
	 */
	private Object createEntity(Class aEntitClass, String aNameField, String aValue) {
		try {
			Object obj = aEntitClass.newInstance() ;
			setProperty(obj, aNameField, aValue) ;
			EntityManager manager = factory.createEntityManager() ;
			manager.persist(obj) ;
			manager.close() ;
			return PropertyUtil.getPropertyValue(obj, "id") ;
		} catch (Exception e) {
			throw new IllegalStateException("Ошибка создания нового "+aEntitClass.getName()
					+" [property="+aNameField+", value="+aValue+"]: "+e.getMessage(), e) ;
		}
	}
	
	private void setProperty(Object aObject, String aProperty, String aValue) throws Exception {
		if(aObject==null) throw new NullPointerException("Нет объекта aObject, aObject==null") ;
		Class clazz = aObject.getClass() ;
		Method getterMethod = PropertyUtil.getGetterMethod(clazz, aProperty) ;
		Method setterMethod = PropertyUtil.getSetterMethod(clazz, getterMethod) ;
		setterMethod.invoke(aObject, aValue) ;
	}

	public boolean isVocEditabled(String aVocKey) {
		String sb = "/Policy/Voc/" + aVocKey + "/Create";
		return context.isCallerInRole(sb) ;
	}

	private @EJB IVocInfoService infoService ;
	private @Resource SessionContext context;
	private @PersistenceUnit EntityManagerFactory factory;
	
}   


