package net.kukuuuu.tradingstalls.block;

import com.mojang.serialization.MapCodec;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.kukuuuu.tradingstalls.block.entity.TradingBlockEntity;
import net.kukuuuu.tradingstalls.screen.BuyerScreenHandler;
import net.kukuuuu.tradingstalls.screen.MerchantScreenHandler;
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
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

public class TradingBlock extends BlockWithEntity {
    private static final VoxelShape SHAPE = Block.createCuboidShape(0, 0, 0, 16, 6, 16);

    public TradingBlock(Settings settings) {
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
        return new TradingBlockEntity(pos, state);
    }

    @Override
    protected MapCodec<? extends BlockWithEntity> getCodec() {
        return createCodec(TradingBlock::new);
    }

    @Override
    public void onPlaced(World world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack itemStack) {
        super.onPlaced(world, pos, state, placer, itemStack);
        if (!world.isClient && placer instanceof PlayerEntity player
                && world.getBlockEntity(pos) instanceof TradingBlockEntity tradingBlock) {
            tradingBlock.setOwner(player);
        }
    }

    @Override
    protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        if (world.isClient) {
            return ActionResult.SUCCESS;
        }
        if (!(world.getBlockEntity(pos) instanceof TradingBlockEntity tradingBlock)) {
            return ActionResult.PASS;
        }
        if (!tradingBlock.hasOwner()) {
            tradingBlock.setOwner(player);
        }

        boolean merchantView = tradingBlock.isOwner(player) && !player.isSneaking();
        player.openHandledScreen(createScreenFactory(tradingBlock, merchantView));
        return ActionResult.CONSUME;
    }

    private ExtendedScreenHandlerFactory<ShopScreenData> createScreenFactory(
            TradingBlockEntity tradingBlock,
            boolean merchantView
    ) {
        return new ExtendedScreenHandlerFactory<>() {
            @Override
            public ShopScreenData getScreenOpeningData(ServerPlayerEntity player) {
                return new ShopScreenData(tradingBlock.getPos());
            }

            @Override
            public Text getDisplayName() {
                return Text.translatable(merchantView
                        ? "screen.trading-stalls.merchant"
                        : "screen.trading-stalls.buyer");
            }

            @Override
            public ScreenHandler createMenu(int syncId, PlayerInventory playerInventory, PlayerEntity player) {
                return merchantView
                        ? new MerchantScreenHandler(syncId, playerInventory, tradingBlock)
                        : new BuyerScreenHandler(syncId, playerInventory, tradingBlock);
            }
        };
    }

    @Override
    protected void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
        if (!state.isOf(newState.getBlock())
                && world.getBlockEntity(pos) instanceof TradingBlockEntity tradingBlock) {
            ItemScatterer.spawn(world, pos, tradingBlock.getInventory());
        }
        super.onStateReplaced(state, world, pos, newState, moved);
    }

    @Override
    protected BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }
}
