<%@ tag pageEncoding="utf8" %>
<%@ taglib uri="http://www.nuzmsh.ru/tags/msh" prefix="msh" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="tags" %>
<%@ attribute name="name" description="название" required="true"%>
<%@ attribute name="property" description="свойство, куда записывать данные"  required="true" %>
<%@ attribute name="patient" description="свойство пациента"  required="true" %>
<%@ attribute name="dateFinish" description="свойство даты выписки из стационара СЛС" %>
<%@ attribute name="dateStart" description="свойство даты поступления СЛС"  required="true"%>

<style type="text/css">
    #${name}EpicrisisDialog {
        visibility: hidden ;
        display: none ;
        position: absolute ;
    }
</style>

<div id='${name}EpicrisisDialog' class='dialog'>
    <h2>Выбор параметров</h2>
    <div class='rootPane'>
		<form name="${name}EpicrisisParameterForm">
		    <msh:panel>
		    	<msh:ifInRole roles="/Policy/Mis/MedCase/Document/External/Medservice/View">
					<input type="hidden" name="${name}ExtLabsDep" id="${name}ExtLabsDep" value="0" />
					<tr onclick='show${name}DiariesDiv()'>
						<msh:checkBox property="${name}Diaries" label="Протоколы и исследования" fieldColSpan="2" />
					</tr>
					<tr onclick="show${name}DiariesDiv()">
			        	<msh:checkBox property="${name}ExtLabs" label="Только лабораторные исследования" fieldColSpan="2"/>
					</tr>
				<msh:row>
		            	<td></td>
				        <td onclick="format${name}Services();">
				        	<label> <input type="checkbox" id="${name}ExtLabsReg" /> Перевести в ниж.регистр</label>
				        </td>
			        </msh:row>
		            <msh:row>
		            	<td></td>
				        <td onclick="format${name}Services();">
							<label><input type="checkbox" name="${name}ExtLabsStr" id="${name}ExtLabsStr" checked="checked" > Преобразовать в одну строку</label>
				        </td>
			        </msh:row>
		            <msh:row>
		                <td></td>
				        <td onclick="this.childNodes[1].checked='checked';format${name}Services();">
				        	<input type="radio" name="${name}ExtLabsCode" value="0"> Показывать код и название услуги
				        </td>
				        <td onclick="this.childNodes[1].checked='checked';format${name}Services();">
				        	<input type="radio" name="${name}ExtLabsCode" checked="checked" value="1"> Показывать название услуги
				        </td>
				        <td onclick="this.childNodes[1].checked='checked';format${name}Services();">
				        	<input type="radio" name="${name}ExtLabsCode"  value="2"> Не показывать услугу
				        </td>
					</msh:row>
		            <msh:row>
		                <td></td>
				        <td onclick=" format${name}Services();">
				        	<label><input type="checkbox" name="${name}ExtLabsNoIntake" id="${name}ExtLabsNoIntake" checked="checked"> Убрать инф. о заборе</label>
				        </td>
					</msh:row>
		            <msh:row>
		                <td></td>
				        <td onclick=" format${name}Services();">
				        	<label><input type="checkbox" name="${name}ExtLabsDate" id="${name}ExtLabsDate" > Добавить дату выполнения услуги</label>
				        </td>
					</msh:row>
		    	</msh:ifInRole>
			        
		    	<msh:ifInRole roles="/Policy/Mis/MisLpu/IsNuzMsch">

		            <msh:row>
			        	<msh:checkBox property="${name}Labs" label="Лабораторные исследования (DTM)" fieldColSpan="2"/>
			        </msh:row>
		            <msh:row>
			        	<msh:checkBox property="${name}Fisio" label="Физио (DTM)" fieldColSpan="2"/>
			        </msh:row>
		            <msh:row>
			        	<msh:checkBox property="${name}Func" label="Функцион.диагностика (DTM)" fieldColSpan="2"/>
			        </msh:row>
		            <msh:row>
			        	<msh:checkBox property="${name}Cons" label="Консультации (DTM)" fieldColSpan="2"/>
			        </msh:row>
		            <msh:row>
			        	<msh:checkBox property="${name}Luch" label="Лучевая диагностика (DTM)" fieldColSpan="2"/>
			        </msh:row>
		        </msh:ifInRole>
		        <msh:row>
		            <msh:checkBox property="${name}DefaultInfo" label="Общие сведения о пациенте" fieldColSpan="2"/>
		        </msh:row>
		        <msh:row>
		            <msh:checkBox property="${name}Operations" label="Операции" fieldColSpan="2"/>
		        </msh:row>
				<msh:row>
					<td colspan="6">
						<input type="button" name="${name}EpicrisisOk" id='${name}EpicrisisOk' value='OK' onclick='save${name}Epicrisis()'/>
						<input type="button" value='Отменить' onclick='cancel${name}Epicrisis()'/>
					</td>
				</msh:row>
		        <msh:row><td colspan="5">
		        <div id='${name}diariesDiv' style='display:none'>
		        </div></td>
		        </msh:row>
		    </msh:panel>
		        <msh:row>
		            <td colspan="6">
		                <input type="button" name="${name}EpicrisisOk" id='${name}EpicrisisOk' value='OK' onclick='save${name}Epicrisis()'/>
		                <input type="button" value='Отменить' onclick='cancel${name}Epicrisis()'/>
		            </td>
		        </msh:row>
		</form>
		
		</div>
