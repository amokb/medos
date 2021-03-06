package ru.ecom.web.poly.actions.table.info;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import ru.ecom.ejb.services.query.IWebQueryService;
import ru.ecom.ejb.services.query.WebQueryResult;
import ru.ecom.web.util.Injection;
import ru.nuzmsh.web.struts.BaseAction;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Collection;

public class ListDepartmentAction extends BaseAction {
    public ActionForward myExecute(ActionMapping aMapping, ActionForm aForm, HttpServletRequest aRequest, HttpServletResponse aResponse) throws Exception {
    	
		IWebQueryService service = Injection.find(aRequest).getService(IWebQueryService.class) ;
		boolean remoteUser = true ;
		boolean aManyIs = false ;
		//String addParam=PreRecordAction.saveData(aRequest) ;
		String addParam="";
		StringBuilder sql = new StringBuilder() ;
		String vocworkFunction = aRequest.getParameter("vocWorkFunction") ;
		String infomat = aRequest.getParameter("infomat") ;
		String additionUrl ="" ;
		if (infomat!=null) additionUrl = "&infomat="+infomat ;
		StringBuilder addVWF = new StringBuilder() ;
		if (vocworkFunction!=null) {
			addVWF.append(" and vwf.id='").append(vocworkFunction).append("' ") ;
		}
		System.out.println(addVWF) ;
		sql.append("select coalesce(m1.id,m2.id)||'&department='||coalesce(m1.id,m2.id)||'").append(addParam).append("' mlid,coalesce(m1.name,m2.name) as mlname,coalesce(m1.id,m2.id) as mlonlyid") ;
		sql.append(" from VocWorkFunction vwf ") ;
		sql.append(" left join WorkFunction wf on vwf.id=wf.workFunction_id") ; 
		sql.append(" left join MisLpu m1 on m1.id=wf.lpu_id") ; 
		sql.append(" left join Worker w on w.id=wf.worker_id") ; 
		sql.append(" left join MisLpu m2 on m2.id=w.lpu_id") ; 
		sql.append(" left join WorkCalendar wc on wc.workFunction_id=wf.id") ;
		sql.append(" left join WorkCalendarDay wcd on wcd.workCalendar_id=wc.id") ;
 
		sql.append(" where wcd.calendarDate>=Current_date");
		if (remoteUser) sql.append(" and (wf.DTYPE='PersonalWorkFunction' and (m2.isNoViewRemoteUser is null or m2.isNoViewRemoteUser='0') or wf.dtype='GroupWorkFunction' and (m1.isNoViewRemoteUser is null or m1.isNoViewRemoteUser='0')) and (wf.isNoViewRemoteUser is null or wf.isNoViewRemoteUser='0')") ;
		sql.append(addVWF) ;
		sql.append(" group by coalesce(m1.id,m2.id),coalesce(m1.name,m2.name)") ;
		sql.append(" order by coalesce(m1.name,m2.name)") ;
		
		StringBuilder res = new StringBuilder() ;
		Collection<WebQueryResult> list = service.executeNativeSql(sql.toString(),50);
		
		 res.append("<table>" ) ;
		//res.append("<form name='frmFunctions' id='frmFunctions' action='javascript:step3()'><ul id='listFunctions'>") ;
		//res.append("<li class='title'>Специалисты</li>");
		for (WebQueryResult wqr:list) {
			// Спискок отделений
			res.append("<tr>") ;
			res.append("<th align='center'>").append(wqr.get2()).append("</th>") ;
			res.append("</tr>") ;
			res.append("<tr><td><table border='1px solid'>") ;
			res.append("<tr><th>ФИО врача</th><th>Кабинет</th><th>Информация о ближайщем времени для записи</th></tr>") ;
			String sql1 = "select wf.dtype,wf.id,coalesce(vad.code||' ','')||coalesce(vc.code||' ','')" +
					" ||vwf.name" +
					" ,case when wf.dtype='GroupWorkFunction' then wf.groupName" +
					" 	when wf.dtype='PersonalWorkFunction' then wp.lastname||' '||wp.firstname||' '||wp.middlename" +
					" 	else '' end" +
					" ,list('№'||room.name||'<br/> Дни и время приема: '||room.comment)" +
					" ,coalesce(vad.code||' ','')||coalesce(vc.code||' ','') as dp " +
					" ,to_char(min(case when wct.medCase_id is null and wct.prepatient_id is null and (wct.prepatientinfo is null or wct.prepatientinfo='') then wcd.calendarDate else null end),'dd.mm.yyyy') as datmin" +
					", vwf.id as vwfid,replace(replace(upper(vwf.name),'ВРАЧ-',''),'ВРАЧ ','') as vwfname" +
					" from WorkFunction wf" +
					" left join Worker w on w.id=wf.worker_id" +
					" left join Patient wp on wp.id=w.person_id" +
					" left join WorkCalendar wc on wc.workFunction_id=wf.id" +
					" left join WorkCalendarDay wcd on wcd.workCalendar_id=wc.id" +
					" left join WorkCalendarTime wct on wct.workCalendarDay_id=wcd.id" +
					" left join VocWorkFunction vwf on vwf.id=wf.workFunction_id" +
					" left join VocCategory vc on vc.id=category_id" +
					" left join VocAcademicDegree vad on vad.id=wf.degrees_id" +
					" left join MisLpu mlG on mlG.id=wf.lpu_id" +
					" left join MisLpu mlP on mlP.id=w.lpu_id" +
					" left join WorkPlace_WorkFunction wpf on wf.id=wpf.workFunctions_id" +
					" left join WorkPlace room on wpf.workPlace_id=room.id	" +
					" left join WorkPlace fb on fb.id=room.parent_id" +
					" left join MisLpu m1 on m1.id=wf.lpu_id " +
					" left join MisLpu m2 on m2.id=w.lpu_id " +
					" where" +
					" wc.id is not null and (wf.archival is null or wf.archival='0')" +
					addVWF +
					" and wcd.calendardate >=current_date" +
					" and " +
					" case when wf.dtype='GroupWorkFunction' then m1.id" +
					" when wf.dtype='PersonalWorkFunction' then m2.id" +
					" else 0 end = '" + wqr.get3() + "'" +
					" group by wf.dtype,wf.id,vwf.id,vwf.name,vad.code,vc.code,wf.groupName,wp.lastname,wp.firstname,wp.middlename,mlP.name,mlG.name" +
					" order by replace(replace(upper(vwf.name),'ВРАЧ-',''),'ВРАЧ ','')";
			Collection<WebQueryResult> listSpec = service.executeNativeSql(sql1) ;
			for (WebQueryResult wqrS : listSpec) {
				res.append("<tr>") ;
				//res.append("<td>").append(wqrS.get3()).append("</td>") ;
				res.append("<td><b><a href=\"step_table_2.do?vocWorkFunction=")
						.append(wqrS.get8()).append(additionUrl)
						.append("\">").append(wqrS.get9()).append(" </a></b><br/>")
					.append(wqrS.get4()).append(" <br/>")
					.append(wqrS.get6()).append("</td>") ;
				res.append("<td>").append(wqrS.get5()).append("</td>") ;
				if (wqrS.get7()!=null) {
					res.append("<td>").append(wqrS.get7());
					res.append(" <a href=\"step_table_2.do?vocWorkFunction=")
						.append(wqrS.get8())
						.append(additionUrl).append("&workFunction=").append(wqrS.get2());
					res.append("\")> Отобразить</a>") ; 
					res.append("</td>") ;
				} else {
					res.append("<td>").append("нет времен для записи").append("</td>") ;
				}
				res.append("</tr>") ;
			}
			res.append("</table></td></tr>") ;
			
		}
		//res.append("</ul></form>") ;
		res.append("</table>") ;
		aRequest.setAttribute("dep listFunctions", list) ;
		aRequest.setAttribute("tableSpec", res.toString()) ;
        return aMapping.findForward("success") ;

    }
}