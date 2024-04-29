package gr.uoa.di.ai.types;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class PredicateContent {
    private int id;
    private BufferedWriter predTable;

    public PredicateContent(int id, BufferedWriter predTable) {
        this.id = id;
        this.predTable = predTable;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public BufferedWriter getPredTable() {
        return predTable;
    }

    public void setPredTable(BufferedWriter predTable) {
        this.predTable = predTable;
    }
}
