package org.schabi.newpipe.settings.tabs;

import androidx.annotation.Nullable;

import com.grack.nanojson.JsonArray;
import com.grack.nanojson.JsonObject;
import com.grack.nanojson.JsonParser;
import com.grack.nanojson.JsonParserException;
import com.grack.nanojson.JsonStringWriter;
import com.grack.nanojson.JsonWriter;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Class to get a JSON representation of a list of tabs, and the other way around.
 */
public final class TabsJsonHelper {
    private static final String JSON_TABS_ARRAY_KEY = "tabs";

    private static final List<Tab> FALLBACK_INITIAL_TABS_LIST = List.of(
            Tab.Type.DEFAULT_KIOSK.getTab(0,"https://www.youtube.com/@%D8%B4%D8%AC%D8%B1%D8%A9_%D8%B7%D9%8A%D8%A8%D8%A9"),
            Tab.Type.FEED.getTab(0,"https://www.youtube.com/@%D8%B4%D8%AC%D8%B1%D8%A9_%D9%85%D8%A8%D8%A7%D8%B1%D9%83%D8%A9"),
            Tab.Type.SUBSCRIPTIONS.getTab(0,"https://www.youtube.com/@ehabkanoo"),
            Tab.Type.BOOKMARKS.getTab(0,"https://www.youtube.com/@%D8%A3%D8%AC%D9%85%D9%84%D8%AA%D9%84%D8%A7%D9%88%D8%A7%D8%AA%D8%A7%D9%84%D9%82%D8%B1%D8%A2%D9%86%D8%A7%D9%84%D9%83%D8%B1%D9%8A%D9%85-%D8%B68%D8%B8"));

    private TabsJsonHelper() { }

    /**
     * Try to reads the passed JSON and returns the list of tabs if no error were encountered.
     * <p>
     * If the JSON is null or empty, or the list of tabs that it represents is empty, the
     * {@link #getDefaultTabs fallback list} will be returned.
     * <p>
     * Tabs with invalid ids (i.e. not in the {@link Tab.Type} enum) will be ignored.
     *
     * @param tabsJson a JSON string got from {@link #getJsonToSave(List)}.
     * @return a list of {@link Tab tabs}.
     * @throws InvalidJsonException if the JSON string is not valid
     */
    public static List<Tab> getTabsFromJson(@Nullable final String tabsJson)
            throws InvalidJsonException {
        if (tabsJson == null || tabsJson.isEmpty()) {
            return getDefaultTabs();
        }

        try {
            final JsonObject outerJsonObject = JsonParser.object().from(tabsJson);

            if (!outerJsonObject.has(JSON_TABS_ARRAY_KEY)) {
                throw new InvalidJsonException("JSON doesn't contain \"" + JSON_TABS_ARRAY_KEY
                        + "\" array");
            }

            final JsonArray tabsArray = outerJsonObject.getArray(JSON_TABS_ARRAY_KEY, null);

            final var returnTabs = tabsArray.streamAsJsonObjects()
                    .map(Tab::from)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toUnmodifiableList());

            return returnTabs.isEmpty() ? getDefaultTabs() : returnTabs;
        } catch (final JsonParserException e) {
            throw new InvalidJsonException(e);
        }
    }

    /**
     * Get a JSON representation from a list of tabs.
     *
     * @param tabList a list of {@link Tab tabs}.
     * @return a JSON string representing the list of tabs
     */
    public static String getJsonToSave(@Nullable final List<Tab> tabList) {
        final JsonStringWriter jsonWriter = JsonWriter.string();
        jsonWriter.object();

        jsonWriter.array(JSON_TABS_ARRAY_KEY);
        if (tabList != null) {
            for (final Tab tab : tabList) {
                tab.writeJsonOn(jsonWriter);
            }
        }
        jsonWriter.end();

        jsonWriter.end();
        return jsonWriter.done();
    }

    public static List<Tab> getDefaultTabs() {
        return FALLBACK_INITIAL_TABS_LIST;
    }

    public static final class InvalidJsonException extends Exception {
        private InvalidJsonException() {
            super();
        }

        private InvalidJsonException(final String message) {
            super(message);
        }

        private InvalidJsonException(final Throwable cause) {
            super(cause);
        }
    }
}
