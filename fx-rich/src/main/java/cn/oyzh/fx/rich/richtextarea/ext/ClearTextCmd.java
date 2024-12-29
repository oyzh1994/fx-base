//
//package cn.oyzh.fx.rich.richtextarea.ext;
//
//import com.gluonhq.richtextarea.viewmodel.AbstractEditCmd;
//import com.gluonhq.richtextarea.viewmodel.RichTextAreaViewModel;
//
//public class ClearTextCmd extends AbstractEditCmd {
//
//    @Override
//    public void doRedo(RichTextAreaViewModel viewModel) {
//        viewModel.setCaretPosition(0);
//        viewModel.remove(0, viewModel.getTextLength());
//        viewModel.setCaretPosition(0);
//    }
//
//    @Override
//    public void doUndo(RichTextAreaViewModel viewModel) {
//        viewModel.undo();
//    }
//}
