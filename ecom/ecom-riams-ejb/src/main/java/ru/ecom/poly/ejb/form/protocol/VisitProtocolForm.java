package ru.ecom.poly.ejb.form.protocol;

import ru.ecom.ejb.services.entityform.WebTrail;
import ru.ecom.ejb.services.entityform.interceptors.ACreateInterceptors;
import ru.ecom.ejb.services.entityform.interceptors.AEntityFormInterceptor;
import ru.ecom.ejb.services.entityform.interceptors.AParentEntityFormInterceptor;
import ru.ecom.ejb.services.entityform.interceptors.AParentPrepareCreateInterceptors;
import ru.ecom.ejb.services.entityform.interceptors.ASaveInterceptors;
import ru.ecom.mis.ejb.form.medcase.MedCaseForm;
import ru.ecom.poly.ejb.form.interceptors.ProtocolPreCreateInterceptor;
import ru.ecom.poly.ejb.form.interceptors.ProtocolSaveInterceptor;
import ru.nuzmsh.commons.formpersistence.annotation.Comment;
import ru.nuzmsh.commons.formpersistence.annotation.EntityForm;
import ru.nuzmsh.commons.formpersistence.annotation.EntityFormSecurityPrefix;
import ru.nuzmsh.commons.formpersistence.annotation.Parent;
import ru.nuzmsh.commons.formpersistence.annotation.Persist;
import ru.nuzmsh.ejb.formpersistence.annotation.EntityFormPersistance;
import ru.nuzmsh.forms.validator.transforms.DoDateString;
import ru.nuzmsh.forms.validator.transforms.DoTimeString;
import ru.nuzmsh.forms.validator.validators.DateString;
import ru.nuzmsh.forms.validator.validators.Required;
import ru.nuzmsh.forms.validator.validators.TimeString;

@Comment("Протокол")
@EntityForm
@EntityFormPersistance(clazz = ru.ecom.poly.ejb.domain.protocol.Protocol.class)
@WebTrail(comment = "Протокол", nameProperties = "info"
	, view = "entityParentView-smo_visitProtocol.do"
		,list = "entityParentList-smo_visitProtocol.do"
		)
//@Parent(property = "medCase", parentMapForm = "$$map$$smo_visitForm")
@Parent(property = "medCase", parentForm = MedCaseForm.class)
@EntityFormSecurityPrefix("/Policy/Mis/MedCase/Protocol")
@ASaveInterceptors(
        @AEntityFormInterceptor(ProtocolSaveInterceptor.class)
)
@ACreateInterceptors(
        @AEntityFormInterceptor(ProtocolSaveInterceptor.class)
)
@AParentPrepareCreateInterceptors(
        @AParentEntityFormInterceptor(ProtocolPreCreateInterceptor.class)
)
public class VisitProtocolForm extends ProtocolForm {
	/** ВИзит */
	@Comment("ВИзит")
	@Persist
	public Long getMedCase() {return theMedCase;}
	public void setMedCase(Long aMedCase) {theMedCase = aMedCase;}

	/** Тип протокола */
	@Comment("Тип протокола")
	@Persist 
	public Long getType() {return theType;}
	public void setType(Long aType) {theType = aType;}

	/** Тип протокола инфо */
	@Comment("Тип протокола инфо")
	@Persist
	public String getTypeInfo() {return theTypeInfo;}
	public void setTypeInfo(String aTypeInfo) {theTypeInfo = aTypeInfo;}

	/** Для выписки */
	@Comment("Для выписки")
	@Persist
	public Boolean getIsDischarge() {return theIsDischarge;}
	public void setIsDischarge(Boolean aIsDischange) {theIsDischarge = aIsDischange;}

	/** Время регистрации */
	@Comment("Время регистрации")
	@Persist @Required
	@TimeString @DoTimeString
	public String getTimeRegistration() {return theTimeRegistration;}
	public void setTimeRegistration(String aTimeRegistration) {theTimeRegistration = aTimeRegistration;}
	
	/** Дата печати */
	@Comment("Дата печати")
	@DateString @DoDateString
	public String getPrintDate() {return thePrintDate;}
	public void setPrintDate(String aPrintDate) {thePrintDate = aPrintDate;}
	
	/** Время печати */
	@Comment("Время печати")
	@DoTimeString @TimeString
	public String getPrintTime() {return thePrintTime;}
	public void setPrintTime(String aPrintTime) {thePrintTime = aPrintTime;}

	/** Время печати */
	private String thePrintTime;
	/** Дата печати */
	private String thePrintDate;
	/** Время регистрации */
	private String theTimeRegistration;	
	/** Для выписки */
	private Boolean theIsDischarge;
	/** Тип протокола инфо */
	private String theTypeInfo;
	/** Тип протокола */
	private Long theType;
	/** ВИзит */
	private Long theMedCase;
	/** Визит */
}
