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
    @FXML private TextField txtSearch;

    // Top Nav Buttons
    @FXML private Button btnLoginNav;
    @FXML private Button btnRegisterNav;

    // Login Fields
    @FXML private StackPane popupOverlay;
    @FXML private TextField txtUsername;
    @FXML private PasswordField txtPassword;
    @FXML private Label lblLoginError;

    // Registration Fields (NEW)
    @FXML private StackPane registerOverlay;
    @FXML private TextField txtRegUsername;
    @FXML private PasswordField txtRegPassword;
    @FXML private Label lblRegError;

    // Cart Fields
    @FXML private StackPane cartOverlay;
    @FXML private VBox cartItemsContainer;
    @FXML private Label lblCartTotal;
    @FXML private Button btnCart;
    @FXML private TextField txtCustomerName;
    @FXML private Label lblCheckoutStatus;

    private ItemDao itemDao = new ItemDao();
    private UserDao userDao = new UserDao();
    private OrderDao orderDao = new OrderDao();

    private List<Item> cartList = new ArrayList<>();
    private double currentTotal = 0.0;

    // NEW: Track the currently logged-in customer!
    private User loggedInCustomer = null;

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

    @FXML void handleShowRegister(ActionEvent event) {
        registerOverlay.setVisible(true);
        lblRegError.setText("");
    }

    @FXML void handleCloseRegister(ActionEvent event) {
        registerOverlay.setVisible(false);
        txtRegUsername.clear(); txtRegPassword.clear();
    }

    @FXML void handleShowCart(ActionEvent event) {
        cartOverlay.setVisible(true);
        lblCheckoutStatus.setText("");
        // If logged in, pre-fill and hide the name field
        if (loggedInCustomer != null) {
            txtCustomerName.setText(loggedInCustomer.getUsername());
            txtCustomerName.setVisible(false);
            txtCustomerName.setManaged(false);
        }
    }

    @FXML void handleCloseCart(ActionEvent event) {
        cartOverlay.setVisible(false);
    }

    // ----- AUTHENTICATION LOGIC -----

    @FXML void handleLoginSubmit(ActionEvent event) {
        String username = txtUsername.getText();
        String password = txtPassword.getText();
        User user = userDao.getUserByUsername(username);

        if (user != null && user.getPassword().equals(password)) {
            if ("ADMIN".equals(user.getRole())) {
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/View/AdminDashboard.fxml"));
                    Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                    stage.setScene(new Scene(loader.load()));
                    stage.setTitle("Admin Dashboard");
                    stage.show();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else if ("CUSTOMER".equals(user.getRole())) {
                // Customer Login Success!
                loggedInCustomer = user;
                handleClosePopup(null);
                btnLoginNav.setText("Hi, " + user.getUsername());
                btnRegisterNav.setVisible(false);
                btnRegisterNav.setManaged(false);
            }
        } else {
            lblLoginError.setText("Invalid username or password.");
        }
    }

    @FXML void handleRegisterSubmit(ActionEvent event) {
        String newUsername = txtRegUsername.getText();
        String newPassword = txtRegPassword.getText();

        if (newUsername.isEmpty() || newPassword.isEmpty()) {
            lblRegError.setText("Please fill out all fields.");
            return;
        }

        if (userDao.getUserByUsername(newUsername) != null) {
            lblRegError.setText("Username already exists!");
            return;
        }

        // Save new customer to database
        User newCustomer = new User(newUsername, newPassword, "CUSTOMER");
        userDao.saveUser(newCustomer);

        lblRegError.setStyle("-fx-text-fill: green;");
        lblRegError.setText("Success! You can now log in.");

        // Clear fields so they are ready for next time
        txtRegUsername.clear();
        txtRegPassword.clear();
    }

    // ----- CART & CHECKOUT LOGIC -----

    private void addToCart(Item item) {
        cartList.add(item);
        currentTotal += item.getPrice();
        btnCart.setText("Cart (" + cartList.size() + ")");
        lblCartTotal.setText("Rs. " + currentTotal);
        cartItemsContainer.getChildren().add(new Label("- " + item.getName() + " (Rs. " + item.getPrice() + ")"));
    }

    @FXML void handleCheckout(ActionEvent event) {
        if (cartList.isEmpty()) {
            lblCheckoutStatus.setStyle("-fx-text-fill: red;");
            lblCheckoutStatus.setText("Your cart is empty!");
            return;
        }

        String customerName = (loggedInCustomer != null) ? loggedInCustomer.getUsername() : txtCustomerName.getText();
        if (customerName.isEmpty()) customerName = "Guest Customer";

        orderDao.saveOrder(new Order(customerName, currentTotal));

        for (Item cartItem : cartList) {
            if (cartItem.getStockQuantity() > 0) {
                cartItem.setStockQuantity(cartItem.getStockQuantity() - 1);
                itemDao.saveOrUpdateItem(cartItem);
            }
        }

        cartList.clear();
        currentTotal = 0.0;
        cartItemsContainer.getChildren().clear();
        btnCart.setText("Cart (0)");
        lblCartTotal.setText("Rs. 0.0");
        if(loggedInCustomer == null) txtCustomerName.clear();

        lblCheckoutStatus.setStyle("-fx-text-fill: green;");
        lblCheckoutStatus.setText("Order placed successfully!");
        loadItemsToGrid();
    }

    // ----- PRODUCT GRID LOGIC -----

    @FXML void handleSearch(ActionEvent event) {
        String keyword = txtSearch.getText();
        if (keyword == null || keyword.trim().isEmpty()) {
            loadItemsToGrid();
            return;
        }
        List<Item> searchResults = itemDao.searchItemsByName(keyword);
        itemGrid.getChildren().clear();
        for (Item item : searchResults) itemGrid.getChildren().add(createProductCard(item));
    }

    private void loadItemsToGrid() {
        itemGrid.getChildren().clear();
        List<Item> items = itemDao.getAllItems();
        for (Item item : items) itemGrid.getChildren().add(createProductCard(item));
    }

    private VBox createProductCard(Item item) {
        VBox card = new VBox(10);
        card.getStyleClass().add("product-card");
        card.setPrefSize(220, 310);
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

        Button btnAdd = new Button("Add to Cart");
        btnAdd.setStyle("-fx-background-color: white; -fx-border-color: #111; -fx-cursor: hand; -fx-pref-width: 150;");
        btnAdd.setOnAction(e -> addToCart(item));

        card.getChildren().addAll(nameLabel, categoryLabel, priceLabel, btnAdd);
        return card;
    }
}