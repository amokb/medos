package ru.ecom.mis.ejb.service.jaas;

import ru.ecom.ejb.services.monitor.ILocalMonitorService;
import ru.ecom.ejb.services.monitor.IMonitor;
import ru.ecom.jaas.ejb.service.ISecPolicyImportService;
import ru.ecom.mis.ejb.domain.lpu.LpuArea;
import ru.ecom.mis.ejb.domain.lpu.MisLpu;

import javax.annotation.EJB;
import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.naming.NamingException;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.HashMap;
import java.util.List;

/**
 * Работа с политиками безопасности
 */
@Stateless
@Remote(IJaasMisService.class)
public class JaasMisServiceBean implements IJaasMisService {

    @SuppressWarnings("unchecked")
	public void updateJaasPolicy(long aMinitorId) {


        List<MisLpu> list = manager.createQuery("from MisLpu").getResultList();
        List<LpuArea> ares = manager.createQuery("from LpuArea").getResultList();

        IMonitor monitor = monitorService.startMonitor(aMinitorId, "Синхронизация политик", list.size()+ares.size()) ;
        try {
            ISecPolicyImportService service = findPolicyService() ;

            // Обновление участков
            for (LpuArea area : ares) {
                if(monitor.isCancelled()) throw new IllegalMonitorStateException("Прервано пользователем") ;
                MisLpu lpu = area.getLpu();
                importPolicy(service, "/Policy/Mis/MisLpuDynamic/"+lpu.getId()+"/Areas", area.getId()
                        , area.getNumber() + " " + area.getTypeName());
                monitor.setText(area.getName());
                monitor.advice(1);
            }

            // Обновление ЛПУ
            for (MisLpu lpu : list) {
                if(monitor.isCancelled()) throw new IllegalMonitorStateException("Прервано пользователем") ;
                importPolicy(service, "/Policy/Mis/MisLpuDynamic/", lpu.getId(), lpu.getName());
                monitor.setText(lpu.getName());
                monitor.advice(1);
            }
            monitor.finish("");
        } catch (Exception e) {
            monitor.error("Ошибка синхронизации", e) ;
        }
    }

    private static void importPolicy(ISecPolicyImportService aService, String aPrefix, long aId, String aName) {
        String prefix = aPrefix + "/" + aId;
        HashMap<String,String> map = new HashMap<>();
        map.put(String.valueOf(aId), aName) ;
        aService.importPolicy(prefix+"/View",map) ;
        aService.importPolicy(prefix+"/Edit",map) ;
        aService.importPolicy(prefix+"/Create",map) ;
        aService.importPolicy(prefix+"/Delete",map) ;
    }

    private ISecPolicyImportService findPolicyService() throws NamingException {
    	//return EjbInjection.getInstance().getLocalService(ISecPolicyImportService.class) ;
    	return secPolicyImportService ;
    }

    private @EJB ISecPolicyImportService secPolicyImportService ;
//    @EJB(name="jaas-app/SecPolicyImportServiceBean/remote")
//    ISecPolicyImportService policyImportService ;

    @PersistenceContext EntityManager manager ;

    @EJB ILocalMonitorService monitorService;

}
