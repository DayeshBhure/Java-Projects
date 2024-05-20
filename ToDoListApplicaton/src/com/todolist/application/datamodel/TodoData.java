package com.todolist.application.datamodel;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Iterator;

public class TodoData {
    private static final TodoData instance = new TodoData();
    private static final String fileName = "TodoListItems.tsv";

    private ObservableList<TodoItem> todoItems;
    private final DateTimeFormatter df;

    public static TodoData getInstance(){
        return instance;
    }

    private TodoData(){
        this.df = DateTimeFormatter.ofPattern("dd-MM-yyyy");
    }

    public ObservableList<TodoItem> getTodoItems() {
        return todoItems;
    }

    public void addTodoItem(TodoItem item){
        this.todoItems.add(item);
    }

    public void updateTodoItem(TodoItem oldItem, TodoItem newItem){
        this.todoItems.remove(oldItem);
        addTodoItem(newItem);
//        this.todoItems.set(todoItems.indexOf(oldItem), newItem);
    }

    public void deleteTodoItem(TodoItem item){
        todoItems.remove(item);
    }

    public void storeTodoItems() throws IOException{
        Path path = Paths.get(fileName);
        BufferedWriter bw = Files.newBufferedWriter(path);

        try {
            Iterator<TodoItem> iter = todoItems.iterator();
            while(iter.hasNext()){
                TodoItem item = iter.next();
                bw.write(String.format("%s\t%s\t%s",
                        item.getShortDescription(),
                        item.getDetails(),
                        item.getDeadline().format(df)));
                bw.newLine();
            }
        } finally {
            if(bw != null)
                bw.close();
        }
    }

    public void loadTodoItems() throws IOException{
        todoItems = FXCollections.observableArrayList();
        Path path = Paths.get(fileName);
        BufferedReader br = Files.newBufferedReader(path);

        String input;

        try {
            while((input = br.readLine()) != null) {
                String[] itemPieces = input.split("\t");

                String shortDescription = itemPieces[0];
                String details = itemPieces[1];
                String dateString = itemPieces[2];

                LocalDate date = LocalDate.parse(dateString, df);

                TodoItem item = new TodoItem(shortDescription, details, date);
                todoItems.add(item);
            }
        } finally{
            if(br!=null)
                br.close();
        }
    }
}
