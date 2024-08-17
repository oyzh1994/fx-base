package cn.oyzh.fx.rich.richtextarea.ext;

import com.gluonhq.richtextarea.viewmodel.ActionCmd;
import com.gluonhq.richtextarea.viewmodel.RichTextAreaViewModel;
import javafx.beans.binding.BooleanBinding;

import java.util.Objects;

public class ActionCmdAppendText implements ActionCmd {

    private final String content;

    public ActionCmdAppendText(String content) {
        this.content = content;
    }

    @Override
    public void apply(RichTextAreaViewModel viewModel) {
        if (this.content != null) {
            viewModel.getCommandManager().execute(new AppendTextCmd(content));
        }
    }

    @Override
    public BooleanBinding getDisabledBinding(RichTextAreaViewModel viewModel) {
        return viewModel.editableProperty().not();
    }
}
