package com.todolist.application;

import com.todolist.application.datamodel.TodoData;
import com.todolist.application.datamodel.TodoItem;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.util.Callback;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.Optional;
import java.util.function.Predicate;

public class Controller {
    private final DateTimeFormatter df = DateTimeFormatter.ofPattern("MMMM d, yyyy");
    private FilteredList<TodoItem> filteredList;
    private Predicate<TodoItem> wantAllItems;
    private Predicate<TodoItem> wantTodaysItems;
    private Predicate<TodoItem> wantWeeksItems;

    @FXML
    private ListView<TodoItem> todoListView;
    @FXML
    private TextArea itemDetailsTextArea;
    @FXML
    private Label deadlineLabel;
    @FXML
    private BorderPane mainBorderPane;
    @FXML
    private Label filterText;
    @FXML
    private Button filterTodayButton;
    @FXML
    private RadioMenuItem subMenuToday;
    @FXML
    private Button filterWeekButton;
    @FXML
    private RadioMenuItem subMenuWeek;
    @FXML
    private Button filterAllButton;
    @FXML
    private RadioMenuItem subMenuAll;

    private ContextMenu listContextMenu;

    public void initialize(){

        listContextMenu = new ContextMenu();
        MenuItem editContextItem = new MenuItem("Edit");
        editContextItem.setOnAction(this::showEditTodoDialog);
        MenuItem deleteContextItem = new MenuItem("Delete");
        deleteContextItem.setOnAction(this::handleDeleteItem);

        listContextMenu.getItems().addAll(editContextItem, deleteContextItem);

        todoListView.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<TodoItem>() {
            @Override
            public void changed(ObservableValue<? extends TodoItem> observableValue, TodoItem toDoItem, TodoItem t1) {
                if(t1!=null){
                    TodoItem item = todoListView.getSelectionModel().getSelectedItem();
                    itemDetailsTextArea.setText(item.getDetails());
                    deadlineLabel.setText(df.format(item.getDeadline()));
                }
            }
        });

        wantAllItems = new Predicate<TodoItem>() {
            @Override
            public boolean test(TodoItem item) {
                return true;
            }
        };

        wantTodaysItems = new Predicate<TodoItem>() {
            @Override
            public boolean test(TodoItem item) {
                return item.getDeadline().equals(LocalDate.now());
            }
        };

        wantWeeksItems = new Predicate<TodoItem>() {
            @Override
            public boolean test(TodoItem item) {
                return (item.getDeadline().isAfter(LocalDate.now()) && item.getDeadline().isBefore(LocalDate.now().plusDays(7)));
            }
        };

        filteredList = new FilteredList<TodoItem>(TodoData.getInstance().getTodoItems(), wantAllItems);
        filterText.setText("(All items)");

        SortedList<TodoItem> sortedList = new SortedList<>(filteredList, new Comparator<TodoItem>() {
            @Override
            public int compare(TodoItem o1, TodoItem o2) {
                return o1.getDeadline().compareTo(o2.getDeadline());
            }
        });

        todoListView.setItems(sortedList);
        todoListView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        todoListView.getSelectionModel().selectFirst();

