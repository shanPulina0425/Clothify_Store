package controller;

import dao.ItemDao;
import dao.OrderDao;
import dao.UserDao;
import entity.Item;
import entity.Order;
import entity.User;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class ClothifyStoreController implements Initializable {

    @FXML private FlowPane itemGrid;
    @FXML private StackPane popupOverlay;

    // Login Fields
    @FXML private TextField txtUsername;
    @FXML private PasswordField txtPassword;
    @FXML private Label lblLoginError;

    // Cart Fields
    @FXML private StackPane cartOverlay;
    @FXML private VBox cartItemsContainer;
    @FXML private Label lblCartTotal;
    @FXML private Button btnCart;
    @FXML private TextField txtCustomerName;
    @FXML private Label lblCheckoutStatus;

    private ItemDao itemDao = new ItemDao();
    private UserDao userDao = new UserDao();
    private OrderDao orderDao = new OrderDao(); // NEW: Database access for Orders

    // NEW: Memory list to hold items before checkout
    private List<Item> cartList = new ArrayList<>();
    private double currentTotal = 0.0;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        loadItemsToGrid();
    }

    // ----- UI TOGGLES -----
    @FXML void handleShowLogin(ActionEvent event) {
        popupOverlay.setVisible(true);
        lblLoginError.setText("");
    }

    @FXML void handleClosePopup(ActionEvent event) {
        popupOverlay.setVisible(false);
        txtUsername.clear(); txtPassword.clear();
    }

    @FXML void handleShowCart(ActionEvent event) {
        cartOverlay.setVisible(true);
        lblCheckoutStatus.setText("");
    }

    @FXML void handleCloseCart(ActionEvent event) {
        cartOverlay.setVisible(false);
    }

    // ----- CART LOGIC -----
    private void addToCart(Item item) {
        cartList.add(item);
        currentTotal += item.getPrice();

        // Update Cart Button Text
        btnCart.setText("Cart (" + cartList.size() + ")");

        // Update Cart UI
        lblCartTotal.setText("Rs. " + currentTotal);

        // Create a small text label for the cart popup
        Label itemLabel = new Label("- " + item.getName() + " (Rs. " + item.getPrice() + ")");
        cartItemsContainer.getChildren().add(itemLabel);
    }

    @FXML void handleCheckout(ActionEvent event) {
        if (cartList.isEmpty()) {
            lblCheckoutStatus.setStyle("-fx-text-fill: red;");
            lblCheckoutStatus.setText("Your cart is empty!");
            return;
        }

        String customerName = txtCustomerName.getText();
        if (customerName.isEmpty()) {
            customerName = "Guest Customer";
        }

        // 1. Save order to database
        Order newOrder = new Order(customerName, currentTotal);
        orderDao.saveOrder(newOrder);

        // 2. Clear the cart
        cartList.clear();
        currentTotal = 0.0;
        cartItemsContainer.getChildren().clear();
        btnCart.setText("Cart (0)");
        lblCartTotal.setText("Rs. 0.0");
        txtCustomerName.clear();

        // 3. Show Success
        lblCheckoutStatus.setStyle("-fx-text-fill: green;");
        lblCheckoutStatus.setText("Order placed successfully! Thank you.");
    }

    // ----- AUTHENTICATION LOGIC -----
    @FXML void handleLoginSubmit(ActionEvent event) {
        String username = txtUsername.getText();
        String password = txtPassword.getText();

        User user = userDao.getUserByUsername(username);

        if (user != null && user.getPassword().equals(password) && "ADMIN".equals(user.getRole())) {
            try {
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
            lblLoginError.setText("Invalid username, password, or access level.");
        }
    }

    // ----- PRODUCT GRID LOGIC -----
    private void loadItemsToGrid() {
        itemGrid.getChildren().clear();
        List<Item> items = itemDao.getAllItems();

        for (Item item : items) {
            VBox productCard = createProductCard(item);
            itemGrid.getChildren().add(productCard);
        }
    }

    private VBox createProductCard(Item item) {
        VBox card = new VBox(10);
        card.getStyleClass().add("product-card");
        card.setPrefSize(220, 310); // Slightly taller to fit the button
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

        // NEW: Add to Cart Button for each item!
        Button btnAdd = new Button("Add to Cart");
        btnAdd.setStyle("-fx-background-color: white; -fx-border-color: #111; -fx-cursor: hand; -fx-pref-width: 150;");
        btnAdd.setOnAction(e -> addToCart(item)); // Connects button to our logic

        card.getChildren().addAll(nameLabel, categoryLabel, priceLabel, btnAdd);
        return card;
    }
}