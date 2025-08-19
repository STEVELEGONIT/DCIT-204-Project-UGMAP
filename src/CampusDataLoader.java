import com.google.gson.*;
import java.io.*;
import java.nio.file.*;
import java.util.*;

public class CampusDataLoader {
    // Helper classes for JSON parsing
    private static class JsonLocation {
        String name;
        double lat;
        double lon;
        List<String> tags;
    }

    private static class JsonPath {
        String from;
        String to;
        double distance;
        double time;
    }

    private static class JsonData {
        List<JsonLocation> locations;
        List<JsonPath> paths;
    }

    public static Map<Location, List<Route.PathSegment>> loadFromFile(String filename) throws Exception {
        Gson gson = new Gson();
        
        try (Reader reader = Files.newBufferedReader(Paths.get("lib/" + filename))) {
            JsonData data = gson.fromJson(reader, JsonData.class);

            if (data == null || data.locations == null || data.paths == null) {
                throw new IllegalArgumentException("Invalid JSON data structure");
            }

            // Parse locations
            Map<String, Location> locationMap = new HashMap<>();
            for (JsonLocation loc : data.locations) {
                if (loc.name != null) {
                    locationMap.put(loc.name, new Location(loc.name, loc.lat, loc.lon, loc.tags));
                }
            }

            // Parse paths
            Map<Location, List<Route.PathSegment>> graph = new HashMap<>();
            locationMap.values().forEach(loc -> graph.put(loc, new ArrayList<>()));

            for (JsonPath path : data.paths) {
                Location from = locationMap.get(path.from);
                Location to = locationMap.get(path.to);
                if (from != null && to != null) {
                    graph.get(from).add(new Route.PathSegment(from, to, path.distance, path.time));
                    graph.get(to).add(new Route.PathSegment(to, from, path.distance, path.time)); // Bidirectional
                }
            }

            return graph;
        } catch (Exception e) {
            throw new Exception("Failed to load campus data from " + filename + ": " + e.getMessage(), e);
        }
    }
}