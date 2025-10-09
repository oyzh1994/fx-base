package cn.oyzh.fx.rich.incubator;

/**
* @author oyzh
* @since 2025-05-30
*/
/*
* Copyright (c) 2023, 2025, Oracle and/or its affiliates. All rights reserved.
* DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
*
* This code is free software; you can redistribute it and/or modify it
* under the terms of the GNU General Public License version 2 only, as
* published by the Free Software Foundation.  Oracle designates this
* particular file as subject to the "Classpath" exception as provided
* by Oracle in the LICENSE file that accompanied this code.
*
* This code is distributed in the hope that it will be useful, but WITHOUT
* ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
* FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
* version 2 for more details (a copy is included in the LICENSE file that
* accompanied this code).
*
* You should have received a copy of the GNU General Public License version
* 2 along with this work; if not, write to the Free Software Foundation,
* Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
*
* Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
* or visit www.oracle.com if you need additional information or have any
* questions.
*/

import javafx.scene.AccessibleAttribute;
import jfx.incubator.scene.control.richtext.CodeArea;
import jfx.incubator.scene.control.richtext.RichTextArea;
import jfx.incubator.scene.control.richtext.TextPos;
import jfx.incubator.scene.control.richtext.model.CodeTextModel;
import jfx.incubator.scene.control.richtext.model.StyledTextModel;
import jfx.incubator.scene.control.richtext.skin.RichTextAreaSkin;

/**
* CodeArea is an editable text component which supports styling (for example, syntax highlighting) of plain text.
* <p>
* Unlike its base class {@link RichTextArea}, the {@code CodeArea} requires a special kind of model to be used,
* a {@link CodeTextModel}.
*
* <h2>Creating a CodeArea</h2>
* The following example creates an editable control with the default {@link FXRichTextArea1}:
* <pre>{@code    CodeArea codeArea = new CodeArea();
*   codeArea.setWrapText(true);
*   codeArea.setLineNumbersEnabled(true);
*   codeArea.setText("Lorem\nIpsum");
* }</pre>
* Which results in the following visual representation:
* <p>
* <img src="doc-files/CodeArea.png" alt="Image of the CodeArea control">
*
* <h2>Usage Considerations</h2>
* {@code CodeArea} extends the {@link RichTextArea} class, meaning most of the functionality works as it does
* in the base class.
* There are some differences that should be mentioned:
* <ul>
* <li>Model behavior: any direct changes to the styling, such as
* {@link #applyStyle(TextPos, TextPos, jfx.incubator.scene.control.richtext.model.StyleAttributeMap) applyStyle()},
* will be ignored
* <li>Line numbers: the {@code CodeArea} sets the {@link #leftDecoratorProperty()} to support the line numbers,
* so applications should not set or bind that property.
* </ul>
*
* @since 24
*/
@Deprecated
public class FXRichTextArea1 extends CodeArea {

   /**
    * This constructor creates the CodeArea with the specified {@link CodeTextModel}.
    * @param model the instance of {@link CodeTextModel} to use
    */
   public FXRichTextArea1(CodeTextModel model) {
       super(model);
   }

   /**
    * This constructor creates the CodeArea with the default {@link CodeTextModel}.
    */
   public FXRichTextArea1() {
       this(new CodeTextModel(){

           // @Override
           // protected Set<StyleAttribute<?>> getSupportedAttributes() {
           //     return Set.of(
           //             StyleAttributeMap.BOLD,
           //             StyleAttributeMap.ITALIC,
           //             StyleAttributeMap.STRIKE_THROUGH,
           //             StyleAttributeMap.UNDERLINE
           //     );
           // }

       });
   }

   @Override
   protected void validateModel(StyledTextModel m) {
       if ((m != null) && (!(m instanceof CodeTextModel))) {
           throw new IllegalArgumentException("CodeArea accepts models that extend CodeTextModel");
       }
   }

   @Override
   protected RichTextAreaSkin createDefaultSkin() {
       return new RichTextAreaSkin(this);
   }

   @Override
   public Object queryAccessibleAttribute(AccessibleAttribute attribute, Object... parameters) {
       switch (attribute) {
           case FONT:
               return getFont();
           default:
               return super.queryAccessibleAttribute(attribute, parameters);
       }
   }


}
