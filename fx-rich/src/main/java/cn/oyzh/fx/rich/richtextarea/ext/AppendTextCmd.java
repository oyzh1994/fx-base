////
//// Source code recreated from a .class file by IntelliJ IDEA
//// (powered by FernFlower decompiler)
////
//
//package cn.oyzh.fx.rich.richtextarea.ext;
//
//import com.gluonhq.richtextarea.viewmodel.AbstractEditCmd;
//import com.gluonhq.richtextarea.viewmodel.RichTextAreaViewModel;
//
//public class AppendTextCmd extends AbstractEditCmd {
//
//    private final String content;
//
//    public AppendTextCmd(String content) {
//        this.content = content;
//    }
//
//    @Override
//    public void doRedo(RichTextAreaViewModel viewModel) {
//        viewModel.getTextBuffer().insert(this.content, viewModel.getTextLength());
//    }
//
//    @Override
//    public void doUndo(RichTextAreaViewModel viewModel) {
//        viewModel.undo();
//    }
//}
