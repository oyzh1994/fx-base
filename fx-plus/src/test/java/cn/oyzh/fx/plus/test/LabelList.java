package cn.oyzh.fx.plus.test;

import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;

public class LabelList
{
    private ScrollPane scrollPane;
    private VBox listPane;
    private int maxLabelCount;
    private boolean addToBottom;

    public LabelList(int maxLabelCount, boolean addToBottom)
    {
        listPane = new VBox ();

        scrollPane = new ScrollPane ();
        scrollPane.setContent (listPane);
        scrollPane.setFitToWidth ( true ); // 不显示横向滚动条

        this.maxLabelCount = maxLabelCount;

        this.addToBottom = addToBottom;
    }

    public Control getControl()
    {
        return scrollPane;
    }

    public Label addLabel(String labelText)
    {
        if (listPane.getChildren ().size () >= maxLabelCount)
        {
            if (addToBottom)
            {
                listPane.getChildren ().remove ( 0 );
            }
            else
            {
                listPane.getChildren ().remove (listPane.getChildren ().size () - 1 );
            }
        }

        if (addToBottom)
        {
            scrollPane.setVvalue ( 1.0 ); // 自动下拉到底部
        }

        Label label = new Label (labelText);
        label.setPrefWidth (scrollPane.getWidth () * 0.99 );

        label.setOnMouseEntered ( (e) -> {
            label.setStyle ( "-fx-background-color: #F0F8FF;" );
        });
        label.setOnMouseExited ( (e) -> {
            label.setStyle ( "-fx-background-color: transparent;" );
        });

        if (addToBottom)
        {
            listPane.getChildren ().add (label);
        }
        else
        {
            listPane.getChildren ().add ( 0, label);
        }

        return label;
    }
}
