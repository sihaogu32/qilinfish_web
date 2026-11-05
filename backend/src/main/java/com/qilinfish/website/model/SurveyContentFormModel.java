package com.qilinfish.website.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.ArrayList;
import java.util.List;

/**
 * Form-binding Model for the article-app survey (/survey/content, spec appendix A).
 * Choice answers are the Chinese option text in both languages (the page only changes what is shown),
 * so the stored rows read the same whichever page they came from.
 * Annotations cover the always-required fields and length caps; rules that depend on another answer
 * (other-text, hours, the blind price, the contact format, allowed options) live in SurveyContentController.
 */
public class SurveyContentFormModel {

    // ---- option lists: the stored value of each choice, in page order (appendix A) ----
    public static final String OTHER = "其他";
    public static final List<String> ROLES = List.of(
        "行銷／內容工作室", "自由接案的內容或行銷工作者", "研究型創作者（部落格、電子報、YouTube…）", OTHER);
    public static final List<String> TEAM_SIZES = List.of("1 人", "2–5", "6–20", "20 人以上");
    public static final List<String> MONTHLY_PIECES = List.of("1–3", "4–10", "11–30", "30 以上");
    public static final List<String> AFTER_DELIVERY = List.of("沒人問", "有人問出處", "有人指出錯誤", "不知道");
    /** 7a is asked only after one of these. */
    public static final List<String> AFTER_DELIVERY_FOLLOW_UP = List.of("有人問出處", "有人指出錯誤");
    public static final String NO_TOOLS = "都沒用";
    public static final List<String> TOOLS = List.of(
        "ChatGPT", "Claude", "Gemini", "Perplexity", "Jasper／Koala 等 AI 寫作工具", "ComfyUI／Midjourney 等生圖工具", NO_TOOLS, OTHER);
    public static final List<String> TOOL_COUNTS = List.of("1–2", "3–4", "5 個以上");
    public static final List<String> FORMATS = List.of(
        "部落格文章／網站頁面", "WordPress 等網站後台的文章", "電子報", "社群貼文（圖文、輪播）", "短影音、影片腳本",
        "簡報", "研究報告、白皮書（PDF）", "產品介紹、型錄", OTHER);
    public static final List<String> BLIND_OPTIONS = List.of("不會付費", "不知道");
    public static final List<String> REVEAL_BUY = List.of("會買", "會考慮", "太貴", "不需要");
    public static final List<String> REVEAL_RENEW = List.of("會", "看當時的更新內容", "不會");
    public static final List<String> TRIAL = List.of("願意", "不願意");

    // ---- 01 about you ----
    @NotBlank @Size(max = 64)
    private String role;
    @Size(max = 200)
    private String roleOther;
    @NotBlank @Size(max = 16)
    private String teamSize;
    @NotBlank @Size(max = 16)
    private String monthlyPieces;

    // ---- 02 the last time ----
    @Size(max = 2000)
    private String lastPiece;
    @Size(max = 2000)
    private String howChecked;
    private String checkHours;           // hours per piece; a number, checked in the controller
    @Size(max = 16)
    private String afterDelivery;
    @Size(max = 2000)
    private String afterDeliveryCost;    // 7a, only kept after AFTER_DELIVERY_FOLLOW_UP

    // ---- 03 how you work now ----
    private List<String> tools = new ArrayList<>();
    @Size(max = 200)
    private String toolsOther;
    @Size(max = 16)
    private String toolCount;
    private String moveHours;            // hours per piece; a number, checked in the controller
    private List<String> formats = new ArrayList<>();
    @Size(max = 200)
    private String formatsOther;

    // ---- 04 pricing: one amount, or an option (BLIND_OPTIONS); then the two reveal questions ----
    private String blindUsd;
    private String blindOption;
    @NotBlank @Size(max = 16)
    private String revealBuy;
    @NotBlank @Size(max = 32)
    private String revealRenew;
    /** "1" when the browser showed the reveal questions only after "Next" (CP4); anything else counts as 0. */
    private String blindLocked;

    // ---- 05 contact ----
    @NotBlank @Size(max = 64)
    private String name;
    @NotBlank @Size(max = 128)
    private String contact;              // email or phone, see SurveyFormModel.isValidContact
    @Size(max = 8)
    private String trial;

    public SurveyContentFormModel() { }

    public String getRole() { return role; }
    public void setRole(String __role) { this.role = __role; }

    public String getRoleOther() { return roleOther; }
    public void setRoleOther(String __roleOther) { this.roleOther = __roleOther; }

    public String getTeamSize() { return teamSize; }
    public void setTeamSize(String __teamSize) { this.teamSize = __teamSize; }

    public String getMonthlyPieces() { return monthlyPieces; }
    public void setMonthlyPieces(String __monthlyPieces) { this.monthlyPieces = __monthlyPieces; }

    public String getLastPiece() { return lastPiece; }
    public void setLastPiece(String __lastPiece) { this.lastPiece = __lastPiece; }

    public String getHowChecked() { return howChecked; }
    public void setHowChecked(String __howChecked) { this.howChecked = __howChecked; }

    public String getCheckHours() { return checkHours; }
    public void setCheckHours(String __checkHours) { this.checkHours = __checkHours; }

    public String getAfterDelivery() { return afterDelivery; }
    public void setAfterDelivery(String __afterDelivery) { this.afterDelivery = __afterDelivery; }

    public String getAfterDeliveryCost() { return afterDeliveryCost; }
    public void setAfterDeliveryCost(String __afterDeliveryCost) { this.afterDeliveryCost = __afterDeliveryCost; }

    public List<String> getTools() { return tools; }
    public void setTools(List<String> __tools) { this.tools = __tools == null ? new ArrayList<>() : __tools; }

    public String getToolsOther() { return toolsOther; }
    public void setToolsOther(String __toolsOther) { this.toolsOther = __toolsOther; }

    public String getToolCount() { return toolCount; }
    public void setToolCount(String __toolCount) { this.toolCount = __toolCount; }

    public String getMoveHours() { return moveHours; }
    public void setMoveHours(String __moveHours) { this.moveHours = __moveHours; }

    public List<String> getFormats() { return formats; }
    public void setFormats(List<String> __formats) { this.formats = __formats == null ? new ArrayList<>() : __formats; }

    public String getFormatsOther() { return formatsOther; }
    public void setFormatsOther(String __formatsOther) { this.formatsOther = __formatsOther; }

    public String getBlindUsd() { return blindUsd; }
    public void setBlindUsd(String __blindUsd) { this.blindUsd = __blindUsd; }

    public String getBlindOption() { return blindOption; }
    public void setBlindOption(String __blindOption) { this.blindOption = __blindOption; }

    public String getRevealBuy() { return revealBuy; }
    public void setRevealBuy(String __revealBuy) { this.revealBuy = __revealBuy; }

    public String getRevealRenew() { return revealRenew; }
    public void setRevealRenew(String __revealRenew) { this.revealRenew = __revealRenew; }

    public String getBlindLocked() { return blindLocked; }
    public void setBlindLocked(String __blindLocked) { this.blindLocked = __blindLocked; }

    public String getName() { return name; }
    public void setName(String __name) { this.name = __name; }

    public String getContact() { return contact; }
    public void setContact(String __contact) { this.contact = __contact; }

    public String getTrial() { return trial; }
    public void setTrial(String __trial) { this.trial = __trial; }
}
