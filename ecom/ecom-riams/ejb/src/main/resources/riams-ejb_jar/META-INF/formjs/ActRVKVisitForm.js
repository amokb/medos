/**
 * Перед созданием
 */
function onPreCreate(aForm, aCtx) {
    var date = new java.util.Date() ;
    aForm.setCreateDate(Packages.ru.nuzmsh.util.format.DateFormat.formatToDate(date)) ;
    aForm.setCreateTime(new java.sql.Time (date.getTime())) ;
    aForm.setCreateUsername(aCtx.getSessionContext().getCallerPrincipal().toString()) ;
    var pat = aCtx.manager.createNativeQuery("select patient_id from medcase where id="+aForm.getMedCase()).getResultList();
    if (!pat.isEmpty())
        aForm.setPatient(java.lang.Long.valueOf(pat.get(0)));
    var wf=getWorkFunction(aCtx);
    if (typeof wf[0]!=='undefined')
        aForm.setWorkFunctionStart(wf[0]);
    if (typeof wf[1]!=='undefined')
        aForm.setSpecName(wf[1]);
}

// Получить рабочую функцию пользователя
function getWorkFunction(aCtx) {
    var wf=[];
    var specP = aCtx.manager.createNativeQuery("select wf.id as wfid,vwf.id as vwfid from secuser su left join workFunction wf on wf.secuser_id=su.id "
        + "left join vocworkfunction vwf on vwf.id=wf.workfunction_id where su.login='"
        + aCtx.getSessionContext().getCallerPrincipal().toString()+"'").getResultList();
    if (!specP.isEmpty()) {
        wf[0]=java.lang.Long.valueOf(specP.get(0)[0]);
        wf[1]=java.lang.Long.valueOf(specP.get(0)[1]);
    }
    return wf;
}

//проверка на соответствиее vocworkfunction
function checkSpecName(aForm, aCtx) {
    var wf=getWorkFunction(aCtx);
    if (typeof wf[1]!=='undefined' && wf[1]!=java.lang.Long.valueOf(aForm.getSpecName()))
        throw 'Редактировать акт РВК можно пользователь с той же рабочей функцией, что и пользователь, создавший его!';
}

function onPreSave(aForm, aEntity, aCtx) {
    checkSpecName(aForm,aCtx);
    var date = new java.util.Date() ;
    aForm.setEditDate(Packages.ru.nuzmsh.util.format.DateFormat.formatToDate(date)) ;
    aForm.setEditTime(new java.sql.Time (date.getTime())) ;
    aForm.setEditUsername(aCtx.getSessionContext().getCallerPrincipal().toString()) ;
}

/**
 * При сохранении
 */
function onSave(aForm, aEntity, aCtx) {
    var wf=getWorkFunction(aCtx);
    if (typeof wf[0]!=='undefined')
        aEntity.setWorkFunctionFinish(aCtx.manager.find(Packages.ru.ecom.mis.ejb.domain.worker.WorkFunction,wf[0]));
}
/**
 * При создании
 */
function onCreate(aForm, aEntity, aCtx) {

}

function onPreDelete(aEntityId, aCtx) {

}