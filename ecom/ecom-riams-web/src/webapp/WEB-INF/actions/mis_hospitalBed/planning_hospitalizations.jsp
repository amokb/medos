<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://struts.apache.org/tags-tiles" prefix="tiles" %>
<%@ taglib uri="http://www.nuzmsh.ru/tags/msh" prefix="msh" %>
<%@ taglib uri="http://www.ecom-ast.ru/tags/ecom" prefix="ecom" %>
<%@ taglib uri="/WEB-INF/mis.tld" prefix="mis" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="tags" %>

<tiles:insert page="/WEB-INF/tiles/mainLayout.jsp" flush="true">

  <tiles:put name="body" type="string">
    <msh:form action="/stac_planning_hospitalizations.do" defaultField="dateBegin" disableFormDataConfirm="true" method="GET" guid="d7b31bc2-38f0-42cc-8d6d-19395273168f">
    <msh:panel>
    	<msh:row>
    		<msh:autoComplete property="department" vocName="lpu" fieldColSpan="3" horizontalFill="true"/>
    	</msh:row>
    	<msh:row>
    		<msh:autoComplete property="roomType" vocName="vocRoomType" label="Уровень палаты"/>
    		<msh:autoComplete property="countBed" vocName="vocCountBedInHospitalRoom" label="Вместимость"/>
    	</msh:row>
    	<msh:row>
    		<msh:separator label="ПЕРИОД" colSpan="4"/>
    	</msh:row>
    	<msh:row>
    		<msh:textField property="dateBegin" label="Дата начала"/>
    		<msh:textField property="dateEnd" label="Окончания"/>
    	</msh:row>
    	<msh:row>
    		<td colspan="4">
    			<input type="submit" onclick="find()" value="Поиск"/>
    			<msh:ifInRole roles="/Policy/Mis/MedCase/Stac/Ssl/Planning/Create">
    			<input type="button" onclick="newP()" value="Добавить" >
    			</msh:ifInRole>
    		</td>
    	</msh:row>
    </msh:panel>
    </msh:form>
        <%
    
    String date = (String)request.getParameter("dateBegin") ;
    String dateEnd = (String)request.getParameter("dateEnd") ;
    String id = (String)request.getParameter("id") ;
    String period = (String)request.getParameter("period") ;
    
    if (dateEnd==null || dateEnd.equals("")) dateEnd=date ;
    request.setAttribute("dateBegin", date) ;
    request.setAttribute("dateEnd", dateEnd) ;
    
    String view = (String)request.getAttribute("typeView") ;
    String department = request.getParameter("department") ;
    boolean isgoing = false ;
	if (department!=null && !department.equals("") && !department.equals("0")) {
		request.setAttribute("department", " and wp.lpu_id='"+department+"'") ;
		isgoing=true ;
	}
    if (date!=null && !date.equals("") && isgoing) {
    	
    	String roomType = request.getParameter("roomType") ;
    	if (roomType!=null && !roomType.equals("") && !roomType.equals("0")) {
    		request.setAttribute("roomType", " and wp.roomType_id='"+roomType+"'") ;
    	}
    	String countBed = request.getParameter("countBed") ;
    	if (countBed!=null && !countBed.equals("") && !countBed.equals("0")) {
    		request.setAttribute("countBed", " and wp.countBed_id='"+countBed+"'") ;
    	}
    %>
    <msh:section>
    <msh:sectionTitle>Список пациентов</msh:sectionTitle>
    <msh:sectionContent>
    <ecom:webQuery name="journal_pat" nativeSql="
    select wp.id as wpid,wp.name as wpnamw,vcbihr.name as vcbihr
,list(case 
when slo.dateStart between to_date('${dateBegin}','dd.mm.yyyy') and to_date('${dateEnd}','dd.mm.yyyy')
or coalesce(slo.datefinish,slo.transferdate,current_date)  between to_date('${dateBegin}','dd.mm.yyyy') and to_date('${dateEnd}','dd.mm.yyyy')

then p.lastname ||' '|| coalesce(substring(p.firstname,1,1),'') ||' '||coalesce(substring(p.middlename,1,1),'') 
||' '|| to_char(slo.dateStart,'dd.mm.yyyy')||'-'||coalesce(to_char(slo.dateFinish,'dd.mm.yyyy'),to_char(slo.transferDate,'dd.mm.yyyy')
,'')||'<br/>'
else null end
) as realPat
,list(case 
when wchb.dateFrom between to_date('${dateBegin}','dd.mm.yyyy') and to_date('${dateEnd}','dd.mm.yyyy')
or wchb.dateTo  between to_date('${dateBegin}','dd.mm.yyyy') and to_date('${dateEnd}','dd.mm.yyyy')

then pp.lastname ||' '|| coalesce(substring(pp.firstname,1,1),'') ||' '||coalesce(substring(pp.middlename,1,1),'')||'<br/>' 
||' '|| to_char(wchb.dateFrom,'dd.mm.yyyy')||'-'||coalesce(to_char(wchb.dateTo,'dd.mm.yyyy'),'')
else null end
) as planPat
 from workplace wp
left join medcase slo on slo.roomNumber_id=wp.id
left join patient p on p.id=slo.patient_id
left join VocCountBedInHospitalRoom vcbihr on vcbihr.id=wp.countBed_id
left join WorkCalendarHospitalBed wchb on wchb.hospitalRoom_id=wp.id
left join patient pp on pp.id=wchb.patient_id 

where wp.dtype='HospitalRoom' ${department}
${roomType} ${countBed}
group by wp.id,wp.name,vcbihr.name
order by cast(wp.name as int)
 " />
    <msh:table name="journal_pat" 
     action="javascript:void(0)" idField="1" guid="b621e361-1e0b-4ebd-9f58-b7d919b45bd6">
      <msh:tableColumn columnName="Палата" property="2" />
      <msh:tableColumn columnName="Вместимость" property="3" />
      <msh:tableColumn columnName="Список пациентов, которые лежат" property="4" />
      <msh:tableColumn columnName="Список пациентов, которые планируются" property="5" />
    </msh:table>
    </msh:sectionContent>
    </msh:section>
    <%} %>
    <div id="divViewBed">
    </div>
    <script type="text/javascript">
    	function find() {
    		
    	}
    	function newP() {
    		window.location = 'entityPrepareCreate-stac_planHospital.do?department='+$('department').value+"&roomType="+$('roomType').value+"&countBed="+$('countBed').value+"&dateEnd="+$('dateEnd').value+"&dateBegin="+$('dateBegin').value+"&tmp="+Math.random() ;
      	}
    </script>
  </tiles:put>
  <tiles:put name="side" type="string">
  	<tags:stac_journal currentAction="stac_planning_hospitalizations"/>
  </tiles:put>

  <tiles:put name="title" type="string">
  </tiles:put>

</tiles:insert>