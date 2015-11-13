<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://struts.apache.org/tags-tiles" prefix="tiles" %>
<%@ taglib uri="http://www.nuzmsh.ru/tags/msh" prefix="msh" %>
<%@ taglib uri="http://www.ecom-ast.ru/tags/ecom" prefix="ecom" %>
<%@ taglib uri="http://www.ecom-ast.ru/tags/mis" prefix="mis" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="tags" %>
<tiles:insert page="/WEB-INF/tiles/mainLayout.jsp" flush="true">

	<tiles:put name="javascript" type="string">
	<script type="text/javascript" src="./dwr/interface/PrescriptionService.js"></script>
	<msh:ifFormTypeIsNotView formName="pres_operationPrescriptionForm">
	<script type="text/javascript">
	
	var currentDate = new Date;
	var textDay = currentDate.getDate()<10?'0'+currentDate.getDate():currentDate.getDate();
	var textMonth = currentDate.getMonth()+1;
	var textMonth = textMonth<10?'0'+textMonth:textMonth;
	var textYear =currentDate.getFullYear();
	var textDate = textDay+'.'+textMonth+'.'+textYear;
	
/* 	function changeDate(days) {
		var l = $('labDate')?$('labDate').value:$('planStartDate').value;
		l=l.substr(6,4)+'-'+l.substr(3,2)+'-'+l.substr(0,2);
		currentDate.setTime (Date.parse(l));
		currentDate.setDate(currentDate.getDate()+days);
		var newTextDay = currentDate.getDate()<10?'0'+currentDate.getDate():currentDate.getDate();
		var newTextMonth = currentDate.getMonth()+1;
		var newTextMonth = newTextMonth<10?'0'+newTextMonth:newTextMonth;
		var newTextYear =currentDate.getFullYear();
		var newTextDate = newTextDay+'.'+newTextMonth+'.'+newTextYear;
		if ($('labDate')) $('labDate').value=newTextDate;
		if ($('planStartDate')) $('planStartDate').value=newTextDate;
		
		for (var i=1;i<=labNum;i++) {
			$('labDate'+i).value=newTextDate;
		}
	} */
	//Заполняем ЛН данными из шаблона (не удаляя существующие назначения). 
	function fillFormFromTemplate(aData) {
		
		var aRow = aData.split("#");
		if (aRow.length>0) {
			for (var i=0;i<aRow.length;i++) {
				var research = aRow[i].split("@");
				if (research[0]!=null && research[0]!="" ){
					var prescType = research[0];
					if (prescType=="SERVICE") {
						addRows(research[1],1);
					}
				}
			}
		}
		
	}
	
