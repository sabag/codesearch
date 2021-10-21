package com.daniel.search.model;

import java.util.ArrayList;
import java.util.List;
import lombok.Data;


@Data
public class FileResultsTree {

    private String id;
    private String text;
    private List<FileResultsTree> children;

    public FileResultsTree(String id, String text){
        this.id = id;
        this.text = text;
    }

    public FileResultsTree(String text){
        this.text = text;
    }

    public void add(String childText){
        add(new FileResultsTree(childText));
    }

    public void add(FileResultsTree child){
        if(children == null){
            children = new ArrayList<>();
        }
        children.add(child);
    }

    public boolean hasChildren() {
        return children != null && !children.isEmpty();
    }

    public void removeChild(FileResultsTree child) {
        children.removeIf(c -> c.getText().equals(child.getText()));
        if(children.isEmpty()) {
            children = null;
        }
    }
}
