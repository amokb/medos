
<%@page import="ru.ecom.mis.web.action.medcase.journal.AdmissionJournalForm"%>
<%@page import="ru.ecom.web.util.ActionUtil"%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://struts.apache.org/tags-tiles" prefix="tiles" %>
<%@ taglib uri="http://www.nuzmsh.ru/tags/msh" prefix="msh" %>
<%@ taglib uri="http://www.ecom-ast.ru/tags/ecom" prefix="ecom" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="tags" %>

<tiles:insert page="/WEB-INF/tiles/main${param.short}Layout.jsp" flush="true" >

  <tiles:put name="title" type="string">
    <msh:title guid="helloItle-123" mainMenu="StacJournal" title="Форма 32. Раздел 3."/>
  </tiles:put>
  <tiles:put name="side" type="string">

  </tiles:put>
  <tiles:put name="body" type="string">
  <%
  	//request.setAttribute("diag_typeReg_cl", "4") ;
  	//request.setAttribute("diag_typeReg_pat", "5") ;
  	//request.setAttribute("diag_priority_m", "1")	
  	
  	if (request.getParameter("short")==null||request.getParameter("short").equals("")) {	
  %>
    <msh:form action="/stac_report_32.do" defaultField="dateBegin" disableFormDataConfirm="true" method="GET" guid="d7b31bc2-38f0-42cc-8d6d-19395273168f">
    <input type="hidden" name="id" id="id" value=""/>
    <msh:panel guid="6ae283c8-7035-450a-8eb4-6f0f7da8a8ff">
      <msh:row guid="53627d05-8914-48a0-b2ec-792eba5b07d9">
        <msh:separator label="Параметры поиска" colSpan="7" />
      </msh:row>

      <msh:row>
        <msh:textField property="dateBegin" label="Период с" />
		<msh:textField property="dateEnd" label="по" />
      </msh:row>
              <msh:row>
        	<msh:autoComplete property="sex" fieldColSpan="4" horizontalFill="true" label="Пол" vocName="vocSex"/>
        </msh:row>
        <msh:row>
                
           <td>
            <input type="submit" value="Найти" />
     
          </td>
        </msh:row>
    </msh:panel>
    </msh:form>
    
    <% }%>
    <script type="text/javascript" src="./dwr/interface/HospitalMedCaseService.js">/**/</script>
           <script type='text/javascript'>
          
      //     checkFieldUpdate('typeView','${typeView}',1) ;
       //    checkFieldUpdate('typeDiagOrder','${typeDiagOrder}',1) ;
        //   checkFieldUpdate('typeHour','${typeHour}',1) ;
           

   function checkFieldUpdate(aField,aValue,aDefaultValue) {
   	eval('var chk =  document.forms[0].'+aField) ;
   	var aMax=chk.length ;
   	//alert(aField+" "+aValue+" "+aMax+" "+chk) ;
   	if ((+aValue)==0 || (+aValue)>(+aMax)) {
   		chk[+aDefaultValue-1].checked='checked' ;
   	} else {
   		chk[+aValue-1].checked='checked' ;
   	}
   }
			 
    
    
    if ($('dateBegin').value=="") {
    	$('dateBegin').value=getCurrentDate() ;
    }

			 
    </script>
    <%
    
    String date = request.getParameter("dateBegin") ;
    if (date!=null) {
    String dateEnd = request.getParameter("dateEnd") ;
    request.setAttribute("isReportBase", ActionUtil.isReportBase(date, dateEnd,request));
    //String id = (String)request.getParameter("id") ;

    if (dateEnd==null || dateEnd.equals("")) dateEnd=date ;
    request.setAttribute("dateBegin", date) ;
    request.setAttribute("dateEnd", dateEnd) ;
	request.setAttribute("dateEnddd", dateEnd.substring(0,2)) ;
	request.setAttribute("dateEndmm", dateEnd.substring(3,5)) ;
	request.setAttribute("dateEndyyyy", dateEnd.substring(6)) ;

  //  String view = (String)request.getAttribute("typeView") ;
    String sex = request.getParameter("sex");
    String sqlAdd = "";
    if (sex!=null&&!sex.equals("")) {
    	sqlAdd+=" and pat.sex_id="+sex;
    }

    String type=request.getParameter("type");
  
   
    
if (type!=null&&type.equals("reestr")) {
	String id =  request.getParameter("id");
	if (id!=null&&!id.equals("")) {
		if (id.equals("alive")) { sqlAdd+=" and vlb.code='1' and dc.id is null";}    
		else if (id.equals("born2die")) {sqlAdd+=" and vlb.code='1' and dc.deathdate is not null";}    
		else if (id.equals("die")) {sqlAdd+=" and vlb.code='2'";}    
	}
 	String w = request.getParameter("weight");
	if (w!=null&&!w.equals("")) {
		int weight = Integer.valueOf(w).intValue();
		
		switch(weight) {
		case 1: 
			sqlAdd+=" and nb.birthweight between 500 and 749";
			break;
		case 2: 
			sqlAdd+=" and nb.birthweight between 750 and 999";
			break;
		case 3: 
			sqlAdd+=" and nb.birthweight between 1000 and 1499";
			break;
		case 4: 
			sqlAdd+=" and nb.birthweight between 1500 and 1999";
			break;
		case 5: 
			sqlAdd+=" and nb.birthweight between 2000 and 2499";
			break;
		case 6: 
			sqlAdd+=" and nb.birthweight between 2500 and 2999";
			break;
		case 7: 
			sqlAdd+=" and nb.birthweight between 3000 and 3499";
			break;
		case 8: 
			sqlAdd+=" and nb.birthweight between 3500 and 3999";
			break;
		case 9: 
			sqlAdd+=" and nb.birthweight >= 4000";
			break;
		case 10: 
			sqlAdd+=" and cb.durationpregnancy <37.00 ";
			break;
		case 11: 
			sqlAdd+=" and cb.durationpregnancy <28.00";
			break;
		case 12: 
			sqlAdd+=" and (extract(epoch from age(cast(to_char(dc.deathdate, 'yyyy-mm-dd') || ' ' || to_char(dc.deathtime, 'hh:mi:00') as timestamp), ";
			sqlAdd+=" cast(to_char(birthdate, 'yyyy-mm-dd') || ' ' || to_char(birthtime, 'hh:mi:00') as timestamp)))/3600)< 24";
			break;
		case 13: 
			sqlAdd+=" and (extract(epoch from age(cast(to_char(dc.deathdate, 'yyyy-mm-dd') || ' ' || to_char(dc.deathtime, 'hh:mi:00') as timestamp), ";
			sqlAdd+=" cast(to_char(birthdate, 'yyyy-mm-dd') || ' ' || to_char(birthtime, 'hh:mi:00') as timestamp)))/3600) between 24 and 168";
			break;
		}  
	}
	
	request.setAttribute("sqlAdd", sqlAdd);
	%> 
	 <msh:section>
    <ecom:webQuery isReportBase="${isReportBase}" name="Report32_reestr" nameFldSql="Report32_reestr_sql" nativeSql="
select pat.id
,pat.patientinfo
from newborn nb
left join vocliveborn vlb on vlb.id=nb.liveborn_id
left join childbirth cb on cb.id=nb.childbirth_id
left join patient pat on pat.id=nb.patient_id
left join deathcase dc on dc.patient_id=nb.patient_id
where nb.birthdate between to_date('${dateBegin}','dd.MM.yyyy') and to_date('${dateEnd}','dd.MM.yyyy')
${sqlAdd}

" />
    <msh:sectionTitle>
    </msh:sectionTitle>
    <msh:sectionContent>
    <msh:table name="Report32_reestr" 
     action="entityView-mis_patient.do" idField="1"     >
      <msh:tableColumn columnName="ID" property="1" />
      <msh:tableColumn columnName="Fam" property="2" addParam=""  />
      
    </msh:table>
    
    </msh:sectionContent>
    </msh:section>
	
	<%
} else {
    request.setAttribute("sqlAdd", sqlAdd);
     %>
    
    <msh:section>
    <msh:sectionTitle>Результаты поиска за период с ${dateBegin} по ${dateEnd}.</msh:sectionTitle>
    </msh:section>
   
    <msh:section>
    <ecom:webQuery isReportBase="${isReportBase}" name="Report32" nameFldSql="Report32_sql" nativeSql="
select case 
when vlb.code='1' and dc.id is null then 'Родился живым' 
when vlb.code='1' and dc.deathdate is not null then 'Родился и умер'
when vlb.code='2' then 'Родился мертвым'
end as f1_name
, count(nb.id) as f2_cntAll
,count(case when nb.birthweight between 500 and 749 then nb.id else null end) as f3_749
,count(case when nb.birthweight between 750 and 999 then nb.id else null end) as f4_999
,count(case when nb.birthweight between 1000 and 1499 then nb.id else null end) as f5_1499
,count(case when nb.birthweight between 1500 and 1999 then nb.id else null end) as f6_1999
,count(case when nb.birthweight between 2000 and 2499 then nb.id else null end) as f7_2499
,count(case when nb.birthweight between 2500 and 2999 then nb.id else null end) as f8_2999
,count(case when nb.birthweight between 3000 and 3499 then nb.id else null end) as f9_3499
,count(case when nb.birthweight between 3500 and 3999 then nb.id else null end) as f10_3999
,count(case when nb.birthweight > 3999 then nb.id else null end) as f11_4000 
,count(case when cb.durationpregnancy <37.00 then nb.id else null end) as f12_nedonos
,count(case when cb.durationpregnancy <28.00 then nb.id else null end) as f13_nedonos_28
,count(case when (extract(epoch from age(cast(to_char(dc.deathdate, 'yyyy-mm-dd') || ' ' || to_char(dc.deathtime, 'hh:mi:00') as timestamp),
cast(to_char(birthdate, 'yyyy-mm-dd') || ' ' || to_char(birthtime, 'hh:mi:00') as timestamp)))/3600) 
< 24 then nb.id end) as f14_dead24
,count (case when (extract(epoch from age(cast(to_char(dc.deathdate, 'yyyy-mm-dd') || ' ' || to_char(dc.deathtime, 'hh:mi:00') as timestamp),
cast(to_char(birthdate, 'yyyy-mm-dd') || ' ' || to_char(birthtime, 'hh:mi:00') as timestamp)))/3600) 
between 24 and 168 then nb.id end) as f15_death168
,max(
case 
when vlb.code='1' and dc.id is null then 'alive' 
when vlb.code='1' and dc.deathdate is not null then 'born2die'
when vlb.code='2' then 'die' end
) as idLld
from newborn nb
left join vocliveborn vlb on vlb.id=nb.liveborn_id
left join childbirth cb on cb.id=nb.childbirth_id
left join patient pat on pat.id=nb.patient_id
left join deathcase dc on dc.patient_id=nb.patient_id
where nb.birthdate between to_date('${dateBegin}','dd.MM.yyyy') and to_date('${dateEnd}','dd.MM.yyyy')
${sqlAdd}
group by 
case 
when vlb.code='1' and dc.id is null then 'Родился живым' 
when vlb.code='1' and dc.deathdate is not null then 'Родился и умер'
when vlb.code='2' then 'Родился мертвым'
end 

" />
    <msh:sectionTitle>
    	    <form action="print-stac_report_32.do" method="post" target="_blank">
	    Распределение родившихся и умерших по массе тела при рождении
	    <input type='hidden' name="sqlText" id="sqlText" value="${Report32swod_sql}"> 
	    <input type='hidden' name="sqlInfo" id="sqlInfo" value="Свод по нозоологиям (выписанные) за ${param.dateBegin}-${dateEnd}.">
	    <input type='hidden' name="sqlColumn" id="sqlColumn" value="${groupName}">
	    <input type='hidden' name="s" id="s" value="PrintService"><input type='hidden' name="isReportBase" id="isReportBase" value="${isReportBase}">
	    <input type='hidden' name="m" id="m" value="printNativeQuery">
	    <!-- <input type="submit" value="Печать"> --> 
	    </form>     
    </msh:sectionTitle>
    <msh:sectionContent>
    <msh:table name="Report32" 
     
     action="stac_report_32.do?short=Short&type=reestr&dateBegin=${dateBegin}&dateEnd=${dateEnd}" idField="16"
    cellFunction="true"  
     >
      <msh:tableColumn columnName="Наименование" property="1" />
      <msh:tableColumn columnName="Всего" property="2" addParam=""  />
      <msh:tableColumn columnName="500-749" property="3" addParam="&weight=1"/>
      <msh:tableColumn columnName="750-999" property="4" addParam="&weight=2"/>
      <msh:tableColumn columnName="1000-1499" property="5" addParam="&weight=3"/>
      <msh:tableColumn columnName="1500-1999" property="6" addParam="&weight=4"/>
      <msh:tableColumn columnName="2000-2499" property="7" addParam="&weight=5"/>
      <msh:tableColumn columnName="2500-2999" property="8" addParam="&weight=6"/>
      <msh:tableColumn columnName="3000-3499" property="9" addParam="&weight=7"/>
      <msh:tableColumn columnName="3500-3999" property="10" addParam="&weight=8"/>
      <msh:tableColumn columnName="4000 и выше" property="11" addParam="&weight=9"/>
      <msh:tableColumn columnName="Недоношенные" property="12" addParam="&weight=10"/>
      <msh:tableColumn columnName="Недон. до 28 нед." property="13" addParam="&weight=11"/>
      <msh:tableColumn columnName="Умерли в I сутки" property="14" addParam="&weight=12"/>
      <msh:tableColumn columnName="Умерли на I неделе" property="15" addParam="&weight=13"/>
    </msh:table>
    
    </msh:sectionContent>
    </msh:section>
    <% }
    }
    	%>
    
  </tiles:put>
</tiles:insert>