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
				<h1>Март 2016 года</h1>
				<h2>Изменения от 24.03.2016</h2>
				<ul>
					<li>Исправлена ошибка по сохранению предварительной выписки</li>
					<li>Добавлено удаление пустого СПО, после удаления визита или талона. </li>
					<li>Добавлена настройка префикса при печати больничных листов (в подразделение)</li>
					<li>Доработан журнал учета больных в соответствие с поступлением (добровольно, недобровольно, а также состоящих на К или Д наблюдениях)</li>
					<li></li>
				</ul>
				<h2>Изменения от 22.03.2016</h2>
				<ul>
					<li>Доработана печать реестра по пациентам поступившим/ выбывшим из стационара</li>
					<li>Доработана предварительная выписка</li>
					<li>Доработано отображание СЛО. По умолчанию отображаются только дневники специалистов</li>
					<li>Доработан случай смерти</li>
					<li>Исправлена ошибка по отображению доп. кода в СЛО</li>
				</ul>
				<h1>Март 2016 года</h1>
				<h2>Изменения от 17.03.2016</h2>
				<ul>
					<li>Доработан импорт адреса пациента при проверке по базе ФОМС</li>
					<li>Доработан просмотр классификатора - добавлено отображение листов назначения</li>
					<li>Исправлена ошибка, при которой было возможно создание нескольких назанчений на одно время</li>
					<li>Добавлена печать чек-листа в списке назначений на операцию</li>
					<li>Устранена невозможность создания назначения на резервное время (для работников стационара)</li>
					<li>Заявочная кампания: У операторов появилась возможность создавать заявки от имени других пользователей</li>
					<li>Заявочная кампания: Пользователь может подтвердить (или не подтвердить) выполнение заявки</li>
					<li>Диспансеризация: Добавлен запрет на создание карты ДД пациентам без актуального полиса</li>
					<li>Диспансеризация: Добавлен запрет на создание карты ДД пациенту, если он в период ДД лежал в стационаре</li>
					<li>Лаборатория: Добавлена возможность аннулирования результатов лабораторного исследования</li>
					<li>Добавлена возможность добавления мед.услуг из прейскуранта при выборе услуг по ДМС</li>
					<li>Доработан интерфейс по добавлению услуг в СЛС</li>
					<li>Добавлен быстрый ввод по СЛС,СЛО осложнений и сопутствующих диагнозов</li>
					<li>Доработан поиск договоров и договорных персон.</li>
					<li>Доработана форма ввода по договорам юридических лиц</li>
					<li>Добавлена возможность на ввод запрета по созданию операций по отделению</li>
					<li>Добавлена возможность выбора календаря для автоматической генерации в шаблонах календаря</li>
					<li>В список талонов по мед. карте добавлено отображение диагноза</li>
					<li></li>
					<li></li>
				</ul>
				<h3>Руководства</h3>
				<ul>
					<li>Добавлено руководство</li>
					<li>Добавление шаблона листа назначений</li>
					<li>Работа с платными договорами по физическим лицам</li>
					<li>Работа с шаблонами протоколов</li>
				</ul>
				</div>
			</tr>

			
		</table>
	</tiles:put>
</tiles:insert>