package com.hammy275.immersivemc.common.immersive.storage.network.impl;

import com.hammy275.immersivemc.api.common.hitbox.OBB;
import com.hammy275.immersivemc.api.common.hitbox.OBBFactory;
import com.hammy275.immersivemc.api.common.immersive.NetworkStorage;
import com.hammy275.immersivemc.api.common.immersive.WorldStorageHandler;
import com.hammy275.immersivemc.api.server.WorldStorage;
import com.hammy275.immersivemc.common.immersive.handler.ImmersiveHandlers;
import com.hammy275.immersivemc.common.network.Network;
import com.hammy275.immersivemc.common.network.packet.PageTurnPacket;
import com.hammy275.immersivemc.common.util.PageChangeState;
import com.hammy275.immersivemc.common.util.PosRot;
import com.hammy275.immersivemc.common.util.Util;
import com.hammy275.immersivemc.common.vr.VRPlugin;
import com.hammy275.immersivemc.common.vr.VRPluginVerify;
import com.hammy275.immersivemc.common.vr.VRUtil;
import com.mojang.math.Axis;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.WrittenBookItem;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

public class BookData implements NetworkStorage, WorldStorage {

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

    public ItemStack book = ItemStack.EMPTY;
    public int leftPageIndex = 0;
    public PageChangeState pageChangeState = PageChangeState.NONE;
    public float leftPageTurn = 0f;
    public float rightPageTurn = 1f;

    public transient Player pageTurner;
    public transient int pageTurnerIndex = -1;
    // Indices 0-1: Left page start turn box and right page start turn boxes
    // Index 2: The "page progress" box. If the hand isn't in this box, the turn is cancelled.
    // If the hand is in the opposite one from when the turn started, the turn auto-completes.
    public transient OBB[] pageTurnBoxes = new OBB[3];
    // Index 0 is left center, index 1 is right center, index 2 is true center.
    public transient Vec3[] positions = new Vec3[3];
    public transient PosRot lecternPosRot;

    private transient boolean isDirty = false;
    public transient boolean authoritative = false;
    public transient BlockPos pos = BlockPos.ZERO;

    public BookData() {

    }

    public BookData(boolean authoritative) {
        this.authoritative = authoritative;
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
        setDirty();
    }

    public void nextPage() {
        setPage(leftPageIndex + 2);
    }

    public void lastPage() {
        setPage(leftPageIndex - 2);
    }

    public boolean isDirty() {
        return isDirty;
    }

    public void setDirty() {
        isDirty = true;
    }

    public void markNoLongerDirty() {
        isDirty = false;
    }

    public void lecternPlayerTick(Player player, BlockPos pos) {
        if (lecternPosRot == null) {
            Direction direction = player.level().getBlockState(pos).getValue(BlockStateProperties.HORIZONTAL_FACING);

            Vec3 renderPos = Vec3.atBottomCenterOf(pos).add(0, 1, 0)
                    .add(Vec3.atLowerCornerOf(direction.getNormal()).scale(0.1));
            lecternPosRot = new PosRot(renderPos, Util.getLookAngle((float) -Math.PI / 8f, (float) -Math.toRadians(direction.getOpposite().toYRot())),
                    22.5f,
                    direction.getOpposite().toYRot(), 0);
        }
        if (VRPluginVerify.hasAPI && VRPlugin.API.playerInVR(player)) {
            tick(lecternPosRot, VRUtil.posRot(VRPlugin.API.getVRPlayer(player).getController0()),
                    VRUtil.posRot(VRPlugin.API.getVRPlayer(player).getController1()));
        } else {
            maybeConvertToNonVR();
            tick(lecternPosRot);
        }
    }

    public void lecternServerTick() {
        if (pageTurner != null) {
            lecternPlayerTick(pageTurner, pos);
        }
    }

