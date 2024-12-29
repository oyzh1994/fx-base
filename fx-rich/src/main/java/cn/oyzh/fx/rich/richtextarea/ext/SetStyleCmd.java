//package cn.oyzh.fx.rich.richtextarea.ext;
//
//import com.gluonhq.richtextarea.Selection;
//import com.gluonhq.richtextarea.model.Decoration;
//import com.gluonhq.richtextarea.undo.AbstractCommand;
//import com.gluonhq.richtextarea.viewmodel.RichTextAreaViewModel;
//
//import java.util.Objects;
//
//public class SetStyleCmd extends AbstractCommand<RichTextAreaViewModel> {
//    private final Selection selection;
//    private final Decoration decoration;
//
//    public SetStyleCmd(Selection selection, Decoration decoration) {
//        this.selection = Objects.requireNonNull(selection);
//        this.decoration = Objects.requireNonNull(decoration);
//        this.setDisableUndo(true);
//    }
//
//    @Override
//    protected void doUndo(RichTextAreaViewModel context) {
//
//    }
//
//    @Override
//    protected void doRedo(RichTextAreaViewModel viewModel) {
//        viewModel.setSelection(this.selection);
//        viewModel.decorate(this.decoration);
//        viewModel.setSelection(Selection.UNDEFINED);
//    }
//}
