package bikram.util;

import bikram.db.EmployeeTimeRepository;
import bikram.db.ProductRepository;
import bikram.db.UserRepository;
import javafx.application.HostServices;
import javafx.stage.Stage;

public class AppContext {
    private static HostServices hostServices;
    private static Stage primaryStage;
    public final UserRepository users;
    public final ProductRepository products;
    public final EmployeeTimeRepository employees;

    public static void setHostServices(HostServices hs) {
        hostServices = hs;
    }
    public AppContext(UserRepository users,ProductRepository products,EmployeeTimeRepository employees){
        this.employees=employees;
        this.products=products;
        this.users=users;
    }

    public static HostServices getHostServices() {
        return hostServices;
    }
    public static void setPrimaryStage(Stage stage) { primaryStage = stage; }
    public static Stage getPrimaryStage() { return primaryStage; }
}
