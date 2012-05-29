package ru.ecom.poly.web.action.medcard;

import java.util.ArrayList;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import ru.ecom.poly.ejb.services.IMedcardService;
import ru.ecom.web.util.Injection;
import ru.nuzmsh.web.struts.BaseAction;


/**
 * Поиск мед. карт. 
 */
public class MedcardSearchAction extends BaseAction {

     public ActionForward myExecute(ActionMapping aMapping, ActionForm aForm, HttpServletRequest aRequest, HttpServletResponse aResponse) throws Exception
     {
         MedcardSearchForm form = (MedcardSearchForm) aForm;
         IMedcardService service = Injection.find(aRequest).getService(IMedcardService.class);
         if (form.getNumber()!=null && !form.getNumber().equals("")) {
         aRequest.setAttribute("list", service.findMedCard(form.getNumber()));
         } else {
        	 aRequest.setAttribute("list", new ArrayList()) ;
         }
         return aMapping.findForward("success");
    }
}
