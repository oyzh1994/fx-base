
package com.gluonhq.richtextarea.viewmodel;

import com.gluonhq.richtextarea.Selection;

import java.util.Objects;

public class SetTextCmd extends AbstractEditCmd {

    private final String content;

    public SetTextCmd(String content) {
        this.content = content;
    }

    @Override
    public void doRedo(RichTextAreaViewModel viewModel) {
        viewModel.getTextBuffer().delete(0, viewModel.getTextLength());
        viewModel.getTextBuffer().insert(this.content, 0);
    }

    @Override
    public void doUndo(RichTextAreaViewModel viewModel) {
        viewModel.undo();
    }
}
