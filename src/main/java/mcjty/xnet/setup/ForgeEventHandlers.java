package mcjty.xnet.setup;

import mcjty.xnet.multiblock.XNetWirelessChannels;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;

public class ForgeEventHandlers {

    private static final int AMOUNT = 10;

    private int cnt = AMOUNT;

    @SubscribeEvent
    public void onWorldTick(TickEvent.WorldTickEvent event) {
        if (event.side == LogicalSide.SERVER) {
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
