package com.hammy275.immersivemc.client.immersive.book;

import com.hammy275.immersivemc.api.common.hitbox.OBB;
import com.hammy275.immersivemc.client.immersive.info.render_state.BookDataRenderState;
import com.hammy275.immersivemc.common.immersive.CommonBookData;
import com.hammy275.immersivemc.common.util.PosRot;
import net.minecraft.client.Minecraft;

import java.util.ArrayList;
import java.util.List;

/**
 * Data used only by the client for a book that can be used in Immersives. Has everything from its parent class, along
 * with the ability to add renderables and interactables for rendering and allowing interactions. Interactions are
 * only handled for VR users.
 */
public class ClientBookData extends CommonBookData {
    public final List<BookInteractable> interactables = new ArrayList<>();
    public final List<BookRenderable> renderables = new ArrayList<>();

    protected final List<OBB> obbs = new ArrayList<>();

    protected float lastLeftPageTurn;
    protected float lastRightPageTurn;

    public ClientBookData() {
        super();
        this.pageTurner = Minecraft.getInstance().player;
        this.lastLeftPageTurn = leftPageTurn;
        this.lastRightPageTurn = rightPageTurn;
    }

    /**
     * Should be called every tick.
     * @param bookPosRot PosRot of the book itself.
     * @param others PosRots for things that can interact with the book (VR controllers).
     */
    @Override
    public void tick(PosRot bookPosRot, PosRot... others) {
        lastLeftPageTurn = leftPageTurn;
        lastRightPageTurn = rightPageTurn;
        super.tick(bookPosRot, others);
        obbs.clear();
        BookInteractable[] interacted = new BookInteractable[others.length];
        for (BookInteractable interactable : interactables) {
            OBB obb = interactable.getOBB();
            obbs.add(obb);
            for (int o = 0; o < others.length; o++) {
                PosRot other = others[o];
                if (interacted[o] == null && obb.contains(other.getPos())) {
                     interacted[o] = interactable;
                }
            }
        }

        for (int o = 0; o < others.length; o++) {
            if (interacted[o] != null) {
                if (Minecraft.getInstance().options.keyAttack.isDown()) {
                    interacted[o].interact(this, bookPosRot, others[o]);
                } else {
                    interacted[o].hover(this, bookPosRot, others[o]);
                }
            }
        }
    }

    /**
     * Merges a {@link CommonBookData} instance from the server into this instance. Important to keep the
     * server and client in-sync.
     * @param fromServer Data from the server.
     */
    public void mergeFromServer(CommonBookData fromServer) {
        this.leftPageIndex = fromServer.leftPageIndex;
        this.leftPageTurn = fromServer.leftPageTurn;
        this.rightPageTurn = fromServer.rightPageTurn;
        this.pageChangeState = fromServer.pageChangeState;
    }

    public List<OBB> getPageTurnHitboxes() {
        return List.of(pageTurnBoxes);
    }

    public List<OBB> getInteractableHitboxes() {
        return interactables.stream().map(BookInteractable::getOBB).toList();
    }

    @Override
    public void resetTurnState() {
        super.resetTurnState();
        lastLeftPageTurn = leftPageTurn;
        lastRightPageTurn = rightPageTurn;
    }

    public void extractRenderState(BookDataRenderState renderState) {
        renderState.pageTurnBoxes = pageTurnBoxes.clone();
        renderState.pageTurnPositions = pageTurnPositions.clone();
        renderState.leftPageIndex = leftPageIndex;
        renderState.leftPageTurn = leftPageTurn;
        renderState.rightPageTurn = rightPageTurn;
        renderState.pageChangeState = pageChangeState;
        renderState.renderables = renderables.stream().map(BookRenderable::getRenderingCopy).toList();
        renderState.obbs = List.copyOf(obbs);
        renderState.lastLeftPageTurn = lastLeftPageTurn;
        renderState.lastRightPageTurn = lastRightPageTurn;
    }
}
