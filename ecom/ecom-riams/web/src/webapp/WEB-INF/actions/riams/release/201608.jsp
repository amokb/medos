<%@ page contentType="text/html;charset=UTF-8" language="java"%>
<%@ taglib uri="http://struts.apache.org/tags-tiles" prefix="tiles"%>
<%@ taglib uri="http://www.nuzmsh.ru/tags/msh" prefix="msh"%>
<%@ taglib uri="/WEB-INF/mis.tld" prefix="mis"%>

<tiles:insert page="/WEB-INF/tiles/mainLayout.jsp" flush="true">

	<tiles:put name='title' type='string'>
	</tiles:put>

	<tiles:put name='body' type='string'>
		<table class='mainMenu'>
			<tr>
				<td class='col1'>
				<div class='menu'>
				<h1>Август 2016 года</h1>
				<ul>
					<li>Добавлена возможность вести карты оценки (на основе параметризированных шаблонов)</li>
					<li>Прикрепление: автоматически проставляется тип прикрепления в зависимости от адреса</li>
					<li>Из активных направлений убраны лаб. назначения</li>
					<li>При закрытии СПО в талонной версии корректно выбирается врач, закрывший СПО (по рабочей функции специалиста, выполневшего последний прием пациента)</li>
					<li>Запрет на набивку нескольких талонов к одно специалисту</li>
					<li>Вывод корректного сообщения при удалении пациента с существующей картой ДД </li>
					<li>Создано разрешение на редактирование дневника другим специалистом</li>
					
					<msh:ifInRole roles="/Policy/Config/HelpAdmin">
						<br>
						для создания разрешения для одного дневника создаем запись с кодом "editOtherUser" . objectCode = "Protocol". ИД = ИД дневника
						Для редактирования всех дневников в конкретной ИБ создаем запись с кодом "editOtherUserAllHosp", objectCode="DischargeMedCase". ИД = ИД госпитализации
					</msh:ifInRole>
					<li>Сохраняются данные по новорожденных при перезагрузке страницы создания родов</li>
					<li>Добавлена возможность прикреплять внешние документы к протоколу и пациенту</li>
					
					<li>Лист назначений: добавлена проверка перед созданием назначения- свободно ли время</li>
					<li>Архив: при передачи карт в архив в журнале добавлено поле "УМЕР"</li>
					<li>Архив: возможность отмены передачи карты в архив, если передача была осуществлена только что</li>
					<li></li>
				
				</ul>
				<h2>В рамках движения "Свободное ПО" Ткачевой Светланой сделано:</h2>
				<ul>
					<li>Оптимизация работы печати протоколов по СЛО</li>
					<li>039-форма. В реестре пациентов (СМО) добавлена кнопка "Н" - Открыть в новой вкладке. 
					Добавлено ограничение по просмотру информации только по своим данным
					<msh:ifInRole roles="/Policy/Config/HelpAdmin">
						<br>
						для просмотра информации по всем специалистам: /Policy/Mis/MedCase/Visit/ViewAll
					</msh:ifInRole>
					
					</li>
					<li>Добавлена возможность удалять только СВОИ протоколы в течение 24 часов от момента создания
					<msh:ifInRole roles="/Policy/Config/HelpAdmin">
						<br>
						для добавления ограничения нужно подключить политику: /Policy/Mis/MedCase/Protocol/EnableDeleteOnlyTheir
						<br>
						для отключения ограничения нужно подключить политику: /Policy/Mis/MedCase/Protocol/DisableDeleteOnlyTheir (если нужно отключить возможность для пользователя, у которого подключена политика /Policy/Mis/MedCase/Protocol/EnableDeleteOnlyTheir)
					</msh:ifInRole>
					</li>
					<li>В журнале диагнозов по стационару по умолчанию специалист может просмотреть данные только своего отделения
					<msh:ifInRole roles="/Policy/Config/HelpAdmin">
						<br>
						для отключения ограничения нужно подключить политику: /Policy/Mis/MedCase/Stac/Journal/ShowInfoAllDepartments
					</msh:ifInRole>
					</li>
					<li>Исправлена ошибка по отправке на редакцию дневников в СЛО заведующим (или членом ВК) леч.врачу</li>
					<li>Исправлена ошибка отображения дерева при создание договора по платному пациенту (совместимость версий баз данных). </li>
					<li>Доработана печать мед.карты пациента (12 пункт)</li>
					<li>Добавлено ограничение по инвалидности для списков по движению по участкам</li>
				</ul>
				</div>
			</tr>

			
		</table>
	</tiles:put>
</tiles:insert>