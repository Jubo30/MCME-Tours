package com.mcmiddleearth.mcmetours.util;

import net.md_5.bungee.api.ChatColor;

/**
 * @author Jubo
 */
public enum ChatRanks {

    HOST (ChatColor.DARK_AQUA+"<Tour Host> "),
    PARTICIPANT (ChatColor.GRAY+"<Participant> "),
    BADGEHOLDER (ChatColor.BLUE+"<Participant> ");

    private final String chatPrefix;

    ChatRanks(String chatPrefix){
        this.chatPrefix = chatPrefix;
    }

    public String getChatPrefix(){
        return chatPrefix;
    }
}