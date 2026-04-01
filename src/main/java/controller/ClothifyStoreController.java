package controller;

import dao.ItemDao;
import dao.UserDao;
import entity.Item;
import entity.User;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class ClothifyStoreController implements Initializable {

    @FXML private FlowPane itemGrid;
    @FXML private StackPane popupOverlay;

    // Login Fields
    @FXML private TextField txtUsername;
    @FXML private PasswordField txtPassword;
    @FXML private Label lblLoginError;

    private ItemDao itemDao = new ItemDao();
    private UserDao userDao = new UserDao(); // NEW: Added UserDao for login

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        loadItemsToGrid();
    }

    // ----- UI TOGGLES -----

    @FXML
    void handleShowLogin(ActionEvent event) {
        popupOverlay.setVisible(true);
        lblLoginError.setText(""); // Clear old errors
    }

    @FXML
    void handleClosePopup(ActionEvent event) {
        popupOverlay.setVisible(false);
        txtUsername.clear();
        txtPassword.clear();
    }

    // ----- AUTHENTICATION LOGIC -----

    @FXML
    void handleLoginSubmit(ActionEvent event) {
        String username = txtUsername.getText();
        String password = txtPassword.getText();

        // Query the database for the user
        User user = userDao.getUserByUsername(username);

        // Check if user exists, password matches, and role is ADMIN
        if (user != null && user.getPassword().equals(password) && "ADMIN".equals(user.getRole())) {
            try {
                // Login Success! Load the Admin Dashboard
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/View/AdminDashboard.fxml"));
                Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                stage.setScene(new Scene(loader.load()));
                stage.setTitle("Admin Dashboard - Clothify Store");
                stage.show();
            } catch (IOException e) {
                e.printStackTrace();
                lblLoginError.setText("Error loading admin dashboard.");
            }
        } else {
            // Login Failed
            lblLoginError.setText("Invalid username, password, or access level.");
        }
    }

    // ----- PRODUCT GRID LOGIC (From Commit 5) -----

    private void loadItemsToGrid() {
        itemGrid.getChildren().clear();
        List<Item> items = itemDao.getAllItems();

        if (items.isEmpty()) {
            itemDao.saveOrUpdateItem(new Item("Moose Heritage Polo", "MEN", 2790.0, 50));
            itemDao.saveOrUpdateItem(new Item("Oversized Graphic Tee", "UNISEX", 1990.0, 30));
            itemDao.saveOrUpdateItem(new Item("Premium Casual Shirt", "MEN", 3490.0, 20));
            itemDao.saveOrUpdateItem(new Item("Floral Summer Dress", "WOMEN", 4290.0, 15));
            itemDao.saveOrUpdateItem(new Item("Luxury Gold Watch", "JEWELLERY", 15000.0, 5));
            items = itemDao.getAllItems();
        }

        for (Item item : items) {
            VBox productCard = createProductCard(item);
            itemGrid.getChildren().add(productCard);
        }
    }

    private VBox createProductCard(Item item) {
        VBox card = new VBox(10);
        card.getStyleClass().add("product-card");
        card.setPrefSize(220, 280);
        card.setPadding(new Insets(20));
        card.setAlignment(Pos.BOTTOM_CENTER);

        Label nameLabel = new Label(item.getName());
        nameLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 16px; -fx-text-fill: #111;");
        nameLabel.setWrapText(true);
        nameLabel.setAlignment(Pos.CENTER);

        Label categoryLabel = new Label(item.getCategory());
        categoryLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #888;");

        Label priceLabel = new Label("Rs. " + item.getPrice());
        priceLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 18px; -fx-text-fill: #E53935;");

        card.getChildren().addAll(nameLabel, categoryLabel, priceLabel);
        return card;
    }
}