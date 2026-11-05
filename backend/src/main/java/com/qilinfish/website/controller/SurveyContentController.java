package com.qilinfish.website.controller;

import com.qilinfish.website.config.LangLocaleResolver;
import com.qilinfish.website.model.SurveyContentFormModel;
import com.qilinfish.website.model.SurveyFormModel;
import com.qilinfish.website.repository.SurveyRepository;
import com.qilinfish.website.vb.SurveyContentViewBinder;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;
import java.util.stream.IntStream;

/**
 * The article-app survey, /survey/content (spec B16–B25, appendix A). One submit sends the whole form;
 * a valid one is one row in the survey_content table (SurveyRepository), never in the verification survey's table.
 * The copy lives in i18n/survey_content*.properties, every key prefixed sc. (survey.properties has no prefix and
 * shares the MessageSource, so an unprefixed key here could silently replace one there).
 */
@Controller
public class SurveyContentController {

    // messages shown on an amount: the verification survey's wording (i18n/survey*.properties), same as qilinfish.js
    private static final String MSG_AMOUNT_INVALID = "err.amount.invalid";
    private static final String MSG_AMOUNT_TOO_LONG = "err.amount.tooLong";
    private static final String MSG_BLIND_REQUIRED = "sc.err.blind";
    private static final String MSG_HOURS = "sc.err.hours";

    private final SurveyRepository surveyRepository;
    private final MessageSource messages;

    public SurveyContentController(SurveyRepository __surveyRepository, MessageSource __messages) {
        this.surveyRepository = __surveyRepository;
        this.messages = __messages;
    }

    @GetMapping("/survey/content")
    public String renderContentSurvey(Model __model) {
        __model.addAttribute("vb", buildBinder());
        __model.addAttribute("contentForm", new SurveyContentFormModel());
        return "survey_content";
    }

    @PostMapping("/survey/content")
    public String submitContentSurvey(@Valid @ModelAttribute("contentForm") SurveyContentFormModel __form,
                                      BindingResult __bindingResult,
                                      Model __model,
                                      HttpServletRequest __request) {
        __model.addAttribute("vb", buildBinder());
        checkAnswers(__form, __bindingResult);
        if (__bindingResult.hasErrors()) {
            __model.addAttribute("submitStatus", "error");
            return "survey_content";
        }
        // the page language the respondent answered in; the choice values themselves are Chinese on both pages
        String lang = LangLocaleResolver.isEnglish(__request) ? "en" : "zh";
        if (surveyRepository.saveContentSurvey(__form, lang)) {
            __model.addAttribute("submitStatus", "success");
            __model.addAttribute("contentForm", new SurveyContentFormModel());
        } else {
            __model.addAttribute("submitStatus", "save-error");
        }
        return "survey_content";
    }

    /** Rules the annotations cannot say: allowed options, "other" needs words, numbers, the blind price, contact. */
    private void checkAnswers(SurveyContentFormModel __form, BindingResult __bindingResult) {
        checkOne(__bindingResult, "role", __form.getRole(), SurveyContentFormModel.ROLES);
        if (SurveyContentFormModel.OTHER.equals(__form.getRole()) && isBlank(__form.getRoleOther())) {
            __bindingResult.rejectValue("role", "otherRequired");
        }
        checkOne(__bindingResult, "teamSize", __form.getTeamSize(), SurveyContentFormModel.TEAM_SIZES);
        checkOne(__bindingResult, "monthlyPieces", __form.getMonthlyPieces(), SurveyContentFormModel.MONTHLY_PIECES);
        checkHours(__bindingResult, "checkHours", __form.getCheckHours());
        checkOne(__bindingResult, "afterDelivery", __form.getAfterDelivery(), SurveyContentFormModel.AFTER_DELIVERY);
        checkAll(__bindingResult, "tools", __form.getTools(), SurveyContentFormModel.TOOLS);
        // "none" and a tool cannot both be true; the page unticks the others (D39), so keep "none" here too
        if (__form.getTools().contains(SurveyContentFormModel.NO_TOOLS)) {
            __form.setTools(List.of(SurveyContentFormModel.NO_TOOLS));
            __form.setToolsOther(null);
        }
        checkOne(__bindingResult, "toolCount", __form.getToolCount(), SurveyContentFormModel.TOOL_COUNTS);
        checkHours(__bindingResult, "moveHours", __form.getMoveHours());
        checkAll(__bindingResult, "formats", __form.getFormats(), SurveyContentFormModel.FORMATS);
        checkBlind(__form, __bindingResult);
        checkOne(__bindingResult, "revealBuy", __form.getRevealBuy(), SurveyContentFormModel.REVEAL_BUY);
        checkOne(__bindingResult, "revealRenew", __form.getRevealRenew(), SurveyContentFormModel.REVEAL_RENEW);
        String contact = __form.getContact() == null ? "" : __form.getContact().trim();
        if (!contact.isEmpty() && !SurveyFormModel.isValidContact(contact)) {
            __bindingResult.rejectValue("contact", "invalid");
        }
        checkOne(__bindingResult, "trial", __form.getTrial(), SurveyContentFormModel.TRIAL);
    }

