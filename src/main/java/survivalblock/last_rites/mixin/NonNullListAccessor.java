package survivalblock.last_rites.mixin;

import net.minecraft.core.NonNullList;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.List;

@Mixin(NonNullList.class)
public interface NonNullListAccessor {
    @Invoker("<init>")
    static <E> NonNullList<E> invokeInit(final List<E> list, final @Nullable E defaultValue) {
        throw new UnsupportedOperationException();
    }
}
