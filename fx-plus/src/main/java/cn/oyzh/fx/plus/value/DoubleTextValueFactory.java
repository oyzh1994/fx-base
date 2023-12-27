package cn.oyzh.fx.plus.value;

import cn.oyzh.fx.plus.util.TextUtil;
import javafx.beans.NamedArg;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.control.SpinnerValueFactory;
import javafx.util.StringConverter;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.ParseException;

public class DoubleTextValueFactory extends SpinnerValueFactory<Double> {
    private DoubleProperty min;
    private DoubleProperty max;
    private DoubleProperty amountToStepBy;

    public DoubleTextValueFactory() {
    }


    public DoubleTextValueFactory(@NamedArg("min") double var1, @NamedArg("max") double var3) {
        this(var1, var3, var1);
    }

    public DoubleTextValueFactory(@NamedArg("min") double var1, @NamedArg("max") double var3, @NamedArg("initialValue") double var5) {
        this(var1, var3, var5, 1.0);
    }

    public DoubleTextValueFactory(@NamedArg("min") double var1, @NamedArg("max") double var3, @NamedArg("initialValue") double var5, @NamedArg("amountToStepBy") double var7) {
        this.min = new SimpleDoubleProperty(this, "min") {
            protected void invalidated() {
                Double var1 = (Double) this.getValue();
                if (var1 != null) {
                    double var2 = this.get();
                    if (var2 > getMax()) {
                        setMin(getMax());
                    } else {
                        if (var1 < var2) {
                            this.setValue(var2);
                        }

                    }
                }
            }
        };
        this.max = new SimpleDoubleProperty(this, "max") {
            protected void invalidated() {
                Double var1 = this.getValue();
                if (var1 != null) {
                    double var2 = this.get();
                    if (var2 < getMin()) {
                        setMax(getMin());
                    } else {
                        if (var1 > var2) {
                            this.setValue(var2);
                        }
                    }
                }
            }
        };
        this.amountToStepBy = new SimpleDoubleProperty(this, "amountToStepBy");
        this.setMin(var1);
        this.setMax(var3);
        this.setAmountToStepBy(var7);
        this.setConverter(new StringConverter<Double>() {
            private final DecimalFormat df = new DecimalFormat("#.##");

            public String toString(Double var1) {
                return var1 == null ? "" : this.df.format(var1);
            }

            public Double fromString(String var1) {
                try {
                    if (var1 == null) {
                        return null;
                    } else {
                        var1 = var1.trim();
                        return var1.length() < 1 ? null : this.df.parse(var1).doubleValue();
                    }
                } catch (ParseException var3) {
                    throw new RuntimeException(var3);
                }
            }
        });
        this.valueProperty().addListener((var1x, var2, var3x) -> {
            if (var3x != null) {
                if (var3x < this.getMin()) {
                    this.setValue(this.getMin());
                } else if (var3x > this.getMax()) {
                    this.setValue(this.getMax());
                }

            }
        });
        this.setValue(var5 >= var1 && var5 <= var3 ? var5 : var1);
    }

    public final void setMin(double var1) {
        this.min.set(var1);
    }

    public final double getMin() {
        return this.min.get();
    }

    public final DoubleProperty minProperty() {
        return this.min;
    }

    public final void setMax(double var1) {
        this.max.set(var1);
    }

    public final double getMax() {
        return this.max.get();
    }

    public final DoubleProperty maxProperty() {
        return this.max;
    }

    public final void setAmountToStepBy(double var1) {
        this.amountToStepBy.set(var1);
    }

    public final double getAmountToStepBy() {
        return this.amountToStepBy.get();
    }

    public final DoubleProperty amountToStepByProperty() {
        return this.amountToStepBy;
    }

    public void decrement(int var1) {
        BigDecimal var2 = BigDecimal.valueOf(this.getValue());
        BigDecimal var3 = BigDecimal.valueOf(this.getMin());
        BigDecimal var4 = BigDecimal.valueOf(this.getMax());
        BigDecimal var5 = BigDecimal.valueOf(this.getAmountToStepBy());
        BigDecimal var6 = var2.subtract(var5.multiply(BigDecimal.valueOf(var1)));
        this.setValue(var6.compareTo(var3) >= 0 ? var6.doubleValue() : (this.isWrapAround() ? TextUtil.wrapValue(var6, var3, var4).doubleValue() : this.getMin()));
    }

    public void increment(int var1) {
        BigDecimal var2 = BigDecimal.valueOf(this.getValue());
        BigDecimal var3 = BigDecimal.valueOf(this.getMin());
        BigDecimal var4 = BigDecimal.valueOf(this.getMax());
        BigDecimal var5 = BigDecimal.valueOf(this.getAmountToStepBy());
        BigDecimal var6 = var2.add(var5.multiply(BigDecimal.valueOf(var1)));
        this.setValue(var6.compareTo(var4) <= 0 ? var6.doubleValue() : (this.isWrapAround() ? TextUtil.wrapValue(var6, var3, var4).doubleValue() : this.getMax()));
    }
}