</div>
<%--<script type='text/javascript' src='./dwr/interface/EpicrisisService.js'></script> --%>
<script type="text/javascript">
     var theIs${name}EpicrisisDialogInitialized = false ;
     var the${name}EpicrisisDialog = new msh.widget.Dialog($('${name}EpicrisisDialog')) ;
	function escapeHtml(aText, lowerCase, oneString, noIntake) {
	    if (true==lowerCase) aText = aText.toLowerCase();
	    if (true==noIntake) aText = aText.replace(/([Зз]абор)[абор имтелпзвдн:]*\d{2}.\d{2}.\d{4} \d{2}:\d{2}/g,'');
		aText = aText.replace(/&/g, '&amp;')
            .replace(/>/g, '&gt;')
            .replace(/</g, '&lt;')
            .replace(/"/g, '&quot;');
		aText = aText.replace(/[\r\n]/g,false==oneString ? '<br>' :' ');
		return aText;

	}
     function unEscapeHtml(aText) {
		aText = aText.replace(/<br>/g,'\r');
         return aText.replace('&amp;',/&/g)
             .replace( '&gt;',/>/g)
             .replace('&lt;',/</g)
             .replace('&quot;',/"/g);
     }
     //Отображаем лаб. исследования в нужном виде
	 var servicesList =[]; // будем хранить дневники здесь

	 function format${name}Services() {
	 	if (($('${name}Diaries').checked || $('${name}ExtLabs').checked )) {
			var p = '';
			p+='<table border=\'1\' ><tr> <td></td><td>Дата</td><td>Дневник</td></tr><tbody id=\'diariesTable\'>';
			//формирование дневников лаборатории
			var showService = jQuery('[name=${name}ExtLabsCode]:checked').val()=='0';
			var showServiceName = jQuery('[name=${name}ExtLabsCode]:checked').val()!='2';

			var makeOneString = jQuery('#${name}ExtLabsStr').is(':checked');
			var lowerCase = jQuery('#${name}ExtLabsReg').is(':checked');
			var noIntake = jQuery('#${name}ExtLabsNoIntake').is(':checked');
			var addDate = jQuery('#${name}ExtLabsDate').is(':checked');
			p+="<tr><td colspan='3'><label><input type='checkbox' onclick='checkAllServices(this.checked)'>Отметить все</label></td></tr>";
			for (var i=0;i<servicesList.length;i++){
				var diary = servicesList[i];
				p+='<tr>';
				p+='<td><input type=\'checkbox\' class=\'diary${name}Checkbox\' name=\'diary'+diary.id+'\'></td>';
				p+='<td>'+diary.recordDate+" "+diary.recordTime+'</td>';
				p+='<td id='+diary.id+'>'+escapeHtml(
						(true==addDate ? diary.recordDate+" " : "") +
						(true==showService ? diary.serviceCode+" " : "")
						+(true==showServiceName ? diary.serviceName : "")+" "+diary.recordText
						,lowerCase,makeOneString, noIntake)+'</td>';
				p+='</tr>';
			}
			p+='</tbody></table>'

			$('${name}diariesDiv').style.display='block';
			$('${name}diariesDiv').innerHTML=p;
		}
	 }
	 function checkAllServices(isShow) {
	 	jQuery('.diary${name}Checkbox').prop('checked',isShow );

	 }

     function show${name}DiariesDiv() {
		 if ($('${name}Diaries').checked || $('${name}ExtLabs').checked ){
				HospitalMedCaseService.getDiariesByHospital($('id').value, $('${name}ExtLabs').checked ? "LABSURVEY" : null ,{
					callback: function (aResult) {
						servicesList = JSON.parse(aResult);
						the${name}EpicrisisDialog.hide() ;
							format${name}Services();
						the${name}EpicrisisDialog.show() ;
					}});
			 } else {
			 	$('${name}diariesDiv').style.display='none';
		 }
     }
     // Показать
     function show${name}Epicrisis() {
         // устанавливается инициализация для диалогового окна
         if (!theIs${name}EpicrisisDialogInitialized) {
         	init${name}Epicrisis() ;

         } 
         
         the${name}EpicrisisDialog.show() ;
		if ($('${name}Labs')) {
			$("${name}Labs").focus() ;
			$("${name}Labs").select() ;
		} else {
			$("${name}Operations").focus() ;
			$("${name}Operations").select() ;
		}
         //setFocusOnField('${name}tempProtCategoryName' );

     }

     // Отмена
     function cancel${name}Epicrisis() {
         the${name}EpicrisisDialog.hide() ;
     }

     // Сохранение данных
     function save${name}Epicrisis() {
		get${name}LabsInfo() ;
     }
     function get${name}LabsInfo() {
		 if ($('${name}Labs')&& ($('${name}Labs').checked)||$('${name}Fisio')&& ($('${name}Fisio').checked)
			||$('${name}Func')&& ($('${name}Func').checked) ||$('${name}Cons')&& ($('${name}Cons').checked)||$('${name}Luch')&& ($('${name}Luch').checked)) {
			HospitalMedCaseService.getLabInvestigations($('${patient}').value,$('${dateStart}').value
				,$('${dateFinish}').value, $('${name}Labs').checked,
				$('${name}Fisio').checked,$('${name}Func').checked,
				$('${name}Cons').checked, $('${name}Luch').checked
				 , {
					callback: function(aString) {
						$('${property}').value += "\n" ;
						$('${property}').value += aString ;
						get${name}OperationsInfo() ;
					}
			} ) ;
		} else { 
			get${name}OperationsInfo() ;
     	 }
         //Milamesher localStorage
         if (document.getElementsByName("stac_sslDischargeForm") !=null) {
             try {
                 localStorage.setItem("stac_sslDischargeForm"+";"+medCaseId.value+";"+document.getElementById('current_username_li').innerHTML, $('dischargeEpicrisis').value);
             }
             catch (e) {}
         }
     }
     function get${name}OperationsInfo() {
     	if ($('${name}Operations').checked) {
			HospitalMedCaseService.getOperations($('${patient}').value,$('${dateStart}').value
				,$('${dateFinish}').value, {
					callback: function(aString) {
						$('${property}').value += "\n" ;
						$('${property}').value += aString ;
						//get${name}ExpMedserviceInfo() ;
						get${name}DiariesInfo() ;
					}
			} ) ;
		} else {
			//get${name}ExpMedserviceInfo() ;
			get${name}DiariesInfo() ;
		}
     }

     function get${name}ExpMedserviceInfo() {
     	//Отображение внешних мед. лаб. исследований не искольуется
    /*  	if ($('${name}ExtLabs')&& $('${name}ExtLabs').checked) {
      		var frm = document.forms["${name}EpicrisisParameterForm"]
      		var reg = getCheckedRadio(frm,"${name}ExtLabsReg") ;
      		var str = getCheckedRadio(frm,"${name}ExtLabsStr") ;
      		var dep = getCheckedRadio(frm,"${name}ExtLabsDep") ;
 			HospitalMedCaseService.getExpMedservices(dep, reg, str, $('${patient}').value,$('${dateStart}').value
 				,$('${dateFinish}').value, {
 					callback: function(aString) {
 						$('${property}').value += "\n" ;
 						$('${property}').value += aString ;
 						get${name}DiariesInfo() ;
 						
 					}
 			} ) ;
 		} else {
 			get${name}DiariesInfo() ;
 		} */
      }
     
     function get${name}DiariesInfo() {
    	 if ($('diariesTable')) {
    		 var res = '';
    		 var rows = $('diariesTable').childNodes;
    		 for (var i=0;i<rows.length;i++){
    			 var td = rows[i].childNodes;
    			// alert ('tDD = '+td[2].innerHTML)
    			 if (td[0].childNodes[0].checked){
    			 	res+=unEscapeHtml(td[2].innerHTML)+'\n\n';
    			 }
    		 }
    		 $('${property}').value+=res;
    	 }
    		 get${name}DefaultInfo() ;
     }
     
     function get${name}DefaultInfo() {
       	if ($('${name}DefaultInfo').checked) {
      		var frm = document.forms["${name}EpicrisisParameterForm"]
      		var reg = getCheckedRadio(frm,"${name}DefaultInfo") ;
 			HospitalMedCaseService.getPatientDefaultInfo($('${patient}').value,$('${dateStart}').value
 				,$('${dateFinish}').value, {
 					callback: function(aString) {
 						$('${property}').value = aString +"\n"+$('${property}').value;
 						the${name}EpicrisisDialog.hide() ;
 						$('${property}').focus() ;
 						
 					}
 			} ) ;
 		} else {
 			the${name}EpicrisisDialog.hide() ;
 			$('${property}').focus() ;
 		}
      }
     // инициализация диалогового окна выбора шаблона протокола
     function init${name}Epicrisis() {
             <%--
             $('${name}textTemplProtocol').readOnly=true ;
             ${name}tempProtCategoryAutocomplete.addOnChangeCallback(function() {
             	 ${name}tempProtocolAutocomplete.setVocId("");
             	 $('${name}textTemplProtocol').value = "" ;
             }) ;
             ${name}tempProtocolAutocomplete.addOnChangeCallback(function() {
                 EpicrisisService.getText($('${name}tempProtocol').value, {
                    callback: function(aString) {
                        $('${name}textTemplProtocol').value = aString ;
                     }
                 } ) ;
             }) ;

             eventutil.addEnterSupport('${name}tempProtocolName', 'buttonTempProtOk') ;
             --%>
             theIs${name}EpicrisisDialogInitialized = true ;
             
             
     }
</script>
