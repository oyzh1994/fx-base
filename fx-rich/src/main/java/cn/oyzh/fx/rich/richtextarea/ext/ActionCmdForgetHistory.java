package cn.oyzh.fx.rich.richtextarea.ext;

import com.gluonhq.richtextarea.viewmodel.ActionCmd;
import com.gluonhq.richtextarea.viewmodel.RichTextAreaViewModel;
import javafx.beans.binding.BooleanBinding;

public class ActionCmdForgetHistory implements ActionCmd {

    @Override
    public void apply(RichTextAreaViewModel viewModel) {
        viewModel.getCommandManager().execute(new ForgetHistoryCmd());
    }

    @Override
    public BooleanBinding getDisabledBinding(RichTextAreaViewModel viewModel) {
        return viewModel.editableProperty().not();
    }
}
