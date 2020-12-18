package ru.ecom.diary.web.action.protocol;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import ru.ecom.diary.ejb.service.protocol.IDiaryService;
import ru.ecom.web.util.Injection;
import ru.nuzmsh.web.struts.BaseAction;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Created by IntelliJ IDEA.
 * User: STkacheva
 * Date: 18.12.2006
 * Time: 15:40:51
 * To change this template use File | Settings | File Templates.
 */
public class ProtocolSearchRedirectAction extends BaseAction {
    public ActionForward myExecute(ActionMapping aMapping, ActionForm aForm, HttpServletRequest aRequest, HttpServletResponse aResponse) throws Exception {
        IDiaryService service = Injection.find(aRequest).getService(IDiaryService.class) ;
        aRequest.setAttribute("list",service.findProtocol(1));
        return aMapping.findForward(SUCCESS);
    }
}
