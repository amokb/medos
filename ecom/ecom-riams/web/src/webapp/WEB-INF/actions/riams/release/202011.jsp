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
					<% // Версия от 30 октября %>
				<h1>Ноябрь 2020 года</h1>
				<ul>
					<li></li>
					<li>Удаление данных выписки по ролям в течение календарного дня (строго не в инфекционных отделениях) </li>
					<li>Перенос случая родов, классификации Робсона и, если есть, карты оценки риска ВТЭО, в другое СЛО (если рано создали случай в родовом отделении)</li>
					<li>Отчёт по данным выписки, которые были удалены</li>
					<li>Печать данных по принятым пациентам</li>
					<li>Вывод результата госпитализации при печати выписной формы</li>
					<li>В карте Covid-19 добавлен справочник с КТ. Если выбран результат КТ, обязательно заполнить дату и место проведения</li>
					<li>Правки в отчёт "Анализ работы родового отделения":</li>
                        <ul>
                            <li> - добавлен отбор по случаям с обсервацией без родов/выкидышей</li>
                            <li> - изменён поиск случаев с выкидышами</li>
                        </ul>
                    <li>Механизм выполнения ПЦР-анализов на SARS-COV2 лабораторией ГБУЗ АО АМОКБ:</li>
						<ul>
							<li> - назначение услуги SARS-COV2 в листе назначений</li>
							<li> - если выбрана SARS-COV2, то необходимо ввести номер пробирки, дата исследования - всегда завтрашний день, остальные опции недоступны</li>
							<li> - если выбрана другая услуга - всё по старому механизму</li>
							<li> - номер пробирки уникален в рамках одного дня</li>
							<li> - можно назначить пациенту только один анализ SARS-COV2 на следующий день в рамках СЛС</li>
							<li> - реализован забор биоматериала для врачей: Журнал обращений\Забор биомат. (только анализ SARS-COV2)</li>
							<li> - реализована Передача биоматериала ПЦР (только анализ SARS-COV2)</li>
							<li> - реализовано Рабочее место ПЦР (только анализ SARS-COV2)</li>
							<li> - т.к. пробирка Эппенгорфа относится и к другим услугам, создана новая пробирка для SARS-COV2</li>
							<li> - реализована отдельная кнопка Рез. ПЦР для SARS-COV2</li>
							<li> - в шаблоне услуги при вводе результата сделаны значения по умолчанию (лаборатория, дата забора и дата результата)<li>
							<li> - добавлен календарь при вводе даты в результате ПЦР SARS-COV2</li>
							<li> - добавлена причина брака для SARS-COV2 "Отправлено в другую лабораторию"</li>
							<li> - при браке назначений с причиной "Отправлено в другую лабораторию":
								<ul>
									<li> - не должны отправляться сообщения о браке</li>
									<li> - в листе назначений выделить цветом, отличным от "обычного" брака</li>
								</ul>
							<li> - формирование и печать реестра приёма-передачи для SARS-COV2</li>
                            <li> - формирование и печать информации о положительных результатах диагностических исследований на наличие COVID-19</li>
						</ul>
					<li></li>
				</ul>
					</div>
			</tr>
		</table>
	</tiles:put>
</tiles:insert>