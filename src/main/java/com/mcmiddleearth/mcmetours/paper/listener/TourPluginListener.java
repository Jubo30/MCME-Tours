package com.mcmiddleearth.mcmetours.paper.listener;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;
import com.mcmiddleearth.mcmetours.discord.DiscordMessageType;
import com.mcmiddleearth.mcmetours.paper.Channel;
import com.mcmiddleearth.mcmetours.paper.ToursPaperPlugin;
import com.mcmiddleearth.mcmetours.paper.functions.*;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Consumer;
import org.jetbrains.annotations.NotNull;

/**
 * @author Jubo
 */
public class TourPluginListener implements PluginMessageListener {

    public TourPluginListener(){}

    @Override
    public void onPluginMessageReceived(@NotNull String channel, @NotNull Player player, @NotNull byte[] message) {
        if(!channel.equals(Channel.MAIN)){
            return;
        }
        ByteArrayDataInput in = ByteStreams.newDataInput(message);
        String subchannel = in.readUTF();

        if(subchannel.equals(Channel.HAT)){
            String playerData = in.readUTF();
            TourHatPaper.TourHat(playerData);
        }else if(subchannel.equals(Channel.REFRESHMENTS)){
            String playerData = in.readUTF();
            TourRefreshmentsPaper.giveRefreshments(Bukkit.getPlayer(playerData));
        }else if(subchannel.equals(Channel.GLOW)){
            String playerData = in.readUTF();
            Boolean bool = in.readBoolean();
            runAfterArrival(playerData,source -> {
                TourGlowPaper.setGlow(playerData,bool);
            });
        }else if(subchannel.equals(Channel.DISCORD)){
            String messageType = in.readUTF();
            if(messageType.equals(DiscordMessageType.ANNOUNCEMENT)){
                TourDiscordPaper.AnnounceTour(in.readUTF(),in.readUTF(),in.readBoolean(),in.readUTF());
            }else if(messageType.equals(DiscordMessageType.END)){
                TourDiscordPaper.EndTour(in.readUTF());
            }else if(messageType.equals(DiscordMessageType.REQUEST)){
                TourDiscordPaper.RequestTour(in.readBoolean(),in.readUTF(),in.readUTF());
            }
        }else if(subchannel.equals(Channel.TP)){
            String sender = in.readUTF();
            String target = in.readUTF();
            runAfterArrival(sender,source -> {
                TourTeleportPaper.teleportPlayer(source,Bukkit.getPlayer(target));
            });
        }
    }

    private void runAfterArrival(String playerName, Consumer<Player> callback) {
        new BukkitRunnable() {
            int counter = 40;
            @Override
            public void run() {
                Player source = Bukkit.getPlayer(playerName);
                if(source==null) {
                    if(counter==0) {
                        cancel();
                    } else {
                        counter--;
                    }
                } else {
                    callback.accept(source);
                    cancel();
                }
            }
        }.runTaskTimer(ToursPaperPlugin.getInstance(), 1, 10);
    }
}
