<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://struts.apache.org/tags-tiles" prefix="tiles" %>
<%@ taglib uri="http://www.nuzmsh.ru/tags/msh" prefix="msh" %>
<%@ taglib uri="/WEB-INF/mis.tld" prefix="mis" %>

<tiles:insert page="/WEB-INF/tiles/main${param.short}Layout.jsp" flush="true">

    <tiles:put name='title' type='string'>
    </tiles:put>

    <tiles:put name='body' type='string'>
        <table class='mainMenu'>
            <tr>
                <td class='col1'>
                    <div class='menu'>
                        <% // Версия от 1 марта %>
                        <h1>Март 2021 года</h1>
                        <ul>
                            <li></li>
                            <li>Лабораторные анализы для отдела реализации платных медицинских услуг:
                                <ul>
                                    <li> - групповая рабочая функция для администраторов отдела, на которую будут
                                        направляться пациенты
                                    </li>
                                    <li> - данная групповая функция не будет выводится при создании дополнительного
                                        времени
                                    </li>
                                    <li> - вывод лабораторных услуг в направлении</li>
                                    <li> - общий свод всех лабораторных услуг добавлен в Рабочий календарь\Журнал
                                        направленных ->
                                        свод лаб. услуг (доступ к отчёту по отдельной роли)
                                    </li>
                                    <li> - вывод списка оплаченных услуг в журнале направленных
                                    </li>
                                    <li> - переход к визиту из журнала направленных по кнопке (по умолчанию переход
                                        осуществляется на направление)
                                    </li>
                                    <li> - вывод списка оплаченных услуг в журнале направленных
                                    </li>
                                    <li> - форматирование вывода услуг
                                    </li>
                                </ul>
                            </li>
                            <li> - Отчёт по времени на ИВЛ (отбор по периоду госпитализации, результату госпитализации,
                                профилю коек и типу ИВЛ)
                            </li>
                        </ul>
                    </div>
            </tr>
        </table>
    </tiles:put>
</tiles:insert>