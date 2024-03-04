package mcjty.xnet.setup;

import mcjty.xnet.multiblock.XNetWirelessChannels;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.event.TickEvent;
import net.neoforged.bus.api.SubscribeEvent;

public class ForgeEventHandlers {

    private static final int AMOUNT = 10;

    private int cnt = AMOUNT;

    @SubscribeEvent
    public void onWorldTick(TickEvent.LevelTickEvent event) {
        if (event.phase == TickEvent.Phase.START && !event.level.isClientSide && event.level.dimension().equals(Level.OVERWORLD)) {
            cnt--;
            if (cnt > 0) {
                return;
            }
            cnt = AMOUNT;

            XNetWirelessChannels data = XNetWirelessChannels.get(event.level);
            data.tick(event.level, AMOUNT);
        }
    }
}
