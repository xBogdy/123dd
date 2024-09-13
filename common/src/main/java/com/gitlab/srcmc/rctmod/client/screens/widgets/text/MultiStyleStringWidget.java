/*
 * This file is part of Radical Cobblemon Trainers.
 * Copyright (c) 2024, HDainester, All rights reserved.
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
package com.gitlab.srcmc.rctmod.client.screens.widgets.text;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;

public class MultiStyleStringWidget extends StringWidget {
   private final List<Component> styles = new ArrayList<>();

   public MultiStyleStringWidget(Component c, Font f) {
      super(c, f);
      this.styles.add(c);
   }

   public MultiStyleStringWidget(int x, int y, Component c, Font f) {
      super(x, y, c, f);
      this.styles.add(c);
   }

   public MultiStyleStringWidget(int x, int y, int w, int h, Component c, Font f) {
      super(x, y, w, h, c, f);
      this.styles.add(c);
   }

   public MultiStyleStringWidget addStyle(Style style) {
      this.styles.add(this.styles.get(0).copy().withStyle(style));
      return this;
   }

   public void setStyle(int i) {
      this.setMessage(this.styles.get(Math.max(0, Math.min(this.styles.size() - 1, i))));
   }

   public void renderScrolling(GuiGraphics g, Font f, int x, int y) {
      super.renderScrollingString(g, f, x, y);
   }

   @Override
   public MultiStyleStringWidget alignRight() {
      super.alignRight();
      return this;
   }

   @Override
   public MultiStyleStringWidget alignLeft() {
      super.alignLeft();
      return this;
   }

   @Override
   public MultiStyleStringWidget alignCenter() {
      super.alignCenter();
      return this;
   }
}
