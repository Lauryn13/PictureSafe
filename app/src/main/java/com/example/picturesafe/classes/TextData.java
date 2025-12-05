package com.example.picturesafe.classes;

public class TextData extends StoringData<String> {
    public String content;

    public TextData(String text){
        this.content = text;
    }

    @Override
    public byte[] convert_to_bytes(){
        return this.content.getBytes();
    }
}
