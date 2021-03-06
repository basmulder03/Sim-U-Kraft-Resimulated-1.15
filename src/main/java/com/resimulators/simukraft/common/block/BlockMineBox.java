package com.resimulators.simukraft.common.block;

import com.resimulators.simukraft.client.gui.GuiHandler;
import com.resimulators.simukraft.common.entity.sim.SimEntity;
import com.resimulators.simukraft.common.tileentity.ITile;
import com.resimulators.simukraft.common.tileentity.TileMiner;
import com.resimulators.simukraft.common.world.Faction;
import com.resimulators.simukraft.common.world.SavedWorldData;
import com.resimulators.simukraft.handlers.SimUKraftPacketHandler;
import com.resimulators.simukraft.packets.OpenJobGuiPacket;
import com.resimulators.simukraft.packets.SimFirePacket;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.fml.network.NetworkDirection;

import java.util.ArrayList;

public class BlockMineBox extends BlockBase {


    public BlockMineBox(Properties properties) {
        super(properties);
    }



    @Override
    public boolean hasTileEntity(final BlockState state) {
        return true;
    }

    @Override
    public ActionResultType onBlockActivated(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult rayTrace) {
        if (!world.isRemote) {
            Faction faction = SavedWorldData.get(world).getFactionWithPlayer(player.getUniqueID());
            ArrayList<Integer> simids = faction.getSimIds((ServerWorld) world);
            TileMiner miner = ((TileMiner)world.getTileEntity(pos));
            int hiredId;
            if (miner != null){
            miner.onOpenGui(player.getAdjustedHorizontalFacing());
            if (miner.getHired()){
                SimEntity sim =  (SimEntity) ((ServerWorld) world).getEntityByUuid(miner.getSimId());
                if (sim != null){
                 hiredId = sim.getEntityId();
                }else{
                    miner.setHired(false);
                    miner.setSimId(null);
                    return ActionResultType.FAIL;
                }
                SimUKraftPacketHandler.INSTANCE.sendTo(new OpenJobGuiPacket(simids,pos,hiredId, GuiHandler.MINER, "Miner"),((ServerPlayerEntity) player).connection.netManager, NetworkDirection.PLAY_TO_CLIENT);// used when there is a sim hired
            } else {
                SimUKraftPacketHandler.INSTANCE.sendTo(new OpenJobGuiPacket(simids,pos,GuiHandler.MINER, "Miner"),((ServerPlayerEntity) player).connection.netManager, NetworkDirection.PLAY_TO_CLIENT);//used when there is no sim employed at this block
            }

        }}
        return ActionResultType.SUCCESS;
    }


    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return new TileMiner();
    }

    @Override
    public void onBlockHarvested(World worldIn, BlockPos pos, BlockState state, PlayerEntity player) {
        super.onBlockHarvested(worldIn, pos, state, player);
        if (!worldIn.isRemote){

            ITile tile = ((ITile) worldIn.getTileEntity(pos));

            SimEntity sim = (SimEntity) ((ServerWorld)worldIn).getEntityByUuid(tile.getSimId());
            if (sim != null){
                int id = SavedWorldData.get(worldIn).getFactionWithPlayer(player.getUniqueID()).getId();
                sim.fireSim(sim,id,false);
            }
        }
    }
}
