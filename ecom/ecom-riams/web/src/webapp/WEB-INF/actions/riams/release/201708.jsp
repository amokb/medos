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
					<% // Версия от 15 августа
					%>
				<h1>Август 2017 года</h1>
				
				<ul>

					<li>Добавлено логирование выдачи номеров направлений ВК</li>

					<li>Нетрудоспособность:<br>
					1. Исправлены ошибки при аннулировании ЭЛН.<br>
					2. При создании дубликата ЛН (по совместительству, либо взамен испорченного) появилась возможность запросить номер ЭЛН, тем самым сделав ЛН электронным.<br>
					3. При запросе номера пользователем и отсутствии его в базе происходит запрос нового номера с сервера ФСС.<br>
					4. Добавлена проверка при аннулировании ЛН: если аннулируемый ЛН является электронным, выдача дубликата взамен испорченного возможна только после аннулирования ЭЛН.<br></li>


					<li>Справочник услуг по специальности: добавлена проверка на привязку к ЛПУ.<br>
					Услуга у специалиста выводится только в том случае, если в соответствии услуги рабочей функции ЛПУ совпадает с ЛПУ специалиста, либо ЛПУ пустое.
					</li>
					<li>Телефонный справочник:<br>
						поиск по отделению.<br>
						добавлена возможность сохранить в excel<br>
					</li>

					<li>Исправлено присвоение номера при создании ВК (без создания направления на ВК)
					</li>

					<li>Работа с кассой.<br>
						Вызов функции печати чека происходит после сохранения чека.<br>
						При просмотре уже аннулированного начисления кнопка "оформить возврат" неактивна.
					</li>

					<li>Больничные листы: Исправлены ошибки при редактировании периодов, увеличено поле "номер больничного листа"<br>
					Добавлено поле "хеш ЭЛН" для корректной выгрузки продлений ЭЛН<br>
					Печать журнала больничных листов - добавлено поле "возраст пациента", номер печатается без слов "первичный, продление", согласно форме 036<br>
					Запрет редактирования/удаления ЭЛН после выгрузки<br>
					Запрет редактирования/удаления периода после выгрузки ЭЛН<br>
					</li>

					<li>Ежедневный отчет: Исправлены запросы (в списке состоящих, выписанных поиск идет по текущему отделению, во всех остальных случаях - по отделению поступления).
					</li>

					<li>Врачебные комиссии:<br>
						Журнал врачебных комиссий: добавлена автогенерация номеров врачебных комиссий.<br>
						Номера генерируется исходя из типа ВК, года=месяца экспертизы<br>
						В журнал направлений на ВК добавлено поле "номер в журнале"<br>
						В журнале состоящий поиск идет по отделению, в котором состоит пацеиент, а не по отделению поступления.<br>
						Журнал врачебных комиссий: в журнале отображается номер по журналу<br>
					</li>

					<li>Случай ВМП. Добавлено поле "количество установленных стентов"<br>
					Для определенных видов ВМП обязательное указание кол-ва стентов.
					</li>

					<li>263 приказ: Исправлена ошибка расчета значения поля DET для пакета N1
					</li>
					<li>
						Предварительная госпитализация: Поле "отделение" обязательно для заполнения.
					</li>

					<li>Исправлена ошибка при удалении случая ServiceMedCase</li>
						<msh:ifInRole roles="/Policy/Config/HelpAdmin">
							<br>
						</msh:ifInRole>
				</ul>
					</div>
			</tr>
		</table>
	</tiles:put>
</tiles:insert>