        todoListView.setCellFactory(new Callback<ListView<TodoItem>, ListCell<TodoItem>>() {
            @Override
            public ListCell<TodoItem> call(ListView<TodoItem> todoItemListView) {
                ListCell<TodoItem> cell = new ListCell<>(){
                    @Override
                    protected void updateItem(TodoItem item, boolean empty) {
                        super.updateItem(item, empty);
                        if(empty){
                            setText(null);
                        }else{
                            setText(item.getShortDescription());
                            if(item.getDeadline().isBefore(LocalDate.now().plusDays(1))){
                                setTextFill(Color.RED);
                            }else if(item.getDeadline().equals(LocalDate.now().plusDays(1))){
                                setTextFill(Color.DARKORANGE);
                            }else{
                                setTextFill(Color.BLACK);
                            }
                        }
                    }
                };

                cell.emptyProperty().addListener(
                        (obs, wasEmpty, isNowEmpty) -> {
                            if(isNowEmpty){
                                cell.setContextMenu(null);
                            }else{
                                cell.setContextMenu(listContextMenu);
                            }
                        }
                );

                return cell;
            }
        });
    }

    @FXML
    public void showAddTodoDialog(ActionEvent event){

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.initOwner(mainBorderPane.getScene().getWindow());

        dialog.getDialogPane().getButtonTypes().add(ButtonType.OK);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CANCEL);

        FXMLLoader fxmlLoader = new FXMLLoader();
        fxmlLoader.setLocation(getClass().getResource("todoItemDialog.fxml"));

        try {
            dialog.getDialogPane().setContent(fxmlLoader.load());
        }catch(IOException e){
            e.printStackTrace();
        }

        dialog.setTitle("New To-do Item");
        dialog.setHeaderText("Add a new to-do item."); // this is dialog's header text and in fxml it is dialog pane's header text.

        Optional<ButtonType> result = dialog.showAndWait();
        if(result.isPresent() && result.get()==ButtonType.OK){
            DialogController controller = fxmlLoader.getController();
            TodoItem newItem = controller.processAddTodoDialog();
            todoListView.getSelectionModel().select(newItem);
        }
    }

    @FXML
    public void showEditTodoDialog(ActionEvent event){
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.initOwner(mainBorderPane.getScene().getWindow());

        dialog.setTitle("Edit To-do Item");
        dialog.setHeaderText("Edit an existing to-do item.");

        dialog.getDialogPane().getButtonTypes().add(ButtonType.OK);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CANCEL);

        FXMLLoader fxmlLoader = new FXMLLoader();
        fxmlLoader.setLocation(getClass().getResource("todoItemDialog.fxml"));

        try {
            dialog.getDialogPane().setContent(fxmlLoader.load());
        }catch(IOException e){
            e.printStackTrace();
        }

        DialogController controller = fxmlLoader.getController();
        TodoItem editItem = todoListView.getSelectionModel().getSelectedItem();
        controller.setFields(editItem);

        Optional<ButtonType> result = dialog.showAndWait();
        if(result.isPresent() && result.get()==ButtonType.OK){
            TodoItem selectedItem = todoListView.getSelectionModel().getSelectedItem();
            TodoItem editedItem = controller.processEditTodoDialog(selectedItem);
            todoListView.getSelectionModel().select(editedItem);
        }
    }

    public void deleteItem(TodoItem item){
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete To-do Item");
        alert.setHeaderText("Delete item: " + item.getShortDescription());
        alert.setContentText("Are you sure?\nPress OK to confirm, cancel to back out.");

        Optional<ButtonType> result = alert.showAndWait();
        if(result.isPresent() && result.get() == ButtonType.OK){
            TodoData.getInstance().deleteTodoItem(item);
        }

    }

    @FXML
    public void handleDeleteItem(ActionEvent e){
        TodoItem item = todoListView.getSelectionModel().getSelectedItem();
        deleteItem(item);
    }

    @FXML
    public void handleDeleteKeyPress(KeyEvent k){
        TodoItem selectedItem = todoListView.getSelectionModel().getSelectedItem();
        if(selectedItem != null){
            if(k.getCode().equals(KeyCode.DELETE)){
                deleteItem(selectedItem);
            }
        }
    }

    @FXML
    public void handleExit(){
        Platform.exit();
    }

    @FXML
    public void handleFiltering(ActionEvent event){
        if(event.getSource().equals(filterTodayButton) || event.getSource().equals(subMenuToday)){
            filteredList.setPredicate(wantTodaysItems);
            filterText.setText("(Today)");
            if(filteredList.isEmpty()){
                itemDetailsTextArea.clear();
                deadlineLabel.setText("");
            }else{
                todoListView.getSelectionModel().selectFirst();
            }
        }else if(event.getSource().equals(filterWeekButton) || event.getSource().equals(subMenuWeek)){
            filteredList.setPredicate(wantWeeksItems);
            filterText.setText("(This Week)");
            if(filteredList.isEmpty()){
                itemDetailsTextArea.clear();
                deadlineLabel.setText("");
            }else{
                todoListView.getSelectionModel().selectFirst();
            }
        }else if(event.getSource().equals(filterAllButton) || event.getSource().equals(subMenuAll)){
            filteredList.setPredicate(wantAllItems);
            filterText.setText("(All items)");
            todoListView.getSelectionModel().selectFirst();
        }
    }


}
