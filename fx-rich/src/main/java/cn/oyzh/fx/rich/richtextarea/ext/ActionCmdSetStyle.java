//package cn.oyzh.fx.rich.richtextarea.ext;
//
//import com.gluonhq.richtextarea.Selection;
//import com.gluonhq.richtextarea.model.Decoration;
//import com.gluonhq.richtextarea.viewmodel.ActionCmdSelectAndDecorate;
//import com.gluonhq.richtextarea.viewmodel.RichTextAreaViewModel;
//
//import java.util.Objects;
//
//public class ActionCmdSetStyle extends ActionCmdSelectAndDecorate {
//
//    private final Selection selection;
//    private final Decoration decoration;
//
//    public ActionCmdSetStyle(Selection selection, Decoration decoration) {
//        super(selection, decoration);
//        this.selection = Objects.requireNonNull(selection);
//        this.decoration = Objects.requireNonNull(decoration);
//    }
//
//    @Override
//    public void apply(RichTextAreaViewModel viewModel) {
//        viewModel.getCommandManager().execute(new SetStyleCmd(selection, decoration));
//    }
//
//}
