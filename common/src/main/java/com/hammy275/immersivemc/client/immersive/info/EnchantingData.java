package com.hammy275.immersivemc.client.immersive.info;

import com.hammy275.immersivemc.Platform;
import com.hammy275.immersivemc.api.client.immersive.BuiltImmersiveInfo;
import com.hammy275.immersivemc.client.immersive.book.BookRenderable;
import com.hammy275.immersivemc.client.immersive.book.ClientBookData;
import com.hammy275.immersivemc.client.immersive.book.WrittenBookHelpers;
import com.hammy275.immersivemc.common.compat.apotheosis.Apoth;
import com.hammy275.immersivemc.common.compat.apotheosis.ApothStats;
import com.hammy275.immersivemc.common.immersive.storage.network.impl.ETableStorage;
import com.hammy275.immersivemc.common.util.PosRot;
import com.hammy275.immersivemc.common.util.Util;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import static com.hammy275.immersivemc.common.immersive.CommonBookData.textStackScaleSize;

public class EnchantingData {

    public final ETableData weakData = new ETableData();
    public final ETableData midData = new ETableData();
    public final ETableData strongData = new ETableData();
    public ApothStats apothStats = ApothStats.EMPTY;

    protected ClientBookData bookData = null;

    public boolean hasAnyEnchantments() {
        return strongData.isPresent() || midData.isPresent() || weakData.isPresent();
    }

    @Nullable
    public ClientBookData getBookData(BuiltImmersiveInfo<EnchantingData> info) {
        if (bookData == null && Apoth.apothImpl.enchantModuleEnabled()) {
            bookData = new ClientBookData();
            bookData.renderables.add(new WrittenBookHelpers.BookTextRenderer(leftPage -> {
                if (leftPage) {
                    boolean hasItem = !info.getItem(0).isEmpty();
                    return FormattedText.composite(
                            Component.literal("\n"),
                            translate("gui.apothic_enchanting.enchant.eterna").withStyle(ChatFormatting.GREEN),
                            Component.literal("\n"),
                            makeStatComponent(apothStats.eterna(), 100f, hasItem, false).withStyle(ChatFormatting.GREEN),
                            Component.literal("\n\n"),
                            translate("gui.apothic_enchanting.enchant.quanta").withStyle(ChatFormatting.RED),
                            Component.literal("\n"),
                            makeStatComponent(apothStats.quanta(), 100f, hasItem, true).withStyle(ChatFormatting.RED),
                            Component.literal("\n\n"),
                            translate("gui.apothic_enchanting.enchant.arcana").withStyle(ChatFormatting.DARK_PURPLE),
                            Component.literal("\n"),
                            makeStatComponent(apothStats.arcana(), 100f, hasItem, true).withStyle(ChatFormatting.DARK_PURPLE)
                    );
                } else {
                    return Component.EMPTY;
                }
            }, textStackScaleSize * 1.5f, new Vec3(0.45, 4d/3d, 0)));
            bookData.renderables.add(new ApothBarBookRenderable(() -> apothStats.eterna() / 100f, new Vec3(1, 0.5, 0),
                    0f, 197f, false, info));
            bookData.renderables.add(new ApothBarBookRenderable(() -> apothStats.quanta() / 100f, new Vec3(1, 0.1, 0),
                    5f, 202f, false, info));
            bookData.renderables.add(new ApothBarBookRenderable(() -> apothStats.arcana() / 100f, new Vec3(1, -0.3, 0),
                    10f, 207f, false, info));
        } else if (bookData != null && !Apoth.apothImpl.enchantModuleEnabled()) {
            bookData = null;
        }
        return bookData;
    }

    /**
     * Basic conversion of Apotheosis translatable keys to Zenith if Zenith is loaded
     * @param key Apotheosis key
     * @return Zenith key
     */
    private MutableComponent translate(String key) {
        if (Platform.isModLoaded("zenith")) {
            return Component.translatable(key.replaceFirst("apothic_enchanting", "zenith"));
        }
        return Component.translatable(key);
    }

    private MutableComponent makeStatComponent(float stat, float max, boolean hasItem, boolean isPercent) {
        String end = isPercent ? "%" : "";
        if (!hasItem) {
            return Component.literal("??.??%s/??.??%s".formatted(end, end)).withStyle(ChatFormatting.OBFUSCATED);
        }
        return Component.literal("%.2f%s/%.2f%s".formatted(stat, end, max, end));
    }

    public static class ETableData {
        public int levelsNeeded;
        public List<Component> textPreviews = new ArrayList<>();

        public boolean isPresent() {
            return levelsNeeded > -1;
        }

