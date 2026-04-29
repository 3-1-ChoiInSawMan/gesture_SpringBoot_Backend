package chainsawman.gesture.service;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum MediaEntityType {
    PROFILE("profiles"),
    ROOM("rooms"),
    CHAT("chats");

    private final String prefix;
}
