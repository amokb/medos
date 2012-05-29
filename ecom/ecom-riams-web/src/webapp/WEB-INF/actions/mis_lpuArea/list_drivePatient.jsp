<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://struts.apache.org/tags-tiles" prefix="tiles" %>
<%@ taglib uri="http://www.nuzmsh.ru/tags/msh" prefix="msh" %>
<%@ taglib uri="http://www.ecom-ast.ru/tags/ecom" prefix="ecom" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="tags" %>

<tiles:insert page="/WEB-INF/tiles/mainLayout.jsp" flush="true" >

    <tiles:put name='title' type='string'>
    <msh:title property="Medcard">Просмотр данных по участку</msh:title>
    </tiles:put>

    <tiles:put name='side' type='string'>
        <style type="text/css">
         #rwDel {
         	border-top: 1px solid gray;
         }
        </style>
    </tiles:put>
    
  <tiles:put name="body" type="string">
    <msh:form action="psych_listByArea.do" defaultField="dateBegin" disableFormDataConfirm="true" method="GET" guid="d7b31bc2-38f0-42cc-8d6d-19395273168f">
    <msh:panel guid="6ae283c8-7035-450a-8eb4-6f0f7da8a8ff">
	<input type="hidden" id="check" name="check" />
	<input type="hidden" id="m" value="printArea" name="m"/>
	<input type="hidden" id="s" value="PsychiatricService" name="s"/>
      <msh:row guid="53627d05-8914-48a0-b2ec-792eba5b07d9">
        <msh:separator label="Параметры поиска" colSpan="7" guid="15c6c628-8aab-4c82-b3d8-ac77b7b3f700" />
      </msh:row>
      <msh:row>
        <td class="label" title="Список  (typeDate)" colspan="1"><label for="typeDateName" id="typeDateLabel">Список:</label></td>
        <td onclick="this.childNodes[1].checked='checked';">
        	<input type="radio" name="typeDate" value="1">  состоящие
        </td>
	        <td onclick="this.childNodes[1].checked='checked';" colspan="2">
	        	<input type="radio" name="typeDate" value="2">  снятые и переведенные
	        </td>
       </msh:row>
       <msh:row>
       <td colspan="1"></td>
        <td onclick="this.childNodes[1].checked='checked';">
        	<input type="radio" name="typeDate" value="3">  взятые на учет
        </td>
                <msh:autoComplete property="reasonBegin" 
                vocName="vocPsychObservationReason" horizontalFill="true" fieldColSpan="3" label="причина"/>
        
        </msh:row>
        <msh:row>
	       <td colspan="1"></td>
	        <td onclick="this.childNodes[1].checked='checked';">
	        	<input type="radio" name="typeDate" value="4">  переведенные
	        </td>
            <msh:autoComplete property="reasonTransfer" vocName="vocPsychTransferReason"
             horizontalFill="true" fieldColSpan="3" label="причина"/>
        </msh:row>
        <msh:row>
	       <td colspan="1"></td>
	        <td onclick="this.childNodes[1].checked='checked';">
	        	<input type="radio" name="typeDate" value="5">  снятые с учета
	        </td>
	        <msh:autoComplete property="reasonEnd" vocName="vocPsychStrikeOffReason"
	         horizontalFill="true" fieldColSpan="3" label="причина"/>
        </msh:row>

        <msh:row  styleId="rwDel">
        <td class="label" title="Пациенты" colspan="1"><label for="typeInvName" id="typeFirstLabel">Пациенты:</label></td>
        <td onclick="this.childNodes[1].checked='checked';">
        	<input type="radio" name="typeInv" value="1">  все
        </td>
        <td onclick="this.childNodes[1].checked='checked';" colspan="3">
        	<input type="radio" name="typeInv" value="2">  недееспособные
        </td>
        </msh:row>
        <msh:row>
        <td></td>
	        <td onclick="this.childNodes[1].checked='checked';">
	        	<input type="radio" name="typeInv" value="3">  инвалиды
	        </td>
	        <td onclick="this.childNodes[1].checked='checked';" colspan="2">
	        	<input type="radio" name="typeInv" value="4">  перв. инвалиды
	        </td>
	        
	        <td onclick="this.childNodes[1].checked='checked';" colspan="3">
	        	<input type="radio" name="typeInv" value="5">  инвалиды (недеесп.)
	        </td>
	        
        </msh:row>
        <msh:row>
	        <msh:autoComplete property="groupInv" fieldColSpan="5" horizontalFill="true" vocName="vocInvalidity" hideLabel="true"/>
        	
        </msh:row>
        <msh:row styleId="rwDel">
        <td class="label" title="Суицид" colspan="1">
        <label for="typeSuicideName" id="typeFirstLabel">Суицид:</label></td>
                <td onclick="this.childNodes[1].checked='checked';">
        	<input type="radio" name="typeSuicide" value="1">  не учитывается
        </td>
	        <td onclick="this.childNodes[1].checked='checked';">
	        	<input type="radio" name="typeSuicide" value="2">  был
	        </td>
	        <td onclick="this.childNodes[1].checked='checked';" colspan="2">
	        	<input type="radio" name="typeSuicide" value="3">  был завершенный
	        </td>
        </msh:row>
        <msh:row>
        	<td></td>
        	<msh:autoComplete property="natureSuicide" vocName="vocPsychSuicideNature" fieldColSpan="3" horizontalFill="true"/>
        </msh:row>
        <msh:row styleId="rwDel">
        	<msh:autoComplete property="lpuArea" vocName="lpuArea" fieldColSpan="5" horizontalFill="true" label="Участок"/>
        </msh:row>
        <msh:row>
        	<msh:autoComplete property="ambulatoryCare" vocName="vocPsychAmbulatoryCare" label="Вид наблюдения" fieldColSpan="5" horizontalFill="true" />
        </msh:row>
        <msh:row styleId="rwDel">
        	<msh:autoComplete parentAutocomplete="ambulatoryCare" property="group" vocName="vocPsychDispensaryGroup" label="Группа (АДН+АПЛ)" fieldColSpan="5" horizontalFill="true" />
        </msh:row>
        <msh:row>
        	<msh:autoComplete property="compTreatment" vocName="vocPsychCompulsoryTreatment" fieldColSpan="5" horizontalFill="true" label="Принуд.лечен"/>
        </msh:row>
      <msh:row>
        <td class="label" title="Список  (typeCare)" colspan="1"><label for="typeCareName" id="typeCareLabel">Список:</label></td>
        <td onclick="this.childNodes[1].checked='checked';">
        	<input type="radio" name="typeCare" value="1">  не учитывать
        </td>
        <td onclick="this.childNodes[1].checked='checked';">
        	<input type="radio" name="typeCare" value="2">  состоящие
        </td>
        <td onclick="this.childNodes[1].checked='checked';" colspan="1">
        	<input type="radio" name="typeCare" value="3">  взятые
        </td>
        <td onclick="this.childNodes[1].checked='checked';" colspan="2">
        	<input type="radio" name="typeCare" value="4">  снятые
        </td>
       </msh:row>
        
        <msh:row styleId="rwDel">
        	<msh:autoComplete  property="sex" vocName="vocSex" label="Пол" fieldColSpan="1" horizontalFill="true" />
        	
        <td onclick="this.childNodes[1].checked='checked';" colspan="2">
        	<input type="radio" name="typeAge" value="1">  с учетом возраста
        </td>
        <td onclick="this.childNodes[1].checked='checked';" colspan="3">
        	<input type="radio" name="typeAge" value="2">  без учета
        </td>
        </msh:row>
        <msh:row>
        	<msh:textField property="ageFrom" label="Возраст с"/>
        	<msh:textField property="ageTo" label="по"/>
        </msh:row>
        <msh:row styleId="rwDel">
        <msh:textField property="dateBegin" label="Период с" guid="8d7ef035-1273-4839-a4d8-1551c623caf1" />
        <msh:textField property="dateEnd" label="по" guid="f54568f6-b5b8-4d48-a045-ba7b9f875245" />
        <td>
            <input type="submit" onclick="find()" value="Найти" />
            <input type="submit" onclick="print()" value="Печать" />
          </td>
        </msh:row>
        
    </msh:panel>
    </msh:form>
    
    <script type='text/javascript'>

     checkFieldUpdate('typeDate','${typeDate}',5) ;
     //checkFieldUpdate('typeFirst','${typeFirst}',3) ;
     checkFieldUpdate('typeInv','${typeInv}',5) ;
     checkFieldUpdate('typeAge','${typeAge}',2) ;
     checkFieldUpdate('typeSuicide','${typeSuicide}',3) ;
     checkFieldUpdate('typeCare','${typeCare}',4) ;
     function checkFieldUpdate(aField,aValue,aDefault) {
     	
     	eval('var chk =  document.forms[0].'+aField) ;
     	aValue=+aValue ;
     	max = chk.length ;
     	if (aValue==0 || aValue>max) aValue=+aDefault ;
     	if (aValue==0 ||aValue>max) {
     		chk[max-1].checked='checked' ;
     	} else {
     		chk[aValue-1].checked='checked' ;
     	}
     }
   
    
    function find() {
    	var frm = document.forms[0] ;
    	var id = getCheckedRadio(frm,"typeDate")
    				//+":" +$("reasonBegin").value
    				//+":" +$("reasonTransfer").value+":" +$("reasonEnd").value
    				;
    	id = id+":"+getCheckedRadio(frm,"typeInv")
    	//+":"+$('groupInv').value 
    	;
    	id = id+":"+getCheckedRadio(frm,"typeSuicide") ;
    	id = id+":"+getCheckedRadio(frm,"typeAge") ;
    	$('check').value = id ;
    	frm.target='' ;
    	frm.action='psych_listByArea.do' ;
    }
    function print() {
    	var frm = document.forms[0] ;
    	frm.target='_blank' ;
    	frm.action='print-psych_listByArea.do' ;
    }
    
    </script>
    
    <%
    String date = (String)request.getParameter("dateBegin") ;
    Long lpuArea = (Long)request.getAttribute("lpuArea") ;
    if (lpuArea!=null && date!=null && !date.equals(""))  {
    	%>
    
    <msh:section>
    <msh:sectionTitle>Результаты поиска ${infoTypePat}.
     Период с ${param.dateBegin} по ${param.dateEnd}. ${infoSearch} ${dateInfo} ${dateFInfo} ${typeInvInfo}
     ${suicideInfo} ${sexInfo} ${ageInfo}
     </msh:sectionTitle>
    <msh:sectionContent>
    <ecom:webQuery name="journal_ticket" nativeSql="
   select distinct pcc.id as pccid
   ,pcc.cardNumber as pcccardNumber
   ,coalesce(p.lastname,'-')||' '||coalesce(p.firstname,'-')|| ' '||coalesce(p.middlename,'-') as lfm
   ,area.startDate as areastartdate,area.finishDate as areafinishdate,p.birthday as pbirthday 
   , case when p.address_addressId is not null 
	          then coalesce(a.fullname,a.name) || 
	               case when p.houseNumber is not null and p.houseNumber!='' then ' д.'||p.houseNumber else '' end 
	 ||case when p.houseBuilding is not null and p.houseBuilding!='' then ' корп.'|| p.houseBuilding else '' end 
	||case when p.flatNumber is not null and p.flatNumber!='' then ' кв. '|| p.flatNumber else '' end
	       when p.territoryRegistrationNonresident_id is not null
	          then okt.name||' '||p.RegionRegistrationNonresident||' '||oq.name||' '||p.SettlementNonresident
	               ||' '||ost.name||' '||p.StreetNonresident||
	               case when p.HouseNonresident is not null and p.HouseNonresident!='' then ' д.'||p.HouseNonresident else '' end
	 ||case when p.BuildingHousesNonresident is not null and p.BuildingHousesNonresident!='' then ' корп.'|| p.BuildingHousesNonresident else '' end 
	||case when p.ApartmentNonresident is not null and p.ApartmentNonresident!='' then ' кв. '|| p.ApartmentNonresident else '' end
	   else '' 
	  end as address
   ,$$getDiagnosis^ZPsychUtil(p.id,isnull(area.finishDate,CURRENT_DATE))
   ,vpor.name as vporname,vptr.name as vptrname,vpsor.name as vpsorname
   from PsychiatricCareCard pcc 
   left join Patient p on p.id=pcc.patient_id 
   left join LpuAreaPsychCareCard area on pcc.id=area.careCard_id 
   left join CompulsoryTreatment ct on pcc.id=ct.careCard_id
   left join PsychiaticObservation po on po.lpuAreaPsychCareCard_id=area.id
   ${groupDopJoin}
   left join Suicide sui on sui.careCard_id=pcc.id
   left join VocPsychObservationReason vpor on vpor.id=area.observationReason_id
   left join VocPsychTransferReason vptr on vptr.id=area.transferReason_id
   left join VocPsychStrikeOffReason vpsor on vpsor.id=area.stikeOffReason_id
   left join Invalidity inv on inv.patient_id=p.id
	left join Address2 a on a.addressId=p.address_addressId
	left join Omc_KodTer okt on okt.id=p.territoryRegistrationNonresident_id
	left join Omc_Qnp oq on oq.id=p.TypeSettlementNonresident_id
	left join Omc_StreetT ost on ost.id=p.TypeStreetNonresident_id

   where  area.lpuArea_id=${lpuArea} 
   ${dateT} ${groupDopSql} ${typeI} ${compTreat}
   ${suicide} 
   ${group} ${sexT} ${ageFrom} ${ageTo}
   group by area.id 
   order by p.lastname,p.firstname,p.middlename" guid="4a720225-8d94-4b47-bef3-4dbbe79eec74" />
        <msh:table viewUrl="entityShortView-psych_careCard.do" editUrl="entityParentEdit-psych_careCard.do" deleteUrl="entityParentDeleteGoParentView-psych_careCard.do" name="journal_ticket" action="entityView-psych_careCard.do" idField="1" noDataMessage="Не найдено">
			<msh:tableColumn columnName="#" property="sn"/>
			<msh:tableColumn columnName="№карты" property="2"/>
			<msh:tableColumn columnName="ФИО пациента" property="3"/>
			<msh:tableColumn columnName="Год рождения" property="6"/>
			<msh:tableColumn columnName="Дата взятия" property="4"/>
			<msh:tableColumn columnName="Причина взятия" property="9"/>
			<msh:tableColumn columnName="Дата снятия (перевода)" property="5"/>
			<msh:tableColumn columnName="Причина перевода" property="10"/>
			<msh:tableColumn columnName="Причина снятия" property="11"/>
			<msh:tableColumn columnName="Адрес" property="7"/>
			<msh:tableColumn columnName="Диагноз" property="8"/>
        </msh:table>
    <%-- 
   ,(select list(mkb.code) from Diagnosis dd left join vocidc10 mkb on mkb.id=dd.idc10_id where dd.patient_id=pcc.patient_id and dd.establishDate between area.startDate and area.finishDate)
   
    --%>
    </msh:sectionContent>
    </msh:section>
    <% } else {%>
    	<i>Выберите параметры и нажмите найти </i>
    	<% }   %>
  </tiles:put>
  <tiles:put name="javascript" type="string">
  	<script type="text/javascript">
    if (groupInvAutocomplete) groupInvAutocomplete.addOnChangeCallback(function() {
    	document.forms[0].typeInv[2].checked='checked' ;
    });
    if (reasonEndAutocomplete) reasonEndAutocomplete.addOnChangeCallback(function() {
    	document.forms[0].typeDate[4].checked='checked' ;
    });
    if (reasonBeginAutocomplete) reasonBeginAutocomplete.addOnChangeCallback(function() {
    	document.forms[0].typeDate[2].checked='checked' ;
    	//document.forms[0].typeFirst[2].checked='checked' ;
    });
    if (reasonTransferAutocomplete) reasonTransferAutocomplete.addOnChangeCallback(function() {
    	document.forms[0].typeDate[3].checked='checked' ;
    });
    if (natureSuicideAutocomplete) natureSuicideAutocomplete.addOnChangeCallback(function() {
    	document.forms[0].typeSuicide[2].checked='checked' ;
    });
  	</script>
  </tiles:put>
</tiles:insert>

