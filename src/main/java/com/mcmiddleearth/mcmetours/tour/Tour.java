package com.mcmiddleearth.mcmetours.tour;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import com.mcmiddleearth.mcmetours.data.ChatRanks;
import com.mcmiddleearth.mcmetours.data.Permission;
import com.mcmiddleearth.mcmetours.data.PluginData;
import com.mcmiddleearth.mcmetours.data.Style;
import com.mcmiddleearth.mcmetours.discord.TourDiscordHandler;
import com.mcmiddleearth.mcmetours.paper.Channel;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Jubo
 */
public class Tour {

    private ProxiedPlayer host;
    private List<ProxiedPlayer> players = new ArrayList<>();
    private List<ProxiedPlayer> tourChat = new ArrayList<>();
    private final TourDiscordHandler discordHandler;
    private List<ProxiedPlayer> coHost = new ArrayList<>();
    private String info = null;
    private boolean glow;

    public Tour(ProxiedPlayer host){
        this.host = host;
        glow = true;
        players.add(host);
        tourChat.add(host);
        glowHandle(host,glow);
        coHost.add(host);
        discordHandler = new TourDiscordHandler(host);
    }

    public void addPlayer(ProxiedPlayer player){
        players.add(player);
        tourChat.add(player);
        //TpHandler.handle(player.getName(),host.getServer().getInfo().getName(),host.getName());
        teleportHandle(player,host);
        notifyTour("Everyboy welcome "+Style.HIGHLIGHT+player.getName()+Style.INFO+" to the tour!");
        PluginData.getMessageUtil().sendInfoMessage(player,"Welcome to the tour. For the best experience, join "+ Style.HIGHLIGHT_STRESSED+host.getName()+Style.INFO+" in Discord!");
    }

    public void removePlayer(ProxiedPlayer player){
        if(player == host){
            endTour();
            return;
        }
        players.remove(player);
        tourChat.remove(player);
        glowHandle(player,false);
        notifyTour(player.getName()+" left the tour.");
        PluginData.getMessageUtil().sendInfoMessage(player, "You left the tour.");
    }

    public void endTour(){
        notifyTour("The tour has ended.");
        players.clear();
        tourChat.clear();
        PluginData.removeTour(this);
        for(ProxiedPlayer player : coHost){
            glowHandle(player,false);
        }
        discordHandler.endTour();
        coHost.clear();
    }

    public void TeleportToHost(ProxiedPlayer player){
        //TpHandler.handle(player.getName(),host.getServer().getInfo().getName(),host.getName());
        teleportHandle(player,host);
        PluginData.getMessageUtil().sendInfoMessage(player,"You were teleport to "+host.getName()+".");
    }

    public void teleportPlayer(ProxiedPlayer player){
        if(players.contains(player)){
            //TpHandler.handle(player.getName(),host.getServer().getInfo().getName(),host.getName());
            teleportHandle(player,host);
            PluginData.getMessageUtil().sendInfoMessage(player,host.getName()+" teleported you to them.");
            PluginData.getMessageUtil().sendInfoMessage(host,"You teleported "+player.getName()+" to yourself.");
        }else{
            PluginData.getMessageUtil().sendErrorMessage(host,"This player is not part of the tour.");
        }
    }

    public void teleportAll(){
        for(ProxiedPlayer player: players){
            if(player != host){
                //TpHandler.handle(player.getName(),host.getServer().getInfo().getName(),host.getName());
                teleportHandle(player,host);
                PluginData.getMessageUtil().sendInfoMessage(player,host.getName()+" teleported you to them.");
            }
        }
        PluginData.getMessageUtil().sendInfoMessage(host, "Players teleported.");
    }

    public void activateTourChat(ProxiedPlayer player){
        if(tourChat.contains(player)){
            tourChat.remove(player);
            PluginData.getMessageUtil().sendInfoMessage(player,"Tour-Chat deactivated.");
        }else{
            tourChat.add(player);
            PluginData.getMessageUtil().sendInfoMessage(player,"Tour-Chat activated.");
        }
    }

