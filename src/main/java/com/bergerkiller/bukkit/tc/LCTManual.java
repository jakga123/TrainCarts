package com.bergerkiller.bukkit.tc;


import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.block.BlockFace;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import com.bergerkiller.bukkit.tc.controller.MinecartGroup;
import com.bergerkiller.bukkit.tc.controller.MinecartMember;

public class LCTManual {
    //Variable
	private MinecartGroup group;
	public String pilot = "";
    public int notch;
    private boolean pressed;
    public boolean stopped;
    private boolean ldoor;
    private boolean rdoor;
    private BossBar bossbar;
    private BossBar signalbar;
    private BossBar stationbar;
    private boolean targetStation;
    private double targetSpeed;
    private double targetDistance;
    private double tsLeftForce;
    private double tsLeftDistance;
    private Location targetLoc;
	public LCTManual(MinecartGroup newGroup, String playerName) {
		group = newGroup;
		group.getProperties().setSoundEnabled(false);
		pilot = playerName;
		if (pilot.equals("")) {
			group.isManualMovement = false;
		} else {
			group.isManualMovement = true;
		}
	    notch = 0;
	    pressed = false;
	    stopped = false;
	    ldoor = false;
	    rdoor = false;
	    targetStation = false;
	    targetSpeed = group.getProperties().getSpeedLimit();
	    targetDistance = 0;
	    targetLoc = new Location(group.getWorld(), 0, 0, 0);
	    bossbar = Bukkit.createBossBar("", BarColor.BLUE, BarStyle.SEGMENTED_12);
	    signalbar = Bukkit.createBossBar("차상 신호기", BarColor.YELLOW, BarStyle.SOLID);
	   	stationbar = Bukkit.createBossBar("정차 위치", BarColor.BLUE, BarStyle.SEGMENTED_12);
	}
	/*put this on SignAction(Blocker, Launcher, Station, Wait)
    	if (info.getGroup().isManualMovement()) return;
    	
    	SignActionStation
    	if (info.getGroup().isManualMovement) {
    		info.getGroup().lctManual.setTarget("station", info.getLocation());
    		return;
    	}
	*/
	
	/*put this on MinecartGroupStore
        if (g1.size() > g2.size() || g1.getTicksLived() > g2.getTicksLived() || g1.isManualMovement) {
	        // Transfer properties
	        g2.getProperties().load(g1.getProperties());
	        // Transfer name, assigning a random name to the removed properties
	        String name = g1.getProperties().getTrainName();
	        g1.getProperties().setName(TrainProperties.generateTrainName());
	        g2.getProperties().setName(name);
			if (g1.isManualMovement) {
	            g2.lctManual = new LCTManual(g2, g1.lctManual.pilot);
	        }
        }
    */
	
