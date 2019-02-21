<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://struts.apache.org/tags-tiles" prefix="tiles" %>
<%@ taglib uri="http://www.nuzmsh.ru/tags/msh" prefix="msh" %>
<%@ taglib uri="http://www.ecom-ast.ru/tags/ecom" prefix="ecom" %>

<tiles:insert page="/WEB-INF/tiles/mainShortLayout.jsp" flush="true" >

  <tiles:put name="title" type="string">
    <msh:title guid="helloItle-123" mainMenu="Patient">Пред. запись на день</msh:title>
  </tiles:put>
  <tiles:put name="body" type="string">
    <ecom:webQuery name="predirectList" nameFldSql="qqq" nativeSql="select wct.timefrom, coalesce(pat.patientinfo, wct.prepatientinfo)
      from workCalendarTime wct left join patient pat on pat.id=wct.prepatient_id
      where wct.workcalendarday_id=${param.id}  and (wct.isdeleted is null or wct.isdeleted='0') order by wct.timeFrom"/>
    <msh:table name="predirectList" action="/javascript:void()" idField="1">
      <msh:tableColumn columnName="Время приема" property="1" />
      <msh:tableColumn columnName="Пациент" property="2" />
    </msh:table>
  </tiles:put>
</tiles:insert>

