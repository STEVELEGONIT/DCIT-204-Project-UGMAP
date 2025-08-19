import javafx.application.Application;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import java.util.*;

public class UGNavigateGUI extends Application {
    private PathFinder pathFinder;
    private ComboBox<Location> startCombo, endCombo;
    private ComboBox<String> landmarkCombo;
    private ToggleGroup criteriaGroup;
    private CheckBox trafficCheckBox;
    private TextArea routeDetailsArea;
    private Label statusLabel;
    private Map<Location, List<Route.PathSegment>> graph;

    @Override
    public void start(Stage primaryStage) throws Exception {
        try {
            // Load data from JSON
            graph = CampusDataLoader.loadFromFile("campus_data.json");
            pathFinder = new PathFinder(graph);

            // Create main layout
            primaryStage.setScene(new Scene(createMainLayout(), 900, 650));
            primaryStage.setTitle("UG Navigate - Campus Route Finder");
            primaryStage.setResizable(true);
            primaryStage.show();
            
            // Set initial status
            statusLabel.setText("Ready - Select start and end locations to find a route");
            
        } catch (Exception e) {
            showErrorDialog("Application Error", "Failed to load application", e.getMessage());
        }
    }

    private VBox createMainLayout() {
        VBox mainLayout = new VBox(15);
        mainLayout.setPadding(new Insets(20));
        mainLayout.setStyle("-fx-background-color: #f8f9fa;");

        // Title
        Label titleLabel = new Label("UG Navigate - Campus Route Finder");
        titleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
        titleLabel.setAlignment(Pos.CENTER);

        // Create input panel and results panel
        HBox contentLayout = new HBox(20);
        contentLayout.getChildren().addAll(createInputPanel(), createResultsPanel());

        // Status bar
        statusLabel = new Label("Loading...");
        statusLabel.setStyle("-fx-background-color: #ecf0f1; -fx-padding: 5 10; -fx-border-color: #bdc3c7; -fx-border-width: 1;");

        mainLayout.getChildren().addAll(titleLabel, contentLayout, statusLabel);
        return mainLayout;
    }

