package cn.oyzh.fx.plus.test;

import javafx.application.Application;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.Scene;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.net.URISyntaxException;

public class TabPaneTest extends Application {

    @Override
    public void start(Stage primaryStage) {

        VBox root = new VBox();
        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        Tab tab1 = new Tab("测试1");

        TabPane tabPane1 = new TabPane();
        Tab tab11 = new Tab("测试11");
        Tab tab12 = new Tab("测试12");
        Tab tab13 = new Tab("测试13");
        tabPane1.getTabs().add(tab11);
        tabPane1.getTabs().add(tab12);
        tabPane1.getTabs().add(tab13);
        tab1.setContent(tabPane1);

        Tab tab2 = new Tab("测试2");
        TabPane tabPane2 = new TabPane();
        Tab tab21 = new Tab("测试11");
        Tab tab22 = new Tab("测试12");
        Tab tab23 = new Tab("测试13");
        tabPane2.getTabs().add(tab21);
        tabPane2.getTabs().add(tab22);
        tabPane2.getTabs().add(tab23);
        tab2.setContent(tabPane2);

        tabPane.getTabs().add(tab1);
        tabPane.getTabs().add(tab2);
        tabPane.getTabs().add(new Tab("测试3"));
        root.getChildren().add(tabPane);

        // 创建场景并显示
        Scene scene = new Scene(root, 400, 300);
        primaryStage.setScene(scene);
        primaryStage.show();

    }

    // 定义数据模型
    public static class Person {
        private final SimpleStringProperty firstName;
        private final SimpleStringProperty lastName;

        public Person(String firstName, String lastName) {
            this.firstName = new SimpleStringProperty(firstName);
            this.lastName = new SimpleStringProperty(lastName);
        }

        public SimpleStringProperty firstNameProperty() {
            return firstName;
        }

        public SimpleStringProperty lastNameProperty() {
            return lastName;
        }
    }

    public static void main(String[] args) {
        launch(args);
    }

    public static class TabPaneTestStarter {

        public static void main(String[] args) throws URISyntaxException {
            TabPaneTest.main(args);
        }
    }
}