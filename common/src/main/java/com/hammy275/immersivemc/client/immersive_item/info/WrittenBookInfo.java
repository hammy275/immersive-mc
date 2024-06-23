package com.hammy275.immersivemc.client.immersive_item.info;

import com.hammy275.immersivemc.api.common.hitbox.BoundingBox;
import com.hammy275.immersivemc.api.common.hitbox.OBB;
import net.minecraft.client.gui.screens.inventory.BookViewScreen;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

public class WrittenBookInfo extends AbstractItemInfo {
    private int leftPageIndex = 0;
    public FormattedText left = FormattedText.EMPTY;
    public FormattedText right = FormattedText.EMPTY;
    // Indices 0-1: Left page start turn box and right page start turn boxes
    // Index 2: The "page progress" box. If the hand isn't in this box, the turn is cancelled.
    // If the hand is in the opposite one from when the turn started, the turn auto-completes.
    public OBB[] pageTurnBoxes = new OBB[3];
    // Index 0 is left center, index 1 is right center, index 2 is true center.
    public Vec3[] positions = new Vec3[3];
    public PageChangeState pageChangeState = PageChangeState.NONE;
    public float leftPageTurn = 0f;
    public float rightPageTurn = 1f;
    public List<BookClickInfo> clickInfos = new ArrayList<>();
    public int selectedClickInfo = -1;

    public WrittenBookInfo(ItemStack item, InteractionHand hand) {
        super(item, hand);
    }

    public void nextPage() {
        if (leftPageIndex + 2 <= maxLeftPageIndex()) {
            leftPageIndex += 2;
        }
    }

    public void lastPage() {
        if (leftPageIndex - 2 >= 0) {
            leftPageIndex -= 2;
        }
    }

    public void setPage(int newPageIndex) {
        if (newPageIndex % 2 != 0) {
            newPageIndex--;
        }
        if (newPageIndex > maxLeftPageIndex()) {
            newPageIndex = maxLeftPageIndex();
        } else if (newPageIndex < 0) {
            newPageIndex = 0;
        }
        leftPageIndex = newPageIndex;
    }

    public boolean onFirstPage() {
        return leftPageIndex == 0;
    }

    public boolean onLastPage() {
        return leftPageIndex == maxLeftPageIndex();
    }

    public int getLeftPageIndex() {
        return this.leftPageIndex;
    }

    public int getRightPageIndex() {
        return getLeftPageIndex() + 1;
    }

    public BoundingBox[] getClickBoxes() {
        if (this.clickInfos.isEmpty()) {
            return new BoundingBox[0];
        }
        List<BoundingBox> list = new ArrayList<>();
        this.clickInfos.forEach((clickInfo) -> list.add(clickInfo.obb));
        return list.toArray(new BoundingBox[0]);
    }

    // Note: Current behavior assumes you can't remove pages from the book.
    private int maxLeftPageIndex() {
        BookViewScreen.BookAccess access = BookViewScreen.BookAccess.fromItem(item);
        return access.getPageCount() % 2 == 0 ? access.getPageCount() - 2 : access.getPageCount() - 1;
    }

    public enum PageChangeState {
        LEFT_TO_RIGHT(false), RIGHT_TO_LEFT(false), NONE(false),
        LEFT_TO_RIGHT_ANIM(true), RIGHT_TO_LEFT_ANIM(true);

        public final boolean isAnim;

        PageChangeState(boolean isAnim) {
            this.isAnim = isAnim;
        }
    }

    public record BookClickInfo(OBB obb, Style style) {}
}
