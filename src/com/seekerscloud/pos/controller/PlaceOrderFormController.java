package com.seekerscloud.pos.controller;

import com.seekerscloud.pos.db.Database;
import com.seekerscloud.pos.modal.Customer;
import com.seekerscloud.pos.modal.Item;
import com.seekerscloud.pos.modal.ItemDetails;
import com.seekerscloud.pos.modal.Order;
import com.seekerscloud.pos.view.tm.CartTm;
import com.seekerscloud.pos.view.tm.ItemTm;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import javax.xml.crypto.Data;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Optional;

public class PlaceOrderFormController {
    public TextField txtDate;
    public ComboBox<String> cmbCustomerIds;
    public ComboBox<String> cmbItemCodes;
    public TextField txtName;
    public TextField txtAddress;
    public TextField txtSalary;
    public TextField txtDescription;
    public TextField txtUnitPrice;
    public TextField txtQtyOnHand;
    public TextField txtQty;
    public TableView<CartTm> tblCart;
    public TableColumn colCode;
    public TableColumn colDescription;
    public TableColumn colUnitPrice;
    public TableColumn colQty;
    public TableColumn colTotal;
    public TableColumn colOption;
    public Label lblTotal;
    public TextField txtOrderId;
    public AnchorPane placeOrderFormContext;

    public void initialize(){

        colCode.setCellValueFactory(new PropertyValueFactory<>("code"));
        colDescription.setCellValueFactory(new PropertyValueFactory<>("description"));
        colUnitPrice.setCellValueFactory(new PropertyValueFactory<>("unitPrice"));
        colQty.setCellValueFactory(new PropertyValueFactory<>("qty"));
        colTotal.setCellValueFactory(new PropertyValueFactory<>("total"));
        colOption.setCellValueFactory(new PropertyValueFactory<>("btn"));

        setDateAndOrderId();
        loadAllCustomerIds();
        loadAllItemCodes();
        setOrderId();

        cmbCustomerIds.getSelectionModel()
                .selectedItemProperty()
                .addListener((observable, oldValue, newValue) -> {
                    if(newValue!=null){
                        setCustomerDetails();
                    }
        });

        cmbItemCodes.getSelectionModel()
                .selectedItemProperty()
                .addListener((observable, oldValue, newValue) -> {
                    if(newValue!=null){
                        setItemDetails();
                    }
                });

    }

    private void setOrderId() {
        if (Database.orderTable.isEmpty()){
            txtOrderId.setText("D-1");
            return;
        }
        String tempOrderId=Database.orderTable.get(Database.orderTable.size()-1).getOrderId();// D-3
        String[] array = tempOrderId.split("-");//[D,3]
        int tempNumber=Integer.parseInt(array[1]);
        int finalizeOrderId=tempNumber+1;
        txtOrderId.setText("D-"+finalizeOrderId);
    }

    private void setItemDetails() {
        for (Item i:Database.itemTable
        ) {
            if (i.getCode().equals(cmbItemCodes.getValue())){
                txtDescription.setText(i.getDescription());
                txtUnitPrice.setText(String.valueOf(i.getUnitPrice()));
                txtQtyOnHand.setText(String.valueOf(i.getQtyOnHand()));
            }
        }
    }

    private void setCustomerDetails() {
        for (Customer c:Database.customerTable
             ) {
            if (c.getId().equals(cmbCustomerIds.getValue())){
                txtName.setText(c.getName());
                txtAddress.setText(c.getAddress());
                txtSalary.setText(String.valueOf(c.getSalary()));
            }
        }
    }

    private void loadAllItemCodes() {
        for(Item i : Database.itemTable){
            cmbItemCodes.getItems().add(i.getCode());
        }
    }

    private void loadAllCustomerIds() {
        for(Customer c: Database.customerTable){
            cmbCustomerIds.getItems().add(c.getId());
        }
    }

