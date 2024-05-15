/*
 * Copyright (c) 2011, 2022, Oracle and/or its affiliates. All rights reserved.
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

package cn.oyzh.fx.plus.property;

import cn.oyzh.fx.plus.format.DigitalDecimalFormat;
import javafx.beans.property.ObjectPropertyBase;

import java.math.BigDecimal;

public class DigitalDecimalProperty extends ObjectPropertyBase<BigDecimal> {

    /**
     * 保留小数位数
     */
    private final int scaleLen;

    public DigitalDecimalProperty(int scaleLen) {
        this.scaleLen = scaleLen;
    }

    @Override
    public Object getBean() {
        return DigitalDecimalProperty.this;
    }

    public void setValue(Number n) {
        if (n != null) {
            super.setValue(this.format(n.doubleValue()));
        }
    }

    public void setValue(Double d) {
        if (d != null) {
            super.setValue(this.format(d));
        }
    }

    public Double getDouble() {
        BigDecimal decimal = this.get();
        if (decimal != null) {
            return decimal.doubleValue();
        }
        return null;
    }

    private BigDecimal format(double d) {
        String text = this.format().format(d);
        return new BigDecimal(text.replaceAll(",", ""));
    }

    private DigitalDecimalFormat format;

    private DigitalDecimalFormat format() {
        if (this.format == null) {
            this.format = new DigitalDecimalFormat(this.scaleLen);
        }
        return this.format;
    }

    @Override
    public String getName() {
        return "BigDecimal";
    }
}
