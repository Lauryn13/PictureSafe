package com.example.picturesafe.classes;

public abstract class StoringData<T> {
    protected T content;

    public abstract byte[] convert_to_bytes();
}

