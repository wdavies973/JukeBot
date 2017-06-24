package modules.music;

import models.Loader;
import models.PlaylistModel;
import models.Settings;
import modules.Module;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;

import java.util.ArrayList;

/**
 * Manages commands and music playing for JukeBot.
 */
public class Music extends Module {

    private Queue queue;
    private Settings settings;

    public Music() {
        queue = new Queue();
        settings = new Loader().getSettings();
    }

    @Override
    public boolean processCommand(final GuildMessageReceivedEvent event){
        String message = event.getMessage().getRawContent();

        try {
            if (message.startsWith("add")) {
                queue.add(new Track(message.split("\\s+")[1]));
                String reply;
                if (queue.getSongsInQueue() == 1) reply = "Added to queue. There is 1 song in the queue.";
                else reply = "Added to queue. There are " + queue.getSongsInQueue() + " songs in the queue.";
                reply(event, reply, true);
                return true;
            } else if (message.equals("play") && (getRole(event) >= MOD || queue.getSongsInQueue() == 0)) {
                if(queue.getSongsInQueue() == 0) {
                    reply(event, "There aren't any songs in the queue. :(", true);
                    return true;
                }
                smartSummon(event, queue);
                queue.play();
                reply(event, "♫♫♫♫", true);
                return true;
            }

            if (getRole(event) < MOD) return false;

            if (message.equals("skip")) {
                queue.skip();
                reply(event, "Skipping song...", true);
                return true;
            } else if (message.equals("back")) {
                queue.back();
                reply(event, "Playing previous song...", true);
                return true;
            } else if (message.equals("stop")) {
                queue.clear();
                reply(event, "Queue cleared", true);
                return true;
            } else if (message.equals("pause")) {
                queue.pause();
                reply(event, "Paused. Type resume to resume music.", true);
                return true;
            } else if(message.equals("resume")) {
                queue.resume();
                reply(event, "Resuming tunes...", true);
                return true;
            } else if (message.startsWith("!vol")) {
                queue.setVolume(Integer.parseInt(message.split("\\s+")[1]));
                return true;
            } else if (message.startsWith("!playnow")) {
                queue.addNext(new Track(message.split("\\s+")[1]));
                queue.skip();
                return true;
            } else if(message.startsWith("!summon")) {
                if(message.split("\\s+").length == 2) {
                    summonByName(event, queue, message.split("\\s+")[1]);
                    return true;
                }
                else if(message.split("\\s+").length == 1) {
                    smartSummon(event, queue);
                    return true;
                }
            }


        /* PLAYLIST MANAGEMENT */
            String[] tokens = message.split("\\s+");
            if (message.equals("!ls")) {
                ArrayList<PlaylistModel> playlists = settings.getPlaylists();
                if (playlists == null || playlists.size() == 0) {
                    reply(event, "No playlists found. Use !pcreate to create a playlist.", true);
                    return true;
                }
                String temp = "";
                for (int i = 0; i < playlists.size(); i++) {
                    temp += playlists.get(i).getName() + ", ";
                    if (i == playlists.size() - 1) temp = temp.substring(0, temp.length() - 2);
                }
                reply(event, "Found " + playlists.size() + " playlist(s): " + temp, true);
                return true;
            }
            else if (message.startsWith("!pcreate")) {
                if (tokens.length == 1) {
                    reply(event, "You must specify a playlist name.", true);
                    return true;
                } else if (tokens.length == 2) { // creating blank playlist
                    settings.addPlaylist(tokens[1], new ArrayList<>());
                    reply(event, "Created playlist: " + tokens[1] + ". Use !p <name> add <url> to start adding songs.", true);
                    new Loader().saveSettings(settings);
                    return true;
                } else if (tokens[2].contains("spotify")) { // creating from Spotify playlist
                    // Smart playlist getter
                    reply(event, "Accessing Spotify and YouTube databases. This will take about 30 seconds.", true);
                    ArrayList<Track> tracks = new SpotifyToYoutube().convert(tokens[2]);
                    settings.addPlaylist(tokens[1], tracks);
                    reply(event, "Created playlist: " + tokens[1] + " with " + tracks.size() + " songs. Use !p "+tokens[1]+ " to play your new playlist.", false);
                    new Loader().saveSettings(settings);
                    return true;
                } else if (tokens[2].equals("q")) {
                    settings.addPlaylist(tokens[1], queue.getTracks());
                    reply(event, "Created playlist: " + tokens[1] + " from current queue with " + queue.getSongsInQueue() + " tracks.", true);
                    new Loader().saveSettings(settings);
                    return true;
                }
            } else if (message.startsWith("!p ")) {
                if (tokens.length == 2) {
                    PlaylistModel pm = settings.getPlaylist(tokens[1]);
                    if (pm == null) {
                        reply(event, "Playlist: " + tokens[1] + " does not exist.", true);
                        return true;
                    }
                    queue.clear();
                    smartSummon(event, queue);
                    for (Track t : pm.getTracks()) queue.add(t);
                    queue.play();
                    reply(event, "Playing: " + tokens[1] + ".", true);
                    return true;
                }
                else if (tokens.length == 3 && tokens[2].equals("del")) {
                    PlaylistModel pm = settings.getPlaylist(tokens[2]);
                    if (pm == null) {
                        reply(event, "Playlist: " + tokens[1] + " does not exist.", true);
                        return true;
                    }
                    settings.remove(pm);
                    new Loader().saveSettings(settings);
                    reply(event, "Playlist: " + tokens[1] + " was deleted.", true);
                }
                else if (tokens[2].equals("add") && tokens.length == 4) {
                    PlaylistModel pm = settings.getPlaylist(tokens[1]);
                    if (pm == null) {
                        reply(event, "Playlist: " + tokens[1] + " does not exist.", true);
                        return true;
                    }
                    pm.addTrack(new Track(tokens[3]));
                    settings.remove(pm);
                    settings.addPlaylist(tokens[1], pm.getTracks());
                    reply(event, "1 song was added to playlist: " + tokens[1] + ".", true);
                    new Loader().saveSettings(settings);
                    return true;
                }
            }

        } catch(Exception e) {
            e.printStackTrace();
            reply(event, "Incorrect syntax. Type !help for help.", true);
            return false;
        }

        return false;
    }

    @Override
    public boolean processVoiceCommand(final String command) {
        return false;
    }

}
