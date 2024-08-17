package cn.oyzh.fx.rich.richtextarea.ext;

import com.gluonhq.richtextarea.viewmodel.ActionCmd;
import com.gluonhq.richtextarea.viewmodel.RichTextAreaViewModel;
import javafx.beans.binding.BooleanBinding;

public class ActionCmdInsertText implements ActionCmd {

    private final String content;

    public ActionCmdInsertText(String content) {
        this.content = content;
    }

    @Override
    public void apply(RichTextAreaViewModel viewModel) {
        if (this.content != null) {
            viewModel.getCommandManager().execute(new InsertTextCmd(content));
        }
    }

    @Override
    public BooleanBinding getDisabledBinding(RichTextAreaViewModel viewModel) {
        return viewModel.editableProperty().not();
    }
}
