package cn.oyzh.fx.rich.richtextarea.ext;

import com.gluonhq.richtextarea.RichTextArea;
import com.gluonhq.richtextarea.Selection;
import com.gluonhq.richtextarea.action.Action;
import com.gluonhq.richtextarea.action.ActionFactory;
import com.gluonhq.richtextarea.action.BasicAction;
import com.gluonhq.richtextarea.model.Decoration;

public class RichActionFactory extends ActionFactory {

    private final RichTextArea control;

    public RichActionFactory(RichTextArea control) {
        super(control);
        this.control = control;
    }

    public Action selectAndDecorate2(Selection selection, Decoration decoration) {
        return new BasicAction(control, action -> new ActionCmdSelectAndDecorate2(selection, decoration));
    }

    public Action setText(String text) {
        return new BasicAction(control, action -> new ActionCmdSetText(text));
    }

    public Action clearText() {
        return new BasicAction(control, action -> new ActionCmdClearText());
    }

    public Action appendText(String text) {
        return new BasicAction(control, action -> new ActionCmdAppendText(text));
    }

    public Action deleteText(int start, int end) {
        return new BasicAction(control, action -> new ActionCmdDeleteText(start, end));
    }
}
