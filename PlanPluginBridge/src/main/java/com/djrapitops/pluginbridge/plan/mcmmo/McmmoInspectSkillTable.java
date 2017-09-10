/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.djrapitops.pluginbridge.plan.mcmmo;

import com.gmail.nossr50.datatypes.player.McMMOPlayer;
import com.gmail.nossr50.datatypes.player.PlayerProfile;
import com.gmail.nossr50.datatypes.skills.SkillType;
import com.gmail.nossr50.util.player.UserManager;
import main.java.com.djrapitops.plan.data.additional.PluginData;
import main.java.com.djrapitops.plan.utilities.html.Html;
import org.apache.commons.lang.StringUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.bukkit.Bukkit.getOfflinePlayer;

/**
 * PluginData class for McMMO-plugin.
 * <p>
 * Registered to the plugin by McmmoHook
 *
 * @author Rsl1122
 * @see McmmoHook
 * @since 3.2.1
 */
public class McmmoInspectSkillTable extends PluginData {

    /**
     * Class Constructor, sets the parameters of the PluginData object.
     */
    public McmmoInspectSkillTable() {
        super("McMMO", "inspect_skill_table");
        super.setAnalysisOnly(false);
        final String skill = Html.FONT_AWESOME_ICON.parse("star") + " Skill";
        final String level = Html.FONT_AWESOME_ICON.parse("plus") + " Level";
        final String notice = "Only online players shown. " + Html.LINK_EXTERNAL.parse("https://github.com/mcMMO-Dev/mcMMO/blob/master/src/main/java/com/gmail/nossr50/util/player/UserManager.java#L105", "More info") + "<br>";
        super.setPrefix(notice + Html.TABLE_START_2.parse(skill, level));
        super.setSuffix(Html.TABLE_END.parse());
    }

    @Override
    public String getHtmlReplaceValue(String modifierPrefix, UUID uuid) {
        McMMOPlayer user = UserManager.getOfflinePlayer(getOfflinePlayer(uuid));
        if (user == null) {
            return parseContainer("", Html.TABLELINE_2.parse("User not known/online", ""));
        }
        final PlayerProfile skillProfile = user.getProfile();
        final List<SkillType> skills = new ArrayList<>();
        skills.addAll(Arrays.stream(SkillType.values()).distinct().collect(Collectors.toList()));
        final StringBuilder html = new StringBuilder();
        for (SkillType skill : skills) {
            html.append(Html.TABLELINE_2.parse(StringUtils.capitalize(skill.getName().toLowerCase()), skillProfile.getSkillLevel(skill)));
        }
        return parseContainer("", html.toString());
    }

    @Override
    public Serializable getValue(UUID uuid) {
        return -1;
    }
}
