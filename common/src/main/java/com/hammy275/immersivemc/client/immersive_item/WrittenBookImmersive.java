package com.hammy275.immersivemc.client.immersive_item;

import com.hammy275.immersivemc.ImmersiveMC;
import com.hammy275.immersivemc.api.client.ImmersiveRenderHelpers;
import com.hammy275.immersivemc.api.common.hitbox.OBB;
import com.hammy275.immersivemc.api.common.hitbox.OBBFactory;
import com.hammy275.immersivemc.client.ClientUtil;
import com.hammy275.immersivemc.client.immersive_item.info.WrittenBookInfo;
import com.hammy275.immersivemc.client.workaround.ClickHandlerScreen;
import com.hammy275.immersivemc.common.config.ActiveConfig;
import com.hammy275.immersivemc.common.obb.OBBClientUtil;
import com.hammy275.immersivemc.common.util.Util;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.util.Pair;
import com.mojang.math.Axis;
import net.blf02.vrapi.api.data.IVRData;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.inventory.BookViewScreen;
import net.minecraft.client.model.BookModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class WrittenBookImmersive extends AbstractItemImmersive<WrittenBookInfo> {
    /*
        Notes:
        - Book is 14 lines long, with 20 characters per line
        - Page turning is done using a central hitbox and edge hitboxes. Hand moves from edge to center to confirm turn.
        If it doesn't make it to center, cancel the turn.
     */

    public static final BookModel bookModel = new BookModel(Minecraft.getInstance().getEntityModels().bakeLayer(ModelLayers.BOOK));
    public static final ResourceLocation writtenBookTexture = new ResourceLocation(ImmersiveMC.MOD_ID, "nahnotfox_written_book.png");

    // User Controlled
    public static final float scaleSize = 1f;

    // Derived from user controlled
    public static final double singlePageWidth = scaleSize * 0.3d;
    public static final double pageHalfHeight = scaleSize / 4d;
    public static final float textStackScaleSize = -scaleSize * 0.0025f;
    public static final double textUpAmount = 0.1875 * (scaleSize / 2f);
    public static final double textInteractDistanceSqr = (textUpAmount * 1.2) * (textUpAmount * 1.2);

    // Helpful constants
    public static final float pageTilt = 11f;
    public static final int linesPerPage = 14;
    public static final int pixelsPerLine = 114;
    public static final double leftPageRot = Math.toRadians(15);

    /*
     * Pitch is 0 forward, with 30 up and -30 down
     * Yaw is just point direction
     */

    @Override
    protected void render(WrittenBookInfo info, PoseStack stack, IVRData hand) {
        stack.pushPose();

        Vec3 pos = hand.position();
        Camera cameraInfo = Minecraft.getInstance().gameRenderer.getMainCamera();
        stack.translate(-cameraInfo.getPosition().x + pos.x,
                -cameraInfo.getPosition().y + pos.y,
                -cameraInfo.getPosition().z + pos.z);

        stack.scale(scaleSize, scaleSize, scaleSize);

        stack.mulPose(Axis.YN.rotationDegrees(hand.getYaw() + 90f));
        stack.mulPose(Axis.ZP.rotationDegrees(90f));
        stack.mulPose(Axis.ZP.rotationDegrees(hand.getPitch()));

        float bookOpenAmount = 1.1f;

        bookModel.setupAnim(
                0, // Partial tick time is always 0 to have page stay in one constant spot
                info.leftPageTurn, // 0-1. How far the page is in the turn. Range is [0f, 1f] with 0f being left.
                info.rightPageTurn, // 0-1. How far across a different page is. Range is [0f, 1f] with 0f being left.
                bookOpenAmount // How open the book is. A good range seems to be (0f,1.2f]
        );
        bookModel.render(stack,
                Minecraft.getInstance().renderBuffers().bufferSource()
                        .getBuffer(RenderType.entitySolid(writtenBookTexture)),
                15728880, OverlayTexture.NO_OVERLAY,
                1, 1, 1, 1);

        stack.popPose();

        if (info.pageChangeState == WrittenBookInfo.PageChangeState.NONE) {
            renderPage(stack, hand, info.left, true);
            renderPage(stack, hand, info.right, false);
        }

        if (info.pageChangeState == WrittenBookInfo.PageChangeState.NONE) {
            for (int i = 0; i <= 1; i++) {
                OBBClientUtil.renderOBB(stack, info.pageTurnBoxes[i], false, 1f, 1f, 1f, 1f);
            }
        } else if (!info.pageChangeState.isAnim) {
            OBBClientUtil.renderOBB(stack, info.pageTurnBoxes[2], false, 1f, 1f, 1f, 1f);
        }

        for (WrittenBookInfo.BookClickInfo clickInfo : info.clickInfos) {
            ImmersiveRenderHelpers.instance().renderHitbox(stack, clickInfo.obb(), false, 0f, 0f, 1f, 1f);
        }
    }

    protected void renderPage(PoseStack stack, IVRData hand, FormattedText textRaw, boolean leftPage) {
        stack.pushPose();

        Vec3 awayFromBookUp = getAway(hand);

        Vec3 pageUp = hand.getLookAngle();
        Vec3 left = getLeftRight(hand, leftPage); // Should be called "right" for right page
        Vec3 pos = hand.position().add(pageUp.scale(pageHalfHeight)).add(left.scale(singlePageWidth / 2d))
                .add(awayFromBookUp.scale(textUpAmount));

        Camera cameraInfo = Minecraft.getInstance().gameRenderer.getMainCamera();
        stack.translate(-cameraInfo.getPosition().x + pos.x,
                -cameraInfo.getPosition().y + pos.y,
                -cameraInfo.getPosition().z + pos.z);
        stack.mulPose(Axis.YN.rotationDegrees(hand.getYaw() + 90f));
        stack.mulPose(Axis.ZP.rotationDegrees(hand.getPitch()));
        stack.mulPose(Axis.XP.rotationDegrees(90f + (leftPage ? pageTilt : -pageTilt)));
        stack.mulPose(Axis.ZP.rotationDegrees(270f));
        stack.scale(textStackScaleSize, textStackScaleSize, textStackScaleSize);

        Font font = Minecraft.getInstance().font;
        List<FormattedCharSequence> text = font.split(textRaw, 114);
        int lineNum = 0;
        for (FormattedCharSequence seq : text) {
            // -56f is used to make the text left-aligned.
            font.drawInBatch(seq, -56f, 32 + lineNum++ * 9, 0xFF000000, false,
                    stack.last().pose(), Minecraft.getInstance().renderBuffers().bufferSource(),
                    Font.DisplayMode.NORMAL, 0, ClientUtil.maxLight);
        }

        stack.popPose();
    }

    @Override
    protected void tick(WrittenBookInfo info, IVRData hand, IVRData other) {
        ItemStack book = info.item;

        // Get page contents. Can change at random, whether due to command blocks or due to editing for a book and quill
        BookViewScreen.BookAccess access = BookViewScreen.BookAccess.fromItem(book);
        info.left = access.getPage(info.getLeftPageIndex());
        info.right = access.getPage(info.getRightPageIndex());
        info.clickInfos.clear();

        Vec3 left = getLeftRight(hand, true);
        Vec3 right = getLeftRight(hand, false);
        Vec3 away = getAway(hand);

        info.positions[2] = hand.position().add(away.scale(textUpAmount)); // Center
        info.positions[0] = info.positions[2].add(left.scale(singlePageWidth * 1.25d)); // Left edge
        info.positions[1] = info.positions[2].add(right.scale(singlePageWidth * 1.25d)); // Right edge
        Vec3 upCenter = info.positions[2].add(away.scale(singlePageWidth * 0.5)); // Used for "continue page turning" boxes.

        double pitch = Math.toRadians(hand.getPitch());
        double yaw = Math.toRadians(hand.getYaw());

        // Boxes to start a page turn are a box on the page edge to generally capture the hand
        info.pageTurnBoxes[0] = OBBFactory.instance().create(AABB.ofSize(info.positions[0], 0.2, 0.2, pageHalfHeight * 2),
                pitch, yaw, 0);
        info.pageTurnBoxes[1] = OBBFactory.instance().create(AABB.ofSize(info.positions[1], 0.2, 0.2, pageHalfHeight * 2),
                pitch, yaw, 0);
        // Box to continue a page turn
        info.pageTurnBoxes[2] = OBBFactory.instance().create(AABB.ofSize(upCenter, singlePageWidth * 11d/3d, singlePageWidth * 2d, pageHalfHeight * 2.25),
                pitch, yaw, 0);

        if (info.pageChangeState == WrittenBookInfo.PageChangeState.NONE) {
            if (possiblyBeginPageTurn(other.position(), info.pageTurnBoxes[0]) && !info.onFirstPage()) {
                info.pageChangeState = WrittenBookInfo.PageChangeState.LEFT_TO_RIGHT;
            } else if (possiblyBeginPageTurn(other.position(), info.pageTurnBoxes[1]) && !info.onLastPage()) {
                info.pageChangeState = WrittenBookInfo.PageChangeState.RIGHT_TO_LEFT;
            }
        } else if (!info.pageChangeState.isAnim) {
            if (info.pageTurnBoxes[2].contains(other.position())) {
                boolean doingLToR = info.pageChangeState == WrittenBookInfo.PageChangeState.LEFT_TO_RIGHT;
                double distToLeft = other.position().distanceTo(info.positions[0]);
                double distToRight = other.position().distanceTo(info.positions[1]);
                if (doingLToR && distToRight < distToLeft) {
                    info.pageChangeState = WrittenBookInfo.PageChangeState.LEFT_TO_RIGHT_ANIM;
                    info.lastPage();
                } else if (!doingLToR && distToLeft < distToRight) {
                    info.pageChangeState = WrittenBookInfo.PageChangeState.RIGHT_TO_LEFT_ANIM;
                    info.nextPage();
                } else if (doingLToR) {
                    info.leftPageTurn = (float) (distToLeft / (distToLeft + distToRight));
                } else {
                    info.rightPageTurn = 1f - ((float) (distToRight / (distToLeft + distToRight)));
                }
            } else {
                resetTurnState(info);
            }
        }

        // Automatic page turning
        // Note that the next page/last page on the info is done as the animation starts, so the text is loaded
        // by the time we get to resetState here.
        if (info.pageChangeState == WrittenBookInfo.PageChangeState.LEFT_TO_RIGHT_ANIM) {
            info.leftPageTurn = Math.min(info.leftPageTurn + 0.05f, 1f);
            if (info.leftPageTurn == 1f) {
                resetTurnState(info);
            }
        } else if (info.pageChangeState == WrittenBookInfo.PageChangeState.RIGHT_TO_LEFT_ANIM) {
            info.rightPageTurn = Math.max(info.rightPageTurn - 0.05f, 0f);
            if (info.rightPageTurn == 0f) {
                resetTurnState(info);
            }
        }

        // Place positions for interacting with text. Note that these lists are cleared much earlier in tick()
        if (info.pageChangeState == WrittenBookInfo.PageChangeState.NONE) {
            setClickPositions(info, hand, other, true);
            setClickPositions(info, hand, other, false);
        }

        // Find nearest link to click
        info.selectedClickInfo = -1;
        for (int i = 0; i < info.clickInfos.size(); i++) {
            if (info.clickInfos.get(i).obb().contains(other.position())) {
                info.selectedClickInfo = i;
                break;
            }
        }

        // Attempt to trace to hitboxes if we aren't in one
        if (info.selectedClickInfo == -1) {
            Optional<Integer> hit = Util.rayTraceClosest(other.position(), other.position().add(other.getLookAngle()), info.getClickBoxes());
            info.selectedClickInfo = hit.orElse(-1);
        }

        // Indicator on currently selected click info
        if (info.selectedClickInfo > -1) {
            WrittenBookInfo.BookClickInfo clickInfo = info.clickInfos.get(info.selectedClickInfo);
            Vec3 particlePos = clickInfo.obb().getCenter();
            Minecraft.getInstance().player.level().addParticle(
                    new DustParticleOptions(new Vector3f(0f, 0f, 1f), 0.2f),
                    particlePos.x, particlePos.y, particlePos.z,
                    0, 0, 0
            );
        }
    }

    private void setClickPositions(WrittenBookInfo info, IVRData hand, IVRData other, boolean isLeft) {
        Vec3 pageUp = hand.getLookAngle();
        Vec3 pageDown = pageUp.scale(-1);
        Vec3 left = getLeftRight(hand, true);
        Vec3 right = getLeftRight(hand, false);
        Vec3 away = getAway(hand);

        // Makes pos be the very top left of the page text
        Vec3 leftStartMove = isLeft ? left.scale(singlePageWidth * 0.96) : Vec3.ZERO;
        Vec3 pos = hand.position().add(pageUp.scale(pageHalfHeight)).add(leftStartMove)
                .add(away.scale(textUpAmount)).add(pageDown.scale(9 * Math.abs(textStackScaleSize)));
        Font font = Minecraft.getInstance().font;
        List<FormattedCharSequence> text = font.split(isLeft ? info.left : info.right, 114);
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
                        addClickableStyle(stylePositions, styleForPositions, length, info, hand, isLeft);
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
            addClickableStyle(stylePositions, styleForPositions, length, info, hand, isLeft);
        }
    }

    private void addClickableStyle(List<Vec3> positions, Style style, double length, WrittenBookInfo info, IVRData hand, boolean isLeft) {
        if (style != null && style.getClickEvent() != null && !positions.isEmpty()) {
            Vec3 centerPos = getCenterPos(positions);
            info.clickInfos.add(new WrittenBookInfo.BookClickInfo(
                    OBBFactory.instance().create(AABB.ofSize(centerPos, length, 0.04 * scaleSize, 0.02 * scaleSize),
                            Math.toRadians(hand.getPitch()),
                            Math.toRadians(hand.getYaw()),
                            isLeft ? leftPageRot : -leftPageRot),
                    style));
        }
    }

    @Override
    public boolean itemMatches(ItemStack item) {
        return item.getItem() == Items.WRITTEN_BOOK || item.getItem() == Items.WRITABLE_BOOK;
    }

    @Override
    protected WrittenBookInfo createInfo(ItemStack item, InteractionHand hand) {
        return new WrittenBookInfo(item, hand);
    }

    @Override
    public boolean isEnabled() {
        return ActiveConfig.active().useWrittenBookImmersion;
    }

    @Override
    public boolean onLeftClick(WrittenBookInfo info, IVRData hand, IVRData other) {
        if (info.clickInfos.isEmpty()) {
            return false;
        }
        if (info.selectedClickInfo > -1) {
            WrittenBookInfo.BookClickInfo clickInfo = info.clickInfos.get(info.selectedClickInfo);
            ClickEvent clickEvent = clickInfo.style().getClickEvent();
            if (clickEvent != null) {
                String eventValue = clickEvent.getValue();
                if (clickEvent.getAction() == ClickEvent.Action.CHANGE_PAGE) {
                    try {
                        int newPageNum = Integer.parseInt(eventValue) - 1;
                        info.setPage(newPageNum);
                    } catch (Exception ignored) {}

                } else {
                    ClickHandlerScreen tempScreen = new ClickHandlerScreen();
                    Minecraft.getInstance().setScreen(tempScreen);
                    tempScreen.handleComponentClicked(clickInfo.style());
                }
                return true;
            }
        }
        return false;
    }

    private Vec3 getLeftRight(IVRData hand, boolean left) {
        Vec3 look = hand.getLookAngle();
        Vector3f leftF = new Vector3f((float) look.x(), 0, (float) look.z());
        leftF.normalize();
        leftF.rotate(Axis.YN.rotationDegrees(left ? 270 : 90));
        return new Vec3(leftF.x(), Math.abs(leftF.y()), leftF.z());
    }

    /**
     *
     * @param hand Hand data
     * @return The vector pointing away from the book. This is the opposite of the look vector of an HMD looking
     * directly at the book.
     */
    private Vec3 getAway(IVRData hand) {
        Vector3f awayFromBookF = new Vector3f(0, 1, 0);
        awayFromBookF.rotate(Axis.XN.rotationDegrees(hand.getPitch()));
        awayFromBookF.rotate(Axis.YN.rotationDegrees(hand.getYaw()));
        return new Vec3(awayFromBookF.x(), awayFromBookF.y(), awayFromBookF.z());
    }

    public boolean possiblyBeginPageTurn(Vec3 handPos, OBB startBox) {
        return startBox.contains(handPos);
    }

    private void resetTurnState(WrittenBookInfo info) {
        info.leftPageTurn = 0f;
        info.rightPageTurn = 1f;
        info.pageChangeState = WrittenBookInfo.PageChangeState.NONE;
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
}
