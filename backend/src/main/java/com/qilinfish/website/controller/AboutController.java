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
        vb.setStory(new String[] {
            "我們遇過系統遷移的顧問案，也接手過規模很大、交接卻不齊全的舊系統。傳統做法是靠人一項一項讀、一項一項對，曠日費時，對人員的經驗和系統的條件要求都很高。",
            "現在，很多這類工作都期待導入 AI，或者被要求導入 AI。可是傳統做法有嚴謹的流程、有專業的人，仍然會出錯；換成一個本身就不穩定的 AI 工具，風險只會更大。我們自己每天用 AI 代理寫程式，也常常遇到它回報「做完了」，打開一看，有一部分沒做，或者做的跟它說的不一樣。",
            "同時，要求監管 AI 的聲音越來越多。主管、客戶、稽核都會問：「這是怎麼來的？」",
            "所以我們在做一個工具，讓 AI 的結果可以被檢查，人要監督也比較省力，出了事也查得到根據。",
            "我們還不知道其他行業怎麼處理這件事。律師、顧問、客服遇到的情況，可能跟我們很不一樣。這就是我們做問卷的原因。"
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