    public void tick(PosRot hand, PosRot... others) {
        Vec3 left = getLeftRight(hand, true);
        Vec3 right = getLeftRight(hand, false);
        Vec3 away = getAway(hand);

        positions[2] = hand.position().add(away.scale(textUpAmount)); // Center
        positions[0] = positions[2].add(left.scale(singlePageWidth * 1.25d)); // Left edge
        positions[1] = positions[2].add(right.scale(singlePageWidth * 1.25d)); // Right edge
        Vec3 upCenter = positions[2].add(away.scale(singlePageWidth * 0.5)); // Used for "continue page turning" boxes.

        double pitch = Math.toRadians(hand.getPitch());
        double yaw = Math.toRadians(hand.getYaw());

        // Boxes to start a page turn are a box on the page edge to generally capture the hand
        pageTurnBoxes[0] = OBBFactory.instance().create(AABB.ofSize(positions[0], 0.2, 0.2, pageHalfHeight * 2),
                pitch, yaw, 0);
        pageTurnBoxes[1] = OBBFactory.instance().create(AABB.ofSize(positions[1], 0.2, 0.2, pageHalfHeight * 2),
                pitch, yaw, 0);
        // Box to continue a page turn
        pageTurnBoxes[2] = OBBFactory.instance().create(AABB.ofSize(upCenter, singlePageWidth * 11d/3d, singlePageWidth * 2d, pageHalfHeight * 2.25),
                pitch, yaw, 0);

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
        int start = pageTurnerIndex == -1 ? 0 : pageTurnerIndex;
        int end = pageTurnerIndex == -1 ? others.length : pageTurnerIndex + 1;
        for (int i = start; i < end; i++) {
            PosRot other = others[i];
            if (pageChangeState == PageChangeState.NONE) {
                if (possiblyBeginPageTurn(other.position(), pageTurnBoxes[0]) && !onFirstPage()) {
                    someHandPageTurning = true;
                    startPageTurn(PageChangeState.LEFT_TO_RIGHT, i);
                } else if (possiblyBeginPageTurn(other.position(), pageTurnBoxes[1]) && !onLastPage()) {
                    someHandPageTurning = true;
                    startPageTurn(PageChangeState.RIGHT_TO_LEFT, i);
                }
            } else if (!pageChangeState.isAnim && authoritative) {
                if (pageTurnBoxes[2].contains(other.position())) {
                    boolean doingLToR = pageChangeState == PageChangeState.LEFT_TO_RIGHT;
                    double distToLeft = other.position().distanceTo(positions[0]);
                    double distToRight = other.position().distanceTo(positions[1]);
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

    @Override
    public void encode(FriendlyByteBuf buffer) {
        buffer.writeItem(book).writeInt(leftPageIndex).writeEnum(pageChangeState)
                .writeFloat(leftPageTurn).writeFloat(rightPageTurn);

    }

    @Override
    public void decode(FriendlyByteBuf buffer) {
        this.book = buffer.readItem();
        this.leftPageIndex = buffer.readInt();
        this.pageChangeState = buffer.readEnum(PageChangeState.class);
        this.leftPageTurn = buffer.readFloat();
        this.rightPageTurn = buffer.readFloat();
    }

    public void startPageTurn(PageChangeState state, int handIndex) {
        pageTurnerIndex = handIndex;
        if (authoritative) {
            pageChangeState = state;
            setDirty();
        } else {
            Network.INSTANCE.sendToServer(new PageTurnPacket(pos));
        }
    }

    public void startPageTurnAnim(Player pageTurner, boolean isNextPage) {
        // Turn to the next page and start a full animation if out of VR and it's valid to
        // go to the next page
        this.pageTurner = pageTurner;
        this.pageChangeState = isNextPage ? PageChangeState.RIGHT_TO_LEFT_ANIM : PageChangeState.LEFT_TO_RIGHT_ANIM;
        this.pageTurnerIndex = -1;
        if (isNextPage) {
            nextPage();
        } else {
            lastPage();
        }
        setDirty();
    }

    // Converts this state from VR to non-vr if in the middle of turning a page.
    protected void maybeConvertToNonVR() {
        if (pageChangeState != PageChangeState.NONE && !pageChangeState.isAnim) {
            startPageTurnAnim(this.pageTurner, pageChangeState == PageChangeState.RIGHT_TO_LEFT);
        }
    }


    // Note: Current behavior assumes you can't remove pages from the book.
    protected int maxLeftPageIndex() {
        return getPageCount() % 2 == 0 ? getPageCount() - 2 : getPageCount() - 1;
    }

    protected int getPageCount() {
        if (book.isEmpty()) return 0;
        return WrittenBookItem.getPageCount(book);
    }

    protected Vec3 getLeftRight(PosRot hand, boolean left) {
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
    protected Vec3 getAway(PosRot hand) {
        Vector3f awayFromBookF = new Vector3f(0, 1, 0);
        awayFromBookF.rotate(Axis.XN.rotationDegrees(hand.getPitch()));
        awayFromBookF.rotate(Axis.YN.rotationDegrees(hand.getYaw()));
        return new Vec3(awayFromBookF.x(), awayFromBookF.y(), awayFromBookF.z());
    }

    protected void resetTurnState() {
        pageTurnerIndex = -1;
        if (authoritative) {
            leftPageTurn = 0f;
            rightPageTurn = 1f;
            pageChangeState = PageChangeState.NONE;
            pageTurner = null;
            setDirty();
        }
    }

    protected boolean possiblyBeginPageTurn(Vec3 handPos, OBB startBox) {
        return startBox.contains(handPos);
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

    // Book is saved and loaded so setPage() has the max page number to work with.
    @Override
    public void load(CompoundTag nbt) {
        this.book = ItemStack.of(nbt.getCompound("book"));
        setPage(nbt.getInt("leftPageIndex"));
    }

    @Override
    public CompoundTag save(CompoundTag nbt) {
        nbt.put("book", book.save(new CompoundTag()));
        nbt.putInt("leftPageIndex", leftPageIndex);
        return nbt;
    }

    @Override
    public WorldStorageHandler<? extends NetworkStorage> getHandler() {
        return ImmersiveHandlers.lecternHandler;
    }
}
