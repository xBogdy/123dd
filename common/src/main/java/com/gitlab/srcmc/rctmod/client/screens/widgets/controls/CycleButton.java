/*
 * This file is part of Radical Cobblemon Trainers.
 * Copyright (c) 2025, HDainester, All rights reserved.
 *
 * Radical Cobblemon Trainers is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Radical Cobblemon Trainers is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for
 * more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along
 * with Radical Cobblemon Trainers. If not, see <http://www.gnu.org/licenses/lgpl>.
 */
package com.gitlab.srcmc.rctmod.client.screens.widgets.controls;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;

import net.minecraft.client.OptionInstance;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.Mth;

public class CycleButton<T> extends AbstractButton {
    private final Component name;
    private int index;
    private T value;
    private final List<T> values;
    private final Function<T, Component> valueStringifier;
    private final Function<CycleButton<T>, MutableComponent> narrationProvider;
    private final BiConsumer<CycleButton<T>, T> onValueChange;
    private final boolean displayOnlyValue;
    private final OptionInstance.TooltipSupplier<T> tooltipSupplier;

    public static <T> CycleButton<T> create(Function<T, Component> componentFunc, List<T> values, int selected, int x, int y, int w, int h, boolean onlyValue) {
        selected = values.size() > 0 ? Mth.positiveModulo(selected, values.size()) : -1;
        var value = values.size() > 0 ? values.get(selected) : null;
        var prefix = Component.empty();
        var component = values.size() > 0 ? createLabelForValue(value, prefix, onlyValue, componentFunc) : Component.empty();

        return new CycleButton<>(
            x, y, w, h,
            component,
            prefix,
            selected,
            value,
            values,
            componentFunc,
            c -> (MutableComponent) c.getMessage(),
            (c, v) -> {},
            v -> null,
            onlyValue);
    }

    CycleButton(int i, int j, int k, int l,
        Component component, Component component2,
        int m, T object, List<T> values,
        Function<T, Component> function,
        Function<CycleButton<T>, MutableComponent> function2,
        BiConsumer<CycleButton<T>, T> onValueChange,
        OptionInstance.TooltipSupplier<T> tooltipSupplier, boolean bl)
    {
        super(i, j, k, l, component);
        this.name = component2;
        this.index = m;
        this.value = object;
        this.values = values;
        this.valueStringifier = function;
        this.narrationProvider = function2;
        this.onValueChange = onValueChange;
        this.displayOnlyValue = bl;
        this.tooltipSupplier = tooltipSupplier;
        this.updateTooltip();
    }

    private void updateTooltip() {
        this.setTooltip(this.tooltipSupplier.apply(this.value));
    }

    public void onPress() {
        if (Screen.hasShiftDown()) {
            this.cycleValue(-1);
        } else {
            this.cycleValue(1);
        }
    }

    private void cycleValue(int i) {
        List<T> list = this.values;
        this.index = Mth.positiveModulo(this.index + i, list.size());
        T object = list.get(this.index);
        this.updateValue(object);
        this.onValueChange.accept(this, object);
    }

    private T getCycledValue(int i) {
        List<T> list = this.values;
        return list.get(Mth.positiveModulo(this.index + i, list.size()));
    }

    public boolean mouseScrolled(double d, double e, double f, double g) {
        if (g > 0.0) {
            this.cycleValue(-1);
        } else if (g < 0.0) {
            this.cycleValue(1);
        }

        return true;
    }

    public void setValue(T object) {
        List<T> list = this.values;
        int i = list.indexOf(object);
        if (i != -1) {
            this.index = i;
        }

        this.updateValue(object);
    }

    public boolean setValue(int i) {
        i = Math.min(i, this.values.size() - 1);

        if (i >= 0) {
            this.index = i;
            this.updateValue(this.values.get(i));
            return true;
        }

        return false;
    }

    public void addValue(T object) {
        this.values.add(object);
    }

    private void updateValue(T object) {
        Component component = this.createLabelForValue(object);
        this.setMessage(component);
        this.value = object;
        this.updateTooltip();
    }

    private Component createLabelForValue(T object) {
        return (Component) (this.displayOnlyValue ? (Component) this.valueStringifier.apply(object)
                : this.createFullName(object));
    }

    private MutableComponent createFullName(T object) {
        return CommonComponents.optionNameValue(this.name, (Component) this.valueStringifier.apply(object));
    }

    private static <T> Component createLabelForValue(T object, Component name, boolean displayOnlyValue, Function<T, Component> valueStringifier) {
        return (Component) (displayOnlyValue ? (Component) valueStringifier.apply(object)
                : createFullName(object, name, valueStringifier));
    }

    private static <T> MutableComponent createFullName(T object, Component name, Function<T, Component> valueStringifier) {
        return CommonComponents.optionNameValue(name, (Component) valueStringifier.apply(object));
    }

    public T getValue() {
        return this.value;
    }

    public int getIndex() {
        return this.index;
    }

    protected MutableComponent createNarrationMessage() {
        return (MutableComponent) this.narrationProvider.apply(this);
    }

    public void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
        narrationElementOutput.add(NarratedElementType.TITLE, this.createNarrationMessage());
        if (this.active) {
            T object = this.getCycledValue(1);
            Component component = this.createLabelForValue(object);

            if (this.isFocused()) {
                narrationElementOutput.add(NarratedElementType.USAGE,
                    Component.translatable("narration.cycle_button.usage.focused", new Object[] { component }));
            } else {
                narrationElementOutput.add(NarratedElementType.USAGE,
                    Component.translatable("narration.cycle_button.usage.hovered", new Object[] { component }));
            }
        }
    }

    public MutableComponent createDefaultNarrationMessage() {
        return wrapDefaultNarrationMessage(
            (Component) (this.displayOnlyValue ? this.createFullName(this.value) : this.getMessage()));
    }
}
