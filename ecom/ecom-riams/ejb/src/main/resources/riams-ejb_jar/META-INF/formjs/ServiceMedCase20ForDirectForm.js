function onCreate(aForm, aEntity, aContext) {
    var addMedServicies = aForm.additionMedService.split("#");
    if (addMedServicies.length > 0 && aForm.additionMedService != null && aForm.additionMedService != "") {
        var parent = aEntity.parent;
        for (var i = 0; i < addMedServicies.length; i++) {
            var param = addMedServicies[i].split(":");
            var par1 = java.lang.Long.valueOf(param[0]);
            var par4 = (param[3]) ? java.lang.Long.valueOf(param[3]) : null;
            var medService = aContext.manager.find(Packages.ru.ecom.mis.ejb.domain.medcase.MedService, par1);
            var cnt = java.lang.Integer.valueOf(param[2]);
            var adMedService = new Packages.ru.ecom.mis.ejb.domain.medcase.ServiceMedCase();
            adMedService.setParent(parent);
            adMedService.setMedService(medService);
            adMedService.setCountMedService(cnt);
            aContext.manager.persist(adMedService);
        }
    }
}