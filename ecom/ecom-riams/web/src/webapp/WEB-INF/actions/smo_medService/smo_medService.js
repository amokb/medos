function listByDate(aForm,aCtx){
//"select id, number, phoneDate, phoneTime,phone,recieverOrganization from PhoneMessage &#xA;where phoneMessageType_id=1 and phoneDate=${param.id}" ;
	return aCtx.createForward("/WEB-INF/actions/smo_medService/listByDate.jsp") ;
}
function add(aForm,aCtx){
//"select id, number, phoneDate, phoneTime,phone,recieverOrganization from PhoneMessage &#xA;where phoneMessageType_id=1 and phoneDate=${param.id}" ;
	return aCtx.createForward("/WEB-INF/actions/smo_medService/add.jsp") ;
}