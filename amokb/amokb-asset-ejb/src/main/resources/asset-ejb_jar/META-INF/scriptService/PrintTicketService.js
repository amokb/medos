var map = new java.util.HashMap() ;	

function printBakExp(aCtx, aParams) {
	
	var ticket = aCtx.manager.find(Packages.ru.ecom.poly.ejb.domain.Ticket
		, new java.lang.Long(aParams.get("id"))) ;
    var mc = ticket.medcard;
    var prs = mc.person;
    var plc = null;
    
    record("pat",prs) ;
    record("dateReg", ticket.date) ;
    return map ;
}

function back(aCtx, aParams) {
	return map ;
}
	
function printProtocol(aCtx, aParams) {
	//var list = aCtx.manager.createQuery("from Protocol where medCase_id=:sls")
		//.setParameter("sls",medCase.id).getResultList();
	//var protocol = !list.isEmpty()?list.iterator().next().record:"";
	var protocol = aCtx.manager.find(Packages.ru.ecom.poly.ejb.domain.protocol.Protocol
		, new java.lang.Long(aParams.get("id"))) ;
	map.put("prot.date",protocol.dateRegistration);
	map.put("prot.time",protocol.timeRegistration);
	map.put("protocol",protocol);
	map.put("prot.spec",protocol.specialistInfo);
	//map.put("prot.rec",protocol.record) ;
	recordMultiText("prot.rec", protocol.record,1) ;

	return map ;
}

function recordMultiText(aKey, aValue,aRtf) {
	if (aRtf!=null && aRtf>0) {
		var ret = new java.lang.StringBuilder () ;
		var val = aValue!=null?"" +aValue:"" ;
		var n = /\n/ ;
		var items = val.split(n);
		//ret.append("</text:p>") ;
		for (var i = 0; i < items.length; i++) {
			//ret.append("<text:p text:style-name=\"P6\">") ;
			//ret.append("<text:tab/>") ;
			ret.append(items[i]);
			ret.append("                                                                                                                                  ") ;
			ret.append("                                                                                                                                  ") ;
		}
		//ret.append("<text:p>") ;
		map.put(aKey,ret.toString()) ;
	} else {
		var ret = new java.lang.StringBuilder () ;
		var val = aValue!=null?"" +aValue:"" ;
		val=val.replace("&", "&amp;") ;
		val=val.replace("<", "&lt;");
		val=val.replace(">", "&gt;");
		var n = /\n/ ;
		var items = val.split(n);
		ret.append("</text:p>") ;
		for (var i = 0; i < items.length; i++) {
			ret.append("<text:p text:style-name=\"P6\">") ;
			//ret.append("<text:tab/>") ;
			ret.append(items[i]);
			ret.append("</text:p>") ;
		}
		ret.append("<text:p>") ;
		map.put(aKey,ret.toString()) ;
	}
}
	
