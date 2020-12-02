package ru.ecom.expert2.service;

import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.XMLOutputter;
import ru.ecom.ejb.services.monitor.ILocalMonitorService;
import ru.ecom.ejb.services.monitor.IMonitor;
import ru.ecom.ejb.util.injection.EjbEcomConfig;
import ru.ecom.expert2.domain.*;
import ru.ecom.expert2.domain.voc.VocE2BillStatus;
import ru.ecom.expert2.domain.voc.VocE2EntrySubType;
import ru.ecom.expert2.domain.voc.VocE2Sanction;
import ru.ecom.expert2.domain.voc.federal.*;
import ru.ecom.mis.ejb.domain.lpu.MisLpu;
import ru.ecom.mis.ejb.domain.medcase.voc.VocMedService;
import ru.ecom.mis.ejb.domain.worker.PersonalWorkFunction;

import javax.annotation.EJB;
import javax.ejb.Local;
import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Stateless
@Local(IExpert2ImportService.class)
@Remote(IExpert2ImportService.class)
public class Expert2ImportServiceBean implements IExpert2ImportService {
    private static final Logger LOG = Logger.getLogger(Expert2ImportServiceBean.class);
    private static final EjbEcomConfig CONFIG = EjbEcomConfig.getInstance();
    private static final String XMLDIR = CONFIG.get("expert2.input.folder", "/opt/jboss-4.0.4.GAi/server/default/riams/expert2xml");
    private final HashMap<String, PersonalWorkFunction> DOCTORLIST = new HashMap<>();
    /**
     * Загружаем MP файл (ответ от фонда)
     * импорт версии от 2020 года
     */

    private final HashMap<String, VocE2Sanction> sanctionMap = new HashMap<>();
    private @PersistenceContext
    EntityManager manager;
    private @EJB
    IExpert2Service expertService;
    private @EJB
    ILocalMonitorService monitorService;

    private Date toDate(String date) throws ParseException {
        return new java.sql.Date(new SimpleDateFormat("yyy-MM-dd").parse(date).getTime());
    }

    private Document getDocumentFromFile(String path, String filename, boolean deleteAfter) {
        try {
            if (path == null) {
                path = "";
            }
            File file = new File(path + filename);
            Document doc = new SAXBuilder().build(file);
            if (deleteAfter && !file.delete()) LOG.warn("Can't delete file: " + filename);
            return doc;
        } catch (JDOMException | IOException e) {
            LOG.error("Ошибка формирования документа из файла " + filename, e);
            throw new IllegalStateException("Ошибка открытия файла " + path + " " + filename);
        }
    }

    /*Импортируем ответ ФЛК от фонда*/
    public void importFlkAnswer(long monitorId, String filename, Long listEntryId) {
        LOG.info("start import FLK=" + filename);
        IMonitor monitor = startMonitor(monitorId, "Импортируем ФЛК " + filename + ".");
        Document root = getDocumentFromFile(XMLDIR + File.separator, filename, true);
        XMLOutputter out = new XMLOutputter();
        Element rootElement = root.getRootElement();
        List<Element> defs = rootElement.getChildren("PR");
        manager.createNativeQuery("update e2entry set isdefect='0' where listentry_id=:id and (isdeleted is null or isdeleted='0')")
                .setParameter("id", listEntryId).executeUpdate();
        LOG.info("clean defect before flk " + defs.size());
        monitor.setText("ФЛК " + filename + ", записей для расчета: " + defs.size());
        int cnt = 0;
        for (Element el : defs) {
            String entryId = el.getChildText("N_ZAP");
            if (cnt % 100 == 0 && isMonitorCancel(monitor, "Импортировано записей: " + cnt)) return;
            if (isNotNull(entryId)) {
                E2Entry entry = manager.find(E2Entry.class, Long.parseLong(entryId));
                entry.setFondComment(out.outputString(el));
                entry.setIsDefect(true);
                manager.persist(entry);
                manager.persist(new E2EntrySanction(entry, null, "FLK_ERR", true, "ФЛК"));
                cnt++;
            }
        }
        monitor.finish("Импорт ФЛК закончен: " + cnt + " записей из " + defs.size());
    }

