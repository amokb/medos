<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://struts.apache.org/tags-tiles" prefix="tiles" %>
<%@ taglib uri="http://www.nuzmsh.ru/tags/msh" prefix="msh" %>
<%@ taglib uri="http://www.ecom-ast.ru/tags/ecom" prefix="ecom" %>

<tiles:insert page="/WEB-INF/tiles/mainLayout.jsp" flush="true">

  <tiles:put name="body" type="string">
    <!-- 
    	  - Проба
    	  -->
    <msh:form action="/entitySaveGoView-ecom_hello.do" defaultField="hello">
      <msh:hidden property="id" />
      <msh:hidden property="saveType" />
      <msh:hidden property="parent" />
      <msh:panel>
        <msh:row>
          <msh:textField property="hello" label="Проба" />
          <msh:textField property="parent" label="Тест2" />
        </msh:row>
        <msh:row>
          <msh:autoComplete viewAction="entityView-ecom_hello.do" vocName="vocHello" property="link" horizontalFill="true" fieldColSpan="3" />
        </msh:row>
        <msh:submitCancelButtonsRow colSpan="4" />
      </msh:panel>
    </msh:form>
    <msh:ifFormTypeIsView formName="ecom_helloForm">
      <msh:section title="Потомки">
        <ecom:parentEntityListAll formName="ecom_helloForm" attribute="childs" />
        <msh:table name="childs" action="entityParentView-ecom_hello.do" idField="id">
          <msh:tableColumn columnName="ИД" property="id" />
          <msh:tableColumn columnName="Проба" property="hello" />
          <msh:tableColumn columnName="Родитель" property="parent" />
        </msh:table>
      </msh:section>
    </msh:ifFormTypeIsView>
  </tiles:put>
  <tiles:put name="title" type="string">
    <ecom:titleTrail mainMenu="Patient" beginForm="ecom_helloForm" />
  </tiles:put>
  <tiles:put name="side" type="string">
    <msh:sideMenu title="Проба">
      <msh:sideLink key="ALT+2" params="id" action="/entityEdit-ecom_hello" name="Изменить" roles="/Policy/IdeMode/Hello/Edit" />
      <msh:sideLink key="ALT+N" params="id" action="/entityParentPrepareCreate-ecom_hello" name="Потомка" roles="/Policy/IdeMode/Hello/Create" title="Добавить потомка" />
      <msh:sideLink key="ALT+DEL" confirm="Удалить?" params="id" action="/entityDelete-ecom_hello" name="Удалить" roles="/Policy/IdeMode/Hello/Delete" />
    </msh:sideMenu>
    <msh:sideMenu title="Добавить" />
  </tiles:put>
</tiles:insert>

