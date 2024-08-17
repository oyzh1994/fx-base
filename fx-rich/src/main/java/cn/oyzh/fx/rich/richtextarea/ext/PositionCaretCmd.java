
package cn.oyzh.fx.rich.richtextarea.ext;

import com.gluonhq.richtextarea.viewmodel.AbstractEditCmd;
import com.gluonhq.richtextarea.viewmodel.RichTextAreaViewModel;

public class PositionCaretCmd extends AbstractEditCmd {

    private final int position;

    public PositionCaretCmd(int position) {
        this.position = position;
    }

    @Override
    public void doRedo(RichTextAreaViewModel viewModel) {
        viewModel.setCaretPosition(this.position);
    }

    @Override
    public void doUndo(RichTextAreaViewModel viewModel) {
        viewModel.undo();
    }
}
