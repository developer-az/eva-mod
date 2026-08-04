package com.eva.evamod.mail;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.List;

/** A short letter from an NPC (birthday reminders, festival invites, heart notes). */
public record MailMessage(String from, String subject, String body, long day, boolean read) {
    public static final Codec<MailMessage> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("from").forGetter(MailMessage::from),
            Codec.STRING.fieldOf("subject").forGetter(MailMessage::subject),
            Codec.STRING.fieldOf("body").forGetter(MailMessage::body),
            Codec.LONG.fieldOf("day").forGetter(MailMessage::day),
            Codec.BOOL.fieldOf("read").forGetter(MailMessage::read)
    ).apply(instance, MailMessage::new));

    public static final Codec<List<MailMessage>> LIST_CODEC = CODEC.listOf();

    public MailMessage markRead() {
        return new MailMessage(from, subject, body, day, true);
    }

    public static List<MailMessage> copyList(List<MailMessage> source) {
        return new ArrayList<>(source);
    }
}
