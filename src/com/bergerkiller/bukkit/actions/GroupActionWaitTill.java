package com.bergerkiller.bukkit.actions;

import com.bergerkiller.bukkit.tc.MinecartGroup;

public class GroupActionWaitTill extends GroupAction {

	private long finishtime;
	public GroupActionWaitTill(final MinecartGroup group, final long finishtime) {
		super(group);
		this.setTime(finishtime);
	}
	
	protected void setTime(long finishtime) {
		this.finishtime = finishtime;
	}

	@Override
	public boolean update() {
		return this.finishtime <= System.currentTimeMillis();
	}

}
