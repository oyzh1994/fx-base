
package cn.oyzh.fx.rich.richtextarea.ext;

import com.gluonhq.richtextarea.viewmodel.AbstractEditCmd;
import com.gluonhq.richtextarea.viewmodel.RichTextAreaViewModel;

public class SetTextCmd extends AbstractEditCmd {

    private final String content;

    public SetTextCmd(String content) {
        this.content = content;
    }

    @Override
    public void doUndo(RichTextAreaViewModel viewModel) {
        viewModel.undo();
    }

    @Override
    public void doRedo(RichTextAreaViewModel viewModel) {
        viewModel.getTextBuffer().delete(0, viewModel.getTextLength());
        viewModel.setCaretPosition(0);
        viewModel.getTextBuffer().append(this.content);
    }
}
