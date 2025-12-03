package com.example.picturesafe.classes;

public class TextData extends StoringData<String> {

    public TextData(String text){
        this.content = text;
    }

    @Override
    public byte[] convert_to_binary(){
        return this.content.getBytes();
    }
}
