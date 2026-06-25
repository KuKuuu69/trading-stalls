package net.kukuuuu.tradingstalls.block;

import com.mojang.serialization.MapCodec;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.kukuuuu.tradingstalls.block.entity.CashDrawerBlockEntity;
import net.kukuuuu.tradingstalls.screen.CashDrawerScreenHandler;
import net.kukuuuu.tradingstalls.screen.ShopScreenData;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

public class CashDrawerBlock extends BlockWithEntity {
    private static final VoxelShape SHAPE = VoxelShapes.union(
            Block.createCuboidShape(0, 0, 0, 16, 14, 15),
            Block.createCuboidShape(0, 14, 0, 16, 16, 16)
    );

    public CashDrawerBlock(Settings settings) {
        super(settings);
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return SHAPE;
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return SHAPE;
    }

    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new CashDrawerBlockEntity(pos, state);
    }

    @Override
    protected MapCodec<? extends BlockWithEntity> getCodec() {
        return createCodec(CashDrawerBlock::new);
    }

    @Override
    public void onPlaced(World world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack itemStack) {
        super.onPlaced(world, pos, state, placer, itemStack);
        if (!world.isClient && placer instanceof PlayerEntity player
                && world.getBlockEntity(pos) instanceof CashDrawerBlockEntity drawer) {
            drawer.setOwner(player);
        }
    }

    @Override
    protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        if (world.isClient) {
            return ActionResult.SUCCESS;
        }
        if (!(world.getBlockEntity(pos) instanceof CashDrawerBlockEntity drawer)) {
            return ActionResult.PASS;
        }
        if (!drawer.hasOwner()) {
            drawer.setOwner(player);
        }
        if (!drawer.isOwner(player)) {
            player.sendMessage(Text.translatable("message.trading-stalls.not_owner"), true);
            return ActionResult.CONSUME;
        }

        player.openHandledScreen(new ExtendedScreenHandlerFactory<ShopScreenData>() {
            @Override
            public ShopScreenData getScreenOpeningData(ServerPlayerEntity serverPlayer) {
                return new ShopScreenData(pos);
            }

            @Override
            public Text getDisplayName() {
                return Text.translatable("screen.trading-stalls.cash_drawer");
            }

            @Override
            public ScreenHandler createMenu(int syncId, PlayerInventory inventory, PlayerEntity menuPlayer) {
                return new CashDrawerScreenHandler(syncId, inventory, drawer);
            }
        });
        return ActionResult.CONSUME;
    }

    @Override
    protected void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
        if (!state.isOf(newState.getBlock())
                && world.getBlockEntity(pos) instanceof CashDrawerBlockEntity drawer) {
            ItemScatterer.spawn(world, pos, drawer.getInventory());
        }
        super.onStateReplaced(state, world, pos, newState, moved);
    }

    @Override
    protected BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }
}