	/*put this on MinecartGroup.doPhysics();
		if (isManualMovement) {
			this.lctManual.doTick();
			this.lctManual.getSound().onTick();
		}
	*/
	public void doTick() {
		BlockFace forwardFace = BlockFace.SELF;
	    BlockFace trueFace = BlockFace.SELF;
		Player pilotPlayer = null;
		for (MinecartMember<?> member : group) {
			if (member != group.head() && member != group.tail()) {
				continue;
			}
			for (Entity entity : member.getEntity().getEntity().getPassengers()) {
				if (entity.getName() == pilot) {
					pilotPlayer = (Player) entity;
				} else {
					continue;
				}
				Vector pdir = pilotPlayer.getEyeLocation().getDirection();
				if (pilotPlayer.getVelocity().getX() != 0 || pilotPlayer.getVelocity().getZ() != 0) {
					Vector vec = new Vector(pdir.getX(), 0, pdir.getZ());
				    forwardFace = Util.vecToFace(vec, true);
				    trueFace = member.getDirection().getOppositeFace();
	    			Vector velo = pilotPlayer.getVelocity();
	    			double degressA = -Math.toDegrees(Math.atan2(velo.getX(), velo.getZ()));
					double degressB = pilotPlayer.getLocation().getYaw();
					while (degressB > 180) {
						degressB -= 360;
					}
					while (degressB < -180) {
						degressB += 360;
					}
					long degressC = Math.round(degressA - degressB);
					if (!pressed) {
		    			//Key Event
		    			if (Math.abs(degressC) == 180) {//S
			    			if (notch < 4) {
			    				notch++;
			    				pilotPlayer.playSound(pilotPlayer.getLocation(), Sound.BLOCK_NOTE_BLOCK_SNARE, 10000.0f, 1.0f);
			    				if (notch > 0) {
			    					pilotPlayer.sendTitle("", ChatColor.DARK_AQUA + "[역행" + notch + "]", 0, 70, 20);
			    				} else if (notch < 0) {
			    					pilotPlayer.sendTitle("", ChatColor.GOLD + "[제동" + Math.abs(notch) + "]", 0, 70, 20);
			    				} else {
				    				notch = 0;
			    					pilotPlayer.sendTitle("", ChatColor.GRAY + "[N]", 0, 70, 20);
			    				}
			    			}
		    			} else if (degressC == -90 || degressC == 270) {//A
		    				if (ldoor) {
		    					ldoor = false;
				    			group.playNamedAnimation("ldclose");
		    					if (!rdoor) {
		    						group.getProperties().setPlayersEnter(false);
		    						group.getProperties().setPlayersExit(false);
		    					}
			    		    	for (MinecartMember<?> member2 : group) {
			    		    		for (Entity entity2 : member2.getEntity().getEntity().getPassengers()) {
			    		    			if (entity2 == pilotPlayer) {
			    		    				pilotPlayer.playSound(pilotPlayer.getLocation(), Sound.BLOCK_NOTE_BLOCK_SNARE, 10000.0f, 1.0f);
					    					pilotPlayer.sendTitle("", ChatColor.GOLD + "[왼쪽 출입문 폐쇄]", 0, 70, 20);
			    		    			} else {
			    		    				entity2.sendMessage(ChatColor.GREEN + "출입문이 닫힙니다.");
			    		    			}
			    		    		}
			    		    		group.getWorld().playSound(member2.getEntity().getLocation(), Sound.BLOCK_FIRE_EXTINGUISH, 1.5f, 2.0f);
			    		    	}
		    				} else {
		    					leftDoorOpen();
		    				}
		    			} else if (degressC == 90 || degressC == -270) {//D
		    				if (rdoor) {
		    					rdoor = false;
		    					group.playNamedAnimation("rdclose");
		    					if (!ldoor) {
		    						group.getProperties().setPlayersEnter(false);
		    						group.getProperties().setPlayersExit(false);
		    					}
			    		    	for (MinecartMember<?> member2 : group) {
			    		    		for (Entity entity2 : member2.getEntity().getEntity().getPassengers()) {
			    		    			if (entity2 == pilotPlayer) {
			    		    				pilotPlayer.playSound(pilotPlayer.getLocation(), Sound.BLOCK_NOTE_BLOCK_SNARE, 10000.0f, 1.0f);
					    					pilotPlayer.sendTitle("", ChatColor.GOLD + "[오른쪽 출입문 폐쇄]", 0, 70, 20);
			    		    			} else {
			    		    				entity2.sendMessage(ChatColor.GREEN + "출입문이 닫힙니다.");
			    		    			}
			    		    		}
			    		    		group.getWorld().playSound(member2.getEntity().getLocation(), Sound.BLOCK_FIRE_EXTINGUISH, 1.5f, 2.0f);
			    		    	}
		    				} else {
		    					rightDoorOpen();
		    				}
		    			} else if (degressC == 0) {//W
		    				if (notch > -8) {
		    					notch--;
		    					pilotPlayer.playSound(pilotPlayer.getLocation(), Sound.BLOCK_NOTE_BLOCK_SNARE, 10000.0f, 1.0f);
		    					if (notch > 0) {
			    					pilotPlayer.sendTitle("", ChatColor.DARK_AQUA + "[역행" + notch + "]", 0, 70, 20);
			    				} else if (notch < 0) {
			    					pilotPlayer.sendTitle("", ChatColor.GOLD + "[제동" + Math.abs(notch) + "]", 0, 70, 20);
			    				} else {
			    					pilotPlayer.sendTitle("", ChatColor.GRAY + "[N]", 0, 70, 20);
		    					}
		    				}
		    			}
					}
					pressed = true;
				} else {
					pressed = false;
				}
				break;
			}
		}
		//계산작업
		if (targetSpeed < group.getAverageForce()) {
			double brake = group.getAverageForce() - 0.005d;
			if (brake <= 0) {
				brake = 0;
				if (!stopped) {
			    	for (MinecartMember<?> member : group) {
			    		group.getWorld().playSound(member.getEntity().getLocation(), Sound.BLOCK_FIRE_EXTINGUISH, 1.5f, 2.0f);
			    	}
			    	stopped = true;
			    	stopSound();
				}
			}
			group.setForwardForce(brake);
		} else if (notch > 0) {
			if (forwardFace == trueFace && group.getAverageForce() == 0 && stopped) {
				group.setForwardForce(-0.02d);
				group.updateDirection();
				if (pilotPlayer != null) {
					pilotPlayer.playSound(pilotPlayer.getLocation(), Sound.BLOCK_NOTE_BLOCK_SNARE, 10000.0f, 1.0f);
					pilotPlayer.sendTitle("", "[방향전환]", 0, 70, 20);
				}
				stopped = false;
			} else if ((double) (group.getProperties().getSpeedLimit() * (double) ((double)notch / 4.0d)) > group.getAverageForce()) {
				group.setForwardForce(group.getAverageForce() + ((double)notch / 1000.0d * Math.sqrt(group.getProperties().getSpeedLimit())));
				stopped = false;
			}
		} else if (notch < 0) {
			double brake = group.getAverageForce() + ((double)notch / 2000.0d);
			if (notch == -8) {
				brake = group.getAverageForce() - 0.005d;
			}
			if (brake <= 0) {
				brake = 0;
				if (!stopped) {
			    	for (MinecartMember<?> member : group) {
			    		group.getWorld().playSound(member.getEntity().getLocation(), Sound.BLOCK_FIRE_EXTINGUISH, 1.5f, 2.0f);
			    	}
			    	stopped = true;
			    	stopSound();
				}
			}
			group.setForwardForce(brake);
		}
		if (group.getAverageForce() > 0.1) {
			group.getProperties().playerCollision = CollisionMode.KILL;
		} else {
			group.getProperties().playerCollision = CollisionMode.PUSH;
		}
		String doorString = "";
		String notchString = "";
		if (ldoor) {
			doorString += ChatColor.GREEN + "[개방";
		} else {
			doorString += ChatColor.GOLD + "[폐쇄";
		}
		doorString += ChatColor.RESET + "/";
		if (rdoor) {
			doorString += ChatColor.GREEN + "개방]";
		} else {
			doorString += ChatColor.GOLD + "폐쇄]";
		}
		if (notch > 0) {
			notchString = ChatColor.DARK_AQUA + "[역행" + notch + "]";
		} else if (notch < 0) {
			notchString = ChatColor.GOLD + "[제동" + Math.abs(notch) + "]";
		} else {
			notchString = ChatColor.GRAY + "[N]";
		}
		if (group.size() > 0 && targetStation) {
			Location locA = group.get((int) Math.round((group.size() - 1) / 2)).getEntity().getLocation();
			if (group.size() % 2 == 0) {
				locA = group.get((group.size() / 2) - 1).getEntity().getLocation();
				locA.add(group.get((group.size() / 2)).getEntity().getLocation());
				locA.multiply(0.5);
			}
			double distance = targetLoc.distance(locA);
			if (distance < 0) {
				distance = 0;
			}
			stationbar.setTitle("정차 위치 || " + String.format("%.2f", distance) + " 블럭");
			double distanceB = (distance / 10d);
			if (distanceB >= 0 && distanceB <= 1) {
				stationbar.setProgress(distanceB);
			}
		} else if (!targetStation) {
			stationbar.removeAll();
		}
		bossbar.setTitle("출입문 : " + doorString + ChatColor.RESET + " || " + notchString + ChatColor.RESET + " || 속력 : " + ChatColor.BOLD + Math.round(Math.min(group.getProperties().getSpeedLimit(), group.getAverageForce()) * 100) + ChatColor.RESET +  "/" + Math.round(group.getProperties().getSpeedLimit() * 100) + "km/h");
		if (targetDistance > 0) {
			signalbar.setTitle("차상 신호기 || 제한 속도 : 0km/h");
			signalbar.setColor(BarColor.RED);
			if (group.getSpeedAhead(targetDistance) == Double.MAX_VALUE) {
				targetDistance = 0;
			}
		} else if (targetSpeed >= group.getProperties().getSpeedLimit()) {
			signalbar.setTitle("차상 신호기 || 제한 속도 : 없음");
			signalbar.setColor(BarColor.GREEN);
		} else if (targetSpeed == 0) {
			signalbar.setTitle("차상 신호기 || 제한 속도 : 0km/h");
			signalbar.setColor(BarColor.RED);
		} else {
			signalbar.setTitle("차상 신호기 || 제한 속도 : " + Math.round(targetSpeed * 100) + "km/h");
			signalbar.setColor(BarColor.YELLOW);
		}
		bossbar.setProgress(Math.max(0, Math.min(1.0d, group.getAverageForce() / group.getProperties().getSpeedLimit())));
		if (pilotPlayer != null && pilotPlayer.isInsideVehicle()) {
			if (pilotPlayer.getVehicle() == group.head().getEntity().getEntity() || pilotPlayer.getVehicle() == group.tail().getEntity().getEntity()) {
				if (!bossbar.getPlayers().contains(pilotPlayer)) {
					bossbar.addPlayer(pilotPlayer);
					signalbar.addPlayer(pilotPlayer);
				}
				if (!stationbar.getPlayers().contains(pilotPlayer) && targetStation) {
					stationbar.addPlayer(pilotPlayer);
				}
			}
		}
		for (Player p : group.getWorld().getPlayers()) {
			if (p.isInsideVehicle()) {
				if (p.getVehicle() != group.head().getEntity().getEntity() && p.getVehicle() != group.tail().getEntity().getEntity()) {
					bossbar.removePlayer(p);
					signalbar.removePlayer(p);
					stationbar.removePlayer(p);
				}
			} else {
				bossbar.removePlayer(p);
				signalbar.removePlayer(p);
				stationbar.removePlayer(p);
			}
		}
		if (Math.abs(tsLeftForce) > 0) {
			double mDist = Math.abs(group.getAverageForce());
			double leftDist = tsLeftDistance - mDist;
			if (leftDist <= 0) {
				targetSpeed += tsLeftForce;
				tsLeftForce = 0;
			} else if (leftDist > 0) {
				double leftRate = mDist / tsLeftDistance;
				targetSpeed += tsLeftForce * leftRate;
				tsLeftForce -= tsLeftForce * leftRate;
			}
			tsLeftDistance = leftDist;
			//System.out.println(targetSpeed + ", " + tsLeftForce + ", " + tsLeftDistance);
		}
	}

