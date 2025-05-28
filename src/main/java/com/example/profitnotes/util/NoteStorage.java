package com.example.profitnotes.util;

import com.example.profitnotes.model.Note;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.File;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class NoteStorage {
    private static final String FILE_PATH = "notes.json";

    public static void saveNotes(List<Note> notes){
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new JavaTimeModule());
            mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
            mapper.writeValue(new File(FILE_PATH), notes);
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public static List<Note> loadNotes() {
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new JavaTimeModule());
            File file = new File(FILE_PATH);
            if (!file.exists()){
                return new ArrayList<>();
            }
            return mapper.readValue(file, new TypeReference<List<Note>>() {});
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
}
