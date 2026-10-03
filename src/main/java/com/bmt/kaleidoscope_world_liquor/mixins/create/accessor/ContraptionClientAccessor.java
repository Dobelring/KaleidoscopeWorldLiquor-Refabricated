package com.bmt.kaleidoscope_world_liquor.mixins.create.accessor;

import com.simibubi.create.content.contraptions.Contraption;
import com.simibubi.create.content.contraptions.render.ClientContraption;
import java.util.concurrent.atomic.AtomicReference;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(
    value = {Contraption.class},
    remap = false
)
public interface ContraptionClientAccessor {
    @Accessor("clientContraption")
    AtomicReference<ClientContraption> getClientContraptionReference();

    @Invoker("createClientContraption")
    ClientContraption invokeCreateClientContraption();
}
