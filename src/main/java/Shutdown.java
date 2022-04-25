import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;

import javax.security.auth.login.LoginException;

public class Shutdown {

    public static void shutdown() throws LoginException {
        JDA bot = JDABuilder.createLight("ODkyOTAzMTY1MDc3MzY4ODMy.YVTq3w.9mYmuBzv7FJo4pqhFxSSoby0YWY")
                .addEventListeners(new Bot())
                .build();

        bot.shutdown();
    }
}
