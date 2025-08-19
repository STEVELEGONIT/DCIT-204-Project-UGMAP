import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Route {
    // Inner class for path segments (replaces separate Path class)
    public static class PathSegment {
        private Location start;
        private Location end;
        private double distance;
        private double baseTime;
        private double congestionFactor;

        public PathSegment(Location start, Location end, double distance, double baseTime) {
            this.start = start;
            this.end = end;
            this.distance = distance;
            this.baseTime = baseTime;
            this.congestionFactor = 1.0;
        }

        // Getters
        public Location getStart() { return start; }
        public Location getEnd() { return end; }
        public double getDistance() { return distance; }
        public double getTime() { return baseTime * congestionFactor; }

        public void setCongestion(double factor) {
            this.congestionFactor = factor > 0 ? factor : 1.0;
        }
    }

    private List<Location> path;
    private double totalDistance;
    private double totalTime;
    private List<String> landmarks;

    public Route(List<Location> path, double distance, double time) {
        this.path = new ArrayList<>(path);
        this.totalDistance = distance;
        this.totalTime = time;
        this.landmarks = extractLandmarks(path);
    }

    private List<String> extractLandmarks(List<Location> path) {
        return path.stream()
                   .flatMap(loc -> loc.getTags().stream())
                   .map(String::toLowerCase)
                   .distinct()
                   .collect(Collectors.toList());
    }

    // Getters
    public List<Location> getPath() { return new ArrayList<>(path); }
    public double getDistance() { return totalDistance; }
    public double getTime() { return totalTime; }
    public List<String> getLandmarks() { return new ArrayList<>(landmarks); }

    public boolean passesLandmark(String landmark) {
        return landmarks.contains(landmark.toLowerCase());
    }

    @Override
    public String toString() {
        return String.format("Route: %s\nDistance: %.2fm, Time: %.2f min\nLandmarks: %s",
                path.stream().map(Location::getName).collect(Collectors.toList()),
                totalDistance,
                totalTime,
                String.join(", ", landmarks));
    }
}
