package com.hammy275.immersivemc.common.immersive;

import com.hammy275.immersivemc.api.common.hitbox.OBB;
import com.hammy275.immersivemc.api.common.hitbox.OBBFactory;
import com.hammy275.immersivemc.api.common.immersive.NetworkStorage;
import com.hammy275.immersivemc.common.util.PageChangeState;
import com.hammy275.immersivemc.common.util.PosRot;
import com.mojang.math.Axis;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import java.util.function.Consumer;

/**
 * Data used by both the client and server (if the server is needed) for a book that can be used in Immersives.
 * Handles being a book and page turning.
 */
public class CommonBookData implements NetworkStorage {

    // Constants
    /*
        Notes:
        - Book is 14 lines long, with 20 characters per line
        - Page turning is done using a central hitbox and edge hitboxes. Hand moves from edge to center to confirm turn.
        If it doesn't make it to center, cancel the turn.
     */
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
    
    // Settings
    public Consumer<CommonBookData> startVRPageTurnCallback = null;
    public Consumer<Integer> onSetPageCallback = null;
    protected int maxLeftPageIndex = 0;

    // State
    protected boolean isDirty = false;
    protected OBB[] pageTurnBoxes = new OBB[3];
    protected Vec3[] pageTurnPositions = new Vec3[3]; // Index 0 is left center, index 1 is right center, index 2 is true center.
    public Player pageTurner;
    protected int pageTurnerHandIndex = -1;
    // Synced State
    public int leftPageIndex = 0;
    public float leftPageTurn = 0f;
    public float rightPageTurn = 1f;
    public PageChangeState pageChangeState = PageChangeState.NONE;
    
    public CommonBookData() {
    }

    /**
     * Should be called every tick.
     * @param bookPosRot PosRot of the book itself.
     * @param others PosRots for things that can interact with the book (VR controllers).
     */
    public void tick(PosRot bookPosRot, PosRot... others) {
        if (maxLeftPageIndex < 0) return;
        Vec3 left = getLeftRightVector(bookPosRot, true);
        Vec3 right = getLeftRightVector(bookPosRot, false);
        Vec3 away = getAwayVector(bookPosRot);

        pageTurnPositions[2] = bookPosRot.getPos().add(away.scale(textUpAmount)); // Center
        pageTurnPositions[0] = pageTurnPositions[2].add(left.scale(singlePageWidth * 1.25d)); // Left edge
        pageTurnPositions[1] = pageTurnPositions[2].add(right.scale(singlePageWidth * 1.25d)); // Right edge
        Vec3 upCenter = pageTurnPositions[2].add(away.scale(singlePageWidth * 0.5)); // Used for "continue page turning" boxes.

        double pitch = bookPosRot.getPitch();
        double yaw = bookPosRot.getYaw();

        // Boxes to start a page turn are a box on the page edge to generally capture the hand
        pageTurnBoxes[0] = OBBFactory.instance().create(AABB.ofSize(pageTurnPositions[0], 0.2, 0.2, pageHalfHeight * 2),
                pitch, yaw, bookPosRot.getRoll());
        pageTurnBoxes[1] = OBBFactory.instance().create(AABB.ofSize(pageTurnPositions[1], 0.2, 0.2, pageHalfHeight * 2),
                pitch, yaw, bookPosRot.getRoll());
        // Box to continue a page turn
        pageTurnBoxes[2] = OBBFactory.instance().create(AABB.ofSize(upCenter, singlePageWidth * 11d/3d, singlePageWidth * 2d, pageHalfHeight * 2.25),
                pitch, yaw, bookPosRot.getRoll());

        // Automatic page turning
        // Note that the next page/last page on the info is done as the animation starts, so the text is loaded
        // by the time we get to resetState here.
        if (pageChangeState == PageChangeState.LEFT_TO_RIGHT_ANIM) {
            leftPageTurn = Math.min(leftPageTurn + 0.05f, 1f);
            if (leftPageTurn == 1f) {
                resetTurnState();
                return;
            }
        } else if (pageChangeState == PageChangeState.RIGHT_TO_LEFT_ANIM) {
            rightPageTurn = Math.max(rightPageTurn - 0.05f, 0f);
            if (rightPageTurn == 0f) {
                resetTurnState();
                return;
            }
        }

        boolean someHandPageTurning = false;
        // If a hand is turning the page, only run code for it
        int start = pageTurnerHandIndex == -1 ? 0 : pageTurnerHandIndex;
        int end = pageTurnerHandIndex == -1 ? others.length : pageTurnerHandIndex + 1;
        for (int i = start; i < end; i++) {
            PosRot other = others[i];
            if (pageChangeState == PageChangeState.NONE) {
                if (pageTurnBoxes[0].contains(other.getPos()) && !onFirstPage()) {
                    someHandPageTurning = true;
                    startVRPageTurn(PageChangeState.LEFT_TO_RIGHT, i);
                } else if (pageTurnBoxes[1].contains(other.getPos()) && !onLastPage()) {
                    someHandPageTurning = true;
                    startVRPageTurn(PageChangeState.RIGHT_TO_LEFT, i);
                }
            } else if (!pageChangeState.isAnim) {
                if (pageTurnBoxes[2].contains(other.getPos())) {
                    boolean doingLToR = pageChangeState == PageChangeState.LEFT_TO_RIGHT;
                    double distToLeft = other.getPos().distanceTo(pageTurnPositions[0]);
                    double distToRight = other.getPos().distanceTo(pageTurnPositions[1]);
                    if (doingLToR && distToRight < distToLeft) {
                        pageChangeState = PageChangeState.LEFT_TO_RIGHT_ANIM;
                        lastPage();
                    } else if (!doingLToR && distToLeft < distToRight) {
                        pageChangeState = PageChangeState.RIGHT_TO_LEFT_ANIM;
                        nextPage();
                    } else if (doingLToR) {
                        leftPageTurn = (float) (distToLeft / (distToLeft + distToRight));
                        someHandPageTurning = true;
                        setDirty();
                    } else {
                        rightPageTurn = 1f - ((float) (distToRight / (distToLeft + distToRight)));
                        someHandPageTurning = true;
                        setDirty();
                    }
                }
            }
            if (!someHandPageTurning && !pageChangeState.isAnim && pageChangeState != PageChangeState.NONE) {
                resetTurnState();
            }
        }
    }