    /**
     * The blind price is required (spec B24): an option (不會付費 / 不知道) wins over a typed amount;
     * otherwise one US$ amount, digits with optional decimals (D35). All errors go on blindUsd.
     */
    private void checkBlind(SurveyContentFormModel __form, BindingResult __bindingResult) {
        String option = __form.getBlindOption();
        if (option != null && SurveyContentFormModel.BLIND_OPTIONS.contains(option)) {
            return;
        }
        if (!isBlank(option) || SurveyFormModel.isBlankUsd(__form.getBlindUsd())) {
            __bindingResult.rejectValue("blindUsd", "required", msg(MSG_BLIND_REQUIRED));
            return;
        }
        SurveyFormModel.UsdProblem problem = SurveyFormModel.usdProblem(__form.getBlindUsd());
        if (problem == SurveyFormModel.UsdProblem.INVALID) {
            __bindingResult.rejectValue("blindUsd", "invalid", msg(MSG_AMOUNT_INVALID));
        } else if (problem == SurveyFormModel.UsdProblem.TOO_LONG) {
            __bindingResult.rejectValue("blindUsd", "tooLong", msg(MSG_AMOUNT_TOO_LONG));
        }
    }

    /** Optional hours per piece (D36): blank, or a number with optional decimals. */
    private void checkHours(BindingResult __bindingResult, String __field, String __value) {
        if (!SurveyFormModel.isBlankUsd(__value) && SurveyFormModel.usdProblem(__value) != null) {
            __bindingResult.rejectValue(__field, "invalid", msg(MSG_HOURS));
        }
    }

    /** A single-choice answer, when given, must be one of the page's options (required ones are @NotBlank). */
    private void checkOne(BindingResult __bindingResult, String __field, String __value, List<String> __allowed) {
        if (!isBlank(__value) && !__allowed.contains(__value) && !__bindingResult.hasFieldErrors(__field)) {
            __bindingResult.rejectValue(__field, "invalid");
        }
    }

    private void checkAll(BindingResult __bindingResult, String __field, List<String> __values, List<String> __allowed) {
        if (!__allowed.containsAll(__values)) {
            __bindingResult.rejectValue(__field, "invalid");
        }
    }

    private boolean isBlank(String __value) {
        return __value == null || __value.isBlank();
    }

    /** A page message in the language of the current request (LangLocaleResolver decides it from the URL). */
    private String msg(String __key) {
        return messages.getMessage(__key, null, LocaleContextHolder.getLocale());
    }

    /** Option labels are sc.opt.<group>.<n>, n counting from 1 in the order of the Model's list. */
    private List<SurveyContentViewBinder.Choice> choices(String __group, List<String> __values) {
        return IntStream.range(0, __values.size())
            .mapToObj(i -> new SurveyContentViewBinder.Choice(__values.get(i), msg("sc.opt." + __group + "." + (i + 1))))
            .toList();
    }

    private SurveyContentViewBinder buildBinder() {
        SurveyContentViewBinder vb = new SurveyContentViewBinder();
        vb.setRoles(choices("role", SurveyContentFormModel.ROLES));
        vb.setTeamSizes(choices("team", SurveyContentFormModel.TEAM_SIZES));
        vb.setMonthlyPieces(choices("pieces", SurveyContentFormModel.MONTHLY_PIECES));
        vb.setAfterDelivery(choices("after", SurveyContentFormModel.AFTER_DELIVERY));
        vb.setAfterDeliveryFollowUpRule("afterDelivery=" + String.join("|", SurveyContentFormModel.AFTER_DELIVERY_FOLLOW_UP));
        vb.setTools(choices("tools", SurveyContentFormModel.TOOLS));
        vb.setNoTools(SurveyContentFormModel.NO_TOOLS);
        vb.setToolCounts(choices("toolCount", SurveyContentFormModel.TOOL_COUNTS));
        vb.setFormats(choices("formats", SurveyContentFormModel.FORMATS));
        vb.setBlindOptions(choices("blind", SurveyContentFormModel.BLIND_OPTIONS));
        vb.setRevealBuy(choices("buy", SurveyContentFormModel.REVEAL_BUY));
        vb.setRevealRenew(choices("renew", SurveyContentFormModel.REVEAL_RENEW));
        vb.setTrial(choices("trial", SurveyContentFormModel.TRIAL));
        return vb;
    }
}