        public void set(ETableStorage.SlotData data) {
            this.textPreviews.clear();
            if (data.enchantmentHints().isEmpty()) {
                this.textPreviews.add(Component.literal("???"));
            } else {
                for (int i = 0; i < data.enchantmentHints().size(); i++) {
                    Registry<Enchantment> enchantments = Minecraft.getInstance().level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
                    Enchantment ench = enchantments.byId(data.enchantmentHints().get(i));
                    if (ench != null) {
                        this.levelsNeeded = data.xpLevel();
                        String end = i == data.enchantmentHints().size() - 1 ? "...?" : "";
                        this.textPreviews.add(Component.literal(Enchantment.getFullname(enchantments.wrapAsHolder(ench), data.enchantmentHintLevels().get(i)).getString() + end));
                    } else {
                        this.levelsNeeded = -1;
                        break;
                    }
                }
            }
        }
    }

    private record ApothBarBookRenderable(Supplier<Float> amountFullSupplier, Vec3 offset,
                                          float startYEmpty, float startYFull, boolean useOurImageForFull,
                                          BuiltImmersiveInfo<EnchantingData> info) implements BookRenderable {

        private static ResourceLocation fullLocation = null;
        private static final ResourceLocation emptyLocation = Util.id("immersive/apoth_enchanting_table/apoth_bars.png");
        private static final float barMaxX = 109f;
        private static final float maxXY = 255f;

        @Override
        public void render(PoseStack stack, ClientBookData data, boolean leftPage, int light, PosRot bookPosRot) {
            if (leftPage || info.getItem(0).isEmpty()) return;
            if (fullLocation == null) {
                String modId = Platform.isModLoaded("zenith") ? "zenith" : "apothic_enchanting";
                fullLocation = Util.id(modId, "textures/gui/enchanting_table.png");
            }
            renderBar(stack, light, amountFullSupplier.get());
        }

        private void renderBar(PoseStack stack, int light, float fullAmount) {
            // Most UIs render the empty bar, then draw the full one on top.
            // Here we need to draw part of the empty and part of the full instead to prevent z-fighting issues.
            stack.pushPose();
            stack.mulPose(Axis.YP.rotation((float) Math.PI));
            float size = 1f / (109f * 4f);
            stack.scale(size, size, size);
            renderBarPart(false, stack, light, 0, startYFull, fullAmount);
            renderBarPart(true, stack, light, barMaxX * fullAmount, startYEmpty, fullAmount);
            stack.popPose();
        }

        private void renderBarPart(boolean isEmpty, PoseStack stack, int light, float startX, float startY, float fullAmount) {
            if ((!isEmpty && fullAmount <= 0) || (isEmpty && 1 - fullAmount <= 0)) return;
            ResourceLocation barLoc = !isEmpty && !useOurImageForFull ? fullLocation : emptyLocation;
            VertexConsumer consumer =
                    Minecraft.getInstance().renderBuffers().bufferSource().getBuffer(RenderType.entityCutoutNoCull(barLoc));
            PoseStack.Pose lastPose = stack.last();
            Matrix4f pose = lastPose.pose();

            float minImageU = startX / maxXY;
            float maxImageU = (startX + barMaxX * (isEmpty ? 1 - fullAmount : fullAmount)) / maxXY;
            float minImageV = startY / maxXY;
            float maxImageV = (startY + 4) / maxXY;

            float minX = isEmpty ? barMaxX * fullAmount : 0f;
            float minY = 0f;
            float maxX = isEmpty ? barMaxX : barMaxX * fullAmount;
            float maxY = 16f; // Normal bar sizing would be 4f, but want it to be taller

            consumer.addVertex(pose, minX, minY, 0)
                    .setColor(255, 255, 255, 255)
                    .setUv(minImageU, maxImageV)
                    .setOverlay(OverlayTexture.NO_OVERLAY)
                    .setLight(light)
                    .setNormal(0, 1, 0);
            consumer.addVertex(pose, maxX, minY, 0)
                    .setColor(255, 255, 255, 255)
                    .setUv(maxImageU, maxImageV)
                    .setOverlay(OverlayTexture.NO_OVERLAY)
                    .setLight(light)
                    .setNormal(0, 1, 0);
            consumer.addVertex(pose, maxX, maxY, 0)
                    .setColor(255, 255, 255, 255)
                    .setUv(maxImageU, minImageV)
                    .setOverlay(OverlayTexture.NO_OVERLAY)
                    .setLight(light)
                    .setNormal(0, 1, 0);
            consumer.addVertex(pose, minX, maxY, 0)
                    .setColor(255, 255, 255, 255)
                    .setUv(minImageU, minImageV)
                    .setOverlay(OverlayTexture.NO_OVERLAY)
                    .setLight(light)
                    .setNormal(0, 1, 0);
        }

        @Override
        public Vec3 getStartOffset(ClientBookData data, boolean leftPage, PosRot bookPosRot) {
            return offset;
        }
    }
}