function printInfo(aCtx, aParams) {
	
	var ticket = aCtx.manager.find(Packages.ru.ecom.poly.ejb.domain.Ticket
		, new java.lang.Long(aParams.get("id"))) ;
	var mc = ticket.medcard;
    var prs = mc.person;
    var plc = null;
    record("pat",prs) ;
    var FORMAT = new java.text.SimpleDateFormat("dd.MM.yyyy") ;
    record("bd",FORMAT.format(prs.birthday)) ;
    record("ticket",ticket) ;
    record("ticketd",FORMAT.format(ticket.date)) ;
    record("medcard",mc) ;
    record("idticket",""+ticket.id) ;
    recordBlanks("blank",ticket.prescriptionBlanks,4);
    recordVocProba("sex", prs.sex, 1, 2);
    recordVocProba("paym", ticket.vocPaymentType, 1, 5);
    recordVocProba("servPl", ticket.vocServicePlace, 1, 3);
    recordVocProba("reas", ticket.vocReason, 1, 4);
    recordVocProba("res", ticket.vocVisitResult, 1, 10);
    recordVocProba("ill", ticket.primary, 1, 2);
    recordVocProba("tr", ticket.vocTrauma, 1, 13);
    recordVocProba("disDocSt", ticket.disabilityDocumentStatus, 1, 2);
    recordVocProba("disReas", ticket.disabilityReason, 1, 6);
    //recordVocProba("disSex", ticket.sex, 1, 2);
    recordVocProba("disp", ticket.dispRegistration, 1, 4);
    if (prs.address!=null) {
    	recordBoolean("city",prs.address.addressIsCity);
		recordBoolean("village",prs.address.addressIsVillage);
	} else {
		record("city.k1","") ;
		record("city.k2","") ;
		record("village.k1","") ;
		record("village.k2","") ;
	}
	

    /*
   if (prs.getMedPolicies().size() > 0) {
            plc = prs.getMedPolicies().get(prs.getMedPolicies().size()-1);
    }

        // 1.льгота
        // 2.Номер полиса ОМС      
        
    if (plc!=null) {
		record("pat.polOmc", plc.getSeries() + plc.getPolNumber());
	} else {
		record("pat.polOmc","") ;
	}
   */
        // Пол
   // ifVocIsNotNull(prs.getSex(),"Sex");
   //recordArray("pat.sex",mc.sex,1,2,"__");
       /*
        // Адрес
    if (prs.getAddress()!=null) {
    	record("Address", prs.getAddress().getAddressInfo());
    	recordBoolean("p.c",mc.person.address.addressIsCity);
		recordBoolean("p.v",mc.person.address.addressIsVillage);
    } else {
    	record("Address", "") ;
    	recordBoolean("p.c");
		recordBoolean("p.v");
    }
        // социальный статус
    ifVocIsNotNull(prs.getSocialStatus(),"SocialStatus");
        // Инвалидность
        
        // Специалист
    if (ticket.getWorkFunction()!=null) {
    	record("Doctor", ticket.getWorkFunction().getWorkerInfo());
    	ifVocIsNotNull(ticket.getWorkFunction().getWorkFunction(),"DoctorFunction") ;
    	if (ticket.workFunction.worker!=null && ticket.workFunction.worker.lpu!=null) {
    		record("LPUName",ticket.workFunction.worker.lpu.name) ;
    	} else {
    		record("LPUName","") ;
    	}
    } else {
    	record("Doctor","") ;
    	record("DoctorFunction","") ;
    	record("LPUName","") ;
    }
    */
    //Город, село
	//if (prs.address!=null) {
		
   /* } else  {
    	recordBoolean("p.c");
		recordBoolean("p.v");
       // Специалист
        // Вид оплаты
    ifVocIsNotNull(ticket.getVocPaymentType(),"PaymentType");
        // Место обслуживания
    ifVocIsNotNull(ticket.getVocServicePlace(),"ServicePlace");
        // Цель посещения
    ifVocIsNotNull(ticket.getVocReason(),"Reason");
        // Результат обращения
    ifVocIsNotNull(ticket.getVocVisitResult(), "VisitResult");
        // Диагноз по МКБ
    ifIsNotNull(ticket.getIdc10(), "Idc10", ticket.getIdc10().getCode() + " " + ticket.getIdc10().getName());
        // Характер заболевания
    ifIsNotNull(ticket.getVocIllnesType(),"IllnesType", ticket.getVocIllnesType().getName());
        // Диспансерный учет
    ifVocIsNotNull(ticket.getVocDispanseryRegistration(),"DispanseryRegistration");
        // Травма
    ifVocIsNotNull(ticket.getVocTrauma(),"Trauma");
  */   	
    return map ;
}

function ifIsNotNull(aObj,aKey, aValue) {
	if (aObj!=null) {
		map.put(aKey,aValue) ;
	} else {
		map.put(aKey,"") ;
	}
}
function ifVocIsNotNull(aObj,aKey) {
	if (aObj!=null) {
		map.put(aKey,aObj.getName()) ;
	} else {
		map.put(aKey,"") ;
	}
}

