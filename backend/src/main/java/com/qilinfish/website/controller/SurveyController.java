package com.qilinfish.website.controller;

import com.qilinfish.website.model.SurveyFormModel;
import com.qilinfish.website.repository.SurveyRepository;
import com.qilinfish.website.vb.SurveyViewBinder;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import java.math.BigDecimal;
import java.util.Set;
import java.util.regex.Pattern;

@Controller
public class SurveyController {

    private static final Set<String> SITUATIONS = Set.of("verify", "explain", "inventory", "other", "none");
    private static final Set<String> ANSWER_OPTIONS = Set.of("none", "unknown");
    private static final Pattern EMAIL = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");
    private static final Pattern PHONE = Pattern.compile("^\\+?[0-9()\\-\\s#]{7,24}$");

    // amount-question messages: same wording as amountError in qilinfish.js, shown via #fields.errors(...)[0]
    private static final String MSG_PAY_REQUIRED = "> 請填金額範圍，或選「不會付費」「不知道」。";
    private static final String MSG_PAY_PARTIAL = "> 最低和最高都要填；只願付一個金額的話，兩格填一樣。";
    private static final String MSG_PAY_RANGE = "> 最高金額不可小於最低金額。";
    private static final String MSG_AMOUNT_INVALID = "> 金額只能填數字（不加幣別），例如 500 或 1,200.5。";
    private static final String MSG_AMOUNT_TOO_LONG = "> 金額最多 16 位數。";
    private static final String MSG_EXTRA_SPEND_OPTION = "> 請選「沒有」「不知道」，或填一個金額。";

    private final SurveyRepository surveyRepository;

    public SurveyController(SurveyRepository __surveyRepository) {
        this.surveyRepository = __surveyRepository;
    }

    @GetMapping("/survey")
    public String renderSurvey(Model __model) {
        __model.addAttribute("vb", buildSurveyBinder());
        __model.addAttribute("surveyForm", new SurveyFormModel());
        __model.addAttribute("activeNav", "survey");
        return "survey";
    }

    @PostMapping("/survey")
    public String submitSurvey(@Valid @ModelAttribute("surveyForm") SurveyFormModel __form,
                               BindingResult __bindingResult,
                               Model __model) {
        __model.addAttribute("vb", buildSurveyBinder());
        __model.addAttribute("activeNav", "survey");

        checkConditionalFields(__form, __bindingResult);
        if (__bindingResult.hasErrors()) {
            __model.addAttribute("submitStatus", "error");
            return "survey";
        }
        boolean ok = surveyRepository.saveSurvey(__form);
        if (ok) {
            __model.addAttribute("submitStatus", "success");
            __model.addAttribute("surveyForm", new SurveyFormModel());
        } else {
            __model.addAttribute("submitStatus", "save-error");
        }
        return "survey";
    }

    /** Story questions are required unless the respondent picked "none of these happened". */
    private void checkConditionalFields(SurveyFormModel __form, BindingResult __bindingResult) {
        String situation = __form.getSituation();
        if (situation != null && !situation.isBlank() && !SITUATIONS.contains(situation)) {
            __bindingResult.rejectValue("situation", "invalid");
        }
        if ("other".equals(situation)) {
            rejectIfBlank(__bindingResult, "situation", __form.getSituationOther());
        }
        if (situation != null && SITUATIONS.contains(situation) && !"none".equals(situation)) {
            rejectIfBlank(__bindingResult, "occurredWhen", __form.getOccurredWhen());
            rejectIfBlank(__bindingResult, "whatHappened", __form.getWhatHappened());
            rejectIfBlank(__bindingResult, "steps", __form.getSteps());
            rejectIfBlank(__bindingResult, "triedOther", __form.getTriedOther());
        }
        if (!"none".equals(situation)) {
            checkExtraSpend(__form, __bindingResult);
        }
        checkPay(__form, __bindingResult);
        String contact = __form.getContact() == null ? "" : __form.getContact().trim();
        if (!contact.isEmpty() && !EMAIL.matcher(contact).matches() && !PHONE.matcher(contact).matches()) {
            __bindingResult.rejectValue("contact", "invalid");
        }
    }

