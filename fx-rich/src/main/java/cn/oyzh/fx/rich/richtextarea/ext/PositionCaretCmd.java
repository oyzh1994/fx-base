
package cn.oyzh.fx.rich.richtextarea.ext;

import com.gluonhq.richtextarea.undo.AbstractCommand;
import com.gluonhq.richtextarea.viewmodel.RichTextAreaViewModel;

public class PositionCaretCmd extends AbstractCommand<RichTextAreaViewModel> {

    private final int position;

    public PositionCaretCmd(int position) {
        this.position = position;
    }

    @Override
    public void doUndo(RichTextAreaViewModel viewModel) {
    }

    @Override
    public void doRedo(RichTextAreaViewModel viewModel) {
        viewModel.setCaretPosition(Math.min(this.position, viewModel.getTextLength()));
    }
}
