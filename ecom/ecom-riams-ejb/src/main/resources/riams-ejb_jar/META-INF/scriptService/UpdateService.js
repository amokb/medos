function update_postgres(aCtx, aParams) {
	default_id(aCtx,"CustomMessage") ;
	default_id(aCtx,"DiaryMessage") ;
	default_id(aCtx,"VocDefectDiary") ;
	default_id(aCtx,"prescriptionlist") ;
	default_id(aCtx,"HospitalDataFond") ;
	default_id(aCtx,"ContractPerson") ;
	default_id(aCtx,"LpuContractNosologyGroup") ;
	default_id(aCtx,"VocMkbAdc") ;
	return "1" ;
}

function default_id(aCtx,aTable) {
	aTable=aTable.toLowerCase() ;
	aCtx.manager.createNativeQuery("alter table "+aTable+" alter column id set default nextval('"+aTable+"_sequence')").executeUpdate() ;
	return "1" ;
}

