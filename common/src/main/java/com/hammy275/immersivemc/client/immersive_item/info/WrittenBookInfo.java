package com.hammy275.immersivemc.client.immersive_item.info;

import net.minecraft.client.gui.screens.inventory.BookViewScreen;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.lwjgl.system.CallbackI;

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

    public enum PageChangeState {
        LEFT_TO_RIGHT(false), RIGHT_TO_LEFT(false), NONE(false),
        LEFT_TO_RIGHT_ANIM(true), RIGHT_TO_LEFT_ANIM(true);

        public final boolean isAnim;

        PageChangeState(boolean isAnim) {
            this.isAnim = isAnim;
        }
    }
}
