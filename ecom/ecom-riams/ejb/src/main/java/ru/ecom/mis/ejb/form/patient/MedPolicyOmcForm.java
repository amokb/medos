package ru.ecom.mis.ejb.form.patient;

import lombok.Setter;
import ru.ecom.ejb.services.entityform.WebTrail;
import ru.ecom.ejb.services.entityform.interceptors.AParentEntityFormInterceptor;
import ru.ecom.ejb.services.entityform.interceptors.AParentPrepareCreateInterceptors;
import ru.ecom.mis.ejb.domain.patient.MedPolicyOmc;
import ru.ecom.mis.ejb.form.patient.interceptors.MedPolicyPreCreate;
import ru.nuzmsh.commons.formpersistence.annotation.*;
import ru.nuzmsh.ejb.formpersistence.annotation.EntityFormPersistance;
import ru.nuzmsh.forms.validator.transforms.DoInputNonLat;
import ru.nuzmsh.forms.validator.transforms.DoUpperCase;
import ru.nuzmsh.forms.validator.validators.Required;

/**
 * Полис ОМС
 */
@EntityForm
@EntityFormPersistance(clazz = MedPolicyOmc.class)
@Comment("Полис ОМС")
@WebTrail(comment = "Полис ОМС", nameProperties = "polNumber", view = "entityView-mis_medPolicyOmc.do")
@Parent(property = "patient", parentForm =PatientForm.class)
@EntityFormSecurityPrefix("/Policy/Mis/MedPolicy/Omc")
@AParentPrepareCreateInterceptors(
        @AParentEntityFormInterceptor(MedPolicyPreCreate.class)
)
@Setter
public class MedPolicyOmcForm extends MedPolicyForm {

    /** Серия */
    @Comment("Серия")
    @Persist
    @DoUpperCase @DoInputNonLat
    public String getSeries() { return super.getSeries() ; }

    /** Организация */
    @Persist
    public Long getOrg() { return org ; }

    /** Прикрепленное ЛПУ */
	@Comment("Прикрепленное ЛПУ")
	@Persist
	public Long getAttachedLpu() {
		return attachedLpu;
	}

	/** Тип полиса */
	@Comment("Тип полиса")
	@Persist @Required 
	public Long getType() {return type;}

	/** Тип полиса */
	private Long type;
	/** Страховая компания */
    @Comment("Страховая компания")
    @Persist @Required
    public Long getCompany() { return company ; }
    /** Страховая компания */
    private Long company ;
	/** Прикрепленное ЛПУ */
	@Deprecated
	private Long attachedLpu;
    /** Организация */
    @Deprecated
    private Long org ;
    

}