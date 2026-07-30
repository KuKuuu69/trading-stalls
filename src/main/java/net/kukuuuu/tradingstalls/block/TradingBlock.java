package net.kukuuuu.tradingstalls.block;

import com.mojang.serialization.MapCodec;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.kukuuuu.tradingstalls.block.entity.TradingBlockEntity;
import net.kukuuuu.tradingstalls.screen.BuyerScreenHandler;
import net.kukuuuu.tradingstalls.screen.MerchantScreenHandler;
import net.kukuuuu.tradingstalls.screen.ShopScreenData;
import net.kukuuuu.tradingstalls.shop.ShopAccess;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class TradingBlock extends BaseEntityBlock {
    private static final VoxelShape SHAPE = Block.box(0, 0, 0, 16, 6, 16);

    public TradingBlock(Properties settings) {
        super(settings);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new TradingBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(
            Level world,
            BlockState state,
            BlockEntityType<T> type
    ) {
        return world.isClientSide()
                ? null
                : createTickerHelper(type, net.kukuuuu.tradingstalls.block.entity.ModBlockEntities.TRADING_BLOCK_ENTITY,
                TradingBlockEntity::serverTick);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return simpleCodec(TradingBlock::new);
    }

    @Override
    public void setPlacedBy(Level world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack itemStack) {
        super.setPlacedBy(world, pos, state, placer, itemStack);
        if (!world.isClientSide() && placer instanceof Player player
                && world.getBlockEntity(pos) instanceof TradingBlockEntity tradingBlock) {
            tradingBlock.setOwner(player);
        }
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level world, BlockPos pos, Player player, BlockHitResult hit) {
        if (world.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        if (!(world.getBlockEntity(pos) instanceof TradingBlockEntity tradingBlock)) {
            return InteractionResult.PASS;
        }
        if (!tradingBlock.hasOwner()) {
            tradingBlock.setOwner(player);
        }

        boolean merchantView = tradingBlock.isOwner(player) && !player.isShiftKeyDown();
        player.openMenu(createScreenFactory(tradingBlock, merchantView));
        return InteractionResult.CONSUME;
    }

    @Override
    protected float getDestroyProgress(BlockState state, Player player, BlockGetter world, BlockPos pos) {
        if (world.getBlockEntity(pos) instanceof TradingBlockEntity tradingBlock
                && !ShopAccess.canBreak(player, tradingBlock)) {
            return 0.0F;
        }
        return super.getDestroyProgress(state, player, world, pos);
    }

    private ExtendedScreenHandlerFactory<ShopScreenData> createScreenFactory(
            TradingBlockEntity tradingBlock,
            boolean merchantView
    ) {
        return new ExtendedScreenHandlerFactory<>() {
            @Override
            public ShopScreenData getScreenOpeningData(ServerPlayer player) {
                return new ShopScreenData(tradingBlock.getBlockPos(), merchantView && tradingBlock.isVillageConnected());
            }

            @Override
            public Component getDisplayName() {
                return Component.translatable(merchantView
                        ? "screen.trading-stalls.merchant"
                        : "screen.trading-stalls.buyer");
            }

            @Override
            public AbstractContainerMenu createMenu(int syncId, Inventory playerInventory, Player player) {
                return merchantView
                        ? new MerchantScreenHandler(syncId, playerInventory, tradingBlock)
                        : new BuyerScreenHandler(syncId, playerInventory, tradingBlock);
            }
        };
    }

    @Override
    protected void affectNeighborsAfterRemoval(BlockState state, ServerLevel world, BlockPos pos, boolean moved) {
        if (world.getBlockEntity(pos) instanceof TradingBlockEntity tradingBlock) {
            Containers.dropContents(world, pos, tradingBlock.getInventory());
        }
        super.affectNeighborsAfterRemoval(state, world, pos, moved);
    }
    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }
}
