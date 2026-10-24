package io.github.werl1ck.discord;

import dev.firstdark.rpc.DiscordRpc;
import dev.firstdark.rpc.enums.ActivityType;
import dev.firstdark.rpc.enums.ErrorCode;
import dev.firstdark.rpc.handlers.RPCEventHandler;
import dev.firstdark.rpc.models.DiscordRichPresence;
import dev.firstdark.rpc.models.User;
import org.jetbrains.annotations.Nullable;

public class DiscordRPC {
    private final DiscordRpc rpc = new DiscordRpc();

    private static final String APPLICATION_ID = "1563533795007930398";

    private volatile boolean connected = false;

    public void rpcConenct() throws Exception{
        rpc.setDebugMode(false);

        RPCEventHandler handler = new RPCEventHandler() {
            @Override
            public void ready(User user) {
                connected = true;

                DiscordRichPresence presence = DiscordRichPresence.builder()
                        .details("In the console")
                        .state("Is typing...")
                        .largeImageKey("launcher_logo")
                        .largeImageText("bro, delete windows please")
                        .activityType(ActivityType.PLAYING)
                        .button(DiscordRichPresence.RPCButton.of(
                                "View repository",
                                "https://github.com/werl1ck"
                        ))
                        .build();

                rpc.updatePresence(presence);
            }

            @Override
            public void disconnected(
                    ErrorCode errorCode,
                    @Nullable String message
            ) {
                connected = false;
            }

            @Override
            public void errored(
                    ErrorCode errorCode,
                    @Nullable String message
            ) {
                connected = false;
            }
        };

        rpc.init(APPLICATION_ID, handler, false);
    }

    public void updateStatus(String details, String state) {
        if (!connected) {
            return;
        }

        DiscordRichPresence presence = DiscordRichPresence.builder()
                .details(details)
                .state(state)
                .largeImageKey("launcher_logo")
                .largeImageText("bro, delete windows please")
                .activityType(ActivityType.PLAYING)
                .button(DiscordRichPresence.RPCButton.of(
                        "View repository",
                        "https://github.com/werl1ck"
                ))
                .build();

        rpc.updatePresence(presence);
    }
}
