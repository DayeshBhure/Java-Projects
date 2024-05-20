package com.todolist.application;

import com.todolist.application.datamodel.TodoData;
import com.todolist.application.datamodel.TodoItem;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.time.LocalDate;
import java.util.Optional;

public class DialogController {

    @FXML
    private TextField shortDescriptionField;
    @FXML
    private TextArea detailsArea;
    @FXML
    private DatePicker deadlinePicker;

    public TodoItem processAddTodoDialog(){
        String shortDescription = shortDescriptionField.getText().trim();
        String details = detailsArea.getText().trim();
        LocalDate deadlineValue = deadlinePicker.getValue();

        if(shortDescription.equals("")){
            shortDescription = "Untitled To-do";
        }

        if(details.equals("")){
            details = "<EMPTY>";
        }

        if(deadlineValue == null){
            deadlineValue = LocalDate.now();
        }

        TodoItem newItem = new TodoItem(shortDescription, details, deadlineValue);
        TodoData.getInstance().addTodoItem(newItem);
        return newItem;
    }

    public TodoItem processEditTodoDialog(TodoItem oldItem){
        String shortDescription = shortDescriptionField.getText().trim();
        String details = detailsArea.getText().trim();
        LocalDate deadlineValue = deadlinePicker.getValue();

        if(shortDescription.equals("")){
            shortDescription = "Untitled To-do";
        }

        if(details.equals("")){
            details = "<EMPTY>";
        }

        if(deadlineValue == null){
            deadlineValue = LocalDate.now();
        }

        TodoItem editedItem = new TodoItem(shortDescription, details, deadlineValue);
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Edit To-do Item");
        alert.setHeaderText("Edit item: " + oldItem.getShortDescription());
        alert.setContentText("Are you sure?\nPress OK to confirm, cancel to back out.");

        Optional<ButtonType> result = alert.showAndWait();
        if(result.isPresent() && result.get() == ButtonType.OK){
            TodoData.getInstance().updateTodoItem(oldItem, editedItem);
            return editedItem;
        }

        return oldItem;
    }

    public void setFields(TodoItem item){
        shortDescriptionField.setText(item.getShortDescription());
        detailsArea.setText(item.getDetails());
        deadlinePicker.setValue(item.getDeadline());
    }

}
