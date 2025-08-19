import java.util.List;
import java.util.Objects;
import java.util.ArrayList;

public class Location {
    private String name;
    private double latitude;
    private double longitude;
    private List<String> tags;

    public Location(String name, double lat, double lon, List<String> tags) {
        this.name = name;
        this.latitude = lat;
        this.longitude = lon;
        this.tags = tags != null ? new ArrayList<>(tags) : new ArrayList<>();
    }

    // Calculate heuristic (straight-line distance to another location)
    public double heuristicTo(Location other) {
        double dx = this.latitude - other.latitude;
        double dy = this.longitude - other.longitude;
        return Math.sqrt(dx * dx + dy * dy) * 1000; // Approx. meters
    }

    // Getters and other methods unchanged
    public String getName() { return name; }
    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }
    public List<String> getTags() { return tags; }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Location location = (Location) obj;
        return Double.compare(location.latitude, latitude) == 0 &&
               Double.compare(location.longitude, longitude) == 0 &&
               Objects.equals(name, location.name);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(name, latitude, longitude);
    }
    
    @Override
    public String toString() {
        return name; // This will be displayed in ComboBox
    }
}