<%@ page contentType="text/html;charset=UTF-8" language="java"%>
<%@ taglib uri="http://struts.apache.org/tags-tiles" prefix="tiles"%>
<%@ taglib uri="http://www.nuzmsh.ru/tags/msh" prefix="msh"%>
<%@ taglib uri="/WEB-INF/mis.tld" prefix="mis"%>

<tiles:insert page="/WEB-INF/tiles/main${param.short}Layout.jsp" flush="true">

    <tiles:put name='title' type='string'>
    </tiles:put>

    <tiles:put name='body' type='string'>
        <table class='mainMenu'>
            <tr>
                <td class='col1'>
                    <div class='menu'>
                        <% // Версия от 30 ноября %>
                        <h1>Декабрь 2020 года</h1>
                        <ul>
                            <li></li>
                            <li>Механизм выполнения ПЦР-анализов на SARS-COV2 лабораторией ГБУЗ АО АМОКБ:</li>
                            <ul>
                                <li> - отчёт "Положительные ПЦР" добавлен в меню лаборатории</li>
                                <li> - убран забор биоматериала ПЦР для врачей </li>
                                <li> - забор биоматериала ПЦР ставится автоматически при создании назначения на SARS-COV2 на следующий день на 06:00 утра</li>
                                <li> - настроен механизм редактирования браслетов при редактирования результата анализа до подтверждения врачом КДЛ</li>
                                <li> - в реестр приёма-передачи добавлен вывод даты рождения</li>
                                <li> - Кнопка Рез. ПЦР доступна только для SARS-COV2-исследований при наличии роли</li>
                                <li> - сводная таблица ПЦР-анализов на SARS-COV2, выполненных лабораторией ГБУЗ АО АМОКБ</li>
                                <li> - перенос реестров на ПЦР из раздела "Передача ПЦР в лабораторию" в Отчёты\Свод анализов ПЦР SARV-COV2</li>
                                <li> - в Своде анализов ПЦР SARV-COV2 добавлено разделение на инфекционные и неинфекционные отделения</li>
                                <li> - свод анализов ПЦР SARV-COV2 доступен в Журнале обращений, в отчётах и на вкладке "Лаборатория"</li>
                                <li>Механизм выполнения ПЦР-анализов на SARS-COV2 лабораторией ГБУЗ АО АМОКБ:</li>
                                <ul>
                                    <li> - создание назначений на SARV-COV2 из листа назначений: номер пробирки необязателен, дата любая</li>
                                    <li> - забор осуществляет процедурная м/с, вводит номер пробирки при заборе</li>
                                    <li> - запрет на создание назначений, пока не выполнено или не отменено предыдущее назначение на SARS-COV2</li>
                                    <li> - запрет на одинаковые номера пробирок в течение одного дня забора </li>
                                    <li> - добавлены реестры направленных и приёма-передачи для поликлиники</li>
                                </ul>
                            </ul>
                            <li>Вывод врачебных комиссий в госпитализации за все СЛО</li>
                            <li>Оптимизация отчёта по браслетам</li>
                            <li>Вывод информации о том, что время занято, в форму создания времени по специалисту</li>
                            <li>Вывод информации о результате госпитализации в реестр карт COVID-19 при выписке</li>
                            <li>Запрет направлений на прошедщие даты</li>
                            <li>Вывод отделения в 13 форму</li>
                            <li>Форма 16_ВН: разбивка по длительности, когда причина нетрудоспособности 05</li>
                            <li>Форма 16_ВН: переход на госпитализацию (поиск по номеру истории и году)</li>
                            <li>Форма 16_ВН: вывод номера истории в отчёт</li>
                            <li>Экспорт листков нетрудоспособности: обновление и актуализация справочников</li>
                            <li>Запрет на снятие браслетов (ИВЛ, результаты анализов, патология анализов) для пользователей, не являющихся администраторами</li>
                            <li>Автоматическое снятие браслета ИВЛ при удалении хирургической операции</li>
                            <li>Вывод эпид. номера из карты Covid-19 в реестр ПЦР</li>
                            <li></li>
                        </ul>
                    </div>
            </tr>
        </table>
    </tiles:put>
</tiles:insert>