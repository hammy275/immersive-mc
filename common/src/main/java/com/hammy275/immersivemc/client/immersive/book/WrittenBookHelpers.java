package com.hammy275.immersivemc.client.immersive.book;

import com.hammy275.immersivemc.api.common.hitbox.OBB;
import com.hammy275.immersivemc.api.common.hitbox.OBBFactory;
import com.hammy275.immersivemc.client.workaround.ClickHandlerScreen;
import com.hammy275.immersivemc.common.util.PageChangeState;
import com.hammy275.immersivemc.common.util.PosRot;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.util.Pair;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.inventory.BookViewScreen;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import static com.hammy275.immersivemc.common.immersive.CommonBookData.leftPageRot;
import static com.hammy275.immersivemc.common.immersive.CommonBookData.pageHalfHeight;
import static com.hammy275.immersivemc.common.immersive.CommonBookData.pixelsPerLine;
import static com.hammy275.immersivemc.common.immersive.CommonBookData.scaleSize;
import static com.hammy275.immersivemc.common.immersive.CommonBookData.singlePageWidth;
import static com.hammy275.immersivemc.common.immersive.CommonBookData.textStackScaleSize;
import static com.hammy275.immersivemc.common.immersive.CommonBookData.textUpAmount;

/**
 * Some helpers used with both the written book Immersive and the lectern Immersive.
 */
public class WrittenBookHelpers {

    /**
     * Make a {@link ClientBookData} with included renderable for book text.
     * @param dataHolder Something that has access to the book data being created here, a book, and a callback for
     *                   handling page changes via book text clicking.
     * @return A {@link ClientBookData} that should be returned by {@link WrittenBookDataHolder#getData()}.
     */
    public static ClientBookData makeClientBookData(WrittenBookDataHolder dataHolder) {
        ClientBookData bookData = new ClientBookData();
        BookViewScreen.BookAccess access = BookViewScreen.BookAccess.fromItem(dataHolder.getBook());
        bookData.renderables.add(new BookTextRenderer(isLeftPage -> {
            int rightMod = isLeftPage ? 0 : 1;
            return access.getPage(dataHolder.getData().getLeftPageIndex() + rightMod);
        }, textStackScaleSize, new Vec3(0, 1, 0)));
        bookData.setMaxLeftPageIndex(Math.max(access.getPageCount() - 1, 0));
        return bookData;
    }

    /**
     * Add interactables for text interactions for this tick. The interactables should be cleared before calling this.
     * @param dataHolder Something that has access to the book data being created here, a book, and a callback for
     *                   handling page changes via book text clicking.
     * @param bookPosRot The PosRot of the book.
     * @param isLeft Whether to add interactables for the left page or right page.
     */
    public static void addInteractablesForThisTick(WrittenBookDataHolder dataHolder, PosRot bookPosRot, boolean isLeft) {
        if (dataHolder.getData().getPageChangeState() != PageChangeState.NONE) return;
        BookViewScreen.BookAccess access = BookViewScreen.BookAccess.fromItem(dataHolder.getBook());
        if (access == null) return;
        ClientBookData data = dataHolder.getData();
        Vec3 pageUp = bookPosRot.getDir();
        Vec3 pageDown = pageUp.scale(-1);
        Vec3 left = data.getLeftRightVector(bookPosRot, true);
        Vec3 right = data.getLeftRightVector(bookPosRot, false);
        Vec3 away = data.getAwayVector(bookPosRot);

        // Makes pos be the very top left of the page text
        Vec3 leftStartMove = isLeft ? left.scale(singlePageWidth * 0.96) : Vec3.ZERO;
        Vec3 pos = bookPosRot.getPos().add(pageUp.scale(pageHalfHeight)).add(leftStartMove)
                .add(away.scale(textUpAmount)).add(pageDown.scale(9 * Math.abs(textStackScaleSize)));
        Font font = Minecraft.getInstance().font;
        int rightMod = isLeft ? 0 : 1;
        FormattedText rawText = access.getPage(dataHolder.getData().getLeftPageIndex() + rightMod);
        List<FormattedCharSequence> text = font.split(rawText, 114);
        for (int lineNum = 0; lineNum < text.size(); lineNum++) {
            // Set leftPos to be down the page to now be the center left of the line
            Vec3 leftPos = pos.add(pageDown.scale((26 + lineNum * (Minecraft.getInstance().font.lineHeight))
                    * Math.abs(textStackScaleSize)));
            List<Pair<String, Style>> chars = new ArrayList<>();
            // Iterates through each character on the line and adds it to chars
            text.get(lineNum).accept((charIndex, style, codePoint) -> {
                chars.add(new Pair<>(new StringBuilder().appendCodePoint(codePoint).toString(), style));
                return true; // Return true to move to next char always
            });
            double pixelsMoved = 0;
            Style styleForPositions = null;
            List<Vec3> stylePositions = new ArrayList<>();
            double length = 0;
            for (Pair<String, Style> c : chars) {
                String str = c.getFirst();
                Style style = c.getSecond();
                // Move halfway into the char, make a hitbox, then move the other half for the next char
                pixelsMoved += font.width(str) / 2d;
                double halfCharWidth = (font.width(str) / 2d) * Math.abs(textStackScaleSize);
                leftPos = leftPos.add(right.scale(halfCharWidth));
                if (style.getClickEvent() != null) {
                    // Set placePos to where the next character will be placed
                    Vec3 placePos;
                    double pixelMovedRatio = pixelsMoved / pixelsPerLine;
                    if (pixelMovedRatio < 0.5 && isLeft || pixelMovedRatio > 0.5 && !isLeft) {
                        if (pixelMovedRatio > 0.5) {
                            pixelMovedRatio -= 0.5;
                        } else {
                            pixelMovedRatio = 0.5 - pixelMovedRatio;
                        }
                        placePos = leftPos.add(away.scale(textUpAmount).scale(pixelMovedRatio / 2d));
                    } else {
                        if (pixelMovedRatio > 0.5) {
                            pixelMovedRatio -= 0.5;
                        } else {
                            pixelMovedRatio = 0.5 - pixelMovedRatio;
                        }
                        placePos = leftPos.add(away.scale(textUpAmount).scale(-pixelMovedRatio / 2d));
                    }

                    // If this character has a different style than the last, save the old style, assuming it
                    // has a click event, into the list of clickable objects, then start building the new one.
                    if (style != styleForPositions) {
                        // Save current style (click event) then start tracking the new one
                        BookInteractable interactable = makeInteractable(dataHolder, stylePositions, styleForPositions, length, bookPosRot, isLeft);
                        if (interactable != null) {
                            data.interactables.add(interactable);
                        }
                        styleForPositions = style;
                        stylePositions.clear();
                    } else { // Else, add this to the list, and continue tracking the length
                        stylePositions.add(placePos);
                        length += halfCharWidth * 2;
                    }
                }
                // Move forward the pixels
                pixelsMoved += font.width(str) / 2d;
                leftPos = leftPos.add(right.scale(halfCharWidth));
            }
            // After loop, add this as a clickable style in case if it is.
            BookInteractable interactable = makeInteractable(dataHolder, stylePositions, styleForPositions, length, bookPosRot, isLeft);
            if (interactable != null) {
                data.interactables.add(interactable);
            }
        }
    }

