package com.hammy275.immersivemc.common.immersive.storage.network.impl;

import com.hammy275.immersivemc.api.common.immersive.NetworkStorage;
import com.hammy275.immersivemc.common.immersive.CommonBookData;
import com.hammy275.immersivemc.common.immersive.handler.ImmersiveHandlers;
import com.hammy275.immersivemc.common.util.PageChangeState;
import com.hammy275.immersivemc.common.util.PosRot;
import com.hammy275.immersivemc.common.util.Util;
import com.hammy275.immersivemc.common.vr.VRVerify;
import com.hammy275.immersivemc.common.vr.VRUtil;
import com.hammy275.immersivemc.mixin.LecternBlockEntityAccessor;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.LecternBlockEntity;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.vivecraft.api.VRAPI;

/**
 * Represents data about a lectern. Contains the data for its book, the book itself, and the position and level
 * the lectern is in.
 * @param <T> Should always be CommonBookData when not extending this class.
 */
public class LecternData<T extends CommonBookData> implements NetworkStorage {

    public ItemStack book = ItemStack.EMPTY;
    public T bookData;
    public transient BlockPos pos = BlockPos.ZERO;
    public transient Level level = null;

    public LecternData(T bookData) {
        this.bookData = bookData;
    }

    /**
     * Sets the book in this data.
     * @param book Book to set or item to set that isn't a book.
     * @param lecternServerSide The lectern if this method is being called from the server.
     */
    public void setBook(ItemStack book, @Nullable LecternBlockEntity lecternServerSide) {
        if (!ItemStack.matches(book, this.book)) {
            this.book = book;
            this.bookData.resetTurnState();
            this.bookData.setMaxLeftPageIndex(findMaxLeftPageIndex());
            if (lecternServerSide != null) {
                this.bookData.setPage(lecternServerSide.getPage());
                this.bookData.onSetPageCallback = newPage -> ((LecternBlockEntityAccessor) lecternServerSide).immersiveMC$setPage(newPage);
            }
        }
    }

    /**
     * Function to be run every tick to tick this data.
     * @param player The current player if on the client, or null if on the server.
     */
    public void tick(@Nullable Player player) {
        if (level == null || !ImmersiveHandlers.lecternHandler.isValidBlock(pos, level)) return;

        if (!level.isClientSide()) {
            LecternBlockEntity lectern = (LecternBlockEntity) level.getBlockEntity(pos);
            int lecternPageNum = lectern.getPage();
            if (lecternPageNum != this.bookData.getLeftPageIndex() && lecternPageNum != this.bookData.getLeftPageIndex() + 1) {
                this.bookData.setPage(lecternPageNum, false);
            }
        }

        if (player != null) {
            this.bookData.pageTurner = player;
        }
        if (this.bookData.pageTurner == null) return;

        if (!VRVerify.playerInVR(this.bookData.pageTurner) &&
            this.bookData.getPageChangeState() != PageChangeState.NONE && !this.bookData.getPageChangeState().isAnim) {
            this.bookData.startNonVRPageTurnAnim(this.bookData.pageTurner, this.bookData.getPageChangeState() == PageChangeState.RIGHT_TO_LEFT);
        }

        PosRot lecternPosRot = getLecternPosRot(pos);
        if (VRVerify.playerInVR(this.bookData.pageTurner)) {
            this.bookData.tick(lecternPosRot,
                    VRUtil.posRot(VRAPI.instance().getVRPose(this.bookData.pageTurner).getMainHand()),
                    VRUtil.posRot(VRAPI.instance().getVRPose(this.bookData.pageTurner).getOffHand()));
        } else {
            this.bookData.tick(lecternPosRot);
        }
    }

    /**
     * Get the PosRot of a book in a lectern.
     * @param pos Position of the lectern.
     * @return The PosRot of the book in the lectern.
     */
    public PosRot getLecternPosRot(BlockPos pos) {
        Direction lecternDir = level.getBlockState(pos).getValue(BlockStateProperties.HORIZONTAL_FACING);
        Vec3 lecternPos = Vec3.atBottomCenterOf(pos).add(0, 1, 0)
                .add(lecternDir.getUnitVec3().scale(0.1));
        return new PosRot(lecternPos, Util.getLookAngle((float) -Math.PI / 8f, (float) -Math.toRadians(lecternDir.getOpposite().toYRot())),
                (float) Math.PI / 8f,
                Math.toRadians(lecternDir.getOpposite().toYRot()), 0);
    }

    protected int findMaxLeftPageIndex() {
        if (book.isEmpty()) return 0;
        int size = 0;
        if (book.has(DataComponents.WRITTEN_BOOK_CONTENT)) {
            size = book.get(DataComponents.WRITTEN_BOOK_CONTENT).pages().size();
        } else if (book.has(DataComponents.WRITABLE_BOOK_CONTENT)) {
            size = book.get(DataComponents.WRITABLE_BOOK_CONTENT).pages().size();
        }
        return Math.max(size - 1, 0);
    }

    @Override
    public void encode(RegistryFriendlyByteBuf buffer) {
        ItemStack.OPTIONAL_STREAM_CODEC.encode(buffer, this.book);
        bookData.encode(buffer);
    }

    @Override
    public void decode(RegistryFriendlyByteBuf buffer) {
        this.book = ItemStack.OPTIONAL_STREAM_CODEC.decode(buffer);
        bookData.decode(buffer);
    }
}
