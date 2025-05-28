package com.example.profitnotes.controller;

import com.example.profitnotes.model.ExchangeRateService;
import com.example.profitnotes.model.Note;
import com.example.profitnotes.util.NoteStorage;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.time.LocalDate;


public class MainController {
    @FXML
    private TextArea noteTextArea;
    @FXML
    private TextField usdtField;
    @FXML
    private Label exchangeRateLabel;
    @FXML
    private Button addButton;
    @FXML
    private DatePicker datePicker;
    @FXML
    private TableView<Note> notesTable;
    @FXML
    private TableColumn<Note, String> indexColumn;
    @FXML
    private TableColumn<Note, String> textColumn;
    @FXML
    private TableColumn<Note, Double> usdtColumn;
    @FXML
    private TableColumn<Note, Double> uahColumn;
    @FXML
    private TableColumn<Note, LocalDate> dateColumn;

    private final ObservableList<Note> notes = FXCollections.observableArrayList();

    @FXML
    public void initialize(){
        textColumn.setCellValueFactory(cellData -> cellData.getValue().textProperty());
        usdtColumn.setCellValueFactory(cellData -> cellData.getValue().usdtAmountProperty().asObject());
        uahColumn.setCellValueFactory(cellData -> cellData.getValue().uahAmountProperty().asObject());
        dateColumn.setCellValueFactory(cellData -> cellData.getValue().dateObjectProperty());
        notesTable.setItems(notes);

        indexColumn.setCellValueFactory(cellData -> {
            int index = notes.indexOf(cellData.getValue()) + 1;
            return new ReadOnlyStringWrapper(index + ")");
                });

        notes.addAll(NoteStorage.loadNotes());
        notesTable.setItems(notes);

        updateExchangeRateLabel();
    }

    @FXML
    public void onAddClicked(){
        String text = noteTextArea.getText();
        double usdt;
        try {
            usdt = Double.parseDouble(usdtField.getText());
        } catch (NumberFormatException e){
            showAlert("Некоректне значення в USDT.");
            return;
        }

        LocalDate date = datePicker.getValue();
        if (date == null){
            showAlert("Будь ласка оберіть дату.");
            return;
        }

        double rate = ExchangeRateService.getUsdtToUahRate();
        double uah = usdt * rate;

        Note note = new Note(text, usdt, uah, date);
        notes.add(note);
        NoteStorage.saveNotes(notes);

        noteTextArea.clear();
        usdtField.clear();
    }

    @FXML
    public void onDeleteClicked(){
        Note selectedNote = notesTable.getSelectionModel().getSelectedItem();
        if (selectedNote != null){
            notes.remove(selectedNote);
            NoteStorage.saveNotes(notes);
        } else {
            showAlert("Будь ласка, виберіть запис для видалення.");
        }
    }

    public void updateExchangeRateLabel(){
        double rate = ExchangeRateService.getUsdtToUahRate();
        exchangeRateLabel.setText("Курс USDT -> UAH: " + rate);
    }

    public void showAlert(String msg){
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setContentText(msg);
        alert.show();
    }
}
