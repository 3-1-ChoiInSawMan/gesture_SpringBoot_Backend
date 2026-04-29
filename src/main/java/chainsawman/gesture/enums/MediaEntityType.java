package chainsawman.gesture.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum MediaEntityType {
    PROFILE("profiles"),
    ROOM("rooms"),
    CHAT("chats"),
    QUICK_SLOT("quick-slots");

    private final String prefix;
}
