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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
    @FXML
    private Label totalProfitLabel;
    @FXML
    private ComboBox<String> periodComboBox;

    // Повний список всіх нотаток
    private ObservableList<Note> allNotes = FXCollections.observableArrayList();
    private final ObservableList<Note> notes = FXCollections.observableArrayList();

    // Pattern для перевірки формату запису (число+ або число-)
    private static final Pattern NOTE_PATTERN = Pattern.compile("(\\d+)([+-])");

    @FXML
    public void initialize(){
        textColumn.setCellValueFactory(cellData -> cellData.getValue().textProperty());
        usdtColumn.setCellValueFactory(cellData -> cellData.getValue().usdtAmountProperty().asObject());
        uahColumn.setCellValueFactory(cellData -> cellData.getValue().uahAmountProperty().asObject());
        dateColumn.setCellValueFactory(cellData -> cellData.getValue().dateObjectProperty());
        notesTable.setItems(notes);

        List<Note> updateNotes = NoteStorage.loadNotes();
        Note.updateUahAmounts(updateNotes);

        usdtColumn.setCellFactory(column -> new TableCell<>(){
            @Override
            protected void updateItem(Double item, boolean empty){
                super.updateItem(item, empty);
                if (empty || item == null){
                    setText(null);
                } else {
                    setText(String.format("%.6f", item));
                }
            }
        });

        uahColumn.setCellFactory(column -> new TableCell<>(){
            @Override
            protected void updateItem(Double item, boolean empty){
                super.updateItem(item, empty);
                if (empty || item == null){
                    setText(null);
                } else {
                    setText(String.format("%.6f", item));
                }
            }
        });

        indexColumn.setCellValueFactory(cellData -> {
            int index = notes.indexOf(cellData.getValue()) + 1;
            return new ReadOnlyStringWrapper(index + ")");
        });

        allNotes.addAll(NoteStorage.loadNotes());
        notes.setAll(allNotes);
        notesTable.setItems(notes);

        // Заповнюємо комбобокс
        periodComboBox.setItems(FXCollections.observableArrayList("Усі записи", "Останні 7 днів", "Останні 30 днів"));
        periodComboBox.setValue("Усі записи");

        updateExchangeRateLabel();
        updateTotalProfit();
    }

    @FXML
    public void onAddClicked(){
        String text = noteTextArea.getText().trim();

        // Перевірка формату тексту
        Matcher matcher = NOTE_PATTERN.matcher(text);
        if (!matcher.matches()) {
            showAlert("Некоректний формат. Використовуйте формат: число+ або число- (наприклад: 1+ або 2-)");
            return;
        }

        int inputCount = Integer.parseInt(matcher.group(1));
        String operation = matcher.group(2);
        boolean isPositive = "+".equals(operation);

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

        // Для від'ємних операцій використовуємо абсолютне значення,
        // але будемо віднімати його від існуючого балансу
        usdt = Math.abs(usdt);
        uah = Math.abs(uah);

        // Шукаємо існуючий запис з такою самою датою
        Optional<Note> existingNote = allNotes.stream()
                .filter(note -> note.getDate().equals(date))
                .findFirst();

        if (existingNote.isPresent()) {
            // Оновлюємо існуючий запис
            Note noteToUpdate = existingNote.get();

            // Отримуємо поточну кількість з тексту
            Matcher existingMatcher = NOTE_PATTERN.matcher(noteToUpdate.getText());
            int currentCount = 0;
            if (existingMatcher.matches()) {
                currentCount = Integer.parseInt(existingMatcher.group(1));
            }

            // Оновлюємо кількість
            int newCount = isPositive ? currentCount + inputCount : currentCount - inputCount;

            // Оновлюємо суми в залежності від операції
            double newUsdt = isPositive ?
                    noteToUpdate.getUsdtAmount() + usdt :
                    noteToUpdate.getUsdtAmount() - usdt;
            double newUah = isPositive ?
                    noteToUpdate.getUahAmount() + uah :
                    noteToUpdate.getUahAmount() - uah;

            // Визначаємо знак для відображення
            String sign = newCount >= 0 ? "+" : "-";
            String newText = Math.abs(newCount) + sign;

            // Оновлюємо запис
            noteToUpdate.setText(newText);
            noteToUpdate.setUsdtAmount(newUsdt);
            noteToUpdate.setUahAmount(newUah);

        } else {
            // Створюємо новий запис
            double finalUsdt = isPositive ? usdt : -usdt;
            double finalUah = isPositive ? uah : -uah;
            Note note = new Note(text, finalUsdt, finalUah, date);
            allNotes.add(note);
        }

        // Зберігаємо зміни
        NoteStorage.saveNotes(new ArrayList<>(allNotes));

        // Оновлюємо відображення
        onPeriodChanged();
        updateTotalProfit();

        // Очищаємо поля
        noteTextArea.clear();
        usdtField.clear();
    }

    @FXML
    public void onDeleteClicked(){
        Note selectedNote = notesTable.getSelectionModel().getSelectedItem();
        if (selectedNote != null){
            allNotes.remove(selectedNote);
            notes.remove(selectedNote);
            NoteStorage.saveNotes(new ArrayList<>(allNotes));
            updateTotalProfit();
        } else {
            showAlert("Будь ласка, виберіть запис для видалення.");
        }
    }

    @FXML
    private void onPeriodChanged() {
        String selectedPeriod = periodComboBox.getValue();
        LocalDate now = LocalDate.now();

        List<Note> filteredNotes = switch (selectedPeriod) {
            case "Останні 7 днів" -> allNotes.stream()
                    .filter(note -> note.getDate() != null && !note.getDate().isBefore(now.minusDays(7)))
                    .toList();
            case "Останні 30 днів" -> allNotes.stream()
                    .filter(note -> note.getDate() != null && !note.getDate().isBefore(now.minusDays(30)))
                    .toList();
            default -> new ArrayList<>(allNotes);
        };

        notes.setAll(filteredNotes);
        updateTotalProfit(filteredNotes);
    }

    public void updateTotalProfit(List<Note> currentNotes) {
        double totalUsdt = currentNotes.stream().mapToDouble(Note::getUsdtAmount).sum();
        double totalUah = currentNotes.stream().mapToDouble(Note::getUahAmount).sum();

        totalProfitLabel.setText(String.format("Загалом: %.2f USDT / %.2f UAH", totalUsdt, totalUah));
    }

    public void updateExchangeRateLabel(){
        double rate = ExchangeRateService.getUsdtToUahRate();
        exchangeRateLabel.setText("Курс USDT -> UAH: " + rate);
    }

    public void updateTotalProfit(){
        updateTotalProfit(new ArrayList<>(notes));
    }

    public void showAlert(String msg){
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setContentText(msg);
        alert.show();
    }
}