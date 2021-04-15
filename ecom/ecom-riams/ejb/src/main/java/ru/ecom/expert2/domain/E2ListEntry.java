package ru.ecom.expert2.domain;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import ru.ecom.ejb.domain.simple.BaseEntity;
import ru.ecom.ejb.services.entityform.annotation.UnDeletable;
import ru.ecom.expert2.domain.voc.VocListEntryType;
import ru.nuzmsh.commons.formpersistence.annotation.Comment;

import javax.persistence.*;
import java.sql.Date;
import java.sql.Time;
import java.util.List;

/**
 * Список записей (заполнение
 */
@NamedQueries({
        @NamedQuery( name="E2ListEntry.findAllEntries"
                , query="from E2Entry where listEntry=:list and (isDeleted is null or isDeleted='0') and (doNotSend is null or doNotSend='0')")
})
@Entity
@UnDeletable
@Getter
@Setter
@Accessors(prefix = "the")
public class E2ListEntry extends BaseEntity {
    public E2ListEntry(){}
    public E2ListEntry(E2ListEntry aListEntry, String aNewName) {
        theName=(aNewName!=null?aNewName:"КОПИЯ_"+aListEntry.getName());
        theStartDate=aListEntry.getStartDate();
        theFinishDate=aListEntry.getFinishDate();
        theEntryType=aListEntry.getEntryType();
        theLpuOmcCode=aListEntry.theLpuOmcCode;

    }

    /** ИД монитора процесса проверки */
    private Long theMonitorId ;

    /** Черновик */
    private Boolean theIsDraft ;

    /** Закрыто для редакторирования */
    private Boolean theIsClosed =false;

    /** Имя заполнения */
    private String theName ;

    /** Дата начала периода */
    private Date theStartDate ;

    /** Дата окончания периода */
    private Date theFinishDate ;

    /** Тип заполнения */
    @OneToOne
    private VocListEntryType theEntryType ;

    /** Код ЛПУ, создавшее заполнение */
    private String theLpuOmcCode ;

    /** Дата создания */
    private Date theCreateDate ;

    /** Время создания */
    private Time theCreateTime ;

    @PrePersist
    void onPrePersist() {
        Long currentTime = System.currentTimeMillis();
        theCreateDate=new java.sql.Date(currentTime);
        theCreateTime=new java.sql.Time(currentTime);
    }

    /** Удаленная запись */
    private Boolean theIsDeleted = false;

    /** Список записей по заполнению */
    @Comment("Список записей по заполнению")
    @OneToMany(mappedBy = "listEntry",cascade = CascadeType.REMOVE)
    public List<E2Entry> getEntryList() {return theEntryList;}
    /** Список записей по заполнению */
    private List<E2Entry> theEntryList ;

    /** Дата последней проверки */
    private Date theCheckDate ;

    /** Время последней проверки */
    private Time theCheckTime ;




}
