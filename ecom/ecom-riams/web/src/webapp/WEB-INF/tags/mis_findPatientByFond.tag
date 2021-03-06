<%@ tag pageEncoding="utf8" %>
<%@ taglib uri="http://www.nuzmsh.ru/tags/msh" prefix="msh" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="tags" %>
<%@ taglib uri="http://struts.apache.org/tags-tiles" prefix="tiles" %>

<%@ attribute name="name" required="true" description="Название" %>
<%@ attribute name="patientField" required="true" description="Поле Id пациента" %>

<style type="text/css">
    #${name}FindPatientByFond {
        visibility: hidden;
        display: none;
        position: absolute;
    }
</style>

<div id='${name}FindPatientByFond' class='dialog'>
    <h2>Поиск по базе фонда</h2>
    <div class='rootPane'>
        <h3>Найденные данные по базе фонда</h3>
        <form action="javascript:void(0)" id="frmFond" name="frmFond">
            <msh:panel>

                <msh:row>
                    <span id='${name}FindPatientByFondText'/>
                </msh:row>
            </msh:panel>
            <msh:row>
                <msh:separator label="Обновить данные" colSpan="8"/>
            </msh:row>
            <msh:row>
                <msh:checkBox property="is${name}Patient" label="Пациент"/>
                <msh:checkBox property="is${name}Policy" label="Полис"/>
                <msh:checkBox property="is${name}Document" label="Документы"/>
                <msh:checkBox property="is${name}Address" label="Адрес"/>
                <msh:checkBox property="is${name}Attachment" label="Прикрепление"/>
            </msh:row>
            <msh:row>
                <td colspan="8" align="center">
                    <input type="button" value='Обновить данные'
                           onclick='javascript:update${name}FindPatientByFond()' id="button${name}Update"/>
                    <input type="button" value='Закрыть окно' onclick='javascript:cancel${name}FindPatientByFond()'/>
                </td>
            </msh:row>
        </form>

    </div>
</div>


