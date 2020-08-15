package com.resimulators.simukraft.common.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.resimulators.simukraft.common.item.ItemStructureTest;
import com.resimulators.simukraft.handlers.StructureHandler;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.BlockPosArgument;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MutableBoundingBox;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.gen.feature.template.Template;
import org.apache.commons.lang3.mutable.Mutable;
import sun.java2d.pipe.SpanShapeRenderer;

public class CommandStructure {
    private static final SimpleCommandExceptionType FAILED_EXCEPTION = new SimpleCommandExceptionType(new TranslationTextComponent("commands.structure.failed"));
    private static final SimpleCommandExceptionType WRONG_ITEM_EXCEPTION = new SimpleCommandExceptionType(new TranslationTextComponent("commands.structure.item"));

    public static void register(CommandDispatcher<CommandSource> dispatcher) {
        dispatcher.register(Commands.literal("structure").requires((context) -> {
            return context.hasPermissionLevel(0);
        }).then(Commands.literal("save").then(Commands.argument("from", BlockPosArgument.blockPos()).then(Commands.argument("to", BlockPosArgument.blockPos()).then(Commands.argument("name", StringArgumentType.greedyString()).executes((context -> {
            return save(context.getSource(), BlockPosArgument.getLoadedBlockPos(context, "from"), BlockPosArgument.getLoadedBlockPos(context, "to"), StringArgumentType.getString(context, "name"));
        })))))).then(Commands.literal("load").then(Commands.argument("name", StringArgumentType.string()).executes(context -> {
            return load(context.getSource(), StringArgumentType.getString(context, "name"));
        }))));
    }

    private static int save(CommandSource source, BlockPos pos1, BlockPos pos2, String name) throws CommandSyntaxException {
        MutableBoundingBox bounds = new MutableBoundingBox(pos1, pos2);
        BlockPos minPos = new BlockPos(bounds.minX, bounds.minY, bounds.minZ);
        BlockPos size = new BlockPos(bounds.getXSize(), bounds.getYSize(), bounds.getZSize());
        int i = bounds.getXSize() * bounds.getYSize() * bounds.getZSize();
        if (i == 0)
            throw FAILED_EXCEPTION.create();
        StructureHandler.saveStructure(source.getWorld(), minPos, size, name, source.getName());
        source.sendFeedback(new StringTextComponent("Saved " + name), true);
        return i;
    }

    private static int load(CommandSource source, String name) throws CommandSyntaxException {
        PlayerEntity player = source.asPlayer();
        ItemStack stack = player.getHeldItemMainhand();
        if (stack.getItem() instanceof ItemStructureTest) {
            ((ItemStructureTest) stack.getItem()).setTemplate(stack, name);
            return 1;
        } else {
            throw WRONG_ITEM_EXCEPTION.create();
        }
    }

    private static int list() {
        return 0;
    }
}
