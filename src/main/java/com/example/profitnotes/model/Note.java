package com.example.profitnotes.model;

import javafx.beans.property.*;

import java.time.LocalDate;

public class Note {
    private final StringProperty text = new SimpleStringProperty();
    private final DoubleProperty usdtAmount = new SimpleDoubleProperty();
    private final DoubleProperty uahAmount = new SimpleDoubleProperty();
    private final ObjectProperty<LocalDate> date = new SimpleObjectProperty<>();

    public Note(){}

    public Note(String text, double usdtAmount, double uahAmount, LocalDate date) {
        this.text.set(text);
        this.usdtAmount.set(usdtAmount);
        this.uahAmount.set(uahAmount);
        this.date.set(date);
    }

    public String getText() {
        return text.get();
    }

    public void setText(String text){
        this.text.set(text);
    }

    public double getUsdtAmount(){
        return usdtAmount.get();
    }

    public void setUsdtAmount(double usdtAmount){
        this.usdtAmount.set(usdtAmount);
    }

    public double getUahAmount(){
        return uahAmount.get();
    }

    public void setUahAmount(double uahAmount){
        this.uahAmount.set(uahAmount);
    }

    public LocalDate getDate(){
        return date.get();
    }

    public void setDate(LocalDate date){
        this.date.set(date);
    }
    public StringProperty textProperty(){return text;}
    public DoubleProperty usdtAmountProperty(){return usdtAmount;}
    public DoubleProperty uahAmountProperty(){return uahAmount;}
    public ObjectProperty<LocalDate> dateObjectProperty(){return date;}
}
