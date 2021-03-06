package ru.ecom.mis.ejb.domain.medcase;

import lombok.Getter;
import lombok.Setter;
import ru.ecom.ejb.services.index.annotation.AIndex;
import ru.ecom.ejb.services.index.annotation.AIndexes;
import ru.ecom.ejb.util.DurationUtil;
import ru.ecom.expomc.ejb.domain.med.VocIdc10;
import ru.ecom.mis.ejb.domain.medcase.voc.VocHospType;
import ru.ecom.mis.ejb.domain.worker.WorkFunction;
import ru.nuzmsh.commons.formpersistence.annotation.Comment;

import javax.persistence.Entity;
import javax.persistence.OneToOne;
import javax.persistence.Transient;
import java.sql.Time;

/**
 * Длительный СМО
 * @author oegorova
 *
 */
@Comment("Длительный СМО")
@Entity
@AIndexes({
	@AIndex(properties="ownerFunction", table="MedCase")
})
@Getter
@Setter
public abstract class LongMedCase extends MedCase {

	/** МКБ10*/
	@Comment("МКБ10")
	@OneToOne
	public VocIdc10 getIdc10() {return idc10;}

	/** Кто завершил */
	@Comment("Кто завершил")
	@OneToOne
	public WorkFunction getFinishFunction() {return finishFunction;	}

	/** Рабочая функция лечащего врача */
	@Comment("Рабочая функция лечащего врача")
	@OneToOne
	public WorkFunction getOwnerFunction() {	return ownerFunction;}

	/** Рабочая функция лечащего врача */
	private WorkFunction ownerFunction;

	/** Время выписки */
	private Time dischargeTime;
	/** Время поступления */
	private Time entranceTime;

	/** Кто завершил */
	private WorkFunction finishFunction;
	/** МКБ10 */
	private VocIdc10 idc10;
	
	/** Откуда поступил */
	@Comment("Откуда поступил")
	@OneToOne
	public VocHospType getSourceHospType() {
		return sourceHospType;
	}
	private VocHospType sourceHospType;
	
	/** Куда переведен */
	@Comment("Куда переведен")
	@OneToOne
	public VocHospType getTargetHospType() {
		return targetHospType;
	}
	private VocHospType targetHospType;

	
	// [start] Вычисляемые поля
	/** Длительность в днях */
	@Comment("Длительность в днях")
	@Transient
	public String getDuration() {
		return getDaysCount();
	}
	
	/** Количество дней */
	@Comment("Количество дней")
	@Transient
	public String getDaysCount() {
		return DurationUtil.getDuration(getDateStart(), getDateFinish());
	}
}