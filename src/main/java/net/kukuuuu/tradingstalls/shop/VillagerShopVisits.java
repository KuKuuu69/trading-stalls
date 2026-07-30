package net.kukuuuu.tradingstalls.shop;

import net.kukuuuu.tradingstalls.block.entity.TradingBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.PoiTypeTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.npc.villager.VillagerProfession;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.phys.AABB;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public final class VillagerShopVisits {
    public static final int VILLAGE_RADIUS = 64;

    private static final int WORK_START_TIME = 2_000;
    private static final int WORK_END_TIME = 9_000;
    private static final int MIN_VISITS_PER_DAY = 8;
    private static final int MAX_VISITS_PER_DAY = 10;
    private static final int ARRIVAL_DISTANCE_SQUARED = 4;
    private static final int WAIT_TICKS_BEFORE_PURCHASE = 40;
    private static final int VISIT_TIMEOUT_TICKS = 1_200;
    private static final int REPOLL_NAVIGATION_TICKS = 40;
    private static final double WALK_SPEED = 0.55D;
    private static final float SUCCESSFUL_TRADE_CHANCE = 0.45F;

    private VillagerShopVisits() {
    }

    public static boolean hasNearbyVillage(ServerLevel world, BlockPos pos) {
        return world.getPoiManager()
                .findClosest(
                        entry -> entry.is(PoiTypeTags.VILLAGE),
                        ignored -> true,
                        pos,
                        VILLAGE_RADIUS,
                        PoiManager.Occupancy.ANY
                )
                .isPresent();
    }

    public static final class State {
        private long scheduledDay = Long.MIN_VALUE;
        private final List<Integer> visitTimes = new ArrayList<>();
        private final Set<UUID> visitedToday = new HashSet<>();
        private UUID activeVillagerUuid;
        private long activeVisitStartTime;
        private int arrivedTicks;
        private int navigationRetryTicks;

        public void tick(ServerLevel world, TradingBlockEntity tradingBlock) {
            long timeOfDay = world.getDayTime();
            long day = timeOfDay / 24_000L;
            int dayTime = (int) (timeOfDay % 24_000L);
            if (scheduledDay != day) {
                scheduleDay(world, tradingBlock, day, dayTime);
            }
            tickActiveVisit(world, tradingBlock);
            tickScheduledVisits(world, tradingBlock, dayTime);
        }

        private void scheduleDay(ServerLevel world, TradingBlockEntity tradingBlock, long day, int dayTime) {
            scheduledDay = day;
            visitTimes.clear();
            visitedToday.clear();
            clearActiveVisit();
            if (!hasNearbyVillage(world, tradingBlock.getBlockPos()) || dayTime > WORK_END_TIME) {
                return;
            }

            RandomSource random = world.getRandom();
            int visits = MIN_VISITS_PER_DAY + random.nextInt(MAX_VISITS_PER_DAY - MIN_VISITS_PER_DAY + 1);
            int earliestTime = Math.max(dayTime + 100, WORK_START_TIME);
            for (int index = 0; index < visits; index++) {
                if (earliestTime >= WORK_END_TIME) {
                    break;
                }
                visitTimes.add(earliestTime + random.nextInt(WORK_END_TIME - earliestTime + 1));
            }
            visitTimes.sort(Comparator.naturalOrder());
        }

        private void tickScheduledVisits(ServerLevel world, TradingBlockEntity tradingBlock, int dayTime) {
            if (visitTimes.isEmpty()) {
                return;
            }
            if (dayTime > WORK_END_TIME) {
                visitTimes.clear();
                return;
            }
            if (dayTime < visitTimes.getFirst()) {
                return;
            }
            visitTimes.removeFirst();
            if (!hasNearbyVillage(world, tradingBlock.getBlockPos()) || !tradingBlock.hasVillagerOffer()) {
                return;
            }
            RandomSource random = world.getRandom();
            if (random.nextFloat() < SUCCESSFUL_TRADE_CHANCE
                    && tradingBlock.executeRandomVillagerTrade(random)) {
                if (activeVillagerUuid == null) {
                    startVisitorIfPossible(world, tradingBlock);
                }
            }
        }

        private void startVisitorIfPossible(ServerLevel world, TradingBlockEntity tradingBlock) {
            BlockPos pos = tradingBlock.getBlockPos();
            AABB searchBox = new AABB(pos).inflate(VILLAGE_RADIUS);
            List<Villager> candidates = world.getEntities(
                    EntityTypeTest.forClass(Villager.class),
                    searchBox,
                    villager -> isEligibleVisitor(villager) && !visitedToday.contains(villager.getUUID())
            );
            if (candidates.isEmpty()) {
                return;
            }

            List<Villager> interested = candidates.stream()
                    .filter(VillagerShopVisits.State::prefersVisualShopVisit)
                    .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
            if (interested.isEmpty()) {
                interested = new ArrayList<>(candidates);
            }
            Collections.shuffle(interested);
            interested.sort(Comparator.comparingDouble(villager -> villager.distanceToSqr(
                    pos.getX() + 0.5D,
                    pos.getY() + 1.0D,
                    pos.getZ() + 0.5D
            )));
            for (Villager villager : interested) {
                if (startVisit(tradingBlock, villager)) {
                    return;
                }
            }
        }

        private static boolean prefersVisualShopVisit(Villager villager) {
            return villager.getVillagerData().profession().is(VillagerProfession.NONE);
        }
        private boolean startVisit(TradingBlockEntity tradingBlock, Villager villager) {
            BlockPos pos = tradingBlock.getBlockPos();
            boolean pathStarted = villager.getNavigation().moveTo(
                    pos.getX() + 0.5D,
                    pos.getY() + 1.0D,
                    pos.getZ() + 0.5D,
                    WALK_SPEED
            );
            if (!pathStarted) {
                return false;
            }
            visitedToday.add(villager.getUUID());
            activeVillagerUuid = villager.getUUID();
            activeVisitStartTime = villager.level().getGameTime();
            arrivedTicks = 0;
            navigationRetryTicks = 0;
            return true;
        }

        private void tickActiveVisit(ServerLevel world, TradingBlockEntity tradingBlock) {
            if (activeVillagerUuid == null) {
                return;
            }
            Entity entity = world.getEntity(activeVillagerUuid);
            if (!(entity instanceof Villager villager)
                    || !isEligibleVisitor(villager)
                    || world.getGameTime() - activeVisitStartTime > VISIT_TIMEOUT_TICKS) {
                clearActiveVisit();
                return;
            }

            BlockPos pos = tradingBlock.getBlockPos();
            if (villager.distanceToSqr(pos.getX() + 0.5D, pos.getY() + 1.0D, pos.getZ() + 0.5D)
                    <= ARRIVAL_DISTANCE_SQUARED) {
                villager.getNavigation().stop();
                arrivedTicks++;
                if (arrivedTicks >= WAIT_TICKS_BEFORE_PURCHASE) {
                    playTradeFeedback(world, pos);
                    clearActiveVisit();
                }
                return;
            }

            arrivedTicks = 0;
            navigationRetryTicks++;
            if (villager.getNavigation().isDone() && navigationRetryTicks >= REPOLL_NAVIGATION_TICKS) {
                navigationRetryTicks = 0;
                boolean pathStarted = villager.getNavigation().moveTo(
                        pos.getX() + 0.5D,
                        pos.getY() + 1.0D,
                        pos.getZ() + 0.5D,
                        WALK_SPEED
                );
                if (!pathStarted) {
                    clearActiveVisit();
                }
            }
        }

        private void clearActiveVisit() {
            activeVillagerUuid = null;
            activeVisitStartTime = 0;
            arrivedTicks = 0;
            navigationRetryTicks = 0;
        }

        private boolean isEligibleVisitor(Villager villager) {
            return villager.isAlive()
                    && !villager.isRemoved()
                    && !villager.isBaby()
                    && !villager.isSleeping()
                    && !villager.getVillagerData().profession().is(VillagerProfession.NITWIT);
        }

        private void playTradeFeedback(ServerLevel world, BlockPos pos) {
            world.playSound(
                    null,
                    pos.getX() + 0.5D,
                    pos.getY() + 0.5D,
                    pos.getZ() + 0.5D,
                    SoundEvents.VILLAGER_TRADE,
                    SoundSource.NEUTRAL,
                    0.8F,
                    1.0F
            );
        }
    }
}
