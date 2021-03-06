/*
 * This source file is subject to the license that is bundled with this package in the file LICENSE.
 */
package com.montealegreluis.yelpv3.jsonparser;

import com.montealegreluis.yelpv3.businesses.*;
import com.montealegreluis.yelpv3.businesses.distance.Distance;
import com.montealegreluis.yelpv3.parser.ParsingFailure;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

class BusinessParser {
    static BusinessDetails detailsFrom(JSONObject information) {
        try {
            return new BusinessDetails(
                information.getDouble("rating"),
                information.has("price") ? PricingLevel.fromSymbol(information.getString("price")) : PricingLevel.NONE,
                information.getString("phone"),
                information.getString("id"),
                information.getBoolean("is_closed"),
                buildCategories(information.getJSONArray("categories")),
                information.getInt("review_count"),
                information.getString("name"),
                new URL(information.getString("url")),
                CoordinatesParser.from(information.getJSONObject("coordinates")),
                new URL(information.getString("image_url")),
                LocationParser.from(information.getJSONObject("location")),
                !information.isNull("distance") ? Distance.inMeters(information.getDouble("distance")) : null,
                buildTransactions(information.getJSONArray("transactions")),
                !information.isNull("is_claimed") && information.getBoolean("is_claimed"),
                !information.isNull("photos") ? buildPhotos(information.getJSONArray("photos")) : null,
                !information.isNull("hours") ? ScheduleParser.from(information.getJSONArray("hours")) : null
            );
        } catch (JSONException | MalformedURLException exception) {
            throw ParsingFailure.producedBy(information, exception);
        }
    }

    static Business businessFrom(JSONObject information) {
        try {
            return new Business(
                information.getDouble("rating"),
                information.has("price") ? PricingLevel.fromSymbol(information.getString("price")) : PricingLevel.NONE,
                information.getString("phone"),
                information.getString("id"),
                information.getBoolean("is_closed"),
                buildCategories(information.getJSONArray("categories")),
                information.getInt("review_count"),
                information.getString("name"),
                new URL(information.getString("url")),
                CoordinatesParser.from(information.getJSONObject("coordinates")),
                !information.getString("image_url").trim().isEmpty() ? new URL(information.getString("image_url")) : null,
                LocationParser.from(information.getJSONObject("location")),
                !information.isNull("distance") ? Distance.inMeters(information.getDouble("distance")) : null,
                buildTransactions(information.getJSONArray("transactions"))
            );
        } catch (JSONException | MalformedURLException exception) {
            throw ParsingFailure.producedBy(information, exception);
        }
    }

    private static Categories buildCategories(JSONArray businessCategories) {
        List<Category> categories = new ArrayList<>();
        for (int i = 0; i < businessCategories.length(); i++)
            categories.add(CategoryParser.from(businessCategories.getJSONObject(i)));
        return new Categories(categories);
    }

    private static List<Transaction> buildTransactions(JSONArray registeredTransactions) {
        List<Transaction> transactions = new ArrayList<>();
        for (int i = 0; i < registeredTransactions.length(); i++)
            transactions.add(new Transaction(registeredTransactions.getString(i)));
        return transactions;
    }

    private static List<URL> buildPhotos(JSONArray businessPhotos) throws MalformedURLException {
        List<URL> photos = new ArrayList<>();

        for (int i = 0; i < businessPhotos.length(); i++)
            photos.add(new URL(businessPhotos.getString(i)));

        return photos;
    }
}

class CategoryParser {
    static Category from(JSONObject category) {
        return new Category(
            category.getString("alias"),
            category.getString("title")
        );
    }
}

class CoordinatesParser {
    static Coordinates from(JSONObject coordinates) {
        return new Coordinates(
            !coordinates.isNull("latitude") ? coordinates.getDouble("latitude") : 0,
            !coordinates.isNull("longitude") ? coordinates.getDouble("longitude") : 0
        );
    }
}

class LocationParser {
    static Location from(JSONObject location) {
        return new Location(
            !location.isNull("address1") ? location.getString("address1") : null,
            !location.isNull("address2") ? location.getString("address2") : null,
            !location.isNull("address3") ? location.getString("address3") : null,
            location.getString("city"),
            location.getString("state"),
            location.getString("country"),
            location.getString("zip_code"),
            !location.isNull("cross_streets") ? location.getString("cross_streets") : "",
            !location.isNull("display_address") ? setDisplayAddress(location.getJSONArray("display_address")) : null
        );
    }

    private static List<String> setDisplayAddress(JSONArray businessDisplayAddress) {
        List<String> displayAddress = new ArrayList<>();

        for (int i = 0; i < businessDisplayAddress.length(); i++)
            displayAddress.add(businessDisplayAddress.getString(i));

        return displayAddress;
    }
}

class ScheduleParser {
    static Schedule from(JSONArray hours) {
        JSONObject weekSchedule = hours.getJSONObject(0);

        return new Schedule(
            weekSchedule.getBoolean("is_open_now"),
            buildHours(weekSchedule.getJSONArray("open"))
        );
    }

    private static Map<DayOfWeek, List<Hours>> buildHours(JSONArray businessHours) {
        Map<DayOfWeek, List<Hours>> hours = new LinkedHashMap<>();
        List<Hours> allDayHours;

        for (int i = 0; i < businessHours.length(); i++) {
            Hours dayHours = HoursParser.from(businessHours.getJSONObject(i));

            if (!hours.containsKey(dayHours.day)) allDayHours = new ArrayList<>();
            else allDayHours = hours.get(dayHours.day);

            allDayHours.add(dayHours);
            hours.put(dayHours.day, allDayHours);
        }

        return hours;
    }
}

class HoursParser {
    static Hours from(JSONObject hours) {
        return new Hours(
            DayOfWeek.of(hours.getInt("day") + 1),
            createTimeFrom(hours.getString("start")),
            createTimeFrom(hours.getString("end"))
        );
    }

    private static LocalTime createTimeFrom(String text) {
        return LocalTime.of(
            Integer.valueOf(text.substring(0, 2)),
            Integer.valueOf(text.substring(2, 4))
        );
    }
}
