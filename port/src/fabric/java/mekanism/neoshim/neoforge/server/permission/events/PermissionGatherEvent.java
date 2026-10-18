package mekanism.neoshim.neoforge.server.permission.events;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import mekanism.neoshim.bus.api.Event;
import mekanism.neoshim.neoforge.server.permission.nodes.PermissionNode;

public class PermissionGatherEvent extends Event {

    public static class Nodes extends PermissionGatherEvent {

        private final List<PermissionNode<?>> nodes = new ArrayList<>();

        public void addNodes(PermissionNode<?>... nodes) {
            this.nodes.addAll(List.of(nodes));
        }

        public void addNodes(Collection<PermissionNode<?>> nodes) {
            this.nodes.addAll(nodes);
        }

        public List<PermissionNode<?>> getNodes() {
            return nodes;
        }
    }
}
