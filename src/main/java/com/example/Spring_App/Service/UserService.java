package com.example.Spring_App.Service;

import com.example.Spring_App.Repo.Repo;
import com.example.Spring_App.Service.lexemeService.LexAnalyzer;
import com.example.Spring_App.Service.lexemeService.Lexeme;
import com.example.Spring_App.Service.lexemeService.LexemeBuffer;
import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.*;

@Service
public class UserService {

    private final LexAnalyzer lexAnalyzer;
    private ExecutorService executorService;

    private final Repo repo;
    private final int processors = Runtime.getRuntime().availableProcessors();

    List<Integer> notValidRows;

    @Autowired
    public UserService(LexAnalyzer lexAnalyzer, @Qualifier("repo") Repo repo) {
        this.lexAnalyzer = lexAnalyzer;
        this.repo = repo;
    }

    public boolean addPackage(String pkg) {
        if (lexAnalyzer.lexAnalyze(pkg) != null) {
            StringBuilder new_pkg = new StringBuilder();
            for (Lexeme lexeme : lexAnalyzer.getLexemes()) {
                new_pkg.append(lexeme.getValue());
            }
            return repo.addPkg(new_pkg.toString());
        } else return false;
    }

    public boolean addOut(String out) {
        if (lexAnalyzer.lexAnalyze(out) != null) {
            StringBuilder new_out = new StringBuilder();
            for (Lexeme lexeme : lexAnalyzer.getLexemes()) {
                new_out.append(lexeme.getValue());
            }
            return repo.addOut(new_out.toString());
        } else return false;
    }

    public void addOuts(List<String> outs) {
        for (String out : outs) {
            if (lexAnalyzer.lexAnalyze(out) != null) {
                StringBuilder new_out = new StringBuilder();
                for (Lexeme lexeme : lexAnalyzer.getLexemes()) {
                    new_out.append(lexeme.getValue());
                }
                repo.addOut(new_out.toString());
            }
        }
    }

    public void addPkgs(List<String> pkgs) {
        for (String pkg : pkgs) {
            if (lexAnalyzer.lexAnalyze(pkg) != null) {
                StringBuilder new_pkg = new StringBuilder();
                for (Lexeme lexeme : lexAnalyzer.getLexemes()) {
                    new_pkg.append(lexeme.getValue());
                }
                repo.addPkg(new_pkg.toString());
            }
        }
    }

    public boolean delPackage(String pkg) {
        return repo.delPkg(pkg);
    }

    public boolean delOut(String out) {
        return repo.delOut(out);
    }

    public boolean delAllPkg() {
        return repo.dellAllPkg();
    }

    public boolean delAllOut() {
        return repo.dellAllOut();
    }

    public Map<String, String> getOperandsName(List<String> pkgs, List<String> outs) {
        StringBuilder sb = new StringBuilder();
        Map<String, String> operandMap = new TreeMap<>();
        for (String pkg : pkgs) {
            sb.append(pkg).append(" ");
        }
        for (String out : outs) {
            sb.append(out).append(" ");
        }
        if (!sb.toString().isEmpty()) {
            lexAnalyzer.lexAnalyze(sb.toString());
            List<Character> operandList = new ArrayList<>(lexAnalyzer.getSetOfOperand());
            for (Character op : operandList) {
                operandMap.put(String.valueOf(op), "Operand");
            }
            return operandMap;
        } else return null;
    }

    public Map<String, String> getOperandsAndVariableNames(List<String> pkgs, List<String> outs) {
        Map<String, String> operandMap = getOperandsName(pkgs, outs);
        Map<String, String> OperandsAndVariableNames;
        if (operandMap != null) {
            OperandsAndVariableNames = new LinkedHashMap<>(operandMap);
        } else return null;
        if (pkgs != null) {
            int i = 1;
            for (String pkg : pkgs) {
                OperandsAndVariableNames.put("P" + i, pkg);
                i++;
            }
            OperandsAndVariableNames.put("<P>", "ConP");
        }
        if (outs != null) {
            int i = 1;
            for (String out : outs) {
                OperandsAndVariableNames.put("C" + i, out);
                i++;
            }
        }
        return OperandsAndVariableNames;
    }
    private class LexemeInfo {
        public List<Lexeme> lexemes;
        public Set<Character> operands;

