import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.MongoException;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction;
import org.bson.Document;
import org.bson.types.ObjectId;

import javax.security.auth.login.LoginException;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

import static net.dv8tion.jda.api.interactions.commands.OptionType.*;
import static net.dv8tion.jda.api.interactions.commands.OptionType.STRING;

public class Bot extends ListenerAdapter {
    private static MongoDatabase database;

    public static void main(String[] args) throws LoginException {

        // Build MongoClient
        try {
            File file = new File("connectionString.txt");
            ConnectionString connectionString = new ConnectionString(new Scanner(file).nextLine());
            MongoClientSettings settings = MongoClientSettings.builder()
                    .applyConnectionString(connectionString)
                    .build();
            MongoClient mongoClient = MongoClients.create(settings);
            database = mongoClient.getDatabase("userdata");

            // Build JDA
            JDA bot = JDABuilder.createLight("ODkyOTAzMTY1MDc3MzY4ODMy.YVTq3w.9mYmuBzv7FJo4pqhFxSSoby0YWY")
                    .addEventListeners(new Bot())
                    .build();

            // These commands take up to an hour to be activated after creation/update/delete
            CommandListUpdateAction commands = bot.updateCommands();

            // Moderation commands with required options
            commands.addCommands(
                    Commands.slash("submit", "Submit an art piece credited to you, tags optional.")
                            .addOptions(new OptionData(ATTACHMENT, "image", "The art piece to be uploaded.")
                                    .setRequired(true))
                            .addOptions(new OptionData(STRING, "tags", "Optional tags, separate with spaces.")),

                    //TODO: IMPLEMENT
                    Commands.slash("gallery", "Display a gallery of a user's works.")
                            .addOptions(new OptionData(USER, "artist", "The artist to search for.")
                                    .setRequired(true))
                            .addOptions(new OptionData(STRING, "tags", "Optional tags, separate with spaces.")),

                    //TODO: IMPLEMENT
                    Commands.slash("profile", "See an artist's stats and social media links.")
                            .addOptions(new OptionData(USER, "artist", "The artist to search for.")
                                    .setRequired(true)),

                    //TODO: IMPLEMENT
                    Commands.slash("showcase", "Display a showcase of works from the specified time period, tags optional.")
                            .addOptions(new OptionData(STRING, "time", "The time period to select works from.")
                                    .setRequired(true).addChoice("Last week", "week").addChoice("Last month", "month"))
                            .addOptions(new OptionData(STRING, "tags", "Optional tags, separate with spaces."))
            );

            // Send the new set of commands to discord, this will override any existing global commands with the new set provided here
            commands.queue();
        } catch (FileNotFoundException e) {
            System.out.println("Connection string file not found.");
            e.printStackTrace();
        }
    }


    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        // Only accept commands from guilds
        if (event.getGuild() == null)
            return;
        switch (event.getName()) {
            case "submit":
                submit(event);
            default:
                event.reply("I can't handle that command right now.").setEphemeral(true).queue();
        }
    }

    public void submit(SlashCommandInteractionEvent event) {
        event.deferReply().queue(); // tell the user we are thinking while handling
        InteractionHook hook = event.getHook();
        hook.setEphemeral(false);

        // SUBMISSION INFO
        String userID = event.getMember().getId();
        Message.Attachment attachment = event.getOption("image").getAsAttachment();
        if (!attachment.isImage()) {
            hook.setEphemeral(true).sendMessage("Submission failed: Attachment must be an image!").queue(); //Only user can see failed upload message
            return;
        }
        Date date = new Date(); //current date
        List<String> tags = new ArrayList<>();
        if (event.getOption("tags") != null) {
            tags = Arrays.asList(event.getOption("tags").getAsString().split("\\s+"));
        }

        // WRITE TO DATABASE
        try {
            writeSubmission(userID, attachment, date, tags);
            hook.sendMessage("Submission successful.").queue();
        } catch (MongoException me) {
            hook.sendMessage("Your submission failed to upload. Please try again!").queue();
        }
        //TODO: make confirmation message an embed
    }

    public void gallery(SlashCommandInteractionEvent event) {
        event.deferReply().queue(); // tell the user we are thinking while handling
        InteractionHook hook = event.getHook();
        hook.setEphemeral(false);

        // SUBMISSION INFO
        User artist = event.getOption("artist").getAsUser();

    }

    public void writeSubmission(String userID, Message.Attachment attachment, Date date, List<String> tags) throws MongoException {
        MongoCollection<org.bson.Document> userCollection = database.getCollection(userID);
        userCollection.insertOne(new Document()
                .append("_id", new ObjectId())
                .append("userID", userID)
                .append("imageLink", attachment.getUrl())
                .append("date", date)
                .append("tags", tags)
                .append("likes", 0));
    }

}