<script type="text/javascript">
    var theIs${name}FindPatientByFondInitialized = false;
    var the${name}FindPatientByFond = new msh.widget.Dialog($('${name}FindPatientByFond'));

    // Показать
    function show${name}FindPatientByFond(aText, aError) {
        // устанавливается инициализация для диалогового окна
        if (!theIs${name}FindPatientByFondInitialized) {
            init${name}FindPatientByFond();
        }
        if (aError) {
            if (aText.indexOf("null") != -1) {
                aText = "НЕТ ДАННЫХ В БАЗЕ ФОНДА";
            } else {
                if (aText.indexOf("java.net.SocketException") != -1) {
                    aText = "НЕТ СВЯЗИ С БАЗОЙ ФОНДА!!!";
                }
            }

            $('${name}FindPatientByFondText').innerHTML = "<b color='red'>ОШИБКА:" + aText + "</b>";
        } else {
            if (aText != null) {
                $('${name}FindPatientByFondText').innerHTML = aText;
                try {
                    var frm = document.forms['frmFond'];
                    if (getCheckedCheckBox(frm, "fondPolicy", "&") != '') {
                        $('is${name}Policy').checked = true;
                    } else {
                        $('is${name}Policy').checked = false;
                    }
                } catch (e) {

                }
                PatientButtonView(1);
            } else {
                $('${name}FindPatientByFondText').innerHTML = "ДАННЫЕ ПО ПАЦИЕНТУ НЕ НАЙДЕНЫ";
            }
        }
        the${name}FindPatientByFond.show();

    }

    function patientcheck(param) {
        if (param == 'patient') {
            $('is${name}Patient').checked = true;
        } else {
            if (param == 'address') {
                $('is${name}Address').checked = true;
            } else {
                if (param == 'document') {
                    $('is${name}Document').checked = true;
                } else {
                    if (param == 'policy') {
                        var frm = document.forms['frmFond'];
                        if (getCheckedCheckBox(frm, "fondPolicy", "&") != '') {
                            $('is${name}Policy').checked = true;
                        } else {
                            $('is${name}Policy').checked = false;
                        }

                    }
                }
            }
        }
    }

    // Отмена
    function update${name}FindPatientByFond() {
        var frm = document.forms['frmFond'];
        var fondFiodr = getCheckedRadio(frm, "fondFiodr");
        var fondDocument = getCheckedRadio(frm, "fondDocument");
        var fondPolicy = getCheckedCheckBox(frm, "fondPolicy", "&");
        var fondAdr = getCheckedRadio(frm, "fondAdr");
        if (+$('${patientField}').value > 0) {
            PatientService.updateDataByFond(
                $('${patientField}').value, fondFiodr, fondDocument, fondPolicy, fondAdr
                , $('is${name}Patient').checked, $('is${name}Policy').checked
                , $('is${name}Document').checked, $('is${name}Address').checked
                , $('is${name}Attachment').checked
                , {
                    callback: function () {
                        window.document.location.reload();
                    }
                }
            );
        } else {
            if (fondFiodr != "") {
                var fiodr = fondFiodr.split("#");
                $('lastname').value = fiodr[0];
                $('firstname').value = fiodr[1];
                $('middlename').value = fiodr[2];
                $('birthday').value = fiodr[3];
                $('deathDate').value = fiodr[6];
                if (fiodr[4] != null) $('snils').value = fiodr[4];
                if (fiodr[5] != null) $('commonNumber').value = fiodr[5];
            }
            var passType = "";
            var address = "";
            if (fondDocument != "") {
                var doc = fondDocument.split("#");
                passType = doc[0];
                $('passportSeries').value = doc[1];
                $('passportNumber').value = doc[2];
                $('passportDateIssued').value = doc[3];
                $('passportWhomIssued').value = doc[4];
            }
            var policyInfo = "";
            if (fondPolicy != "") {
                $('createNewOmcPolicy').checked = true;
                onCreateNewOmcPolicy();
                var pol = fondPolicy.split("&");
                var ind = 0;
                var policy = "";
                if (pol.length == 1) {
                    policy = pol[ind].split("#");
                } else {
                    for (var i = 0; i < pol.length; i++) {
                        policy = pol[i].split("#");
                        if (+policy[6] == 1) break;

                    }
                }

                $('policyOmcForm.commonNumber').value = policy[5];
                $('policyOmcForm.series').value = policy[1];
                $('policyOmcForm.polNumber').value = policy[2];
                $('policyOmcForm.actualDateFrom').value = policy[3];
                $('policyOmcForm.actualDateTo').value = policy[4];
                policyInfo = policy[0] + "#" + policy[1];
            }
            if (fondAdr != "") {
                var adr = fondAdr.split("#");
                $('houseNumber').value = adr[1];
                $('houseBuilding').value = adr[2];
                $('flatNumber').value = adr[3];
                address = adr[0] + "#" + adr[4] + "#" + adr[5] + "#" + adr[6] + "#" + adr[8] + "#" + (adr.length > 9 ? adr[9] : "");
            }
            if (passType.length > 1 || address.length > 6 || policyInfo.length > 2) {
                PatientService.getInfoVocForFond(
                    passType, address, policyInfo, {
                        callback: function (aResult) {
                            var js = JSON.parse(aResult);
                            if (js.passportType) {
                                $('passportType').value = js.passportType;
                                $('passportTypeName').value = js.passportName;
                            }
                            if (js.rayonId) {
                                $('rayon').value = js.rayonId;
                                $('rayonName').value = js.rayonName;
                            }
                            if (address != "") {
                                if (js.address) {
                                    $('address').value = js.address;
                                    reloadAddress();
                                }
                            }
                            if (js.companyId) {
                                $('policyOmcForm.company').value = js.companyId;
                                $('policyOmcForm.companyName').value = js.companyName;
                            }
                            if (js.policyId) {
                                $('policyOmcForm.type').value = js.policyId;
                                $('policyOmcForm.typeName').value = js.policyName;
                            }
                            $('sexName').focus();
                            $('sexName').select();
                            cancel${name}FindPatientByFond();
                        }
                    }
                );


            } else {
                $('sexName').focus();
                $('sexName').select();
                cancel${name}FindPatientByFond();
            }
        }
    }

    function ${name}ButtonView(aView) {
        if (+aView > 0) {
            $('button${name}Update').style.display = '';
        } else {
            $('button${name}Update').style.display = 'none';
        }
    }

    // Отмена
    function cancel${name}FindPatientByFond() {
        the${name}FindPatientByFond.hide();
        msh.effect.FadeEffect.pushFadeAll();
    }

    function checkedAll${name}FindPatientByFond(aValue) {
        var frm = document.forms['frmFond'];
        setCheckedAllRadio(frm, "fondFiodr", aValue);
        setCheckedAllRadio(frm, "fondDocument", aValue);
        setCheckedAllCheckBox(frm, "fondPolicy", aValue);
        setCheckedAllRadio(frm, "fondAdr", aValue);

    }

    // инициализация диалогового окна
    function init${name}FindPatientByFond() {
        theIs${name}FindPatientByFondInitialized = true;

    }
</script>