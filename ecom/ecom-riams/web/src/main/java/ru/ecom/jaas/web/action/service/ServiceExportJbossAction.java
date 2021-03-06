package ru.ecom.jaas.web.action.service;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import ru.ecom.jaas.ejb.service.ISecUserService;
import ru.ecom.web.util.Injection;
import ru.nuzmsh.web.messages.InfoMessage;
import ru.nuzmsh.web.struts.BaseAction;

import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.MalformedObjectNameException;
import javax.management.ReflectionException;
import javax.naming.NamingException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 */
public class ServiceExportJbossAction extends BaseAction {
    public ActionForward myExecute(ActionMapping aMapping, ActionForm aForm, HttpServletRequest aRequest, HttpServletResponse aResponse) throws Exception {
        export(aRequest);
        new InfoMessage(aRequest, "Экспорт завершен") ;
        return new ActionForward(aMapping.findForward(SUCCESS)) ;
    }


    public static void export(HttpServletRequest aRequest) throws IOException, ReflectionException, InstanceNotFoundException, MBeanException, MalformedObjectNameException, NamingException {
        ISecUserService service = (ISecUserService) Injection.find(aRequest).getService("SecUserService") ;
        service.exportRolesProperties();
        service.exportUsersProperties();
        service.fhushJboss();
    }

}
