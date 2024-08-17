
package cn.oyzh.fx.rich.richtextarea.ext;

import com.gluonhq.richtextarea.Selection;
import com.gluonhq.richtextarea.undo.AbstractCommand;
import com.gluonhq.richtextarea.viewmodel.AbstractEditCmd;
import com.gluonhq.richtextarea.viewmodel.RichTextAreaViewModel;

public class SelectRangeCmd extends AbstractCommand<RichTextAreaViewModel> {

    private final int start;

    private final int end;

    public SelectRangeCmd(int start, int end) {
        this.start = start;
        this.end = end;
    }

    @Override
    public void doUndo(RichTextAreaViewModel viewModel) {
        viewModel.clearSelection();
    }

    @Override
    public void doRedo(RichTextAreaViewModel viewModel) {
        viewModel.setSelection(new Selection(this.start, this.end));
    }
}
