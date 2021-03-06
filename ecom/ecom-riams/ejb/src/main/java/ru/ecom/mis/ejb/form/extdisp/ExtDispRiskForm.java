package ru.ecom.mis.ejb.form.extdisp;

import lombok.Setter;
import ru.ecom.ejb.form.simple.IdEntityForm;
import ru.ecom.ejb.services.entityform.WebTrail;
import ru.ecom.mis.ejb.domain.extdisp.ExtDispRisk;
import ru.nuzmsh.commons.formpersistence.annotation.Comment;
import ru.nuzmsh.commons.formpersistence.annotation.EntityForm;
import ru.nuzmsh.commons.formpersistence.annotation.EntityFormSecurityPrefix;
import ru.nuzmsh.commons.formpersistence.annotation.Parent;
import ru.nuzmsh.commons.formpersistence.annotation.Persist;
import ru.nuzmsh.ejb.formpersistence.annotation.EntityFormPersistance;

@EntityForm
@EntityFormPersistance(clazz = ExtDispRisk.class)
@Comment("Выявленный при дополнительной диспансеризации риск возникновения заболевания")
@WebTrail(comment = "Выявленный при дополнительной диспансеризации риск возникновения заболевания", nameProperties= "id", list="entityParentList-extdisp_extDispRisk.do", view="entityParentView-extdisp_extDispRisk.do")
@Parent(property="card", parentForm=ExtDispCardForm.class)
@EntityFormSecurityPrefix("/Policy/Mis/ExtDisp/Card/Risk")
@Setter
public class ExtDispRiskForm extends IdEntityForm{
	/**
	 * Карта дополнительной диспансеризации
	 */
	@Comment("Карта дополнительной диспансеризации")
	@Persist
	public Long getCard() {return card;}
	/**
	 * Карта дополнительной диспансеризации
	 */
	private Long card;
	/**
	 * Фактов риска заболевания при проведение дополнительной диспансеризации
	 */
	@Comment("Фактов риска заболевания при проведение дополнительной диспансеризации")
	@Persist
	public Long getDispRisk() {return dispRisk;}
	/**
	 * Фактов риска заболевания при проведение дополнительной диспансеризации
	 */
	private Long dispRisk;
}
