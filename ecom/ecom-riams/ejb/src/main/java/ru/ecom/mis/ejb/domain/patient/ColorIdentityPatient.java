package ru.ecom.mis.ejb.domain.patient;/**
 * Created by Milamesher on 30.04.2019.
 */

import ru.ecom.ejb.domain.simple.BaseEntity;
import ru.ecom.mis.ejb.domain.patient.voc.VocColorIdentityPatient;
import ru.nuzmsh.commons.formpersistence.annotation.Comment;

import javax.persistence.Entity;
import javax.persistence.OneToOne;
import java.sql.Date;

/** Информация о пациенте (для браслета) */
@Entity
public class ColorIdentityPatient extends BaseEntity {
    /** Браслет */
    @Comment("Браслет")
    @OneToOne
    public VocColorIdentityPatient getVocColorIdentity() {return theVocColorIdentity;}
    public void setVocColorIdentity(VocColorIdentityPatient aVocColorIdentity) {theVocColorIdentity = aVocColorIdentity;}
    /** Браслет */
    private VocColorIdentityPatient theVocColorIdentity ;
    
    /** Дата установки */
    @Comment("Дата установки")
    public Date getStartDate() {return theStartDate;}
    public void setStartDate(Date aStartDate) {theStartDate = aStartDate;}
    /** Дата установки */
    private Date theStartDate ;

    /** Дата снятия */
    @Comment("Дата снятия")
    public Date getFinishDate() {return theFinishDate;}
    public void setFinishDate(Date aFinishDate) {theFinishDate = aFinishDate;}
    /** Дата снятия */
    private Date theFinishDate ;

    /** Пользователь, который последний редактировал запись */
    @Comment("Пользователь, который последний редактировал запись")
    public String getEditUsername() {return theEditUsername;}
    public void setEditUsername(String aEditUsername) {theEditUsername = aEditUsername;}
    /** Пользователь, который последний редактировал запись */
    private String theEditUsername;

    /** Пользователь, который создал запись */
    @Comment("Пользователь, который создал запись")
    public String getCreateUsername() {return theCreateUsername;}
    public void setCreateUsername(String aCreateUsername) {theCreateUsername = aCreateUsername;}
    /** Пользователь, который создал запись */
    private String theCreateUsername;

    /** Доп. информация о пациенте (для браслета) */
    @Comment("Доп. информация о пациенте (для браслета)")
    public String getInfo() {return theInfo;}
    public void setInfo(String aInfo) {theInfo = aInfo;}
    /** Доп. информация о пациенте (для браслета) */
    private String theInfo;
}