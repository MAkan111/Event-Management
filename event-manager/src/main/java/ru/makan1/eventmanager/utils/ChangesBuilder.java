package ru.makan1.eventmanager.utils;

import ru.makan1.eventcommon.model.ChangeItem;
import ru.makan1.eventmanager.event.dto.EventResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ChangesBuilder {

    public static List<ChangeItem> collectChanges(EventResponse oldValue, EventResponse newValue) {
        List<ChangeItem> changes = new ArrayList<>();

        if (!Objects.equals(oldValue.name(), newValue.name())) {
            changes.add(change("name", oldValue.name(), newValue.name()));
        }

        if (!Objects.equals(oldValue.date(), newValue.date())) {
            changes.add(change("date", oldValue.date(), newValue.date()));
        }

        if (oldValue.duration() != newValue.duration()) {
            changes.add(change("duration", oldValue.duration(), newValue.duration()));
        }

        if (!Objects.equals(oldValue.cost(), newValue.cost())) {
            changes.add(change("cost", oldValue.cost(), newValue.cost()));
        }

        if (oldValue.maxPlaces() != newValue.maxPlaces()) {
            changes.add(change("maxPlaces", oldValue.maxPlaces(), newValue.maxPlaces()));
        }

        if (!Objects.equals(oldValue.locationId(), newValue.locationId())) {
            changes.add(change("locationId", oldValue.locationId(), newValue.locationId()));
        }

        return changes;
    }

    public static ChangeItem change(String field, Object oldValue, Object newValue) {
        ChangeItem c = new ChangeItem();
        c.setField(field);
        c.setOldValue(oldValue == null ? null : String.valueOf(oldValue));
        c.setNewValue(newValue == null ? null : String.valueOf(newValue));
        return c;
    }
}
