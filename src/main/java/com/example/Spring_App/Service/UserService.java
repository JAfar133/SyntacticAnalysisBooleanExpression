package com.example.Spring_App.Service;

import com.example.Spring_App.Repo.Repo;
import com.example.Spring_App.Service.lexemeService.LexAnalyzer;
import com.example.Spring_App.Service.lexemeService.Lexeme;
import com.example.Spring_App.Service.lexemeService.LexemeBuffer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class UserService {

    Repo repo = new Repo();
    @Autowired
    LexAnalyzer lexAnalyzer;
    List<Integer> notValidRows;

    public boolean addPackage(String pkg){
        if(lexAnalyzer.lexAnalyze(pkg)!=null) {
            StringBuilder new_pkg = new StringBuilder();
            for (Lexeme lexeme : lexAnalyzer.getLexemes()) {
                new_pkg.append(lexeme.getValue());
            }
            return repo.addPkg(new_pkg.toString());
        }
        else return false;
    }
    public boolean addOut(String out){
        if(lexAnalyzer.lexAnalyze(out)!=null) {
            StringBuilder new_out = new StringBuilder();
            for (Lexeme lexeme : lexAnalyzer.getLexemes()) {
                new_out.append(lexeme.getValue());
            }
            return repo.addOut(new_out.toString());
        }
        else return false;
    }
    public boolean delPackage(String pkg){
        return repo.delPkg(pkg);
    }
    public boolean delOut(String out){
        return repo.delOut(out);
    }

    public boolean delAllPkg(){
        return repo.dellAllPkg();
    }
    public boolean delAllOut(){
        return repo.dellAllOut();
    }

    public Map<String,String> getOperandsName(){
        StringBuilder sb = new StringBuilder();
        Map<String,String> operandMap = new TreeMap<>();
        for (String pkg: repo.getPackages()) {
            sb.append(pkg+" ");
        }
        for (String out: repo.getOuts()) {
            sb.append(out+" ");
        }
        if(!sb.toString().isEmpty()) {
            lexAnalyzer.lexAnalyze(sb.toString());
            List<Character> operandList = new ArrayList<>(lexAnalyzer.getSetOfOperand());
            for (Character op:operandList) {
                operandMap.put(String.valueOf(op),"Operand");
            }
            return operandMap;
        }
        else return null;
    }
    public Map<String,String> getOperandsAndVariableNames(){
        Map<String,String> operandMap = getOperandsName();
        Map<String,String> OperandsAndVariableNames = new LinkedHashMap<>();
        if(operandMap!=null) {
            OperandsAndVariableNames.putAll(operandMap);
        }
        else return null;
        if(repo.getPackages()!=null){
            int i = 1;
            for(String pkg: repo.getPackages()){
                OperandsAndVariableNames.put("P"+i,pkg);
                        i++;
            }
            OperandsAndVariableNames.put("<P>","ConP");
        }
        if(repo.getOuts()!=null){
            int i = 1;
            for(String out: repo.getOuts()){
                OperandsAndVariableNames.put("C"+i,out);
                i++;
            }
        }
        return OperandsAndVariableNames;
    }
    public List<List<String>> getListOfRowsOfOperandAndVariableValues(){
        List<List<String>> tableRowsList = getListOfRowsOfOperandValues();
        List<List<String>> tableRowsValueList;
        List<Boolean> boolPkgs = new ArrayList<>();
        if(tableRowsList!=null) {
            tableRowsValueList = new ArrayList<>(tableRowsList);
        }
        else return null;
        for (List<String> opValueList : tableRowsList) {
            for (String pkg : repo.getPackages()) {
                tableRowsValueList.get(tableRowsList.indexOf(opValueList))
                        .add(getPkgOrOutResult(opValueList, pkg));
                boolPkgs.add(getPkgOrOutResult(opValueList, pkg)=="1"?true:false);
            }
            String conP = get??onjunctionP(boolPkgs)?"1":"0";
            tableRowsValueList.get(tableRowsList.indexOf(opValueList)).
                    add(conP);
            boolPkgs=new ArrayList<>();
            for (String out : repo.getOuts()) {
                tableRowsValueList.get(tableRowsList.indexOf(opValueList))
                        .add(getPkgOrOutResult(opValueList, out));
            }
        }
        return tableRowsList;

    }

    public List<List<String>> getListOfRowsOfOperandValues(){
        int n;
        if(getOperandsName()!=null) {
            n = getOperandsName().size();
        }
        else return null;
        List<List<String>> tableRowsList = new ArrayList<>();
        int rows = (int) Math.pow(2,n);
        for (int i=0; i<rows; i++) {
            List<String> row = new ArrayList<>();
            for (int j=n-1; j>=0; j--) {
                //?????????????????? ????????????
                int boolValue = (i/(int) Math.pow(2, j))%2;
                row.add(String.valueOf(boolValue));
            }
            tableRowsList.add(row);
        }
        return tableRowsList;
    }
    public String getPkgOrOutResult(List<String> valueList,String pkg){
        Map<String,String> operandMap = getOperandsName();
        //???????????? ?????????????????? ?? ???????????? ????????????(?????????????????? ?? ????????????????)
        List<Lexeme> lexemes = lexAnalyzer.lexAnalyze(pkg);
        //?????????????? ?????????? ?????? ????????????
        LexemeBuffer lexemeBuffer = new LexemeBuffer(lexemes);
        //?????????????? ??????????????????(????????????????????) ??????????????????
        Set<Character> characterSet = lexAnalyzer.getSetOfOperand();
        //???????????????? ?????????????????? ?? ????????????
        List<String> list = new ArrayList<>();
        for (Character op:characterSet) {
            list.add(String.valueOf(op));
        }
        //???????????????????? ???? ???????????? ???????? ??????????????????
        for (String operand: operandMap.keySet()) {
            //???????? ?? ???????????? ?????????????????? ???????????? ??????????????????, ???????????????????? ???????????? ??????????????
            if(list.contains(operand)){
                //?????????????? ??????????????
                //???????????????????? ????????????????, ???????????? ?????????????????? ???????????????????? ???????????? ????????????????
                while(lexAnalyzer.getLexemeByValue(operand)!=null)
                {
                    //???????????????? ?????????????? ?? ??????????-???? ??????????????????
                    Lexeme lexeme = lexAnalyzer.getLexemeByValue(operand);
                    //?????????????????????????? ???????????????? ???????????????? 0 ?????? 1
                    List<String> OperandList = new ArrayList<>(operandMap.keySet());
                    int a = OperandList.indexOf(operand);
                    lexeme.setValue(valueList.get(a));
                }
            }
        }
        boolean pkgBool = lexAnalyzer.Expr(lexemeBuffer);
        return pkgBool?"1":"0";
    }
    public boolean get??onjunctionP(List<Boolean>pkgs){
        boolean pkges=true;
        for (Boolean b:pkgs) {
            pkges = pkges&&b;
        }
        return pkges;
    }
    public boolean isValid(boolean conP, boolean out){
        if(conP&!out){
            return false;
        }
        else return true;
    }
    public String getResult(){
        List<List<String>> rowsOfOperands = getListOfRowsOfOperandAndVariableValues();
        Map<String,String> OperandsAndVariableNames;
        if(rowsOfOperands!=null){
            OperandsAndVariableNames = getOperandsAndVariableNames();
        }
        else return null;
        Map<String,String> notValidOuts = new LinkedHashMap<>();
        List<String> outs = repo.getOuts();
        notValidRows = new ArrayList<>();
        List<String> OperandAndVariableNameList = new ArrayList<>(OperandsAndVariableNames.keySet());
        int i = 1;
        for(String out: outs) {
            for (List<String> row : rowsOfOperands) {
                int numberColumnP = OperandAndVariableNameList.indexOf("<P>");
                String pValue = row.get(numberColumnP);
                int numberColumnOut = OperandAndVariableNameList.indexOf("C"+i);
                String outValue = row.get(numberColumnOut);
                if(!isValid(pValue.equals("1")?true:false,outValue.equals("1")?true:false))
                {
                    notValidRows.add(rowsOfOperands.indexOf(row));
                    notValidOuts.put("C"+i,out);
                }
            }
            i++;
        }
        StringBuilder result = new StringBuilder();
        if(notValidOuts.isEmpty()){
            return "?????? ???????????? ????????????????";
        }
        else {
            int num = notValidOuts.size();
            result.append(num>1?"????????????: ":"??????????: ");
            int n = 0;
            for (Map.Entry<String,String> pair: notValidOuts.entrySet()){
                n++;
                result.append(pair.getKey()).append("= ").append(pair.getValue());
                if(n<num){
                    result.append(", ");
                }
                else result.append(" ");
            }
            result.append(num>1?"???? ????????????????":"???? ????????????????");
        }
        return result.toString();
    }
    public int getPosP(){
        Map<String,String> OperandsAndVariableNames = getOperandsAndVariableNames();
        List<String> keyList = new ArrayList<>(OperandsAndVariableNames.keySet());
        return keyList.indexOf("<P>");
    }

    public List<Integer> getNotValidRows() {
        return notValidRows;
    }


    public Repo getRepo() {
        return repo;
    }
}
