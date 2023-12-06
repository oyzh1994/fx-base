package cn.oyzh.fx.plus.test;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class LabelListExample extends Application
{
    private int counter = 0;

    @Override
    public void start(Stage window) throws Exception
    {
        VBox root = new VBox ();
        root.setSpacing ( 10 );

        LabelList list = new LabelList ( 50, true );
        list.getControl ().prefHeightProperty ().bind (root.heightProperty ().divide ( 2 ));

        Button button = new Button ( "Click to Add" );
        button.setOnAction (e -> {
            counter++;
            list.addLabel ( "Hello World " + counter);
        });

        root.getChildren ().addAll (list.getControl (), button);

        Scene scene = new Scene (root, 400, 300 );

        window.setScene (scene);
        window.setTitle ( this.getClass ().getSimpleName ());
        window.show ();
    }

    public static void main(String [] args)
    {
        launch (args);
    }
}
