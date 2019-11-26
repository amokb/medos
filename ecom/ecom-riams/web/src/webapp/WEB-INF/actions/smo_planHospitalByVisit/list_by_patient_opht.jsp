<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib uri="http://struts.apache.org/tags-tiles" prefix="tiles" %>
<%@ taglib uri="http://www.nuzmsh.ru/tags/msh" prefix="msh" %>
<%@ taglib uri="http://www.ecom-ast.ru/tags/ecom" prefix="ecom" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="tags" %>

<tiles:insert page="/WEB-INF/tiles/main${param.short}Layout.jsp" flush="true" >

    <tiles:put name="title" type="string">
        <msh:title guid="helloItle-123" mainMenu="StacJournal" title="" />
    </tiles:put>
    <tiles:put name="side" type="string">

    </tiles:put>
    <tiles:put name="body" type="string">
        <msh:section title="Планирования введения ингибиторов ангиогенеза пациента">
            <msh:sectionContent>
                <ecom:webQuery name="list" nameFldSql="list_sql" nativeSql="
                   select wct.id as id
                    ,to_char(wct.dateokt,'dd.mm.yyyy') as dateokt
                    ,e.name as eye
                    ,to_char(wct.datefrom,'dd.mm.yyyy') as dateFrom
                    ,wct.comment as cmnt
                    ,wct.createusername as creator
                    ,to_char(wct.createdate,'dd.mm.yyyy')||' '||to_char(wct.createTime,'HH24:MI') as dt
                    from WorkCalendarHospitalBed wct
                    left join patient pat on wct.patient_id=pat.id
                    left join voceye e on e.id=wct.eye_id
                    where pat.id=${param.patient}
                    order by wct.dateFrom
      "/>
                <msh:table name="list" action="entityView-stac_planOphtHospital.do" idField="1" noDataMessage="Не найдено">
                    <msh:tableColumn columnName="#" property="sn" />
                    <msh:tableColumn columnName="Дата ОКТ" property="2" guid="5905cf65-048f-4ce1-8301-5aef1e9ac80e" />
                    <msh:tableColumn columnName="Глаз" property="3" guid="2bab495e-eadb-4cd9-b2e9-140bf7a5f43f" />
                    <msh:tableColumn columnName="Дата планируемой госпитализации" property="4" guid="6682eeef-105f-43a0-be61-30a865f27972" />
                    <msh:tableColumn columnName="Замечания" property="5" guid="f34e1b12-3392-4978-b31f-5e54ff2e45bd" />
                </msh:table>
            </msh:sectionContent>
        </msh:section>
    </tiles:put>
</tiles:insert>