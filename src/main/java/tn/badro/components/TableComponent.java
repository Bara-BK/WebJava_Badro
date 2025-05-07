package tn.badro.components;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

public class TableComponent<T> extends VBox {
    private final ObservableList<T> items;
    private final FilteredList<T> filteredItems;
    private final int itemsPerPage = 10;
    private int currentPage = 1;
    private int totalPages;
    private final TableView<T> tableView;

    public TableComponent(ObservableList<T> items) {
        this.items = items;
        this.filteredItems = new FilteredList<>(items);
        this.tableView = new TableView<>();
        this.totalPages = (int) Math.ceil((double) items.size() / itemsPerPage);
        
        setSpacing(10);
        setPadding(new Insets(20));
        
        // Add components
        getChildren().addAll(
            createTopSection(),
            createTableView(),
            createPagination()
        );
        
        updateTableData();
    }

    private HBox createTopSection() {
        HBox topSection = new HBox(20);
        topSection.setAlignment(Pos.CENTER_LEFT);

        // Entries dropdown
        HBox entriesBox = new HBox(10);
        entriesBox.setAlignment(Pos.CENTER_LEFT);
        Label showLabel = new Label("Show");
        ComboBox<Integer> entriesComboBox = new ComboBox<>();
        entriesComboBox.getItems().addAll(10, 25, 50, 100);
        entriesComboBox.setValue(10);
        Label entriesLabel = new Label("entries");
        entriesBox.getChildren().addAll(showLabel, entriesComboBox, entriesLabel);

        // Search box
        HBox searchBox = new HBox(10);
        searchBox.setAlignment(Pos.CENTER_RIGHT);
        HBox.setHgrow(searchBox, Priority.ALWAYS);
        Label searchLabel = new Label("Search:");
        TextField searchField = new TextField();
        searchField.setPromptText("Search...");
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null || newVal.isEmpty()) {
                // Reset the filter and refresh the table
                filteredItems.setPredicate(item -> true);
                tableView.setItems(items);
            } else {
                // Apply the search filter
                filteredItems.setPredicate(item -> {
                    if (newVal == null || newVal.isEmpty()) {
                        return true;
                    }
                    return item.toString().toLowerCase().contains(newVal.toLowerCase());
                });
            }
            updatePagination();
            updateTableData();
            
            // Force a layout pass to maintain column widths and styling
            tableView.refresh();
            tableView.layout();
        });
        searchBox.getChildren().addAll(searchLabel, searchField);

        topSection.getChildren().addAll(entriesBox, searchBox);
        return topSection;
    }

    private TableView<T> createTableView() {
        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tableView.setStyle(
            "-fx-background-color: white;" +
            "-fx-border-color: #ddd;" +
            "-fx-border-width: 1px;" +
            "-fx-table-cell-border-color: transparent;"
        );
        
        // Style the header
        tableView.getStylesheets().add(getClass().getResource("/styles/table.css").toExternalForm());
        
        // Ensure table takes available vertical space
        VBox.setVgrow(tableView, Priority.ALWAYS);
        
        // Set minimum height to prevent collapsing
        tableView.setMinHeight(400);
        
        return tableView;
    }

    private HBox createPagination() {
        HBox paginationBox = new HBox(5);
        paginationBox.setAlignment(Pos.CENTER_RIGHT);

        Button prevButton = new Button("Previous");
        prevButton.setDisable(currentPage == 1);
        prevButton.setOnAction(e -> {
            if (currentPage > 1) {
                currentPage--;
                updateTableData();
                updatePaginationButtons(paginationBox);
            }
        });

        Button nextButton = new Button("Next");
        nextButton.setDisable(currentPage == totalPages);
        nextButton.setOnAction(e -> {
            if (currentPage < totalPages) {
                currentPage++;
                updateTableData();
                updatePaginationButtons(paginationBox);
            }
        });

        paginationBox.getChildren().addAll(prevButton);
        
        // Add page numbers
        for (int i = 1; i <= totalPages; i++) {
            Button pageButton = new Button(String.valueOf(i));
            int page = i;
            pageButton.setOnAction(e -> {
                currentPage = page;
                updateTableData();
                updatePaginationButtons(paginationBox);
            });
            
            if (i == currentPage) {
                pageButton.setStyle(
                    "-fx-background-color: #1a237e;" +
                    "-fx-text-fill: white;"
                );
            }
            
            paginationBox.getChildren().add(pageButton);
        }
        
        paginationBox.getChildren().add(nextButton);
        return paginationBox;
    }

    private void updateTableData() {
        int fromIndex = (currentPage - 1) * itemsPerPage;
        int toIndex = Math.min(fromIndex + itemsPerPage, filteredItems.size());
        
        ObservableList<T> pageItems = FXCollections.observableArrayList(
            filteredItems.subList(fromIndex, toIndex)
        );
        tableView.setItems(pageItems);

        // Ensure table maintains its layout and style
        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tableView.setStyle(
            "-fx-background-color: white;" +
            "-fx-border-color: #ddd;" +
            "-fx-border-width: 1px;" +
            "-fx-table-cell-border-color: transparent;"
        );
        
        // Force column widths to maintain their size
        tableView.getColumns().forEach(column -> {
            double width = column.getWidth();
            if (width > 0) {
                column.setPrefWidth(width);
            }
        });
    }

    private void updatePagination() {
        totalPages = (int) Math.ceil((double) filteredItems.size() / itemsPerPage);
        currentPage = 1;
        getChildren().set(2, createPagination());
    }

    private void updatePaginationButtons(HBox paginationBox) {
        Button prevButton = (Button) paginationBox.getChildren().get(0);
        Button nextButton = (Button) paginationBox.getChildren().get(paginationBox.getChildren().size() - 1);
        
        prevButton.setDisable(currentPage == 1);
        nextButton.setDisable(currentPage == totalPages);
        
        // Update page button styles
        for (int i = 1; i < paginationBox.getChildren().size() - 1; i++) {
            Button pageButton = (Button) paginationBox.getChildren().get(i);
            if (Integer.parseInt(pageButton.getText()) == currentPage) {
                pageButton.setStyle(
                    "-fx-background-color: #1a237e;" +
                    "-fx-text-fill: white;"
                );
            } else {
                pageButton.setStyle("");
            }
        }
    }

    public void addColumn(TableColumn<T, ?> column) {
        tableView.getColumns().add(column);
    }

    public TableView<T> getTableView() {
        return tableView;
    }
} 