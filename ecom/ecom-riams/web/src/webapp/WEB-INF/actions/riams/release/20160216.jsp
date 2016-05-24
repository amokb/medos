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
				<h1>Февраль 2016 года</h1>
				<h2>от 18.02.2016</h2>
				<ul>
					<li>Доработаны шаблоны при работе с заключениями</li>
					<li>В 039 форме добавлена разбивка экстренных посещений на скорую помощь и неотложную</li>
					<li>Добавлена печать листа назначений</li>
					<li>В карточке пациента появилась возможность печати пользовательских документов. Подробнее в инструкциях</li>
					<li>Доработано сохранение комментария назначения на операцию</li>
					<li>Доработано ограничение на основные диагнозы по отделениям</li>
					<li>Доработана форма приемного отделения стационара. При выборе поля кем направлен необходимо будет в обязательном порядке выбрать код МКБ направителя</li>
					<li>Доработано ограничение на редакцию талона врачом. Исправлена ошибка при редактировании талона направлененного регистратурой (для талонной версии)</li>
					<li>Доработан журнал диагнозов по стационару (СЛС). Добавлены поля в реестр: тип доставки, показания, район. Исправлена ошибка при отображение свода</li>
					<li>Добавлено оповещение всех пользователей об обновлении</li>
					<li>Исправлена ошибка при удаление юридической персоны</li>
					<li>Доработаны поиск контрактных персон и мед.договоров</li>
					<li>Изменен справочник контрактных персон</li>
					<li>В версии для инфоматов обновлена программа гос. гарантий</li>
					<li>Исправлена ошибка отображению списков (посещений, осмотров, выписок) по пациенту в разделе добавить данные из визитов</li>
					<li></li>
				</ul>
				<h2>от 25.02.2016</h2>
				<ul>
				<li>Добавлена возможность поточного ввода температурных листов.</li>
				<li>Возможность добавлять в акт для военкомата заключения с визитов</li>
				<li>При выписке появилась возможность вставить данные диагностических исследований </li>
				<li>Создан журнал расхождений клинического и выписного диагноза </li>
				<li>Доработан отчет по заявкам в техническую поддержку (фильтры по пользователю, сортировка по датам)</li>
				<li>Добавлен множественный ввод сопутствующих диагнозов и осложнений по СЛО и СЛС</li>
				<li>Создан журнал по просмотра логов по синхронизации с Парусом</li>
				<li>Исправлена ошибка при работе с ДД</li>
				<li>Добавлены ограничения на виды и методы ВМП</li>
				<li>Добавлена печать журнала заявок</li>
				<li>Исправлена ошибка при создании госпитализации на дому</li>
				<li></li>
				<li></li>
				<li></li>
				</ul>
				</div>
			</tr>

			
		</table>
	</tiles:put>
</tiles:insert>