package ru.ecom.mis.ejb.service.medcase;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.annotation.EJB;
import javax.annotation.Resource;
import javax.ejb.Remote;
import javax.ejb.SessionContext;
import javax.ejb.Stateless;
import javax.naming.NamingException;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.apache.log4j.Logger;
import org.jboss.annotation.security.SecurityDomain;
import org.jdom.Document;
import org.jdom.IllegalDataException;
import org.jdom.input.SAXBuilder;
import org.json.JSONException;
import org.w3c.dom.Element;

import ru.ecom.diary.ejb.domain.DischargeEpicrisis;
import ru.ecom.diary.ejb.domain.protocol.template.TemplateProtocol;
import ru.ecom.diary.ejb.service.template.ITemplateProtocolService;
import ru.ecom.diary.ejb.service.template.TemplateProtocolServiceBean;
import ru.ecom.ejb.sequence.service.ISequenceService;
import ru.ecom.ejb.services.entityform.EntityFormException;
import ru.ecom.ejb.services.entityform.IEntityForm;
import ru.ecom.ejb.services.entityform.ILocalEntityFormService;
import ru.ecom.ejb.services.monitor.ILocalMonitorService;
import ru.ecom.ejb.services.monitor.IMonitor;
import ru.ecom.ejb.services.query.WebQueryResult;
import ru.ecom.ejb.services.util.ConvertSql;
import ru.ecom.ejb.util.injection.EjbEcomConfig;
import ru.ecom.ejb.util.injection.EjbInjection;
import ru.ecom.ejb.xml.XmlUtil;
import ru.ecom.expomc.ejb.domain.med.VocDiagnosis;
import ru.ecom.expomc.ejb.domain.med.VocIdc10;
import ru.ecom.jaas.ejb.domain.SecPolicy;
import ru.ecom.mis.ejb.domain.licence.voc.VocDocumentParameter;
import ru.ecom.mis.ejb.domain.licence.voc.VocDocumentParameterConfig;
import ru.ecom.mis.ejb.domain.lpu.MisLpu;
import ru.ecom.mis.ejb.domain.medcase.HospitalMedCase;
import ru.ecom.mis.ejb.domain.medcase.MedCase;
import ru.ecom.mis.ejb.domain.medcase.MedCaseMedPolicy;
import ru.ecom.mis.ejb.domain.medcase.SurgicalOperation;
import ru.ecom.mis.ejb.domain.medcase.hospital.TemperatureCurve;
import ru.ecom.mis.ejb.domain.medcase.voc.VocAcuityDiagnosis;
import ru.ecom.mis.ejb.domain.medcase.voc.VocDiagnosisRegistrationType;
import ru.ecom.mis.ejb.domain.medcase.voc.VocPrimaryDiagnosis;
import ru.ecom.mis.ejb.domain.patient.MedPolicy;
import ru.ecom.mis.ejb.domain.psychiatry.CompulsoryTreatmentAggregate;
import ru.ecom.mis.ejb.domain.report.AggregateHospitalReport;
import ru.ecom.mis.ejb.form.medcase.hospital.ExtHospitalMedCaseForm;
import ru.ecom.mis.ejb.form.medcase.hospital.HospitalMedCaseForm;
import ru.ecom.mis.ejb.form.medcase.hospital.SurgicalOperationForm;
import ru.ecom.mis.ejb.form.medcase.hospital.interceptors.HospitalMedCaseViewInterceptor;
import ru.ecom.mis.ejb.form.medcase.hospital.interceptors.StatisticStubStac;
import ru.ecom.mis.ejb.form.patient.MedPolicyForm;
import ru.ecom.mis.ejb.service.patient.QueryClauseBuilder;
import ru.ecom.poly.ejb.domain.protocol.Protocol;
import ru.ecom.poly.ejb.services.MedcardServiceBean;
import ru.ecom.report.util.XmlDocument;
import ru.nuzmsh.util.StringUtil;
import ru.nuzmsh.util.format.DateFormat;
/**
 * Created by IntelliJ IDEA.
 * User: STkacheva
 * Date: 26.01.2007
 */
@Stateless
@Remote(IHospitalMedCaseService.class)
@SecurityDomain("other")
public class HospitalMedCaseServiceBean implements IHospitalMedCaseService {
	
    private final static Logger LOG = Logger.getLogger(MedcardServiceBean.class);
    private final static boolean CAN_DEBUG = LOG.isDebugEnabled();
    
    public String getDischargeEpicrisis(long aMedCaseId) {
    	return HospitalMedCaseViewInterceptor.getDischargeEpicrisis(aMedCaseId, theManager) ;
    }
    
