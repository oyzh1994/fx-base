//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package com.gluonhq.richtextarea.viewmodel;

import com.gluonhq.richtextarea.model.UnitBuffer;

class AppendTextCmd extends AbstractEditCmd {
    private final String content;

    public AppendTextCmd(String content) {
        this.content = content;
    }

    @Override
    public void doRedo(RichTextAreaViewModel viewModel) {
        if (this.content != null) {
            viewModel.getTextBuffer().insert(this.content, viewModel.getTextLength());
        }
    }

    @Override
    public void doUndo(RichTextAreaViewModel viewModel) {
        viewModel.undo();
    }
}
