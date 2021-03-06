package ru.ecom.mis.ejb.form.workcalendar.interceptor;

import ru.ecom.ejb.services.entityform.IEntityForm;
import ru.ecom.ejb.services.entityform.interceptors.IParentFormInterceptor;
import ru.ecom.ejb.services.entityform.interceptors.InterceptorContext;
import ru.ecom.mis.ejb.form.workcalendar.PlanOphtHospitalByVisitForm;
import ru.ecom.mis.ejb.form.workcalendar.PlanOphtHospitalForm;
import ru.nuzmsh.forms.response.FormMessage;
import ru.nuzmsh.util.format.DateFormat;

import javax.persistence.EntityManager;
import java.util.Date;
import java.util.List;

/**
 * Created by Milamesher on 06.11.2019.
 */
public class PlanOphtHospitalCreate implements IParentFormInterceptor {
    @Override
    public void intercept(IEntityForm aForm, Object aEntity, Object aParentId, InterceptorContext aContext) {
        PlanOphtHospitalForm form = (PlanOphtHospitalForm) aForm;
        Date date = new Date();
        String username = aContext.getSessionContext().getCallerPrincipal().toString() ;
        form.setCreateUsername(username);
        form.setCreateDate(DateFormat.formatToDate(date));

        EntityManager manager = aContext.getEntityManager();
        String parentStr = form instanceof PlanOphtHospitalByVisitForm? "vis" : "pat";
        List<Object[]> list = manager.createNativeQuery("select pre.id,to_char(pre.createDate,'dd.MM.yyyy') ||' '|| e.name ||' ' || vwf.name ||' '||wpat.lastname as info" +
                " from workcalendarhospitalbed pre " +
                " left join patient pat on pat.id=pre.patient_id" +
                " left join mislpu ml on ml.id=pre.department_id" +
                " left join workfunction wf on wf.id=pre.workfunction_id" +
                " left join worker w on w.id=wf.worker_id" +
                " left join vocworkfunction vwf on vwf.id=wf.workfunction_id" +
                " left join patient wpat on wpat.id=w.person_id" +
                " left join voceye e on e.id=pre.eye_id" +
                " left join medcase vis on vis.id=pre.visit_id" +
                " where pre.dtype='PlanOphtHospital' and "+parentStr+".id =:id and pre.createDate>=current_date order by pre.createDate").setParameter("id",aParentId).getResultList();
        if (!list.isEmpty()) {
            StringBuilder sb = new StringBuilder("ВНИМАНИЕ, у пациента созданы направления на введение ингибиторов ангиогенеза: ");
            for (Object[] obj: list){
                sb.append("<br><a href='entityView-stac_planOphtHospital.do?id=").append(obj[0]).append("'>")
                        .append(obj[1]).append("</a>");
            }
            FormMessage formMessage = new FormMessage(sb.toString());
            form.addMessage(formMessage);
        }
    }
}
