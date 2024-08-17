
package cn.oyzh.fx.rich.richtextarea.ext;

import com.gluonhq.richtextarea.undo.AbstractCommand;
import com.gluonhq.richtextarea.viewmodel.AbstractEditCmd;
import com.gluonhq.richtextarea.viewmodel.RichTextAreaViewModel;

public class ForgetHistoryCmd extends AbstractCommand<RichTextAreaViewModel> {

    @Override
    public void doUndo(RichTextAreaViewModel viewModel) {
    }

    @Override
    public void doRedo(RichTextAreaViewModel viewModel) {
        viewModel.getCommandManager().clearStacks();
    }
}
