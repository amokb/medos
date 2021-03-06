package ru.nuzmsh.web.tags;

import org.apache.struts.action.ActionForm;
import org.apache.struts.taglib.html.Constants;
import ru.nuzmsh.forms.validator.BaseValidatorForm;
import ru.nuzmsh.web.util.IdeTagHelper;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.PageContext;
import java.io.IOException;

import static ru.nuzmsh.util.BooleanUtils.isTrue;

/**
 * @jsp.tag name="submitCancelButtonsRow"
 * display-name="submitCancelButtonsRow"
 * body-content="JSP"
 * description="submitCancelButtonsRow"
 */
public class SubmitCancelButtonsRowTag extends AbstractGuidSupportTag {

    /**
     * Функция сохранения
     *
     * @jsp.attribute description="Функция сохранения"
     * required="false"
     * rtexprvalue="true"	 *
     */
    public String getFunctionSubmit() {
        return theFunctionSubmit;
    }

    public void setFunctionSubmit(String aFunctionSubmit) {
        theFunctionSubmit = aFunctionSubmit;
    }

    /**
     * Функция сохранения
     */
    private String theFunctionSubmit;

    /**
     * Количество столбцов, занимаемое кнопками
     *
     * @jsp.attribute description="Количество столбцов, занимаемое кнопками"
     * required="true"
     * rtexprvalue="true"
     */
    public int getColSpan() {
        return theColSpan;
    }

    public void setColSpan(int aColSpan) {
        theColSpan = aColSpan;
    }

    /**
     * На показывать кнопку отмены
     *
     * @jsp.attribute description="а показывать кнопку отмены"
     * required="false"
     * rtexprvalue="true"
     */
    public boolean getNotDisplayCancel() {
        return theNotDisplayCancel;
    }

    public void setNotDisplayCancel(boolean aNotDisplayCancel) {
        theNotDisplayCancel = aNotDisplayCancel;
    }

    /**
     * Не показывать кпонку отмены
     */
    private boolean theNotDisplayCancel = false;

    /**
     * Подпись на кнопке СОЗДАТЬ
     *
     * @jsp.attribute description="Подпись на кнопке СОЗДАТЬ"
     * required="false"
     * rtexprvalue="true"
     */
    public String getLabelCreate() {
        return theLabelCreate;
    }

    public void setLabelCreate(String aLabelCreate) {
        theLabelCreate = aLabelCreate;
    }

    /**
     * Подписть на кнопке создать, при ее нажатии
     *
     * @jsp.attribute description="Подписть на кнопке создать, при ее нажатии"
     * required="false"
     * rtexprvalue="true"
     */
    public String getLabelCreating() {
        return theLabelCreating;
    }

    public void setLabelCreating(String aLabelCreating) {
        theLabelCreating = aLabelCreating;
    }

    /**
     * Подпись на кнопке СОХРАНИТЬ
     *
     * @jsp.attribute description="Подпись на кнопке СОХРАНИТЬ"
     * required="false"
     * rtexprvalue="true"
     */
    public String getLabelSave() {
        return theLabelSave;
    }

    public void setLabelSave(String aLabelSave) {
        theLabelSave = aLabelSave;
    }

    /**
     * Подписть на кнопке СОХРАНИТЬ, при ее нажатии
     *
     * @jsp.attribute description="Подписть на кнопке СОХРАНИТЬ, при ее нажатии"
     * required="false"
     * rtexprvalue="true"
     */
    public String getLabelSaving() {
        return theLabelSaving;
    }

    public void setLabelSaving(String aLabelSaving) {
        theLabelSaving = aLabelSaving;
    }

    /**
     * Не disable кнопки
     *
     * @jsp.attribute description = "Не disable кнопки"
     * required = "false"
     * rtexprvalue = "true"
     */
    public boolean getDoNotDisableButtons() {
        return theDoNotDisableButtons;
    }

    public void setDoNotDisableButtons(boolean aDoNotDisableButtons) {
        theDoNotDisableButtons = aDoNotDisableButtons;
    }

    /**
     * Не disable кнопки
     */
    private boolean theDoNotDisableButtons = false;

    /**
     * Подпись на кнопке SUMBIT
     *
     * @jsp.attribute description="Подпись на кнопке SUMBIT"
     * required="false"
     * rtexprvalue="true"
     * @deprecated используйте setLabelSave()
     */
    public String getSubmitLabel() {
        return getLabelSave();
    }

    /**
     * @param aSumbitLabel
     * @deprecated используйте setLabelSave()
     */
    public void setSubmitLabel(String aSumbitLabel) {
        setLabelSave(aSumbitLabel);
    }