    public static boolean saveDischargeEpicrisisByCase(MedCase aMedCase,String aDischargeEpicrisis,EntityManager aManager) {
    	aManager.createNativeQuery("delete from diary d where d.medcase_id= "+aMedCase.getId()+" and upper(d.dtype)='DISCHARGEEPICRISIS' ").executeUpdate() ;
    	int len = 15000 ;
    	int lend = aDischargeEpicrisis.length() ;
    	int cnt = lend/len;
    	System.out.println("len = "+len) ;
    	System.out.println("lend = "+lend) ;
    	System.out.println("cnt = "+cnt) ;
    	System.out.println("cnt%len = "+(lend%len)) ;
    	for (int i=0;i<cnt;i++) {
    		DischargeEpicrisis prot = new DischargeEpicrisis() ;
    		System.out.println("record1="+aDischargeEpicrisis.substring(i*len,(i+1)*len<lend?(i+1)*len:lend)) ;
    		prot.setRecord(aDischargeEpicrisis.substring(i*len,(i+1)*len<lend?(i+1)*len:lend)) ;
    		prot.setMedCase(aMedCase) ;
    		aManager.persist(prot);
    	}
    	if (lend%len>0) {
    		DischargeEpicrisis prot = new DischargeEpicrisis() ;
    		System.out.println("record2="+aDischargeEpicrisis.substring(len*cnt)) ;
    		prot.setRecord(aDischargeEpicrisis.substring(len*cnt)) ;
    		prot.setMedCase(aMedCase) ;
    		aManager.persist(prot);
    	}
		try {
			TemplateProtocolServiceBean bean = new TemplateProtocolServiceBean();
			bean.sendProtocolToExternalResource(null,aMedCase, aDischargeEpicrisis, aManager);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return true ;
    	
    }
    public static boolean saveDischargeEpicrisis(long aMedCaseId,String aDischargeEpicrisis,EntityManager aManager) {
    	HospitalMedCase medCase = aManager.find(HospitalMedCase.class, aMedCaseId) ;
    	return saveDischargeEpicrisisByCase(medCase, aDischargeEpicrisis, aManager) ; 
    }
    private String getText(org.jdom.Element aEl, String aParameter) {
    	return aEl!=null?
    			(aEl.getChild(aParameter)!=null && !aEl.getChild(aParameter).getText().trim().equals("")?(aEl.getChild(aParameter).getText().trim()):null)
    			:
    				null ;
    }
    private String getChildText(org.jdom.Element aEl, String aParameter) {
    	StringBuilder ret = new StringBuilder() ;
    	if (aEl!=null &&( aEl.getChild(aParameter)!=null) ) { 
    		for (org.jdom.Element e : (List<org.jdom.Element>) aEl.getChild(aParameter).getChildren() ) {
    			ret.append(", ").append(e.getName()).append(" ").append(e.getText()) ;
    		}
    	}
    	return ret.substring(2) ;
    }
    private String getDate(org.jdom.Element aEl,String aParameter) {
    		String value=getText(aEl,aParameter) ;
    		if (value==null) return null ;
    		if (value instanceof String) {
        		SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd") ;
    			long l = 0 ;
    			
    			try {
    				l=f.parse(value).getTime() ;
    			} catch(Exception e) {
    				e.printStackTrace() ;
            		//String aFormat1 =  ;
        			f = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss") ;
    				try {
    					l=f.parse(value).getTime() ;
    				
	    			} catch(Exception e1) {
	    				e1.printStackTrace() ;
	    				return null ;
	    			}
    			}
    			return f.format(l) ;
    		}
    		return null ;
    	
    }
    private Object getTime(org.jdom.Element aEl,String aParameter) {
    	String value=getText(aEl,aParameter) ;
    	try {
    		return value==null || value.equals("")?null:value ;
    	} catch (Exception e) {
    		e.printStackTrace() ;
    		return null ;
    	}
    }

    
    public String importFileDataFond(long aMonitorId, String aFilename) throws Exception {
    	File f = new File(aFilename) ;
    	InputStream in = null;
    	String type =null;
    	boolean isErrorFile = false ;
    	final String filename ;
    	String contentType = null ;
    	String filenameError = null ;
    	List<WebQueryResult> list = new LinkedList<WebQueryResult>() ;

        try {
        	org.jdom.Document doc = new SAXBuilder().build(f);
           	XmlDocument xmlDocError = new XmlDocument() ;
           	org.w3c.dom.Element root_error = xmlDocError.newElement(xmlDocError.getDocument(), "ZL_LIST", null);
            org.jdom.Element parConfigElement = doc.getRootElement();
            //System.out.println(new StringBuilder().append("		root=").append(parConfigElement).toString());
            Long i =Long.valueOf(1) ; int ind=0 ;
            for (Object o : parConfigElement.getChildren()) {
                org.jdom.Element parEl = (org.jdom.Element) o;
                if ("ZGLV".equals(parEl.getName())) {
                	type = parEl.getChild("FILENAME").getText().trim().toUpperCase().substring(0,2) ;
                	System.out.println("type-------------"+type) ;
                } else if("NPR".equals(parEl.getName())) {
                	//System.out.println("parel=="+parEl.getChildren("REFREASON")) ;
                	if (parEl.getChildren("REFREASON").isEmpty()) {
                		//System.out.println("par===el=="+parEl.getChildren("REFREASON")) ;
                    	WebQueryResult wqr = new WebQueryResult() ;
                    	wqr.set1(getText(parEl,"N_NPR")) ;
                    	wqr.set2(getDate(parEl,"D_NPR")) ;
                    	wqr.set3(getText(parEl,"FOR_POM")) ;
                    	wqr.set4(getText(parEl,"NCODE_MO")) ;
                    	wqr.set5(getText(parEl,"DCODE_MO")) ;
                    	wqr.set6(getText(parEl,"VPOLIS")) ;
                    	wqr.set7(getText(parEl,"SPOLIS")) ;
                    	wqr.set8(getText(parEl,"NPOLIS")) ;
                    	wqr.set9(getText(parEl,"SMO")) ;
                    	wqr.set10(getText(parEl,"SMO_OGRN")) ;
                    	wqr.set11(getText(parEl,"SMO_OK")) ;
                    	wqr.set12(getText(parEl,"SMO_NAM")) ;
                    	wqr.set13(getText(parEl,"FAM")) ;
                    	wqr.set14(getText(parEl,"IM")) ;
                    	wqr.set15(getText(parEl,"OT")) ;
                    	wqr.set16(getText(parEl,"W")) ;
                    	wqr.set17(getDate(parEl,"DR")) ;
                    	wqr.set18(getText(parEl,"CT")) ;
                    	wqr.set19(getText(parEl,"DS1")) ;
                    	wqr.set20(getText(parEl,"PROFIL")) ;
                    	wqr.set21(getText(parEl,"IDDOKT")) ;
                    	if (type.equals("N1")) {
                    		wqr.set22(getDate(parEl,"DATE_1")) ;
                    	} else {
                    		wqr.set23(getDate(parEl,"DATE_1")) ;
                    	}
                    	wqr.set24(getText(parEl,"NHISTORY")) ;
                    	wqr.set25(getTime(parEl,"TIME_1")) ;
                    	wqr.set26(getDate(parEl,"DATE_2")) ;
                    	wqr.set27(getText(parEl,"DET")) ;
                    	wqr.set28(getText(parEl,"USL_OK")) ;
                    	if (wqr.get1()!=null && !(""+wqr.get1()).equals("")){
                    		list.add(wqr) ;
                    	}
                	} else {
                		isErrorFile = true ;
                		org.w3c.dom.Element zap = xmlDocError.newElement(root_error, "NPR", null);
                		String[] flds ={"N_NPR","D_NPR","FOR_POM","NCODE_MO","DCODE_MO"
                				,"VPOLIS","SPOLIS","NPOLIS","SMO","SMO_OGRN","SMO_OK"
                				,"SMO_NAM","FAM","IM","OT","W","DR","CT","DS1"
                				,"PROFIL","IDDOKT","DATE_1","NHISTORY","TIME_1","DATE_2"
                		} ;
                		for (String fld:flds) {
                			XmlUtil.recordElementInDocumentXml(xmlDocError,zap,fld,getText(parEl,fld),true,"") ;
                		}
                		String fld = "REFREASON" ;
                		XmlUtil.recordElementInDocumentXml(xmlDocError,zap,fld,getChildText(parEl,fld),true,"") ;
                	}
                }
            }
        } catch (Exception e) {
        	
        }
        
        
    	return null ;
    }
    public String importDataFond(long aMonitorId, String aFileType,List<WebQueryResult> aList) {
    	IMonitor monitor = null;
    	try {
    		System.out.println("start importDataFond");
    		
    		monitor = theMonitorService.startMonitor(aMonitorId, "ImportFondMonitor", aList.size());
    		monitor.advice(20) ;
    		System.out.println("start importDataFond");
    		int size = aList.size()/80 ;
    		
    		for (int i=0;i<aList.size();i++) {
    			WebQueryResult wqr=aList.get(i) ;
    			if (monitor.isCancelled()) {
    				throw new IllegalMonitorStateException("Прервано пользователем");
    			}
    			//Object id = null ;
    			if (wqr.get1()!=null) {
    				List<Object> lf = theManager.createNativeQuery("select id from HospitalDataFond where numberFond='"+wqr.get1()+"' order by id desc").setMaxResults(1).getResultList() ;
    				//HospitalDataFond hdf ;
    				Object id = null ;
    				if (!lf.isEmpty()) {
    					id=lf.get(0) ;
    				}
    				StringBuilder sql1 = new StringBuilder() ;
    				StringBuilder sql2 = new StringBuilder() ;
    				// create
    				if (id==null) {
    					sql1.append("insert into HospitalDataFond (") ;
    					if (wqr.get1()!=null) {sql1.append("numberFond,") ; sql2.append("'").append(wqr.get1()).append("',") ;}
    					if (wqr.get2()!=null) {sql1.append("directDate,") ; sql2.append("to_date('").append(wqr.get2()).append("','yyyy-mm-dd'),") ;}
    					if (wqr.get3()!=null) {sql1.append("formHelp,") ; sql2.append("'").append(wqr.get3()).append("',") ;}
    					if (wqr.get4()!=null) {sql1.append("OrderLpuCode,") ; sql2.append("'").append(wqr.get4()).append("',") ;}
    					if (wqr.get5()!=null) {sql1.append("DirectLpuCode,") ; sql2.append("'").append(wqr.get5()).append("',") ;}
    					if (wqr.get6()!=null) {sql1.append("TypePolicy,") ; sql2.append("'").append(wqr.get6()).append("',") ;}
    					if (wqr.get7()!=null) {sql1.append("SeriesPolicy,") ; sql2.append("'").append(wqr.get7()).append("',") ;}
    					if (wqr.get8()!=null) {sql1.append("NumberPolicy,") ; sql2.append("'").append(wqr.get8()).append("',") ;}
    					if (wqr.get9()!=null) {sql1.append("Smo,") ; sql2.append("'").append(wqr.get9()).append("',") ;}
    					if (wqr.get10()!=null) {sql1.append("SmoOgrn,") ; sql2.append("'").append(wqr.get10()).append("',") ;}
    					if (wqr.get11()!=null) {sql1.append("SmoOkato,") ; sql2.append("'").append(wqr.get11()).append("',") ;}
    					if (wqr.get12()!=null) {sql1.append("SmoName,") ; sql2.append("'").append(wqr.get12()).append("',") ;}
    					if (wqr.get13()!=null) {sql1.append("Lastname,") ; sql2.append("'").append(wqr.get13()).append("',") ;}
    					sql1.append("Firstname,") ; sql2.append("'").append(wqr.get14()!=null?wqr.get14():"").append("',") ;
    					sql1.append("Middlename,") ; sql2.append("'").append(wqr.get15()!=null?wqr.get15():"").append("',") ;
    					if (wqr.get16()!=null) {
    						String sex = ""+wqr.get16() ;
    						if (sex.equals("М")) {sex="1";} else if (sex.equals("Ж")) {sex="2" ;}
    						List<Object> l = theManager.createNativeQuery("select id from VocSex where omcCode='"+sex+"'").setMaxResults(1).getResultList() ;
    						if (!l.isEmpty()) {sql1.append("sex_id,") ; sql2.append("'").append(l.get(0)).append("',") ;} ;
    					}
    					if (wqr.get17()!=null) {sql1.append("Birthday,") ; sql2.append("to_date('").append(wqr.get17()).append("','yyyy-mm-dd'),") ;}
    					if (wqr.get18()!=null) {sql1.append("Phone,") ; sql2.append("'").append(wqr.get18()).append("',") ;}
    					if (wqr.get19()!=null) {sql1.append("Diagnosis,") ; sql2.append("'").append(wqr.get19()).append("',") ;}
    					if (wqr.get20()!=null) {sql1.append("Profile,") ; sql2.append("'").append(wqr.get20()).append("',") ;}
    					if (wqr.get21()!=null) {sql1.append("Snils,") ; sql2.append("'").append(wqr.get21()).append("',") ;}
    					if (wqr.get22()!=null) {sql1.append("PreHospDate,") ; sql2.append("to_date('").append(wqr.get22()).append("','yyyy-mm-dd'),") ;}
    					if (wqr.get23()!=null) {sql1.append("HospDate,") ; sql2.append("to_date('").append(wqr.get23()).append("','yyyy-mm-dd'),") ;}
    					if (wqr.get24()!=null) {sql1.append("StatCard,") ; sql2.append("'").append(wqr.get24()).append("',") ;}
    					if (wqr.get25()!=null) {sql1.append("HospTime,") ; sql2.append("'").append(wqr.get25()).append("',") ;}
    					if (wqr.get26()!=null) {sql1.append("HospDischargeDate,") ; sql2.append("to_date('").append(wqr.get26()).append("','yyyy-mm-dd'),") ;}
    					if (wqr.get27()!=null) {sql1.append("ForChild,") ; sql2.append("'").append(wqr.get27()).append("',") ;}
    					if (wqr.get28()!=null) {sql1.append("BedSubType,") ; sql2.append("'").append(wqr.get28()).append("',") ;}
    					sql1.append("Voc_time,") ; sql2.append("'0',") ;
    					if (aFileType.equals("N1")) {
    						sql1.append("IsTable1") ; sql2.append("'1'") ;
    					} else if (aFileType.equals("N2")) {
    						sql1.append("IsTable2,") ; sql2.append("'1',") ;
    						sql1.append("deniedhospital,") ; sql2.append("null,") ;
    						sql1.append("IsTable4,") ; sql2.append("null,") ;
    						sql1.append("IsTable5") ; sql2.append("null") ;
    					} else if (aFileType.equals("N3")) {
    						sql1.append("IsTable3") ; sql2.append("'1'") ;
    					} else if (aFileType.equals("N4")) {
    						sql1.append("IsTable4") ; sql2.append("'1'") ;
    					} else if (aFileType.equals("N5")) {
    						sql1.append("IsTable5") ; sql2.append("'1'") ;
    					}
    					sql1.append(") values (").append(sql2).append(")") ;
    					//update
    				} else {
    					sql1.append("update HospitalDataFond set ") ;
    					if (wqr.get2()!=null) {sql1.append("directDate").append("=").append("to_date('").append(wqr.get2()).append("','yyyy-mm-dd')").append(",") ;}
    					if (wqr.get3()!=null) {sql1.append("formHelp") .append("=").append("'").append(wqr.get3()).append("'").append(",") ;}
    					if (wqr.get4()!=null) {sql1.append("OrderLpuCode") .append("=").append("'").append(wqr.get4()).append("'").append(",") ;}
    					if (wqr.get5()!=null) {sql1.append("DirectLpuCode") .append("=").append("'").append(wqr.get5()).append("'").append(",") ;}
    					if (wqr.get6()!=null) {sql1.append("TypePolicy") .append("=").append("'").append(wqr.get6()).append("'").append(",") ;}
    					if (wqr.get7()!=null) {sql1.append("SeriesPolicy") .append("=").append("'").append(wqr.get7()).append("'").append(",") ;}
    					if (wqr.get8()!=null) {sql1.append("NumberPolicy") .append("=").append("'").append(wqr.get8()).append("'").append(",") ;}
    					if (wqr.get9()!=null) {sql1.append("Smo") .append("=").append("'").append(wqr.get9()).append("'").append(",") ;}
    					if (wqr.get10()!=null) {sql1.append("SmoOgrn") .append("=").append("'").append(wqr.get10()).append("'").append(",") ;}
    					if (wqr.get11()!=null) {sql1.append("SmoOkato") .append("=").append("'").append(wqr.get11()).append("'").append(",") ;}
    					if (wqr.get12()!=null) {sql1.append("SmoName") .append("=").append("'").append(wqr.get12()).append("'").append(",") ;}
    					if (wqr.get13()!=null) {sql1.append("Lastname") .append("=").append("'").append(wqr.get13()).append("'").append(",") ;}
    					if (wqr.get14()!=null) {sql1.append("Firstname") .append("=").append("'").append(wqr.get14()).append("'").append(",") ;}
    					if (wqr.get15()!=null) {sql1.append("Middlename") .append("=").append("'").append(wqr.get15()).append("'").append(",") ;}
    					if (wqr.get16()!=null) {
    						String sex = ""+wqr.get16() ;
    						if (sex.equals("М")) {sex="1";} else if (sex.equals("Ж")) {sex="2" ;}
    						List<Object> l = theManager.createNativeQuery("select id from VocSex where omcCode='"+sex+"'").setMaxResults(1).getResultList() ;
    						if (!l.isEmpty()) {sql1.append("sex_id") .append("=").append("'").append(l.get(0)).append("'").append(",") ;} ;
    					}
    					if (wqr.get17()!=null) {sql1.append("Birthday").append("=").append("to_date('").append(wqr.get17()).append("','yyyy-mm-dd')").append(",") ;}
    					if (wqr.get18()!=null) {sql1.append("Phone").append("=").append("'").append(wqr.get18()).append("'").append(",") ;}
    					if (wqr.get19()!=null) {sql1.append("Diagnosis").append("=").append("'").append(wqr.get19()).append("'").append(",") ;}
    					if (wqr.get20()!=null) {sql1.append("Profile").append("=").append("'").append(wqr.get20()).append("'").append(",") ;}
    					if (wqr.get21()!=null) {sql1.append("Snils").append("=").append("'").append(wqr.get21()).append("'").append(",") ;}
    					if (wqr.get22()!=null) {sql1.append("PreHospDate").append("=").append("to_date('").append(wqr.get22()).append("','yyyy-mm-dd')").append(",") ;}
    					if (wqr.get23()!=null) {sql1.append("HospDate").append("=").append("to_date('").append(wqr.get23()).append("','yyyy-mm-dd')").append(",") ;}
    					if (wqr.get24()!=null) {sql1.append("StatCard").append("=").append("'").append(wqr.get24()).append("'").append(",") ;}
    					if (wqr.get25()!=null) {sql1.append("HospTime").append("=").append("cast('").append(wqr.get25()).append("' as time)").append(",") ;}
    					if (wqr.get26()!=null) {sql1.append("HospDischargeDate").append("=").append("to_date('").append(wqr.get26()).append("','yyyy-mm-dd')").append(",") ;}
    					if (aFileType.equals("N1")) {
    						sql1.append("IsTable1").append("=").append("'1'") ;
    					} else if (aFileType.equals("N2")) {
    						sql1.append("IsTable2").append("=").append("'1',") ;
    						sql1.append("deniedhospital").append("=").append("null,") ;
    						sql1.append("IsTable4").append("=").append("null,") ;
    						sql1.append("IsTable5").append("=").append("null") ;
    					} else if (aFileType.equals("N3")) {
    						sql1.append("IsTable3").append("=").append("'1'") ;
    					} else if (aFileType.equals("N4")) {
    						sql1.append("IsTable4").append("=").append("'1'") ;
    					} else if (aFileType.equals("N5")) {
    						sql1.append("IsTable5").append("=").append("'1'") ;
    					}
    					sql1.append(" where id=").append(id) ;
    				}
    				//try {
    				//System.out.println("sql="+sql1) ;
    				theManager.createNativeQuery(sql1.toString()).executeUpdate() ;
    				//}catch(Exception e) {
    				//	e.printStackTrace() ;
    				//	if (id==null) {
    				//theManager.createNativeQuery("alter table hospitaldatafond alter column id set default nextval('hospitaldatafond_sequence')").executeUpdate() ;
    				//theManager.createNativeQuery(sql1.toString()).executeUpdate() ;
    				//	}
    				//}
    			}
    			if(i%10==0) monitor.setText(new StringBuilder().append("Импортируется: ").append(i+" ").append(wqr.get1()).append(" ").append(wqr.get2()).append("...").toString());
    			if(size>0&&i%size==0) monitor.advice(1);
    			
    			if (i % 300 == 0) {
    				monitor.setText("Импортировано " + i);
    			}
    		}
    		System.out.println("==== Закончили импорт!");
    		monitor.finish("");
    	} catch (Exception e) {
    		e.printStackTrace();
    		if(monitor!=null) monitor.setText(e+"");
    		throw new IllegalStateException(e) ;
    	}
    	return "" ;
    }
    public void addMonitor(long aMonitorId, int aInt) {
    	IMonitor monitor = null;
    	try {
    		monitor = theMonitorService.getMonitor(aMonitorId) ;
    		monitor.advice(aInt) ;
    	}catch (Exception e) {
    		
    	}
    }
    public void finishMonitor(long aMonitorId) {
    	IMonitor monitor = null;
    	try {
    		monitor = theMonitorService.getMonitor(aMonitorId) ;
    		monitor.finish("") ;
    	}catch (Exception e) {
    		
    	}
    }
    public void startMonitor(long aMonitorId) {
    	IMonitor monitor = null;
    	try {
    		monitor = theMonitorService.startMonitor(aMonitorId, "Обработка данных", 100);
    	}catch (Exception e) {
    		
    	}
    }
    public String importDataFondForDBF(long aMonitorId) {
    	IMonitor monitor = null;
    	try {
    		monitor = theMonitorService.startMonitor(aMonitorId, "Обработка данных", 100);
    		List<Object[]> lf = theManager.createNativeQuery("select id,fio from HospitalDataFond where fio is not null and lastname is null").setMaxResults(1).getResultList() ;
    		
    		for (Object[] obj:lf) {
    			String[] fio = new StringBuilder().append(obj).toString().split(")")[1].split(" ") ;
    			String birthday = new StringBuilder().append(obj).toString().split(")")[1].replaceAll(")", "").trim() ;
    			theManager.createNativeQuery(new StringBuilder()
	    			.append("update hospitaldatafond set withoutHosp='1',lastname='").append(fio[0])
	    			.append("',firstname='").append(fio[1])
	    			.append("',middlename='").append(fio.length>2?fio[2]:"") 
	    			.append("',birthday=to_date('").append(birthday).append("','dd.mm.yyyy')")
	    			.append(" where id=").append(obj[0]) 
	    			.toString()).executeUpdate() ;
    		}
			monitor.advice(50) ;
	    	monitor.finish("") ;
    	} catch (Exception e) {
            if(monitor!=null) monitor.setText(e+"");
            throw new IllegalStateException(e) ;
        }
    	return "" ;
    }
    
    
    public void refreshCompTreatmentReportByPeriod(String aEntranceDate,String aDischargeDate,long aIdMonitor) {
    	IMonitor monitor = null;
    	java.util.Date dt = new java.util.Date() ;
    	String curDate = DateFormat.formatToDate(dt) ;
    	try {
    		monitor = theMonitorService.startMonitor(aIdMonitor, "Обработка данных за период: "+aEntranceDate+" "+aDischargeDate, 100);
    		
    		StringBuilder sqlD = new StringBuilder() ;
    		sqlD.append(" delete from CompulsoryTreatmentAggregate where entrancehospdate <= (to_date('").append(aDischargeDate).append("','dd.mm.yyyy')-1)") ;
    		sqlD.append(" and (dischargehospdate >= (to_date('").append(aEntranceDate).append("','dd.mm.yyyy')-1) or dischargehospdate is null)") ;
    		theManager.createNativeQuery(sqlD.toString()).executeUpdate() ;
    		StringBuilder sql = new StringBuilder() ;
    		sql.append("select ct.ordernumber,ct.carecard_id,pcc.patient_id from CompulsoryTreatment ct ");
    		sql.append(" left join PsychiatricCareCard pcc on pcc.id=ct.careCard_id ");
    		sql.append(" where coalesce(ct.registrationdate,ct.DecisionDate)<=to_date('").append(aDischargeDate).append("','dd.mm.yyyy') "); 
    		sql.append(" and coalesce(ct.RegistrationReplaceDate,ct.Datereplace,to_date('").append(aEntranceDate).append("','dd.mm.yyyy'))>=to_date('").append(aEntranceDate).append("','dd.mm.yyyy') and ct.kind_id in (2,3) ");
    		sql.append(" group by ct.ordernumber,ct.carecard_id,pcc.patient_id") ;
    		
    		monitor.advice(20) ;
    		
    		List<Object[]> list = theManager.createNativeQuery(sql.toString()).getResultList() ;
    		int size = list.size()/80 ;
    		
    		for (int ii=0;ii<list.size();ii++) {
    			Object[] obj = list.get(ii) ;
    			if (monitor.isCancelled()) {
    				throw new IllegalMonitorStateException("Прервано пользователем");
    			}
    			StringBuilder sql1 = new StringBuilder() ;
    			sql1.append("select to_char(ct1.registrationdate,'dd.mm.yyyy') as r0egistrationdate,to_char(ct1.registrationreplacedate,'dd.mm.yyyy') as r1egistrationreplacedate");
    			sql1.append(",to_char(ct1.decisiondate,'dd.mm.yyyy') as d2ecisiondate,to_char(ct1.datereplace,'dd.mm.yyyy') as d3atereplace ") ;
    			sql1.append(" ,(select count(*) from CompulsoryTreatment ct2 where ct2.careCard_id='").append(obj[1]).append("' and ct2.orderNumber='").append(obj[0]).append("' and ct2.kind_id in (2,3) and ct1.decisiondate=ct2.datereplace) as p4revCT") ;
    			//sql1.append(" ,(select count(*) from CompulsoryTreatment ct2 where ct2.careCard_id='").append(obj[1]).append("' and ct2.orderNumber='").append(obj[0]).append("' and ct2.kind_id in (2,3) and ct2.decisiondate=ct1.datereplace) as nextCT") ;
    			sql1.append(" ,(select list(''||mc.id) from medcase mc where mc.patient_id=pcc.patient_id and upper(mc.dtype)='HOSPITALMEDCASE' and case when (mc.datestart <= coalesce(ct1.datereplace,current_date)") ;
    			sql1.append(" and coalesce(mc.datefinish,current_date) >=ct1.decisiondate  ) then 1 else 0 end = 1) as s5ls") ;
    			sql1.append(" ,vs.omcCode as f6sex") ;
    			sql1.append(" from CompulsoryTreatment ct1 ") ;
    			sql1.append(" left join PsychiatricCareCard pcc on pcc.id=ct1.careCard_id") ;
    			sql1.append(" left join Patient pat on pat.id=pcc.patient_id") ;
    			sql1.append(" left join VocSex vs on pat.sex_id=vs.id") ;
    			sql1.append(" where ct1.careCard_id='").append(obj[1]).append("' and ct1.orderNumber='").append(obj[0]).append("' and ct1.kind_id in (2,3)") ;
    			sql1.append(" order by ct1.decisiondate") ;
    			List<Object[]> l1 = theManager.createNativeQuery(sql1.toString()).getResultList() ;
    			List<Object[]> l2 = new ArrayList<Object[]>() ;
    			System.out.println("l1 size="+l1.size()) ;
    			
    			for (Object[] o1:l1) {
    				theManager.createNativeQuery("delete from CompulsoryTreatmentAggregate where sls in ("+o1[5]+")").executeUpdate() ;
    				System.out.println("o1="+o1[0]+" - "+o1[1]+" - "+o1[2]+" - "+o1[3]+" - "+o1[4]+" - "+o1[5]+" - ") ;
    				boolean prevCT = (o1[4]!=null && Integer.valueOf(""+o1[4]).intValue()==1)?true:false ;
    				//boolean nextCT = (o1[5]!=null && Integer.valueOf(""+o1[5]).intValue()==1)?true:false ;
    				if (!prevCT) {
    					System.out.println("no prev") ;
    					l2.add(o1) ;
    				} else {
    					
    					if (l2.size()>0) {
    						System.out.println("yes prev") ;
    						l2.get(l2.size()-1)[1]=o1[1] ;
    						l2.get(l2.size()-1)[3]=o1[3] ;
    					} else {
    						System.out.println("yes prev -") ;
    						l2.add(o1);
    					}
    				}
    				
    			}
    			long hospInd = Long.valueOf(0) ;
    			for (Object[] o2:l2) {
    				hospInd++ ;
    				
    				System.out.println("o1="+o2[0]+" - "+o2[1]+" - "+o2[2]+" - "+o2[3]+" - "+o2[4]+" - "+o2[5]+" - ") ;
    				
    				StringBuilder sql3 = new StringBuilder() ;
    				sql3.append("select mc.parent_id as s0ls,mc.id as s1lo,case when mc.datestart>to_date('").append(o2[2]==null?curDate:o2[2]).append("','dd.mm.yyyy') then '1' else null end as s2rdate");
    				sql3.append(", to_char(mc.datestart,'dd.mm.yyyy') as d3atestart,to_char(mc.transferdate,'dd.mm.yyyy') as t4ransferdate,to_char(mc.datefinish,'dd.mm.yyyy') as d5atefinish,mc.department_id as d6epartment") ;
    				sql3.append(",case when ('").append(o2[3]==null?"":o2[3]).append("'='' or '").append(o2[3]==null?"":o2[3]).append("'!='' and coalesce(mc.transferdate,mc.datefinish) is not null and coalesce(mc.transferdate,mc.datefinish)<to_date('").append(o2[3]==null?curDate:o2[3]).append("','dd.mm.yyyy')) then to_char(coalesce(mc.transferdate,mc.datefinish),'dd.mm.yyyy') else '").append(o2[3]==null?"":o2[3]).append("' end  as s7rdate");
    				sql3.append(" ,list(distinct case when vdrtD.code='4' and vpdD.code='1' then mkbD.code else null end) as f8depDiag") ;
    		    	sql3.append(" ,list(distinct case when vdrt.code='2' then mkb.code else null end) as f9orderDiag") ;
    		    	sql3.append(" ,list(distinct case when vdrt.code='3' and vpd.code='1' then mkb.code else null end) as f10dischargeDiag") ;
    		    	sql3.append(" ,case when vhr.code='11' then cast('1' as int) else null end as f11isDeath") ;
    		    	
    				sql3.append(" from medcase mc") ;
    				sql3.append(" left join medcase sls on mc.parent_id=sls.id") ;
    		    	sql3.append(" left join bedfund bf on bf.id=mc.bedfund_id") ;
    		    	sql3.append(" left join vocbedsubtype vbst on bf.bedsubtype_id=vbst.id") ;
    		    	sql3.append(" left join mislpu ml on ml.id=mc.department_id") ;
    		    	sql3.append(" left join diagnosis diag on sls.id=diag.medcase_id") ;
    		    	sql3.append(" left join vocidc10 mkb on mkb.id=diag.idc10_id") ;
    		    	sql3.append(" left join VocDiagnosisRegistrationType vdrt on vdrt.id=diag.registrationType_id") ;
    		    	sql3.append(" left join VocPriorityDiagnosis vpd on vpd.id=diag.priority_id") ;
    		    	sql3.append(" left join diagnosis diagD on mc.id=diagD.medcase_id") ;
    		    	sql3.append(" left join vocidc10 mkbD on mkbD.id=diagD.idc10_id") ;
    		    	sql3.append(" left join VocDiagnosisRegistrationType vdrtD on vdrtD.id=diagD.registrationType_id") ;
    		    	sql3.append(" left join VocPriorityDiagnosis vpdD on vpdD.id=diagD.priority_id") ;
    		    	sql3.append(" left join VocHospitalizationResult  vhr on vhr.id=sls.result_id") ;
    		    	sql3.append(" left join VocHospitalization  vh on vh.id=sls.hospitalization_id") ;
    		    	sql3.append(" left join VocHospType vht on vht.id=sls.targetHospType_id") ;
    		    	sql3.append(" left join VocHospType vhtHosp on vhtHosp.id=sls.hospType_id") ;
    		    	
    				sql3.append(" where mc.patient_id='").append(obj[2]).append("' and upper(mc.dtype)='DEPARTMENTMEDCASE' and case when coalesce(mc.transferdate,mc.datefinish,current_date) >= to_date('").append(o2[0]==null?curDate:o2[0]).append("','dd.mm.yyyy') and mc.datestart<=to_date('").append(o2[3]==null?curDate:o2[3]).append("','dd.mm.yyyy') ") ;
    				sql3.append(" then 1 else 0 end = 1") ;
    				sql3.append(" group by mc.datestart,mc.datefinish,mc.transferdate") ;
    		    	sql3.append(" ,sls.id,mc.id") ;
    		    	sql3.append(" ,sls.admissionInHospital_id,vh.code") ;
    		    	sql3.append(" ,sls.admissionOrder_id,vhr.code") ;
    		    	sql3.append(" ,mc.department_id") ;
    		    	sql3.append(" ,sls.serviceStream_id,bf.bedType_id,bf.bedSubType_id") ;
    		    	sql3.append(" ,mc.entranceTime, mc.dischargeTime,sls.emergency,vht.code,mc.transferTime,vhtHosp.id,vbst.code,sls.datefinish,sls.datestart,mc.parent_id ") ;
    		    	
    				sql3.append(" order by mc.datestart") ;
    				sql3.append("");
    				List<Object[]> l3=theManager.createNativeQuery(sql3.toString()).getResultList() ;
    				if (l3.size()==0) {
    					System.out.println("bad") ;
    					CompulsoryTreatmentAggregate ahr = new CompulsoryTreatmentAggregate() ;
		    			ahr.setOrderCompTr(ConvertSql.parseString(obj[0])) ;
		    			ahr.setPatient(ConvertSql.parseLong(obj[2])) ;
		    			ahr.setIsDeath(false) ;
		    			ahr.setEntranceHospDate(ConvertSql.parseDate(o2[3]==null?curDate:o2[3])) ;
		    			ahr.setNumberHosp(hospInd) ;
		    			theManager.persist(ahr);
    				}else{
    					System.out.println("good") ;
    					
    					Date begDate = null ;
    					Date endDate = null ;
	    				Object[] oF = l3.get(0) ;
	    				Object[] oL = l3.get(l3.size()-1) ;
	    				
	    				if (oF[2]==null) {begDate=ConvertSql.parseDate(o2[2]) ;} else {begDate=ConvertSql.parseDate(oF[3]) ;}
	    				
	    				
    					if (oL[7]!=null) {
    						endDate=ConvertSql.parseDate(oL[7]) ;
    						//System.out.println("endDate1="+endDate) ;
    					} 
	    				
	    				for (int ind =0;ind<l3.size();ind++) {
	    					
	    					Object[]o3 = l3.get(ind) ;
	    					Date begDateDep = null ;
		    				Date endDateDep = null ;
		    				
		    				if (o3[3]!=null&&o3[2]!=null) {
								begDateDep=ConvertSql.parseDate(o3[3]) ;
		    				} else {
		    					begDateDep=ConvertSql.parseDate(o2[2]) ;
		    				}
		    				
	    					if (o3[7]!=null) {
	    						endDateDep=ConvertSql.parseDate(o3[7]) ;
	    						//System.out.println("endDateDep1="+endDateDep) ;
	    					} 
	    					
		    				
			    			CompulsoryTreatmentAggregate ahr = new CompulsoryTreatmentAggregate() ;
			    			ahr.setSls(ConvertSql.parseLong(o3[0])) ;
			    			ahr.setSlo(ConvertSql.parseLong(o3[1])) ;
			    			ahr.setSexCode(ConvertSql.parseString(o2[6])) ;
			    			ahr.setOrderCompTr(ConvertSql.parseString(obj[0])) ;
			    			ahr.setPatient(ConvertSql.parseLong(obj[2])) ;
			    			ahr.setDepartment(ConvertSql.parseLong(o3[6])) ;
			    			ahr.setEntranceHospDate(ConvertSql.parseDate(begDate));
			    			ahr.setEntranceDepDate(ConvertSql.parseDate(begDateDep));
			    			ahr.setDischargeHospDate(ConvertSql.parseDate(endDate)) ;
			    			ahr.setDischargeDepDate(ConvertSql.parseDate(endDateDep)) ;
			    			ahr.setIsDeath(ConvertSql.parseBoolean(o3[11])) ;
			    			ahr.setNumberHosp(hospInd) ;
			    			ahr.setIdcDepartmentCode(ConvertSql.parseString(o3[8])) ;
				    		ahr.setIdcDischarge(ConvertSql.parseString(o3[10])) ;
				    		ahr.setIdcEntranceCode(ConvertSql.parseString(o3[9])) ;
				    		//ahr.setIdcTransferCode(ConvertSql.parseString(obj[34])) ;
				    		
			    			if (ind>0) {
			    				ahr.setIdcTransferCode(ConvertSql.parseString(l3.get(ind-1)[8], true));
			    				ahr.setTransferDepartmentFrom(ConvertSql.parseLong(l3.get(ind-1)[6]));
			    			}
			    			if (ind<l3.size()-1) ahr.setTransferDepartmentIn(ConvertSql.parseLong(l3.get(ind+1)[6]));
			    			theManager.persist(ahr);
	    				}
    	/*		//ahr.setAgeDischargeSlo(ConvertSql.parseLong(0));
    			ahr.setAgeDischargeSls(ConvertSql.parseLong(obj[35]));
    			ahr.setAgeEntranceSlo(ConvertSql.parseLong(obj[37]));
    			ahr.setAgeEntranceSls(ConvertSql.parseLong(obj[36]));
    			ahr.setCntDaysSls(ConvertSql.parseLong(0)) ;
    			ahr.setDepartment(ConvertSql.parseLong(obj[11])) ;
    			ahr.setDischargeHospDate(ConvertSql.parseDate(obj[5])) ;
    			ahr.setEntranceHospDate(ConvertSql.parseDate(obj[4])) ;
    			ahr.setEntranceDate7(ConvertSql.parseDate(obj[4],ConvertSql.parseBoolean(obj[23])?-1:0)) ;
    			ahr.setEntranceDate9(ConvertSql.parseDate(obj[4],ConvertSql.parseBoolean(obj[24])?-1:0)) ;
    			ahr.setIdcDepartmentCode(ConvertSql.parseString(obj[14])) ;
    			ahr.setIdcDischarge(ConvertSql.parseString(obj[16])) ;
    			ahr.setIdcTransferCode(ConvertSql.parseString(obj[34])) ;
    			ahr.setIsDeath(ConvertSql.parseBoolean(obj[10])) ;
    			ahr.setIsFirstCurrentYear(ConvertSql.parseBoolean(obj[8])) ;
    			ahr.setIsFirstLife(ConvertSql.parseBoolean(obj[7])) ;
    			ahr.setIsIncompetent(ConvertSql.parseBoolean(obj[9])) ;
    			ahr.setIsVillage(ConvertSql.parseBoolean(obj[8])) ;
    			ahr.setPatient(ConvertSql.parseLong(obj[0])) ;
    			ahr.setSexCode(ConvertSql.parseString(obj[3])) ;
    			ahr.setSlo(ConvertSql.parseLong(obj[2])) ;
    			ahr.setSls(ConvertSql.parseLong(obj[1])) ;
    			ahr.setTransferDepartmentFrom(ConvertSql.parseLong(obj[13])) ;
    			ahr.setTransferDepartmentIn(ConvertSql.parseLong(obj[12])) ;
    			ahr.setTransferLpuCode(ConvertSql.parseString(obj[27])) ;
    			ahr.setSex(ConvertSql.parseLong(obj[29])) ;
    			ahr.setAddBedDays(ConvertSql.parseLong(obj[33])) ;
    			ahr.setBirthday(ConvertSql.parseDate(obj[38]));*/
    				}
    			}
    			if(ii%10==0) monitor.setText(new StringBuilder().append("Импортируется: ").append(ConvertSql.parseLong(obj[0])).append(" ").append(ConvertSql.parseLong(obj[2])).append("...").toString());
    			if(size>0&&ii%size==0) monitor.advice(1);
    			
    			if (ii % 80 == 0) {
    				//              theUserTransaction.commit();
    				//              theUserTransaction.begin();
    				monitor.setText("Импортировано " + ii);
    			}
    		}
    		monitor.finish("");
    	} catch (Exception e) {
    		e.printStackTrace();
    		if(monitor!=null) monitor.setText(e+"");
    		throw new IllegalStateException(e) ;
    	}
    }
    public void refreshReportByPeriod(String aEntranceDate,String aDischargeDate,long aIdMonitor) {
    	IMonitor monitor = null;
    	try {
	    	monitor = theMonitorService.startMonitor(aIdMonitor, "Обработка данных за период: "+aEntranceDate+" "+aDischargeDate, 100);
	    	
	    	StringBuilder sqlD = new StringBuilder() ;
	    	sqlD.append(" delete from AggregateHospitalReport where entrancedate24 <= (to_date('").append(aDischargeDate).append("','dd.mm.yyyy')-1)") ;
	    	sqlD.append(" and (dischargedate24 >= (to_date('").append(aEntranceDate).append("','dd.mm.yyyy')-1) or dischargedate24 is null)") ;
	    	theManager.createNativeQuery(sqlD.toString()).executeUpdate() ;
	    	StringBuilder sql = new StringBuilder() ;
	    	sql.append(" select pat.id as f0patid") ;
	    	sql.append(" ,sls.id as f1slsid") ;
	    	sql.append(" ,slo.id as f2sloid") ;
	    	sql.append(" ,vs.omccode as f3assex") ;
	    	sql.append(" ,slo.datestart as f4entrancedate") ;
	    	sql.append(" ,coalesce(slo.datefinish,slo.transferdate) as f5dischargedate") ;
	    	sql.append(" ,case when a.addressisvillage='1' then cast('1' as int) else null end as f6isvillage") ;
	    	sql.append(" ,case when sls.admissionInHospital_id='1' then cast('1' as int) else null end as f7isFirstLife") ;
	    	sql.append(" ,case when (vh.code='1' or vh.code='2' or sls.admissionInHospital_id='1') then cast('1' as int) else null end as f8isFirstCurrentYear") ;
	    	sql.append(" ,case when sls.admissionOrder_id in (2,4,5,6,7,8,9) then cast('1' as int) else null end as f9isIncompetent") ;
	    	sql.append(" ,case when vhr.code='11' then cast('1' as int) else null end as f10isDeath") ;
	    	sql.append(" ,slo.department_id as f11slodepartment") ;
	    	sql.append(" ,nextslo.department_id as f12nextslodepartment") ;
	    	sql.append(" ,prevslo.department_id as f13prevslodepartment") ;
	    	sql.append(" ,list(distinct case when vdrtD.code='4' and vpdD.code='1' then mkbD.code else null end) as f14depDiag") ;
	    	sql.append(" ,list(distinct case when vdrt.code='2' then mkb.code else null end) as f15orderDiag") ;
	    	sql.append(" ,list(distinct case when vdrt.code='3' and vpd.code='1' then mkb.code else null end) as f16dischargeDiag") ;
	    	sql.append(" ,case when count(ahr.id)>0 then cast('1' as int) else null end as f17cntAggregate") ;
	    	sql.append(" ,bf.bedType_id as f18bedType") ;
	    	sql.append(" ,bf.bedSubType_id as f19bedSubType") ;
	    	sql.append(" ,sls.serviceStream_id as f20serviceStream") ;
	    	sql.append(" , case when slo.entranceTime<cast('07:00' as time) then cast('1' as int) else null end as f21entranceTime7") ;
	    	sql.append(" , case when slo.entranceTime<cast('09:00' as time) then cast('1' as int) else null end as f22entranceTime9") ;
	    	sql.append(" , case when coalesce(slo.dischargeTime,slo.transferTime)<cast('07:00' as time) then cast('1' as int) else null end as f23entranceTime7") ;
	    	sql.append(" , case when coalesce(slo.dischargeTime,slo.transferTime)<cast('09:00' as time) then cast('1' as int) else null end as f24entranceTime9") ;
	    	sql.append(" , case when sls.emergency='1' then cast('1' as int) else null end as f25emergency") ;
	    	sql.append(" , cast('0' as int) as f26operation") ;
	    	sql.append(" , vht.code as f27transferLpu") ;
	    	sql.append(" , vhtHosp.id as f28hospTypeId") ;
	    	sql.append(" , vs.id as f29sexId") ;
	    	sql.append(" , case when firstSlo.entranceTime<cast('07:00' as time) then cast('1' as int) else null end as f30entranceTime7") ;
	    	sql.append(" , case when firstSlo.entranceTime<cast('09:00' as time) then cast('1' as int) else null end as f31entranceTime9") ;
	    	sql.append(" , firstSlo.datestart as f32entrancedate") ;
	    	sql.append(" , case when vbst.code='1' then '0' else '1' end  as f33isdayhosp") ;
	    	sql.append(" ,list(distinct case when prevVdrtD.code='4' and prevVpdD.code='1' then prevMkbD.code else null end) as f34prevdepDiag") ;
	    	sql.append(" , case when sls.dateFinish is not null then cast(to_char(sls.dateFinish,'yyyy') as int)-cast(to_char(pat.birthday,'yyyy') as int) +(case when (cast(to_char(sls.dateFinish, 'mm') as int)-cast(to_char(pat.birthday, 'mm') as int) +(case when (cast(to_char(sls.dateFinish,'dd') as int) - cast(to_char(pat.birthday,'dd') as int)<0) then -1 else 0 end)<0) then -1 else 0 end) else null end as f35ageDischarge") ;
	    	sql.append(" , cast(to_char(sls.dateStart,'yyyy') as int)-cast(to_char(pat.birthday,'yyyy') as int) +(case when (cast(to_char(sls.dateStart, 'mm') as int)-cast(to_char(pat.birthday, 'mm') as int) +(case when (cast(to_char(sls.dateStart,'dd') as int) - cast(to_char(pat.birthday,'dd') as int)<0) then -1 else 0 end)<0) then -1 else 0 end) as f36ageEntranceSls") ;
	    	sql.append(" , cast(to_char(slo.dateStart,'yyyy') as int)-cast(to_char(pat.birthday,'yyyy') as int) +(case when (cast(to_char(slo.dateStart, 'mm') as int)-cast(to_char(pat.birthday, 'mm') as int) +(case when (cast(to_char(slo.dateStart,'dd') as int) - cast(to_char(pat.birthday,'dd') as int)<0) then -1 else 0 end)<0) then -1 else 0 end) as f37ageEntranceSlo") ;
	    	sql.append(" , pat.birthday as f38birthday") ;
	    	sql.append(" from medcase sls") ;
	    	sql.append(" left join medcase slo on sls.id=slo.parent_id") ;
	    	sql.append(" left join medcase prevSlo on prevSlo.id=slo.prevMedCase_id") ;
	    	sql.append(" left join diagnosis prevDiagD on prevSlo.id=prevDiagD.medcase_id") ;
	    	sql.append(" left join vocidc10 prevMkbD on prevMkbD.id=prevDiagD.idc10_id") ;
	    	sql.append(" left join VocDiagnosisRegistrationType prevVdrtD on prevVdrtD.id=prevDiagD.registrationType_id") ;
	    	sql.append(" left join VocPriorityDiagnosis prevVpdD on prevVpdD.id=prevDiagD.priority_id") ;

	    	sql.append(" left join AggregateHospitalReport ahr on ahr.slo=slo.id") ;
	    	sql.append(" left join medcase nextSlo on nextSlo.prevmedcase_id=slo.id") ;
	    	sql.append(" left join medcase firstSlo on sls.id=firstSlo.parent_id and firstSlo.prevmedcase_id is null") ;
	    	sql.append(" left join patient pat on pat.id=sls.patient_id") ;
	    	sql.append(" left join address2 a on a.addressid=pat.address_addressid") ;
	    	sql.append(" left join vocsex vs on vs.id=pat.sex_id") ;
	    	sql.append(" left join bedfund bf on bf.id=slo.bedfund_id") ;
	    	sql.append(" left join vocbedsubtype vbst on bf.bedsubtype_id=vbst.id") ;
	    	sql.append(" left join mislpu ml on ml.id=slo.department_id") ;
	    	sql.append(" left join mislpu mlN on mlN.id=nextSlo.department_id") ;
	    	sql.append(" left join mislpu mlP on mlP.id=prevSlo.department_id") ;
	    	sql.append(" left join diagnosis diag on sls.id=diag.medcase_id") ;
	    	sql.append(" left join vocidc10 mkb on mkb.id=diag.idc10_id") ;
	    	sql.append(" left join VocDiagnosisRegistrationType vdrt on vdrt.id=diag.registrationType_id") ;
	    	sql.append(" left join VocPriorityDiagnosis vpd on vpd.id=diag.priority_id") ;
	    	sql.append(" left join diagnosis diagD on slo.id=diagD.medcase_id") ;
	    	sql.append(" left join vocidc10 mkbD on mkbD.id=diagD.idc10_id") ;
	    	sql.append(" left join VocDiagnosisRegistrationType vdrtD on vdrtD.id=diagD.registrationType_id") ;
	    	sql.append(" left join VocPriorityDiagnosis vpdD on vpdD.id=diagD.priority_id") ;
	    	sql.append(" left join VocHospitalizationResult  vhr on vhr.id=sls.result_id") ;
	    	sql.append(" left join VocHospitalization  vh on vh.id=sls.hospitalization_id") ;
	    	sql.append(" left join VocHospType vht on vht.id=sls.targetHospType_id") ;
	    	sql.append(" left join VocHospType vhtHosp on vhtHosp.id=sls.hospType_id") ;
	    	sql.append(" where sls.dtype='HospitalMedCase' and firstSlo.dtype='DepartmentMedCase'  and firstSlo.dtype='DepartmentMedCase' and sls.dateStart <= (to_date('").append(aDischargeDate).append("','dd.mm.yyyy')-1)") ;
	    	sql.append(" and (sls.dateFinish >= (to_date('").append(aEntranceDate).append("','dd.mm.yyyy')-1) or sls.dateFinish is null)") ;
	    	sql.append(" and sls.deniedHospitalizating_id is null") ;
	    	sql.append(" group by vs.omccode,vs.id,slo.datestart,slo.datefinish,slo.transferdate") ;
	    	sql.append(" ,pat.id,sls.id,slo.id,a.addressisvillage") ;
	    	sql.append(" ,sls.admissionInHospital_id,vh.code,firstSlo.datestart,firstSlo.entranceTime") ;
	    	sql.append(" ,sls.admissionOrder_id,vhr.code") ;
	    	sql.append(" ,slo.department_id,nextslo.department_id,prevslo.department_id") ;
	    	sql.append(" ,sls.serviceStream_id,bf.bedType_id,bf.bedSubType_id, pat.birthday") ;
	    	sql.append(" ,slo.entranceTime, slo.dischargeTime,sls.emergency,vht.code,slo.transferTime,vhtHosp.id,vbst.code,sls.datefinish,pat.birthday,sls.datestart ") ;
	    	sql.append(" order by sls.id") ;
	    	monitor.advice(20) ;
	    	
	    	List<Object[]> list = theManager.createNativeQuery(sql.toString()).getResultList() ;
	    	int size = list.size()/80 ;
	    	
	    	for (int i=0;i<list.size();i++) {
	    		Object[] obj = list.get(i) ;
	    		if (monitor.isCancelled()) {
	                throw new IllegalMonitorStateException("Прервано пользователем");
	            }
	    		AggregateHospitalReport ahr = new AggregateHospitalReport() ;
	    		//ahr.setAgeDischargeSlo(ConvertSql.parseLong(0));
	    		ahr.setAgeDischargeSls(ConvertSql.parseLong(obj[35]));
	    		ahr.setAgeEntranceSlo(ConvertSql.parseLong(obj[37]));
	    		ahr.setAgeEntranceSls(ConvertSql.parseLong(obj[36]));
	    		ahr.setBedSubType(ConvertSql.parseLong(obj[19])) ;
	    		ahr.setBedType(ConvertSql.parseLong(obj[18])) ;
	    		ahr.setCntDaysSls(ConvertSql.parseLong(0)) ;
	    		ahr.setDepartment(ConvertSql.parseLong(obj[11])) ;
	    		ahr.setDischargeDate24(ConvertSql.parseDate(obj[5])) ;
	    		ahr.setDischargeDate7(ConvertSql.parseDate(obj[5],ConvertSql.parseBoolean(obj[21])?-1:0)) ;
	    		ahr.setDischargeDate9(ConvertSql.parseDate(obj[5],ConvertSql.parseBoolean(obj[22])?-1:0)) ;
	    		ahr.setEntranceDate24(ConvertSql.parseDate(obj[4])) ;
	    		ahr.setEntranceDate7(ConvertSql.parseDate(obj[4],ConvertSql.parseBoolean(obj[23])?-1:0)) ;
	    		ahr.setEntranceDate9(ConvertSql.parseDate(obj[4],ConvertSql.parseBoolean(obj[24])?-1:0)) ;
	    		ahr.setEntranceHospDate24(ConvertSql.parseDate(obj[32])) ;
	    		ahr.setEntranceHospDate7(ConvertSql.parseDate(obj[32],ConvertSql.parseBoolean(obj[30])?-1:0)) ;
	    		ahr.setEntranceHospDate9(ConvertSql.parseDate(obj[32],ConvertSql.parseBoolean(obj[31])?-1:0)) ;
	    		ahr.setIdcDepartmentCode(ConvertSql.parseString(obj[14])) ;
	    		ahr.setIdcDischarge(ConvertSql.parseString(obj[16])) ;
	    		ahr.setIdcEntranceCode(ConvertSql.parseString(obj[15])) ;
	    		ahr.setIdcTransferCode(ConvertSql.parseString(obj[34])) ;
	    		ahr.setIsDeath(ConvertSql.parseBoolean(obj[10])) ;
	    		ahr.setIsEmergency(ConvertSql.parseBoolean(obj[25])) ;
	    		ahr.setIsFirstCurrentYear(ConvertSql.parseBoolean(obj[8])) ;
	    		ahr.setIsFirstLife(ConvertSql.parseBoolean(obj[7])) ;
	    		ahr.setIsIncompetent(ConvertSql.parseBoolean(obj[9])) ;
	    		ahr.setIsOperation(ConvertSql.parseBoolean(obj[26])) ;
	    		ahr.setIsVillage(ConvertSql.parseBoolean(obj[8])) ;
	    		ahr.setPatient(ConvertSql.parseLong(obj[0])) ;
	    		ahr.setServiceStream(ConvertSql.parseLong(obj[20])) ;
	    		ahr.setSexCode(ConvertSql.parseString(obj[3])) ;
	    		ahr.setSlo(ConvertSql.parseLong(obj[2])) ;
	    		ahr.setSls(ConvertSql.parseLong(obj[1])) ;
	    		ahr.setTransferDepartmentFrom(ConvertSql.parseLong(obj[13])) ;
	    		ahr.setTransferDepartmentIn(ConvertSql.parseLong(obj[12])) ;
	    		ahr.setTransferLpuCode(ConvertSql.parseString(obj[27])) ;
	    		ahr.setHospType(ConvertSql.parseLong(obj[28])) ;
	    		ahr.setSex(ConvertSql.parseLong(obj[29])) ;
	    		ahr.setAddBedDays(ConvertSql.parseLong(obj[33])) ;
	    		ahr.setBirthday(ConvertSql.parseDate(obj[38]));
	    		theManager.persist(ahr);
	    		if(i%10==0) monitor.setText(new StringBuilder().append("Импортируется: ").append(ConvertSql.parseLong(obj[0])).append(" ").append(ConvertSql.parseLong(obj[2])).append("...").toString());
	    		if(i%size==0) monitor.advice(1);

	            if (i % 300 == 0) {
	//              theUserTransaction.commit();
	//              theUserTransaction.begin();
	              monitor.setText("Импортировано " + i);
	          }
	    	}
	    	monitor.finish("");
    	} catch (Exception e) {
            if(monitor!=null) monitor.setText(e+"");
            throw new IllegalStateException(e) ;
        }
    }
    
    private String getTitleFile(String aReestr,String aLpu, String aPeriodByReestr,String aNPackage) {
    	//aLpu="300001";
    	String filename = "N"+aReestr+"M"+aLpu+"T30_"+aPeriodByReestr+XmlUtil.namePackage(aNPackage) ;
    	return filename ;
    }
    public WebQueryResult[] exportFondZip23(String aDateFrom, String aDateTo,String aPeriodByReestr, String aLpu) 
    		throws ParserConfigurationException, TransformerException {
    	String nPackage = EjbInjection.getInstance()
    			.getLocalService(ISequenceService.class)
    			.startUseNextValueNoCheck("PACKAGE_HOSP","number");
    	WebQueryResult[] fileExpList = {exportN2(aDateFrom,aDateTo,aPeriodByReestr,aLpu,nPackage)
    			, exportN3(aDateFrom,aDateTo,aPeriodByReestr,aLpu,nPackage)
    			,new WebQueryResult()
    	};
    	
    	EjbEcomConfig config = EjbEcomConfig.getInstance() ;
    	String workDir =config.get("tomcat.data.dir", "/opt/tomcat/webapps/rtf");
    	workDir = config.get("tomcat.data.dir",workDir!=null ? workDir : "/opt/tomcat/webapps/rtf") ;
    	fileExpList[2].set1((""+fileExpList[0].get1()).substring(2).replaceAll("\\.xml", "")+".263") ;
    	
    	StringBuilder sb=new StringBuilder();
    	sb.append("zip -r -9 ") ;
    	for (int i=2;i>-1;i--){
    		sb.append(fileExpList[i].get1()).append(" ");
    		if (fileExpList[i].get2()!=null) sb.append(fileExpList[i].get2()).append(" ");
    		if (fileExpList[i].get3()!=null) sb.append(fileExpList[i].get3()).append(" ");
    	}
    	//System.out.println(sb) ;
    	try {
    		//String[] arraCmd = {new StringBuilder().append("cd ").append(workDir).append("").toString(),sb.toString()} ;
    		Runtime.getRuntime().exec(sb.toString());
    	} catch (IOException e) {
    		e.printStackTrace();
    	}
    	return fileExpList ;
    }
    public String[] exportFondZip45(String aDateFrom, String aDateTo,String aPeriodByReestr, String aLpu) 
    		throws ParserConfigurationException, TransformerException {
    	String nPackage = EjbInjection.getInstance()
    			.getLocalService(ISequenceService.class)
    			.startUseNextValueNoCheck("PACKAGE_HOSP","number");
    	String[] fileExpList = {exportN4(aDateFrom,aDateTo,aPeriodByReestr,aLpu,nPackage)
    			, exportN5(aDateFrom,aDateTo,aPeriodByReestr,aLpu,nPackage)
    			,""
    	};
    	
    	EjbEcomConfig config = EjbEcomConfig.getInstance() ;
    	String workDir =config.get("tomcat.data.dir", "/opt/tomcat/webapps/rtf");
    	workDir = config.get("tomcat.data.dir",workDir!=null ? workDir : "/opt/tomcat/webapps/rtf") ;
    	fileExpList[2]=fileExpList[0].substring(2).replaceAll("\\.xml", "")+".263" ;
    	
    	StringBuilder sb=new StringBuilder();
    	sb.append("zip -r -j -9 ") ;
    	for (int i=2;i>-1;i--){
    		sb.append(workDir).append("/").append(fileExpList[i]).append(" ");
    	}
    	//System.out.println(sb) ;
    	try {
    		//String[] arraCmd = {sb.toString()} ;
			Runtime.getRuntime().exec(sb.toString());
		} catch (IOException e) {
			e.printStackTrace();
		}
    	return fileExpList ;
    }
    
    public WebQueryResult exportN1(String aDateFrom, String aDateTo,String aPeriodByReestr, String aLpu,String aNPackage) 
    		throws ParserConfigurationException, TransformerException {
    	
    	EjbEcomConfig config = EjbEcomConfig.getInstance() ;
    	if (aNPackage==null || aNPackage.equals("")) {aNPackage = EjbInjection.getInstance()
    			.getLocalService(ISequenceService.class)
    			.startUseNextValueNoCheck("PACKAGE_HOSP","number");
    	}
    	String workDir =config.get("tomcat.data.dir", "/opt/tomcat/webapps/rtf");
    	workDir = config.get("tomcat.data.dir",workDir!=null ? workDir : "/opt/tomcat/webapps/rtf") ;
    	
    	String workAddDir =config.get("data.dir.order263.out", null);
    	
    	
    	String filename = getTitleFile("1",aLpu,aPeriodByReestr,aNPackage) ;
    	WebQueryResult res = new WebQueryResult() ;

    	XmlDocument xmlDoc = new XmlDocument() ;
    	XmlDocument xmlDocError = new XmlDocument() ;
    	XmlDocument xmlDocExist = new XmlDocument() ;
    	Element root = xmlDoc.newElement(xmlDoc.getDocument(), "ZL_LIST", null);
    	Element root_error = xmlDocError.newElement(xmlDocError.getDocument(), "ZL_LIST", null);
    	Element root_exist = xmlDocExist.newElement(xmlDocExist.getDocument(), "ZL_LIST", null);
    	StringBuilder sql = new StringBuilder() ;
    	
    	sql.append(" select to_char(wchb.createDate,'yyyy-MM-dd') as w0chbcreatedate");
    	sql.append(" ,cast('1' as varchar(1)) as f1orPom");
    	sql.append(" ,case when lpu.codef is null or lpu.codef='' then plpu.codef else lpu.codef end as l2puSent");
    	sql.append(" ,case when olpu.codef is null or olpu.codef='' then oplpu.codef else olpu.codef end as l3puDirect");
    	sql.append(" ,vmc.code as m4edpolicytype");
    	sql.append(" ,mp.series as m5pseries");
    	sql.append(" , mp.polnumber as p6olnumber");
    	sql.append(" , case when oss.smocode is null or oss.smocode='' then ri.smocode else oss.smoCode end as o7ossmocode");
    	sql.append(" , ri.ogrn as o8grnSmo");
    	sql.append(" ,case when mp.dtype='MedPolicyOmc' then '12000' else okt.okato end as o9katoSmo");
    	sql.append(" ,p.lastname as l10astname");
    	sql.append(" ,p.firstname as f11irstname");
    	sql.append(" ,p.middlename as m12iddlename");
    	sql.append(" ,vs.omcCode as v13somccode");
    	sql.append(" ,to_char(p.birthday,'yyyy-mm-dd') as b14irthday");
    	sql.append(" ,wchb.phone as p15honepatient");
    	sql.append(" ,mkb.code as m16kbcode");
    	sql.append(" ,vbt.codeF as v17btcodef");
    	sql.append(" ,wp.snils as w18psnils");
    	sql.append(" ,wchb.dateFrom as w19chbdatefrom");
    	sql.append(", wchb.visit_id as v20isit");
    	sql.append(", case when vbst.code='3' then '2' else vbst.code end as v21bstcode");
    	sql.append(", cast('0' as varchar(1)) as f22det"); //TODO доделать обработку по детям
    	sql.append(" from WorkCalendarHospitalBed wchb");
    	sql.append(" left join VocBedType vbt on vbt.id=wchb.bedType_id");
    	sql.append(" left join VocBedSubType vbst on vbst.id=wchb.bedSubType_id");
    	sql.append(" left join Patient p on p.id=wchb.patient_id");
    	sql.append(" left join VocSex vs on vs.id=p.sex_id");
    	sql.append(" left join VocServiceStream vss on vss.id=wchb.serviceStream_id");
    	sql.append(" left join MedCase mc on mc.id=wchb.visit_id");
    	sql.append(" left join medpolicy mp on mp.patient_id=wchb.patient_id and mp.actualdatefrom<=wchb.createDate and coalesce(mp.actualdateto,current_date)>=wchb.createDate");
    	sql.append(" left join VocIdc10 mkb on mkb.id=wchb.idc10_id");
    	sql.append(" left join MisLpu ml on ml.id=wchb.department_id");
    	sql.append(" left join Vocmedpolicyomc vmc on mp.type_id=vmc.id");
    	sql.append(" left join Omc_kodter okt on okt.id=mp.insuranceCompanyArea_id");
    	sql.append(" left join Omc_SprSmo oss on oss.id=mp.insuranceCompanyCode_id");
    	sql.append(" left join reg_ic ri on ri.id=mp.company_id");
    	sql.append(" left join WorkFunction wf on wf.id=mc.workFunctionExecute_id");
    	sql.append(" left join Worker w on w.id=wf.worker_id");
    	sql.append(" left join Patient wp on wp.id=w.person_id");
    	sql.append(" left join mislpu lpu on lpu.id=w.lpu_id");
    	sql.append(" left join mislpu plpu on plpu.id=lpu.parent_id");
    	sql.append(" left join mislpu olpu on olpu.id=wchb.orderLpu_id");
    	sql.append(" left join mislpu oplpu on oplpu.id=olpu.parent_id");
    	sql.append(" where wchb.visit_id is not null");
    	sql.append(" and wchb.createDate between to_date('").append(aDateFrom).append("','yyyy-MM-dd') and to_date('").append(aDateTo).append("','yyyy-MM-dd')");
    	sql.append(" and vss.code in ('OBLIGATORYINSURANCE','OTHER') and wchb.visit_id is not null");
    	sql.append(" order by p.lastname,p.firstname,p.middlename");
    	List<Object[]> list = theManager.createNativeQuery(sql.toString())
    			.setMaxResults(70000).getResultList() ;
    	Element title = xmlDoc.newElement(root, "ZGLV", null);
    	Element title_error = xmlDocError.newElement(root_error, "ZGLV", null);
    	Element title_exist = xmlDocExist.newElement(root_exist, "ZGLV", null);
    	xmlDoc.newElement(title, "VERSION", "1.0");
    	xmlDoc.newElement(title, "DATA", aDateFrom);xmlDocExist.newElement(title_exist, "DATA", aDateFrom);xmlDocError.newElement(title_error, "DATA", aDateFrom);
    	xmlDoc.newElement(title, "FILENAME", filename);
    	int i=0 ;ArrayList<WebQueryResult> i_exist=new ArrayList<WebQueryResult>() ;List<WebQueryResult> i_error=new ArrayList<WebQueryResult>() ;
    	for (Object[] obj:list) {
    		if (checkDirect(obj[10],obj[11],obj[12],obj[14],obj[19],obj[17]
    				, obj[4], obj[6],obj[18],obj[16])) {
	    		if (!checkHospitalDataFond(obj[10],obj[11],obj[12],obj[14],obj[19],obj[17],obj[20])) {
	    			Element zap = xmlDoc.newElement(root, "NPR", null);
	    			recordN1(xmlDoc, zap, obj, false) ;
	    			
		    	} else {
		    		Element zapExist = xmlDocExist.newElement(root_exist, "NPR", null);
		    		i_exist.add(recordN1(xmlDocExist, zapExist, obj,true)) ;
		    	} 
	    	} else {
	    		Element zapError = xmlDocError.newElement(root_error, "NPR", null);
	    		i_error.add(recordN1(xmlDocError, zapError, obj,true)) ;
	    		
	    	}
    	}
    	XmlUtil.saveXmlDocument(xmlDoc,new File(new StringBuilder().append(workDir).append("/").append(filename).append(".xml").toString())) ;
    	if (workAddDir!=null) XmlUtil.saveXmlDocument(xmlDoc,new File(new StringBuilder().append(workAddDir).append("/").append(filename).append(".xml").toString())) ;
    	if (!i_exist.isEmpty()) {
    		res.set2(new StringBuilder().append("exist_").append(filename).append(".xml").toString()) ;
    		res.set4(i_exist) ;
    		XmlUtil.saveXmlDocument(xmlDocExist,new File(new StringBuilder().append(workDir).append("/exist_").append(filename).append(".xml").toString())) ;
    		if (workAddDir!=null) XmlUtil.saveXmlDocument(xmlDocExist,new File(new StringBuilder().append(workAddDir).append("/").append("/exist_").append(filename).append(".xml").toString())) ;
    	}
    	if (!i_error.isEmpty()) {
    		res.set3("error_"+filename+".xml") ;
    		res.set5(i_error) ;
    		XmlUtil.saveXmlDocument(xmlDocError,new File(new StringBuilder().append(workDir).append("/error_").append(filename).append(".xml").toString())) ;
    		if (workAddDir!=null) XmlUtil.saveXmlDocument(xmlDocError,new File(new StringBuilder().append(workAddDir).append("/").append("/error_").append(filename).append(".xml").toString())) ;
    	}
    	res.set1(filename+".xml") ;
    	return res;
    }
    
    private WebQueryResult recordN3(XmlDocument xmlDoc, Element zap, Object[] obj, boolean aIsCreateWQR) {
    	XmlUtil.recordElementInDocumentXml(xmlDoc,zap,"DCODE_MO",obj[16],true,"") ;
		XmlUtil.recordElementInDocumentXml(xmlDoc,zap,"DLPU_1",null,false,"") ;
		XmlUtil.recordElementInDocumentXml(xmlDoc,zap,"DATE_1",obj[0],true,"") ;
		XmlUtil.recordElementInDocumentXml(xmlDoc,zap,"TIME_1",XmlUtil.formatTime(obj[1]),true,"") ;
		XmlUtil.recordElementInDocumentXml(xmlDoc,zap,"VPOLIS",obj[2],false,"") ;
		XmlUtil.recordElementInDocumentXml(xmlDoc,zap,"SPOLIS",obj[3],false,"") ;
		XmlUtil.recordElementInDocumentXml(xmlDoc,zap,"NPOLIS",obj[4],false,"") ;
		XmlUtil.recordElementInDocumentXml(xmlDoc,zap,"SMO",obj[5],false,"") ;
		XmlUtil.recordElementInDocumentXml(xmlDoc,zap,"SMO_OGRN",obj[6],false,"") ;
		XmlUtil.recordElementInDocumentXml(xmlDoc,zap,"SMO_OK",obj[7],false,"") ;
		XmlUtil.recordElementInDocumentXml(xmlDoc,zap,"SMO_NAM",null,false,"") ;
		XmlUtil.recordElementInDocumentXml(xmlDoc,zap,"FAM",obj[8],true,"") ;
		XmlUtil.recordElementInDocumentXml(xmlDoc,zap,"IM",obj[9],true,"") ;
		XmlUtil.recordElementInDocumentXml(xmlDoc,zap,"OT",obj[10],false,"") ;
		XmlUtil.recordElementInDocumentXml(xmlDoc,zap,"W",obj[11],true,"") ;
		XmlUtil.recordElementInDocumentXml(xmlDoc,zap,"DR",obj[12],true,"") ;
		XmlUtil.recordElementInDocumentXml(xmlDoc,zap,"USL_OK",obj[17],true,"") ;
		XmlUtil.recordElementInDocumentXml(xmlDoc,zap,"DET",obj[18],true,"") ;
		XmlUtil.recordElementInDocumentXml(xmlDoc,zap,"PROFIL",obj[13],true,"") ;
		XmlUtil.recordElementInDocumentXml(xmlDoc,zap,"PODR",null,false,"") ;
		XmlUtil.recordElementInDocumentXml(xmlDoc,zap,"NHISTORY",obj[14],true,"") ;
		XmlUtil.recordElementInDocumentXml(xmlDoc,zap,"DS1",obj[15],true,"") ;
		XmlUtil.recordElementInDocumentXml(xmlDoc,zap,"REFREASON",null,false,"") ;
		WebQueryResult res = null ;
    	if (aIsCreateWQR) {
    		res= new WebQueryResult() ;
    		res.set1(obj[0]) ;res.set2(obj[1]) ;res.set3(obj[2]) ;res.set4(obj[3]) ;res.set5(obj[4]) ;
    		res.set6(obj[5]) ;res.set7(obj[6]) ;res.set8(obj[7]) ;res.set9(obj[8]) ;res.set10(obj[9]) ;
    		res.set11(obj[10]) ;res.set12(obj[11]) ;res.set13(obj[12]) ;res.set14(obj[13]) ;res.set15(obj[14]) ;
    		res.set16(obj[15]) ;res.set17(obj[16]) ;res.set18(obj[17]) ;res.set19(obj[18]) ;
    		//res.set20(obj[21]);res.set21(obj[22]);
    	}
    	return res ;
    }
    private WebQueryResult recordN2(XmlDocument xmlDoc, Element zap, Object[] obj, boolean aIsCreateWQR) {
    	
    	XmlUtil.recordElementInDocumentXml(xmlDoc,zap,"N_NPR",obj[18],true,"") ;
		XmlUtil.recordElementInDocumentXml(xmlDoc,zap,"D_NPR",obj[19],true,"") ;
		XmlUtil.recordElementInDocumentXml(xmlDoc,zap,"FOR_POM",obj[20],true,"") ;
		XmlUtil.recordElementInDocumentXml(xmlDoc,zap,"DCODE_MO",obj[16],true,"") ;
		XmlUtil.recordElementInDocumentXml(xmlDoc,zap,"DLPU_1",null,false,"") ;
		XmlUtil.recordElementInDocumentXml(xmlDoc,zap,"NCODE_MO",obj[17],true,"") ;
		XmlUtil.recordElementInDocumentXml(xmlDoc,zap,"NLPU_1",null,false,"") ;
		XmlUtil.recordElementInDocumentXml(xmlDoc,zap,"DATE_1",obj[0],true,"") ;
		XmlUtil.recordElementInDocumentXml(xmlDoc,zap,"TIME_1",XmlUtil.formatTime(obj[1]),true,"") ;
		XmlUtil.recordElementInDocumentXml(xmlDoc,zap,"VPOLIS",obj[2],true,"") ;
		XmlUtil.recordElementInDocumentXml(xmlDoc,zap,"SPOLIS",obj[3],true,"") ;
		XmlUtil.recordElementInDocumentXml(xmlDoc,zap,"NPOLIS",obj[4],true,"") ;
		XmlUtil.recordElementInDocumentXml(xmlDoc,zap,"FAM",obj[8],true,"") ;
		XmlUtil.recordElementInDocumentXml(xmlDoc,zap,"IM",obj[9],true,"") ;
		XmlUtil.recordElementInDocumentXml(xmlDoc,zap,"OT",obj[10],true,"") ;
		XmlUtil.recordElementInDocumentXml(xmlDoc,zap,"W",obj[11],true,"") ;
		XmlUtil.recordElementInDocumentXml(xmlDoc,zap,"DR",obj[12],true,"") ;
		XmlUtil.recordElementInDocumentXml(xmlDoc,zap,"USL_OK",obj[21],true,"") ;
		XmlUtil.recordElementInDocumentXml(xmlDoc,zap,"DET",obj[22],true,"") ;
		XmlUtil.recordElementInDocumentXml(xmlDoc,zap,"PROFIL",obj[13],true,"") ;
		XmlUtil.recordElementInDocumentXml(xmlDoc,zap,"PODR",null,false,"") ;
		XmlUtil.recordElementInDocumentXml(xmlDoc,zap,"NHISTORY",obj[14],true,"") ;
		XmlUtil.recordElementInDocumentXml(xmlDoc,zap,"DS1",obj[15],true,"") ;
		XmlUtil.recordElementInDocumentXml(xmlDoc,zap,"REFREASON",null,false,"") ;
    	WebQueryResult res = null ;
    	if (aIsCreateWQR) {
    		res= new WebQueryResult() ;
    		res.set1(obj[0]) ;res.set2(obj[1]) ;res.set3(obj[2]) ;res.set4(obj[3]) ;res.set5(obj[4]) ;
    		res.set6(obj[5]) ;res.set7(obj[6]) ;res.set8(obj[7]) ;res.set9(obj[8]) ;res.set10(obj[9]) ;
    		res.set11(obj[10]) ;res.set12(obj[11]) ;res.set13(obj[12]) ;res.set14(obj[13]) ;res.set15(obj[14]) ;
    		res.set16(obj[15]) ;res.set17(obj[16]) ;res.set18(obj[17]) ;res.set19(obj[18]) ;
    		res.set20(obj[21]);res.set21(obj[22]);
    	}
    	return res ;
    	
    }
    private WebQueryResult recordN1(XmlDocument xmlDoc, Element zap, Object[] obj, boolean aIsCreateWQR) {
    	
    	XmlUtil.recordElementInDocumentXml(xmlDoc,zap,"N_NPR","",true,"") ;
		XmlUtil.recordElementInDocumentXml(xmlDoc,zap,"D_NPR",obj[0],true,"") ;
		XmlUtil.recordElementInDocumentXml(xmlDoc,zap,"FOR_POM",obj[1],true,"") ;
		XmlUtil.recordElementInDocumentXml(xmlDoc,zap,"NCODE_MO",obj[2],true,"") ;
		XmlUtil.recordElementInDocumentXml(xmlDoc,zap,"NLPU_1","",true,"") ;
		XmlUtil.recordElementInDocumentXml(xmlDoc,zap,"DCODE_MO",obj[3],true,"") ;
		XmlUtil.recordElementInDocumentXml(xmlDoc,zap,"DLPU_1",null,false,"") ;
		XmlUtil.recordElementInDocumentXml(xmlDoc,zap,"VPOLIS",obj[4],true,"") ;
		XmlUtil.recordElementInDocumentXml(xmlDoc,zap,"SPOLIS",obj[5],true,"") ;
		XmlUtil.recordElementInDocumentXml(xmlDoc,zap,"NPOLIS",obj[6],true,"") ;
		XmlUtil.recordElementInDocumentXml(xmlDoc,zap,"SMO",obj[7],true,"") ;
		XmlUtil.recordElementInDocumentXml(xmlDoc,zap,"SMO_OGRN",obj[8],true,"") ;
		XmlUtil.recordElementInDocumentXml(xmlDoc,zap,"SMO_OK",obj[9],true,"") ;
		XmlUtil.recordElementInDocumentXml(xmlDoc,zap,"SMO_NAM",null,true,"") ;
		XmlUtil.recordElementInDocumentXml(xmlDoc,zap,"FAM",obj[10],true,"") ;
		XmlUtil.recordElementInDocumentXml(xmlDoc,zap,"IM",obj[11],true,"") ;
		XmlUtil.recordElementInDocumentXml(xmlDoc,zap,"OT",obj[12],true,"") ;
		XmlUtil.recordElementInDocumentXml(xmlDoc,zap,"W",obj[13],true,"") ;
		XmlUtil.recordElementInDocumentXml(xmlDoc,zap,"DR",obj[14],true,"") ;
		XmlUtil.recordElementInDocumentXml(xmlDoc,zap,"CT",obj[15],true,"") ;
		XmlUtil.recordElementInDocumentXml(xmlDoc,zap,"DS1",obj[16],true,"") ;
		XmlUtil.recordElementInDocumentXml(xmlDoc,zap,"USL_OK",obj[21],true,"") ;
		XmlUtil.recordElementInDocumentXml(xmlDoc,zap,"DET",obj[22],true,"") ;
		XmlUtil.recordElementInDocumentXml(xmlDoc,zap,"PROFIL",obj[17],true,"") ;
		XmlUtil.recordElementInDocumentXml(xmlDoc,zap,"PODR",null,true,"") ;
		XmlUtil.recordElementInDocumentXml(xmlDoc,zap,"IDDOKT",obj[18],true,"") ;
		XmlUtil.recordElementInDocumentXml(xmlDoc,zap,"DATE_1",obj[19],true,"") ;
		XmlUtil.recordElementInDocumentXml(xmlDoc,zap,"REFREASON",null,false,"") ;
		WebQueryResult res = null ;
		if (aIsCreateWQR) {
			res= new WebQueryResult() ;
			res.set1(obj[0]) ;res.set2(obj[1]) ;res.set3(obj[2]) ;res.set4(obj[3]) ;res.set5(obj[4]) ;
			res.set6(obj[5]) ;res.set7(obj[6]) ;res.set8(obj[7]) ;res.set9(obj[8]) ;res.set10(obj[9]) ;
			res.set11(obj[10]) ;res.set12(obj[11]) ;res.set13(obj[12]) ;res.set14(obj[13]) ;res.set15(obj[14]) ;
			res.set16(obj[15]) ;res.set17(obj[16]) ;res.set18(obj[17]) ;res.set19(obj[18]) ;res.set20(obj[19]) ;
		}
		return res ;
		
    }
    private boolean checkDirect(Object aLastname, Object aFirstname,Object aMiddlename
    		, Object aBirthday, Object aPreDate, Object aProfile, Object aTypePolicy
    		, Object aNumberPolicy,Object aSnilsDoctor,Object aDiagnosis) {
    	if (aProfile==null) return false ;
    	if (aPreDate==null) return false ;
    	if (aSnilsDoctor==null) return false ;
    	if (aTypePolicy==null) return false ;
    	/*try {
    		java.util.Date preDate = new SimpleDateFormat("yyyy-DD-mm").parse(""+aPreDate) ;
    		
    	} catch(Exception e) {
    		return false ;
    	}*/
    	return true ;
    }
    private boolean checkN2(Object[] aObj) {
    	if (aObj[15]==null) return false ;
    	return true ;
    }
    private boolean checkN3(Object[] aObj) {
    	if (aObj[4]==null) return false ;
    	if (aObj[8]==null) return false ;
    	if (aObj[13]==null) return false ;
    	if (aObj[15]==null) return false ;
    	return true ;
    }
    private boolean checkHospitalDataFond(Object aLastname, Object aFirstname,Object aMiddlename
    		, Object aBirthday, Object aPreDate, Object aProfile, Object aDirectMedCase) {
    	StringBuilder fld = new StringBuilder() ;
    	fld.append("select case when hdf.istable1='1' then '1' else null end as hdfistable1,hdf.istable2,hdf.istable3,hdf.istable4,hdf.istable5 from HospitalDataFond hdf where");
    	
    	StringBuilder sql = new StringBuilder() ;
    	if (aDirectMedCase!=null) {
	    	sql.append(fld) ;
	    	
	    	sql.append(" hdf.directMedCase_id='").append(aDirectMedCase).append("'") ;
	    	List<Object[]> l = theManager.createNativeQuery(sql.toString()).getResultList() ;
	    	if (l.isEmpty()) {
	    		return false ;
	    	}
	    	sql = new StringBuilder() ;
	    }
    	sql.append(fld);
    	sql.append(" hdf.lastname='").append(aLastname).append("'") ;
    	sql.append(" and hdf.firstname='").append(aFirstname).append("'") ;
    	sql.append(" and hdf.middlename='").append(aMiddlename).append("'") ;
    	sql.append(" and hdf.birthday=to_date('").append(aBirthday).append("','yyyy-mm-dd')") ;
    	sql.append(" and hdf.profile='").append(aProfile).append("'") ;
    	sql.append(" and coalesce(hdf.prehospdate,hdf.hospdate)=to_date('").append(aPreDate)
    		.append("','yyyy-mm-dd')") ;
    	List<Object[]> l1 = theManager.createNativeQuery(sql.toString()).getResultList() ;
    	if (l1.isEmpty()) {
    		return false ;
    	} else {
    		if (l1.size()>1) {
    			
    		} else {
    			Object[] o=l1.get(0) ;
    			if (o[1]==null) return false ;
    		}
    		return true ;
    	}
    	
    }
    public WebQueryResult exportN2_plan_otherLpu(String aDateFrom, String aDateTo,String aPeriodByReestr, String aLpu,String aNPackage) 
    		throws ParserConfigurationException, TransformerException {
    	EjbEcomConfig config = EjbEcomConfig.getInstance() ;
    	if (aNPackage==null || aNPackage.equals("")) {aNPackage = EjbInjection.getInstance()
    			.getLocalService(ISequenceService.class)
    			.startUseNextValueNoCheck("PACKAGE_HOSP","number");
    	}
    	String workDir =config.get("tomcat.data.dir", "/opt/tomcat/webapps/rtf");
    	workDir = config.get("tomcat.data.dir",workDir!=null ? workDir : "/opt/tomcat/webapps/rtf") ;
    	String workAddDir =config.get("data.dir.order263.out", null);
    	String filename = getTitleFile("2",aLpu,aPeriodByReestr,aNPackage) ;
    	WebQueryResult res = new WebQueryResult() ;
    	
    	XmlDocument xmlDoc = new XmlDocument() ;
    	XmlDocument xmlDocError = new XmlDocument() ;
    	XmlDocument xmlDocExist = new XmlDocument() ;
    	Element root = xmlDoc.newElement(xmlDoc.getDocument(), "ZL_LIST", null);
    	Element root_error = xmlDocError.newElement(xmlDocError.getDocument(), "ZL_LIST", null);
    	Element root_exist = xmlDocExist.newElement(xmlDocExist.getDocument(), "ZL_LIST", null);
    	int i=0 ;ArrayList<WebQueryResult> i_exist=new ArrayList<WebQueryResult>() ;List<WebQueryResult> i_error=new ArrayList<WebQueryResult>() ;
    	
    	StringBuilder sql = new StringBuilder() ;
    	sql.append("select to_char(sls.dateStart,'yyyy-mm-dd') as f0datestart");
    	sql.append(" ,cast(sls.entranceTime as varchar(5)) as f1entrancetime");
    	sql.append(" ,vmc.code as f2medpolicytype");
    	sql.append(" ,mp.series as f3mpseries");
    	sql.append(" , mp.polnumber as f4polnumber");
    	sql.append(" , case when oss.smocode is null or oss.smocode='' then ri.smocode else oss.smoCode end as f5oossmocode");
    	sql.append(" , ri.ogrn as f6ogrnSmo");
    	sql.append(" ,case when mp.dtype='MedPolicyOmc' then '12000' else okt.okato end as f7okatoSmo");
    	sql.append(" ,p.lastname as f8lastname");
    	sql.append(" ,p.firstname as f9firstname");
    	sql.append(" ,p.middlename as f10middlename");
    	sql.append(" ,vs.omcCode as f11vsomccode");
    	sql.append(" ,to_char(p.birthday,'yyyy-mm-dd') as f12birthday");
    	sql.append(" ,vbt.codeF as f13vbtomccode");
    	sql.append(" ,ss.code as f14sscode");
    	sql.append(" ,(select max(mkb.code) from diagnosis diag left join VocIdc10 mkb on mkb.id=diag.idc10_id left join VocPriorityDiagnosis vpd on vpd.id=diag.priority_id left join VocDiagnosisRegistrationType vdrt on vdrt.id=diag.registrationtype_id where diag.medcase_id=slo.id and vpd.code='1' and vdrt.code = '4')  as f15mkbcode");
    	sql.append(" ,coalesce(hdf.directLpuCode,lpu.codef,plpu.codef) as f16lpucodef") ;
    	sql.append(" ,coalesce(hdf.orderLpuCode,olpu.codef,oplpu.codef) as f17olpucodef") ;
    	sql.append(" ,cast('0' as varchar(1)) as f18numberfond") ;
    	sql.append(" ,to_char(sls.orderDate,'yyyy-mm-dd') as f19directDate") ;
    	sql.append(" ,cast('1' as varchar(1))  as f20pokaz");
    	sql.append(" ,case when vbst.code='3' then '2' else vbst.code end as f21vbtomccode");
    	sql.append(" ,case when bf.forChild='1' then cast('1' as varchar(1)) else cast('0' as varchar(1)) end as f22vbtomccode");
    	
    	sql.append("  from medcase sls");
    	sql.append(" left join HospitalDataFond hdf on hdf.hospitalMedCase_id=sls.id");
    	sql.append(" left join medcase_medpolicy mcmp on mcmp.medcase_id=sls.id");
    	sql.append(" left join medpolicy mp on mp.id=mcmp.policies_id");
    	sql.append(" left join Vocmedpolicyomc vmc on mp.type_id=vmc.id");
    	sql.append(" left join Omc_kodter okt on okt.id=mp.insuranceCompanyArea_id");
    	sql.append(" left join Omc_SprSmo oss on oss.id=mp.insuranceCompanyCode_id");
    	sql.append(" left join reg_ic ri on ri.id=mp.company_id");
    	sql.append(" left join mislpu lpu on lpu.id=sls.lpu_id");
    	sql.append(" left join mislpu plpu on plpu.id=lpu.parent_id");
    	sql.append(" left join mislpu olpu on olpu.id=sls.orderlpu_id");
    	sql.append(" left join mislpu oplpu on oplpu.id=olpu.parent_id");
    	
    	sql.append(" left join StatisticStub ss on ss.id=sls.statisticStub_id");
    	sql.append(" left join Patient p on p.id=sls.patient_id");
    	sql.append(" left join VocSex vs on vs.id=p.sex_id");
    	sql.append(" left join medcase slo on slo.parent_id=sls.id and slo.dtype='DepartmentMedCase'");
    	sql.append(" left join BedFund bf on bf.id=slo.bedFund_id");
    	sql.append(" left join VocBedType vbt on vbt.id=bf.bedType_id");
    	sql.append(" left join VocBedSubType vbst on vbst.id=bf.bedSubType_id");
    	sql.append(" left join VocServiceStream vss on vss.id=sls.serviceStream_id");
    	sql.append(" where sls.dtype='HospitalMedCase' and sls.dateStart between to_date('").append(aDateFrom).append("','yyyy-mm-dd') and to_date('").append(aDateTo).append("','yyyy-mm-dd')");
    	sql.append(" and sls.deniedHospitalizating_id is null and slo.prevMedCase_id is null");
    	sql.append(" and vss.code in ('OBLIGATORYINSURANCE')  and (sls.emergency is null or sls.emergency='0')") ;
    	sql.append(" and coalesce(hdf.orderLpuCode,olpu.codef,oplpu.codef)!='").append(aLpu).append("'") ;
    	sql.append(" and hdf.id is null and (hdf.numberfond is null or hdf.numberfond='')") ;
    	sql.append(" and (hdf.istable3 is null or hdf.istable3='0')") ;
    	sql.append(" and (hdf.istable2 is null or hdf.istable2='0')") ;
    	sql.append(" and (hdf.istable4 is null or hdf.istable4='0')") ;
    	sql.append(" and (hdf.istable5 is null or hdf.istable5='0')") ;
    	sql.append(" order by p.lastname,p.firstname,p.middlename") ;
    	
    	List<Object[]> list = theManager.createNativeQuery(sql.toString())
    			.setMaxResults(70000).getResultList() ;
    	Element title = xmlDoc.newElement(root, "ZGLV", null);
    	Element title_error = xmlDocError.newElement(root_error, "ZGLV", null);
    	Element title_exist = xmlDocExist.newElement(root_exist, "ZGLV", null);
    	xmlDoc.newElement(title, "VERSION", "1.0");
    	xmlDoc.newElement(title, "DATA", aDateFrom);xmlDocExist.newElement(title_exist, "DATA", aDateFrom);xmlDocError.newElement(title_error, "DATA", aDateFrom);
    	xmlDoc.newElement(title, "FILENAME", filename);
    	//int i=0 ;
    	for (Object[] obj:list) {
    		if (checkN2(obj)) {
    			Element zap = xmlDoc.newElement(root, "NPR", null);
    			recordN2(xmlDoc, zap, obj, false) ;
    		} else {
    			Element zapError = xmlDocError.newElement(root_error, "NPR", null);
    			i_error.add(recordN2(xmlDocError, zapError, obj,true)) ;
    			
    		}
    	}
    	XmlUtil.saveXmlDocument(xmlDoc,new File(workDir+"/"+filename+".xml")) ;
    	if (workAddDir!=null) XmlUtil.saveXmlDocument(xmlDoc,new File(workAddDir+"/"+filename+".xml")) ;
    	if (!i_exist.isEmpty()) {
    		res.set2(new StringBuilder().append("exist_").append(filename).append(".xml").toString()) ;
    		res.set4(i_exist) ;
    		XmlUtil.saveXmlDocument(xmlDocExist,new File(new StringBuilder().append(workDir).append("/exist_").append(filename).append(".xml").toString())) ;
    		if (workAddDir!=null) XmlUtil.saveXmlDocument(xmlDocExist,new File(new StringBuilder().append(workAddDir).append("/").append("exist_").append(filename).append(".xml").toString())) ;
    	}
    	if (!i_error.isEmpty()) {
    		res.set3(new StringBuilder().append("error_").append(filename).append(".xml").toString()) ;
    		res.set5(i_error) ;
    		XmlUtil.saveXmlDocument(xmlDocError,new File(new StringBuilder().append(workDir).append("/error_").append(filename).append(".xml").toString())) ;
    		if (workAddDir!=null) XmlUtil.saveXmlDocument(xmlDocError,new File(new StringBuilder().append(workAddDir).append("/").append("error_").append(filename).append(".xml").toString())) ;
    	}
    	res.set1(filename+".xml") ;
    	return res;
    }
    public WebQueryResult exportN2(String aDateFrom, String aDateTo,String aPeriodByReestr, String aLpu,String aNPackage) 
    		throws ParserConfigurationException, TransformerException {
    	EjbEcomConfig config = EjbEcomConfig.getInstance() ;
    	if (aNPackage==null || aNPackage.equals("")) {aNPackage = EjbInjection.getInstance()
    			.getLocalService(ISequenceService.class)
    			.startUseNextValueNoCheck("PACKAGE_HOSP","number");
    	}
    	String workDir =config.get("tomcat.data.dir", "/opt/tomcat/webapps/rtf");
    	workDir = config.get("tomcat.data.dir",workDir!=null ? workDir : "/opt/tomcat/webapps/rtf") ;
    	String workAddDir =config.get("data.dir.order263.out", null);
    	String filename = getTitleFile("2",aLpu,aPeriodByReestr,aNPackage) ;
    	WebQueryResult res = new WebQueryResult() ;

    	XmlDocument xmlDoc = new XmlDocument() ;
    	XmlDocument xmlDocError = new XmlDocument() ;
    	XmlDocument xmlDocExist = new XmlDocument() ;
    	Element root = xmlDoc.newElement(xmlDoc.getDocument(), "ZL_LIST", null);
    	Element root_error = xmlDocError.newElement(xmlDocError.getDocument(), "ZL_LIST", null);
    	Element root_exist = xmlDocExist.newElement(xmlDocExist.getDocument(), "ZL_LIST", null);
    	int i=0 ;ArrayList<WebQueryResult> i_exist=new ArrayList<WebQueryResult>() ;List<WebQueryResult> i_error=new ArrayList<WebQueryResult>() ;
    	
    	StringBuilder sql = new StringBuilder() ;
    	sql.append("select to_char(sls.dateStart,'yyyy-mm-dd') as f0datestart");
    	sql.append(" ,cast(sls.entranceTime as varchar(5)) as f1entrancetime");
    	sql.append(" ,vmc.code as f2medpolicytype");
    	sql.append(" ,mp.series as f3mpseries");
    	sql.append(" , mp.polnumber as f4polnumber");
    	sql.append(" , case when oss.smocode is null or oss.smocode='' then ri.smocode else oss.smoCode end as f5oossmocode");
    	sql.append(" , ri.ogrn as f6ogrnSmo");
    	sql.append(" ,case when mp.dtype='MedPolicyOmc' then '12000' else okt.okato end as f7okatoSmo");
    	sql.append(" ,hdf.lastname as f8lastname");
    	sql.append(" ,hdf.firstname as f9firstname");
    	sql.append(" ,hdf.middlename as f10middlename");
    	sql.append(" ,vs.omcCode as f11vsomccode");
    	sql.append(" ,to_char(hdf.birthday,'yyyy-mm-dd') as f12birthday");
    	sql.append(" ,vbt.codeF as f13vbtomccode");
    	sql.append(" ,ss.code as f14sscode")
    	.append(" ,coalesce ((select max(mkb.code) from diagnosis diag left join VocIdc10 mkb on mkb.id=diag.idc10_id ")
    	.append(" left join VocPriorityDiagnosis vpd on vpd.id=diag.priority_id ")
    	.append(" left join VocDiagnosisRegistrationType vdrt on vdrt.id=diag.registrationtype_id ")
    	.append(" where diag.medcase_id=slo.id and vpd.code='1' and vdrt.code = '4'),(select max(mkb.code) from diagnosis diag left join VocIdc10 mkb")
    	.append(" on mkb.id=diag.idc10_id ")
    	.append(" left join VocPriorityDiagnosis vpd on vpd.id=diag.priority_id")
    	.append(" left join VocDiagnosisRegistrationType vdrt on vdrt.id=diag.registrationtype_id")
    	.append(" where diag.medcase_id=slo.id and vpd.code='1' and (vdrt.code = '1' or vdrt.code = '2')))")
    	.append(" as f15mkbcode");
    	//sql.append(" ,(select max(mkb.code) from diagnosis diag left join VocIdc10 mkb on mkb.id=diag.idc10_id left join VocPriorityDiagnosis vpd on vpd.id=diag.priority_id left join VocDiagnosisRegistrationType vdrt on vdrt.id=diag.registrationtype_id where diag.medcase_id=slo.id and vpd.code='1' and vdrt.code = '4')  as f15mkbcode");
    	sql.append(" ,coalesce(hdf.directLpuCode,lpu.codef,plpu.codef) as f16lpucodef") ;
    	sql.append(" ,coalesce(hdf.orderLpuCode,olpu.codef,oplpu.codef) as f17olpucodef") ;
    	sql.append(" ,hdf.numberfond as f18numberfond") ;
    	sql.append(" ,to_char(hdf.directDate,'yyyy-mm-dd') as f19directDate") ;
    	sql.append(" ,hdf.formHelp  as f20pokaz");
    	sql.append(" ,case when vbst.code='3' then '2' else vbst.code end as f21vbtomccode");
    	sql.append(" ,case when bf.forChild='1' then cast('1' as varchar(1)) else cast('0' as varchar(1)) end as f22vbtomccode");
    	
    	sql.append("  from medcase sls");
    	sql.append(" left join HospitalDataFond hdf on hdf.hospitalMedCase_id=sls.id");
    	sql.append(" left join medcase_medpolicy mcmp on mcmp.medcase_id=sls.id");
    	sql.append(" left join medpolicy mp on mp.id=mcmp.policies_id");
    	sql.append(" left join Vocmedpolicyomc vmc on mp.type_id=vmc.id");
    	sql.append(" left join Omc_kodter okt on okt.id=mp.insuranceCompanyArea_id");
    	sql.append(" left join Omc_SprSmo oss on oss.id=mp.insuranceCompanyCode_id");
    	sql.append(" left join reg_ic ri on ri.id=mp.company_id");
    	sql.append(" left join mislpu lpu on lpu.id=sls.lpu_id");
    	sql.append(" left join mislpu plpu on plpu.id=lpu.parent_id");
    	sql.append(" left join mislpu olpu on olpu.id=sls.orderlpu_id");
    	sql.append(" left join mislpu oplpu on oplpu.id=olpu.parent_id");
    	
    	sql.append(" left join StatisticStub ss on ss.id=sls.statisticStub_id");
    	sql.append(" left join Patient p on p.id=sls.patient_id");
    	sql.append(" left join VocSex vs on vs.id=hdf.sex_id");
    	sql.append(" left join medcase slo on slo.parent_id=sls.id and slo.dtype='DepartmentMedCase'");
    	sql.append(" left join BedFund bf on bf.id=slo.bedFund_id");
    	sql.append(" left join VocBedType vbt on vbt.id=bf.bedType_id");
    	sql.append(" left join VocBedSubType vbst on vbst.id=bf.bedSubType_id");
    	sql.append(" left join VocServiceStream vss on vss.id=sls.serviceStream_id");
    	sql.append(" where sls.dtype='HospitalMedCase' and sls.dateStart between to_date('").append(aDateFrom).append("','yyyy-mm-dd') and to_date('").append(aDateTo).append("','yyyy-mm-dd')");
    	sql.append(" and sls.deniedHospitalizating_id is null and slo.prevMedCase_id is null");
    	sql.append(" and vss.code in ('OBLIGATORYINSURANCE','OTHER')") ;
    	sql.append(" and hdf.id is not null and hdf.numberfond is not null and hdf.numberfond!=''") ;
    	sql.append(" and (hdf.istable3 is null or hdf.istable3='0')") ;
    	sql.append(" and (hdf.istable2 is null or hdf.istable2='0')") ;
    	sql.append(" and (hdf.istable4 is null or hdf.istable4='0')") ;
    	sql.append(" and (hdf.istable5 is null or hdf.istable5='0')") ;
    	sql.append(" order by p.lastname,p.firstname,p.middlename") ;
    	
    	List<Object[]> list = theManager.createNativeQuery(sql.toString())
    			.setMaxResults(70000).getResultList() ;
    	Element title = xmlDoc.newElement(root, "ZGLV", null);
    	Element title_error = xmlDocError.newElement(root_error, "ZGLV", null);
    	Element title_exist = xmlDocExist.newElement(root_exist, "ZGLV", null);
    	xmlDoc.newElement(title, "VERSION", "1.0");
    	xmlDoc.newElement(title, "DATA", aDateFrom);xmlDocExist.newElement(title_exist, "DATA", aDateFrom);xmlDocError.newElement(title_error, "DATA", aDateFrom);
    	xmlDoc.newElement(title, "FILENAME", filename);
    	//int i=0 ;
    	for (Object[] obj:list) {
    		if (checkN2(obj)) {
	    			Element zap = xmlDoc.newElement(root, "NPR", null);
	    			recordN2(xmlDoc, zap, obj, false) ;
	    	} else {
	    		Element zapError = xmlDocError.newElement(root_error, "NPR", null);
	    		i_error.add(recordN2(xmlDocError, zapError, obj,true)) ;
	    		
	    	}
    	}
    	XmlUtil.saveXmlDocument(xmlDoc,new File(workDir+"/"+filename+".xml")) ;
    	if (workAddDir!=null) XmlUtil.saveXmlDocument(xmlDoc,new File(workAddDir+"/"+filename+".xml")) ;
    	if (!i_exist.isEmpty()) {
    		res.set2(new StringBuilder().append("exist_").append(filename).append(".xml").toString()) ;
    		res.set4(i_exist) ;
    		XmlUtil.saveXmlDocument(xmlDocExist,new File(new StringBuilder().append(workDir).append("/exist_").append(filename).append(".xml").toString())) ;
    		if (workAddDir!=null) XmlUtil.saveXmlDocument(xmlDocExist,new File(new StringBuilder().append(workAddDir).append("/").append("/exist_").append(filename).append(".xml").toString())) ;
    	}
    	if (!i_error.isEmpty()) {
    		res.set3(new StringBuilder().append("error_").append(filename).append(".xml").toString()) ;
    		res.set5(i_error) ;
    		XmlUtil.saveXmlDocument(xmlDocError,new File(new StringBuilder().append(workDir).append("/error_").append(filename).append(".xml").toString())) ;
    		if (workAddDir!=null) XmlUtil.saveXmlDocument(xmlDocError,new File(new StringBuilder().append(workAddDir).append("/").append("/error_").append(filename).append(".xml").toString())) ;
    	}
    	res.set1(filename+".xml") ;
    	return res;
    }
    public WebQueryResult exportN1_planHosp(String aDateFrom, String aDateTo,String aPeriodByReestr, String aLpu,String aNPackage) 
    		throws ParserConfigurationException, TransformerException {
    	EjbEcomConfig config = EjbEcomConfig.getInstance() ;
    	WebQueryResult res = new WebQueryResult() ;
    	Map<SecPolicy, String> hash = new HashMap<SecPolicy,String>() ;
    	String workDir =config.get("tomcat.data.dir", "/opt/tomcat/webapps/rtf");
    	workDir = config.get("tomcat.data.dir",workDir!=null ? workDir : "/opt/tomcat/webapps/rtf") ;
    	String workAddDir =config.get("data.dir.order263.out", null);
    	
    	if (aNPackage==null || aNPackage.equals("")) {aNPackage = EjbInjection.getInstance()
    			.getLocalService(ISequenceService.class)
    			.startUseNextValueNoCheck("PACKAGE_HOSP","number");
    	}
    	String filename = getTitleFile("1",aLpu,aPeriodByReestr,aNPackage) ; ;

    	XmlDocument xmlDoc = new XmlDocument() ;
    	XmlDocument xmlDocError = new XmlDocument() ;
    	XmlDocument xmlDocExist = new XmlDocument() ;
    	Element root = xmlDoc.newElement(xmlDoc.getDocument(), "ZL_LIST", null);
    	Element root_error = xmlDocError.newElement(xmlDocError.getDocument(), "ZL_LIST", null);
    	Element root_exist = xmlDocExist.newElement(xmlDocExist.getDocument(), "ZL_LIST", null);
    	StringBuilder sql = new StringBuilder() ;
    	sql.append(" select to_char(sls.dateStart,'yyyy-mm-dd') as wchbcreatedate");
    	sql.append(" ,cast('1' as varchar(1)) as forPom");
    	sql.append(" ,coalesce(lpu.codef,plpu.codef) as lpuSent");
    	sql.append(" ,coalesce(lpu.codef,plpu.codef) as lpuDirect");
    	sql.append(" ,vmc.code as medpolicytype");
    	sql.append(" ,mp.series as mpseries");
    	sql.append(" , mp.polnumber as polnumber");
    	sql.append(" , case when oss.smocode is null or oss.smocode='' then ri.smocode else oss.smoCode end as oossmocode");
    	sql.append(" , ri.ogrn as ogrnSmo");
    	sql.append(" ,case when mp.dtype='MedPolicyOmc' then '12000' else okt.okato end as okatoSmo");
    	sql.append(" ,p.lastname as lastname");
    	sql.append(" ,p.firstname as firstname");
    	sql.append(" ,p.middlename as middlename");
    	sql.append(" ,vs.omcCode as vsomccode");
    	sql.append(" ,to_char(p.birthday,'yyyy-mm-dd') as birthday");
    	sql.append(" ,case when p.phone is null or p.phone='' then '*' else p.phone end as phonepatient");
    	sql.append(" ,mkb.code as mkbcode");
    	sql.append(" ,vbt.codeF as vbtcodef");
    	sql.append(" ,bf.snilsDoctorDirect263 as wpsnils");
    	sql.append(" ,to_char(sls.dateStart,'yyyy-mm-dd') as wchbdatefrom");
    	sql.append(", cast(null as int) as visit");
    	sql.append(", case when vbst.code='3' then '2' else vbst.code end as v22bstcode");
    	sql.append(", case when bf.forChild='1' then cast('1' as varchar(1)) else cast('0' as varchar(1)) end as f23det"); //TODO доделать обработку по детям
    	sql.append("  from medcase sls");
    	sql.append(" left join HospitalDataFond hdf on hdf.hospitalMedCase_id=sls.id");
    	sql.append(" left join medcase_medpolicy mcmp on mcmp.medcase_id=sls.id");
    	sql.append(" left join medpolicy mp on mp.id=mcmp.policies_id");
    	sql.append(" left join Vocmedpolicyomc vmc on mp.type_id=vmc.id");
    	sql.append(" left join Omc_kodter okt on okt.id=mp.insuranceCompanyArea_id");
    	sql.append(" left join Omc_SprSmo oss on oss.id=mp.insuranceCompanyCode_id");
    	sql.append(" left join reg_ic ri on ri.id=mp.company_id");
    	sql.append(" left join mislpu lpu on lpu.id=sls.lpu_id");
    	sql.append(" left join mislpu plpu on plpu.id=lpu.parent_id");
    	sql.append(" left join mislpu olpu on olpu.id=sls.orderlpu_id");
    	sql.append(" left join mislpu oplpu on oplpu.id=olpu.parent_id");
    	
    	sql.append(" left join StatisticStub ss on ss.id=sls.statisticStub_id");
    	sql.append(" left join Patient p on p.id=sls.patient_id");
    	sql.append(" left join VocSex vs on vs.id=p.sex_id");
    	sql.append(" left join medcase slo on slo.parent_id=sls.id and slo.dtype='DepartmentMedCase'");
    	sql.append(" left join diagnosis diag on diag.medcase_id=slo.id and diag.priority_id='1' and diag.registrationType_id = '4'");
    	sql.append(" left join VocIdc10 mkb on mkb.id=diag.idc10_id") ;
    	sql.append(" left join BedFund bf on bf.id=slo.bedFund_id");
    	sql.append(" left join VocBedType vbt on vbt.id=bf.bedType_id");
    	sql.append(" left join VocBedSubType vbst on vbst.id=bf.bedSubType_id");
    	sql.append(" left join VocServiceStream vss on vss.id=sls.serviceStream_id");
    	sql.append(" where sls.dtype='HospitalMedCase' and sls.dateStart between to_date('").append(aDateFrom).append("','yyyy-mm-dd') and to_date('").append(aDateTo).append("','yyyy-mm-dd')");
    	sql.append(" and sls.deniedHospitalizating_id is null and (sls.emergency is null or sls.emergency='0') and slo.prevMedCase_id is null");
    	sql.append(" and vss.code in ('OBLIGATORYINSURANCE')  and (sls.emergency is null or sls.emergency='0')") ;
    	sql.append(" and mkb.code is not null and (hdf.id is null)") ;
    	sql.append(" and coalesce(hdf.orderLpuCode,olpu.codef,oplpu.codef)='").append(aLpu).append("'") ;
    	
    	sql.append(" order by p.lastname,p.firstname,p.middlename") ;
    	
    	List<Object[]> list = theManager.createNativeQuery(sql.toString())
    			.setMaxResults(70000).getResultList() ;
    	Element title = xmlDoc.newElement(root, "ZGLV", null);
    	Element title_error = xmlDocError.newElement(root_error, "ZGLV", null);
    	Element title_exist = xmlDocExist.newElement(root_exist, "ZGLV", null);
    	xmlDoc.newElement(title, "VERSION", "1.0");
    	xmlDoc.newElement(title, "DATA", aDateFrom);xmlDocExist.newElement(title_exist, "DATA", aDateFrom);xmlDocError.newElement(title_error, "DATA", aDateFrom);
    	xmlDoc.newElement(title, "FILENAME", filename);
    	int i=0 ;ArrayList<WebQueryResult> i_exist=new ArrayList<WebQueryResult>() ;List<WebQueryResult> i_error=new ArrayList<WebQueryResult>() ;
    	for (Object[] obj:list) {
    		if (checkDirect(obj[10],obj[11],obj[12],obj[14],obj[19],obj[17]
    				, obj[4], obj[6],obj[18],obj[16])) {
	    		if (!checkHospitalDataFond(obj[10],obj[11],obj[12],obj[14],obj[19],obj[17],obj[20])) {
	    			Element zap = xmlDoc.newElement(root, "NPR", null);
	    			recordN1(xmlDoc, zap, obj, false) ;
		    	} else {
		    		Element zapExist = xmlDocExist.newElement(root_exist, "NPR", null);
		    		i_exist.add(recordN1(xmlDocExist, zapExist, obj,true)) ;
		    	} 
	    	} else {
	    		Element zapError = xmlDocError.newElement(root_error, "NPR", null);
	    		i_error.add(recordN1(xmlDocError, zapError, obj,true)) ;
	    		
	    	}
    	}
    	XmlUtil.saveXmlDocument(xmlDoc,new File(new StringBuilder().append(workDir).append("/").append(filename).append(".xml").toString())) ;
    	if (workAddDir!=null) XmlUtil.saveXmlDocument(xmlDoc,new File(new StringBuilder().append(workAddDir).append("/").append(filename).append(".xml").toString())) ;
    	if (!i_exist.isEmpty()) {
    		res.set2(new StringBuilder().append("exist_").append(filename).append(".xml").toString()) ;
    		res.set4(i_exist) ;
    		XmlUtil.saveXmlDocument(xmlDocExist,new File(new StringBuilder().append(workDir).append("/exist_").append(filename).append(".xml").toString())) ;
    		if (workAddDir!=null) XmlUtil.saveXmlDocument(xmlDocExist,new File(new StringBuilder().append(workAddDir).append("/").append("exist_").append(filename).append(".xml").toString())) ;
    	}
    	if (!i_error.isEmpty()) {
    		res.set3(new StringBuilder().append("error_").append(filename).append(".xml").toString()) ;
    		res.set5(i_error) ;
    		XmlUtil.saveXmlDocument(xmlDocError,new File(new StringBuilder().append(workDir).append("/error_").append(filename).append(".xml").toString())) ;
    		if (workAddDir!=null) XmlUtil.saveXmlDocument(xmlDocError,new File(new StringBuilder().append(workAddDir).append("/").append("error_").append(filename).append(".xml").toString())) ;
    	}
    	res.set1(filename+".xml") ;
    	return res;
    }
    
    public WebQueryResult exportN3(String aDateFrom, String aDateTo,String aPeriodByReestr, String aLpu,String aNPackage) 
    		throws ParserConfigurationException, TransformerException {
    	EjbEcomConfig config = EjbEcomConfig.getInstance() ;
    	WebQueryResult res = new WebQueryResult() ;
    	Map<SecPolicy, String> hash = new HashMap<SecPolicy,String>() ;
    	String workDir =config.get("tomcat.data.dir", "/opt/tomcat/webapps/rtf");
    	workDir = config.get("tomcat.data.dir",workDir!=null ? workDir : "/opt/tomcat/webapps/rtf") ;
    	String workAddDir =config.get("data.dir.order263.out", null);
    	
    	if (aNPackage==null || aNPackage.equals("")) {aNPackage = EjbInjection.getInstance()
    			.getLocalService(ISequenceService.class)
    			.startUseNextValueNoCheck("PACKAGE_HOSP","number");
    	}
    	String filename = getTitleFile("3",aLpu,aPeriodByReestr,aNPackage) ; ;
    	
    	XmlDocument xmlDoc = new XmlDocument() ;
    	XmlDocument xmlDocError = new XmlDocument() ;
    	Element root = xmlDoc.newElement(xmlDoc.getDocument(), "ZL_LIST", null);
    	Element root_error = xmlDocError.newElement(xmlDocError.getDocument(), "ZL_LIST", null);
    	//int i=0 ;
    	List<WebQueryResult> i_error=new ArrayList<WebQueryResult>() ;
    	StringBuilder sql = new StringBuilder() ;
    	sql.append("select to_char(sls.dateStart,'yyyy-mm-dd') as d0atestart");
    	sql.append(" ,cast(sls.entranceTime as varchar(5)) as e1ntrancetime");
    	sql.append(" ,vmc.code as m2edpolicytype");
    	sql.append(" ,mp.series as m3pseries");
    	sql.append(" , mp.polnumber as p4olnumber");
    	sql.append(" , case when oss.smocode is null or oss.smocode='' then ri.smocode else oss.smoCode end as o5ossmocode");
    	sql.append(" , ri.ogrn as o6grnSmo");
    	sql.append(" ,case when mp.dtype='MedPolicyOmc' then '12000' else okt.okato end as o7katoSmo");
    	sql.append(" ,p.lastname as l8astname");
    	sql.append(" ,p.firstname as f9irstname");
    	sql.append(" ,p.middlename as m10iddlename");
    	sql.append(" ,vs.omcCode as v11somccode");
    	sql.append(" ,to_char(p.birthday,'yyyy-mm-dd') as b12irthday");
    	sql.append(" ,vbt.codeF as v13btomccode");
    	sql.append(" ,ss.code as s14scode");
    	sql.append(" ,mkb.code as m15kbcode");
    	sql.append(" ,case when lpu.codef is null or lpu.codef='' then plpu.codef else lpu.codef end as l16pucodef") ;
    	sql.append(" ,case when vbst.code='3' then '2' else vbst.code end as f17vbtomccode");
    	sql.append(" ,case when bf.forChild='1' then cast('1' as varchar(1)) else cast('0' as varchar(1)) end as f18vbtomccode");
    	sql.append("  from medcase sls");
    	sql.append(" left join HospitalDataFond hdf on hdf.hospitalMedCase_id=sls.id");
    	sql.append(" left join medcase_medpolicy mcmp on mcmp.medcase_id=sls.id");
    	sql.append(" left join medpolicy mp on mp.id=mcmp.policies_id");
    	sql.append(" left join Vocmedpolicyomc vmc on mp.type_id=vmc.id");
    	sql.append(" left join Omc_kodter okt on okt.id=mp.insuranceCompanyArea_id");
    	sql.append(" left join Omc_SprSmo oss on oss.id=mp.insuranceCompanyCode_id");
    	sql.append(" left join reg_ic ri on ri.id=mp.company_id");
    	sql.append(" left join mislpu lpu on lpu.id=sls.lpu_id");
    	sql.append(" left join mislpu plpu on plpu.id=lpu.parent_id");
    	
    	sql.append(" left join StatisticStub ss on ss.id=sls.statisticStub_id");
    	sql.append(" left join Patient p on p.id=sls.patient_id");
    	sql.append(" left join VocSex vs on vs.id=p.sex_id");
    	sql.append(" left join medcase slo on slo.parent_id=sls.id and slo.dtype='DepartmentMedCase'");
    	sql.append(" left join diagnosis diag on diag.medcase_id=slo.id and diag.priority_id='1' and diag.registrationType_id = '4'");
    	sql.append(" left join VocIdc10 mkb on mkb.id=diag.idc10_id") ;
    	sql.append(" left join BedFund bf on bf.id=slo.bedFund_id");
    	sql.append(" left join VocBedType vbt on vbt.id=bf.bedType_id");
    	sql.append(" left join VocBedSubType vbst on vbst.id=bf.bedSubType_id");
    	sql.append(" left join VocServiceStream vss on vss.id=sls.serviceStream_id");
    	sql.append(" where sls.dtype='HospitalMedCase' and sls.dateStart between to_date('").append(aDateFrom).append("','yyyy-mm-dd') and to_date('").append(aDateTo).append("','yyyy-mm-dd')");
    	sql.append(" and sls.deniedHospitalizating_id is null and sls.emergency='1' and slo.prevMedCase_id is null");
    	sql.append(" and vss.code in ('OBLIGATORYINSURANCE')") ;
    	sql.append(" and mkb.code is not null and (hdf.id is not null and (hdf.numberfond is null or hdf.numberfond='') or hdf.id is null)") ;
    	sql.append(" order by p.lastname,p.firstname,p.middlename") ;
    	
    	List<Object[]> list = theManager.createNativeQuery(sql.toString())
    			.setMaxResults(10000).getResultList() ;
    	Element title = xmlDoc.newElement(root, "ZGLV", null);
    	xmlDoc.newElement(title, "VERSION", "1.0");
    	xmlDoc.newElement(title, "DATA", aDateFrom);
    	xmlDoc.newElement(title, "FILENAME", filename);
    	for (Object[] obj:list) {
    		if (checkN3(obj)) {
    			Element zap = xmlDoc.newElement(root, "NPR", null);
    			recordN3(xmlDoc, zap, obj, false) ;
	    	} else {
	    		Element zapError = xmlDocError.newElement(root_error, "NPR", null);
	    		i_error.add(recordN3(xmlDocError, zapError, obj,true)) ;
	    	}
    	}
    	XmlUtil.saveXmlDocument(xmlDoc,new File(new StringBuilder().append(workDir).append("/").append(filename).append(".xml").toString())) ;
    	if (workAddDir!=null) XmlUtil.saveXmlDocument(xmlDoc,new File(new StringBuilder().append(workAddDir).append("/").append(filename).append(".xml").toString())) ;
    	res.set1(filename+".xml") ;
    	
    	if (!i_error.isEmpty()) {
    		res.set3(new StringBuilder().append("error_").append(filename).append(".xml").toString()) ;
    		res.set5(i_error) ;
    		XmlUtil.saveXmlDocument(xmlDocError,new File(new StringBuilder().append(workDir).append("/error_").append(filename).append(".xml").toString())) ;
    		if (workAddDir!=null) XmlUtil.saveXmlDocument(xmlDocError,new File(new StringBuilder().append(workAddDir).append("/").append("/error_").append(filename).append(".xml").toString())) ;
    	}
    	return res;
    }
    
    public String exportN4(String aDateFrom, String aDateTo,String aPeriodByReestr, String aLpu,String aNPackage) 
    		throws ParserConfigurationException, TransformerException {
    	EjbEcomConfig config = EjbEcomConfig.getInstance() ;
    	Map<SecPolicy, String> hash = new HashMap<SecPolicy,String>() ;
    	String workDir =config.get("tomcat.data.dir", "/opt/tomcat/webapps/rtf");
    	workDir = config.get("tomcat.data.dir",workDir!=null ? workDir : "/opt/tomcat/webapps/rtf") ;
    	String workAddDir =config.get("data.dir.order263.out", null);
    	
    	if (aNPackage==null || aNPackage.equals("")) {aNPackage = EjbInjection.getInstance()
    			.getLocalService(ISequenceService.class)
    			.startUseNextValueNoCheck("PACKAGE_HOSP","number");
    	}
    	String filename = getTitleFile("4",aLpu,aPeriodByReestr,aNPackage) ; ;
    	
    	XmlDocument xmlDoc = new XmlDocument() ;
    	
    	Element root = xmlDoc.newElement(xmlDoc.getDocument(), "ZL_LIST", null);
    	StringBuilder sql = new StringBuilder() ;
    	sql.append("select hdf.numberFond as orderNumber");
    	sql.append(" ,to_char(hdf.directDate,'yyyy-mm-dd') as direct");
    	sql.append(" ,hdf.directLpuCode as vsomccode");
    	sql.append(" ,hdf.DeniedHospital as DeniedHospital");
    	sql.append("  from HospitalDataFond hdf");
    	sql.append(" where hdf.preHospDate between to_date('").append(aDateFrom).append("','yyyy-mm-dd') and to_date('").append(aDateTo).append("','yyyy-mm-dd')");
    	sql.append(" and hdf.hospitalMedCase_id is null ");
    	sql.append(" and hdf.DeniedHospital is not null") ;
    	sql.append(" and (hdf.istable4 is null or hdf.istable4='0')") ;
    	sql.append(" order by hdf.numberFond") ;
    	
    	List<Object[]> list = theManager.createNativeQuery(sql.toString())
    			.setMaxResults(70000).getResultList() ;
    	Element title = xmlDoc.newElement(root, "ZGLV", null);
    	xmlDoc.newElement(title, "VERSION", "1.0");
    	xmlDoc.newElement(title, "DATA", aDateFrom);
    	xmlDoc.newElement(title, "FILENAME", filename);
    	int i=0 ;
    	for (Object[] obj:list) {
    		Element zap = xmlDoc.newElement(root, "NPR", null);
    		//xmlDoc.newElement(zap, "IDCASE", AddressPointServiceBean.getStringValue(++i)) ;
    		XmlUtil.recordElementInDocumentXml(xmlDoc,zap,"N_NPR",obj[0],true,"") ;
    		XmlUtil.recordElementInDocumentXml(xmlDoc,zap,"D_NPR",obj[1],true,"") ;
    		XmlUtil.recordElementInDocumentXml(xmlDoc,zap,"ISTNPR","2",true,"") ;
    		XmlUtil.recordElementInDocumentXml(xmlDoc,zap,"SMOLPU",obj[2],true,"") ;
    		XmlUtil.recordElementInDocumentXml(xmlDoc,zap,"LPU_1",null,false,"") ;
    		XmlUtil.recordElementInDocumentXml(xmlDoc,zap,"PRNPR",obj[3],true,"") ;
    		XmlUtil.recordElementInDocumentXml(xmlDoc,zap,"REFREASON",null,false,"") ;
    	}
    	XmlUtil.saveXmlDocument(xmlDoc,new File(new StringBuilder().append(workDir).append("/").append(filename).append(".xml").toString())) ;
    	if (workAddDir!=null) XmlUtil.saveXmlDocument(xmlDoc,new File(new StringBuilder().append(workAddDir).append("/").append(filename).append(".xml").toString())) ;
    	return filename+".xml";
    }
    public String exportN4b(String aDateFrom, String aDateTo,String aPeriodByReestr, String aLpu,String aNPackage) 
    		throws ParserConfigurationException, TransformerException {
    	EjbEcomConfig config = EjbEcomConfig.getInstance() ;
    	Map<SecPolicy, String> hash = new HashMap<SecPolicy,String>() ;
    	String workDir =config.get("tomcat.data.dir", "/opt/tomcat/webapps/rtf");
    	workDir = config.get("tomcat.data.dir",workDir!=null ? workDir : "/opt/tomcat/webapps/rtf") ;
   	
    	String filename = getTitleFile("4",aLpu,aPeriodByReestr,aNPackage) ; ;
    	
    	File outFile = new File(workDir+"/"+filename+".xml") ;
    	XmlDocument xmlDoc = new XmlDocument() ;
    	
    	Element root = xmlDoc.newElement(xmlDoc.getDocument(), "ZL_LIST", null);
    	StringBuilder sql = new StringBuilder() ;
    	sql.append("select sls.orderNumber as orderNumber");
    	sql.append(" ,case when sls.emergency='1' then to_char(sls.dateStart,'yyyy-mm-dd') else to_char(sls.orderDate,'yyyy-mm-dd') end as orderDate");
    	sql.append(" ,case when sls.emergency='1' then cast('3' as varchar(1)) else cast('1' as varchar(1)) end as emergency");
    	sql.append(" ,to_char(sls.dateStart,'yyyy-mm-dd') as datestart");
    	sql.append(" ,to_char(sls.dateFinish,'yyyy-mm-dd') as dateFinish");
    	sql.append(" ,cast(sls.entranceTime as varchar(5)) as entrancetime");
    	sql.append(" ,vmc.code as medpolicytype");
    	sql.append(" ,mp.series as mpseries");
    	sql.append(" , mp.polnumber as polnumber");
    	sql.append(" , case when oss.smocode is null or oss.smocode='' then ri.smocode else oss.smoCode end as oossmocode");
    	sql.append(" , ri.ogrn as ogrnSmo");
    	sql.append(" ,case when mp.dtype='MedPolicyOmc' then '12000' else okt.okato end as okatoSmo");
    	sql.append(" ,p.lastname as lastname");
    	sql.append(" ,p.firstname as firstname");
    	sql.append(" ,p.middlename as middlename");
    	sql.append(" ,vs.omcCode as vsomccode");
    	sql.append(" ,to_char(p.birthday,'yyyy-mm-dd') as birthday");
    	sql.append(" ,vbt.codeF as vbtomccode");
    	sql.append(" ,ss.code as sscode");
    	sql.append(" ,mkb.code as mkbcode");
    	sql.append(" ,coalesce(lpu.codef,plpu.codef) as lpucodef") ;
    	sql.append("  from medcase sls");
    	sql.append(" left join medcase_medpolicy mcmp on mcmp.medcase_id=sls.id");
    	sql.append(" left join medpolicy mp on mp.id=mcmp.policies_id");
    	sql.append(" left join Vocmedpolicyomc vmc on mp.type_id=vmc.id");
    	sql.append(" left join Omc_kodter okt on okt.id=mp.insuranceCompanyArea_id");
    	sql.append(" left join Omc_SprSmo oss on oss.id=mp.insuranceCompanyCode_id");
    	sql.append(" left join reg_ic ri on ri.id=mp.company_id");
    	sql.append(" left join mislpu lpu on lpu.id=sls.lpu_id");
    	sql.append(" left join mislpu plpu on plpu.id=lpu.parent_id");
    	
    	sql.append(" left join StatisticStub ss on ss.id=sls.statisticStub_id");
    	sql.append(" left join Patient p on p.id=sls.patient_id");
    	sql.append(" left join VocSex vs on vs.id=p.sex_id");
    	sql.append(" left join medcase slo on slo.parent_id=sls.id and slo.dtype='DepartmentMedCase'");
    	sql.append(" left join diagnosis diag on diag.medcase_id=sls.id and diag.priority_id='1' ");
    	sql.append(" left join VocIdc10 mkb on mkb.id=diag.idc10_id") ;
    	sql.append(" left join BedFund bf on bf.id=slo.bedFund_id");
    	sql.append(" left join VocBedType vbt on vbt.id=bf.bedType_id");
    	sql.append(" left join VocServiceStream vss on vss.id=sls.serviceStream_id");
    	sql.append(" where sls.dtype='HospitalMedCase' and sls.dateStart between to_date('").append(aDateFrom).append("','yyyy-mm-dd') and to_date('").append(aDateTo).append("','yyyy-mm-dd')");
    	sql.append(" and sls.deniedHospitalizating_id is not null ");
    	sql.append(" and vss.code in ('OBLIGATORYINSURANCE','OTHER')") ;
    	sql.append(" and mkb.code is not null") ;
    	sql.append(" order by p.lastname,p.firstname,p.middlename") ;
    	
    	List<Object[]> list = theManager.createNativeQuery(sql.toString())
    			.setMaxResults(70000).getResultList() ;
    	Element title = xmlDoc.newElement(root, "ZGLV", null);
    	xmlDoc.newElement(title, "VERSION", "1.0");
    	xmlDoc.newElement(title, "DATA", aDateFrom);
    	xmlDoc.newElement(title, "FILENAME", filename);
    	int i=0 ;
    	for (Object[] obj:list) {
    		Element zap = xmlDoc.newElement(root, "NPR", null);
    		//xmlDoc.newElement(zap, "IDCASE", AddressPointServiceBean.getStringValue(++i)) ;
    		XmlUtil.recordElementInDocumentXml(xmlDoc,zap,"N_NPR",obj[0],true,"") ;
    		XmlUtil.recordElementInDocumentXml(xmlDoc,zap,"D_NPR",obj[1],true,"") ;
    		XmlUtil.recordElementInDocumentXml(xmlDoc,zap,"ISTNPR","2",true,"") ;
    		XmlUtil.recordElementInDocumentXml(xmlDoc,zap,"SMOLPU",obj[20],true,"") ;
    		XmlUtil.recordElementInDocumentXml(xmlDoc,zap,"LPU_1",null,false,"") ;
    		XmlUtil.recordElementInDocumentXml(xmlDoc,zap,"PRNPR","5",true,"") ;
    		XmlUtil.recordElementInDocumentXml(xmlDoc,zap,"REFREASON",null,false,"") ;
    	}
    	XmlUtil.saveXmlDocument(xmlDoc,outFile) ;
    	return filename+".xml";
    }
    public String exportN5(String aDateFrom, String aDateTo,String aPeriodByReestr, String aLpu,String aNPackage) 
    		throws ParserConfigurationException, TransformerException {
    	EjbEcomConfig config = EjbEcomConfig.getInstance() ;
    	Map<SecPolicy, String> hash = new HashMap<SecPolicy,String>() ;
    	String workDir =config.get("tomcat.data.dir", "/opt/tomcat/webapps/rtf");
    	workDir = config.get("tomcat.data.dir",workDir!=null ? workDir : "/opt/tomcat/webapps/rtf") ;
    	String workAddDir =config.get("data.dir.order263.out", null);
    	
    	if (aNPackage==null || aNPackage.equals("")) {aNPackage = EjbInjection.getInstance()
    			.getLocalService(ISequenceService.class)
    			.startUseNextValueNoCheck("PACKAGE_HOSP","number");
    	}
    	String filename = getTitleFile("5",aLpu,aPeriodByReestr,aNPackage) ;
    	
    	//File outFile = new File(workDir+"/"+filename+".xml") ;
    	XmlDocument xmlDoc = new XmlDocument() ;
    	Element root = xmlDoc.newElement(xmlDoc.getDocument(), "ZL_LIST", null);
    	StringBuilder sql = new StringBuilder() ;
    	sql.append("select hdf.numberfond as orderNumber");
    	sql.append(" ,to_char(coalesce(hdf.directDate,hdf.hospdate),'yyyy-mm-dd')  as orderDate");
    	sql.append(" ,case when hdf.formHelp is null and hdf.istable3='1' then '3' else hdf.formHelp end as pokaz");
    	sql.append(" ,hdf.DirectLpuCode  as lpuSent");
    	sql.append(" ,to_char(coalesce(hdf.hospDate,sls.dateStart),'yyyy-mm-dd') as datestart");
    	sql.append(" ,to_char(sls.dateFinish,'yyyy-mm-dd') as dateFinish");
    	sql.append(" ,cast(coalesce(hdf.hospTime,sls.entranceTime) as varchar(5)) as entrancetime");
    	sql.append(" ,vmc.code as medpolicytype");
    	sql.append(" ,mp.series as mpseries");
    	sql.append(" , mp.polnumber as polnumber");
    	sql.append(" , case when oss.smocode is null or oss.smocode='' then ri.smocode else oss.smoCode end as oossmocode");
    	sql.append(" , ri.ogrn as ogrnSmo");
    	sql.append(" ,case when mp.dtype='MedPolicyOmc' then '12000' else okt.okato end as okatoSmo");
    	sql.append(" ,hdf.lastname as lastname");
    	sql.append(" ,hdf.firstname as firstname");
    	sql.append(" ,hdf.middlename as middlename");
    	sql.append(" ,vs.omcCode as vsomccode");
    	sql.append(" ,to_char(hdf.birthday,'yyyy-mm-dd') as birthday");
    	sql.append(" ,hdf.profile as vbtomccode");
    	sql.append(" ,hdf.statcard as sscode");
    	//sql.append(" ,mkb.code as mkbcode");
    	sql.append(" ") ;
    	sql.append("  from medcase sls");
    	sql.append(" left join HospitalDataFond hdf on hdf.hospitalMedCase_id=sls.id");
    	sql.append(" left join medcase_medpolicy mcmp on mcmp.medcase_id=sls.id");
    	sql.append(" left join medpolicy mp on mp.id=mcmp.policies_id");
    	sql.append(" left join Vocmedpolicyomc vmc on mp.type_id=vmc.id");
    	sql.append(" left join Omc_kodter okt on okt.id=mp.insuranceCompanyArea_id");
    	sql.append(" left join Omc_SprSmo oss on oss.id=mp.insuranceCompanyCode_id");
    	sql.append(" left join reg_ic ri on ri.id=mp.company_id");
    	sql.append(" left join mislpu lpu on lpu.id=sls.lpu_id");
    	sql.append(" left join mislpu plpu on plpu.id=lpu.parent_id");
    	
    	sql.append(" left join StatisticStub ss on ss.id=sls.statisticStub_id");
    	sql.append(" left join Patient p on p.id=sls.patient_id");
    	sql.append(" left join VocSex vs on vs.id=p.sex_id");
    	//sql.append(" left join medcase slo on slo.parent_id=sls.id and slo.dtype='DepartmentMedCase'");
    	//sql.append(" left join diagnosis diag on diag.medcase_id=slo.id and diag.priority_id='1' and diag.registrationType_id = '4'");
    	//sql.append(" left join VocIdc10 mkb on mkb.id=diag.idc10_id") ;
    	//sql.append(" left join BedFund bf on bf.id=slo.bedFund_id");
    	//sql.append(" left join VocBedType vbt on vbt.id=bf.bedType_id");
    	sql.append(" left join VocServiceStream vss on vss.id=sls.serviceStream_id");
    	sql.append(" where sls.dtype='HospitalMedCase' and sls.dateFinish between to_date('").append(aDateFrom).append("','yyyy-mm-dd') and to_date('").append(aDateTo).append("','yyyy-mm-dd')");
    	sql.append(" and sls.deniedHospitalizating_id is null");
    	//sql.append(" and vss.code in ('OBLIGATORYINSURANCE','OTHER')") ;
    	sql.append(" ") ;
    	sql.append(" and hdf.id is not null and hdf.numberfond is not null and hdf.numberfond!=''") ;
    	sql.append(" and (hdf.istable2='1' or hdf.istable3='1')") ;
    	sql.append(" and (hdf.istable4 is null or hdf.istable4='0')") ;
    	sql.append(" and (hdf.istable5 is null or hdf.istable5='0')") ;
    	sql.append(" order by p.lastname,p.firstname,p.middlename") ;
    	
    	List<Object[]> list = theManager.createNativeQuery(sql.toString())
    			.setMaxResults(70000).getResultList() ;
    	Element title = xmlDoc.newElement(root, "ZGLV", null);
    	xmlDoc.newElement(title, "VERSION", "1.0");
    	xmlDoc.newElement(title, "DATA", aDateFrom);
    	xmlDoc.newElement(title, "FILENAME", filename);
    	int i=0 ;
    	for (Object[] obj:list) {
    		Element zap = xmlDoc.newElement(root, "NPR", null);
    		XmlUtil.recordElementInDocumentXml(xmlDoc,zap,"N_NPR",obj[0],true,"") ;
    		//xmlDoc.newElement(zap, "IDCASE", AddressPointServiceBean.getStringValue(++i)) ;
    		XmlUtil.recordElementInDocumentXml(xmlDoc,zap,"D_NPR",obj[1],true,"") ;
    		XmlUtil.recordElementInDocumentXml(xmlDoc,zap,"FOR_POM",obj[2],true,"") ;
    		XmlUtil.recordElementInDocumentXml(xmlDoc,zap,"LPU",obj[3],true,"") ;
    		XmlUtil.recordElementInDocumentXml(xmlDoc,zap,"LPU_1",null,false,"") ;
    		XmlUtil.recordElementInDocumentXml(xmlDoc,zap,"DATE_1",obj[4],true,"") ;
    		XmlUtil.recordElementInDocumentXml(xmlDoc,zap,"DATE_2",obj[5],true,"") ;
    		XmlUtil.recordElementInDocumentXml(xmlDoc,zap,"W",obj[16],true,"") ;
    		XmlUtil.recordElementInDocumentXml(xmlDoc,zap,"DR",obj[17],true,"") ;
    		XmlUtil.recordElementInDocumentXml(xmlDoc,zap,"PROFIL",obj[18],true,"") ;
    		XmlUtil.recordElementInDocumentXml(xmlDoc,zap,"PODR",null,false,"") ;
    		XmlUtil.recordElementInDocumentXml(xmlDoc,zap,"NHISTORY",obj[19],true,"") ;
    		XmlUtil.recordElementInDocumentXml(xmlDoc,zap,"REFREASON",null,false,"") ;
    	}
    	XmlUtil.saveXmlDocument(xmlDoc,new File(new StringBuilder().append(workDir).append("/").append(filename).append(".xml").toString())) ;
    	if (workAddDir!=null) XmlUtil.saveXmlDocument(xmlDoc,new File(new StringBuilder().append(workAddDir).append("/").append(filename).append(".xml").toString())) ;
    	return filename+".xml";
    }
    public String exportN6(String aDateFrom, String aDateTo,String aPeriodByReestr, String aLpu,String aNPackage) 
    			throws ParserConfigurationException, TransformerException {
    	EjbEcomConfig config = EjbEcomConfig.getInstance() ;
    	Map<SecPolicy, String> hash = new HashMap<SecPolicy,String>() ;
    	String workDir =config.get("tomcat.data.dir", "/opt/tomcat/webapps/rtf");
    	workDir = config.get("tomcat.data.dir",workDir!=null ? workDir : "/opt/tomcat/webapps/rtf") ;
    	String workAddDir =config.get("data.dir.order263.out", null);
    	
    	if (aNPackage==null || aNPackage.equals("")) {aNPackage = EjbInjection.getInstance()
    			.getLocalService(ISequenceService.class)
    			.startUseNextValueNoCheck("PACKAGE_HOSP","number");
    	}
    	String filename = getTitleFile("6",aLpu,aPeriodByReestr,aNPackage) ;
    	
    	//File outFile = new File(workDir+"/"+filename+".xml") ;
    	XmlDocument xmlDoc = new XmlDocument() ;
    	Element root = xmlDoc.newElement(xmlDoc.getDocument(), "ZL_LIST", null);
    	StringBuilder sql = new StringBuilder() ;
    	sql.append("select vbt.codef,vbt.name") ;
    	sql.append(" ,count(distinct sls.id) as cntHosp") ;
    	sql.append(" ,count(distinct case when sls.dateStart=to_date('").append(aDateFrom).append("','yyyy-mm-dd') then sls.id else null end) as cntEnter") ;
    	sql.append(" ,count(distinct case when sls.dateFinish=to_date('").append(aDateFrom).append("','yyyy-mm-dd') then sls.id else null end) as cntDischarge") ;
    	sql.append(" ,count(distinct case when sls.dateFinish is null or sls.datefinish>=to_date('").append(aDateTo).append("','yyyy-mm-dd') then sls.id else null end) as cntCurrent") ;
    	sql.append(" ,(select sum(bf1.amount) from bedfund bf1 left join VocBedSubType vbst1 on bf1.bedSubtype_id=vbst1.id left join VocBedType vbt1 on bf1.bedtype_id=vbt1.id where vbt1.codef=vbt.codef and case when vbst.code='3' then '2' else vbst.code end=case when vbst1.code='3' then '2' else vbst1.code end) as sumBed") ;
    	sql.append(" , case when vbst.code='3' then '2' else vbst.code end as usl_ok") ;
    	sql.append(" , case when bf.forChild='1' then cast('1' as varchar(1)) else cast('0' as varchar(1)) end as det") ;
    	sql.append(" from medcase sls") ;
    	sql.append(" left join medcase sloF on sloF.parent_id = sls.id and sloF.dtype='DepartmentMedCase' and sloF.prevMedCase_id is null") ;
    	sql.append(" left join medcase sloL on sloL.parent_id = sls.id and sloF.dtype='DepartmentMedCase' and sloL.dateFinish is not null ") ;
    	sql.append(" left join BedFund bf on bf.id=sloF.bedFund_id") ;
    	sql.append(" left join VocBedType vbt on vbt.id=bf.bedType_id") ;
    	sql.append(" left join VocBedSubType vbst on vbst.id=bf.bedSubType_id") ;
    	sql.append(" left join VocServiceStream vss on vss.id=sls.serviceStream_id") ;
    	sql.append(" where sls.dtype='HospitalMedCase' and sls.dateStart>=to_date('").append(aDateFrom).append("','yyyy-mm-dd')") ;
    	sql.append(" and sls.dateFinish>=coalesce(to_date('").append(aDateTo).append("','yyyy-mm-dd'),current_date)") ;
    	sql.append(" and vss.code in ('OBLIGATORYINSURANCE','OTHER') and vbst.code='1'") ;
    	sql.append(" group by vbt.codef,vbt.name,vbst.code,case when bf.forChild='1' then cast('1' as varchar(1)) else cast('0' as varchar(1)) end") ;
    	sql.append(" order by vbt.name");
    	
    	List<Object[]> list = theManager.createNativeQuery(sql.toString())
    			.setMaxResults(70000).getResultList() ;
    	Element title = xmlDoc.newElement(root, "ZGLV", null);
    	xmlDoc.newElement(title, "VERSION", "1.0");
    	xmlDoc.newElement(title, "DATA", aDateFrom);
    	xmlDoc.newElement(title, "FILENAME", filename);
    	int i=0 ;
    	for (Object[] obj:list) {
    		Element zap = xmlDoc.newElement(root, "NPR", null);
    		//xmlDoc.newElement(zap, "IDCASE", AddressPointServiceBean.getStringValue(++i)) ;
    		XmlUtil.recordElementInDocumentXml(xmlDoc,zap,"DATA",aDateFrom,true,"") ;
    		XmlUtil.recordElementInDocumentXml(xmlDoc,zap,"LPU",aLpu,true,"") ;
    		XmlUtil.recordElementInDocumentXml(xmlDoc,zap,"LPU_1",null,false,"") ;
    		XmlUtil.recordElementInDocumentXml(xmlDoc,zap,"USL_OK",obj[7],true,"") ;
    		XmlUtil.recordElementInDocumentXml(xmlDoc,zap,"DET",obj[8],true,"") ;
    		XmlUtil.recordElementInDocumentXml(xmlDoc,zap,"PROFIL",obj[0],true,"") ;
    		XmlUtil.recordElementInDocumentXml(xmlDoc,zap,"COUNTP",obj[5],true,"") ;
    		XmlUtil.recordElementInDocumentXml(xmlDoc,zap,"POSTP",obj[3],true,"") ;
    		XmlUtil.recordElementInDocumentXml(xmlDoc,zap,"VIBP",obj[4],true,"") ;
    		XmlUtil.recordElementInDocumentXml(xmlDoc,zap,"PLANP",0,true,"") ;
    		XmlUtil.recordElementInDocumentXml(xmlDoc,zap,"FREEK",obj[6],true,"") ;
    		XmlUtil.recordElementInDocumentXml(xmlDoc,zap,"FREEM",0,true,"") ;
    		XmlUtil.recordElementInDocumentXml(xmlDoc,zap,"FREEW",0,true,"") ;
    		XmlUtil.recordElementInDocumentXml(xmlDoc,zap,"FREED",0,true,"") ;
    		/*String[] smoCodes = {"30002","30004"} ;
    		for (String smoCode:smoCodes) {
        		Element obsmo = xmlDoc.newElement(zap, "OBSMO", null) ;
        		XmlUtil.recordElementInDocumentXml(xmlDoc,obsmo,"SMO",smoCode,true,"") ;
        		XmlUtil.recordElementInDocumentXml(xmlDoc,obsmo,"SMOSL",0,true,"") ;
        		XmlUtil.recordElementInDocumentXml(xmlDoc,obsmo,"SMOKD",0,true,"") ;
    		}*/
    	}
    	XmlUtil.saveXmlDocument(xmlDoc,new File(new StringBuilder().append(workDir).append("/").append(filename).append(".xml").toString())) ;
    	if (workAddDir!=null) XmlUtil.saveXmlDocument(xmlDoc,new File(new StringBuilder().append(workAddDir).append("/").append(filename).append(".xml").toString())) ;
    	return filename+".xml";
    }
    public void createNewDiary(String aTitle, String aText, String aUsername) {
    	TemplateProtocol protocol = new TemplateProtocol() ;
    	protocol.setText(aText) ;
    	protocol.setTitle(aTitle) ;
    	protocol.setUsername(aUsername) ;
    	protocol.setDate(new java.sql.Date(new java.util.Date().getTime()));
    	theManager.persist(protocol) ;
    }
	public void updateDataFromParameterConfig(Long aDepartment, boolean aIsLowerCase, String aIds, boolean aIsRemoveExist) {
		String[] obj = aIds.split(",") ;
		String aTableSql = "VocDocumentParameterConfig where department_id='"+aDepartment+"' and documentParameter_id " ;
		MisLpu department = theManager.find(MisLpu.class, aDepartment) ;
		for (int i = 0; i < obj.length; i++) {
			String jsId = obj[i];
			if (jsId!=null && !jsId.equals("") || jsId.equals("0")) {
				//System.out.println("    id="+jsonId) ;
				
				Long jsonId=java.lang.Long.valueOf(jsId) ;
				
				String sql ="from "+aTableSql+" ='"+jsonId+"' " ;
				List<VocDocumentParameterConfig> count = theManager.createQuery(sql).setMaxResults(1).getResultList() ;
				VocDocumentParameterConfig vdpc ;
				if (count.isEmpty()) {
					VocDocumentParameter documentParameter = theManager.find(VocDocumentParameter.class, jsonId) ;
					vdpc = new VocDocumentParameterConfig() ; 
					vdpc.setDepartment(department) ;
					vdpc.setDocumentParameter(documentParameter) ;
				} else {
					vdpc = count.get(0) ;
				}
				vdpc.setIsLowerCase(aIsLowerCase) ;
				theManager.persist(vdpc) ;
			}
		}
		if (aIsRemoveExist && aIds.length()>0 ) {
			String sql = "delete "+aTableSql+" not in ("+aIds+") " ;
			theManager.createNativeQuery(sql).executeUpdate();
		} else {
		}
	}
	public void removeDataFromParameterConfig(Long aDepartment, String aIds) {
		String aTableSql = "VocDocumentParameterConfig where department_id='"+aDepartment+"' and documentParameter_id " ;
		String sql = "delete from "+aTableSql+" in ("+aIds+") " ;
		theManager.createNativeQuery(sql).executeUpdate();
	}
	
	
    public void changeServiceStreamBySmo(Long aSmo,Long aServiceStream) {
    	List<Object[]> list = theManager.createNativeQuery("select sls.dtype,count(distinct slo.id) from medcase sls left join MedCase slo on slo.parent_id=sls.id and slo.dtype='DepartmentMedCase' where sls.id="+aSmo+" group by sls.id,sls.dtype").getResultList() ;
    	if (!list.isEmpty()) {
    		Object[] obj =list.get(0) ;
    		if (obj[0]!=null) {
    			String dtype=new StringBuilder().append(obj[0]).toString() ;
	    		if (dtype.equals("HospitalMedCase")) {
	    			List<Object[]> listBedFund = theManager.createNativeQuery("select slo.id as sloid,bfNew.id as bfNewid from MedCase slo" 
	    				+" left join BedFund bf on bf.id=slo.bedFund_id"
	    				+" left join BedFund bfNew on bfNew.lpu_id=bf.lpu_id"
	    				+" where slo.parent_id='"+aSmo+"' and slo.dtype='DepartmentMedCase'"
	    				+" and bfNew.bedSubType_id = bf.bedSubType_id"
	    				+" and bfNew.bedType_id = bf.bedType_id"
	    				+" and bfNew.serviceStream_id = '"+aServiceStream+"' and slo.dateStart between bfNew.dateStart and coalesce(bfNew.dateFinish,CURRENT_DATE)")
	    				.getResultList() ;
	    			Long cntSlo=ConvertSql.parseLong(obj[1]) ;
	    			if (cntSlo.intValue()==listBedFund.size()) {
	    				for (Object[] slo:listBedFund) {
	    					theManager.createNativeQuery("update MedCase set serviceStream_id='"+aServiceStream+"',bedFund_id='"+slo[1]+"' where id='"+slo[0]+"'").executeUpdate() ;
	    					theManager.createNativeQuery("update SurgicalOperation set serviceStream_id='"+aServiceStream+"' where medCase_id='"+slo[0]+"'").executeUpdate() ;
	    				}
	    				theManager.createNativeQuery("update MedCase set serviceStream_id='"+aServiceStream+"' where id='"+aSmo+"'").executeUpdate() ;
    					theManager.createNativeQuery("update SurgicalOperation set serviceStream_id='"+aServiceStream+"' where medCase_id='"+aSmo+"'").executeUpdate() ;
	    			} else {
	    				throw new IllegalArgumentException("По данному потоку обслуживания не во всех отделениях, в которых производилось лечение, заведен коечный фонд"); 
	    			}
	    		} else if (dtype.equals("DepartmentMedCase")) {
	    			List<Object[]> listBedFund = theManager.createNativeQuery("select slo.id as sloid,bfNew.id as bfNewid from MedCase slo" 
		    				+" left join BedFund bf on bf.id=slo.bedFund_id"
		    				+" left join BedFund bfNew on bfNew.lpu_id=bf.lpu_id"
		    				+" where slo.id='"+aSmo+"' and slo.dtype='DepartmentMedCase'"
		    				+" and bfNew.bedSubType_id = bf.bedSubType_id"
		    				+" and bfNew.bedType_id = bf.bedType_id"
		    				+" and bfNew.serviceStream_id = '"+aServiceStream+"' and slo.dateStart between bfNew.dateStart and coalesce(bfNew.dateFinish,CURRENT_DATE)")
		    				.getResultList() ;
	    			Object[] slo=listBedFund.get(0) ;
	    			if (listBedFund.size()==1) {
	    				theManager.createNativeQuery("update MedCase set serviceStream_id='"+aServiceStream+"',bedFund_id='"+slo[1]+"' where id='"+aSmo+"'").executeUpdate() ;
    					theManager.createNativeQuery("update SurgicalOperation set serviceStream_id='"+aServiceStream+"' where medCase_id='"+aSmo+"'").executeUpdate() ;
	    			} else {
	    				throw new IllegalArgumentException("По данному потоку обслуживания в отделение не заведен коечный фонд"); 
	    			}
	    		} else if (dtype.equals("Visit")) {
    				theManager.createNativeQuery("update MedCase set serviceStream_id='"+aServiceStream+"' where id='"+aSmo+"'").executeUpdate() ;	    			
	    		} else if (dtype.equals("PolyclinicMedCase")) {
    				theManager.createNativeQuery("update MedCase set serviceStream_id='"+aServiceStream+"' where parent_id='"+aSmo+"' and (dtype='Visit' or dtype='ShortMedCase')").executeUpdate() ;	    			
	    		}
    		} else {
    			
    		}
    	}
    }
    public void unionSloWithNextSlo(Long aSlo) {
    	List<Object[]> list = theManager.createNativeQuery("select  "
			+"case when sloNext1.department_id is not null and" 
			+" sloNext1.department_id=slo.department_id then '1' else null end equalsDep"
			+" ,sloNext.id as sloNext,sloNext1.id as sloNext1,sloNext2.id as sloNext2"
			+" ,sloNext.dateFinish as sloNextDateFinish,sloNext.dischargeTime as sloNextDischargeTime"
			+" ,sloNext.transferDate as sloNextTransferDate,sloNext.transferTime as sloNextTransferTime"
			+" ,sloNext1.dateFinish as sloNext1DateFinish,sloNext1.dischargeTime as sloNext1DischargeTime"
			+" ,sloNext1.transferDate as sloNext1TransferDate,sloNext1.transferTime as sloNext1TransferTime"

			+" from medcase slo"
			+" left join MedCase sloNext on sloNext.prevMedCase_id=slo.id"
			+" left join MedCase sloNext1 on sloNext1.prevMedCase_id=sloNext.id"
			+" left join MedCase sloNext2 on sloNext2.prevMedCase_id=sloNext1.id"
			+" where slo.id='"+aSlo+"'")
			.getResultList() ;
    	if (!list.isEmpty()) {
    		Object[] obj = list.get(0) ;
    		if (obj[1]!=null) {
	    		if (obj[0]!=null) {
	    			// Отд next1=current (объединять 2 отделения)
	    			theManager.createNativeQuery("update childBirth cb set medcase_id='"+aSlo+"' where cb.medCase_id='"+obj[1]+"'").executeUpdate() ;
	    			theManager.createNativeQuery("update medcase  set parent_id='"+aSlo+"' where parent_id='"+obj[1]+"'").executeUpdate() ;
	    			
	    			//Закрытие диет и Режимов из объединяемого.Копирование назначений из листа назначения объединяемого СЛО и затем его удаление.
	    			theManager.createNativeQuery("update prescription " +
	    					"set planEndDate=planStartDate, planEndTime=planStartTime " +
	    					"where (dtype='DietPrescription' or dtype='ModePrescription') " +
	    					"and prescriptionlist_id = (select id from prescriptionlist where medcase_id = '"+obj[1]+"')").executeUpdate();
	    			theManager.createNativeQuery("update prescription " +
	    	    			"set prescriptionlist_id = (select id from prescriptionlist where medcase_id = '"+obj[1]+"') " +
	    	    			"where prescriptionlist_id in (select id from prescriptionlist where medcase_id = '"+obj[2]+"')").executeUpdate();
	    			theManager.createNativeQuery("delete from prescriptionlist where medcase_id ='"+obj[2]+"'").executeUpdate();
	    			
	    			
	    			theManager.createNativeQuery("update prescription " +
	    					"set planEndDate=planStartDate, planEndTime=planStartTime " +
	    					"where (dtype='DietPrescription' or dtype='ModePrescription') " +
	    					"and prescriptionlist_id = (select id from prescriptionlist where medcase_id = '"+aSlo+"')").executeUpdate();
	    			theManager.createNativeQuery("update prescription " +
	    	    			"set prescriptionlist_id = (select id from prescriptionlist where medcase_id = '"+aSlo+"') " +
	    	    			"where prescriptionlist_id in (select id from prescriptionlist where medcase_id = '"+obj[1]+"')").executeUpdate();
	    			theManager.createNativeQuery("delete from prescriptionlist where medcase_id ='"+obj[1]+"'").executeUpdate();
	    			
	    			theManager.createNativeQuery("update diary d set medcase_id='"+aSlo+"' where d.medCase_id='"+obj[1]+"'").executeUpdate() ;
	    	    	theManager.createNativeQuery("update diagnosis d set medcase_id='"+aSlo+"' where d.medCase_id='"+obj[1]+"'").executeUpdate() ;
	    	    	theManager.createNativeQuery("update SurgicalOperation d set medcase_id='"+aSlo+"' where d.medCase_id='"+obj[1]+"'").executeUpdate() ;
	    	    	theManager.createNativeQuery("update ClinicExpertCard d set medcase_id='"+aSlo+"' where d.medCase_id='"+obj[1]+"'").executeUpdate() ;
	    	    	theManager.createNativeQuery("update diary d set medcase_id='"+aSlo+"' where d.medCase_id='"+obj[2]+"'").executeUpdate() ;
	    	    	theManager.createNativeQuery("update diagnosis d set medcase_id='"+aSlo+"' where d.medCase_id='"+obj[2]+"'").executeUpdate() ;
	    	    	theManager.createNativeQuery("update SurgicalOperation d set medcase_id='"+aSlo+"' where d.medCase_id='"+obj[2]+"'").executeUpdate() ;
	    	    	theManager.createNativeQuery("update ClinicExpertCard d set medcase_id='"+aSlo+"' where d.medCase_id='"+obj[2]+"'").executeUpdate() ;
	    	    	theManager.createNativeQuery("update transfusion d set medcase_id='"+aSlo+"' where d.medCase_id='"+obj[2]+"'").executeUpdate() ;
	    	    	theManager.createNativeQuery("update medcase set dateFinish=(select dateFinish from medcase where id='"+obj[2]+"') "
	    	    			+" ,transferDate=(select transferDate from medcase where id='"+obj[2]+"')"
	    	    			+" ,transferTime=(select transferTime from medcase where id='"+obj[2]+"')"
	    	    			+" ,dischargeTime=(select dischargeTime from medcase where id='"+obj[2]+"')"
	    	    			+" ,transferDepartment_id=(select transferDepartment_id from medcase where id='"+obj[2]+"')"
	    	    			+" ,targetHospType_id=(select targetHospType_id from medcase where id='"+obj[2]+"')"
	    	    			+" where id='"+aSlo+"'").executeUpdate() ;
	    	    	if (obj[3]!=null) {
	    	    		theManager.createNativeQuery("update MedCase set prevMedCase_id='"+aSlo+"' where id='"+obj[3]+"'").executeUpdate() ;
	    	    	}
	    	    	theManager.createNativeQuery("delete from medcase m where m.id='"+obj[2]+"'").executeUpdate() ;
	    	    	theManager.createNativeQuery("delete from medcase m where m.id='"+obj[1]+"'").executeUpdate() ;
	    		} else {
	    			//
	    			theManager.createNativeQuery("update childBirth cb set medcase_id='"+aSlo+"' where cb.medCase_id='"+obj[1]+"' and '1'=(select case when dep.isMaternityWard='1' then '1' else '0' end from medcase slo left join mislpu dep on dep.id=slo.department_id where slo.id='"+aSlo+"')").executeUpdate() ;
	    		
	    			theManager.createNativeQuery("update medcase  set parent_id='"+aSlo+"' where parent_id='"+obj[1]+"'").executeUpdate() ;
	    			
	    			theManager.createNativeQuery("update prescription " +
	    					"set planEndDate=planStartDate, planEndTime=planStartTime " +
	    					"where (dtype='DietPrescription' or dtype='ModePrescription') " +
	    					"and prescriptionlist_id = (select id from prescriptionlist where medcase_id = '"+aSlo+"')").executeUpdate();
	    			theManager.createNativeQuery("update prescription " +
	    	    			"set prescriptionlist_id = (select id from prescriptionlist where medcase_id = '"+aSlo+"') " +
	    	    			"where prescriptionlist_id in (select id from prescriptionlist where medcase_id = '"+obj[1]+"')").executeUpdate();
	    			theManager.createNativeQuery("delete from prescriptionlist where medcase_id ='"+obj[1]+"'").executeUpdate();
	    			
	    	    	
	    	    	theManager.createNativeQuery("update diary d set medcase_id='"+aSlo+"' where d.medCase_id='"+obj[1]+"'").executeUpdate() ;
	    	    	theManager.createNativeQuery("update diagnosis d set medcase_id='"+aSlo+"' where d.medCase_id='"+obj[1]+"'").executeUpdate() ;
	    	    	theManager.createNativeQuery("update SurgicalOperation d set medcase_id='"+aSlo+"' where d.medCase_id='"+obj[1]+"'").executeUpdate() ;
	    	    	theManager.createNativeQuery("update ClinicExpertCard d set medcase_id='"+aSlo+"' where d.medCase_id='"+obj[1]+"'").executeUpdate() ;
	    	    	theManager.createNativeQuery("update transfusion d set medcase_id='"+aSlo+"' where d.medCase_id='"+obj[1]+"'").executeUpdate() ;
	    	    	theManager.createNativeQuery("update medcase set dateFinish=(select dateFinish from medcase where id='"+obj[1]+"') "
	    	    			+" ,transferDate=(select transferDate from medcase where id='"+obj[1]+"')"
	    	    			+" ,transferTime=(select transferTime from medcase where id='"+obj[1]+"')"
	    	    			+" ,dischargeTime=(select dischargeTime from medcase where id='"+obj[1]+"')"
	    	    			+" ,transferDepartment_id=(select transferDepartment_id from medcase where id='"+obj[1]+"')"
	    	    			+" ,targetHospType_id=(select targetHospType_id from medcase where id='"+obj[1]+"')"
	    	    			+" where id='"+aSlo+"'").executeUpdate() ;
	    	    	if (obj[2]!=null) {
	    	    		theManager.createNativeQuery("update MedCase set prevMedCase_id='"+aSlo+"' where id='"+obj[2]+"'").executeUpdate() ;
	    	    	}
	    	    	theManager.createNativeQuery("delete from medcase m where m.id='"+obj[1]+"'").executeUpdate() ;
	    		}
    		} else {
    			throw new IllegalArgumentException("Нет след. СЛО"); 
    		}
    	}
    }
    public void deniedHospitalizatingSls(Long aMedCaseId, Long aDeniedHospitalizating) {
    	theManager.createNativeQuery("update diary d set medcase_id='"+aMedCaseId+"' where d.medCase_id in (select m.id from medcase m where m.parent_id='"+aMedCaseId+"')").executeUpdate() ;
    	theManager.createNativeQuery("update diagnosis d set medcase_id='"+aMedCaseId+"' where d.medCase_id in (select m.id from medcase m where m.parent_id='"+aMedCaseId+"')").executeUpdate() ;
    	theManager.createNativeQuery("update SurgicalOperation d set medcase_id='"+aMedCaseId+"' where d.medCase_id in (select m.id from medcase m where m.parent_id='"+aMedCaseId+"')").executeUpdate() ;
    	theManager.createNativeQuery("update ClinicExpertCard d set medcase_id='"+aMedCaseId+"' where d.medCase_id in (select m.id from medcase m where m.parent_id='"+aMedCaseId+"')").executeUpdate() ;
    	theManager.createNativeQuery("update PrescriptionList d set medcase_id='"+aMedCaseId+"' where d.medCase_id in (select m.id from medcase m where m.parent_id='"+aMedCaseId+"')").executeUpdate() ;
    	theManager.createNativeQuery("delete from medcase m where m.parent_id='"+aMedCaseId+"' and m.dtype='DepartmentMedCase'").executeUpdate() ;
    	theManager.createNativeQuery("update medcase set deniedHospitalizating_id='"+aDeniedHospitalizating+"',ambulanceTreatment='1' where id='"+aMedCaseId+"'").executeUpdate() ;
    	HospitalMedCase medCase = theManager.find(HospitalMedCase.class, aMedCaseId) ;
    	StatisticStubStac.removeStatCardNumber(theManager, theContext,medCase);
    }
    public void preRecordDischarge(Long aMedCaseId, String aDischargeEpicrisis) {
    	
    	HospitalMedCase sls = theManager.find(HospitalMedCase.class, aMedCaseId) ;
    	if (sls.getDateFinish()!=null) throw new IllegalArgumentException("На выписанных пациентов, предварительная выписка не оформляется!!!!");
    	saveDischargeEpicrisis(aMedCaseId,aDischargeEpicrisis,theManager);
    	//theManager.persist(sls) ;
    }
    public void updateDischargeDateByInformationBesk(String aIds, String aDate) throws ParseException {
    	String[] ids = aIds.split(",") ;
    	//Date date = DateFormat.parseSqlDate(aDate) ;
    	
    	for (String id :ids) {
    		Object cnt = theManager.createNativeQuery("select count(*) from medcase where id='"+id+"' and dateStart<=to_date('"+aDate+"','dd.mm.yyyy') and dischargeTime is null and deniedHospitalizating_id is null")
    		//.setParameter("dat", date)
    		.getSingleResult() ;
    		Long cntL = ConvertSql.parseLong(cnt) ;
    		if (cntL>Long.valueOf(0)) {
        		theManager.createNativeQuery("update MedCase set dateFinish=to_date('"+aDate+"','dd.mm.yyyy') where id='"+id+"' and dtype='HospitalMedCase' and dischargeTime is null")
    				//.setParameter("dat", date)
    				.executeUpdate() ;
    		} else {
        		List<Object[]> list = theManager.createNativeQuery("select p.lastname||' '||p.firstname||' '||coalesce(p.middlename)||' '||to_char(p.birthday,'dd.mm.yyyy'),ss.code,case when sls.deniedHospitalizating_id is not null then 'при отказе от госпитализации дата выписки не ставится' when sls.dischargeTime is not null then 'Изменение даты выписки у оформленных историй болезни производится через выписку' when sls.dateStart>to_date('"+aDate+"','dd.mm.yyyy') then 'Дата выписки должна быть больше, чем дата поступления' else '' end from medcase sls left join patient p on p.id=sls.patient_id left join statisticstub ss on ss.id=sls.statisticstub_id where sls.id='"+id+"'")
        	    		//.setParameter("dat", date)
        	    		.getResultList() ;
        		if (list.size()>0) {
        			Object[] objs = list.get(0) ;
        			throw new IllegalArgumentException(
        					new StringBuilder().append("Пациент:").append(objs[0])
        					.append(objs[1]!=null?" стат.карта №"+objs[1]:"")
        					.append(" ОШИБКА:").append(objs[2]).toString() 
        					); 
        		}
    		}
    	}
    	//return "" ;
    }
    public String addressInfo(EntityManager aManager,Long aAddressId, Object[] aAddress) {
        StringBuilder sb = new java.lang.StringBuilder();
        String fullname = new StringBuilder().append(aAddress[1]).toString().trim() ;
        
        //Address a = aAddress ;
        //Long id = a.getId() ;
        if (aAddress[1]!=null && !fullname.equals("")) return fullname;
        
        sb.insert(0, aAddress[2]) ;
        if (aAddress[3]!=null) {
        	String shortName = aAddress[3]+" " ;
            sb.insert(0,  shortName) ;
        	
        }
        
        //long oldId = a.getId() ;
        ////a = a.getParent() ;
        
        //sb.insert(0, aAddress[2]) ;
        //System.out.println(aAddress) ;
            if(aAddress[4]!=null) {
            	Long id1 = ConvertSql.parseLong(aAddress[4]) ;
                //System.out.println("parent="+id1) ;
            	List<Object[]> list = theManager.createNativeQuery("select a.addressid,a.fullname,a.name,att.shortName,a.parent_addressid from address2 a left join AddressType att on att.id=a.type_id where a.addressid="+id1+" order by a.addressid")
        				.setMaxResults(1)	
        				.getResultList() ;
            	if (list.size()>0) {
            		
                	sb.insert(0, ", ") ;
                	sb.insert(0, addressInfo(aManager,id1,list.get(0))) ;
            	}
            }
        
        
        //throw "address"+aAddress.id+" "+sb ;
        //throw sb.toString() ;
        //aAddress.setFullname(sb.toString()) ;
        
        //theManager.persist(aAddress) ;
        //Address a =theManager.find(Address.class, aAddressId) ;
        //theManager.createNativeQuery("update set fullname='"+sb.toString()+"' ") 
        //a.setFullname(sb.toString()) ;
        //theManager.persist(a) ;
    	aManager.createNativeQuery("update Address2 set fullname='"+sb.toString().trim()+"' where addressid="+aAddressId).executeUpdate() ;
        return sb.toString().trim() ;
    }

    public void addressClear() {
    	theManager.createNativeQuery("update Address2 set fullname=null").executeUpdate() ;
    }
    public long addressUpdate(long id) {
    	//long id=0 ;
    	List<Object[]> list ;

    		//String sql = "from Address where id>"+id+" and fullname is null order by id" ;
    		//if (id>0) throw sql ;
    		//list = theManager.createQuery(sql)
    		//	.setMaxResults(450)
    		//	.getResultList() ;
    		list = theManager.createNativeQuery("select a.addressid,a.fullname,a.name,att.shortName,a.parent_addressid from address2 a left join AddressType att on att.id=a.type_id where a.addressid>"+id+" and a.fullname is null order by a.addressid")
    				.setMaxResults(450)	
    				.getResultList() ;
    		if (list.size()>0) {
    			for (Object[] adr:list) {
    				
    				//Address adr = list.get(i) ;
    				id = ConvertSql.parseLong(adr[0]);
    				addressInfo(theManager,id,adr) ;
    				//adr.setFullname() ;
    				//aCtx.manager.persist(adr) ;
    				
    				//throw id ;
    			}
    			//list.clear() ;
    		} else {
    			id=-1 ;
    		}
    		
    	return id ;
    }
    
    public String getIdc10ByDocDiag(Long aIdDocDiag){
    	VocDiagnosis diag = theManager.find(VocDiagnosis.class, aIdDocDiag) ;
    	VocIdc10 mkb = diag.getIdc10() ;
    	if (mkb!=null) {
    		return new StringBuilder().append(mkb.getId()).append("#").append(mkb.getCode()).append("#").append(mkb.getName()).toString() ;
    	}
    	return "" ;
    }
    public String getOperationsText(Long aPatient, String aDateStart
    		,String aDateFinish) {
    	if (aDateFinish==null || aDateFinish.equals("")) {
    		aDateFinish = "CURRENT_DATE";
    	} else {
    		aDateFinish = new StringBuilder().append("to_date('").append(aDateFinish).append("','dd.mm.yyyy')").toString();
    	}
    	StringBuilder sql = new StringBuilder() ;
    	sql.append("select to_char(operationDate,'DD.MM.YYYY') as operDate1,cast(operationTime as varchar(5)) as timeFrom,cast(operationTimeTo as varchar(5)) as timeTo,vo.name as voname from SurgicalOperation so left join MedService vo on vo.id=so.operation_id where so.patient_id='")
    		.append(aPatient)
    		.append("' and so.operationDate between to_date('").append(aDateStart).append("','dd.mm.yyyy') and ").append(aDateFinish).append(" order by so.operationDate") ;
    	List<Object[]> opers = theManager.createNativeQuery(sql.toString()).getResultList() ;
    	StringBuilder res = new StringBuilder() ;
    	for (Object[] obj :opers) {
    		if (res.length()>0) res.append("; ") ;
    		res.append("").append(obj[3]).append(" ").append(obj[0]).append(" ").append(obj[1]).append("-").append(obj[2]) ;
    		
    	}
    	return res.toString() ;
    }
    public String getnvestigationsTextDTM(Long aPatient, String aDateStart
			,String aDateFinish,boolean aLabsIs,boolean aFisioIs,boolean aFuncIs,boolean aConsIs
			, boolean aLuchIs) {
    	try {
	    	if (aDateFinish==null || aDateFinish.equals("")) {
	    		aDateFinish = "CURRENT_DATE";
	    	} else {
	    		aDateFinish = new StringBuilder().append("convert(DATE,'").append(aDateFinish).append("',104)").toString();
	    	}
	    	StringBuilder sql = new StringBuilder() ;
	    	sql.append("select top 1 code,$$getUslByPatient^ZLinkPol('")
	    		.append(aPatient)
	    		.append("',TO_DATE(convert(DATE,'").append(aDateStart).append("',104),'YYYYMMDD'),TO_DATE(").append(aDateFinish).append(",'YYYYMMDD')")
	    		.append(",").append(aLabsIs?1:0)
	    		.append(",").append(aFisioIs?1:0)
	    		.append(",").append(aFuncIs?1:0)
	    		.append(",").append(aConsIs?1:0)
	    		.append(",").append(aLuchIs?1:0)
	    		.append(") ")
	    		
	    		.append("from VocSex") ;
	    	System.out.println(sql) ;
	    	List<Object[]> usls  = theManager.createNativeQuery(sql.toString()).getResultList() ;
	    	
	    	return usls.size()>0?new StringBuilder().append(usls.get(0)[1]).toString():"" ;
    	} catch (Exception e) {
    		return "" ;
    	}
    }
    
    public String getTypeDiagByAccoucheur() {
    	StringBuilder ret= new StringBuilder() ;
    	List<VocPrimaryDiagnosis> prDiag = theManager.createQuery("from VocPrimaryDiagnosis where code=1").getResultList();
    	List<VocAcuityDiagnosis> actDiag = theManager.createQuery("from VocAcuityDiagnosis where code=1 or omcCode=1").getResultList();
    	List<VocDiagnosisRegistrationType> regTypeDiag = theManager.createQuery("from VocDiagnosisRegistrationType where code=4").getResultList();
    	if (prDiag.size()>0) {
    		VocPrimaryDiagnosis pr = prDiag.get(0) ;
    		ret.append(pr.getId()).append("#").append(pr.getName()).append("#") ;
    	} else {
    		ret.append("##") ;
    	}
    	if (actDiag.size()>0) {
    		VocAcuityDiagnosis act = actDiag.get(0) ;
    		ret.append(act.getId()).append("#").append(act.getName()).append("#") ;
    	} else {
    		ret.append("##") ;
    	}
    	if (regTypeDiag.size()>0) {
    		VocDiagnosisRegistrationType regType = regTypeDiag.get(0) ;
    		ret.append(regType.getId()).append("#").append(regType.getName()) ;
    	} else {
    		ret.append("#") ;
    	}
    	return ret.toString();
    }
    
    public String findDoubleServiceByPatient(Long aMedService, Long aPatient, Long aService, String aDate) throws ParseException {
    	StringBuilder sql = new StringBuilder() ;
    	Date date=DateFormat.parseSqlDate(aDate) ;
    	sql.append("select smc.id,to_char(smc.dateExecute,'dd.mm.yyyy') as dateexecute,smc.timeExecute,vss.name,'Оказана в '|| case when p.DTYPE='DepartmentMedCase' then ' отделении '||d.name when p.DTYPE='HospitalMedCase' then 'приемном отделении ' else 'поликлинике' end from medcase as smc ")
    			.append(" left join medcase as p on p.id=smc.parent_id ")
    			.append(" left join mislpu as d on d.id=p.department_id ")
    			.append(" left join vocservicestream as vss on vss.id=smc.servicestream_id")
    			.append(" where smc.patient_id=:pat and smc.DTYPE='ServiceMedCase' and smc.medService_id=:usl and smc.dateExecute=:dat") ;
    	System.out.println("sql="+sql) ;
    	System.out.println("pat="+aPatient) ;
    	System.out.println("medservice="+aMedService) ;
    	System.out.println("service="+aService) ;
    	System.out.println("date="+aDate) ;
    	if (aMedService!=null && aMedService>0) {
    		sql.append(" and smc.id!='").append(aMedService).append("'") ;
    	}
    	
    	List<Object[]> doubles = theManager.createNativeQuery(
				sql.toString())
				.setParameter("pat", aPatient)
				.setParameter("usl", aService)
				.setParameter("dat", date)
				.getResultList() ;
		
		if (doubles.size()>0) {
			StringBuilder ret = new StringBuilder() ;
			ret.append("<br/><ol>") ;
			for (Object[] res:doubles) {
				ret.append("<li>")
				.append("<a href='entitySubclassView-mis_medCase.do?id=").append(res[0]).append("'>")
				.append(res[1]).append(" ").append(res[2]).append(" ").append(res[3]).append(" ").append(res[4])
				.append("</a>")
				.append("</li>") ;
			}
			ret.append("</ol><br/>") ;
			return ret.toString() ;
		} else {
			return null ;
		}
    	
    	
    }
    public String findDoubleOperationByPatient(Long aSurOperation, Long aParentMedCase, Long aOperation, String aDate) throws ParseException {
    	StringBuilder sql = new StringBuilder() ;
    	Date date=DateFormat.parseSqlDate(aDate) ;
    	sql.append("select so.id,to_char(so.operationDate,'dd.mm.yyyy'),so.operationTime,to_char(so.operationDateTo,'dd.mm.yyyy'),so.operationTimeTo,'Зарегистрирована в '|| case when p.DTYPE='DepartmentMedCase' then ' отделении '||d.name when p.DTYPE='HospitalMedCase' then 'приемном отделении ' else 'поликлинике' end ")
    			.append(" from medcase as mc")
    			.append(" left join SurgicalOperation as so on so.patient_id=mc.patient_id")
    			.append(" left join medcase as p on p.id=so.medcase_id ")
    			.append(" left join mislpu as d on d.id=p.department_id ")
//    			.append(" left join vocservicestream as vss on vss.id=so.servicestream_id")
    			.append(" where mc.id=:mcid and so.medService_id=:usl and so.operationDate=:dat") ;
    	//System.out.println("sql="+sql) ;
    	//System.out.println("parentmedcase="+aParentMedCase) ;
    	//System.out.println("suroperation="+aSurOperation) ;
    	//System.out.println("operation="+aOperation) ;
    	//System.out.println("date="+aDate) ;
    	if (aSurOperation!=null && aSurOperation>0) {
    		sql.append(" and so.id!='").append(aSurOperation).append("'") ;
    	}
    	
    	List<Object[]> doubles = theManager.createNativeQuery(
				sql.toString())
				.setParameter("mcid", aParentMedCase)
				.setParameter("usl", aOperation)
				.setParameter("dat", date)
				.getResultList() ;
		
		if (doubles.size()>0) {
			StringBuilder ret = new StringBuilder() ;
			ret.append("<br/><ol>") ;
			for (Object[] res:doubles) {
				ret.append("<li>")
				.append("<a href='entitySubclassView-mis_medCase.do?id=").append(res[0]).append("'>")
				.append(res[1]).append(" ").append(res[2]).append("-").append(res[3]).append(" ").append(res[4]).append(" ").append(res[5])
				.append("</a>")
				.append("</li>") ;
			}
			ret.append("</ol><br/>") ;
			return ret.toString() ;
		} else {
			return null ;
		}
    }
    public String deleteDataDischarge(Long aMedCaseId) {
    	StringBuilder sql = new StringBuilder() ;
    	sql.append("update MedCase set dischargeTime=null,dateFinish=null")
    		.append(" where (id=:idMC and DTYPE='HospitalMedCase')")
    		.append(" or (parent_id=:idMC and DTYPE='DepartmentMedCase' and dateFinish is not null)");
    	LOG.info("SQL delete discharge: "+sql) ;
    	int result = theManager.createNativeQuery(sql.toString()).setParameter("idMC", aMedCaseId).executeUpdate() ;
    	return "Запрос выполнен: "+result ;
    }
    
    public List<HospitalMedCaseForm>findSlsByStatCard(String aNumber) {
    	 if (CAN_DEBUG) {
             LOG.debug("findStatCard Number = " + aNumber );
         }
 		//Patient patient = theManager.find(Patient.class, aParentId) ;
 		StringBuilder query = new StringBuilder() ;
 		query.append("from HospitalMedCase c")
 		.append(" where statisticStub.code like :number order by dateStart");
 		 Query query2 = theManager.createQuery(query.toString()) ;
          query2.setParameter("number", "%"+aNumber+"%") ;
          System.out.println("Запрос по medCase: ");
	        System.out.println(query.toString()) ;
	        return createHospitalList(query2);
    }
    public String getRW(long aIdSls) {
    	HospitalMedCase hospital = theManager.find(HospitalMedCase.class, aIdSls) ;
    	StringBuilder ret = new StringBuilder() ;
    	if(hospital.getRwDate()!=null) ret.append(DateFormat.formatToDate(hospital.getRwDate())) ;
    	ret.append("#") ;
    	if (hospital.getRwNumber()!=null) ret.append(hospital.getRwNumber()) ;
    	return ret.toString() ;
    }
    public void setRW(long aIdSls, String aRwDate, String aRwNumber) throws ParseException {
    	HospitalMedCase hospital = theManager.find(HospitalMedCase.class, aIdSls) ;
    	hospital.setRwNumber(aRwNumber);
    	hospital.setRwDate(DateFormat.parseSqlDate(aRwDate)) ;
    	theManager.persist(hospital) ;
    }
    public void setPatientByExternalMedservice(String aNumberDoc, String aOrderDate, String aPatient) {
    	theManager.createNativeQuery("update Document set patient_id='"+aPatient+"' where NumberDoc='"+aNumberDoc+"' and OrderDate=to_date('"+aOrderDate+"','dd.mm.yyyy')").executeUpdate() ;
    }

    /**
     * Есть ли открытый случай лечения в отделении
     * @param aIdSls
     * @return
     */
    public String isOpenningSlo(long aIdSls) {
    	StringBuilder sql = new StringBuilder() ;
    	sql.append("select mc.id,ml.name from MedCase as mc")
    			.append(" left join MisLpu as ml on ml.id=mc.department_id")
    			.append( " where mc.parent_id=:idsls and mc.DTYPE='DepartmentMedCase' and mc.dateFinish is null and mc.transferDate is null") ;
    	List<Object[]> list = theManager.createNativeQuery(sql.toString()).setParameter("idsls", aIdSls).getResultList() ;
    	if (list.size()>0) {
    		StringBuilder ret = new StringBuilder() ;
    		Object[] row = list.get(0) ;
    		ret.append(" СЛО №").append(row[0]).append(" отделение: ").append(row[1]) ;
    		return ret.toString() ;
    	}
    	return "" ;
    	
    }
    public Long getPatient(long aIdSsl) {
        HospitalMedCase hospital = theManager.find(HospitalMedCase.class, aIdSsl) ;
        if(hospital==null) throw new IllegalArgumentException("Нет стационарного случая лечения с ИД "+aIdSsl) ;
        return hospital.getPatient().getId() ;
    }
    
    public String getChangeStatCardNumber(Long aMedCase, String aNewStatCardNumber, boolean aAlwaysCreate){
    	HospitalMedCase hospital = theManager.find(HospitalMedCase.class,aMedCase);
    	try {
    		if (!aAlwaysCreate) {
    			if (hospital.getDeniedHospitalizating()!=null) {
    				throw new IllegalArgumentException("Нельзя изменить номер стат.карты при отказе госпитализации");
    			}
    		}
    		StatisticStubStac.changeStatCardNumber(aMedCase, aNewStatCardNumber, theManager, theContext);
    	} catch(Exception e) {
    		throw new IllegalArgumentException(e.getMessage());
    	}
		return hospital.getStatCardNumber();
	}
	
	public Collection<MedPolicyForm> listPolicies(Long aMedCase) {
		HospitalMedCase hospital = theManager.find(HospitalMedCase.class,aMedCase);
		//List<MedCaseMedPolicy> listPolicies = hospital.getPolicies() ;
		List<MedPolicy> listPolicies =new ArrayList<MedPolicy>() ;
		List<MedCaseMedPolicy> hospmp = theManager.createQuery("from MedCaseMedPolicy where medCase=:mc").setParameter("mc", hospital).getResultList() ;
		for (MedCaseMedPolicy mp : hospmp) {
			listPolicies.add(mp.getPolicies()) ;
		}
		return convert(listPolicies);
	}

	public Collection<MedPolicyForm> listPoliciesToAdd(Long aMedCase) {
		HospitalMedCase hospital = theManager.find(HospitalMedCase.class,aMedCase);
		List<Object[]> listPolicies = theManager.createNativeQuery("select p.id,count(case when mp.medCase_id='"
				+aMedCase
				+"' then 1 else null end) from MedPolicy p left join MedCase_MedPolicy mp on p.id=mp.policies_id left join MedCase m on m.id=mp.medCase_id where p.patient_id='"
				+hospital.getPatient().getId()+"' group by p.id")
				.getResultList();
		//List<MedPolicy> allPolicies = theManager.createQuery("from MedPolicy where patient_id=:pat").setParameter("pat", hospital.getPatient().getId()).getResultList();
		List<MedPolicy> allPolicies = new ArrayList<MedPolicy>() ;
		for (Object[] obj:listPolicies) {
			Long cnt = ConvertSql.parseLong(obj[1]) ;
			if (cnt==null || cnt.equals(Long.valueOf(0))) {
				Long pId = ConvertSql.parseLong(obj[0]) ;
				MedPolicy p = theManager.find(MedPolicy.class, pId) ;
				allPolicies.add(p) ;
			}
			//allPolicies.remove(pol);
		}
		return convert(allPolicies);
	}
	
	private static Collection<MedPolicyForm> convert(Collection<MedPolicy> aPolicies) {
		LinkedList<MedPolicyForm> list = new LinkedList<MedPolicyForm>();
		for (MedPolicy policy:aPolicies) {
			MedPolicyForm frm = new MedPolicyForm() ;
			frm.setId(policy.getId());
			frm.setActualDateFrom(DateFormat.formatToDate(policy.getActualDateFrom()));
			frm.setActualDateTo(DateFormat.formatToDate(policy.getActualDateTo()));
			frm.setText(policy.getText());
			list.add(frm);
		}
		return list;
		
	}
	
	public void addPolicies(long aMedCaseId, long[] aPolicies) {
		HospitalMedCase hospital = theManager.find(HospitalMedCase.class,aMedCaseId);
		//List<MedCaseMedPolicy> listPolicies = hospital.getPolicies() ;
		for (long policyId: aPolicies) {
			
			MedPolicy policy= theManager.find(MedPolicy.class, policyId);
			System.out.println("adding="+policy.getId());
			if (!checkExistsAttachedPolicy(aMedCaseId, policyId)) {
				MedCaseMedPolicy mp = new MedCaseMedPolicy() ;
				mp.setMedCase(hospital) ;
				MedPolicy p = theManager.find(MedPolicy.class, policyId) ;
				mp.setPolicies(p) ;
				theManager.persist(mp) ;
			}
		}
		theManager.persist(hospital) ;
		//theManager.refresh(hospital);
	}
	private boolean checkExistsAttachedPolicy(long aMedCaseId,long aPolicy) {
		StringBuilder sql = new StringBuilder() ;
		sql.append(" select count(*) from MedCase_MedPolicy where medCase_id='")
			.append(aMedCaseId)
			.append("' and policies_id='").append(aPolicy).append("'") ;
		Object res= theManager.createNativeQuery(sql.toString()).getSingleResult() ;
		Long cnt=ConvertSql.parseLong(res) ;
		return cnt>Long.valueOf(0)?true:false ;
	}

	public void removePolicies(long aMedCaseId, long[] aPolicies) {
		//HospitalMedCase hospital = theManager.find(HospitalMedCase.class,aMedCaseId);
		//List<MedCaseMedPolicy> listPolicies = hospital.getPolicies() ;
		for (long policyId:aPolicies) {
			//MedPolicy policy= theManager.find(MedPolicy.class, policyId);
			//System.out.println("remove="+policy.getId());
			//listPolicies.remove(policy) ;
			theManager.createNativeQuery(new StringBuilder().append("delete from MedCase_MEdPolicy where medCase_id='")
			.append(aMedCaseId).append("' and policies_id='").append(policyId).append("'").toString()).executeUpdate();
		}
		
		//theManager.persist(hospital);
		//theManager.refresh(hospital);
		
	}
	
	public String getTemperatureCurve(long aMedCaseId) {
		HospitalMedCase hospital = theManager.find(HospitalMedCase.class,aMedCaseId);
		List<TemperatureCurve> list = theManager.createQuery("from TemperatureCurve where medCase_id=:medCase").setParameter("medCase", aMedCaseId).getResultList() ; 
				
		StringBuilder json = new StringBuilder() ;
		json.append("{\"childs\":[") ;
		boolean isFirst = true ;
		for (TemperatureCurve curve:list) {
			if (!isFirst) {
				json.append(",") ;
				//isFirst =  ;
			} else {
				isFirst=false ;
			}
			json.append("{");
			if (curve.getTakingDate()!=null) {
				SimpleDateFormat FORMAT_1 = new SimpleDateFormat("yyyy/MM/dd");
				String date = FORMAT_1.format(curve.getTakingDate().getTime()) ;
				json.append("\"date\":")
						.append("\"")
						.append(date)
						.append(" ") ;
				if (curve.getDayTime()!=null && curve.getDayTime().getCode()!=null && curve.getDayTime().getCode().equals("2")) {
					json.append("20") ;
				} else {
					json.append("8") ;
				}
				json.append(":00")
						.append("\",") ;
						
				
			}
			json.append("\"id\":").append(curve.getId()).append(",") ;
			json.append("\"pulse\":")
				.append(curve.getPulse())
				.append(",");
			json.append("\"bloodPressureDown\":")
				.append(curve.getBloodPressureDown())
				.append(",");
			json.append("\"bloodPressureUp\":")
				.append(curve.getBloodPressureUp())
				.append(",");
			json.append("\"weight\":")
				.append(curve.getWeight())
				.append(",");
			json.append("\"respirationRate\":")
				.append(curve.getRespirationRate())
				.append(",");
			json.append("\"degree\":")
				.append(curve.getDegree())
				.append("}");
		}
		json.append("]}");
		// TODO Auto-generated method stub
		return json.toString();
	}
	
	public List<IEntityForm> listAll(Long aParentId) throws EntityFormException {
		//Collection<HospitalMedCase> results = null ;
		//Patient patient = theManager.find(Patient.class, aParentId) ;
		StringBuilder query = new StringBuilder() ;
		query.append("select sls.id as f0slsid,case when sls.dtype='ExtHospitalMedCase' then sls.dtype else null end as f1isexthosp");
		query.append(",to_char(sls.dateStart,'dd.mm.yyyy') as f2dateStart,to_char(sls.dateFinish,'dd.mm.yyyy') as f3dateFinish") ;
		query.append(" ,vdh.id as f4vhdid,sls.username as f5slsusername,case when sls.emergency='1' then 'да' else null end as f6emergency") ;
		query.append(" ,coalesce(ss.code,'')||case when vdh.id is not null then ' '||vdh.name else '' end as f7stacard");
		query.append(" ,ml.name as f8entdep,mlLast.name as f9mlLastdep") ;
		query.append(" ,case when vdh.id is not null then null when (coalesce(sls.dateFinish,CURRENT_DATE)-sls.dateStart)=0 then 1 when vht.code='DAYTIMEHOSP' then ((coalesce(sls.dateFinish,CURRENT_DATE)-sls.dateStart)+1) else (coalesce(sls.dateFinish,CURRENT_DATE)-sls.dateStart) end as f10countDays") ;
		query.append(" ,list(vpd.name||' '||mkb.code) as f11diagDisch") ;
		query.append(" ,list(vpd1.name||' '||mkb1.code) as f12diagClinic") ;
		query.append(" from MedCase sls");
		query.append(" left join VocHospType vht on vht.id=sls.hospType_id");
		query.append(" left join VocDeniedHospitalizating vdh on vdh.id=sls.deniedHospitalizating_id");
		query.append(" left join MedCase sloLast on sloLast.parent_id=sls.id and UPPER(sloLast.dtype)='DEPARTMENTMEDCASE'");
		query.append(" left join StatisticStub ss on ss.id=sls.statisticStub_id");
		query.append(" left join MisLpu mlLast on mlLast.id=sloLast.department_id");
		query.append(" left join MisLpu ml on ml.id=sls.department_id");
		query.append("	left join Diagnosis diag on sls.id=diag.medCase_id");
		query.append("	left join VocIdc10 mkb on mkb.id=diag.idc10_id");
		query.append("	left join VocDiagnosisRegistrationType vdrt on vdrt.id=diag.registrationType_id and vdrt.code='3'");
		query.append("	left join VocPriorityDiagnosis vpd on vpd.id=diag.priority_id");
		query.append("	left join Diagnosis diag1 on sloLast.id=diag1.medCase_id");
		query.append("	left join VocIdc10 mkb1 on mkb1.id=diag1.idc10_id");
		query.append("	left join VocDiagnosisRegistrationType vdrt1 on vdrt1.id=diag1.registrationType_id and vdrt1.code='4'");
		query.append("	left join VocPriorityDiagnosis vpd1 on vpd1.id=diag1.priority_id");

		query.append(" where sls.patient_id=:patient and UPPER(sls.DTYPE) IN ('HOSPITALMEDCASE','EXTHOSPITALMEDCASE') and  (sloLast.id is null or sloLast.transferDate is null) ");
		query.append(" group by sls.id,sls.dtype,sls.dateStart,sls.dateFinish,vdh.id ,sls.username ,sls.emergency, ss.code,vdh.id,vdh.name ,ml.name,mlLast.name,vht.code");
		query.append(" order by sls.dateStart");
		 //Query query2 = theManager.createQuery(query.toString()) ;
        // query2.setParameter("patient", aParentId) ;
         //results = query2.setMaxResults(1000).getResultList();
		List<Object[]> list = theManager.createNativeQuery(query.toString()).setParameter("patient", aParentId).getResultList() ;
         //System.out.println("RESULT == "+results.size()) ;
         LinkedList<IEntityForm> ret = new LinkedList<IEntityForm>();
         for (Object[] hospit : list) {
             HospitalMedCaseForm form ;
        	 if (hospit[1]!=null) {
        		 form = new ExtHospitalMedCaseForm() ;
			} else {
				form = new HospitalMedCaseForm() ;
			}
             form.setId(ConvertSql.parseLong(hospit[0])) ;
             form.setIsDeniedHospitalizating(hospit[4]!=null?Boolean.TRUE:Boolean.FALSE) ;
             form.setDateStart(ConvertSql.parseString(hospit[2]));
             form.setDateFinish(ConvertSql.parseString(hospit[3]));
             //form.setFinishWorkerText(hospit.getFinishWorkerText());
             form.setUsername(ConvertSql.parseString(hospit[5]));
             form.setDaysCount(ConvertSql.parseString(hospit[10])) ;
             form.setStatCardNumber(ConvertSql.parseString(hospit[7])) ;
             form.setEmergency(hospit[6]!=null?Boolean.TRUE:Boolean.FALSE);
             form.setDischargeEpicrisis(ConvertSql.parseString(hospit[9]));
             form.setDepartmentInfo(ConvertSql.parseString(hospit[8]));
             form.setClinicalDiagnos(ConvertSql.parseString(hospit[12])) ;
             form.setConcludingDiagnos(ConvertSql.parseString(hospit[11])) ;
             ret.add(form);
             
         }
        return ret;
	}
	
	public List<SurgicalOperationForm> getSurgicalOperationByDate(String aDate) {
        if (CAN_DEBUG) {
            LOG.debug("findAllSpecialistTickets() aSpecialist = " + aDate);
        }
        QueryClauseBuilder builder = new QueryClauseBuilder();
        //builder.add("operationDate", aDate);
        Query query = builder.build(theManager, "from SurgicalOperation where operationDate='"+aDate+"' ", " order by operationTime");
        return createList(query);
	}
    private List<SurgicalOperationForm> createList(Query aQuery) {
        List<SurgicalOperation> list = aQuery.getResultList();
        List<SurgicalOperationForm> ret = new LinkedList<SurgicalOperationForm>();
        for (SurgicalOperation surOper : list) {
            try {
                ret.add(theEntityFormService.loadForm(SurgicalOperationForm.class, surOper));
            } catch (EntityFormException e) {
                throw new IllegalStateException(e);
            }
        }
        return ret;
    }
    
    public List<HospitalMedCaseForm> findOpenHospitalByDate(String aDate) {
	       QueryClauseBuilder builder = new QueryClauseBuilder();
	        Date date = null;
	        if(!StringUtil.isNullOrEmpty(aDate)) {
	            try {
	                date = DateFormat.parseSqlDate(aDate);
	            } catch (Exception e) {
	                LOG.warn("Ошибка преобразования даты "+aDate, e);
	            }
	        }
	        if (date != null){
	        	builder.add("dateStart", date);
	        } else {
	        	throw new IllegalDataException("Неправильная дата") ;
	        }
	        Query query = builder.build(theManager, "from MedCase where DTYPE='HospitalMedCase' and dateFinish is null  and deniedHospitalizating_id is null and (ambulanceTreatment is null or cast(ambulanceTreatment as int)=0)", " order by entranceTime");
	        System.out.println("Запрос по medCase: ");
	        System.out.println(query.toString()) ;
	        return createHospitalList(query);
	}
    
    private List<HospitalMedCaseForm> createHospitalList(Query aQuery) {
        List<HospitalMedCase> list = aQuery.getResultList();
        List<HospitalMedCaseForm> ret = new LinkedList<HospitalMedCaseForm>();
        for (HospitalMedCase medCase : list) {
            try {
                ret.add(theEntityFormService.loadForm(HospitalMedCaseForm.class, medCase));
            } catch (EntityFormException e) {
                throw new IllegalStateException(e);
            }
        }
        return ret;
    }
    
	@EJB ILocalEntityFormService theEntityFormService ;
    @PersistenceContext EntityManager theManager ;
    @Resource SessionContext theContext ;
    @EJB ILocalMonitorService theMonitorService;



}
