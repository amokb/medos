<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://struts.apache.org/tags-tiles" prefix="tiles" %>
<%@ taglib uri="http://www.nuzmsh.ru/tags/msh" prefix="msh" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="tags" %>
<%@ taglib uri="http://www.ecom-ast.ru/tags/ecom" prefix="ecom" %>

<tiles:insert page="/WEB-INF/tiles/mainLayout.jsp" flush="true">

  <tiles:put name="title" type="string">
    <msh:title mainMenu="Medcard">Список талонов по специалисту с разбивкой по МКБ ....</msh:title>
  </tiles:put>
  <tiles:put name="side" type="string">
    <tags:ticket_finds currentAction="ticketsBySpec" />
  </tiles:put>
  <tiles:put name="body" type="string">
    <ecom:webQuery name="list" nativeSql=" select t.id as tid,m.number as mnumber
    , p.lastname||' '|| p.firstname||' '||coalesce(p.middlename,'') ||' г.р.'||p.birthday as fio
    ,t.dateCreate as tdateCreate,t.date as tdate
    ,vwf.name||' '||wp.lastname||' '|| wp.firstname||' '||wp.middlename as wfinfo
    ,mkb.code as mkbcode ,vr.name as vrname
    ,case when cast(t.talk as int)=1 then 1 else null end as talk  
    from Ticket t left join medcard m on m.id=t.medcard_id     
    left join patient p on p.id=m.person_id     
    left join Omc_Oksm ok on p.nationality_id=ok.id  
    left join workfunction wf on wf.id=t.workFunction_id    
    left join vocworkfunction vwf on vwf.id=wf.workFunction_id    
    left join worker  w on w.id=wf.worker_id    left join patient wp on wp.id=w.person_id    left join vocIdc10 mkb on mkb.id=t.idc10_id    left join vocreason vr on vr.id=t.vocreason_id    
    where t.date  between '${dateBegin}'  and '${dateEnd}' and t.status='2' and t.workfunction_id='${spec }' and t.idc10_id='${mkb}'  ${add}     order by p.lastname,p.firstname,p.middlename" />
    <msh:ifInRole roles="/Policy/Mis/MisLpu/Psychiatry">
	    <msh:table name="list" action="entityParentEdit-poly_ticket.do" idField="1" noDataMessage="Не найдено">
	      <msh:tableColumn columnName="#" property="sn" />
	      <msh:tableColumn columnName="№талона" property="1" />
	      <msh:tableColumn columnName="№мед.карты" property="2" />
	      <msh:tableColumn columnName="Пациент" property="3" />
	      <msh:tableColumn columnName="Дата приема" property="5" />
	      <msh:tableColumn columnName="Специалист" property="6" />
	      <msh:tableColumn columnName="Диагноз" property="7" />
	      <msh:tableColumn columnName="Цель посещения" property="8" />
	      <msh:tableColumn columnName="Беседа с родс." property="9" />
	    </msh:table>
    </msh:ifInRole>
    <msh:ifNotInRole roles="/Policy/Mis/MisLpu/Psychiatry">
	    <msh:table name="list" action="entityParentEdit-poly_ticket.do" idField="1" noDataMessage="Не найдено">
	      <msh:tableColumn columnName="#" property="sn" />
	      <msh:tableColumn columnName="№талона" property="1" />
	      <msh:tableColumn columnName="№мед.карты" property="2" />
	      <msh:tableColumn columnName="Пациент" property="3" />
	      <msh:tableColumn columnName="Дата приема" property="5" />
	      <msh:tableColumn columnName="Специалист" property="6" />
	      <msh:tableColumn columnName="Диагноз" property="7" />
	      <msh:tableColumn columnName="Цель посещения" property="8" />
	    </msh:table>
    </msh:ifNotInRole>
  </tiles:put>
</tiles:insert>

