package mekanism.common.capabilities.resolver.manager;

import mekanism.api.resource.IResourceContainer;
import mekanism.common.capabilities.MultiTypeCapability;
import mekanism.common.capabilities.holder.container.IContainerHolder;
import mekanism.common.capabilities.proxy.ProxyResourceHandler;
import mekanism.neoshim.neoforge.transfer.ResourceHandler;
import mekanism.neoshim.neoforge.transfer.resource.Resource;

/// Helper class to make reading easier rather than having super messy generics
public class ResourceHandlerManager<RESOURCE extends Resource, CONTAINER extends IResourceContainer<RESOURCE>> extends CapabilityHandlerManager<IContainerHolder<CONTAINER>, CONTAINER, ResourceHandler<RESOURCE>> {

    public ResourceHandlerManager(MultiTypeCapability<ResourceHandler<RESOURCE>> supportedCapability, IContainerHolder<CONTAINER> holder) {
        super(holder, supportedCapability.block(), IContainerHolder::getContainers, (side, containerHolder) ->
              new ProxyResourceHandler<>(() -> containerHolder.getContainers(side), side, containerHolder)
        );
    }
}