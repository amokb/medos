
function onPreCreate(aForm, aCtx) {
	//throw "hello";
	var date = new java.util.Date() ;
	aForm.setCreateDate(Packages.ru.nuzmsh.util.format.DateFormat.formatToDate(date)) ;
	aForm.setCreateUsername(aCtx.getUsername()) ;
}

function onCreate(aForm, aEntity, aCtx) {
	saveParameters (aForm, aEntity, aCtx.getUsername(),aCtx);
}

function onSave (aForm, aEntity, aCtx) {
	saveParameters (aForm, aEntity, aCtx.getUsername(),aCtx);
}
function saveParameters (aForm, aEntity, aUsername, aCtx) {
	Packages.ru.ecom.diary.ejb.service.assessmentcard.AssessmentCardServiceBean.saveParametersByCard(aForm.getPatient(),aEntity,aForm.getParams(), aCtx.manager);
}

function onPreDelete(aId, aCtx) {
	aCtx.manager.createNativeQuery("delete from forminputprotocol where assessmentCard='"+aId+"'").executeUpdate() ;
	
}