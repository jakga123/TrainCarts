package com.bergerkiller.bukkit.tc.signactions;

import com.bergerkiller.bukkit.common.utils.StringUtil;
import com.bergerkiller.bukkit.tc.Permission;
import com.bergerkiller.bukkit.tc.controller.MinecartGroup;
import com.bergerkiller.bukkit.tc.controller.MinecartMember;
import com.bergerkiller.bukkit.tc.events.SignActionEvent;
import com.bergerkiller.bukkit.tc.events.SignChangeActionEvent;

public class SignActionVariableText extends SignAction {

    @Override
    public boolean match(SignActionEvent info) {
        return info.isType("setvtext");
    }

    @Override
    public void execute(SignActionEvent info) {
        if (!info.isPowered()) {
            return;
        }
        String key = StringUtil.getAfter(info.getLine(1), " ").trim();
        String value = info.getLine(2) + info.getLine(3);

        if (info.isTrainSign() && info.hasGroup() && info.isAction(SignActionType.REDSTONE_ON, SignActionType.GROUP_ENTER)) {
            if (key.isEmpty()) {
            	return;
            }
            info.getGroup().setVariableText(key, value);
        } else if (info.isRCSign() && info.isAction(SignActionType.REDSTONE_ON)) {
            if (key.isEmpty()) {
            	return;
            }
            for (MinecartGroup group : info.getRCTrainGroups()) {
            	group.setVariableText(key, value);
            }
        }
    }

    @Override
    public boolean build(SignChangeActionEvent event) {
        if (event.isCartSign()) {
            return false;
        } else if (event.isTrainSign()) {
            return handleBuild(event, Permission.BUILD_FLIPPER, "train setvtext", "make text attachments variable");
        } else if (event.isRCSign()) {
            return handleBuild(event, Permission.BUILD_FLIPPER, "train setvtext", "make text attachments variable remotely");
        }
        return false;
    }

    @Override
    public boolean canSupportRC() {
        return true;
    }
}