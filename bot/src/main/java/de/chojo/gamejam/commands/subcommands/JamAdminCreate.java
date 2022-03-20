package de.chojo.gamejam.commands.subcommands;

import de.chojo.gamejam.data.JamData;
import de.chojo.gamejam.data.wrapper.jam.JamBuilder;
import de.chojo.gamejam.data.wrapper.jam.JamTimes;
import de.chojo.gamejam.data.wrapper.jam.TimeFrame;
import de.chojo.jdautil.wrapper.SlashCommandContext;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;

import java.time.DateTimeException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class JamAdminCreate implements SubCommand {

    private final DateTimeFormatter dateParser = DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm");

    private final JamData jamData;

    public JamAdminCreate(JamData jamData) {
        this.jamData = jamData;
    }

    @Override
    public void execute(SlashCommandInteractionEvent event, SlashCommandContext context) {
        var topic = String.join("\n", event.getOption("topic").getAsString(), event.getOption("topic-tagline", "", OptionMapping::getAsString));
        ZoneId timezone;
        try {
            timezone = ZoneId.of(event.getOption("timezone").getAsString());
        } catch (DateTimeException e) {
            event.reply("Invalid timezone.").queue();
            return;
        }

        var jamBuilder = new JamBuilder(-1)
                .setTopic(topic);
        try {
            var registerStart = parseTime(event.getOption("register-start").getAsString(), timezone);
            var registerEnd = parseTime(event.getOption("register-end").getAsString(), timezone);
            var jamStart = parseTime(event.getOption("jam-start").getAsString(), timezone);
            var jamEnd = parseTime(event.getOption("jam-end").getAsString(), timezone);
            var times = new JamTimes(timezone, new TimeFrame(registerStart, registerEnd), new TimeFrame(jamStart, jamEnd));
            jamBuilder.setTimes(times);
        } catch (DateTimeException e) {
            event.reply("Invalid time format. Format is YYYY.MM.DD hh:mm.").queue();
            return;
        }

        jamData.createJam(jamBuilder.build(), event.getGuild());
        event.reply("Jam created.").setEphemeral(true).queue();
    }


    private ZonedDateTime parseTime(String time, ZoneId zoneId) throws DateTimeException {
        var parsed = LocalDateTime.from(dateParser.parse(time));
        return ZonedDateTime.ofInstant(parsed, zoneId.getRules().getOffset(parsed), zoneId);
    }
}
