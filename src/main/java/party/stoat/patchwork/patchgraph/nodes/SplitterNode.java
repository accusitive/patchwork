package party.stoat.patchwork.patchgraph.nodes;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;
import org.checkerframework.checker.nullness.qual.NonNull;
import party.stoat.patchwork.Patchwork;
import party.stoat.patchwork.block.PatchworkHandler;
import party.stoat.patchwork.patchgraph.Node;
import party.stoat.patchwork.patchgraph.NodeDescriptor;
import party.stoat.patchwork.patchgraph.PatchInstance;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class SplitterNode extends Node {

    public static final ResourceLocation IDENTIFIER = ResourceLocation.fromNamespaceAndPath(Patchwork.MOD_ID, "splitter");

    public SplitterNode(UUID uuid, NodeDescriptor descriptor) {
        super(uuid, descriptor);
    }

    record SplitterItemHandler(List<IItemHandler> handlers) implements IItemHandler {

        @Override
        public int getSlots() {
            return handlers.stream().mapToInt(IItemHandler::getSlots).sum();
        }

        @Override
        public ItemStack getStackInSlot(int slot) {
            IItemHandler handler = handlerForSlot(slot);

            if (handler == null) return ItemStack.EMPTY;

            return handler.getStackInSlot(localSlot(slot));
        }

        @Override
        public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
            if (handlers.isEmpty() || stack.isEmpty()) return stack;

            ItemStack remaining = stack.copy();
            int per = stack.getCount() / handlers.size();
            if (per > 0) {
                for (IItemHandler handler : handlers) {

                    ItemStack part = stack.copy();
                    part.setCount(per);

                    ItemStack leftover =
                            handler.insertItem(0, part, simulate);

                    remaining.shrink(per - leftover.getCount());
                }
            }

            while (!remaining.isEmpty()) {

                boolean inserted = false;

                for (IItemHandler handler : handlers) {

                    ItemStack one = remaining.copy();
                    one.setCount(1);

                    ItemStack leftover =
                            handler.insertItem(0, one, simulate);

                    if (leftover.isEmpty()) {
                        remaining.shrink(1);
                        inserted = true;

                        if (remaining.isEmpty())
                            break;
                    }
                }

                if (!inserted)
                    break;
            }

            return remaining;
        }

        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            return ItemStack.EMPTY;
        }

        @Override
        public int getSlotLimit(int slot) {
            IItemHandler handler = handlerForSlot(slot);

            if (handler == null)
                return 0;

            return handler.getSlotLimit(localSlot(slot));
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            IItemHandler handler = handlerForSlot(slot);

            return handler != null &&
                    handler.isItemValid(localSlot(slot), stack);
        }

        private IItemHandler handlerForSlot(int slot) {
            int offset = 0;

            for (IItemHandler handler : handlers) {
                int slots = handler.getSlots();
                if (slot < offset + slots) {
                    return handler;
                }
                offset += slots;
            }

            return null;
        }

        private int localSlot(int slot) {
            int offset = 0;

            for (IItemHandler handler : handlers) {
                int slots = handler.getSlots();
                if (slot < offset + slots) {
                    return slot - offset;
                }
                offset += slots;
            }

            return -1;
        }
    }

    @Override
    public @Nullable IItemHandler getItemHandler(ServerLevel level, NodeDescriptor.IO port, PatchInstance patch) {
        var outputs = getOutputConnections(patch.graph);

        return new SplitterItemHandler(outputs.stream().map(output -> {
            var foreignPort = patch.graph.nodeDescriptors.get(output.to()).getPort(output.keyTo());
            var foreignNode = patch.nodes.get(output.to());
            return foreignNode.getItemHandler(level, foreignPort, patch);
        }).filter(Objects::nonNull).toList()
        );
    }

    @Override
    public ResourceLocation getIdentifier() {
        return null;
    }

    @Override
    public void acceptConfiguration(String string) {

    }
}