function record(aKey,aValue) {
	map.put(aKey,aValue) ;
}
function recordDate(aKey, aDate) {
	if (aDate!=null) {
		var calen = java.util.Calendar.getInstance() ;
		calen.setTime(aDate) ;
		map.put(aKey+".day",""+calen.get(java.util.Calendar.DAY_OF_MONTH)) ;
		var month = ""+(calen.get(java.util.Calendar.MONTH)+1) ;
		map.put(aKey+".month",month) ;
		map.put(aKey+".monthname","");
		if (month!=null && month.equals("1")) map.put(aKey+".monthname","ЯНВАРЯ");
		if (month!=null && month.equals("2")) map.put(aKey+".monthname","ФЕВРАЛЯ");
		if (month!=null && month.equals("3")) map.put(aKey+".monthname","МАРТА");
		if (month!=null && month.equals("4")) map.put(aKey+".monthname","АПРЕЛЯ");
		if (month!=null && month.equals("5")) map.put(aKey+".monthname","МАЯ");
		if (month!=null && month.equals("6")) map.put(aKey+".monthname","ИЮНЯ");
		if (month!=null && month.equals("7")) map.put(aKey+".monthname","ИЮЛЯ");
		if (month!=null && month.equals("8")) map.put(aKey+".monthname","АВГУСТА");
		if (month!=null && month.equals("9")) map.put(aKey+".monthname","СЕНТЯБРЯ");
		if (month!=null && month.equals("10")) map.put(aKey+".monthname","ОКТЯБРЯ");
		if (month!=null && month.equals("11")) map.put(aKey+".monthname","НОЯБРЯ");
		if (month!=null && month.equals("12")) map.put(aKey+".monthname","ДЕКАБРЯ");
		
		map.put(aKey+".year",""+calen.get(java.util.Calendar.YEAR)) ;
	} else {
		map.put(aKey+".day","") ;
		map.put(aKey+".month","") ;
		map.put(aKey+".year","") ;
	}
}
function recordBoolean(aKey,aBool) {
	if (aBool!=null && aBool==true) {
		map.put(aKey+".k1","<text:span text:style-name=\"T13\">") ;
		map.put(aKey+".k2","</text:span>");
	} else {
		map.put(aKey+".k1","") ;
		map.put(aKey+".k2","");
	}
}
function recordArray(aKey,aValue,aMinVal,aMaxVal,aPrint) {
	
	for (var i=aMinVal;i<=aMaxVal;i++) {
		map.put(aKey+i,"");
	}
	if (aValue!=null)
	map.put(aKey+aValue.code,aPrint) ;
	}

function recordBlanks(aKey,aBlanks,aMax) {
	for (var i=0;i<=aBlanks.size()-1;i++) {
		var j=i+1;
		var bl=aBlanks.get(i);
		var info="Серия "+bl.series+" номер "+bl.number;
		if (bl.writingOutDate==null) {
			
		}else {
			var dat = Packages.ru.nuzmsh.util.format.DateFormat.formatToDate(bl.writingOutDate);
			info=info+" дата выписки "+dat;
		}
		map.put(aKey+j,info);
	}
	for (var i=aBlanks.size()+1;i<=aMax;i++) {
		map.put(aKey+i,"");
	}
}

function recordTime(aKey, aTime) {
	if (aTime!=null) {
		map.put(aKey,aTime.toString()) ;
	} else {
		map.put(aKey,"") ;
	}
}
function recordVocProba(aKey, aValue, aMin, aMax) {
	for (i=aMin;i<=aMax;i++) {
		map.put(aKey+i+".k1","");
		map.put(aKey+i+".k2","");
	}
	if (aValue!=null) {
		var ind= aValue.id ;
		map.put(aKey+ind+".k1","<text:span text:style-name=\"T14\">");
		map.put(aKey+ind+".k2","</text:span>");
		}
	} 