    /**
     * Extra spend is optional: a picked option (none / unknown) wins; otherwise a typed amount
     * must be a number. All errors go on extraSpendUsd so the page marks one place.
     */
    private void checkExtraSpend(SurveyFormModel __form, BindingResult __bindingResult) {
        String option = __form.getExtraSpend();
        if (option != null && ANSWER_OPTIONS.contains(option)) {
            return;
        }
        if (option != null && !option.isBlank()) {
            __bindingResult.rejectValue("extraSpendUsd", "invalidOption", MSG_EXTRA_SPEND_OPTION);
            return;
        }
        String usd = __form.getExtraSpendUsd();
        if (!SurveyFormModel.isBlankUsd(usd)) {
            rejectAmount(__bindingResult, "extraSpendUsd", SurveyFormModel.usdProblem(usd));
        }
    }

    /** Reject an amount field with the message for its problem; does nothing when there is none. */
    private boolean rejectAmount(BindingResult __bindingResult, String __field, SurveyFormModel.UsdProblem __problem) {
        if (__problem == null) return false;
        if (__problem == SurveyFormModel.UsdProblem.INVALID) {
            __bindingResult.rejectValue(__field, "invalid", MSG_AMOUNT_INVALID);
        } else {
            __bindingResult.rejectValue(__field, "tooLong", MSG_AMOUNT_TOO_LONG);
        }
        return true;
    }

    /**
     * Willingness to pay is required for everyone: either an option (none / unknown),
     * or both ends of a US$ range with max >= min. A picked option wins over typed amounts.
     * All errors go on payMin so the page marks one place.
     */
    private void checkPay(SurveyFormModel __form, BindingResult __bindingResult) {
        String option = __form.getPayOption();
        if (option != null && ANSWER_OPTIONS.contains(option)) {
            return;
        }
        if (option != null && !option.isBlank()) {
            __bindingResult.rejectValue("payMin", "invalidOption", MSG_PAY_REQUIRED);
            return;
        }
        // same order as amountError in qilinfish.js: a non-number wins, then too long, then blank, then half-filled, then range
        boolean minBlank = SurveyFormModel.isBlankUsd(__form.getPayMin());
        boolean maxBlank = SurveyFormModel.isBlankUsd(__form.getPayMax());
        SurveyFormModel.UsdProblem minProblem = minBlank ? null : SurveyFormModel.usdProblem(__form.getPayMin());
        SurveyFormModel.UsdProblem maxProblem = maxBlank ? null : SurveyFormModel.usdProblem(__form.getPayMax());
        SurveyFormModel.UsdProblem problem = minProblem == SurveyFormModel.UsdProblem.INVALID || maxProblem == null ? minProblem : maxProblem;
        if (rejectAmount(__bindingResult, "payMin", problem)) {
            return;
        }
        String min = SurveyFormModel.normalizeUsd(__form.getPayMin());
        String max = SurveyFormModel.normalizeUsd(__form.getPayMax());
        if (minBlank && maxBlank) {
            __bindingResult.rejectValue("payMin", "required", MSG_PAY_REQUIRED);
            return;
        }
        if (minBlank || maxBlank) {
            __bindingResult.rejectValue("payMin", "partial", MSG_PAY_PARTIAL);
            return;
        }
        if (new BigDecimal(max).compareTo(new BigDecimal(min)) < 0) {
            __bindingResult.rejectValue("payMin", "range", MSG_PAY_RANGE);
        }
    }

    private void rejectIfBlank(BindingResult __bindingResult, String __field, String __value) {
        if (__value == null || __value.isBlank()) {
            __bindingResult.rejectValue(__field, "required");
        }
    }

    private SurveyViewBinder buildSurveyBinder() {
        SurveyViewBinder vb = new SurveyViewBinder();
        vb.setPageTitle("說說您最近一次的經驗");
        vb.setPageSubtitle("SURVEY // tell us about the last time");
        return vb;
    }
}
