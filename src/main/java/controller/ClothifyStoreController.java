package controller;

import dao.ItemDao;
import entity.Item;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class ClothifyStoreController implements Initializable {

    // FXML Injections matching the IDs in Scene Builder
    @FXML
    private FlowPane itemGrid;

    @FXML
    private StackPane popupOverlay;

    // Database access object
    private ItemDao itemDao = new ItemDao();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // This runs automatically when the UI loads
        loadItemsToGrid();
    }

    @FXML
    void handleShowLogin(ActionEvent event) {
        // This empty method fixes your crash!
        // In the next commit, we will make this open the Admin Login popup.
        System.out.println("Login button clicked! Admin panel coming soon.");
    }

    private void loadItemsToGrid() {
        itemGrid.getChildren().clear();

        // Fetch items from your MySQL Database!
        List<Item> items = itemDao.getAllItems();

        // If the database is empty, let's auto-generate some dummy items
        // just to see what the UI looks like.
        if (items.isEmpty()) {
            System.out.println("Database is empty. Generating sample items...");
            itemDao.saveOrUpdateItem(new Item("Moose Heritage Polo", "MEN", 2790.0, 50));
            itemDao.saveOrUpdateItem(new Item("Oversized Graphic Tee", "UNISEX", 1990.0, 30));
            itemDao.saveOrUpdateItem(new Item("Premium Casual Shirt", "MEN", 3490.0, 20));
            itemDao.saveOrUpdateItem(new Item("Floral Summer Dress", "WOMEN", 4290.0, 15));
            itemDao.saveOrUpdateItem(new Item("Luxury Gold Watch", "JEWELLERY", 15000.0, 5));

            // Re-fetch the newly created items from the database
            items = itemDao.getAllItems();
        }

        // Loop through the database items and create a visual card for each one
        for (Item item : items) {
            VBox productCard = createProductCard(item);
            itemGrid.getChildren().add(productCard);
        }
    }

    // A helper method to draw a product card programmatically
    private VBox createProductCard(Item item) {
        VBox card = new VBox(10); // 10px spacing between elements
        card.getStyleClass().add("product-card"); // Connects to the CSS we wrote in Commit 4
        card.setPrefSize(220, 280);
        card.setPadding(new Insets(20));
        card.setAlignment(Pos.BOTTOM_CENTER);

        // Item Name
        Label nameLabel = new Label(item.getName());
        nameLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 16px; -fx-text-fill: #111;");
        nameLabel.setWrapText(true);
        nameLabel.setAlignment(Pos.CENTER);

        // Item Category
        Label categoryLabel = new Label(item.getCategory());
        categoryLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #888;");

        // Item Price
        Label priceLabel = new Label("Rs. " + item.getPrice());
        priceLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 18px; -fx-text-fill: #E53935;");

        card.getChildren().addAll(nameLabel, categoryLabel, priceLabel);
        return card;
    }
}