	public void reset() {
		//put this.lctManual.reset(); on MinecartGroup.remove();
	    notch = 0;
	    pressed = false;
	    stopped = true;
		bossbar.removeAll();
		signalbar.removeAll();
		stationbar.removeAll();
	}
	/*put this on TrainCommands.java
    } else if (LogicUtil.contains(cmd, "drive")) {
		LCTManual.commandDrive(prop.getHolder(), p, args);
		return true;
    } else if (LogicUtil.contains(cmd, "unlink")) {
		Permission.DRIVE_ME.handle(p);
		prop.getHolder().lctManual.unlink(p);
		return true;
	*/
	public static void commandDrive(MinecartGroup g, Player p, String[] args) {
		if (g.getProperties().getSpeedLimit() > 2) {
			Permission.DRIVE_NOLIMIT.handle(p);
		}
		if (g.getProperties().getSpeedLimit() <= 2 && g.getProperties().getSpeedLimit() > 1.2d) {
			Permission.DRIVE_200.handle(p);
		}
		if (g.getProperties().getSpeedLimit() <= 1.2d && g.getProperties().getSpeedLimit() >= 1.0d) {
			Permission.DRIVE_120.handle(p);
		}
		if (g.getProperties().getSpeedLimit() < 1.0d && g.getProperties().getSpeedLimit() > 0.4d) {
			Permission.DRIVE_NOLIMIT.handle(p);
		}
		if (args.length > 0) {
            if (args[0].length() > 0) {
            	if (args[0].equals("false") || args[0].equals("off") || args[0].equals("auto")) {
        			Permission.DRIVE_OFF.handle(p);
                    g.lctManual.reset();
            		g.lctManual = new LCTManual(g, "");
            		p.sendMessage(ChatColor.GREEN + "자동운전으로 전환되었습니다.");
            		return;
            	}
    			Permission.DRIVE_ALL.handle(p);
        		p = Bukkit.getPlayer(args[0]);
            }
		} else {
			Permission.DRIVE_ME.handle(p);
		}
    	if (p instanceof Player) {
    		p.sendMessage(ChatColor.GREEN + "기관사님 환영합니다. 안전운전 되십시오.");
    		g.getActions().clear();
            g.setForwardForce(0);
            g.getProperties().setSlowingDown(false);
            g.getProperties().setWaitDistance(0);
            g.lctManual.reset();
    		g.lctManual = new LCTManual(g, p.getName());
    		g.lctManual.stopped = true;
    	}
	}
	public void unlink(Player p) {
		if (!group.isManualMovement) {
			return;
		}
		MinecartMember<?> head;
    	if (group.getAverageForce() == 0 && stopped && pilot == p.getName()) {
        	if (p.getVehicle() == group.head().getEntity().getEntity()) {
        		head = group.head();
        	} else if (p.getVehicle() == group.tail().getEntity().getEntity()) {
        		head = group.tail();
        	} else {
        		p.sendMessage(ChatColor.RED + "열차를 분리할 수 없습니다!");
        		return;
        	}
			group.getActions().clear();
			group.setForwardForce(0);
			group.lctManual = new LCTManual(group, "");
			head.clearGroup();
			head.getGroup().getActions().clear();
			head.getGroup().setForwardForce(0);
			head.getGroup().getProperties().load(group.getProperties());
			head.getGroup().getProperties().setWaitDistance(0);
			head.getGroup().lctManual = new LCTManual(head.getGroup(), pilot);
    	}
		p.sendMessage(ChatColor.RED + "열차를 분리할 수 없습니다!");
	}
	public MinecartGroup getGroup() {
		return group;
	}
	public int getNotch() {
		return notch;
	}
	public void setTarget(Location location) {
		targetStation = true;
		targetLoc = location;
	}
	public void clearStation() {
		targetStation = false;
	}
	public void clearTarget() {
		targetSpeed = group.getProperties().getSpeedLimit();
		tsLeftForce = 0;
		tsLeftDistance = 0;
	}
	public void setTarget(double double01) {
		targetSpeed = double01;
	}
	public void setTarget(double dA, double dB) {
		tsLeftForce = dA - targetSpeed;
		tsLeftDistance = dB;
	}
	public void setWaitTarget(double double01) {
		targetDistance = double01;
	}
	private void stopSound() {
		for (MinecartMember<?> m : group) {
			m.sound.stop();
		}
	}
	private Player getDriver() {
		for (MinecartMember<?> member : group) {
			if (member != group.head() && member != group.tail()) {
				continue;
			}
			for (Entity entity : member.getEntity().getEntity().getPassengers()) {
				if (entity.getName() == pilot) {
					return (Player) entity;
				} else {
					continue;
				}
			}
		}
		return null;
	}
	public void leftDoorOpen() {
		Player pilotPlayer = getDriver();
		ldoor = true;
		group.playNamedAnimation("ldopen");
		group.getProperties().setPlayersEnter(true);
		group.getProperties().setPlayersExit(true);
    	for (MinecartMember<?> member2 : group) {
    		for (Entity entity2 : member2.getEntity().getEntity().getPassengers()) {
    			if (entity2 == pilotPlayer) {
    				pilotPlayer.playSound(pilotPlayer.getLocation(), Sound.BLOCK_NOTE_BLOCK_SNARE, 10000.0f, 1.0f);
					pilotPlayer.sendTitle("", ChatColor.GREEN + "[왼쪽 출입문 개방]", 0, 70, 20);
    			} else {
    				entity2.sendMessage(ChatColor.GREEN + "출입문이 열립니다.");
    			}
    		}
    		group.getWorld().playSound(member2.getEntity().getLocation(), Sound.BLOCK_FIRE_EXTINGUISH, 1.5f, 2.0f);
    	}
	}

	public void rightDoorOpen() {
		Player pilotPlayer = getDriver();
		rdoor = true;
		group.playNamedAnimation("rdopen");
		group.getProperties().setPlayersEnter(true);
		group.getProperties().setPlayersExit(true);
    	for (MinecartMember<?> member2 : group) {
    		for (Entity entity2 : member2.getEntity().getEntity().getPassengers()) {
    			if (entity2 == pilotPlayer) {
    				pilotPlayer.playSound(pilotPlayer.getLocation(), Sound.BLOCK_NOTE_BLOCK_SNARE, 10000.0f, 1.0f);
					pilotPlayer.sendTitle("", ChatColor.GREEN + "[오른쪽 출입문 개방]", 0, 70, 20);
    			} else {
    				entity2.sendMessage(ChatColor.GREEN + "출입문이 열립니다.");
    			}
    		}
    		group.getWorld().playSound(member2.getEntity().getLocation(), Sound.BLOCK_FIRE_EXTINGUISH, 1.5f, 2.0f);
    	}
	}
}
