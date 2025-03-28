package cn.oyzh.fx.plus.test;

import cn.oyzh.fx.plus.tableview.TableViewMouseSelectHelper;
import javafx.application.Application;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.net.URISyntaxException;

public class TableViewSelectionTest2 extends Application {

    private double startX, startY;
    private Pane root;
    private TableView<Person> tableView;

    @Override
    public void start(Stage primaryStage) {
        // 创建数据
        ObservableList<Person> data = FXCollections.observableArrayList(
                new Person("John", "Doe"),
                new Person("Jane", "Smith"),
                new Person("Bob", "Johnson")
        );

        // 创建TableView
        tableView = new TableView<>(data);

        // 创建列
        TableColumn<Person, String> firstNameColumn = new TableColumn<>("First Name");
        firstNameColumn.setCellValueFactory(cellData -> cellData.getValue().firstNameProperty());
        // 设置单元格工厂，以便后续获取单元格
        firstNameColumn.setCellFactory(new Callback<TableColumn<Person, String>, TableCell<Person, String>>() {
            @Override
            public TableCell<Person, String> call(TableColumn<Person, String> param) {
                return new TableCell<Person, String>() {
                    @Override
                    protected void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setText(null);
                        } else {
                            setText(item);
                        }
                    }
                };
            }
        });

        TableColumn<Person, String> lastNameColumn = new TableColumn<>("Last Name");
        lastNameColumn.setCellValueFactory(cellData -> cellData.getValue().lastNameProperty());
        // 设置单元格工厂，以便后续获取单元格
        lastNameColumn.setCellFactory(new Callback<TableColumn<Person, String>, TableCell<Person, String>>() {
            @Override
            public TableCell<Person, String> call(TableColumn<Person, String> param) {
                return new TableCell<Person, String>() {
                    @Override
                    protected void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setText(null);
                        } else {
                            setText(item);
                        }
                    }
                };
            }
        });

        // 将列添加到TableView
        tableView.getColumns().addAll(firstNameColumn, lastNameColumn);

        // 设置多选模式
        tableView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        tableView.setOnContextMenuRequested(contextMenuEvent -> {
            // 创建右键菜单
            ContextMenu contextMenu = new ContextMenu();
            MenuItem item1 = new MenuItem("Option 1");
            MenuItem item2 = new MenuItem("Option 2");
            contextMenu.getItems().addAll(item1, item2);

            // 将右键菜单关联到 TableView
            tableView.setContextMenu(contextMenu);
        });

        root = new Pane(tableView);

        TableViewMouseSelectHelper helper = new TableViewMouseSelectHelper(tableView);

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

    public static class TableViewSelectionTest2App {

        public static void main(String[] args) throws URISyntaxException {
            TableViewSelectionTest2.main(args);
        }
    }
}