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
package com.gitlab.srcmc.rctmod.client.screens.widgets.text;

import java.util.ArrayList;
import java.util.List;

import com.gitlab.srcmc.rctmod.client.screens.widgets.TrainerDataWidget;

import net.minecraft.Util;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;

public class MultiStyleStringWidget extends StringWidget {
   private static final double PI_HALF = Math.PI/2;
   private static final double PI_DOUBLE = 2*Math.PI;

   private final List<Component> styles = new ArrayList<>();
   private TrainerDataWidget owner;
   private boolean scrolling;
   private float alignX;

   public MultiStyleStringWidget(TrainerDataWidget owner, Component c, Font f) {
      super(c, f);
      this.styles.add(c);
      this.owner = owner;
      this.active = true;
   }

   public MultiStyleStringWidget(TrainerDataWidget owner, int x, int y, Component c, Font f) {
      super(x, y, c, f);
      this.styles.add(c);
      this.owner = owner;
      this.active = true;
   }

   public MultiStyleStringWidget(TrainerDataWidget owner, int x, int y, int w, int h, Component c, Font f) {
      super(x, y, w, h, c, f);
      this.styles.add(c);
      this.owner = owner;
      this.active = true;
   }

   public MultiStyleStringWidget addStyle(Style style) {
      this.styles.add(this.styles.get(0).copy().withStyle(style));
      return this;
   }

   public void setStyle(int i) {
      this.setMessage(this.styles.get(Math.max(0, Math.min(this.styles.size() - 1, i))));
   }

   public MultiStyleStringWidget scrolling() {
      return this.scrolling(true);
   }

   public MultiStyleStringWidget scrolling(boolean value) {
      this.scrolling = value;
      return this;
   }

   @Override
   public MultiStyleStringWidget alignRight() {
      super.alignRight();
      this.alignX = 1.0f;
      return this;
   }

   @Override
   public MultiStyleStringWidget alignLeft() {
      super.alignLeft();
      this.alignX = 0f;
      return this;
   }

   @Override
   public MultiStyleStringWidget alignCenter() {
      super.alignCenter();
      this.alignX = 0.5f;
      return this;
   }

   @Override
   public void renderWidget(GuiGraphics guiGraphics, int i, int j, float c) {
      var component = this.getMessage();
      var font = this.getFont();
      var w = this.getWidth();
      var tw = font.width(component);
      var x = this.getX() + Math.round(this.alignX * (w - tw));
      var h = this.getHeight();
      var y = this.getY() + (h - 9) / 2;

      if(tw > w) {
         if(this.scrolling && this.isHovered(guiGraphics, i, j)) {
            var r = tw - w;
            var d = Util.getMillis() / 1000.0;
            var e = Math.max(r * 0.5, 3.0);
            var f = Math.sin(PI_HALF * Math.cos(PI_DOUBLE * d / e)) / 2.0 + 0.5;
            var g = Mth.lerp(f, 0.0, r);

            var x1 = this.absX();
            var x2 = x1 + this.absWidth();
            var y1 = this.absY();
            var y2 = y1 + this.absHeight();

            guiGraphics.enableScissor(x1, y1, x2, y2);
            guiGraphics.drawString(font, component.getVisualOrderText(), x - (int)(g + 0.5), y, this.getColor());
            guiGraphics.disableScissor();
         } else {
            guiGraphics.drawString(font, clip(component, w), x, y, this.getColor());
         }
      } else {
         guiGraphics.drawString(font, component.getVisualOrderText(), x, y, this.getColor());
      }
   }

   protected int absX() {
      return (int)(this.owner.getX() + this.getX() * TrainerDataWidget.INNER_SCALE + 0.5f) + this.owner.getInnerPadding();
   }

   protected int absY() {
      return (int)(this.owner.getY() + this.getY() * TrainerDataWidget.INNER_SCALE - this.owner.getScrollAmount()) + this.owner.getInnerPadding();
   }

   protected int absWidth() {
      return (int)(this.getWidth() * TrainerDataWidget.INNER_SCALE /*+ 0.5f */);
   }

   protected int absHeight() {
      return this.getHeight();
   }

   public boolean isHovered(GuiGraphics guiGraphics, int i, int j) {
      return this.isMouseOver(this.owner.localX(i), this.owner.localY(j));
   }

   private FormattedCharSequence clip(Component component, int w) {
      var font = this.getFont();
      var formattedText = font.substrByWidth(component, w - font.width(CommonComponents.ELLIPSIS));
      return Language.getInstance().getVisualOrder(FormattedText.composite(new FormattedText[]{formattedText, CommonComponents.ELLIPSIS}));
   }
}
