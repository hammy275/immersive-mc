package com.hammy275.immersivemc.client.immersive_item.info;

import net.minecraft.client.gui.screens.inventory.BookViewScreen;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.lwjgl.system.CallbackI;

import java.util.ArrayList;
import java.util.List;

public class WrittenBookInfo extends AbstractItemInfo {
    private int leftPageIndex = 0;
    private boolean pageChanged = true;
    public FormattedText left = FormattedText.EMPTY;
    public FormattedText right = FormattedText.EMPTY;
    // Index 0 - Left point
    // Index 1 - Right point
    // Index 2 - Center point
    // Indicies 3-5 - Above left, right, and center respectively
    public Vec3[] distancePoints = new Vec3[6];
    public PageChangeState pageChangeState = PageChangeState.NONE;
    public float leftPageTurn = 0f;
    public float rightPageTurn = 1f;
    public List<BookClickInfo> clickInfos = new ArrayList<>();
    public int selectedClickInfo = -1;

    private final int maxLeftPageIndex;

    public WrittenBookInfo(ItemStack item, InteractionHand hand) {
        super(item, hand);
        BookViewScreen.WrittenBookAccess access = new BookViewScreen.WrittenBookAccess(item);
        maxLeftPageIndex = access.getPageCount() % 2 == 0 ? access.getPageCount() - 2 : access.getPageCount() - 1;
    }

    public void nextPage() {
        if (leftPageIndex + 2 <= maxLeftPageIndex) {
            leftPageIndex += 2;
        }
        pageChanged = true;
    }

    public void lastPage() {
        if (leftPageIndex - 2 >= 0) {
            leftPageIndex -= 2;
        }
        pageChanged = true;
    }

    public void setPage(int newPageIndex) {
        if (newPageIndex % 2 != 0) {
            newPageIndex--;
        }
        if (newPageIndex > maxLeftPageIndex) {
            newPageIndex = maxLeftPageIndex;
        } else if (newPageIndex < 0) {
            newPageIndex = 0;
        }
        leftPageIndex = newPageIndex;
        pageChanged = true;
    }

    public boolean onFirstPage() {
        return leftPageIndex == 0;
    }

    public boolean onLastPage() {
        return leftPageIndex == maxLeftPageIndex;
    }

    public int getLeftPageIndex() {
        return this.leftPageIndex;
    }

    public int getRightPageIndex() {
        return getLeftPageIndex() + 1;
    }

    public void setPageChanged(boolean pageChanged) {
        this.pageChanged = pageChanged;
    }

    public boolean pageChanged() {
        return this.pageChanged;
    }

    public void addClickInfo(Vec3 pos, Style style) {
        for (BookClickInfo clickInfo : this.clickInfos) {
            if (clickInfo.style == style) {
                clickInfo.positions.add(pos);
                return;
            }
        }
        clickInfos.add(new BookClickInfo(style, pos));
    }

    public void clearClickInfoLists() {
        for (BookClickInfo clickInfo : this.clickInfos) {
            clickInfo.positions.clear();
        }
    }

    public enum PageChangeState {
        LEFT_TO_RIGHT(false), RIGHT_TO_LEFT(false), NONE(false),
        LEFT_TO_RIGHT_ANIM(true), RIGHT_TO_LEFT_ANIM(true);

        public final boolean isAnim;

        PageChangeState(boolean isAnim) {
            this.isAnim = isAnim;
        }
    }

    public static class BookClickInfo {
        public final List<Vec3> positions = new ArrayList<>();
        public final Style style;

        public BookClickInfo(Style style, Vec3 pos) {
            this.style = style;
            this.positions.add(pos);
        }
    }
}
