
package cn.oyzh.fx.rich.richtextarea.ext;

import com.gluonhq.richtextarea.Selection;
import com.gluonhq.richtextarea.undo.AbstractCommand;
import com.gluonhq.richtextarea.viewmodel.AbstractEditCmd;
import com.gluonhq.richtextarea.viewmodel.RichTextAreaViewModel;

public class ReplaceTextCmd extends AbstractCommand<RichTextAreaViewModel> {

    private final String content;

    public ReplaceTextCmd(String content) {
        this.content = content;
    }

    @Override
    public void doUndo(RichTextAreaViewModel viewModel) {
        viewModel.undo();
    }

    @Override
    public void doRedo(RichTextAreaViewModel viewModel) {
        Selection selection = viewModel.getSelection();
        if (selection.isDefined() && selection.getLength() > 0) {
            viewModel.getTextBuffer().delete(selection.getStart(), selection.getEnd() - 1);
            viewModel.clearSelection();
        }
        if (selection.isDefined()) {
            viewModel.getTextBuffer().insert(this.content, selection.getStart());
        } else {
            viewModel.getTextBuffer().insert(this.content, viewModel.getCaretPosition());
        }
    }
}
