import java.util.*;
import java.util.stream.Collectors;

public class PathFinder {
    private final Map<Location, List<Route.PathSegment>> graph;
    private final Map<Location, Map<Location, Route>> allPairsPaths;

    public PathFinder(Map<Location, List<Route.PathSegment>> graph) {
        this.graph = graph;
        this.allPairsPaths = precomputeAllPaths();
    }

    // Add getter for graph
    public Map<Location, List<Route.PathSegment>> getGraph() {
        return graph;
    }

    // Floyd-Warshall Algorithm - Complete implementation
    private Map<Location, Map<Location, Route>> precomputeAllPaths() {
        Map<Location, Map<Location, Route>> dist = new HashMap<>();
        
        // Initialize
        for (Location u : graph.keySet()) {
            dist.put(u, new HashMap<>());
            for (Location v : graph.keySet()) {
                if (u.equals(v)) {
                    dist.get(u).put(v, new Route(List.of(u), 0, 0));
                } else {
                    dist.get(u).put(v, null);
                }
            }
            // Add direct paths
            for (Route.PathSegment p : graph.get(u)) {
                dist.get(u).put(p.getEnd(), new Route(List.of(u, p.getEnd()), p.getDistance(), p.getTime()));
            }
        }

        // Floyd-Warshall Dynamic Programming
        for (Location k : graph.keySet()) {
            for (Location i : graph.keySet()) {
                for (Location j : graph.keySet()) {
                    Route ik = dist.get(i).get(k);
                    Route kj = dist.get(k).get(j);
                    if (ik != null && kj != null) {
                        double newDist = ik.getDistance() + kj.getDistance();
                        double newTime = ik.getTime() + kj.getTime();
                        Route ij = dist.get(i).get(j);
                        if (ij == null || newDist < ij.getDistance()) {
                            List<Location> newPath = new ArrayList<>(ik.getPath());
                            newPath.addAll(kj.getPath().subList(1, kj.getPath().size()));
                            dist.get(i).put(j, new Route(newPath, newDist, newTime));
                        }
                    }
                }
            }
        }
        return dist;
    }

    public Route getPrecomputedPath(Location start, Location end) {
        if (start == null || end == null || 
            !allPairsPaths.containsKey(start) || 
            !allPairsPaths.get(start).containsKey(end)) {
            return null;
        }
        return allPairsPaths.get(start).get(end);
    }

    // Utility methods for route sorting and landmark filtering
    public static void sortRoutes(List<Route> routes, String criteria) {
        if (routes == null || criteria == null) return;
        
        switch (criteria.toLowerCase()) {
            case "distance":
                routes.sort(Comparator.comparingDouble(Route::getDistance));
                break;
            case "time":
                routes.sort(Comparator.comparingDouble(Route::getTime));
                break;
            case "landmarks":
                routes.sort((r1, r2) -> 
                    Integer.compare(r2.getLandmarks().size(), r1.getLandmarks().size()));
                break;
        }
    }

    public static List<Route> filterByLandmark(List<Route> allRoutes, String landmark) {
        if (allRoutes == null || landmark == null) return List.of();
        
        List<Route> filtered = allRoutes.stream()
                  .filter(route -> route != null && route.passesLandmark(landmark))
                  .collect(Collectors.toList());
        
        return filtered.isEmpty() ? allRoutes : filtered;
    }

    // Rest of the class (Dijkstra/A*) remains unchanged
}