package ru.ecom.poly.web.action.ticket;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import ru.nuzmsh.web.struts.BaseAction;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class TicketDoubleDataAction extends BaseAction {

    @Override
    public ActionForward myExecute(ActionMapping aMapping, ActionForm aForm, HttpServletRequest aRequest, HttpServletResponse aResponse) throws Exception {
        String idString = aRequest.getParameter("id");
        int ind1 = idString.indexOf(":");
        int ind2 = idString.indexOf(":", ind1 + 1);
        String medcard = idString.substring(0, ind1);
        String date = idString.substring(ind1 + 1, ind2);
        String workfunction = idString.substring(ind2 + 1);
        aRequest.setAttribute("date", date);
        aRequest.setAttribute("workfunction", workfunction);
        aRequest.setAttribute("medcard", medcard);
        return aMapping.findForward(SUCCESS);
    }

}