    /**
     * Подпись на кнопке из attribute
     *
     * @jsp.attribute description="Подпись на кнопке из attribute"
     * required="false"
     * rtexprvalue="true"
     */
    public String getSubmitLabelAttribute() {
        return theSubmitLabelAttribute;
    }

    public void setSubmitLabelAttribute(String aSubmitLabelAttribute) {
        theSubmitLabelAttribute = aSubmitLabelAttribute;
    }


    private ActionForm getForm() {
        return (ActionForm) pageContext.getAttribute(Constants.BEAN_KEY, PageContext.REQUEST_SCOPE);
    }

    protected boolean isViewOnly() {
        ActionForm form = getForm();
        if (form instanceof BaseValidatorForm) {
            BaseValidatorForm bvForm = (BaseValidatorForm) form;
            return bvForm.isViewOnly();
        }
        return false;
    }

    protected boolean isTypeCreate() {
        ActionForm form = getForm();
        if (form instanceof BaseValidatorForm) {
            BaseValidatorForm bvForm = (BaseValidatorForm) form;
            return bvForm.isTypeCreate();
        }
        return false;
    }


    public int doEndTag() throws JspException {
        JspWriter out = pageContext.getOut();
        try {
            out.println("</td></tr>");
        } catch (IOException e) {
            throw new JspException(e);
        }

        return super.doEndTag();    //To change body of overridden methods use File | Settings | File Templates.
    }

    public int doStartTag() throws JspException {
        if (isViewOnly()) {

        } else {
            try {
                JspWriter out = pageContext.getOut();

                out.print("<tr><td class='buttons' colspan='");
                out.print(theColSpan);
                out.println("'>");

                IdeTagHelper.getInstance().printMarker(this, pageContext);
                if (!theNotDisplayCancel) {
                    out.println("<input id='cancelButton' type='button' onclick='this.disabled=true; window.history.back()' title='Отменить изменения [SHIFT+ESC]'value='Отменить'/>");
                }

                out.print("<input id='submitButton' class='default' type='button' value='");
                String label = isTypeCreate() ? getLabelCreate() : getLabelSave();
                out.print(label);
                out.print("' onclick=\"this.value='");
                String labelIng = isTypeCreate() ? getLabelCreating() : getLabelSaving();

                out.print(labelIng);
                out.print("'; this.disabled=true; ");
                if (theFunctionSubmit != null && !theFunctionSubmit.equals("")) {
                    out.print(theFunctionSubmit);
                } else {
                    out.print("this.form.submit(); ");
                }

                if (!theDoNotDisableButtons) {
                    out.print("  ");
                }
                out.print("return true ;\"");
                out.print(" title=\"");
                out.print(label);
                out.print(" [CTRL+ENTER]");
                out.print("\"");
                out.print(" />");

                String lastDisplayedField = (String) pageContext.getAttribute(AbstractFieldTag.LAST_DISPLAYED_FIELDNAME, PageContext.REQUEST_SCOPE);
                Boolean lastDisplayedFieldIsEsc = (Boolean) pageContext.getAttribute(AbstractFieldTag.LAST_DISPLAYED_FIELDNAME_IsEsc, PageContext.REQUEST_SCOPE);
                if (lastDisplayedField != null) {
                    if (isTrue(lastDisplayedFieldIsEsc)) {
                        AbstractFieldTag.printEscSupport(pageContext, lastDisplayedField, "submitButton", this);
                    } else {
                        AbstractFieldTag.printEnterSupport(pageContext, lastDisplayedField, "submitButton", this);
                    }
                }

            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }
        return EVAL_BODY_INCLUDE;
    }

    /**
     * Подпись на кнопке SUMBIT
     */
    /**
     * Количество столбцов, занимаемое кнопками
     */
    private int theColSpan;
    /**
     * Подпись на кнопке из attribute
     *
     * @deprecated используйте setLabelSave()
     */
    private String theSubmitLabelAttribute;
    /**
     * Подписть на кнопке СОХРАНИТЬ, при ее нажатии
     */
    private String theLabelSaving = "Сохранение изменений ...";
    /**
     * Подпись на кнопке СОХРАНИТЬ
     */
    private String theLabelSave = "Сохранить изменения     ";
    /**
     * Подписть на кнопке создать, при ее нажатии
     */
    private String theLabelCreating = "Создание ...";
    /**
     * Подпись на кнопке СОЗДАТЬ
     */
    private String theLabelCreate = "Создать";

}
