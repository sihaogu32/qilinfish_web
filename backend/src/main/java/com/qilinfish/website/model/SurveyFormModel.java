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
    @Size(max = 32)
    private String extraSpend;
    @Size(max = 2000)
    private String worstCase;

    // ---- what you tried ----
    @Size(max = 2000)
    private String triedOther;
    @Size(max = 2000)
    private String hardestStep;

    // ---- situation = none ----
    @Size(max = 2000)
    private String whyNotNeeded;

    // ---- follow-up ----
    @Size(max = 16)
    private String interview;            // yes / no
    @Size(max = 128)
    private String availableTime;
    @Size(max = 2000)
    private String message;              // anything else the respondent wants to tell us

    // ---- contact ----
    @NotBlank @Size(max = 64)
    private String name;
    @NotBlank @Size(max = 128)
    private String company;
    @NotBlank @Size(max = 128)
    private String contact;              // email or phone

    public SurveyFormModel() { }

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

    public String getWorstCase() { return worstCase; }
    public void setWorstCase(String __worstCase) { this.worstCase = __worstCase; }

    public String getTriedOther() { return triedOther; }
    public void setTriedOther(String __triedOther) { this.triedOther = __triedOther; }

    public String getHardestStep() { return hardestStep; }
    public void setHardestStep(String __hardestStep) { this.hardestStep = __hardestStep; }

    public String getWhyNotNeeded() { return whyNotNeeded; }
    public void setWhyNotNeeded(String __whyNotNeeded) { this.whyNotNeeded = __whyNotNeeded; }

    public String getInterview() { return interview; }
    public void setInterview(String __interview) { this.interview = __interview; }

    public String getAvailableTime() { return availableTime; }
    public void setAvailableTime(String __availableTime) { this.availableTime = __availableTime; }

    public String getMessage() { return message; }
    public void setMessage(String __message) { this.message = __message; }

    public String getName() { return name; }
    public void setName(String __name) { this.name = __name; }

    public String getCompany() { return company; }
    public void setCompany(String __company) { this.company = __company; }

    public String getContact() { return contact; }
    public void setContact(String __contact) { this.contact = __contact; }
}
