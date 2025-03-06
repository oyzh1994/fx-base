package cn.oyzh.fx.plus.test;

import cn.oyzh.fx.plus.tableview.TableViewUtil;
import javafx.application.Application;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javafx.scene.control.TableCell;
import javafx.util.Callback;

public class TableViewSelectionRectangleExample extends Application {

    private double startX, startY;
    private Rectangle selectionRect;
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

        root = new Pane(tableView);
        selectionRect = new Rectangle(0, 0, 0, 0);
        selectionRect.setFill(Color.LIGHTBLUE.deriveColor(0, 1, 1, 0.3));
        selectionRect.setVisible(false);
        root.getChildren().add(selectionRect);

        // 监听鼠标按下事件
        tableView.addEventFilter(MouseEvent.MOUSE_PRESSED, event -> {
            if (event.getButton() == MouseButton.PRIMARY) {
                startX = event.getSceneX();
                startY = event.getSceneY();
                selectionRect.setX(startX);
                selectionRect.setY(startY);
                selectionRect.setWidth(0);
                selectionRect.setHeight(0);
                selectionRect.setVisible(true);
            }
        });

        // 监听鼠标拖动事件
        tableView.addEventFilter(MouseEvent.MOUSE_DRAGGED, event -> {
            if (event.getButton() == MouseButton.PRIMARY) {
                double endX = event.getSceneX();
                double endY = event.getSceneY();
                selectionRect.setX(Math.min(startX, endX));
                selectionRect.setY(Math.min(startY, endY));
                selectionRect.setWidth(Math.abs(endX - startX));
                selectionRect.setHeight(Math.abs(endY - startY));
                onSelection(selectionRect);
            }
        });

        // 监听鼠标释放事件
        tableView.addEventFilter(MouseEvent.MOUSE_RELEASED, event -> {
            if (event.getButton() == MouseButton.PRIMARY) {
//                selectCellsInRectangle();
                onSelection(selectionRect);
                selectionRect.setVisible(false);
            }
        });


        // 设置行工厂，用于获取行高
        tableView.setRowFactory(tv -> {
            TableRow<Person> row = new TableRow<>();
            row.visibleProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue) {
                    // 获取行高
                    double rowHeight = row.getHeight();
                    System.out.println("Row height: " + rowHeight);
                }
            });
            return row;
        });

        // 创建场景并显示
        Scene scene = new Scene(root, 400, 300);
        primaryStage.setScene(scene);
        primaryStage.show();


//        // 获取指定行（例如第一行）的高度
//        int rowIndex = 0;
//        TableRow<Person> row = (TableRow<Person>) tableView.lookup(".table-row-cell[index=" + rowIndex + "]");
//        if (row != null) {
//            double rowHeight = row.getHeight();
//            System.out.println("Row " + rowIndex + " height: " + rowHeight);
//        }


//        // 使用 Platform.runLater 确保在 JavaFX 应用线程的下一次脉冲中执行
//        Platform.runLater(() -> {
//            Set<Node> rows = tableView.lookupAll(".table-row-cell");
//            for (Node row : rows) {
//                if (row instanceof TableRow) {
//                    TableRow<?> tableRow = (TableRow<?>) row;
//                    double rowHeight = tableRow.getHeight();
//                    System.out.println("Row height: " + rowHeight);
//                }
//            }
//        });

    }

    private void onSelection(Rectangle rectangle) {

//        System.out.println(tableView.getRowFactory().call(this.tableView).getHeight());
//        System.out.println(tableView.getRowFactory().call(this.tableView).getPrefHeight());
//        System.out.println(tableView.getRowFactory().call(this.tableView).getMinHeight());

        // 获取指定行（例如第一行）的高度
//        int rowIndex = 0;
//        TableRow<Person> row = (TableRow<Person>) tableView.lookup(".table-row-cell[index=" + rowIndex + "]");
//        if (row != null) {
//            double rowHeight = row.getHeight();
//            System.out.println("Row " + rowIndex + " height: " + rowHeight);
//        }


//        System.out.println(TableViewUtil.getRowHeight(tableView));


        double rowHeight = TableViewUtil.getRowHeight(tableView);
        double startY = tableView.getLayoutBounds().getMinY();
        double endX = tableView.getLayoutBounds().getMaxY();
        System.out.println(startX);
        System.out.println(endX);
//        double endX = startX + tableView.getHeight();
        double selectionStart = rectangle.getLayoutBounds().getMinY();
        double selectionEnd = rectangle.getLayoutBounds().getMaxY();
        System.out.println(selectionStart);
        System.out.println(selectionEnd);
//        double selectionEnd = selectionStart + rectangle.getHeight();
        List<Integer> selected = new ArrayList<>();
        int index = 0;
        double indexY = startY;
        for (Person item : tableView.getItems()) {
            indexY += rowHeight;
            if (indexY >= endX || indexY >= selectionEnd) {
                break;
            }
            if (indexY >= selectionStart - rowHeight) {
                selected.add(index);
            }
            index++;
        }

        this.tableView.getSelectionModel().clearSelection();
        if (selected.size() > 1) {
            System.out.println(Arrays.toString(selected.toArray()));
            int startIndex = selected.getFirst();
            int endIndex = selected.getLast();
            this.tableView.getSelectionModel().selectRange(startIndex, endIndex + 1);
        } else if (selected.size() == 1) {
            System.out.println(Arrays.toString(selected.toArray()));
            int startIndex = selected.getFirst();
            this.tableView.getSelectionModel().select(startIndex);
        }

//        System.out.println(rectangle.getX());
//        System.out.println(rectangle.getX() + rectangle.getHeight());
//        System.out.println(tableView.getLayoutX());
//        System.out.println(tableView.getLayoutX() + tableView.getHeight());
//        System.out.println(startIndex);
//        System.out.println(endIndex);
        System.out.println("------->");

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

    public static class TableViewSelectionRectangleExampleApp {

        public static void main(String[] args) throws URISyntaxException {
            TableViewSelectionRectangleExample.main(args);
        }
    }
}