package com.gluonhq.richtextarea.viewmodel;

import com.gluonhq.richtextarea.Selection;
import com.gluonhq.richtextarea.model.Decoration;

import java.util.Objects;

public class ActionCmdSelectAndDecorate2 extends ActionCmdSelectAndDecorate {

    private final Selection selection;
    private final Decoration decoration;

    public ActionCmdSelectAndDecorate2(Selection selection, Decoration decoration) {
        super(selection,decoration);
        this.selection = Objects.requireNonNull(selection);
        this.decoration = Objects.requireNonNull(decoration);
    }

    @Override
    public void apply(RichTextAreaViewModel viewModel) {
        if (viewModel.isEditable()) {
            SelectAndDecorateCmd selectAndDecorateCmd = new SelectAndDecorateCmd(selection, decoration);
            selectAndDecorateCmd.setDisableUndo(true);
            viewModel.getCommandManager().execute(selectAndDecorateCmd);
        }
    }
}
