//
//package cn.oyzh.fx.rich.richtextarea.ext;
//
//import com.gluonhq.richtextarea.viewmodel.AbstractEditCmd;
//import com.gluonhq.richtextarea.viewmodel.RichTextAreaViewModel;
//
//public class DeleteTextCmd extends AbstractEditCmd {
//
//    private final int start;
//
//    private final int end;
//
//    public DeleteTextCmd(int start, int end) {
//        this.start = start;
//        this.end = end;
//    }
//
//    @Override
//    public void doRedo(RichTextAreaViewModel viewModel) {
//        viewModel.setCaretPosition(this.start);
//        viewModel.getTextBuffer().delete(this.start, end);
//        viewModel.clearSelection();
//    }
//
//    @Override
//    public void doUndo(RichTextAreaViewModel viewModel) {
//        viewModel.undo();
//    }
//}
