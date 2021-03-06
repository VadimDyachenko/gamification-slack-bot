package juja.microservices.gamification.slackbot.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import juja.microservices.gamification.slackbot.exceptions.WrongCommandFormatException;
import lombok.Getter;
import lombok.ToString;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Nikol on 3/4/2017.
 */
@Getter
@ToString
@JsonIgnoreProperties({"parcedUuidPattern", "parcedUuidStartMarker",
        "parcedUuidFinishMarker", "command_EXAMPLE"})
public class CodenjoyAchievement {
    @JsonProperty
    private String from;
    @JsonProperty
    private String firstPlace;
    @JsonProperty
    private String secondPlace;
    @JsonProperty
    private String thirdPlace;

    private String parcedUuidPattern = "@#([a-zA-z0-9\\.\\_\\-]){1,21}#@";
    private String parcedUuidStartMarker = "@#";
    private String parcedUuidFinishMarker = "#@";
    private final String COMMAND_EXAMPLE = "/codenjoy -1th @slack_nick_name -2th @slack_nick_name2 -3th @slack_nick_name3";

    public CodenjoyAchievement(String from,
                               String firstPlace,
                               String secondPlace,
                               String thirdPlace) {
        this.from = from;
        this.firstPlace = firstPlace;
        this.secondPlace = secondPlace;
        this.thirdPlace = thirdPlace;
    }

    public CodenjoyAchievement(String fromUserUuid, String text) {
        this.from = fromUserUuid;
        this.firstPlace = findUuidForToken("-1th", text);
        this.secondPlace = findUuidForToken("-2th", text);
        this.thirdPlace = findUuidForToken("-3th", text);
    }

    private String findUuidForToken(String token, String text) {
        checkText(text, token);
        String[] splitedText = text.split("(?=-[123]th)");
        String uuid = findUuid(token, splitedText);
        return cleanTheUuidOfMarkers(uuid);
    }

    private void checkText(String text, String token) {
        if (!text.contains(token)) {
            throw new WrongCommandFormatException(String.format("token '%s' not found. Example for this command \"%s\"", token, COMMAND_EXAMPLE));
        }
        int count;
        if ((count = text.split(token).length) > 2) {
            throw new WrongCommandFormatException(String.format("token '%s' used %d times, but expected 1. Example for this command \"%s\"", token, count - 1, COMMAND_EXAMPLE));
        }
    }

    private String findUuid(String token, String[] splitedText) {
        for (String s : splitedText) {
            if (s.contains(token)) {
                Pattern uuidPattern = Pattern.compile(parcedUuidPattern);
                Matcher matcher = uuidPattern.matcher(s.substring(s.indexOf(token)));
                if (matcher.find()) {
                    return cleanTheUuidOfMarkers(matcher.group());
                }
            }
        }
        throw new WrongCommandFormatException(String.format("Not found username for token '%s'. Example for this command %s", token, COMMAND_EXAMPLE));
    }

    private String cleanTheUuidOfMarkers(String uuidWithMarkers) {
        return uuidWithMarkers.replaceAll(parcedUuidStartMarker, "")
                .replaceAll(parcedUuidFinishMarker, "");
    }
}