package controller;

import dao.ItemDao;
import dao.UserDao;
import entity.Item;
import entity.User;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class AdminController implements Initializable {

    // --- TAB 1: INVENTORY INJECTIONS ---
    @FXML private TextField txtName;
    @FXML private ComboBox<String> cmbCategory;
    @FXML private TextField txtPrice;
    @FXML private TextField txtQty;
    @FXML private Label lblStatus;

    @FXML private TableView<Item> itemTable;
    @FXML private TableColumn<Item, Long> colId;
    @FXML private TableColumn<Item, String> colName;
    @FXML private TableColumn<Item, String> colCategory;
    @FXML private TableColumn<Item, Double> colPrice;
    @FXML private TableColumn<Item, Integer> colQty;

    // --- TAB 2: USER INJECTIONS ---
    @FXML private TextField txtAdminUser;
    @FXML private PasswordField txtAdminPass;
    @FXML private Label lblUserStatus;

    @FXML private TableView<User> userTable;
    @FXML private TableColumn<User, Long> colUserId;
    @FXML private TableColumn<User, String> colUsername;
    @FXML private TableColumn<User, String> colRole;

    // DAOs and Lists
    private ItemDao itemDao = new ItemDao();
    private UserDao userDao = new UserDao();
    private ObservableList<Item> itemList = FXCollections.observableArrayList();
    private ObservableList<User> userList = FXCollections.observableArrayList();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Init Inventory Table
        cmbCategory.setItems(FXCollections.observableArrayList("MEN", "WOMEN", "UNISEX", "COSMETICS", "JEWELLERY"));
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colCategory.setCellValueFactory(new PropertyValueFactory<>("category"));
        colPrice.setCellValueFactory(new PropertyValueFactory<>("price"));
        colQty.setCellValueFactory(new PropertyValueFactory<>("stockQuantity"));

        // Init User Table
        colUserId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colUsername.setCellValueFactory(new PropertyValueFactory<>("username"));
        colRole.setCellValueFactory(new PropertyValueFactory<>("role"));

        loadTableData();
        loadUserTableData();
    }

    // --- INVENTORY LOGIC ---
    private void loadTableData() {
        List<Item> items = itemDao.getAllItems();
        itemList.setAll(items);
        itemTable.setItems(itemList);
    }

    @FXML void handleAddItem(ActionEvent event) {
        try {
            String name = txtName.getText();
            String category = cmbCategory.getValue();
            double price = Double.parseDouble(txtPrice.getText());
            int qty = Integer.parseInt(txtQty.getText());

            if (name.isEmpty() || category == null) {
                lblStatus.setStyle("-fx-text-fill: red;");
                lblStatus.setText("Please fill out all fields.");
                return;
            }

            itemDao.saveOrUpdateItem(new Item(name, category, price, qty));
            lblStatus.setStyle("-fx-text-fill: green;");
            lblStatus.setText("Item added successfully!");

            txtName.clear(); cmbCategory.getSelectionModel().clearSelection();
            txtPrice.clear(); txtQty.clear();
            loadTableData();

        } catch (NumberFormatException e) {
            lblStatus.setStyle("-fx-text-fill: red;");
            lblStatus.setText("Price and Quantity must be numbers!");
        }
    }

    @FXML void handleDeleteItem(ActionEvent event) {
        Item selectedItem = itemTable.getSelectionModel().getSelectedItem();
        if (selectedItem != null) {
            itemDao.deleteItem(selectedItem.getId());
            loadTableData();
            lblStatus.setText("");
        } else {
            lblStatus.setStyle("-fx-text-fill: red;");
            lblStatus.setText("Please select an item to delete.");
        }
    }

    // --- USER LOGIC ---
    private void loadUserTableData() {
        List<User> users = userDao.getAllUsers();
        userList.setAll(users);
        userTable.setItems(userList);
    }

    @FXML void handleAddAdmin(ActionEvent event) {
        String username = txtAdminUser.getText();
        String password = txtAdminPass.getText();

        if (username.isEmpty() || password.isEmpty()) {
            lblUserStatus.setStyle("-fx-text-fill: red;");
            lblUserStatus.setText("Please fill out all fields.");
            return;
        }

        if (userDao.getUserByUsername(username) != null) {
            lblUserStatus.setStyle("-fx-text-fill: red;");
            lblUserStatus.setText("Username already exists!");
            return;
        }

        // Save new Admin
        userDao.saveUser(new User(username, password, "ADMIN"));
        lblUserStatus.setStyle("-fx-text-fill: green;");
        lblUserStatus.setText("Admin account created!");

        txtAdminUser.clear(); txtAdminPass.clear();
        loadUserTableData();
    }

    @FXML void handleDeleteUser(ActionEvent event) {
        User selectedUser = userTable.getSelectionModel().getSelectedItem();

        if (selectedUser != null) {
            // Optional: Prevent admin from deleting themselves if you want to add that check later
            userDao.deleteUser(selectedUser.getId());
            loadUserTableData();
            lblUserStatus.setText("");
        } else {
            lblUserStatus.setStyle("-fx-text-fill: red;");
            lblUserStatus.setText("Please select a user to delete.");
        }
    }

    // --- NAVIGATION ---
    @FXML void handleLogout(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/View/Clothify Store.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(loader.load()));
            stage.setTitle("Clothify Store");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}