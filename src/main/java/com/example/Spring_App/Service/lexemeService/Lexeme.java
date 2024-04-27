package com.example.Spring_App.Service.lexemeService;

public class Lexeme implements Cloneable {
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

    public Lexeme(Lexeme lexeme) {
        this.type = lexeme.getType();
        this.value = lexeme.getValue();
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

    @Override
    public Lexeme clone() {
        try {
            Lexeme clone = (Lexeme) super.clone();
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
}
