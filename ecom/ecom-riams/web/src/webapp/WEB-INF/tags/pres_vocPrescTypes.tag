<%@ tag pageEncoding="utf8" %>
<%@ taglib uri="http://www.nuzmsh.ru/tags/msh" prefix="msh" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="tags" %>
<%@ taglib uri="http://struts.apache.org/tags-tiles" prefix="tiles" %>

<%@ attribute name="name" required="true" description="Название" %>
<%@ attribute name="parentID" required="true" description="ИД родителя" %>
<%@ attribute name="parentType" required="true" description="Тип родителя (для родителя)" %>
<%@ attribute name="title" required="true" description="Заголовок" %>

<style type="text/css">
    #${name}PrescTypesDialog {
        visibility: hidden;
        display: none;
        position: absolute;
    }
</style>

<div id='${name}PrescTypesDialog' class='dialog'>
    <h2>Укажите тип листа назначения.</h2>
    <div class='rootPane'>
        <h3>${title}</h3>
        <form action="javascript:">
            <table id='tablePresTypes'>
            </table>

        </form>

    </div>
</div>

<script type="text/javascript">
    var theIs${name}PrescTypesDialogInitialized = false;
    var the${name}PrescTypesDialog = new msh.widget.Dialog($('${name}PrescTypesDialog'));

    function show${name}PrescTypes() {
        if (!theIs${name}PrescTypesDialogInitialized) {
            if ($('labDate')) {
                $('labDate').disabled = false;
            }
            init${name}PrescTypesDialog();
            the${name}PrescTypesDialog.show();
        } else if (confirm("Вы действительно хотите изменить тип листа назначения? Некоторые назначение могу быть удалены!")) {
            the${name}PrescTypesDialog.show();
        }
    }

    // Отмена
    function cancel${name}PrescTypes() {
        the${name}PrescTypesDialog.hide();
    }

    function disableEnableDate(i) {
        if (i == 1) {
            canChangeDate = false;
            if ($('labDate')) {
                $('labDate').disabled = true;
            }
        } else {
            canChangeDate = true;
            if ($('labDate')) {
                $('labDate').disabled = false;
            }
        }

    }

    function setType(typeID, typeName, isOnlyCurrentDate) {
        if ($('prescriptType').value != typeID) {
            $('prescriptType').value = typeID;
            $('prescriptTypeName').value = typeName;
            $('prescriptTypeName').disabled = 'true';
            changePrescriptionType();
            disableEnableDate(isOnlyCurrentDate);
            if (isOnlyCurrentDate != 1) {
                canChangeDate = true;
            }
        }
        try {
            labServiciesAutocomplete.setParentId(typeID + "#" + $('serviceStream').value + "#" + '${param.id}');
        } catch (e) {
            console.log(e);
        }
        the${name}PrescTypesDialog.hide();
    }

    // инициализация диалогового окна
    function init${name}PrescTypesDialog() {
        PrescriptionService.checkMedCaseEmergency('${parentID}', '${parentType}', {
                callback: function (aResult) {
                    PrescriptionService.getPrescriptionTypes(aResult, {
                            callback: function (getPresTypes) {
                                var presTypes = getPresTypes.split("#");
                                if (presTypes.length > 0) {
                                    var tbody = document.getElementById('tablePresTypes');
                                    for (var i = 0; i < presTypes.length; i++) {
                                        var row = document.createElement("TR");
                                        var param = presTypes[i].split(":");
                                        var tID = param[0];
                                        var tName = param[1];
                                        var onlyCurrentDate = param[2];
                                        var radio = "<td id='tdPresType" + i + "'  onclick='this.childNodes[0].checked=\"checked\";setType(" + tID + ",\"" + tName + "\"," + onlyCurrentDate + ");'>";
                                        radio += "<input type='radio' id = 'presType" + i + "' name='presType' value='" + tID + "' onclick=''>" + tName + "</td>";
                                        tbody.appendChild(row);
                                        row.innerHTML = radio;
                                    }
                                }
                            }
                        }
                    );
                }
            }
        );
        theIs${name}PrescTypesDialogInitialized = true;
    }
</script>