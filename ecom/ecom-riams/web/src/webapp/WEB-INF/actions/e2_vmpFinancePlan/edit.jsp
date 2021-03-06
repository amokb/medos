<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib uri="http://struts.apache.org/tags-tiles" prefix="tiles" %>
<%@ taglib uri="http://www.nuzmsh.ru/tags/msh" prefix="msh" %>
<%@ taglib uri="http://www.ecom-ast.ru/tags/ecom" prefix="ecom" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="tags" %>

<tiles:insert page="/WEB-INF/tiles/mainLayout.jsp" flush="true">
    <tiles:put name="title" type="string">
        <ecom:titleTrail mainMenu="Expert2" beginForm="e2_vmpFinancePlanForm" />
    </tiles:put>
    <tiles:put name="body" type="string">
        <msh:ifFormTypeIsView formName="e2_vmpFinancePlanForm">
        </msh:ifFormTypeIsView>
        <msh:form action="/entitySaveGoView-e2_vmpFinancePlan.do" defaultField="name">
            <msh:hidden property="id" />
            <msh:hidden property="saveType" />
            <msh:panel>
                <msh:separator colSpan="8" label="Общие"/>
                <msh:row>
                </msh:row><msh:row>
                <msh:textField property="startDate"/>
                <msh:textField property="finishDate"/>
            </msh:row>
                <msh:row>
                    <msh:autoComplete property="vidSluch" vocName="vocE2VidSluch" label="Вид случая" size="50"/>
                </msh:row>
                <msh:row>
                    <msh:autoComplete property="bedProfile" vocName="vocE2FondV020" label="Профиль коек" size="50"/>
                </msh:row>
                <msh:row>
                <msh:autoComplete property="profile" vocName="vocE2MedHelpProfile" size="100"/>
            </msh:row>
                <msh:row>
                <msh:autoComplete property="department" vocName="lpu" size="100"/>
            </msh:row>
                <msh:row>
                    <msh:autoComplete property="bedSubType" vocName="vocBedSubType" size="100"/>
                </msh:row>
                <msh:row>
                    <msh:autoComplete property="method" vocName="vocMethodHighCare" size="100"/>
                </msh:row>
                <msh:row>
                    <msh:textField property="count" />
                    <msh:textField property="cost" />
                </msh:row>

                <msh:submitCancelButtonsRow colSpan="4" />

            </msh:panel>
        </msh:form>
    </tiles:put>


    <tiles:put name="side" type="string">
        <msh:ifFormTypeIsView formName="e2_vmpFinancePlanForm">
            <msh:sideMenu>
                <msh:sideLink key="ALT+2" params="id" action="/entityEdit-e2_vmpFinancePlan" name="Изменить" roles="/Policy/E2/Edit" />
                <msh:sideLink params="id" action="/entityDelete-e2_vmpFinancePlan" name="Удалить" roles="/Policy/E2/View" />
            </msh:sideMenu>
        </msh:ifFormTypeIsView>
    </tiles:put>
    <tiles:put name="javascript" type="string">
        <msh:ifFormTypeIsView formName="e2_vmpFinancePlanForm">

            <script type="text/javascript">
                function addHistoryNumberToList() {

                }

            </script>

        </msh:ifFormTypeIsView>
    </tiles:put>
</tiles:insert>