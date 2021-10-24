package com.example.Spring_App.Service.lexemeService;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

@Service
public class LexAnalyzer {
    private List<Lexeme> lexemes;
    private Set<Character> setOfOperand;

    public List<Lexeme> lexAnalyze(String expText){
        setOfOperand = new TreeSet<>();
        lexemes = new ArrayList<>();
        int pos = 0;
        while(pos < expText.length()){
            char c = expText.charAt(pos);
            switch (c){
                case '(':
                    lexemes.add(new Lexeme(LexemeType.LEFT_BRACKET,c));
                    pos++;
                    continue;
                case ')':
                    lexemes.add(new Lexeme(LexemeType.RIGHT_BRACKET,c));
                    pos++;
                    continue;
                case '+':
                case 'v':
                case 'V':
                case '|':
                    lexemes.add(new Lexeme(LexemeType.OP_OR,c));
                    pos++;
                    continue;
                case '*':
                case '&':
                    lexemes.add(new Lexeme(LexemeType.OP_AND,c));
                    pos++;
                    continue;
                case '!':
                    lexemes.add(new Lexeme(LexemeType.OP_NOT,c));
                    pos++;
                    continue;
                case '@':
                    lexemes.add(new Lexeme(LexemeType.OP_IMP,c));
                    pos++;
                    continue;
                case '-':
                    int currPos = pos;
                    if(expText.charAt(++currPos)=='>'){
                        StringBuilder sb = new StringBuilder();
                        sb.append(c).append('>');
                        lexemes.add(new Lexeme(LexemeType.OP_IMP,sb.toString()));
                        pos+=2;
                        continue;
                    }
                    else return null;
                default:
                    //Таблица ASCII для больших и маленьких букв
                    if(c>=97&&c<=122||c>=65&&c<=90){
                        int currPos1 = pos;
                        if(++currPos1<expText.length()){
                            char c1 = expText.charAt(currPos1);
                            if(c1>=97&&c1<=122||c1>=65&&c1<=90){
                                return null;
                            }
                        }
                        lexemes.add(new Lexeme(LexemeType.OPERAND,c));
                        pos++;
                        setOfOperand.add(c);
                        break;
                    }
                    else{
                        if(c!=' '){
                            return null;
                        }
                        pos++;
                    }
            }
        }
        lexemes.add(new Lexeme(LexemeType.EOF,""));
        return lexemes;
    }
    public Lexeme getLexemeByValue(String value){
        for (Lexeme lexeme: lexemes) {
            if(lexeme.getValue().equals(value))return lexeme;
        }
        return null;
    }

    public boolean Expr(LexemeBuffer lexemeBuffer){
        Lexeme lexeme = lexemeBuffer.next();
        if(lexeme.getType()!=LexemeType.EOF){
            lexemeBuffer.back();
            return Impl(lexemeBuffer);
        }
        else return false;
    }
    public boolean Impl(LexemeBuffer lexemeBuffer){
        boolean value = Or(lexemeBuffer);
        while (true){
            Lexeme lexeme = lexemeBuffer.next();
            switch (lexeme.getType()){
                case OP_IMP:
                    boolean value1 = Or(lexemeBuffer);
                    value = !value || value1;
                    break;
                default:
                    lexemeBuffer.back();
                    return value;
            }
        }
    }
    public boolean Or(LexemeBuffer lexemeBuffer){
        boolean value = And(lexemeBuffer);
        while (true){
            Lexeme lexeme = lexemeBuffer.next();
            switch (lexeme.getType()){
                case OP_OR:
                    boolean value1 = And(lexemeBuffer);
                    value = value || value1;
                    break;
                default:
                    lexemeBuffer.back();
                    return value;
            }
        }
    }
    public boolean And(LexemeBuffer lexemeBuffer){
        boolean value = Operand(lexemeBuffer);
        while (true){
            Lexeme lexeme = lexemeBuffer.next();
            switch (lexeme.getType()){
                case OP_AND:
                    boolean value1=Operand(lexemeBuffer);
                    value = value && value1;
                    break;
                default:
                    lexemeBuffer.back();
                    return value;
            }
        }
    }

    public boolean Operand(LexemeBuffer lexemeBuffer){
        Lexeme lexeme = lexemeBuffer.next();
        boolean value;
        switch (lexeme.getType()){
            case OPERAND:
                return lexeme.getValue().equals("1")?true:false;
            case LEFT_BRACKET:
                value = Expr(lexemeBuffer);
                lexeme = lexemeBuffer.next();
                if(lexeme.getType() != LexemeType.RIGHT_BRACKET){
                    throw new RuntimeException("Don't close bracket: " + lexeme.getValue() +
                            "at position: " +lexemeBuffer.getPos());
                }
                return value;
            case OP_NOT:
                value = Operand(lexemeBuffer);
                return !value;

            default:
                throw new RuntimeException("Unexpected token: " + lexeme.getValue() +
                        "at position: " +lexemeBuffer.getPos());
        }
    }

    public List<Lexeme> getLexemes() {
        return lexemes;
    }

    public void setLexemes(List<Lexeme> lexemes) {
        this.lexemes = lexemes;
    }

    public Set<Character> getSetOfOperand() {
        return setOfOperand;
    }

    public void setSetOfOperand(Set<Character> setOfOperand) {
        this.setOfOperand = setOfOperand;
    }
}
