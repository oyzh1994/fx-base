package com.gluonhq.richtextarea.viewmodel;

import com.gluonhq.richtextarea.Selection;
import javafx.beans.binding.BooleanBinding;

import java.util.Objects;

public class ActionCmdAppendText implements ActionCmd {

    private final String content;

    public ActionCmdAppendText(String content) {
        this.content = Objects.requireNonNull(content);
    }

    @Override
    public void apply(RichTextAreaViewModel viewModel) {
        if (viewModel.isEditable()) {
            viewModel.getCommandManager().execute(new AppendTextCmd(content));
        }
    }

    @Override
    public BooleanBinding getDisabledBinding(RichTextAreaViewModel viewModel) {
        return viewModel.editableProperty().not();
    }
}
