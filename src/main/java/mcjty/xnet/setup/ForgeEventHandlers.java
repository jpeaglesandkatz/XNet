package mcjty.xnet.setup;

import mcjty.lib.varia.DimensionId;
import mcjty.xnet.multiblock.XNetWirelessChannels;
import net.minecraft.world.dimension.DimensionType;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;

public class ForgeEventHandlers {

    private static final int AMOUNT = 10;

    private int cnt = AMOUNT;

    @SubscribeEvent
    public void onWorldTick(TickEvent.WorldTickEvent event) {
        if (event.phase == TickEvent.Phase.START && !event.world.isRemote && event.world.getDimension().getType() == DimensionType.OVERWORLD) {
            cnt--;
            if (cnt > 0) {
                return;
            }
            cnt = AMOUNT;

            XNetWirelessChannels data = XNetWirelessChannels.get(event.world);
            data.tick(event.world, AMOUNT);
        }
    }
}
