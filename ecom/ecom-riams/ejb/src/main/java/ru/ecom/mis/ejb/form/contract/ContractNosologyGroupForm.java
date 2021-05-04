package ru.ecom.mis.ejb.form.contract;

import lombok.Setter;
import ru.ecom.ejb.form.simple.IdEntityForm;
import ru.ecom.ejb.services.entityform.WebTrail;
import ru.ecom.mis.ejb.domain.contract.ContractNosologyGroup;
import ru.nuzmsh.commons.formpersistence.annotation.Comment;
import ru.nuzmsh.commons.formpersistence.annotation.EntityForm;
import ru.nuzmsh.commons.formpersistence.annotation.EntityFormSecurityPrefix;
import ru.nuzmsh.commons.formpersistence.annotation.Persist;
import ru.nuzmsh.ejb.formpersistence.annotation.EntityFormPersistance;
import ru.nuzmsh.forms.validator.transforms.DoUpperCase;
import ru.nuzmsh.forms.validator.validators.Required;
@EntityForm
@EntityFormPersistance(clazz = ContractNosologyGroup.class)
@Comment("Нозологическая группа по договору")
@WebTrail(comment = "Нозологическая группа по договору", nameProperties= "id", list="entityList-contract_nosologyGroup.do", view="entityView-contract_nosologyGroup.do")
@EntityFormSecurityPrefix("/Policy/Mis/Contract/GroupRules/ContractNosologyGroup")
@Setter
public class ContractNosologyGroupForm extends IdEntityForm{
	
	/** Правило установки диагноза по отделению */
	@Comment("Правило установки диагноза по отделению")
	public Long getDiagnosisRule() {return diagnosisRule;}
	/** Правило установки диагноза по отделению */
	private Long diagnosisRule;

	/**
	 * Название
	 */
	@Comment("Название")
	@Persist @DoUpperCase @Required
	public String getName() {
		return name;
	}
	/**
	 * Название
	 */
	private String name;
	
	/** Диапозон */
	@Comment("Диапозон")
	public String getRangeMkb() {
		return rangeMkb;
	}

	/** Диапозон */
	private String rangeMkb;
}
