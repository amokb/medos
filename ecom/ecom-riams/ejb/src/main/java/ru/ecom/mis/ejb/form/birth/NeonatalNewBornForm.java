package ru.ecom.mis.ejb.form.birth;

import lombok.Setter;
import ru.ecom.ejb.services.entityform.WebTrail;
import ru.ecom.ejb.services.entityform.interceptors.AParentEntityFormInterceptor;
import ru.ecom.ejb.services.entityform.interceptors.AParentPrepareCreateInterceptors;
import ru.ecom.mis.ejb.domain.birth.NewBorn;
import ru.ecom.mis.ejb.form.birth.interceptors.NewBornPreCreateInterceptor;
import ru.ecom.mis.ejb.form.medcase.MedCaseForm;
import ru.nuzmsh.commons.formpersistence.annotation.Comment;
import ru.nuzmsh.commons.formpersistence.annotation.EntityForm;
import ru.nuzmsh.commons.formpersistence.annotation.EntityFormSecurityPrefix;
import ru.nuzmsh.commons.formpersistence.annotation.Parent;
import ru.nuzmsh.commons.formpersistence.annotation.Persist;
import ru.nuzmsh.ejb.formpersistence.annotation.EntityFormPersistance;
import ru.nuzmsh.forms.validator.validators.Required;

@EntityForm
@EntityFormPersistance(clazz=NewBorn.class)
@Comment(" Новорожденный")
@WebTrail(comment = " Новорожденный", nameProperties= "id", 
		view="entityParentView-preg_newBorn.do", 
		list = "entityParentList-preg_newBorn.do")
@Parent(property="medCase", parentForm= MedCaseForm.class)
@EntityFormSecurityPrefix("/Policy/Mis/NewBorn")
@AParentPrepareCreateInterceptors(
        @AParentEntityFormInterceptor(NewBornPreCreateInterceptor.class)
)
@Setter
public class NeonatalNewBornForm  extends NewBornForm {
	/** Отделение */
	@Comment("Отделение")
	@Persist @Required
	public Long getDepartment() {return department;}

	/** Отделение */
	private Long department;

	/** Диабет (браслет)*/
	@Comment("Диабет (браслет)")
	@Persist
	public Long getDiabetIdentity() {return diabetIdentity;}
	/** Диабет (браслет)*/
	private Long diabetIdentity;
}
