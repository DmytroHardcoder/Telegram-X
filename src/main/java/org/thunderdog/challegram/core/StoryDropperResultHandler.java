package org.thunderdog.challegram.core;

import org.drinkless.tdlib.Client;
import org.drinkless.tdlib.TdApi;

/**
 * ResultHandler wrapper that drops known "bloat" TDLib updates such as Stories and Sponsored messages
 * before they reach application listeners. This class performs zero-allocation checks and returns
 * immediately for dropped updates.
 */
public final class StoryDropperResultHandler implements Client.ResultHandler {
    private final Client.ResultHandler delegate;

    public StoryDropperResultHandler(Client.ResultHandler delegate) {
        this.delegate = delegate == null ? object -> {} : delegate;
    }

    @Override
    public void onResult(TdApi.Object object) {
        if (object == null) return; // nothing to do
        if (isBloatUpdate(object)) {
            // drop immediately without logging or allocation
            return;
        }
        delegate.onResult(object);
    }

    public static Client.ResultHandler wrap(Client.ResultHandler delegate) {
        return new StoryDropperResultHandler(delegate);
    }

    public static boolean isBloatUpdate(TdApi.Object object) {
        int c = object.getConstructor();
        switch (c) {
            // Stories-related updates
            case TdApi.UpdateStoryListChatPositions.CONSTRUCTOR:
            case TdApi.UpdateChatActiveStories.CONSTRUCTOR:
            case TdApi.UpdateStory.CONSTRUCTOR:
            case TdApi.UpdateStoryDeleted.CONSTRUCTOR:
            case TdApi.UpdateStorySendSucceeded.CONSTRUCTOR:
            case TdApi.UpdateStorySendFailed.CONSTRUCTOR:
                return true;

            // Sponsored / advertising payloads
            case TdApi.SponsoredMessage.CONSTRUCTOR:
            case TdApi.SponsoredMessages.CONSTRUCTOR:
            case TdApi.UpdateNewSponsoredMessageId.CONSTRUCTOR:
                return true;

            default:
                return false;
        }
    }
}