    @Nullable
    private static BookInteractable makeInteractable(WrittenBookDataHolder holder, List<Vec3> positions, Style style, double length, PosRot hand, boolean isLeft) {
        if (style != null && style.getClickEvent() != null && !positions.isEmpty()) {
            Vec3 centerPos = getCenterPos(positions);
            return new BookStyleInteractable(holder,
                    OBBFactory.instance().create(AABB.ofSize(centerPos, length, 0.04 * scaleSize, 0.02 * scaleSize),
                    hand.getPitch(),
                    hand.getYaw(),
                    (isLeft ? leftPageRot : -leftPageRot) + hand.getRoll()),
                    style);
        }
        return null;
    }

    private static Vec3 getCenterPos(List<Vec3> positions) {
        int size = positions.size();
        if (size % 2 == 0) {
            // Average two center positions
            Vec3 a = positions.get(size / 2);
            Vec3 b = positions.get(size / 2 - 1);
            return a.add(b).scale(0.5);
        } else {
            return positions.get(size / 2); // Just get the center
        }
    }

    public record BookTextRenderer(Function<Boolean, FormattedText> textSupplier, float textStackScaleSize, Vec3 offset) implements BookRenderable {

        @Override
        public void render(PoseStack stack, ClientBookData data, boolean leftPage, int light, PosRot bookPosRot) {
            stack.scale(textStackScaleSize, textStackScaleSize, textStackScaleSize);
            Font font = Minecraft.getInstance().font;
            FormattedText textRaw = textSupplier.apply(leftPage);
            if (textRaw == null) return;
            List<FormattedCharSequence> text = font.split(textRaw, 114);
            int lineNum = 0;
            for (FormattedCharSequence seq : text) {
                // -56f is used to make the text left-aligned.
                font.drawInBatch(seq, -56f, 32 + lineNum++ * 9, 0xFF000000, false,
                        stack.last().pose(), Minecraft.getInstance().renderBuffers().bufferSource(),
                        Font.DisplayMode.NORMAL, 0, light);
            }
        }

        @Override
        public Vec3 getStartOffset(ClientBookData data, boolean leftPage, PosRot bookPosRot) {
            return offset;
        }
    }

    private record BookStyleInteractable(WrittenBookDataHolder holder, OBB obb, Style style) implements BookInteractable {

        @Override
        public OBB getOBB() {
            return this.obb;
        }

        @Override
        public void hover(ClientBookData data, PosRot bookPosRot, PosRot other) {
            Vec3 pos = obb.getCenter();
            Minecraft.getInstance().player.level().addParticle(
                    new DustParticleOptions(0x0000FF, 0.2f),
                    pos.x, pos.y, pos.z, 0, 0, 0
            );
        }

        @Override
        public void interact(ClientBookData data, PosRot bookPosRot, PosRot other) {
            ClickEvent clickEvent = style.getClickEvent();
            if (clickEvent != null) {
                if (clickEvent.action() == ClickEvent.Action.CHANGE_PAGE) {
                    try {
                        int newPageNum = ((ClickEvent.ChangePage) clickEvent).page() - 1;
                        data.setPage(newPageNum);
                        holder.onPageChangeStyleClick(newPageNum);
                    } catch (Exception ignored) {}
                } else {
                    ClickHandlerScreen tempScreen = new ClickHandlerScreen();
                    Minecraft.getInstance().setScreen(tempScreen);
                    tempScreen.handleComponentClicked(style);
                }
            }
        }
    }
}
