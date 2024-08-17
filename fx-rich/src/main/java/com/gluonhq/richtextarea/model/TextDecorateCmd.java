package com.gluonhq.richtextarea.model;

import com.gluonhq.richtextarea.undo.AbstractCommand;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class TextDecorateCmd extends AbstractCommand<PieceTable> {

    private int start;
    private int end;
    private final Decoration decoration;

    private boolean execSuccess = false;
    private int pieceIndex = -1;
    private Collection<Piece> newPieces = new ArrayList<>();
    private Collection<Piece> oldPieces = new ArrayList<>();

    /**
     * Decorates the text within the given range with the supplied decoration.
     * @param start index of the first character to decorate
     * @param end index of the last character to decorate
     * @param decoration Decorations to apply on the selected text
     */
    TextDecorateCmd(int start, int end, Decoration decoration) {
        this.start = start;
        this.end = end;
        this.decoration = decoration;
        super.setDisableUndo(true);
    }

    @Override
    protected void doUndo(PieceTable pt) {
        if (execSuccess) {
            pt.pieces.addAll(pieceIndex, oldPieces);
            pt.pieces.removeAll(newPieces);

            oldPieces.forEach(piece -> {
                pt.fire(new TextBuffer.DecorateEvent(piece.start, piece.start + piece.length, piece.decoration));
            });
        }
    }

    @Override
    protected void doRedo(PieceTable pt) {
        if (!PieceTable.inRange(start, 0, pt.getTextLength())) {
            throw new IllegalArgumentException("Position " + start + " is outside of text bounds [0, " + pt.getTextLength() + ")");
        }

        //  Accept length larger than actual and adjust it to actual
        if (end >= pt.getTextLength()) {
            end = pt.getTextLength();
        }

        final int[] startPieceIndex = new int[1];
        final List<Piece> additions = new ArrayList<>(); // start and end pieces
        final List<Piece> removals = new ArrayList<>();

        pt.walkPieces((piece, pieceIndex, textPosition) -> {
            if (isPieceInSelection(piece, textPosition)) {
                startPieceIndex[0] = pieceIndex;
                if (textPosition <= start) {
                    int offset = start - textPosition;
                    int length;
                    if (textPosition + piece.length > end) {
                        length = Math.min(end - start, piece.length); // selection ends in current piece
                    } else {
                        length = piece.length - offset; // selection spans over next piece(s)
                    }
                    if (offset > 0) {
                        additions.add(piece.pieceBefore(offset));
                    }
                    additions.add(piece.copy(piece.start + offset, length, decoration));
                    if (end < textPosition + piece.length) {
                        additions.add(piece.pieceFrom(end - textPosition));
                    }
                    removals.add(piece);
                }  else if (textPosition + piece.length <= end) { // entire piece is in selection
                    additions.add(piece.copy(piece.start, piece.length, decoration));
                    removals.add(piece);
                } else if (textPosition < end) {
                    int offset = end - textPosition;
                    additions.add(piece.copy(piece.start, offset, decoration));
                    additions.add(piece.pieceFrom(offset));
                    removals.add(piece);
                }
            }
            return false;
        });

        newPieces = PieceTable.normalize(additions);
        oldPieces = removals;
        if (newPieces.size() > 0 || oldPieces.size() > 0) {
            pieceIndex = startPieceIndex[0];
            pt.pieces.addAll(pieceIndex, newPieces);
            pt.pieces.removeAll(oldPieces);
            pt.fire(new TextBuffer.DecorateEvent(start, end, decoration));
            execSuccess = true;
        }
    }

    private boolean isPieceInSelection(Piece piece, int textPosition) {
        int pieceEndPosition = textPosition + piece.length - 1;
        return start <= pieceEndPosition && (end >= pieceEndPosition || end >= textPosition);
    }

    @Override
    public String toString() {
        return "TextDecorateCmd[" + start +
                " x " + end + "]";
    }
}