/* 	function prepare1Row(aId,aName) {
  		
  		$('labServicies').value=aId ;
  		$('labServiciesName').value=aName ;
  		show2EnterDate() 		
  	}
	function prepare1RowByDate(aDate) {
		$('labDate').value=aDate ;
		addRow('lab') ;
  		$('labServicies').value='' ;
  		$('labServiciesName').value='' ;
	} */
	var oldaction = document.forms['pres_operationPrescriptionForm'].action ;
	document.forms['pres_operationPrescriptionForm'].action="javascript:checkDoubles()";
	
	var num=0;
	var labNum=0;
	var funcNum=0;
	var surgNum=0;
	var labList="";

	function preShowDir() {
		 $('1IsViewButton').value=$('prescriptType').value ;
		 var list = +$('labServicies').value;
		 clear1DirMedServiceDialog() ;
		 var typeNum = 0;
			type='lab';
				typeNum = labNum;
				
			
			while (typeNum>0) {
				if (document.getElementById(type+"Element"+typeNum)) {
					var ar = $(type+"Service"+typeNum).value ;
					list+="," ;
					list+=ar ;
				}
	       		typeNum-=1;
		 	}
			
		 $('1ListIds').value=list;
	 }
 	//onload =
	function startLoad () {
		var date = new Date();
		var month = date.getMonth()+1; if (month<10) {month="0"+month;}
		var day = date.getDate(); if (day<10) {day="0"+day;}
		var year = date.getFullYear();
	//	if ($('labDate')) $('labDate').value=day+"."+month+"."+year;
	//	if ($('funcDate')) $('funcDate').value=day+"."+month+"."+year;	
	}	

	function checkDoubles () {
		labList="";
		
		
		var i=0;
		// Id from aList 
		var str="";
		var aList =labList;
		aList = aList.substring(0,aList.length-1);
		var aListArr = aList.split("#");
		if (aListArr.length>0) {
			for (var i=0; i<aListArr.length;i++) {
				var id = aListArr[i].split(":");
			str+=id[0]+":";
			}
		}
		str=str.substring(0,str.length-1);
		PrescriptionService.getMedcaseByPrescriptionList($('prescriptionList').value, {
			callback: function (aMedCase) {
				PrescriptionService.getDuplicatePrescriptions(""+aMedCase, str,{
					callback: function(aResult) {
						if (aResult.length>0){
							var aText = "Данные назначения\n "+aResult+"\nуже назначены пациенту в этой истории болезни, все равно назначить?";
								if (!confirm (aText)) {							
									document.getElementById('submitButton').disabled=false;
									document.getElementById('submitButton').value='Создать';
									return;
									}
						}
						checkLabs();
						
						
					}
				});		
			}
		});
		 
	

	}

	function removeRows(type) {
		var rType;
		if (type=='lab') {rType=labNum; labNum=0;}
		else if (type=='func') {rType=funcNum;funcNum=0;}
		else if (type=='drug') {rType=drugNum;drugNum=0;}
		else return;
		 
		if (rType>0) {
			for (var i=1; i<=rType;i++) {
				if (document.getElementById(type+'Element'+i)){
					var node = document.getElementById(type+'Element'+i);
					node.parentNode.removeChild(node);
				}				
			}
		}
		
	}
	function checkLabs() {
		
		if ($('surgServicies')) {
			writeServicesToList('surg');
		}
		$('labList').value=labList ;
	alert($('labList').value);
		document.forms['pres_operationPrescriptionForm'].action=oldaction ;
		document.forms['pres_operationPrescriptionForm'].submit();
	}
	
	function writeServicesToList(type) {
		var typeNum = 0;
		var fld ; reqFld = [0,1] ;
		 if (type=='surg') {
			typeNum=surgNum;
			reqFld=[0,2,4];
			fld = [['Service',1],['CalDateName',1],['Cabinet',1],['',1],['CalTime',1]] ;
		} 
		var isDoubble=0;
		while (typeNum>0) {
			if (document.getElementById(type+"Element"+typeNum)) {
				var ar = getArrayByFld(type, typeNum, fld, reqFld, type+'Servicies', 0) ;
				labList += ar [0] ; isDoubble=ar[2] ; 
			}
       		typeNum-=1;
	 	}
		if (isDoubble==0) {
			fld[0] = ['Servicies',1] ;
			//var fld = [['Servicies',1],['Date',1],['Cabinet',1]] ;
			var ar = getArrayByFld(type, "", fld, reqFld, type+'Servicies', -1) ;
			labList += ar [0] ;  
     	}
	}
		function getArrayByFld(aType, aTypeNum, aFldList, aReqFldId, aCheckFld, aCheckId) {
			var next = true ;
			var l="",lAll="",isDoubble=0 ;
		for (var i=0;i<aReqFldId.length;i++) {
			if ($(aType+aFldList[aReqFldId[i]][0]+aTypeNum).value=="") {next=false ; break;}  
		}
		if (next) {
			//Формат строки - name:date:method:freq:freqU:amount:amountU:duration:durationU# 
			for (var i=0;i<aFldList.length;i++) {
				var val = aFldList[i][0]==""?"":$(aType+aFldList[i][0]+aTypeNum).value ;
				if (i!=0) {
					l += ":" ;lAll+=":";
				} 
				if (aCheckId==i) {
					if ($(aCheckFld).value==val) {
		            	isDoubble=1;	
		            }						
				}
				if (aFldList[i][1]==1) { l += val ; }
				lAll+=val ;
				if (i==(aFldList.length-1)) {l += "#" ;lAll+="#" ;}
			}
		}
		return [l,lAll,isDoubble] ;
		}
		

		function addRows(aResult,aFocus) {
		var resultRow = aResult.split(":");
		/*
		0 - ms.type
		1 - ms.ID 2 - ms. code+name
		3 - date
		4 - cabinetcode           5 - cabinetname
		6 - departmentintakecode  7 - departmentintakename (for lab)
		8 - timecode              9 - timename (for func)
		*/
		var type = resultRow[0];
		var id = resultRow[1]; 
		var name = resultRow[2];
		var date = resultRow[3]!=""?resultRow[3]:textDate;
		var cabinet = resultRow[4]?resultRow[4]:"";
		var cabinetName = resultRow[5]?resultRow[5]:"";
		
		if (type=='LABSURVEY' || type=='lab') {
			type='lab'; num = labNum;
		} else if (type=='DIAGNOSTIC' || type=='func') {
			type='func'; num = funcNum; 
		} else if (type=='surg') {
			num = surgNum;
		} else if (type='hosp') {
			num = hospNum;
		}
		num+=1;
	    
 		var tbody = document.getElementById('add'+type+'Elements');
	    var row = document.createElement("TR");
		row.id = type+"Element"+num;
	    tbody.appendChild(row);
	
	    // Создаем ячейки в вышесозданной строке и добавляем тх 
	    var td1 = document.createElement("TD"); td1.colSpan="2"; td1.align="right";
	    var td2 = document.createElement("TD"); td2.colSpan="2";
	    var td3 = document.createElement("TD");
	    row.appendChild(td1); row.appendChild(td2); row.appendChild(td3);
	   
	    // Наполняем ячейки 
	    //var dt2="<input id='"+type+"Cabinet"+num+"' name='"+type+"Cabinet"+num+"' value='"+cabinet+"' type='hidden'  />";
	    
	  	td1.innerHTML = "";//textInput("Дата",type,"Date",num,resultRow[3],date,10) ;
	    td2.innerHTML = hiddenInput(type,"Service",num,resultRow[1],"")+spanTag("Исследование",resultRow[2],"");
	   	if (type=="lab") {
	   		td2.innerHTML += hiddenInput(type,"Department",num,resultRow[6],"")+spanTag("Место забора",resultRow[7],"") ;
	   		td2.innerHTML += hiddenInput(type,"Cabinet",num,cabinet,"");
	   		labNum = num;
	   	} else if (type=="func"){
		   	td2.innerHTML += hiddenInput(type,"Cabinet",num,resultRow[4],"")+spanTag("Кабинет",resultRow[5],"");
	   		td2.innerHTML += hiddenInput(type,"CalTime",num,resultRow[8],"")+spanTag("Время",resultRow[9],"") ;
	   		funcNum = num;
	   		$(type+'Cabinet').value='';
			$(type+'CabinetName').value='';
	   	} else if (type=='surg') {
	   		surgNum=num;
	   	}
	   	td3.innerHTML = "<input type='button' name='subm' onclick='var node=this.parentNode.parentNode;node.parentNode.removeChild(node);' value='Удалить' />";
	   	//new dateutil.DateField($(type+'Date'+num));
		
		$(type+'Servicies').value='';
		$(type+'ServiciesName').value='';
		if (aFocus) $(type+'ServiciesName').focus() ;
}
	function hiddenInput(aType,aFld,aNum,aValue,aDefaultValue) {
		return "<input id='"+aType+aFld+aNum+"' name='"+aType+aFld+aNum+"' value='"+(aValue==null||aValue==""?aDefaultValue:aValue)+"' type='hidden' />"
	}
	function textInput(aLabel,aType,aFld,aNum,aValue,aDefaultValue,aSize) {
		return "<span>"+aLabel+"</span><input "+(+aSize>0?"size="+aSize:"")+" id='"+aType+aFld+aNum+"' name='"+aType+aFld+aNum+"' value='"+(aValue==null||aValue==""?aDefaultValue:aValue)+"' type='text' />"
	}
	function spanTag(aText,aValue,aDefaultValue) {
		return "<span>"+aText+": <b>"+(aValue!=null&&aValue!=""?aValue.trim():aDefaultValue.trim())+"</b></span>. " ;
	}
	
	function addRow(type) { 
		if (type=='lab') {
			num = labNum;
		} else if (type=='func') {
			num = funcNum;
		}
		if (document.getElementById(type+'Servicies').value==""){
			alert("Выбирите услугу!");
			return;
		}
		
		// Проверим на дубли 
		var checkNum = 1;
		if (num>0){
			while (checkNum<=num) {
				if (document.getElementById(type+'Service'+checkNum)) {
					if ($(type+'Servicies').value==document.getElementById(type+'Service'+checkNum).value){
					//	if ($(type+'Date').value==document.getElementById(type+'Date'+checkNum).value) { 
							alert("Уже существует такое исследование!!!");
							return;
					//	} 
					}
				}
				checkNum+=1;
		}
		}
		
		num+=1;
	    // Считываем значения с формы 
	    
	    var nameId = document.getElementById(type+'Servicies').value;
 			var tbody = document.getElementById('add'+type+'Elements');
	    var row = document.createElement("TR");
		row.id = type+"Element"+num;
	    tbody.appendChild(row);
	
	    // Создаем ячейки в вышесозданной строке 
	    // и добавляем тх 
	    var td1 = document.createElement("TD");
	   	td1.colSpan="2";
	   	td1.align="right";
	    var td2 = document.createElement("TD");
	    td2.colSpan="2";
	    var td3 = document.createElement("TD");
	    var td4 = document.createElement("TD");
	    
		 row.appendChild(td1);
		 row.appendChild(td2);
		 row.appendChild(td3);
		 row.appendChild(td4);
	    // Наполняем ячейки 
	    var dt="<input id='"+type+"Service"+num+"' value='"+$(type+'Servicies').value+"' type='hidden' name='"+type+"Service"+num+"' horizontalFill='true' size='90' readonly='true' />";
	    var dt2="<input id='"+type+"Cabinet"+num+"' value='"+$(type+'Cabinet').value+"' type='hidden' name='"+type+"Cabinet"+num+"' horizontalFill='true' size='1' readonly='true' />";
	    var dt4 = "<input id='"+type+"Department"+num+"' value='"+$(type+'Department').value+"' type='hidden' name='"+type+"Department"+num+"' horizontalFill='true' size='1' readonly='true' />";
	    td2.innerHTML = dt+"<span>"+$(type+'ServiciesName').value+"</span>" ;
	  	td1.innerHTML = "<span>Дата: </span><input id='"+type+"Date"+num+"' name='"+type+"Date"+num+"' label='Дата' value='"+$(type+'Date').value+"' size = '10' />";
	   	//td2.innerHTML += dt2+"<span>. Кабинет: "+$(type+'CabinetName').value+"</span>" ;
	   	td3.innerHTML = "<input type='button' name='subm' onclick='var node=this.parentNode.parentNode;node.parentNode.removeChild(node);' value='-' />";
	   	new dateutil.DateField($(type+'Date'+num));
	   	td4.innerHTML=dt2+dt4;
					   
		if (type=='lab') {
			labNum = num;
		} else if (type=='func'){
			funcNum = num;
		}
	
	}
			</script>
			</msh:ifFormTypeIsNotView>
			</tiles:put>

  <tiles:put name="body" type="string">
    <!-- 
    	  - Назначение медицинской услуги
    	  -->
    <msh:form guid="formHello" action="/entityParentSaveGoView-pres_operationPrescription.do" defaultField="id" title="Назначение операции">
      <msh:hidden guid="hiddenId" property="id" />
      <msh:hidden property="prescriptionList" guid="8b852c-d5aa-40f0-a9f5-21dfgd6" />
      <msh:hidden guid="hiddenSaveType" property="saveType" />
      <msh:hidden property="labList" guid="ac31e2ce-8059-482b-b138-b441c42e4472" />
      <msh:panel guid="panel" colsWidth="3">  
         <msh:row guid="203a1bdd-8e88-4683-ad11-34692e44b66d">
          <msh:autoComplete property="prescriptSpecial" label="Назначил" size="100" vocName="workFunction" guid="c53e6f53-cc1b-44ec-967b-dc6ef09134fc" fieldColSpan="3" viewOnlyField="true" horizontalFill="true"  />
        </msh:row>
 <%-- --------------------------------------------------Начало блока "Операции" ------ --%>
         <msh:ifFormTypeIsCreate formName="pres_operationPrescriptionForm"> 
        <msh:panel styleId="tblSurgOperation">
       	 <msh:row>
        	<msh:separator label="Операции" colSpan="10"/>
        </msh:row>
        <msh:row>
        <tr><td>
        <table id="surgTable">
        <tbody id="addsurgElements">
    		<tr>
    			<msh:autoComplete property="surgServicies" label="Исследование" vocName="surgicalOperations" horizontalFill="true" size="90" />
    			<td>        	
	            	<input type="button" name="subm" onclick="prepareLabRow('surg');" value="Добавить" tabindex="4" />
	            </td>
			 </tr>
			 <tr>
				 <msh:autoComplete property="surgCabinet" label="Операционная"  fieldColSpan="3" vocName="operationRoom" size='20' horizontalFill="true" />
				 <%-- <msh:textField property="surgDate"  label="Дата" size="10"/> --%>
				 <msh:autoComplete property="surgCalDate" parentAutocomplete="surgCabinet" vocName="vocWorkCalendarDayByWorkFunction" label="Дата" size="10"/>
    			 <msh:autoComplete property="surgCalTime" parentAutocomplete="surgCalDate" label="Время" vocName="vocWorkCalendarTimeWorkCalendarDay"/>
			 </tr>
			 <tr>
			 <msh:textField property="comments" label="Примечание"/>
			 </tr>
			<msh:ifFormTypeIsNotView formName="pres_operationPrescriptionForm">
			</msh:ifFormTypeIsNotView>
           </tbody>
    		</table>
    		</td></tr></msh:row>
        </msh:panel>
        </msh:ifFormTypeIsCreate>
        <%-- -- --------------------------------------------------Конец блока "Операции" ------ --%>
        
        <msh:row>
        	<msh:separator label="Дополнительная информация" colSpan="4"/>
        </msh:row>
        <msh:row>
        	<msh:label property="createDate" label="Дата создания"/>
        	<msh:label property="createTime" label="время"/>
        </msh:row>
        <msh:row>
        	<msh:label property="createUsername" label="пользователь"/>
        </msh:row>
        <msh:row>
        	<msh:label property="editDate" label="Дата редактирования"/>
        	<msh:label property="editTime" label="время"/>
        </msh:row>
        <msh:row>
        	<msh:label property="editUsername" label="пользователь"/>
        </msh:row>                
        <msh:submitCancelButtonsRow guid="submitCancel" colSpan="4" />
      </msh:panel>
    </msh:form>
    <tags:enter_date name="2" functionSave="prepare1RowByDate"/>
    <msh:ifFormTypeIsView guid="ifFormTypeIsView" formName="pres_operationPrescriptionForm">
      <msh:section guid="sectionChilds" title="Исполнения">
        <ecom:parentEntityListAll guid="parentEntityListChilds" formName="pres_prescriptionFulfilmentForm" attribute="fulfilments" />
        <msh:table guid="tableChilds3434" name="fulfilments" action="entityParentView-pres_prescriptionFulfilment.do" idField="id">
          <msh:tableColumn columnName="Дата исполнения" property="fulfilDate" guid="8c2s9b-89d7-46a9-a8c3-c08527e" />
          <msh:tableColumn columnName="Время исполнения" property="fulfilTime" guid="d6dd49-a94d-4cf2-af1b-02581f" />
          <msh:tableColumn columnName="Исполнитель" property="executorInfo" guid="bfv1-0967-45f2-a6af-f654e" />
        </msh:table>
      </msh:section>
    </msh:ifFormTypeIsView>
    
  </tiles:put>
  <tiles:put name="title" type="string">
    <ecom:titleTrail guid="titleTrail-123" mainMenu="StacJournal" beginForm="pres_operationPrescriptionForm" />
  </tiles:put>
  <tiles:put name="side" type="string">
    <msh:ifFormTypeIsView formName="pres_operationPrescriptionForm" guid="99ca692-c1d3-4d79-bc37-c6726c">
      <msh:sideMenu title="Назначения" guid="eb3f54-b971-441e-9a90-51jhf">
        <msh:sideLink roles="/Policy/Mis/Prescription/ServicePrescription/Edit" params="id" action="/entityParentEdit-pres_servicePrescription" name="Изменить" guid="ca5sui7r-9239-47e3-aec4-995462584" key="ALT+2"/>
        <msh:sideLink confirm="Удалить?" roles="/Policy/Mis/Prescription/ServicePrescription/Delete" params="id" action="/entityParentDelete-pres_servicePrescription" name="Удалить" guid="ca5sui7r-9239-47e3-aec4-995462584" key="ALT+DEL"/>
      </msh:sideMenu>
      <msh:sideMenu title="Добавить" guid="0e2ac7-5361-434d-a8a7-1284796f">
    
        <msh:sideLink roles="/Policy/Mis/Prescription/PrescriptionFulfilment/Create" params="id" action="/entityParentPrepareCreate-pres_prescriptionFulfilment" name="Исполнение назначения" guid="ca3s9-9239-47e3-aec4-9a846547144" key="ALT+3"/>
      
      </msh:sideMenu>
      <msh:sideMenu title="Показать" guid="67aa758-3ad2-4e6f-a791-4839460955" >
        <msh:sideLink roles="/Policy/Mis/Prescription/ServicePrescription/View" params="id" action="/entityParentListRedirect-pres_servicePrescription" name="К списку назначений на услугу" guid="e9d94-fe74-4c43-85b1-267231ff" key="ALT+4"/>
        <msh:sideLink roles="/Policy/Mis/Prescription/View" params="id" action="/entityParentListRedirect-pres_prescription" name="К списку назначений" guid="e9d94-fe74-4c43-85b1-267231ff" key="ALT+4"/>
      </msh:sideMenu>
    </msh:ifFormTypeIsView>
    <msh:ifFormTypeIsCreate formName="pres_operationPrescriptionForm">
    <tags:templatePrescription record="2" parentId="${param.prescriptionList}" name="add" />
    <msh:sideMenu title="Шаблоны">
  			<msh:sideLink action=" javascript:showaddTemplatePrescription()" name="Назначения из шаблона" guid="a2f380f2-f499-49bf-b205-cdeba65f8888" title="Добавить назначения из шаблона" />
  		</msh:sideMenu>
  		
    </msh:ifFormTypeIsCreate>
  </tiles:put>
</tiles:insert>

