package controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;

import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Logger;

public class ClothifyStoreController implements Initializable {

    Logger logger = Logger.getLogger(getClass().getName());


    // FXML INJECTION: LAYOUTS & PANES

    @FXML
    private StackPane mainRoot;
    @FXML
    private VBox loginPane;
    @FXML
    private BorderPane dashboardPane;
    @FXML
    private StackPane verificationOverlay;

    // Content Views (The different forms inside the dashboard)
    @FXML
    private VBox viewStore;
    @FXML
    private VBox viewCashierReg;
    @FXML
    private VBox viewCustomerReg;
    @FXML
    private VBox viewOrder;
    @FXML
    private VBox viewProfile;


    // FXML INJECTION: INPUT FIELDS

    // Login
    @FXML
    private TextField txtLoginUser;
    @FXML
    private PasswordField txtLoginPass;

    // Verification
    @FXML
    private TextField txtVerifyAdminUser;

    // Cashier Form
    @FXML
    private TextField txtCashierName;
    @FXML
    private TextField txtCashierPhone;
    @FXML
    private TextField txtCashierNic;

    // Customer Form
    @FXML
    private TextField txtCustomerName;
    @FXML
    private TextField txtCustomerPhone;


    @Override
    public void initialize(URL location, ResourceBundle resources) {

        loginPane.setVisible(true);
        dashboardPane.setVisible(true);

    }


    @FXML
    void handleLogin(ActionEvent event) {
        String username = txtLoginUser.getText();
        String password = txtLoginPass.getText();


        if (username.equals("admin") && password.equals("1234")) {

            loginPane.setVisible(false);
            dashboardPane.setVisible(true);
            switchView(viewStore);
            clearLoginFields();
        } else {
            showAlert(Alert.AlertType.ERROR, "Login Failed", "Invalid Username or Password.");
        }
    }

    @FXML
    void handleLogout(ActionEvent event) {

        dashboardPane.setVisible(false);
        loginPane.setVisible(true);
        clearLoginFields();
    }




    private void switchView(VBox targetView) {

        viewStore.setVisible(false);
        viewCashierReg.setVisible(false);
        viewCustomerReg.setVisible(false);
        viewOrder.setVisible(false);
        viewProfile.setVisible(false);


        targetView.setVisible(true);
    }

    @FXML
    void handleShowStore(ActionEvent event) {
        switchView(viewStore);
    }

    @FXML
    void handleShowProfile(ActionEvent event) {
        switchView(viewProfile);
    }

    @FXML
    void handleShowAddCustomer(ActionEvent event) {
        switchView(viewCustomerReg);
    }

    @FXML
    void handleShowOrder(ActionEvent event) {
        switchView(viewOrder);
    }


    @FXML
    void handleNavAddCashier(ActionEvent event) {
        txtVerifyAdminUser.setText("");
        verificationOverlay.setVisible(true);
    }


    @FXML
    void actionVerifyAdmin(ActionEvent event) {
        String adminCheck = txtVerifyAdminUser.getText();


        if (adminCheck.equals("admin")) {

            verificationOverlay.setVisible(false);
            switchView(viewCashierReg);
        } else {
            showAlert(Alert.AlertType.ERROR, "Access Denied", "Incorrect Admin Username.");
        }
    }

    @FXML
    void closeVerification(ActionEvent event) {
        verificationOverlay.setVisible(false);
    }



    @FXML
    void actionAddCashier(ActionEvent event) {

        if (txtCashierName.getText().isEmpty() || txtCashierPhone.getText().isEmpty() || txtCashierNic.getText().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Incomplete", "Please fill all fields.");
            return;
        }


        logger.info("Cashier Added: " + txtCashierName.getText());

        showAlert(Alert.AlertType.INFORMATION, "Success", "Cashier registered successfully!");


        txtCashierName.clear();
        txtCashierPhone.clear();
        txtCashierNic.clear();
    }

    @FXML
    void actionAddCustomer(ActionEvent event) {
        if (txtCustomerName.getText().isEmpty() || txtCustomerPhone.getText().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Incomplete", "Please fill all fields.");
            return;
        }


        logger.info("Customer Added: " + txtCustomerName.getText());

        showAlert(Alert.AlertType.INFORMATION, "Success", "Customer added successfully!");


        txtCustomerName.clear();
        txtCustomerPhone.clear();
    }



    private void clearLoginFields() {
        txtLoginUser.clear();
        txtLoginPass.clear();
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}