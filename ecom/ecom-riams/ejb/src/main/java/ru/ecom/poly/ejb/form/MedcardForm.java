package ru.ecom.poly.ejb.form;

import lombok.Setter;
import ru.ecom.ejb.form.simple.IdEntityForm;
import ru.ecom.ejb.services.entityform.WebTrail;
import ru.ecom.ejb.services.entityform.interceptors.AParentEntityFormInterceptor;
import ru.ecom.ejb.services.entityform.interceptors.AParentPrepareCreateInterceptors;
import ru.ecom.poly.ejb.form.interceptors.PrepareCreateMedcardInterceptor;
import ru.nuzmsh.commons.formpersistence.annotation.*;
import ru.nuzmsh.ejb.formpersistence.annotation.EntityFormPersistance;
import ru.nuzmsh.forms.validator.transforms.DoDateString;
import ru.nuzmsh.forms.validator.transforms.DoTrimString;
import ru.nuzmsh.forms.validator.transforms.DoUpperCase;
import ru.nuzmsh.forms.validator.validators.DateString;
import ru.nuzmsh.forms.validator.validators.Required;

/**
 * Медицинская карта
 * User: morgun * Date: 08.01.2007 * Time: 16:18
 * User: stkacheva * Date: 23.07.2009 * Time: 16:49
 */
@Comment("Медицинская карта")
@EntityForm
@EntityFormPersistance(clazz = ru.ecom.poly.ejb.domain.Medcard.class)
@Parent(property = "person", parentForm = ru.ecom.mis.ejb.form.patient.PatientForm.class)
@WebTrail(comment = "Медицинская карта", nameProperties = "number"
, view = "entityParentView-poly_medcard.do"
, shortView = "entityShortView-poly_medcard.do"
)
@EntityFormSecurityPrefix("/Policy/Poly/Medcard")
@AParentPrepareCreateInterceptors(
        @AParentEntityFormInterceptor(PrepareCreateMedcardInterceptor.class)
)
@Setter
public class MedcardForm extends IdEntityForm {

    /** @return Пациент **/
    @Comment("Пациент")
    @Persist
    public Long getPerson() { return person; }

    @Comment("Номер карты")
    @Required @DoUpperCase @DoTrimString @Persist
    public String getNumber() {return number;}

    @Comment("Дата заведения карты")
    @Persist @DateString @DoDateString
    public String getDateRegistration() {return dateRegistration;}

    @Comment("Регистратор")
    @Persist
    public String getRegistrator() {return registrator;}

    /** Лечебно-профилактическое учреждение */
	@Comment("Лечебно-профилактическое учреждение")
	@Persist
	public Long getLpu() {return lpu;}

	/** Лечебно-профилактическое учреждение */
	private Long lpu;
    /** Пациент **/
    private Long person;
    /** Номер карты*/
    private String number;
    /** Дата заведения карты */
    private String dateRegistration;
    /** Регистратор */
    private String registrator;
}
