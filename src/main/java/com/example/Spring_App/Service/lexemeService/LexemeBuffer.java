package com.example.Spring_App.Service.lexemeService;

import java.util.List;

public class LexemeBuffer {
    private int pos;
    public List<Lexeme> lexemes;

    public LexemeBuffer(List<Lexeme> lexemes) {
        pos=0;
        this.lexemes = lexemes;
    }
    public Lexeme next(){
        return lexemes.get(pos++);
    }
    public void back(){
        pos--;
    }
    public Lexeme getBack(){
        int back = pos;
        return lexemes.get(--back);
    }
    public int getPos(){
        return pos;
    }

    public List<Lexeme> getLexemes() {
        return lexemes;
    }
}
