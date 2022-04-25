//import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
//import net.dv8tion.jda.api.events.message.GenericMessageEvent;
//import net.dv8tion.jda.api.hooks.ListenerAdapter;
//
//public class Commands extends ListenerAdapter {
//
//    @Override
//    public void onGuildMessageReceived(GuildMessageReceivedEvent event) {
//        System.out.println("kloete");
//    }
//
//    @Override
//    public void onGenericGuildMessage(GenericGuildMessageEvent event) {
//        System.out.println("kut");
//    }
//
//    @Override
//    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
//        if (!event.getAuthor().isBot()) {
//            System.out.println("message received");
//            event.getChannel().sendMessage("test").queue();
//        }
//    }
//
//}