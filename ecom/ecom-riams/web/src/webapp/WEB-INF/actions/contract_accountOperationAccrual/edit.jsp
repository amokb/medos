<%@page import="ru.ecom.ejb.services.query.WebQueryResult"%>
<%@page import="java.util.List"%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://struts.apache.org/tags-tiles" prefix="tiles" %>
<%@ taglib uri="http://www.nuzmsh.ru/tags/msh" prefix="msh" %>
<%@ taglib uri="http://www.ecom-ast.ru/tags/ecom" prefix="ecom" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="tags" %>

<tiles:insert page="/WEB-INF/tiles/main${param.short}Layout.jsp" flush="true">
	<tiles:put name="body" type="string">
		<msh:form action="/entityParentSaveGoParentView-contract_accountOperationAccrual.do" defaultField="discount">
			<msh:hidden property="id" />
			<msh:hidden property="saveType" />
			<msh:hidden property="account" />
			<msh:hidden property="createDate" />
			<msh:hidden property="createTime" />
			<msh:hidden property="createUsername" />
			<msh:hidden property="editDate" />
			<msh:hidden property="editTime" />
			<msh:hidden property="editUsername" />
			<msh:hidden property="workFunction"/>
			<msh:hidden property="medServicies"/>
			<msh:panel colsWidth="10%,10%,10%,10%">
				<msh:row>
					<msh:textField viewOnlyField="true" property="cost" label="Стоимость"/>
					
				
					<msh:textField property="discount" label="Скидка"/>
					<td align="left" ">
						<msh:ifFormTypeIsCreate formName="contract_accountOperationAccrualForm">
							<span onclick="$('discount').value='10';getCostInfo();">10% установить</span>
						</msh:ifFormTypeIsCreate>
					</td>
					
				</msh:row>
				<msh:row>
					<msh:checkBox property="isPaymentTerminal" labelColSpan="2" label="Оплата терминалом"/>
				</msh:row>
				<msh:ifFormTypeIsCreate formName="contract_accountOperationAccrualForm">
				<msh:row>
					<td align="left" colspan="5">К оплате: <span id='costInfo'></<span></td>
				</msh:row>
				<msh:row><td colspan="4">
					Получено от клиента   <input type="text" id="cashCount" name="cashCount">
				</td></msh:row>
				<msh:row><td colspan="4">
					Сдача <span style="font-size:20px;" id="cashGiveBackCount"></span>
				</td></msh:row>
				</msh:ifFormTypeIsCreate>
				<ecom:webQuery name="sumCost" nativeSql="
							select sum(CAMS.countMedService*CAMS.cost) as sumaccrual,list(''||cams.id) as lstid ,min(ca.discountDefault) as discount   
							from ContractAccountMedService CAMS
							left join ContractAccount ca on ca.id=cams.account_id
							where ca.id= '${param.id }'
				"/>
				<msh:ifInRole roles="/Policy/Mis/Contract/MedContract/ServedPerson/ContractAccount/ContractAccountOperation/EditOperationInfo">
					<msh:ifFormTypeIsNotView formName="contract_accountOperationAccrualForm">
						<msh:row>
							<msh:textField property="operationDate" label="Дата операции"/>
							<msh:textField property="operationTime" label="Время"/>
						</msh:row>
					</msh:ifFormTypeIsNotView>
				</msh:ifInRole>
				<msh:ifNotInRole roles="/Policy/Mis/Contract/MedContract/ServedPerson/ContractAccount/ContractAccountOperation/EditOperationInfo">
					<msh:hidden property="operationDate" />
					<msh:hidden property="operationTime" />
				</msh:ifNotInRole>
				<msh:ifFormTypeIsView formName="contract_accountOperationAccrualForm">
				<msh:row>
					<msh:textField property="operationDate" label="Дата операции"/>
					<msh:textField property="operationTime" label="Время"/>
				</msh:row>
					<msh:row>
						<msh:autoComplete property="workFunction" label="Оператор" vocName="workFunction" fieldColSpan="3" size="100" />
					</msh:row>
				<msh:row>
					<msh:separator label="Информация о создании" colSpan="5"/>
				</msh:row>
				<msh:row>
					<msh:textField property="createDate" label="Дата"/>
					<msh:textField property="createTime" label="Время"/>
				</msh:row>
				<msh:row>
					<msh:textField property="createUsername" label="Пользователь"/>
				</msh:row>
				<msh:row>
					<msh:separator label="Информация о последней редакции" colSpan="5"/>
				</msh:row>
				<msh:row>
					<msh:textField property="editDate" label="Дата"/>
					<msh:textField property="editTime" label="Время"/>
				</msh:row>
				<msh:row>
					<msh:textField property="editUsername" label="Пользователь"/>
				</msh:row>
				</msh:ifFormTypeIsView>
			<msh:submitCancelButtonsRow colSpan="3" />
			</msh:panel>
		</msh:form>
		<msh:ifFormTypeIsView formName="contract_accountOperationAccrualForm">
					<msh:ifInRole roles="/Policy/Mis/Contract/MedContract/ServedPerson/ContractAccount/MedService/View">
			<msh:section >
			<msh:sectionTitle>Медицинские услуги</msh:sectionTitle>
			<msh:sectionContent>
			<ecom:webQuery name="medicalService" nativeSql="
