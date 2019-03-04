package utils;

public class Report {

    public Report(String name, String data) {
        this.name = name;
        this.data = data;
    }
    private String name, data;

    public String getData() {return data;}
    public String getName() {return name;}
}
