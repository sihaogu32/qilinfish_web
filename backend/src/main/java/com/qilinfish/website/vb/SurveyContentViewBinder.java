package com.qilinfish.website.vb;

import java.util.List;

/**
 * ViewBinder for the article-app survey (/survey/content) — the "VM" in MVVM.
 * Holds the option lists as (stored value, shown label) pairs; the stored value is the Chinese option text
 * on both language pages (SurveyContentFormModel), the label is in the page language.
 * Form state lives in SurveyContentFormModel; fixed copy comes from i18n/survey_content*.properties.
 */
public class SurveyContentViewBinder {

    /** One choice of a radio or checkbox question. */
    public static class Choice {
        private final String value;
        private final String label;

        public Choice(String __value, String __label) {
            this.value = __value;
            this.label = __label;
        }

        public String getValue() { return value; }
        public String getLabel() { return label; }
        /** "其他": answered in its own words, in a text box inside the choice. */
        public boolean isOther() { return "其他".equals(value); }
    }

    private List<Choice> roles;
    private List<Choice> teamSizes;
    private List<Choice> monthlyPieces;
    private List<Choice> afterDelivery;
    private String afterDeliveryFollowUpRule;   // data-show-if value for 7a, e.g. afterDelivery=有人問出處|有人指出錯誤
    private List<Choice> tools;
    private String noTools;                      // the "none" tool, which unticks the others
    private List<Choice> toolCounts;
    private List<Choice> formats;
    private List<Choice> blindOptions;
    private List<Choice> revealBuy;
    private List<Choice> revealRenew;
    private List<Choice> trial;

    public SurveyContentViewBinder() { }

    public List<Choice> getRoles() { return roles; }
    public void setRoles(List<Choice> __roles) { this.roles = __roles; }

    public List<Choice> getTeamSizes() { return teamSizes; }
    public void setTeamSizes(List<Choice> __teamSizes) { this.teamSizes = __teamSizes; }

    public List<Choice> getMonthlyPieces() { return monthlyPieces; }
    public void setMonthlyPieces(List<Choice> __monthlyPieces) { this.monthlyPieces = __monthlyPieces; }

    public List<Choice> getAfterDelivery() { return afterDelivery; }
    public void setAfterDelivery(List<Choice> __afterDelivery) { this.afterDelivery = __afterDelivery; }

    public String getAfterDeliveryFollowUpRule() { return afterDeliveryFollowUpRule; }
    public void setAfterDeliveryFollowUpRule(String __rule) { this.afterDeliveryFollowUpRule = __rule; }

    public List<Choice> getTools() { return tools; }
    public void setTools(List<Choice> __tools) { this.tools = __tools; }

    public String getNoTools() { return noTools; }
    public void setNoTools(String __noTools) { this.noTools = __noTools; }

    public List<Choice> getToolCounts() { return toolCounts; }
    public void setToolCounts(List<Choice> __toolCounts) { this.toolCounts = __toolCounts; }

    public List<Choice> getFormats() { return formats; }
    public void setFormats(List<Choice> __formats) { this.formats = __formats; }

    public List<Choice> getBlindOptions() { return blindOptions; }
    public void setBlindOptions(List<Choice> __blindOptions) { this.blindOptions = __blindOptions; }

    public List<Choice> getRevealBuy() { return revealBuy; }
    public void setRevealBuy(List<Choice> __revealBuy) { this.revealBuy = __revealBuy; }

    public List<Choice> getRevealRenew() { return revealRenew; }
    public void setRevealRenew(List<Choice> __revealRenew) { this.revealRenew = __revealRenew; }

    public List<Choice> getTrial() { return trial; }
    public void setTrial(List<Choice> __trial) { this.trial = __trial; }
}
