package com.qilinfish.website.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Form-binding Model for the experience survey.
 * Annotations cover the always-required fields and length caps; the
 * "required unless situation = none" rules live in SurveyController,
 * because they depend on another field's value.
 */
public class SurveyFormModel {

    // ---- about you ----
    @NotBlank @Size(max = 32)
    private String role;
    @Size(max = 64)
    private String roleOther;
    @NotBlank @Size(max = 32)
    private String orgSize;

    // ---- the last time ----
    @NotBlank @Size(max = 16)
    private String situation;            // verify / explain / inventory / other / none
    @Size(max = 200)
    private String situationOther;       // the respondent's own words when situation = other
    @Size(max = 32)
    private String occurredWhen;
    @Size(max = 2000)
    private String whatHappened;
    @Size(max = 2000)
    private String steps;
    @Size(max = 2000)
    private String outcome;

    // ---- cost ----
    @Size(max = 16)
    private String hours;
    @Size(max = 16)
    private String people;
    // no @Size on the amount questions: SurveyController checks them (length included) so each
    // shows one message in the page's wording, not a Bean Validation default
    private String extraSpend;           // none / unknown / blank (then extraSpendUsd holds the amount)
    private String extraSpendUsd;        // US dollars, digits with optional thousands commas and decimals
    @Size(max = 2000)
    private String worstCase;

    // ---- what you tried ----
    @Size(max = 2000)
    private String triedOther;
    @Size(max = 2000)
    private String hardestStep;

    // ---- willingness to pay (US$ per year; checked in SurveyController, see extraSpend) ----
    private String payMin;
    private String payMax;
    private String payOption;           // none / unknown / blank (then payMin..payMax holds the range)

    // ---- situation = none ----
    @Size(max = 2000)
    private String whyNotNeeded;

    // ---- follow-up ----
    @Size(max = 2000)
    private String message;              // anything else the respondent wants to tell us

    // ---- contact ----
    @NotBlank @Size(max = 64)
    private String name;
    @NotBlank @Size(max = 128)
    private String company;
    @NotBlank @Size(max = 128)
    private String contact;              // email or phone

    /** Same as maxlength on the amount boxes in survey.html: room for commas and spaces. */
    public static final int USD_BOX_LENGTH = 24;
    /** Digits allowed in an amount, after commas and spaces are dropped (decimal point not counted). */
    public static final int USD_MAX_DIGITS = 16;

    /** What is wrong with a typed amount; same order as amountError in qilinfish.js. */
    public enum UsdProblem { INVALID, TOO_LONG }

    public SurveyFormModel() { }

    /**
     * Check a non-blank US-dollar amount typed by the respondent.
     * Returns null when it is a non-negative number with optional decimals and at most USD_MAX_DIGITS digits.
     */
    public static UsdProblem usdProblem(String __value) {
        String s = plainUsd(__value);
        if (!s.matches("\\d+(\\.\\d+)?")) return UsdProblem.INVALID;
        if (__value.length() > USD_BOX_LENGTH || s.replace(".", "").length() > USD_MAX_DIGITS) return UsdProblem.TOO_LONG;
        return null;
    }

    /**
     * Normalize a US-dollar amount: returns the plain number (e.g. "1,200.50" or "１，２００．５" -> "1200.5"),
     * or null if blank or usdProblem finds anything wrong.
     */
    public static String normalizeUsd(String __value) {
        if (isBlankUsd(__value) || usdProblem(__value) != null) return null;
        return new java.math.BigDecimal(plainUsd(__value)).stripTrailingZeros().toPlainString();
    }

    /** True when nothing is left after dropping commas and spaces, same as the page's amount check. */
    public static boolean isBlankUsd(String __value) {
        return __value == null || plainUsd(__value).isEmpty();
    }

    /**
     * Same as plainAmount in qilinfish.js: full-width digits, comma and point become half-width,
     * then commas and spaces are dropped. (?U) so full-width and no-break spaces count as spaces.
     */
    private static String plainUsd(String __value) {
        StringBuilder sb = new StringBuilder(__value.length());
        for (char c : __value.toCharArray()) {
            sb.append((c >= '０' && c <= '９') || c == '，' || c == '．' ? (char) (c - 0xFEE0) : c);
        }
        return sb.toString().replaceAll("(?U)[,\\s]", "");
    }

    public String getRole() { return role; }
    public void setRole(String __role) { this.role = __role; }

    public String getRoleOther() { return roleOther; }
    public void setRoleOther(String __roleOther) { this.roleOther = __roleOther; }

    public String getOrgSize() { return orgSize; }
    public void setOrgSize(String __orgSize) { this.orgSize = __orgSize; }

    public String getSituation() { return situation; }
    public void setSituation(String __situation) { this.situation = __situation; }

    public String getSituationOther() { return situationOther; }
    public void setSituationOther(String __situationOther) { this.situationOther = __situationOther; }

    public String getOccurredWhen() { return occurredWhen; }
    public void setOccurredWhen(String __occurredWhen) { this.occurredWhen = __occurredWhen; }

    public String getWhatHappened() { return whatHappened; }
    public void setWhatHappened(String __whatHappened) { this.whatHappened = __whatHappened; }

    public String getSteps() { return steps; }
    public void setSteps(String __steps) { this.steps = __steps; }

    public String getOutcome() { return outcome; }
    public void setOutcome(String __outcome) { this.outcome = __outcome; }

    public String getHours() { return hours; }
    public void setHours(String __hours) { this.hours = __hours; }

    public String getPeople() { return people; }
    public void setPeople(String __people) { this.people = __people; }

    public String getExtraSpend() { return extraSpend; }
    public void setExtraSpend(String __extraSpend) { this.extraSpend = __extraSpend; }

    public String getExtraSpendUsd() { return extraSpendUsd; }
    public void setExtraSpendUsd(String __extraSpendUsd) { this.extraSpendUsd = __extraSpendUsd; }

    public String getPayMin() { return payMin; }
    public void setPayMin(String __payMin) { this.payMin = __payMin; }

    public String getPayMax() { return payMax; }
    public void setPayMax(String __payMax) { this.payMax = __payMax; }

    public String getPayOption() { return payOption; }
    public void setPayOption(String __payOption) { this.payOption = __payOption; }

    public String getWorstCase() { return worstCase; }
    public void setWorstCase(String __worstCase) { this.worstCase = __worstCase; }

    public String getTriedOther() { return triedOther; }
    public void setTriedOther(String __triedOther) { this.triedOther = __triedOther; }

    public String getHardestStep() { return hardestStep; }
    public void setHardestStep(String __hardestStep) { this.hardestStep = __hardestStep; }

    public String getWhyNotNeeded() { return whyNotNeeded; }
    public void setWhyNotNeeded(String __whyNotNeeded) { this.whyNotNeeded = __whyNotNeeded; }

    public String getMessage() { return message; }
    public void setMessage(String __message) { this.message = __message; }

    public String getName() { return name; }
    public void setName(String __name) { this.name = __name; }

    public String getCompany() { return company; }
    public void setCompany(String __company) { this.company = __company; }

    public String getContact() { return contact; }
    public void setContact(String __contact) { this.contact = __contact; }
}