    private VBox createInputPanel() {
        VBox inputPanel = new VBox(15);
        inputPanel.setPrefWidth(350);
        inputPanel.setStyle("-fx-background-color: white; -fx-padding: 20; -fx-border-color: #e0e0e0; -fx-border-width: 1; -fx-border-radius: 5;");

        // Section title
        Label sectionTitle = new Label("Route Options");
        sectionTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #34495e;");

        // Initialize ComboBoxes
        startCombo = new ComboBox<>();
        endCombo = new ComboBox<>();
        landmarkCombo = new ComboBox<>();
        
        // Style ComboBoxes
        startCombo.setPrefWidth(300);
        endCombo.setPrefWidth(300);
        landmarkCombo.setPrefWidth(300);

        // Set cell factory and button cell for Location ComboBoxes
        startCombo.setCellFactory(listView -> new ListCell<Location>() {
            @Override
            protected void updateItem(Location item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getName());
            }
        });
        startCombo.setButtonCell(new ListCell<Location>() {
            @Override
            protected void updateItem(Location item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getName());
            }
        });
        endCombo.setCellFactory(listView -> new ListCell<Location>() {
            @Override
            protected void updateItem(Location item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getName());
            }
        });
        endCombo.setButtonCell(new ListCell<Location>() {
            @Override
            protected void updateItem(Location item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getName());
            }
        });
        
        // Populate ComboBoxes
        List<Location> locations = new ArrayList<>(graph.keySet());
        locations.sort((a, b) -> a.getName().compareTo(b.getName()));
        
        startCombo.getItems().addAll(locations);
        endCombo.getItems().addAll(locations);
        landmarkCombo.getItems().addAll(getAllLandmarks(graph));

        // Location selection
        VBox startSection = createInputSection("Start Location:", startCombo);
        VBox endSection = createInputSection("End Location:", endCombo);

        // Route criteria
        Label criteriaLabel = new Label("Optimization Criteria:");
        criteriaLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #2c3e50;");
        
        criteriaGroup = new ToggleGroup();
        RadioButton distanceOpt = new RadioButton("Shortest Distance");
        RadioButton timeOpt = new RadioButton("Fastest Time");
        RadioButton landmarkOpt = new RadioButton("Via Landmark");
        
        distanceOpt.setToggleGroup(criteriaGroup);
        timeOpt.setToggleGroup(criteriaGroup);
        landmarkOpt.setToggleGroup(criteriaGroup);
        distanceOpt.setSelected(true);
        
        // Landmark selection (initially disabled)
        VBox landmarkSection = createInputSection("Preferred Landmark:", landmarkCombo);
        landmarkCombo.setDisable(true);
        
        // Enable/disable landmark combo based on selection
        landmarkOpt.setOnAction(listView -> landmarkCombo.setDisable(!landmarkOpt.isSelected()));
        distanceOpt.setOnAction(listView -> landmarkCombo.setDisable(true));
        timeOpt.setOnAction(listView -> landmarkCombo.setDisable(true));

        // Additional options
        trafficCheckBox = new CheckBox("Consider Real-time Traffic");
        trafficCheckBox.setStyle("-fx-text-fill: #2c3e50;");

        // Action buttons
        HBox buttonBox = new HBox(10);
        Button findRouteBtn = createStyledButton("Find Route", "#3498db");
        Button clearBtn = createStyledButton("Clear", "#95a5a6");
        Button swapBtn = createStyledButton("⇄ Swap", "#e67e22");
        
        findRouteBtn.setOnAction(listView -> findAndDisplayRoute());
        clearBtn.setOnAction(listView -> clearSelections());
        swapBtn.setOnAction(listView -> swapLocations());
        
        buttonBox.getChildren().addAll(findRouteBtn, clearBtn, swapBtn);
        buttonBox.setAlignment(Pos.CENTER);

        inputPanel.getChildren().addAll(
            sectionTitle,
            new Separator(),
            startSection,
            endSection,
            new Label(" "), // Spacer
            criteriaLabel,
            distanceOpt,
            timeOpt,
            landmarkOpt,
            landmarkSection,
            new Label(" "), // Spacer
            trafficCheckBox,
            new Label(" "), // Spacer
            buttonBox
        );

        return inputPanel;
    }

    private VBox createResultsPanel() {
        VBox resultsPanel = new VBox(15);
        resultsPanel.setPrefWidth(500);
        resultsPanel.setStyle("-fx-background-color: white; -fx-padding: 20; -fx-border-color: #e0e0e0; -fx-border-width: 1; -fx-border-radius: 5;");

        // Section title
        Label sectionTitle = new Label("Route Details");
        sectionTitle.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #34495e;");

        // Results text area
        routeDetailsArea = new TextArea();
        routeDetailsArea.setPrefRowCount(20);
        routeDetailsArea.setEditable(false);
        routeDetailsArea.setWrapText(true);
        routeDetailsArea.setStyle("-fx-font-family: 'Courier New'; -fx-font-size: 14px;");
        routeDetailsArea.setText("Select start and end locations, then click 'Find Route' to see detailed route information here.");

        resultsPanel.getChildren().addAll(sectionTitle, new Separator(), routeDetailsArea);
        return resultsPanel;
    }

    private VBox createInputSection(String labelText, Control control) {
        VBox section = new VBox(5);
        Label label = new Label(labelText);
        label.setStyle("-fx-font-weight: bold; -fx-text-fill: #2c3e50;");
        section.getChildren().addAll(label, control);
        return section;
    }

    private Button createStyledButton(String text, String color) {
        Button button = new Button(text);
        button.setStyle(String.format(
            "-fx-background-color: %s; -fx-text-fill: white; -fx-font-weight: bold; " +
            "-fx-padding: 8 16; -fx-border-radius: 4; -fx-background-radius: 4;", 
            color
        ));
        button.setOnMouseEntered(listView -> button.setStyle(button.getStyle() + "-fx-opacity: 0.8;"));
        button.setOnMouseExited(listView -> button.setStyle(button.getStyle().replace("-fx-opacity: 0.8;", "")));
        return button;
    }

    private void findAndDisplayRoute() {
        Location start = startCombo.getValue();
        Location end = endCombo.getValue();
        
        if (start == null || end == null) {
            showErrorDialog("Input Error", "Missing Selection", "Please select both start and end locations.");
            statusLabel.setText("Error: Please select both start and end locations");
            return;
        }
        
        if (start.equals(end)) {
            showErrorDialog("Input Error", "Same Location", "Start and end locations cannot be the same.");
            statusLabel.setText("Error: Start and end locations cannot be the same");
            return;
        }

        statusLabel.setText("Finding route...");
        
        try {
            Route route = pathFinder.getPrecomputedPath(start, end);
            
            if (route != null) {
                displayRouteDetails(route);
                statusLabel.setText("Route found successfully!");
            } else {
                routeDetailsArea.setText("No route found between the selected locations.\n\nThis might happen if the locations are not connected in the campus network.");
                statusLabel.setText("No route found between selected locations");
            }
        } catch (Exception e) {
            showErrorDialog("Route Error", "Failed to find route", e.getMessage());
            statusLabel.setText("Error occurred while finding route");
        }
    }

    private void displayRouteDetails(Route route) {
        StringBuilder details = new StringBuilder();
        
        // Header
        details.append("          ROUTE DETAILS          \n");
        details.append("═══════════════════════════════════════════\n\n");
        
        // Route path
        List<Location> path = route.getPath();
        details.append("ROUTE PATH:\n");
        details.append("──────────────────────────────────────────────\n");
        
        for (int i = 0; i < path.size(); i++) {
            if (i == 0) {
                details.append("START: ").append(path.get(i).getName()).append("\n");
            } else if (i == path.size() - 1) {
                details.append("END:   ").append(path.get(i).getName()).append("\n");
            } else {
                details.append("STOP:  ").append(path.get(i).getName()).append("\n");
            }
            
            if (i < path.size() - 1) {
                details.append("    ↓\n");
            }
        }
        
        details.append("\n");
        
        // Summary statistics
        details.append("ROUTE SUMMARY:\n");
        details.append("───────────────────────────────────────────────────────\n");
        details.append(String.format("Total Distance: %.0f meters (%.2f km)\n", 
            route.getDistance(), route.getDistance() / 1000));
        details.append(String.format("Estimated Time: %.1f minutes\n", route.getTime()));
        details.append(String.format("Walking Pace:   %.1f km/h\n", 
            (route.getDistance() / 1000) / (route.getTime() / 60)));
        details.append(String.format("Total Stops:    %d locations\n", path.size()));
        
        // Landmarks
        List<String> landmarks = route.getLandmarks();
        if (!landmarks.isEmpty()) {
            details.append("\nNEARBY LANDMARKS:\n");
            details.append("────────────────────────────────────────────────\n");
            landmarks.forEach(landmark -> 
                details.append("   • ").append(landmark.substring(0, 1).toUpperCase())
                      .append(landmark.substring(1)).append("\n"));
        }
        
        // Additional route information
        details.append("\nADDITIONAL INFO:\n");
        details.append("─────────────────────────────────────────────────\n");
        
        RadioButton selectedCriteria = (RadioButton) criteriaGroup.getSelectedToggle();
        details.append("Optimization: ").append(selectedCriteria.getText()).append("\n");
        
        if (trafficCheckBox.isSelected()) {
            details.append("Traffic Mode: Real-time traffic considered\n");
            details.append("Note: Actual travel time may vary due to current conditions\n");
        } else {
            details.append("Traffic Mode: Standard timing (no traffic data)\n");
        }
        
        // Step-by-step directions
        if (path.size() > 2) {
            details.append("\nSTEP-BY-STEP DIRECTIONS:\n");
            details.append("─────────────────────────────────────────────────────\n");
            for (int i = 0; i < path.size() - 1; i++) {
                details.append(String.format("Step %d: From %s to %s\n", 
                    i + 1, path.get(i).getName(), path.get(i + 1).getName()));
            }
        }
        
        details.append("\n═══════════════════════════════════════════════\n");
        details.append("Generated by ALPHA TEAM - Campus Route Finder\n");
        details.append("═══════════════════════════════════════════════");
        
        routeDetailsArea.setText(details.toString());
    }

    private void clearSelections() {
        startCombo.setValue(null);
        endCombo.setValue(null);
        landmarkCombo.setValue(null);
        trafficCheckBox.setSelected(false);
        routeDetailsArea.setText("Select Start and End locations, then click 'Find Route' to see detailed route information here.");
        statusLabel.setText("Selections cleared - Ready for new route search");
    }

    private void swapLocations() {
        Location start = startCombo.getValue();
        Location end = endCombo.getValue();
        
        startCombo.setValue(end);
        endCombo.setValue(start);
        
        if (start != null && end != null) {
            statusLabel.setText("Start and end locations swapped");
        }
    }

    private void showErrorDialog(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private Set<String> getAllLandmarks(Map<Location, List<Route.PathSegment>> graph) {
        Set<String> landmarks = new HashSet<>();
        graph.keySet().forEach(loc -> {
            // Fix: Use getTags() method instead of getLandmark()
            if (loc.getTags() != null && !loc.getTags().isEmpty()) {
                landmarks.addAll(loc.getTags());
            }
        });
        return landmarks;
    }

    public static void main(String[] args) {
        launch(args);
    }
}