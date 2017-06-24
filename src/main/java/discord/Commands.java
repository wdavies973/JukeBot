package discord;

import edu.cmu.sphinx.api.Configuration;
import edu.cmu.sphinx.api.LiveSpeechRecognizer;
import edu.cmu.sphinx.api.SpeechResult;
import edu.cmu.sphinx.api.StreamSpeechRecognizer;
import files.IO;
import files.Loader;
import files.PlaylistModel;
import files.Settings;
import modules.Module;
import modules.music.Music;
import music.AudioPlayerReceiveHandler;
import music.AudioPlayerSendHandler;
import music.MasterQueue;
import music.Track;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.entities.VoiceChannel;
import net.dv8tion.jda.core.events.message.guild.GenericGuildMessageEvent;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import net.dv8tion.jda.core.managers.AudioManager;
import tools.Constants;

import java.util.ArrayList;
import java.util.function.Consumer;

/*
 * Listens to channel commands processes them.
 *
 * This is basically the center of activity in this bot
 */
public class Commands extends ListenerAdapter {

    private LiveSpeechRecognizer recongizer;
    private final ArrayList<Module> modules;

    public Commands() {
        modules = new ArrayList<>();
        modules.add(new Music());


        /*Configuration configuration = new Configuration();
        configuration.setAcousticModelPath("resource:/edu/cmu/sphinx/models/en-us/en-us");
        configuration.setDictionaryPath("resource:/edu/cmu/sphinx/models/en-us/cmudict-en-us.dict");
        configuration.setLanguageModelPath("resource:/edu/cmu/sphinx/models/en-us/en-us.lm.bin");

        try {
            recongizer = new LiveSpeechRecognizer(configuration);
        } catch(Exception e) {
            e.printStackTrace();
        }*/
    }

    @Override
    public void onGuildMessageReceived(GuildMessageReceivedEvent event) {
        Settings settings = new Loader().getSettings();
        if(settings == null) new Loader().saveSettings(new Settings());

        for(Module m : modules) {
            m.processCommand(event);
        }

    }

}
