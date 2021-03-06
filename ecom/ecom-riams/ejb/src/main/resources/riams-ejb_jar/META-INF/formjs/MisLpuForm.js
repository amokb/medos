function onPreSave(aForm, aEntity, aCtx) {
    if (aEntity.parent != null) {
        if ((0 + aForm.parent) > 0 && !((0 + aEntity.parent.id) == (0 + aForm.parent))) {
            if ((0 + aForm.parent) == (0 + aForm.id)) throw "Нельзя сделать чтоб совпадало головное ЛПУ с текущим!!!!";
            if (isUnitLpu(aForm.id, aForm.parent, aCtx)) throw "Нельзя переместить подразделение в подчиненное";
        }
    }
}

/**
 * Перед сохранением
 */
function onSave(aForm, aEntity, aCtx) {
    var date = new java.util.Date();
    aEntity.setEditDate(new java.sql.Date(date.getTime()));
    aEntity.setEditTime(new java.sql.Time(date.getTime()));
    aEntity.setEditUsername(aCtx.getSessionContext().getCallerPrincipal().toString());
}

/**
 * Перед сохранением
 */
function onCreate(aForm, aEntity, aCtx) {
    var date = new java.util.Date();
    aEntity.setCreateDate(new java.sql.Date(date.getTime()));
    aEntity.setCreateTime(new java.sql.Time(date.getTime()));
    aEntity.setCreateUsername(aCtx.getSessionContext().getCallerPrincipal().toString());

}

function isUnitLpu(aMainLpuId, aChildLpuId, aCtx) {
    var child = true;

    while (child === true) {
        if (aChildLpuId === aMainLpuId) return true;
        var idparent = aCtx.manager.createNativeQuery("select parent_id,count(*) from MisLpu where id=" + aChildLpuId + " group by parent_id").getSingleResult();
        if (idparent[0] == null) break;
        aChildLpuId = 0 + idparent[0];
        if (aChildLpuId < 1) child = false;
    }
    return false;
}