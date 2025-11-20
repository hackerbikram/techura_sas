package bikram.model;

public  class Activity {
    private final javafx.beans.property.StringProperty user;
    private final javafx.beans.property.StringProperty action;
    private final javafx.beans.property.StringProperty time;

    public Activity(String u, String a, String t) {
        user = new javafx.beans.property.SimpleStringProperty(u);
        action = new javafx.beans.property.SimpleStringProperty(a);
        time = new javafx.beans.property.SimpleStringProperty(t);
    }

    // For TableView bindings
    public javafx.beans.property.StringProperty userProperty() { return user; }
    public javafx.beans.property.StringProperty actionProperty() { return action; }
    public javafx.beans.property.StringProperty timeProperty() { return time; }

    // ✅ Getter methods to return plain values
    public String getUser() { return user.get(); }
    public String getAction() { return action.get(); }
    public String getTime() { return time.get(); }
}
