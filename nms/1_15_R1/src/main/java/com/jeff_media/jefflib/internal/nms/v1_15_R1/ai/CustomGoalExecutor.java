/*
 * Copyright (c) 2023. JEFF Media GbR / mfnalex et al.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.jeff_media.jefflib.internal.nms.v1_15_R1.ai;

import com.jeff_media.jefflib.ai.goal.CustomGoal;
import com.jeff_media.jefflib.ai.goal.GoalFlag;
import com.jeff_media.jefflib.ai.navigation.JumpController;
import com.jeff_media.jefflib.ai.navigation.LookController;
import com.jeff_media.jefflib.ai.navigation.MoveController;
import com.jeff_media.jefflib.ai.navigation.PathNavigation;
import java.util.EnumSet;
import java.util.stream.Collectors;
import net.minecraft.server.v1_15_R1.EntityInsentient;
import net.minecraft.server.v1_15_R1.PathfinderGoal;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CustomGoalExecutor extends PathfinderGoal implements com.jeff_media.jefflib.ai.goal.CustomGoalExecutor {

    private final CustomGoal goal;
    private final EntityInsentient pmob;

    public CustomGoalExecutor(final CustomGoal goal, final EntityInsentient pmob) {
        this.pmob = pmob;
        this.goal = goal;
    }

    @NotNull
    private static EnumSet<Type> translateGoalFlags(@Nullable final EnumSet<GoalFlag> flags) {
        if (flags == null) return EnumSet.noneOf(Type.class);
        return flags.stream().map(flag -> Type.valueOf(flag.name())).collect(Collectors.toCollection(() -> EnumSet.noneOf(Type.class)));
    }

    @NotNull
    private static EnumSet<GoalFlag> translateFlags(@Nullable final EnumSet<Type> flags) {
        if (flags == null) return EnumSet.noneOf(GoalFlag.class);
        return flags.stream().map(flag -> GoalFlag.valueOf(flag.name())).collect(Collectors.toCollection(() -> EnumSet.noneOf(GoalFlag.class)));
    }

    @Override
    public boolean a() {
        return goal.canUse();
    }

    @Override
    public boolean b() {
        return goal.canContinueToUse();
    }

    @Override
    public void c() {
        goal.start();
    }

    @Override
    public void d() {
        goal.stop();
    }

    @Override
    public void e() {
        goal.tick();
    }

    @NotNull
    @Override
    public EnumSet<GoalFlag> getGoalFlags() {
        return translateFlags(this.i());
    }

    @Override
    public void setGoalFlags(final EnumSet<GoalFlag> flags) {
        this.a(translateGoalFlags(flags));
    }

    @NotNull
    @Override
    public PathNavigation getNavigation() {
        return new HatchedPathNavigation(pmob.getNavigation());
    }

    @NotNull
    @Override
    public MoveController getMoveController() {
        return new HatchedMoveController(pmob.getControllerMove());
    }

    @NotNull
    @Override
    public LookController getLookController() {
        return new HatchedLookController(pmob.getControllerLook());
    }

    @NotNull
    @Override
    public JumpController getJumpController() {
        return new HatchedJumpController(pmob.getControllerJump());
    }

    @Override
    public boolean D_() {
        return goal.isInterruptable();
    }
}
