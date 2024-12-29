//package cn.oyzh.fx.rich.richtextarea.ext;
//
//import com.gluonhq.richtextarea.RichTextArea;
//import com.gluonhq.richtextarea.Selection;
//import com.gluonhq.richtextarea.action.Action;
//import com.gluonhq.richtextarea.action.ActionFactory;
//import com.gluonhq.richtextarea.action.BasicAction;
//import com.gluonhq.richtextarea.model.Decoration;
//
//public class RichActionFactory extends ActionFactory {
//
//    private final RichTextArea control;
//
//    public RichActionFactory(RichTextArea control) {
//        super(control);
//        this.control = control;
//    }
//
//    public Action setStyle(Selection selection, Decoration decoration) {
//        return new BasicAction(this.control, action -> new ActionCmdSetStyle(selection, decoration));
//    }
//
//    public Action setText(String text) {
//        return new BasicAction(this.control, action -> new ActionCmdSetText(text));
//    }
//
//    public Action clearText() {
//        return new BasicAction(this.control, action -> new ActionCmdClearText());
//    }
//
//    public Action appendText(String text) {
//        return new BasicAction(this.control, action -> new ActionCmdAppendText(text));
//    }
//
//    public Action deleteText(int start, int end) {
//        return new BasicAction(this.control, action -> new ActionCmdDeleteText(start, end));
//    }
//
//    public Action positionCaret(int caretPosition) {
//        return new BasicAction(this.control, action -> new ActionCmdPositionCaret(caretPosition));
//    }
//
//    public Action insertText(String text) {
//        return new BasicAction(this.control, action -> new ActionCmdInsertText(text));
//    }
//
//    public Action forgetHistory() {
//        return new BasicAction(this.control, action -> new ActionCmdForgetHistory());
//    }
//
//    public Action replaceText(String text) {
//        return new BasicAction(this.control, action -> new ActionCmdReplaceText(text));
//    }
//
//    public Action selectRange(int anchor, int caretPosition) {
//        return new BasicAction(this.control, action -> new ActionCmdSelectRange(anchor, caretPosition));
//    }
//}
