package com.qilinfish.website.controller;

import com.qilinfish.website.model.ResourceLinkModel;
import com.qilinfish.website.vb.ResourcesViewBinder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class ResourcesController {

    @GetMapping("/resources")
    public String renderResources(Model __model) {
        ResourcesViewBinder vb = new ResourcesViewBinder();
        vb.setPageTitle("參考資源");
        vb.setPageSubtitle("RESOURCES // external references");
        vb.setLinks(List.of(
            new ResourceLinkModel(
                "r-01",
                "全國法規資料庫",
                "https://law.moj.gov.tw/",
                "法務部維運的台灣現行法規、判解、命令、行政函釋查詢入口。"
            )
        ));
        __model.addAttribute("vb", vb);
        __model.addAttribute("activeNav", "resources");
        return "resources";
    }
}
