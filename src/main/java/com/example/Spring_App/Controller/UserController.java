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
        List<String> pkgs = userService.getRepo().getPackages();
        List<String> outs = userService.getRepo().getOuts();
        model.addAttribute("pkgList", pkgs);
        model.addAttribute("outList", outs);
        List<List<String>> variableValue;
        Map<String,String> variableName;
        try{
            variableName = userService.getOperandsAndVariableNames(pkgs, outs);
            variableValue = userService.getListOfRowsOfOperandAndVariableValues();
            model.addAttribute("variableName",variableName);
            model.addAttribute("variableValue",variableValue);
            model.addAttribute("posP",userService.getPosP(variableName));
            String result = userService.getResult(variableName, variableValue);
            if(result!=null&&userService.getRepo().getOuts()!=null&&!userService.getRepo().getOuts().isEmpty()){
                model.addAttribute("result",result);
                if(userService.getNotValidRows()!=null&&!userService.getNotValidRows().isEmpty()){
                    model.addAttribute("notValidRows",userService.getNotValidRows());
                }
            }
        }catch (RuntimeException e){
            return "main";
        }
        return "main";
    }

    @GetMapping("/example")
    public String example() {
        userService.delAllOut();
        userService.delAllPkg();
        List<String> pkgs = List.of("A->B", "B->C", "C->D", "E->!D");
        List<String> outs = List.of("A->!E", "A*E");
        userService.addPkgs(pkgs);
        userService.addOuts(outs);
        return "redirect:/";
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
        String delALLPkg = request.getParameter("del_pkgs");
        String delAllOut = request.getParameter("del_outs");
        String delete;
        if(delOut!=null){
            delete = delOut.split("Удалить")[1].trim();
            userService.delOut(delete);
        }
        else if(delPkg!=null){
            delete = delPkg.split("Удалить")[1].trim();
            userService.delPackage(delete);
        }
        else if(delALLPkg!=null){
            userService.delAllPkg();
        }
        else if(delAllOut!=null){
            userService.delAllOut();
        }
        return "redirect:/";
    }
}
