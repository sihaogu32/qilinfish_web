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

import java.util.Set;
import java.util.regex.Pattern;

@Controller
public class SurveyController {

    private static final Set<String> SITUATIONS = Set.of("verify", "explain", "inventory", "other", "none");
    private static final Pattern EMAIL = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");
    private static final Pattern PHONE = Pattern.compile("^\\+?[0-9()\\-\\s#]{7,24}$");

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
        String contact = __form.getContact() == null ? "" : __form.getContact().trim();
        if (!contact.isEmpty() && !EMAIL.matcher(contact).matches() && !PHONE.matcher(contact).matches()) {
            __bindingResult.rejectValue("contact", "invalid");
        }
    }

    private void rejectIfBlank(BindingResult __bindingResult, String __field, String __value) {
        if (__value == null || __value.isBlank()) {
            __bindingResult.rejectValue(__field, "required");
        }
    }

    private SurveyViewBinder buildSurveyBinder() {
        SurveyViewBinder vb = new SurveyViewBinder();
        vb.setPageTitle("說說你最近一次的經驗");
        vb.setPageSubtitle("SURVEY // tell us about the last time");
        return vb;
    }
}