        public LexemeInfo(List<Lexeme> lexemes, Set<Character> operands) {
            this.lexemes = lexemes;
            this.operands = operands;
        }
    }
    public List<List<String>> getListOfRowsOfOperandAndVariableValues() {
        this.executorService = Executors.newFixedThreadPool(processors);

        List<LexemeInfo> pkgsLexeme = new ArrayList<>();
        List<LexemeInfo> outsLexeme = new ArrayList<>();
        List<String> pkgs = repo.getPackages();
        List<String> outs = repo.getOuts();

        Map<String, String> operands = getOperandsName(pkgs, outs);
        if (operands == null) {
            return null;
        }
        for (String pkg : pkgs) {
            pkgsLexeme.add(new LexemeInfo(lexAnalyzer.lexAnalyze(pkg), lexAnalyzer.getSetOfOperand()));
        }

        for (String out : outs) {
            outsLexeme.add(new LexemeInfo(lexAnalyzer.lexAnalyze(out), lexAnalyzer.getSetOfOperand()));
        }

        List<List<String>> tableRowsList = getListOfRowsOfOperandValues(operands.size());
        List<List<String>> tableRowsValueList = Collections.synchronizedList(new ArrayList<>(tableRowsList.size()));

        List<List<List<String>>> partitions = Lists.partition(tableRowsList, processors);

        List<Future<List<List<String>>>> futures = new ArrayList<>();

        for (List<List<String>> partition : partitions) {
            Future<List<List<String>>> future = executorService.submit(() -> {
                List<List<String>> resultPartition = new ArrayList<>();
                for (List<String> opValueList : partition) {
                    List<String> row = new ArrayList<>(opValueList);
                    List<Boolean> boolPkgs = new ArrayList<>();
                    for (LexemeInfo lexemes : pkgsLexeme) {
                        String result = getPkgOrOutResult(opValueList, lexemes, operands);
                        boolPkgs.add(Objects.equals(result, "1"));
                        row.add(result);
                    }
                    String conP = getСonjunctionP(boolPkgs) ? "1" : "0";
                    row.add(conP);
                    for (LexemeInfo lexemes : outsLexeme) {
                        row.add(getPkgOrOutResult(opValueList, lexemes, operands));
                    }
                    resultPartition.add(row);
                }
                return resultPartition;
            });
            futures.add(future);
        }

        for (Future<List<List<String>>> future : futures) {
            try {
                List<List<String>> resultPartition = future.get();
                tableRowsValueList.addAll(resultPartition);
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }

        executorService.shutdown();
        return tableRowsValueList;
    }



    public List<List<String>> getListOfRowsOfOperandValues(int operandsSize) {
        List<List<String>> tableRowsList = new ArrayList<>();
        int rows = (int) Math.pow(2, operandsSize);
        for (int i = 0; i < rows; i++) {
            List<String> row = new ArrayList<>();
            for (int j = operandsSize - 1; j >= 0; j--) {
                //Заполняем список
                int boolValue = (i / (int) Math.pow(2, j)) % 2;
                row.add(String.valueOf(boolValue));
            }
            tableRowsList.add(row);
        }
        return tableRowsList;
    }

    public String getPkgOrOutResult(List<String> valueList, LexemeInfo lexemeInfo, Map<String, String> operandMap) {
        //Создаем буфер для лексем
        //Создаем множество(уникальное) операндов
        Set<Character> characterSet = lexemeInfo.operands;
        //Помещаем множество в список
        List<String> list = new ArrayList<>();
        for (Character op : characterSet) {
            list.add(String.valueOf(op));
        }
        List<Lexeme> lexemeValues = new ArrayList<>();
        for (Lexeme lexeme : lexemeInfo.lexemes) {
            lexemeValues.add(lexeme.clone());
        }
        //Проходимся по списку всех операндов
        for (String operand : operandMap.keySet()) {
            //Если в списке операндов нашего выражения, содержится данный операнд
            if (list.contains(operand)) {
                List<String> OperandList = new ArrayList<>(operandMap.keySet());
                List<Lexeme> matchingLexemes = getLexemesByValue(operand, lexemeValues);
                for (Lexeme lexeme : matchingLexemes) {
                    lexeme.setValue(valueList.get(OperandList.indexOf(operand)));
                }
            }
        }
        LexemeBuffer lexemeBuffer = new LexemeBuffer(lexemeValues);
        boolean pkgBool = lexAnalyzer.Expr(lexemeBuffer);
        return pkgBool ? "1" : "0";
    }

    private Lexeme getLexemeByValue(String value, List<Lexeme> lexemes){
        for (Lexeme lexeme: lexemes) {
            if(lexeme.getValue().equals(value)) {
                return lexeme;
            }
        }
        return null;
    }

    private List<Lexeme> getLexemesByValue(String value, List<Lexeme> lexemes) {
        List<Lexeme> matchingLexemes = new ArrayList<>();
        for (Lexeme lexeme : lexemes) {
            if (lexeme.getValue().equals(value)) {
                matchingLexemes.add(lexeme);
            }
        }
        return matchingLexemes;
    }

    public boolean getСonjunctionP(List<Boolean> pkgs) {
        boolean pkges = true;
        for (Boolean b : pkgs) {
            pkges = pkges && b;
        }
        return pkges;
    }

    public boolean isValid(boolean conP, boolean out) {
        if (conP & !out) {
            return false;
        } else return true;
    }

    public String getResult(Map<String, String> OperandsAndVariableNames, List<List<String>> rowsOfOperands) {
        Map<String, String> notValidOuts = new LinkedHashMap<>();
        List<String> outs = repo.getOuts();
        notValidRows = new ArrayList<>();
        List<String> OperandAndVariableNameList = new ArrayList<>(OperandsAndVariableNames.keySet());
        int i = 1;
        for (String out : outs) {
            for (List<String> row : rowsOfOperands) {
                int numberColumnP = OperandAndVariableNameList.indexOf("<P>");
                String pValue = row.get(numberColumnP);
                int numberColumnOut = OperandAndVariableNameList.indexOf("C" + i);
                String outValue = row.get(numberColumnOut);
                if (!isValid(pValue.equals("1") ? true : false, outValue.equals("1") ? true : false)) {
                    notValidRows.add(rowsOfOperands.indexOf(row));
                    notValidOuts.put("C" + i, out);
                }
            }
            i++;
        }
        StringBuilder result = new StringBuilder();
        if (notValidOuts.isEmpty()) {
            return "Все выводы валидные";
        } else {
            int num = notValidOuts.size();
            result.append(num > 1 ? "Выводы " : "Вывод ");
            int n = 0;
            for (Map.Entry<String, String> pair : notValidOuts.entrySet()) {
                n++;
                result.append("<font color=\"#F0823D\">" + pair.getKey() + "</font>").append("= ").append(pair.getValue());
                if (n < num) {
                    result.append(", ");
                } else result.append(" ");
            }
            result.append(num > 1 ? "не валидные" : "не валидный");
        }
        return result.toString();
    }

    public int getPosP(Map<String, String> OperandsAndVariableNames) {
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