    private void setDateAndOrderId() {
        // set Date
        /*Date date = new Date();
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        String d =df.format(date);
        txtDate.setText(d);*/
        txtDate.setText(new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
    }

    private boolean checkQty(String code,int qty){
        for (Item i :Database.itemTable) {
                    if (code.equals(i.getCode())) {
                        if (i.getQtyOnHand()>=qty){
                            return true;
                        }else{
                            return false;
                        }
                    }
                }
            return false;
    }

    ObservableList<CartTm> obList = FXCollections.observableArrayList();

    public void addToCartOnAction(ActionEvent actionEvent) {

        if (!checkQty(cmbItemCodes.getValue(), Integer.parseInt(txtQty.getText()))){
            new Alert(Alert.AlertType.WARNING,"Out Of Stock").show();
            return;
        }


        double unitPrice = Double.parseDouble(txtUnitPrice.getText());
        int qty=Integer.parseInt(txtQty.getText());
        double total = unitPrice*qty;
        Button btn = new Button("Delete");
        int row = isAlreadyExists(cmbItemCodes.getValue());


        if (row==-1){
            CartTm tm = new CartTm(cmbItemCodes.getValue(),txtDescription.getText(),unitPrice,qty,total,btn);
            obList.add(tm);
            tblCart.setItems(obList);
        }else{
            int tempQty=obList.get(row).getQty()+qty;
            double tempTotal = unitPrice* tempQty;

            if (!checkQty(cmbItemCodes.getValue(),tempQty)){
                new Alert(Alert.AlertType.WARNING,"Out Of Stock").show();
                return;
            }

            obList.get(row).setQty(tempQty);
            obList.get(row).setTotal(tempTotal);
            tblCart.refresh();
        }

        calculateTotal();
        clearFields();
        cmbItemCodes.requestFocus();


        btn.setOnAction(event -> {
            Alert alert= new Alert(Alert.AlertType.CONFIRMATION, "Are you sure?", ButtonType.YES,ButtonType.NO);
            Optional<ButtonType> buttonType = alert.showAndWait();

            if (buttonType.get()==ButtonType.YES){
                for (CartTm tm: obList
                     ) {
                        obList.remove(tm);
                        calculateTotal();
                        tblCart.refresh();
                        return;
                }
            }

        });

    }

    private void clearFields() {
        txtDescription.clear();
        txtUnitPrice.clear();
        txtQtyOnHand.clear();
        txtQty.clear();
    }

    private int isAlreadyExists(String code){
        for (int i = 0; i < obList.size(); i++) {
            if (obList.get(i).getCode().equals(code)){
                return i;
            }
        }
        return -1;
    }

    private void calculateTotal(){
        double total=0.00;
        for (CartTm tm: obList
             ) {
            total+=tm.getTotal();
        }
        lblTotal.setText(String.valueOf(total));
    }

    public void placeOrderOnAction(ActionEvent actionEvent) {
        if (obList.isEmpty()) return;
        ArrayList<ItemDetails> details= new ArrayList<>();
        for (CartTm tm:obList
             ) {
            details.add(new ItemDetails(tm.getCode(),
                    tm.getUnitPrice(), tm.getQty()));
        }
        Order order= new Order(
                txtOrderId.getText(),new Date(),
                Double.parseDouble(lblTotal.getText()),
                cmbCustomerIds.getValue(),details
        );
        Database.orderTable.add(order);
        manageQty();
        clearAll();
    }

    private void manageQty() {
        for (CartTm tm:obList
             ) {
            for (Item i: Database.itemTable
                 ) {
                if (i.getCode().equals(tm.getCode())){
                    i.setQtyOnHand(i.getQtyOnHand()-tm.getQty());
                    break;
                }
            }
        }
    }

    private void clearAll() {
        obList.clear();
        calculateTotal();

        txtName.clear();
        txtAddress.clear();
        txtSalary.clear();

        //=======
        cmbCustomerIds.setValue(null);
        cmbItemCodes.setValue(null);
        //========

        clearFields();
        cmbCustomerIds.requestFocus();
        setOrderId();
    }

    public void backToHomeOnAction(ActionEvent actionEvent) throws IOException {
        Stage stage= (Stage) placeOrderFormContext.getScene().getWindow();
        stage.setScene(new Scene
                (FXMLLoader.load(getClass().
                        getResource("../view/DashboardForm.fxml"))));
    }
}
