package com.seekerscloud.pos.controller;

import com.seekerscloud.pos.db.Database;
import com.seekerscloud.pos.modal.ItemDetails;
import com.seekerscloud.pos.modal.Order;
import com.seekerscloud.pos.view.tm.ItemDetailsTm;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import java.io.IOException;

public class ItemDetailsFormController {

    public AnchorPane itemDetailsContext;
    public TableColumn colCode;
    public TableColumn colUnitPrice;
    public TableColumn colQTY;
    public TableColumn colTotal;
    public TableView tblItems;

    public void initialize(){
        colCode.setCellValueFactory(new PropertyValueFactory<>("code"));
        colTotal.setCellValueFactory(new PropertyValueFactory<>("total"));
        colUnitPrice.setCellValueFactory(new PropertyValueFactory<>("unitPrice"));
        colQTY.setCellValueFactory(new PropertyValueFactory<>("qty"));
    }

    public void loadOrderDetails(String id){
        for (Order o : Database.orderTable) {
            if(o.getOrderId().equals(id)){
                ObservableList<ItemDetailsTm> tmList= FXCollections.observableArrayList();
                for (ItemDetails d : o.getItemDetails()) {
                    double tempUnitPrice=d.getUnitPrice();
                    int tempQty=d.getQty();
                    double tempTotal=tempQty*tempUnitPrice;
                    tmList.add(new ItemDetailsTm(
                            d.getCode(),d.getUnitPrice(),d.getQty(),tempTotal
                    ));
                }
                tblItems.setItems(tmList);
            }
        }
    }

    public void backToHomeOnAction(ActionEvent actionEvent) throws IOException {
        Stage stage = (Stage) itemDetailsContext.getScene().getWindow();
        stage.setScene(new Scene(
                FXMLLoader.load(getClass().
                        getResource("../view/DashboardForm.fxml"))));
    }
}
