package com.hammy275.immersivemc.client.immersive_item.info;

import com.hammy275.immersivemc.ImmersiveMC;
import com.hammy275.immersivemc.api.client.ImmersiveRenderHelpers;
import com.hammy275.immersivemc.api.common.hitbox.BoundingBox;
import com.hammy275.immersivemc.api.common.hitbox.OBB;
import com.hammy275.immersivemc.api.common.hitbox.OBBFactory;
import com.hammy275.immersivemc.client.workaround.ClickHandlerScreen;
import com.hammy275.immersivemc.common.immersive.storage.network.impl.BookData;
import com.hammy275.immersivemc.common.network.Network;
import com.hammy275.immersivemc.common.network.packet.PageTurnPacket;
import com.hammy275.immersivemc.common.obb.OBBClientUtil;
import com.hammy275.immersivemc.common.util.PageChangeState;
import com.hammy275.immersivemc.common.util.PosRot;
import com.hammy275.immersivemc.common.util.Util;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.util.Pair;
import com.mojang.math.Vector3f;
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
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ClientBookData extends BookData {
    public static final BookModel bookModel = new BookModel(Minecraft.getInstance().getEntityModels().bakeLayer(ModelLayers.BOOK));
    public static final ResourceLocation writtenBookTexture = new ResourceLocation(ImmersiveMC.MOD_ID, "nahnotfox_written_book.png");
    
    
    public FormattedText left = FormattedText.EMPTY;
    public FormattedText right = FormattedText.EMPTY;
    public List<BookClickInfo> clickInfos = new ArrayList<>();
    public int[] selectedClickInfos = new int[0];
    public ItemStack book;

    public ClientBookData(boolean authoritative) {
        this(authoritative, ItemStack.EMPTY);
    }

    public ClientBookData(boolean authoritative, ItemStack book) {
        super(authoritative);
        this.book = book;
        if (!authoritative) {
            pageTurner = Minecraft.getInstance().player;
        }
    }
    
    public void processFromNetwork(BookData data) {
        this.book = data.book;
        this.leftPageIndex = data.leftPageIndex;
        this.pageChangeState = data.pageChangeState;
        this.leftPageTurn = data.leftPageTurn;
        this.rightPageTurn = data.rightPageTurn;
    }

    public BoundingBox[] getClickBoxes() {
        if (this.clickInfos.isEmpty()) {
            return new BoundingBox[0];
        }
        List<BoundingBox> list = new ArrayList<>();
        this.clickInfos.forEach((clickInfo) -> list.add(clickInfo.obb));
        return list.toArray(new BoundingBox[0]);
    }

    @Override
    protected int getPageCount() {
        if (book.isEmpty()) return 0;
        return BookViewScreen.BookAccess.fromItem(book).getPageCount();
    }

    /*
     * Pitch is 0 forward, with 30 up and -30 down
     * Yaw is just point direction
     */
    public void render(PoseStack stack, PosRot hand, int light) {
        stack.pushPose();

        Vec3 pos = hand.position();
        Camera cameraInfo = Minecraft.getInstance().gameRenderer.getMainCamera();
        stack.translate(-cameraInfo.getPosition().x + pos.x,
                -cameraInfo.getPosition().y + pos.y,
                -cameraInfo.getPosition().z + pos.z);

        stack.scale(scaleSize, scaleSize, scaleSize);

        stack.mulPose(Vector3f.YN.rotationDegrees(hand.getYaw() + 90f));
        stack.mulPose(Vector3f.ZP.rotationDegrees(90f));
        stack.mulPose(Vector3f.ZP.rotationDegrees(hand.getPitch()));
        stack.mulPose(Vector3f.YN.rotationDegrees(hand.getRoll()));

        float bookOpenAmount = 1.1f;

        bookModel.setupAnim(
                0, // Partial tick time is always 0 to have page stay in one constant spot
                leftPageTurn, // 0-1. How far the page is in the turn. Range is [0f, 1f] with 0f being left.
                rightPageTurn, // 0-1. How far across a different page is. Range is [0f, 1f] with 0f being left.
                bookOpenAmount // How open the book is. A good range seems to be (0f,1.2f]
        );
        bookModel.render(stack,
                Minecraft.getInstance().renderBuffers().bufferSource()
                        .getBuffer(RenderType.entitySolid(writtenBookTexture)),
                light, OverlayTexture.NO_OVERLAY,
                1, 1, 1, 1);

        stack.popPose();

        if (pageChangeState == PageChangeState.NONE) {
            renderPage(stack, hand, left, true, light);
            renderPage(stack, hand, right, false, light);
        }

        if (pageChangeState == PageChangeState.NONE) {
            for (int i = 0; i <= 1; i++) {
                OBBClientUtil.renderOBB(stack, pageTurnBoxes[i], false, 1f, 1f, 1f, 1f);
            }
        } else if (!pageChangeState.isAnim) {
            OBBClientUtil.renderOBB(stack, pageTurnBoxes[2], false, 1f, 1f, 1f, 1f);
        }

        for (ClientBookData.BookClickInfo clickInfo : clickInfos) {
            ImmersiveRenderHelpers.instance().renderHitbox(stack, clickInfo.obb(), false, 0f, 0f, 1f, 1f);
        }
    }

    protected void renderPage(PoseStack stack, PosRot hand, FormattedText textRaw, boolean leftPage, int light) {
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
        stack.mulPose(Vector3f.YN.rotationDegrees(hand.getYaw() + 90f));
        stack.mulPose(Vector3f.ZP.rotationDegrees(hand.getPitch()));
        stack.mulPose(Vector3f.XP.rotationDegrees(90f + (leftPage ? pageTilt : -pageTilt)));
        stack.mulPose(Vector3f.ZP.rotationDegrees(270f));
        stack.mulPose(Vector3f.YP.rotationDegrees(hand.getRoll()));
        stack.scale(textStackScaleSize, textStackScaleSize, textStackScaleSize);

        Font font = Minecraft.getInstance().font;
        List<FormattedCharSequence> text = font.split(textRaw, 114);
        int lineNum = 0;
        for (FormattedCharSequence seq : text) {
            // -56f is used to make the text left-aligned.
            font.drawInBatch(seq, -56f, 32 + lineNum++ * 9, 0xFF000000, false,
                    stack.last().pose(), Minecraft.getInstance().renderBuffers().bufferSource(),
                    false, 0, light);
        }

        stack.popPose();
    }

    @Override
    public void tick(PosRot hand, PosRot... others) {
        super.tick(hand, others);
        // Get page contents. Can change at random, whether due to command blocks or due to editing for a book and quill
        BookViewScreen.BookAccess access = BookViewScreen.BookAccess.fromItem(book);
        left = access.getPage(getLeftPageIndex());
        right = access.getPage(getRightPageIndex());
        clickInfos.clear();

        // Place positions for interacting with text. Note that these lists are cleared earlier in tick()
        if (pageChangeState == PageChangeState.NONE) {
            setClickPositions(hand, true);
            setClickPositions(hand, false);
        }

        if (others.length < 2 && pageTurnerIndex != -1) { // Player switched out of VR while page turning
            return;
        }
        selectedClickInfos = new int[others.length];
        // If a hand is turning the page, only run code for it
        int start = pageTurnerIndex == -1 ? 0 : pageTurnerIndex;
        int end = pageTurnerIndex == -1 ? others.length : pageTurnerIndex + 1;
        for (int i = start; i < end; i++) {
            PosRot other = others[i];

            // Find nearest link to click
            selectedClickInfos[i] = -1;
            for (int j = 0; j < clickInfos.size(); j++) {
                if (clickInfos.get(j).obb().contains(other.position())) {
                    selectedClickInfos[i] = j;
                    break;
                }
            }

            // Attempt to trace to hitboxes if we aren't in one
            if (selectedClickInfos[i] == -1) {
                Optional<Integer> hit = Util.rayTraceClosest(other.position(), other.position().add(other.getLookAngle()), getClickBoxes());
                selectedClickInfos[i] = hit.orElse(-1);
            }

            // Indicator on currently selected click info
            if (selectedClickInfos[i] > -1) {
                ClientBookData.BookClickInfo clickInfo = clickInfos.get(selectedClickInfos[i]);
                Vec3 particlePos = clickInfo.obb().getCenter();
                Minecraft.getInstance().player.level.addParticle(
                        new DustParticleOptions(new Vector3f(0f, 0f, 1f), 0.2f),
                        particlePos.x, particlePos.y, particlePos.z,
                        0, 0, 0
                );
            }
        }
    }

    public boolean doPageInteract(int selectedIndex) {
        if (clickInfos.isEmpty()) {
            return false;
        }
        if (selectedClickInfos[selectedIndex] > -1) {
            doPageInteract(clickInfos.get(selectedClickInfos[selectedIndex]));
            return true;
        }
        return false;
    }

    public void doPageInteract(BookClickInfo clickInfo) {
        ClickEvent clickEvent = clickInfo.style().getClickEvent();
        if (clickEvent != null) {
            String eventValue = clickEvent.getValue();
            if (clickEvent.getAction() == ClickEvent.Action.CHANGE_PAGE) {
                try {
                    int newPageNum = Integer.parseInt(eventValue) - 1;
                    if (authoritative) {
                        setPage(newPageNum);
                    } else {
                        Network.INSTANCE.sendToServer(new PageTurnPacket(pos, newPageNum));
                    }
                } catch (Exception ignored) {}
            } else {
                ClickHandlerScreen tempScreen = new ClickHandlerScreen();
                Minecraft.getInstance().setScreen(tempScreen);
                tempScreen.handleComponentClicked(clickInfo.style());
            }
        }
    }

    protected void setClickPositions(PosRot hand, boolean isLeft) {
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
        List<FormattedCharSequence> text = font.split(isLeft ? this.left : this.right, 114);
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
                        addClickableStyle(stylePositions, styleForPositions, length, hand, isLeft);
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
            addClickableStyle(stylePositions, styleForPositions, length, hand, isLeft);
        }
    }

    protected void addClickableStyle(List<Vec3> positions, Style style, double length, PosRot hand, boolean isLeft) {
        if (style != null && style.getClickEvent() != null && !positions.isEmpty()) {
            Vec3 centerPos = getCenterPos(positions);
            clickInfos.add(new ClientBookData.BookClickInfo(
                    OBBFactory.instance().create(AABB.ofSize(centerPos, length, 0.04 * scaleSize, 0.02 * scaleSize),
                            Math.toRadians(hand.getPitch()),
                            Math.toRadians(hand.getYaw()),
                            (isLeft ? leftPageRot : -leftPageRot) + Math.toRadians(hand.getRoll())),
                    style));
        }
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

    public record BookClickInfo(OBB obb, Style style) {}
}
