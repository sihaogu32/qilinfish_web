package com.qilinfish.website.controller;

import com.qilinfish.website.vb.CasesViewBinder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class CasesController {

    @GetMapping("/cases")
    public String renderCases(Model __model) {
        CasesViewBinder vb = new CasesViewBinder();
        vb.setPageTitle("成功案例");
        vb.setPageSubtitle("CASES // selected works");
        vb.setEmpty(true);             // currently reserved
        vb.setCases(List.of());
        __model.addAttribute("vb", vb);
        __model.addAttribute("activeNav", "cases");
        return "cases";
    }
}
