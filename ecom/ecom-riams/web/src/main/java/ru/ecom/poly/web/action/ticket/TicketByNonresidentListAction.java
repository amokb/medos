package ru.ecom.poly.web.action.ticket;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import ru.ecom.mis.ejb.service.patient.HospitalLibrary;
import ru.ecom.web.util.ActionUtil;
import ru.nuzmsh.web.struts.BaseAction;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class TicketByNonresidentListAction   extends BaseAction {

	@Override
	public ActionForward myExecute(ActionMapping aMapping, ActionForm aForm, HttpServletRequest aRequest, HttpServletResponse aResponse) throws Exception {
		String typePat =ActionUtil.updateParameter("TicketByNonresident","typePatient","3", aRequest) ;
		ActionUtil.updateParameter("TicketByNonresident","period","2", aRequest) ;
		switch (typePat) {
			case "2":
				aRequest.setAttribute("add", HospitalLibrary.getSqlForPatient(true, true, "t.date", "p", "pvss", "pmp", "ok"));
				aRequest.setAttribute("infoTypePat", " (по иногородним)");
				break;
			case "1":
				aRequest.setAttribute("add", HospitalLibrary.getSqlForPatient(true, false, "t.date", "p", "pvss", "pmp", "ok"));
				aRequest.setAttribute("infoTypePat", " (по региональным)");
				break;
			case "3":
				aRequest.setAttribute("add", HospitalLibrary.getSqlGringo(true, "ok"));
				aRequest.setAttribute("infoTypePat", "Поиск по иностранцам");
				break;
			default:
				aRequest.setAttribute("add", "");
				aRequest.setAttribute("infoTypePat", " (по всем)");
				break;
		}
		String id = aRequest.getParameter("id" );
		if (id!=null) {
			int ind1 = id.indexOf(":");
			String date =id.substring(0,ind1) ;
			aRequest.setAttribute("date", date) ;
		} else {
			aRequest.setAttribute("date", "") ;
		}
		
		return aMapping.findForward(SUCCESS);
	}
	
	
}
