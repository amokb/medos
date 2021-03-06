<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://struts.apache.org/tags-tiles" prefix="tiles" %>
<%@ taglib uri="http://www.nuzmsh.ru/tags/msh" prefix="msh" %>
<%@ taglib uri="http://www.ecom-ast.ru/tags/ecom" prefix="ecom" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="tags" %>

<tiles:insert page="/WEB-INF/tiles/main${param.short}Layout.jsp" flush="true">
	<tiles:put name="body" type="string">
		<msh:form action="/entityParentSaveGoView-contract_priceGroup.do" defaultField="code">
			<msh:hidden property="id" />
			<msh:hidden property="saveType" />
			<msh:hidden property="priceList" />
			<msh:hidden property="parent" />
			<msh:panel>
			<msh:ifFormTypeIsNotView formName="contract_priceGroupForm">
				<msh:ifFormTypeAreViewOrEdit formName="contract_priceGroupForm">
				<msh:ifNotInRole roles="/Policy/Mis/Contract/PriceList/PriceGroup/EditParent">
					<msh:row>
						<msh:autoComplete parentId="contract_priceGroupForm.priceList" property="parent" vocName="priceGroup" horizontalFill="true" fieldColSpan="3" label="Родит.категория"/>
					</msh:row>
				</msh:ifNotInRole>
				</msh:ifFormTypeAreViewOrEdit>
			</msh:ifFormTypeIsNotView>
				<msh:row>
					<msh:autoComplete property="priceList" label="Прейскурант" fieldColSpan="3" horizontalFill="true" viewOnlyField="true"
						vocName="priceList" shortViewAction="entityView-contract_priceList.do?short=Short" viewAction="entityView-contract_priceList.do"/>
				</msh:row>
				<msh:row>
					<msh:textField property="code" label="Код"/>
				</msh:row>
				<msh:row>
					<msh:textField fieldColSpan="3" property="name" label="Название" horizontalFill="true" size="100"/>
				</msh:row>
				<msh:row>
					<msh:autoComplete fieldColSpan="3" property="lpu" label="Отделение" horizontalFill="true" size="100" vocName="lpu"/>
				</msh:row>
				<msh:row>
					<msh:checkBox property="isViewInfomat" label="Отображать на инфомате" fieldColSpan="3"/>
				</msh:row>				
				<msh:row>
					<msh:textArea property="comment" label="Комментарий" fieldColSpan="3"/>
				</msh:row>
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
				
			<msh:submitCancelButtonsRow colSpan="4" />
			</msh:panel>
		</msh:form>
		<msh:ifFormTypeIsView formName="contract_priceGroupForm">
			<msh:section title="Группы прейскуранта" createRoles="/Policy/Mis/Contract/PriceList/PriceGroup/Create" 
				createUrl="javascript:window.location=&quot;entityParentPrepareCreate-contract_priceGroup.do?id=${param.id}&priceList=&quot+$(&quot;priceList&quot;).value">
			<ecom:webQuery name="priceGroup" nativeSql="
							select pg.id,pg.code,pg.name,lpu.name as lpuname from PricePosition pg
							left join MisLpu lpu on lpu.id=pg.lpu_id 
							where pg.parent_id = '${param.id}' and pg.dtype='PriceGroup' order by pg.code" />
				<msh:table name="priceGroup" action="entityParentView-contract_priceGroup.do" idField="1">
					<msh:tableColumn columnName="#" property="sn"/>
					<msh:tableColumn columnName="Код" property="2"/>
					<msh:tableColumn columnName="Наименование" property="3"/>
					<msh:tableColumn columnName="Отделение" property="4"/>
				</msh:table>
			</msh:section>
			<msh:section title="Позиции прейскуранта" createRoles="/Policy/Mis/Contract/PriceList/PriceGroup/Create" 
				createUrl="entityParentPrepareCreate-contract_pricePosition.do?id=${param.id}">
			<ecom:webQuery name="pricePosition" nativeSql="
							select pp.id,pp.code,pp.name as ppname
							,vpt.name as vptname,pp.cost,pp.dateFrom,pp.dateTo 
							from PricePosition pp 
							left join VocPositionType vpt on vpt.id=pp.positionType_id
							where pp.parent_id = '${param.id}' and pp.dtype='PricePosition' ORDER BY pp.code" />
				<msh:table name="pricePosition" action="entityParentView-contract_pricePosition.do" idField="1">
					<msh:tableColumn columnName="#" property="sn"/>
					<msh:tableColumn columnName="Код" property="2"/>
					<msh:tableColumn columnName="Наименование" property="3"/>
					<msh:tableColumn columnName="Стоимость" property="5"/>
					<msh:tableColumn columnName="Дата начала" property="6"/>
					<msh:tableColumn columnName="Дата окончания" property="7"/>
				</msh:table>
			</msh:section>
		</msh:ifFormTypeIsView>
	</tiles:put>
	<tiles:put name="javascript" type="string">
	<msh:ifFormTypeIsCreate formName="contract_priceGroupForm">
		<script type="text/javascript">
			if (+$("priceList").value>0) {} else {
				if (+$("priceList").value>0){} else {
					$("priceList").value='${param.priceList}' ;
				}
			}
		</script>
		</msh:ifFormTypeIsCreate>
	</tiles:put>
	<tiles:put name="title" type="string">
		<ecom:titleTrail mainMenu="Contract" beginForm="contract_priceGroupForm" />
	</tiles:put>
	<tiles:put name="side" type="string">
		<msh:sideMenu>
			<msh:sideLink key="ALT+2" params="id" action="/entityParentEdit-contract_priceGroup" name="Изменить" title="Изменить" roles="/Policy/Mis/Contract/PriceList/PriceGroup/Edit"/>
			<msh:sideLink key="ALT+DEL" params="id" action="/entityParentDelete-contract_priceGroup" name="Удалить" title="Удалить" roles="/Policy/Mis/Contract/PriceList/PriceGroup/Delete"/>
		</msh:sideMenu>
		<msh:sideMenu title="Добавить" >
			<msh:sideLink key="ALT+N" params="id" action="/entityParentPrepareCreate-contract_pricePosition" name="Позицию" title="Позицию" roles="/Policy/Mis/Contract/PriceList/PricePosition/Create"/>
			<msh:sideLink params="id" action="/javascript:window.location='entityParentPrepareCreate-contract_priceGroup.do?id=${param.id}&priceList='+$('priceList').value" name="Вложенную группу" title="Вложенную группу" roles="/Policy/Mis/Contract/PriceList/PriceGroup/Create"/>
		</msh:sideMenu>
		<tags:contractMenu currentAction="price"/>
	</tiles:put>
</tiles:insert>
