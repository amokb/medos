package ru.ecom.mis.web.tags;

import ru.ecom.mis.ejb.service.lpu.ILpuService;
import ru.ecom.web.util.Injection;

import javax.naming.NamingException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.SimpleTagSupport;
import java.io.IOException;

/**
 *
 */
public class CanShowAreasTag extends SimpleTagSupport {

    public void doTag() throws JspException, IOException {
        PageContext ctx = (PageContext) getJspContext();
        HttpServletRequest request = (HttpServletRequest) ctx.getRequest();
        try {
            ILpuService service = Injection.find(request).getService(ILpuService.class);
            if (service.canShowAreas(Long.parseLong(request.getParameter("id")))) {
                getJspBody().invoke(getJspContext().getOut());
            }
        } catch (NamingException e) {
            throw new JspException(e);
        }
    }
}
