package com.wpi.mqp.musicwearable17.events;

import com.wpi.mqp.musicwearable17.data.TagData;

public class TagAddedEvent {
    private TagData mTagData;

    public TagAddedEvent(TagData pTagData) {
        mTagData = pTagData;
    }

    public TagData getTag() {
        return mTagData;
    }
}
