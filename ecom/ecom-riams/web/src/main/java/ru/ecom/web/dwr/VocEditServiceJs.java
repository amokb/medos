package ru.ecom.web.dwr;

import ru.ecom.ejb.services.voc.IVocEditService;
import ru.ecom.web.util.Injection;
import ru.nuzmsh.util.voc.VocAdditional;
import ru.nuzmsh.util.voc.VocValue;

import javax.ejb.CreateException;
import javax.naming.NamingException;
import javax.servlet.http.HttpServletRequest;
import java.rmi.RemoteException;

public class VocEditServiceJs {
    
	public String createVocValue(HttpServletRequest aRequest, String aVocKey
            , String aId, String aName, String aParentId) throws NamingException {
    	VocValue value = new VocValue(aId, aName) ;
    	VocAdditional add = new VocAdditional(aParentId) ; 
    	Object id =  Injection.find(aRequest).getService(IVocEditService.class)
    		.createVocValue(aVocKey, value, add) ;
    	return id!=null ? id.toString() : null ;
    }

}
