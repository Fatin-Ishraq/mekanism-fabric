package mekanism.common.attachments;

import mekanism.neoshim.bus.api.IEventBus;

@FunctionalInterface
public interface IAttachmentAware {

    void attachAttachments(IEventBus eventBus);
}