    public void nextPage() {
        setPage(leftPageIndex + 2);
    }

    public void lastPage() {
        setPage(leftPageIndex - 2);
    }

    public void setPage(int newPageIndex) {
        setPage(newPageIndex, true);
    }

    public void setPage(int newPageIndex, boolean runCallback) {
        if (newPageIndex % 2 != 0) {
            newPageIndex--;
        }
        if (newPageIndex > maxLeftPageIndex) {
            newPageIndex = maxLeftPageIndex;
        } else if (newPageIndex < 0) {
            newPageIndex = 0;
        }
        leftPageIndex = newPageIndex;
        setDirty();
        if (runCallback && onSetPageCallback != null) {
            onSetPageCallback.accept(leftPageIndex);
        }
    }

    public int getLeftPageIndex() {
        return leftPageIndex;
    }
    
    public void setMaxLeftPageIndex(int max) {
        if (max % 2 != 0) {
            max--;
        }
        // Items may have item components with zero pages (or books with zero pages in older versions, maybe).
        // This ensures the maxLeftPageIndex isn't negative.
        this.maxLeftPageIndex = Math.max(max, 0);
    }

    public boolean isDirty() {
        return isDirty;
    }

    public void setDirty() {
        isDirty = true;
    }

    public void setNoLongerDirty() {
        isDirty = false;
    }

    public boolean onFirstPage() {
        return leftPageIndex == 0;
    }

    public boolean onLastPage() {
        return leftPageIndex == maxLeftPageIndex;
    }

    /**
     * Start an automatic page turn.
     * @param pageTurner Player turning the page.
     * @param isNextPage Whether the page is turning to the next page or the previous.
     */
    public void startNonVRPageTurnAnim(Player pageTurner, boolean isNextPage) {
        // Turn to the next page and start a full animation if out of VR and it's valid to
        // go to the next page
        this.pageTurner = pageTurner;
        this.pageChangeState = isNextPage ? PageChangeState.RIGHT_TO_LEFT_ANIM : PageChangeState.LEFT_TO_RIGHT_ANIM;
        this.pageTurnerHandIndex = -1;
        if (isNextPage) {
            nextPage();
        } else {
            lastPage();
        }
        setDirty();
    }

    public PageChangeState getPageChangeState() {
        return this.pageChangeState;
    }

    @Override
    public void encode(RegistryFriendlyByteBuf buffer) {
        buffer.writeInt(leftPageIndex)
              .writeFloat(leftPageTurn)
              .writeFloat(rightPageTurn)
              .writeEnum(pageChangeState);
    }

    @Override
    public void decode(RegistryFriendlyByteBuf buffer) {
        leftPageIndex = buffer.readInt();
        leftPageTurn = buffer.readFloat();
        rightPageTurn = buffer.readFloat();
        pageChangeState = buffer.readEnum(PageChangeState.class);
    }

    public void resetTurnState() {
        pageTurnerHandIndex = -1;
        leftPageTurn = 0f;
        rightPageTurn = 1f;
        pageChangeState = PageChangeState.NONE;
        pageTurner = null;
        setDirty();
    }

    /**
     * Starts a VR page turn
     * @param state The direction to turn the page.
     * @param handIndex The index of the hand turning the page.
     */
    protected void startVRPageTurn(PageChangeState state, int handIndex) {
        pageTurnerHandIndex = handIndex;
        pageChangeState = state;
        setDirty();
        if (startVRPageTurnCallback != null) {
            startVRPageTurnCallback.accept(this);
        }
    }

    /**
     * Get the vector pointing left or right.
     * @param bookPosRot The PosRot of this book.
     * @param left Whether to get the left vector.
     * @return The left or right vector.
     */
    public Vec3 getLeftRightVector(PosRot bookPosRot, boolean left) {
        Vector3f leftF = new Vector3f(0, 0, 1); // +Z is the default forward vector
        leftF.rotate(Axis.YN.rotationDegrees(left ? 270  : 90));
        leftF.rotate(Axis.ZP.rotation(bookPosRot.getRollF()));
        leftF.rotate(Axis.XN.rotation(bookPosRot.getPitchF()));
        leftF.rotate(Axis.YN.rotation(bookPosRot.getYawF()));
        return new Vec3(leftF.x(), leftF.y(), leftF.z());
    }

    /**
     * @param hand Hand data
     * @return The vector pointing away from the book. This is the opposite of the look vector of an HMD looking
     * directly at the book.
     */
    public Vec3 getAwayVector(PosRot hand) {
        Vector3f awayFromBookF = new Vector3f(0, 1, 0);
        awayFromBookF.rotate(Axis.ZP.rotation(hand.getRollF()));
        awayFromBookF.rotate(Axis.XN.rotation(hand.getPitchF()));
        awayFromBookF.rotate(Axis.YN.rotation(hand.getYawF()));
        return new Vec3(awayFromBookF.x(), awayFromBookF.y(), awayFromBookF.z());
    }
}
