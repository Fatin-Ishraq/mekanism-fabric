package mekanism.neoshim.fml.event.lifecycle;

import java.util.function.Predicate;
import java.util.stream.Stream;
import mekanism.neoshim.fml.InterModComms;
import mekanism.neoshim.fml.ModContainer;

public class InterModProcessEvent extends ModLifecycleEvent {

    public InterModProcessEvent(ModContainer container) {
        super(container);
    }

    public Stream<InterModComms.IMCMessage> getIMCStream() {
        return InterModComms.getMessages(getContainer().getModId());
    }

    public Stream<InterModComms.IMCMessage> getIMCStream(Predicate<String> methodFilter) {
        return InterModComms.getMessages(getContainer().getModId(), methodFilter);
    }
}
