package bikram;

import bikram.db.*;
import bikram.model.Role;
import bikram.model.User;
import bikram.util.AppContext;
import bikram.util.Navigator;
import bikram.views.page.*;
import bikram.views.ui.Navbar;
import bikram.views.ui.Sidebar;
import bikram.views.ui.TimeManagementView;
import javafx.animation.*;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import javafx.util.Duration;

public class Main extends Application {
    private Salesrepository salesRepo = new SalesDB();
    private ProductRepository productRepo = new ProductDB();
    private UserRepository udb = new UserDB();

    private StackPane mainContent;

    @Override
    public void start(Stage primaryStage) {
        createAlltable();
        if (udb.getAllUsers().isEmpty()) {
            User user = new User("admin", "", "", "", "admin@gmail.com", "admin123", Role.OWNER, 100000);
            udb.addUser(user);
            System.out.println("admin created successfully!");
        }

        AppContext.setHostServices(getHostServices());
        primaryStage.setTitle("Techura Business Dashboard");



        // --- Splash Screen ---
        StackPane splashRoot = new StackPane();
        splashRoot.setStyle("-fx-background-color: linear-gradient(to bottom, #141E30, #243B55);");

        ImageView logo = new ImageView(new Image(getClass().getResource("/images/logo.png").toExternalForm()));
        logo.setFitWidth(200);
        logo.setFitHeight(200);
        splashRoot.getChildren().add(logo);

        Scene splashScene = new Scene(splashRoot, 1300, 800);
        Stage splashStage = new Stage();
        splashStage.setScene(splashScene);

        // --- Splash fade in ---
        splashRoot.setOpacity(0);
        FadeTransition rootFadeIn = new FadeTransition(Duration.seconds(1.0), splashRoot);
        rootFadeIn.setFromValue(0);
        rootFadeIn.setToValue(1.0);

        // --- Logo scale + fade ---
        logo.setOpacity(0);
        FadeTransition logoFade = new FadeTransition(Duration.seconds(1.2), logo);
        logoFade.setFromValue(0);
        logoFade.setToValue(1);

        ScaleTransition logoScale = new ScaleTransition(Duration.seconds(1.2), logo);
        logoScale.setFromX(0.6);
        logoScale.setFromY(0.6);
        logoScale.setToX(1.0);
        logoScale.setToY(1.0);

        ParallelTransition logoIntro = new ParallelTransition(logoFade, logoScale);

        SequentialTransition splashSeq = new SequentialTransition(
                rootFadeIn,
                logoIntro,
                new PauseTransition(Duration.seconds(1.0)) // pause to let logo be visible
        );

        splashStage.show();
        splashSeq.play();

        splashSeq.setOnFinished(e -> {
            // --- Fade out splash ---
            FadeTransition fadeOut = new FadeTransition(Duration.seconds(0.8), splashRoot);
            fadeOut.setFromValue(1.0);
            fadeOut.setToValue(0.0);
            fadeOut.setOnFinished(ev -> splashStage.close());
            fadeOut.play();

            // --- Main app setup ---
            Navigator.registerPageClass("TechuraDashboard", TechuraDashboard.class);
            Navigator.registerPageClass("AboutPage", AboutPage.class);
            Navigator.registerPageClass("ProfilePage", ProfilePage.class);
            Navigator.registerPageClass("ProductPage", ProductPage.class);
            Navigator.registerPageClass("LoginPage", LoginPage.class);
            Navigator.registerPageClass("ContactSupportPage", ContactSupportPage.class);
            Navigator.registerPageClass("Calculator", Calculator.class);
            Navigator.registerPageClass("Notebook", Notebook.class);
            Navigator.registerPageClass("TaskManager", TaskManager.class);
            Navigator.registerPageClass("Settings", Settings.class);
            Navigator.registerPageClass("SalesPage", SalesPage.class);
            Navigator.registerPageClass("PriceCardPage", PriceCardPage.class);
            Navigator.registerPageClass("UserIDCardPage", UserIDCardPage.class);
            try {
                Navigator.registerPage("AITrendPage", AITrendPage.class.newInstance());
            } catch (InstantiationException ex) {
                throw new RuntimeException(ex);
            } catch (IllegalAccessException ex) {
                throw new RuntimeException(ex);
            }
            Navigator.registerPageClass("TimeManagementView", TimeManagementView.class);
            Navigator.registerPageClass("EmploymentPaySlip",EmploymentPaySlip.class);
            Navigator.registerPageClass("QRGeneratorPage",QRGeneratorPage.class);
            AppContext.setPrimaryStage(primaryStage);


            BorderPane root = new BorderPane();
            Sidebar sidebar = new Sidebar();
            Navbar navbar = new Navbar();
            mainContent = new StackPane();
            mainContent.getStyleClass().add("main-content");

            root.setTop(navbar);
            root.setLeft(sidebar);
            root.setCenter(mainContent);
            Navigator.setMainContent(mainContent);
            Navigator.navigate("TechuraDashboard");
            mainContent.setOpacity(0); // start invisible
            FadeTransition fadeMain = new FadeTransition(Duration.seconds(1.0), mainContent);
            fadeMain.setFromValue(0);
            fadeMain.setToValue(1.0);
            fadeMain.play();

            Scene scene = new Scene(root, 1300, 800);
            scene.getStylesheets().add("styles.css");

            // --- Smooth exit animation ---
            primaryStage.setOnCloseRequest(ev -> {
                ev.consume();
                FadeTransition fadeRoot = new FadeTransition(Duration.seconds(0.8), root);
                fadeRoot.setFromValue(1.0);
                fadeRoot.setToValue(0.0);
                fadeRoot.setOnFinished(event -> primaryStage.close());
                fadeRoot.play();
            });

            primaryStage.setScene(scene);
            primaryStage.show();
        });
    }
    public void createAlltable(){
        udb.createTable();
        salesRepo.createTable();
        productRepo.createTable();
    }


    public static void main(String[] args) {
        launch(args);
    }
}