select cams.id, pp.code,pp.name,cams.cost,cams.countMedService 
	, cams.countMedService*cams.cost as sumNoAccraulMedService 
	, cao.discount,round(cams.countMedService*(cams.cost*(100-coalesce(cao.discount,0))/100),2),case when cao.repealOperation_id is not null then 'Возврат' else null end as commentReturn
			from ContractAccountMedService cams
			left join ServedPerson sp on cams.servedPerson_id = sp.id
			left join ContractAccountOperationByService caos on caos.accountMedService_id=cams.id
			left join ContractAccountOperation cao on cao.id=caos.accountOperation_id and cao.dtype='OperationAccrual'
			left join ContractPerson cp on cp.id=sp.person_id 
			left join patient p on p.id=cp.patient_id
						
			left join PriceMedService pms on pms.id=cams.medService_id
			left join PricePosition pp on pp.id=pms.pricePosition_id
			where cao.id='${param.id}'
			"/>
				
				<msh:table name="medicalService" 
				action="entityParentView-contract_accountMedService.do"
				
				 idField="1">
					 <msh:tableNotEmpty>
				 <msh:ifInRole roles="/Policy/Mis/Contract/MedContract/ServedPerson/ContractAccount/ContractAccountOperation">
					 	<a href="javascript:void()" onclick="javascript:makeKKMPaymentOrRefund('makeRefund')">Оформить возврат всей суммы</a>
				 </msh:ifInRole>
					 </msh:tableNotEmpty>
					<msh:tableColumn columnName="Код" property="2" />
					<msh:tableColumn columnName="Наименование" property="3" />
					<msh:tableColumn columnName="Тариф" property="4" />
					<msh:tableColumn columnName="Общ. кол-во" property="5" />
					<msh:tableColumn columnName="Стоимость" isCalcAmount="true" property="6" />
					<msh:tableColumn columnName="Скидка" property="7" />
					<msh:tableColumn columnName="Оплачено" isCalcAmount="true" property="8" />
					<msh:tableColumn columnName="Комментарий" property="9" />
					
				</msh:table>
				</msh:sectionContent>
			</msh:section>
			</msh:ifInRole>
		</msh:ifFormTypeIsView>
		<script type="text/javascript" src="./dwr/interface/ContractService.js"></script>
		<script type="text/javascript">
			var createType=0 ;
			var oldaction = "";
			
			function makeKKMPaymentOrRefund(aFunction) {
				ContractService.sendKKMRequest(aFunction, $('account').value,$('discount').value, $('isPaymentTerminal').checked
						, {
					callback: function (a) {
						if (a!=null&&a!="") {
							alert (""+a);
						}
						if (aFunction=="makePayment") {
					document.forms['contract_accountOperationAccrualForm'].action=oldaction ;
					document.forms['contract_accountOperationAccrualForm'].submit();
				}else {
					window.location = "js-contract_medContract-issueRefund.do?id=${param.id}";
				}		
					}
				}
				);
				
				
			}
			
		</script>
		<msh:ifFormTypeIsCreate formName="contract_accountOperationAccrualForm">
		
		<script type="text/javascript">
		oldaction = document.forms['contract_accountOperationAccrualForm'].action ;
		document.forms['contract_accountOperationAccrualForm'].action="javascript:makeKKMPaymentOrRefund('makePayment')";
		createType=1;
	
		function getCostInfo() {
			var cost = +$('cost').value ;
			var discount = +$('discount').value ;
			$('costInfo').innerHTML=cost*(100-discount)/100 ;
			getGiveBack ();
		}
		    eventutil.addEventListener($('discount'),'change',function(){getCostInfo() ;});
            eventutil.addEventListener($('discount'),'keyup',function(){getCostInfo() ;});
            eventutil.addEventListener($('cashCount'),'keyup',function(){getGiveBack () ;});
           
            function getGiveBack (){
           var cash = +$('cashCount').value;
           var totalPrice = +$('costInfo').innerHTML;
           var giveBack = cash-totalPrice;
           if (cash&& cash>0) {
        	   try {
        		   if (giveBack>0) {
        			   $('cashGiveBackCount').innerHTML ="<b> "+ (cash-totalPrice) +" руб</b>";   
        		   } else {
        			   $('cashGiveBackCount').innerHTML ="<b> Нужно больше денег!</b>";
        		   }
           		
           	} catch (e) {
           		$('cashGiveBackCount').innerHTML ="Непонятное число";
           	}
           } 
            	
            }

		
		</script>
		</msh:ifFormTypeIsCreate>		
		<% 
		List list= (List)request.getAttribute("sumCost");
		out.println("<script type='text/javascript'> if (createType>0){");
		//out.println("var sum = 0;list");
		for (int i=0 ; i<list.size();i++) {
			WebQueryResult res = (WebQueryResult)list.get(i) ;
			out.println("$('cost').value = '"+res.get1()+"';"); 
			out.println("$('costReadOnly').value = '"+res.get1()+"';"); 
			out.println("$('medServicies').value = '"+res.get2()+"';"); 
			out.println("if (+'"+res.get3()+"'>0) {"); 
			out.println("$('discount').value = +'"+res.get3()+"';}getCostInfo();"); 
			
		}
		out.println("}</script>");
		
		%>
			

	</tiles:put>
	<tiles:put name="title" type="string">
		<ecom:titleTrail mainMenu="Contract" beginForm="contract_accountOperationAccrualForm" />
	</tiles:put>
	<tiles:put name="side" type="string">
		<msh:ifFormTypeAreViewOrEdit formName="contract_accountOperationAccrualForm">
			<msh:sideMenu>
				<msh:sideLink key="ALT+2" params="id" action="/entityParentEdit-contract_accountOperation" name="Изменить" title="Изменить" roles="/Policy/Mis/Contract/MedContract/ServedPerson/ContractAccount/ContractAccountOperation/Edit"/>
				<msh:sideLink key="ALT+DEL" confirm="Вы точно хотите удалить начисление?" params="id" action="/entityParentDeleteGoParentView-contract_accountOperation" name="Удалить" title="Удалить" roles="/Policy/Mis/Contract/MedContract/ServedPerson/ContractAccount/ContractAccountOperation/Delete"/>
			</msh:sideMenu>
			<tags:contractMenu currentAction="medContract"/>
		</msh:ifFormTypeAreViewOrEdit>
	</tiles:put>
</tiles:insert>
