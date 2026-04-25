package com.qilinfish.website.controller;

import com.qilinfish.website.vb.AboutViewBinder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AboutController {

    @GetMapping("/about")
    public String renderAbout(Model __model) {
        AboutViewBinder vb = new AboutViewBinder();
        vb.setPageTitle("關於我們");
        vb.setPageSubtitle("ABOUT // who we are");
        vb.setStudioName("麒麟魚 Qilinfish");
        vb.setTagline("美感是『普適模式』被『主觀經驗』解構後的殘餘。");
        vb.setCapabilities(new String[] {
            "AI Agent 開發 — LLM Pipeline、RAG、知識圖譜整合",
            "Web 應用開發 — 響應式網頁、後端 API、雲端部署",
            "自動化流程 — 跨系統串接、資料抓取、定時任務",
            "資料工程 — ETL、資料清洗、視覺化報表",
            "客製化專案 — 從需求訪談到上線維運的一條龍服務"
        });
        vb.setPhilosophy(new String[] {
            "工程不是堆疊技術，是把複雜性收斂到使用者看不見的地方。",
            "每一行程式碼都該為一個明確的決策服務。",
            "好的系統不只能跑，還能被下一個人讀懂、改動、擴展。"
        });
        __model.addAttribute("vb", vb);
        __model.addAttribute("activeNav", "about");
        return "about";
    }
}
