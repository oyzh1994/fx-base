
package com.gluonhq.richtextarea.viewmodel;

import java.util.Objects;

class ClearTextCmd extends AbstractEditCmd {

    public void doRedo(RichTextAreaViewModel viewModel) {
        viewModel.setCaretPosition(0);
        viewModel.remove(0, viewModel.getTextLength());
    }

    public void doUndo(RichTextAreaViewModel viewModel) {
        Objects.requireNonNull(viewModel);
        viewModel.undo();
    }
}
