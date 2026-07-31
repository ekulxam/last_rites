package survivalblock.last_rites.mixin.client;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.util.ARGB;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import survivalblock.last_rites.common.component.item.CineraryBladeComponent;
import survivalblock.last_rites.common.init.LastRitesDataComponentTypes;

@Mixin(GuiGraphicsExtractor.class)
public abstract class GuiGraphicsExtractorMixin {
    @Shadow
    public abstract void fill(RenderPipeline pipeline, int x0, int y0, int x1, int y1, int col);

    @SuppressWarnings("DiscouragedShift")
    @Inject(method = "itemBar", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;isBarVisible()Z", shift = At.Shift.BEFORE))
    private void cineraryDoubleBar(ItemStack itemStack, int x, int y, CallbackInfo ci) {
        CineraryBladeComponent component = itemStack.get(LastRitesDataComponentTypes.CINERARY_BLADE);
        if (component == null) {
            return;
        }

        int left = x + 2;
        int top = y + 13 - 1;
        this.fill(RenderPipelines.GUI, left, top, left + 13, top + 2, -16777216);
        this.fill(RenderPipelines.GUI, left, top, left + component.getItemBarWidth(), top + 1, ARGB.opaque(component.getItemBarColor()));
    }
}