    /*Импортируем файл с элмед*/
    public void importElmed(long monitorId, String xmlFilename) {
        try { //делаем только ДД
            Document doc = getDocumentFromFile(XMLDIR + "/", xmlFilename, false);
            if (doc == null) {
                LOG.error("Не удается открыть файл " + xmlFilename);
                return;
            }

            E2ListEntry le = new E2ListEntry();
            le.setName("ELMED_" + xmlFilename);
            String lpuCode = "300052";
            le.setLpuOmcCode(lpuCode);
            manager.persist(le);
            Element root = doc.getRootElement();
            int i = 0;

            List<Element> zaps = root.getChildren("ZAP");
            LOG.info("start import elmed " + xmlFilename + ", found " + zaps.size() + " records");
            IMonitor monitor = startMonitor(monitorId, "Импортируем записи с ЭлМед-а {" + xmlFilename + "}. Всего записей: " + zaps.size());
            for (Element zap : zaps) {
                try {
                    i++;
                    if (i % 100 == 0 && isMonitorCancel(monitor, "Импорт " + i + " записей")) break;
                    E2Entry entry = new E2Entry();
                    Element sl = zap.getChild("SL");
                    entry.setListEntry(le);
                    entry.setEntryType("EXTDISP");
                    entry.setLpuCode(lpuCode);
                    entry.setExternalPatientId(Long.parseLong(zap.getChildText("ID_PAC")));
                    entry.setMedPolicyType(zap.getChildText("VPOLIS"));
                    entry.setMedPolicyNumber(zap.getChildText("NPOLIS"));
                    entry.setInsuranceCompanyCode(zap.getChildText("SMO"));
                    Date startDate = toDate(zap.getChildText("DATE_Z_1"));
                    Date finishDate = toDate(zap.getChildText("DATE_Z_2"));
                    entry.setLastname(zap.getChildText("FAM"));
                    entry.setFirstname(zap.getChildText("IM"));
                    entry.setMiddlename(zap.getChildText("OT"));
                    entry.setBirthDate(toDate(zap.getChildText("DR")));
                    entry.setSex(zap.getChildText("W"));
                    if (isNotNull(zap.getChildText("FAM_P"))) {
                        entry.setKinsmanLastname(zap.getChildText("FAM_P"));
                        entry.setKinsmanFirstname(zap.getChildText("IM_P"));
                        entry.setKinsmanMiddlename(zap.getChildText("OT_P"));
                        entry.setKinsmanBirthDate(toDate(zap.getChildText("DR_P")));
                        entry.setKinsmanSex(zap.getChildText("W_P"));
                        entry.setKinsmanSnils(zap.getChildText("SNILS"));
                    } else {
                        entry.setPatientSnils(zap.getChildText("SNILS"));
                    }
                    entry.setOkatoReg(zap.getChildText("OKATOG"));
                    entry.setCommonNumber(zap.getChildText("ENP"));
                    entry.setPassportType(zap.getChildText("DOCTYPE"));
                    entry.setPassportSeries(zap.getChildText("DOCSER"));
                    entry.setPassportNumber(zap.getChildText("DOCNUM"));

                    entry.setStartDate(startDate);
                    entry.setFinishDate(finishDate);
                    entry.setServiceStream("OBLIGATORYINSURANCE");
                    entry.setMedHelpUsl(getVocByCode(VocE2FondV006.class, finishDate, zap.getChildText("USL_OK")));
                    entry.setMedHelpKind(getVocByCode(VocE2FondV008.class, finishDate, zap.getChildText("VIDPOM")));
                    entry.setIsEmergency(!zap.getChildText("FOR_POM").equals("3"));
                    entry.setIsMobilePolyclinic(false);
                    entry.setHistoryNumber(sl.getChildText("NHISTORY"));
                    entry.setMainMkb(sl.getChildText("DS1"));
                    entry.setFondResult(getVocByCode(VocE2FondV009.class, finishDate, zap.getChildText("RSLT")));
                    entry.setDispResult(getVocByCode(VocE2FondV017.class, finishDate, zap.getChildText("RSLT_D")));
                    entry.setFondIshod(getVocByCode(VocE2FondV012.class, finishDate, zap.getChildText("ISHOD")));
                    VocE2FondV021 medSpec = getVocByCode(VocE2FondV021.class, finishDate, sl.getChildText("PRVS"));
                    if (medSpec != null) {
                        entry.setMedHelpProfile(medSpec.getPolicProfile());
                        entry.setFondDoctorSpecV021(medSpec);
                    }
                    entry.setDoctorSnils(sl.getChildText("IDDOKT"));
                    String doctorCode = sl.getChildText("TABNOM");
                    if (isNotNull(doctorCode)) {
                        PersonalWorkFunction doctor = getWorkFuntionByDoctorCode(doctorCode);
                        if (doctor != null) {
                            entry.setDoctorName(doctor.getWorkFunctionInfo());
                            MisLpu dep = doctor.getWorker().getLpu();
                            entry.setDepartmentId(dep.getId());
                            entry.setDepartmentName(dep.getName());
                            entry.setDepartmentCode(dep.getCodeDepartment());
                        } else {
                            entry.setDoctorName(doctorCode);
                        }
                    } else {
                        LOG.error("Нет докутора в ДД");
                        manager.persist(entry);
                        monitor.setText("Нет докутора в записи " + entry.getId());
                        manager.persist(new E2EntryError(entry, "NO_DISP_DOCTOR"));
                    }

                    entry.setVisitPurpose(getVocByCode(VocE2FondV025.class, finishDate, sl.getChildText("P_CEL")));
                    entry.setIDSP(getVocByCode(VocE2FondV010.class, finishDate, sl.getChildText("IDSP")));
                    String entryTypeCode = "EXTDISP_" + zap.getChildText("VID_SLUCH");
                    VocE2EntrySubType subType = getActualVocByCode(VocE2EntrySubType.class, null, "code='" + entryTypeCode + "'");
                    if (subType != null) {
                        entry.setSubType(subType);
                        entry.setFileType(subType.getFileType());
                        entry.setVidSluch(subType.getVidSluch());
                    }
                    entry.setIDSP(getVocByCode(VocE2FondV010.class, finishDate, zap.getChildText("IDSP")));
                    manager.persist(entry);
                    List<Element> uslList = sl.getChildren("USL");
                    for (Element usl : uslList) {
                        VocMedService vms = getVocByCode(VocMedService.class, finishDate, usl.getChildText("VID_VME"));
                        EntryMedService ms = new EntryMedService(entry, vms);
                        ms.setDoctorSnils(usl.getChildText("IDDOKT_U"));
                        ms.setCost(BigDecimal.ZERO);
                        try {
                            ms.setServiceDate(toDate(usl.getChildText("DATE_IN")));
                        } catch (NullPointerException | ParseException ee) {
                            LOG.warn("no serviceDate");
                        }
                        ms.setDoctorSpeciality(isNotNull(usl.getChildText("PRVS_U")) && !"0".equals(usl.getChildText("PRVS_U"))
                                ? getVocByCode(VocE2FondV021.class, finishDate, usl.getChildText("PRVS_U")) : medSpec);
                        manager.persist(ms);
                    }

                } catch (Exception e1) {
                    monitor.setText("Не удалось загрузить запись: " + new XMLOutputter().outputString(zap));
                    LOG.error(i + " Не удалось загрузить запись:" + new XMLOutputter().outputString(zap));
                }
            }
            LOG.info("finish import elmed");
            monitor.finish("Импорт записей с ЭлМед-а успешно завершен!");

        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
    }

    private PersonalWorkFunction getWorkFuntionByDoctorCode(String tabnom) {
        //находим рабочую функцию по табельному номеру
        if (DOCTORLIST.containsKey(tabnom)) {
            return DOCTORLIST.get(tabnom);
        }
        List<PersonalWorkFunction> personalWorkFunctions = manager.createQuery("from PersonalWorkFunction where code=:code").setParameter("code", tabnom).getResultList();
        PersonalWorkFunction workFunction = personalWorkFunctions.isEmpty() ? null : personalWorkFunctions.get(0);
        DOCTORLIST.put(tabnom, workFunction);
        return workFunction;
    }

    public String getConfigValue(String keyName, String defaultName) {
        return CONFIG.get(keyName, defaultName);
    }

    /*Загружаем ответ от фонда (версия файла {3.1}
     * Добавляем импорт целиком пакета (*.paket)
     * */
    public void importFondMPAnswer(long monitorId, String mpFilename) {
        IMonitor monitor = startMonitor(monitorId, "Импорт дефектов. Файл: " + mpFilename);
        importFondMPAnswer(monitorId, mpFilename, monitor);
        monitor.finish("Закончили импорт дефектов");
    }

    private boolean isTrue(Boolean value) {
        return Boolean.TRUE.equals(value);
    }

    private void importFondMPAnswer(long monitorId, String aMpFilename, IMonitor monitor) {
        try {
            LOG.info("filename = " + aMpFilename);
            if (aMpFilename.toUpperCase().endsWith(".PAKET")) { //like B300026_200205.paket
                //распаковываем в папку. Потом проходимся по каждому файлу в папке
                File unpackedDir = new File(unZip(aMpFilename));
                LOG.info("unpack paket " + unpackedDir.getAbsolutePath());
                for (File mpFile : unpackedDir.listFiles()) {
                    String filename = mpFile.getName();
                    if (filename.toUpperCase().endsWith(".MP")) {
                        monitor.setText("Импорт дефектов. Импортируем файл: " + filename);
                        importFondMPAnswer(monitorId, unpackedDir.getName() + "/" + filename, monitor);
                    }
                }
            } else {
                String dir = unZip(aMpFilename);
                String hFilename = "H" + aMpFilename.substring(aMpFilename.indexOf('M')).replace(".MP", ".XML");
                File hFile = new File(dir + File.separator + hFilename);
                Document doc = new SAXBuilder().build(hFile);
                org.jdom.Element root = doc.getRootElement();
                String ver = root.getChild("ZGLV").getChildText("VERSION");
                if ("3.0".equals(ver)) {
                    throw new IllegalStateException("Импорт в старом формате более не поддерживается!");
                }
                List<Element> zaps = root.getChildren("ZAP");
                Element schet = root.getChild("SCHET");
                String nSchet = schet.getChildText("NSCHET");
                String dSchet = schet.getChildText("DSCHET");
                SimpleDateFormat fromFormat = new SimpleDateFormat("yyyy-MM-dd");
                SimpleDateFormat toFormat = new SimpleDateFormat("dd.MM.yyyy");
                java.sql.Date billDate = new java.sql.Date(fromFormat.parse(dSchet).getTime());
                E2Bill bill = manager.find(E2Bill.class, expertService.getBillIdByDateAndNumber(nSchet, toFormat.format(billDate)));
                bill.setStatus(getActualVocByCode(VocE2BillStatus.class, null, "code='PAID'"));

                int i = 0;
                if (isMonitorCancel(monitor, "Найдено записей для импорта: " + zaps.size())) return;

                BigDecimal totalSum = BigDecimal.ZERO;

                for (Element zap : zaps) {
                    i++;
                    if (i % 100 == 0 && isMonitorCancel(monitor, "Загружено записей: " + i)) break;
                    Element zsl = zap.getChild("Z_SL");
                    List<Element> slList = zsl.getChildren("SL");
                    boolean isComplexCase = false;
                    for (Element sl : slList) {
                        if (isComplexCase) break;
                        Element slId = sl.getChild("SL_ID");
                        Long entryId = Long.parseLong(slId.getText());
                        E2Entry entry = manager.find(E2Entry.class, entryId);
                        if (entry == null || isTrue(entry.getIsDeleted())) {
                            LOG.warn("Ошибка при импорте ответа от фонда - не найдена запись с ИД = " + entryId);
                            continue;
                        }
                        if (entry.getParentEntry() != null) {
                            entry = entry.getParentEntry();
                            isComplexCase = true;
                        }
                        manager.createNativeQuery("delete from E2EntrySanction where entry_id=:entryId").setParameter("entryId", entryId).executeUpdate();
                        entry.setBillNumber(nSchet);
                        entry.setBillDate(billDate);
                        entry.setBill(bill);

                        //Расчет цены случая ФОМС
                        Element commentCalc = sl.getChild("D_COMMENT_CALC");
                        if (commentCalc != null && commentCalc.getChild("root") != null) {

                            Element commentRoot = commentCalc.getChild("root");
                            List<Element> ерт = commentRoot.getChildren();
                            StringBuilder commentError = new StringBuilder();
                            for (Element еб : ерт) {
                                commentError.append(еб.getName()).append(": ").append(еб.getText()).append("\n");
                            }
                            entry.setFondComment(commentError.toString());
                        } else {
                            entry.setFondComment("");
                        }

                        //Добавляем сведения о санкциях
                        if (zsl.getChild("SANK_IT") != null && !zsl.getChildText("SANK_IT").equals("0.00")) { //
                            List<Element> sanks = zsl.getChildren("SANK");
                            ArrayList<String> sanks1 = new ArrayList<>();
                            for (Element sank : sanks) {
                                String key = sank.getChildText("S_OSN");
                                String dopCode = sank.getChildText("S_DOP");
                                if (!sanks1.contains(dopCode)) {
                                    if (!sanctionMap.containsKey(key)) {
                                        sanctionMap.put(key, getActualVocByCode(VocE2Sanction.class, null, "osn='" + key + "'"));
                                    }
                                    String comment = sank.getChildText("SL_ID") + " " + sank.getChildText("S_COM");
                                    manager.persist(new E2EntrySanction(entry, sanctionMap.get(key), dopCode, false, comment));
                                    sanks1.add(dopCode);
                                }
                            }
                            entry.setIsDefect(true);
                        } else {
                            entry.setIsDefect(false);
                            entry.setFondComment(null);
                        }
                        totalSum = totalSum.add(entry.getCost());
                        Element prikMo = zsl.getChild("D_PRIK_MO");
                        if (prikMo != null && isNotNull(prikMo.getText())) { //Проставляем информацию о прик. ЛПУ для формирования МУР
                            entry.setAttachedLpu(prikMo.getText());
                            entry.setAddGroupFld("МУР");
                        } else {
                            entry.setAttachedLpu("");
                            entry.setAddGroupFld("");
                        }
                        manager.persist(entry);
                    }
                }
                LOG.info("По счету №" + bill.getBillNumber() + " сумма = " + totalSum);
                monitor.setText("По счету №" + bill.getBillNumber() + " сумма = " + totalSum);
                bill.setSum(totalSum);
                manager.persist(bill);
                LOG.info("Обновление MP закончено!");
            }
        } catch (Exception e) {
            monitor.error(e.getMessage(), e);
            LOG.error(e.getMessage(), e);
        }
    }

    /**
     * распаковка архива
     */
    private String unZip(String zipFile) {
        StringBuilder sb = new StringBuilder();
        String outputDir = XMLDIR + File.separator + zipFile + "-" + System.currentTimeMillis();
        sb.append("7z e ").append(XMLDIR).append("/").append(zipFile).append(" -o").append(outputDir);

        try {
            Runtime.getRuntime().exec(sb.toString());//arraCmd);
            Thread.sleep(5000); //Заснем, чтобы точно всё распаковалось
        } catch (Exception e) {
            LOG.warn("Похоже, у нас Виндовс. Попробуем запустить 7-zip");
            sb = new StringBuilder().append("\"C:\\Program Files\\7-Zip\\7z.exe\" e ").append(XMLDIR).append("\\").append(zipFile).append(" -o").append(outputDir);
            try {
                LOG.info("sb=" + sb + ", dir=" + outputDir);
                Runtime.getRuntime().exec(sb.toString());
                Thread.sleep(5000); //Заснем, чтобы точно всё распаковалось
            } catch (Exception e1) {
                LOG.warn("NE SMOG :-(");
            }
        }
        return outputDir;


    }

    /**
     * Проверяем, является ли объект NULL либо пустой строкой
     */
    private boolean isNotNull(Object object) {
        if (object == null) return false;
        if (object instanceof String) {
            String ss = (String) object;
            return !ss.trim().equals("");
        } else if (object instanceof Boolean) {
            return (Boolean) object;
        } else if (object instanceof Long) {
            return ((Long) object) > 0L;
        } else if (object instanceof java.sql.Date) {
            return true;
        } else if (object instanceof BigDecimal) {
            return ((BigDecimal) object).compareTo(BigDecimal.ZERO) > 0;
        } else {
            throw new IllegalStateException("Нет преобразования для объекта " + object);
        }
    }

    private <T> T getActualVocByCode(Class aClass, Date aActualDate, String aSqlAdd) {
        String sql = " from " + aClass.getName() + " where ";
        List<T> list;
        if (aActualDate != null) {
            sql += " :actualDate between startDate and finishDate";
            if (isNotNull(aSqlAdd)) {
                sql += " and " + aSqlAdd;
            }
            list = manager.createQuery(sql).setParameter("actualDate", aActualDate).getResultList();
            if (list.isEmpty()) {
                list = manager.createQuery("from " + aClass.getName() + " where finishDate is null and " + aSqlAdd).getResultList();
            }
        } else if (isNotNull(aSqlAdd)) {
            sql += aSqlAdd;
            list = manager.createQuery(sql).getResultList();
        } else {
            throw new IllegalStateException("Необходимо указать дату актуальности либо другое условие");
        }
        if (list.isEmpty()) {
            LOG.error("Не удалось найти действующее значение справочника " + aClass.getCanonicalName() + " с условием " + sql);
            return null;
        } else if (list.size() > 1) {
            LOG.error("Найдено несколько действующих значений справочника " + aClass.getCanonicalName() + " с условием " + sql);
            return null;
        }
        return list.get(0);


    }

    private <T> T getVocByCode(Class aClass, Date aActualDate, String aCode) {
        if (!isNotNull(aCode)) {
            return null;
        }
        String sql = " from " + aClass.getName() + " where ";
        List<T> list;
        if (aActualDate != null) {
            sql += " :actualDate between startDate and finishDate and";
        }
        sql += " code='" + aCode + "'";
        list = manager.createQuery(sql).setParameter("actualDate", aActualDate).getResultList();
        if (list.isEmpty()) {
            list = manager.createQuery("from " + aClass.getName() + " where finishDate is null and code=:code").setParameter("code", aCode).getResultList();
        }

        if (list.isEmpty()) {
            LOG.error("Не удалось найти действующее значение справочника " + aClass.getCanonicalName() + " с условием " + sql);
            return null;
        } else if (list.size() > 1) {
            LOG.error("Найдено несколько действующих значений справочника " + aClass.getCanonicalName() + " с условием " + sql);
            return null;
        }
        return list.get(0);
    }

    /**
     * Выводим сообщение в монитор. Возвращаем - отменен ли монитор
     */
    private boolean isMonitorCancel(IMonitor monitor, String monitorText) {
        monitor.setText(monitorText);
        LOG.info(monitorText);
        if (monitor.isCancelled()) {
            monitor.setText("Проверка прервана пользователем");
            return true;
        }
        return false;
    }

    private IMonitor startMonitor(long monitorId, String message) {
        return monitorService.startMonitor(monitorId, message, 999);
    }
}