    public void tourChat(ProxiedPlayer player, String message){
        String ChatMessage;
        for(ProxiedPlayer receiver : tourChat){
            if(coHost.contains(player)){
                ChatMessage = ChatRanks.HOST.getChatPrefix() + player.getName() + ChatColor.WHITE + ": "  + message;
            }else if(player.hasPermission(Permission.HOST.getPermissionNode())){
                ChatMessage = ChatRanks.BADGEHOLDER.getChatPrefix() + player.getName() + ChatColor.WHITE + ": " + message;
            }else{
                ChatMessage = ChatRanks.PARTICIPANT.getChatPrefix() + player.getName() + ChatColor.WHITE + ": " + message;
            }
            receiver.sendMessage(new ComponentBuilder(ChatMessage).create());
        }
    }

    public void kickPlayer(ProxiedPlayer player){
        if(player != host){
            removePlayer(player);
            PluginData.getMessageUtil().sendErrorMessage(player,"You were kicked from the tour. Think about it!");
            PluginData.getMessageUtil().sendInfoMessage(host,"You kicked " + player.getName() + " from the tour.");
        }else{
            PluginData.getMessageUtil().sendErrorMessage(player,"You can´t kick yourself idiot.");
        }
    }

    public void giveRefreshments(){
        for(ProxiedPlayer player: players){
            refreshmentsHandle(player);
            PluginData.getMessageUtil().sendInfoMessage(player,"You were given refreshments");
        }
    }

    public void switchGlow(){
        if(!glow){
            glow = true;
            PluginData.getMessageUtil().sendInfoMessage(host,"You switchet the glow effect on");
        }else{
            PluginData.getMessageUtil().sendInfoMessage(host,"You switchet the glow effect off");
            glow = false;
        }
        for(ProxiedPlayer player: coHost){
            glowHandle(player,glow);
        }
    }

    public void setGlow(ProxiedPlayer player){
        glowHandle(player,glow);
    }

    public void setHost(ProxiedPlayer host){
        this.host = host;
        discordHandler.setSender(host);
    }

    public void setCoHost(ProxiedPlayer coHost){
        glowHandle(coHost,glow);
        this.coHost.add(coHost);
    }

    public void tourList(){
        Set<String> list;
        list = players.stream().map(ProxiedPlayer::getName).collect(Collectors.toSet());
        PluginData.getMessageUtil().sendInfoMessage(host,ChatColor.WHITE+list.toString());
    }

    private void notifyTour(String text){
        for(ProxiedPlayer player: players){
            PluginData.getMessageUtil().sendInfoMessage(player,text);
        }
    }

    public void setInfoText(String info){
        this.info = info;
    }

    public void sendDAnnouncement(){
        PluginData.getMessageUtil().sendBroadcastMessage(host.getName()+" is hosting a tour. Do "+Style.STRESSED+"/tour join "+ host.getName()+Style.INFO+ " to join the tour");
        discordHandler.AnnnounceTour(info);
    }

    private boolean glowHandle(ProxiedPlayer sender, boolean bool){
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF(Channel.GLOW);
        out.writeUTF(sender.getName());
        out.writeUTF(Boolean.toString(bool));
        ProxyServer.getInstance().getServerInfo(sender.getServer().getInfo().getName()).sendData(Channel.MAIN, out.toByteArray(),true);
        return true;
    }

    private boolean refreshmentsHandle(ProxiedPlayer sender){
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF(Channel.REFRESHMENTS);
        out.writeUTF(sender.getName());
        ProxyServer.getInstance().getServerInfo(sender.getServer().getInfo().getName()).sendData(Channel.MAIN,out.toByteArray(),true);
        return true;
    }

    private boolean teleportHandle(ProxiedPlayer sender, ProxiedPlayer target){
        if(!sender.getServer().getInfo().equals(target.getServer().getInfo())){
            sender.connect(target.getServer().getInfo());
        }
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF(Channel.TP);
        out.writeUTF(sender.getName());
        out.writeUTF(target.getName());
        ProxyServer.getInstance().getServerInfo(target.getServer().getInfo().getName()).sendData(Channel.MAIN, out.toByteArray(),true);
        return true;
    }

    public List<ProxiedPlayer> getCoHost(){return coHost;}
    public ProxiedPlayer getHost() {return host;}
    public List<ProxiedPlayer> getPlayers() { return players;}
    public List<ProxiedPlayer> getTourChat() { return tourChat;}
}
