package com.example.Spring_App.Service.lexemeService;

public class Lexeme {
    private LexemeType type;
    private String value;

    public Lexeme(LexemeType type, String value) {
        this.type = type;
        this.value = value;
    }
    public Lexeme(LexemeType type, Character value) {
        this.type = type;
        this.value = value.toString();
    }

    public LexemeType getType() {
        return type;
    }

    public void setType(LexemeType type) {
        this.type = type;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
