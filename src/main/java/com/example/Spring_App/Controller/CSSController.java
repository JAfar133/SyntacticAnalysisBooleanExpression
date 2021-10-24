package com.example.Spring_App.Controller;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

@Controller
public class CSSController {
    @GetMapping("/styles/css/{code}.css")
    @ResponseBody
    public ResponseEntity<String> styles(@PathVariable("code") String code) throws IOException {
        // получаем содержимое файла из папки ресурсов в виде потока
        InputStream is = getClass().getClassLoader().getResourceAsStream("static/css/"+code+".css");
        // преобразуем поток в строку
        BufferedReader bf = new BufferedReader(new InputStreamReader(is));
        StringBuffer sb = new StringBuffer();
        String line;
        while((line = bf.readLine()) != null){
            sb.append(line+"\n");
        }
        // создаем объект, в котором будем хранить HTTP заголовки
        final HttpHeaders httpHeaders= new HttpHeaders();
        // добавляем заголовок, который хранит тип содержимого
        httpHeaders.add("Content-Type", "text/css; charset=utf-8");
        // возвращаем HTTP ответ, в который передаем тело ответа, заголовки и статус 200 Ok
        return new ResponseEntity<>( sb.toString(), httpHeaders, HttpStatus.OK);
    }

}