package controller;

import dao.ItemDao;
import entity.Item;
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

    // Form Injections
    @FXML private TextField txtName;
    @FXML private ComboBox<String> cmbCategory;
    @FXML private TextField txtPrice;
    @FXML private TextField txtQty;
    @FXML private Label lblStatus;

    // Table Injections
    @FXML private TableView<Item> itemTable;
    @FXML private TableColumn<Item, Long> colId;
    @FXML private TableColumn<Item, String> colName;
    @FXML private TableColumn<Item, String> colCategory;
    @FXML private TableColumn<Item, Double> colPrice;
    @FXML private TableColumn<Item, Integer> colQty;

    private ItemDao itemDao = new ItemDao();
    private ObservableList<Item> itemList = FXCollections.observableArrayList();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Populate the category dropdown
        cmbCategory.setItems(FXCollections.observableArrayList("MEN", "WOMEN", "UNISEX", "COSMETICS", "JEWELLERY"));

        // Tell the table columns which variables to look for in the Item.java entity
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colCategory.setCellValueFactory(new PropertyValueFactory<>("category"));
        colPrice.setCellValueFactory(new PropertyValueFactory<>("price"));
        colQty.setCellValueFactory(new PropertyValueFactory<>("stockQuantity"));

        loadTableData();
    }

    private void loadTableData() {
        // Fetch fresh data from MySQL and put it in the table
        List<Item> items = itemDao.getAllItems();
        itemList.setAll(items);
        itemTable.setItems(itemList);
    }

    @FXML
    void handleAddItem(ActionEvent event) {
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

            // Save to database
            Item newItem = new Item(name, category, price, qty);
            itemDao.saveOrUpdateItem(newItem);

            lblStatus.setStyle("-fx-text-fill: green;");
            lblStatus.setText("Item added successfully!");

            // Clear the form
            txtName.clear(); cmbCategory.getSelectionModel().clearSelection();
            txtPrice.clear(); txtQty.clear();

            // Refresh the table so the new item shows up
            loadTableData();

        } catch (NumberFormatException e) {
            lblStatus.setStyle("-fx-text-fill: red;");
            lblStatus.setText("Price and Quantity must be numbers!");
        }
    }

    @FXML
    void handleDeleteItem(ActionEvent event) {
        Item selectedItem = itemTable.getSelectionModel().getSelectedItem();

        if (selectedItem != null) {
            itemDao.deleteItem(selectedItem.getId()); // Delete from database
            loadTableData(); // Refresh table
            lblStatus.setText("");
        } else {
            lblStatus.setStyle("-fx-text-fill: red;");
            lblStatus.setText("Please click an item in the table to delete it.");
        }
    }

    @FXML
    void handleLogout(ActionEvent event) {
        try {
            // Take the admin back to the main storefront
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