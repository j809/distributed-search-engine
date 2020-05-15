package com.compsci532.minigoogle.dictionary;

import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

@Component
public class Lexicon implements Serializable {
    private Map<Integer, String> dictionary = new HashMap<>();

    public Integer add(String word) {
        Integer hashCode = word.hashCode();
        dictionary.put(hashCode, word);
        return hashCode;
    }

    public String get(Integer id) {
        return dictionary.get(id);
    }
}
