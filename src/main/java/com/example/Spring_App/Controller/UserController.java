package com.example.Spring_App.Controller;

import com.example.Spring_App.Service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Controller
public class UserController {

    @Autowired
    UserService userService;

    @GetMapping()
    public String main(Model model) {
        model.addAttribute("pkgList",userService.getRepo().getPackages());
        model.addAttribute("outList",userService.getRepo().getOuts());
        List<List<String>> variableValue = userService.getListOfRowsOfOperandAndVariableValues();
        Map<String,String> variableName = userService.getOperandsAndVariableNames();
        model.addAttribute("variableName",variableName);
        model.addAttribute("variableValue",variableValue);
        String result = userService.getResult();
        if(result!=null&&userService.getRepo().getOuts()!=null&&!userService.getRepo().getOuts().isEmpty()){
            model.addAttribute("result",result);
        }
        return "main";
    }
    @GetMapping("/get")
    public String main(
            @RequestParam String packge,
            @RequestParam String out,
            Model model){
        if(packge!=null&&!packge.isEmpty()) {
            userService.addPackage(packge);
        }
        if(out!=null&&!out.isEmpty()) {
            userService.addOut(out);
        }
        return "redirect:/";
    }

    @GetMapping("/delete")
    public String delete(HttpServletRequest request,Model model){
        String delOut = request.getParameter("delOut");
        String delPkg = request.getParameter("delPkg");
        String delete;
        if(delOut!=null){
            delete = delOut.split("Удалить ")[1];
            userService.delOut(delete);
            System.out.println(delete);
        }
        else if(delPkg!=null){
            delete = delPkg.split("Удалить ")[1];
            userService.delPackage(delete);
        }
        return "redirect:/";
    }
}
