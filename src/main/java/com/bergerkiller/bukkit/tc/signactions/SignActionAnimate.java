package com.bergerkiller.bukkit.tc.signactions;

import org.bukkit.entity.Player;

import com.bergerkiller.bukkit.tc.Permission;
import com.bergerkiller.bukkit.tc.attachments.animation.AnimationOptions;
import com.bergerkiller.bukkit.tc.controller.MinecartMember;
import com.bergerkiller.bukkit.tc.controller.MinecartMemberStore;
import com.bergerkiller.bukkit.tc.events.SignActionEvent;
import com.bergerkiller.bukkit.tc.events.SignChangeActionEvent;

public class SignActionAnimate extends SignAction {

    @Override
    public boolean match(SignActionEvent info) {
        return info.isType("animate");
    }

    public void animate(SignActionEvent info) {
        AnimationOptions options = new AnimationOptions();
        options.loadFromSign(info);
        if (info.getGroup().isManualMovement) {
	    	if (options.getName().equals("ldopen") || options.getName().equals("rdopen") || options.getName().equals("ldclose") || options.getName().equals("rdclose")) {
	    		return;
	    	}
        }
        for (MinecartMember<?> mm : info.getMembers()) {
            mm.playNamedAnimation(options);
        }
    }

    @Override
    public boolean click(SignActionEvent info, Player player) {
        MinecartMember<?> member = MinecartMemberStore.getFromEntity(player.getVehicle());
        if (member == null) {
            return false;
        }
        info.setMember(member);
        animate(info);
        return true;
    }

    @Override
    public void execute(SignActionEvent info) {
        boolean isRemote = false;
        if (info.isCartSign() && info.isAction(SignActionType.MEMBER_ENTER, SignActionType.REDSTONE_ON)) {
            //TODO: Add something here or remove
        } else if (info.isTrainSign() && info.isAction(SignActionType.GROUP_ENTER, SignActionType.REDSTONE_ON)) {
            //TODO: Add something here or remove
        } else if (info.isRCSign() && info.isAction(SignActionType.REDSTONE_ON)) {
            isRemote = true;
        } else {
            return;
        }
        if (isRemote || (info.hasMember() && info.isPowered())) {
            animate(info);
        }
    }

    @Override
    public boolean build(SignChangeActionEvent event) {
        if (event.isRCSign()) {
            return handleBuild(event, Permission.BUILD_ANIMATOR, "animator", "play train model animations remotely");
        } else {
            return handleBuild(event, Permission.BUILD_ANIMATOR, "animator", "play train model animations");
        }
    }

    @Override
    public boolean canSupportRC() {
        return true;
    }
}
