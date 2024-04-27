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
                case ' ':
                    pos++;
                    continue;
                case '(':
                    lexemes.add(new Lexeme(LexemeType.LEFT_BRACKET,c));
                    pos++;
                    continue;
                case ')':
                    lexemes.add(new Lexeme(LexemeType.RIGHT_BRACKET,c));
                    pos++;
                    continue;
                case '+':
                case '⋁':
                    lexemes.add(new Lexeme(LexemeType.OP_OR,'⋁'));
                    pos++;
                    continue;
                case '*':
                case '&':
                case '∧':
                    lexemes.add(new Lexeme(LexemeType.OP_AND,'∧'));
                    pos++;
                    continue;
                case '!':
                case '¬':
                    lexemes.add(new Lexeme(LexemeType.OP_NOT,'¬'));
                    pos++;
                    continue;
                case '|':
                    lexemes.add(new Lexeme(LexemeType.OP_SCHAEFFER,c));
                    pos++;
                    continue;
                case '#':
                case '↓':
                    lexemes.add(new Lexeme(LexemeType.OP_PIERS,'↓'));
                    pos++;
                    continue;
                case '^':
                case '⊕':
                    lexemes.add(new Lexeme(LexemeType.OP_XOR,'⊕'));
                    pos++;
                    continue;
                case '<':
                case '%':
                case '←':
                    int currPos = pos;
                    if(c=='←'||c=='%'){
                        lexemes.add(new Lexeme(LexemeType.OP_REVERSE_IMP,'←'));
                        pos++;
                        continue;
                    }
                    else if(expText.charAt(++currPos)=='-'){
                        lexemes.add(new Lexeme(LexemeType.OP_REVERSE_IMP,'←'));
                        pos+=2;
                        continue;
                    }
                    else return null;

                case '=':
                case '≡':
                    lexemes.add(new Lexeme(LexemeType.OP_EQUAL,'≡'));
                    pos++;
                    continue;
                case '@':
                case '-':
                case '→':
                    int currPos1 = pos;
                    if(c=='→'||c=='@'){
                        lexemes.add(new Lexeme(LexemeType.OP_IMP,'→'));
                        pos++;
                        continue;
                    }
                    else if(expText.charAt(++currPos1)=='>'){
                        lexemes.add(new Lexeme(LexemeType.OP_IMP,'→'));
                        pos+=2;
                        continue;
                    }
                    else return null;
                default:
                    //Таблица ASCII для больших и маленьких букв
                    if(c>=97&&c<=122||c>=65&&c<=90){
                        int currPos2 = pos;
                        if(++currPos2<expText.length()){
                            char c1 = expText.charAt(currPos2);
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
            if(lexeme.getValue().equals(value)) {
                return lexeme;
            }
        }
        return null;
    }

    public boolean Expr(LexemeBuffer lexemeBuffer){
        Lexeme lexeme = lexemeBuffer.next();
        if(lexeme.getType()!=LexemeType.EOF){
            lexemeBuffer.back();
            return Equal(lexemeBuffer);
        }
        else return false;
    }
    public boolean Equal(LexemeBuffer lexemeBuffer){
        boolean value = Imp(lexemeBuffer);
        while (true){
            Lexeme lexeme = lexemeBuffer.next();
            switch (lexeme.getType()){
                case OP_EQUAL:
                    boolean value1 = Imp(lexemeBuffer);
                    value = value == value1;
                    break;
                default:
                    lexemeBuffer.back();
                    return value;
            }
        }
    }
    public boolean Imp(LexemeBuffer lexemeBuffer){
        boolean value = Or(lexemeBuffer);
        while (true){
            Lexeme lexeme = lexemeBuffer.next();
            switch (lexeme.getType()){
                case OP_IMP:
                    boolean value1 = Or(lexemeBuffer);
                    value = !value || value1;
                    break;
                case OP_REVERSE_IMP:
                    boolean value2 = Or(lexemeBuffer);
                    value = value || !value2;
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
                case OP_XOR:
                    boolean value2 = And(lexemeBuffer);
                    value = (!value||!value2)&&(value||value2);
                    break;
                default:
                    lexemeBuffer.back();
                    return value;
            }
        }
    }
    public boolean And(LexemeBuffer lexemeBuffer){
        boolean value = Piers(lexemeBuffer);
        while (true){
            Lexeme lexeme = lexemeBuffer.next();
            switch (lexeme.getType()){
                case OP_AND:
                    boolean value1=Piers(lexemeBuffer);
                    value = value && value1;
                    break;
                default:
                    lexemeBuffer.back();
                    return value;
            }
        }
    }
    public boolean Piers(LexemeBuffer lexemeBuffer){
        boolean value = Schaeffer(lexemeBuffer);
        while (true){
            Lexeme lexeme = lexemeBuffer.next();
            switch (lexeme.getType()){
                case OP_PIERS:
                    boolean value1=Schaeffer(lexemeBuffer);
                    value = !(value||value1);
                    break;
                default:
                    lexemeBuffer.back();
                    return value;
            }
        }
    }
    public boolean Schaeffer(LexemeBuffer lexemeBuffer){
        boolean value = Operand(lexemeBuffer);
        while (true){
            Lexeme lexeme = lexemeBuffer.next();
            switch (lexeme.getType()){
                case OP_SCHAEFFER:
                    boolean value1=Operand(lexemeBuffer);
                    value = !(value&&value1);
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
