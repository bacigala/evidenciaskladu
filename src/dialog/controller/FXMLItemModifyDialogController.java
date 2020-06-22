
package dialog.controller;

import java.io.IOException;
import java.net.URL;
import java.util.*;

import databaseAccess.CategoryDAO;
import databaseAccess.ItemDAO;
import databaseAccess.Login;
import dialog.DialogFactory;
import domain.Category;
import domain.CustomAttribute;
import domain.Item;
import databaseAccess.ComplexQueryHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * Dialog for Item modification.
 */

public class FXMLItemModifyDialogController implements Initializable {

    @FXML private javafx.scene.layout.AnchorPane mainAnchorPane;
    @FXML private javafx.scene.control.TextField nameTextField;
    @FXML private javafx.scene.control.TextField codeTextField;
    @FXML private javafx.scene.control.TextField curAmountTextField;
    @FXML private javafx.scene.control.TextField minAmountTextField;
    @FXML private javafx.scene.control.TextField unitTextField;
    @FXML private javafx.scene.control.ChoiceBox<Category> categoryChoiceBox;
    @FXML private javafx.scene.control.TableView<CustomAttribute> tableCustomAttributes;
    @FXML private javafx.scene.control.Button permanentDeletionButton;

    private Item item;
    private HashSet<CustomAttribute> attributesToAdd = new HashSet<>();
    private HashSet<CustomAttribute> attributesToDelete = new HashSet<>();
    private HashSet<CustomAttribute> originalAttributes;
    private boolean isNewItem = false;

    @Override
    public void initialize(URL url, ResourceBundle rb) {

    }

    /**
     * Receives initialization data after dialog is shown.
     * Setups default values.
     */
    public void initData(Item item, HashSet<CustomAttribute> customAttributes, boolean isNewItem) {
        this.isNewItem = isNewItem;
        initData(item, customAttributes);
    }

    public void initData(Item item, HashSet<CustomAttribute> customAttributes) {

        if (item != null) {
            ComplexQueryHandler qh = ComplexQueryHandler.getInstance();

            permanentDeletionButton.setDisable(isNewItem && Login.getInstance().hasAdmin());

            this.item = item;
            nameTextField.setText(item.getName());
            codeTextField.setText(item.getBarcode());
            curAmountTextField.setText(Integer.toString(item.getCurAmount()));
            minAmountTextField.setText(Integer.toString(item.getMinAmount()));
            unitTextField.setText(item.getUnit());
            categoryChoiceBox.getItems().addAll(CategoryDAO.getInstance().getCategoryMap().values());
            categoryChoiceBox.setValue(CategoryDAO.getInstance().getCategoryMap().get(item.getCategory()));

            // custom attributes table
            TableColumn attributeName = new TableColumn("Atribút");
            attributeName.setCellValueFactory(new PropertyValueFactory<>("name"));

            TableColumn attributeValue = new TableColumn("Hodnota");
            attributeValue.setCellValueFactory(new PropertyValueFactory<>("value"));

            tableCustomAttributes.getColumns().addAll(attributeName, attributeValue);

            originalAttributes = customAttributes;
            populateCustomAttributesTable();
        }
    }

    /**
     * Button 'Ulozit' sends desired changes to QueryHandler.
     */
    @FXML
    private void saveButton() {
        ComplexQueryHandler qh = ComplexQueryHandler.getInstance();
        DialogFactory df = DialogFactory.getInstance();
        try {
            if (isNewItem) {
                if (nameTextField.getText().equals("")) {
                    df.showAlert(Alert.AlertType.WARNING, "Prosím, zadajte názov položky.");
                    return;
                }
                int minAmount = 0;
                if (!minAmountTextField.getText().equals("")) {
                    minAmount = Integer.parseInt(minAmountTextField.getText());
                }
                Item newItem = new Item(0, nameTextField.getText(), codeTextField.getText(), minAmount,
                        0, unitTextField.getText(), "", categoryChoiceBox.getValue().getId());
                if (ItemDAO.getInstance().itemInsert(newItem, attributesToAdd)) {
                    DialogFactory.getInstance().showAlert(Alert.AlertType.INFORMATION, "Nová položka bola úspešne vytvorená.");
                    cancelButton();
                } else {
                    throw new IOException();
                }
            } else {
                HashMap<String, String> newBasicValues = new HashMap<>();
                //check whether attribute was changed -> add to hashMap
                if (!nameTextField.getText().equals(item.getName())) {
                    newBasicValues.put("name", nameTextField.getText());
                }
                if (!codeTextField.getText().equals(item.getBarcode())) {
                   newBasicValues.put("barcode", codeTextField.getText());
                }
                if (!minAmountTextField.getText().equals(item.getMinAmount())) {
                    newBasicValues.put("min_amount", minAmountTextField.getText());
                }
                if (!unitTextField.getText().equals(item.getUnit())) {
                    newBasicValues.put("unit", unitTextField.getText());
                }
                if (!categoryChoiceBox.getValue().getName().equals(item.getCategoryName())) {
                    newBasicValues.put("category", Integer.toString(categoryChoiceBox.getValue().getId()));
                }
                if (ItemDAO.getInstance().itemUpdate(item, newBasicValues, attributesToAdd, attributesToDelete)) {
                    DialogFactory.getInstance().showAlert(Alert.AlertType.INFORMATION, "Úprava položky prebehla úspešne.");
                    cancelButton();
                } else {
                    throw new IOException();
                }
            }
        } catch (Exception e) {
            df.showAlert(Alert.AlertType.ERROR, "Akciu sa nepodarilo vykonať. Skontrolujte prosím zadané hodnoty.");
        }
    }

    /**
     * Button 'Trvalo odstrániť' requests item deleto from DB - admin only.
     */
    @FXML
    private void permanentDeleteButtonAction() throws IOException {
        ComplexQueryHandler qh = ComplexQueryHandler.getInstance();
        DialogFactory df = DialogFactory.getInstance();
        // todo: extra warning before delete
        try {
            if (ItemDAO.itemDelete(item)) {
                DialogFactory.getInstance().showAlert(Alert.AlertType.INFORMATION, "Položka bola úspešne odstránená z databázy.");
                cancelButton();
            } else {
                df.showAlert(Alert.AlertType.ERROR, "Akciu sa nepodarilo vykonať.");
            }
        } catch (Exception e) {
            df.showAlert(Alert.AlertType.ERROR, "Akciu sa nepodarilo vykonať.");
        }
    }

    /**
     * Button 'Pridat udaj' Creates dialog for new CustomAttribute input.
     */
    @FXML
    private void newCustomAttributeButton() throws IOException {
        mainAnchorPane.setDisable(true);
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("../fxml/FXMLCustomAttributeCreateDialog.fxml"));
        Parent root1 = fxmlLoader.load();
        Stage stage = new Stage();
        stage.setScene(new Scene(root1));
        stage.initModality(Modality.APPLICATION_MODAL);
        FXMLCustomAttributeCreateDialogController controller = fxmlLoader.getController();
        controller.initData(attributesToAdd);
        stage.setTitle("Nový atribút");
        stage.showAndWait();
        System.out.println(attributesToAdd);
        System.out.println(attributesToDelete);
        populateCustomAttributesTable();
        mainAnchorPane.setDisable(false);
    }

    /**
     * Button 'Zrusit' Cancels planed changes.
     */
    @FXML
    private void cancelButton() {
        Stage stage = (Stage) nameTextField.getScene().getWindow();
        stage.close();
    }

    /**
     * CustomAttributeChange dialog popup on custom attribute clicked.
     */
    @FXML
    private void showCustomAttributeChangeDialog() throws IOException {
        mainAnchorPane.setDisable(true);
        CustomAttribute selected = tableCustomAttributes.getSelectionModel().getSelectedItem();
        if (selected != null) {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("../fxml/FXMLCustomAttributeModifyDialog.fxml"));
            Parent root1 = fxmlLoader.load();
            Stage stage = new Stage();
            stage.setScene(new Scene(root1));
            stage.initModality(Modality.APPLICATION_MODAL);
            FXMLCustomAttributeModifyDialogController controller = fxmlLoader.getController();
            controller.initData(selected, attributesToAdd, attributesToDelete);
            stage.setTitle("Upraviť atribút");
            stage.showAndWait();
        }
        System.out.println(attributesToAdd);
        System.out.println(attributesToDelete);
        populateCustomAttributesTable();
        mainAnchorPane.setDisable(false);
    }

    /**
     * Populates table with provided CustomAttributes HashSet.
     */
    private void populateCustomAttributesTable() {
        // todo: add also elements from 2 special dedicated sets of to-delete and to-add
        tableCustomAttributes.getItems().clear();
        HashSet<CustomAttribute> newAttributes;
        if (originalAttributes != null) {
            newAttributes = (HashSet) originalAttributes.clone();
        } else {
            newAttributes = new HashSet<>();
        }
        newAttributes.removeAll(attributesToDelete);
        newAttributes.addAll(attributesToAdd);
        if (!newAttributes.isEmpty()) {
            for (CustomAttribute ca : newAttributes) {
                tableCustomAttributes.getItems().add(ca);
            }
        } else {
            tableCustomAttributes.setPlaceholder(new Label("Bez ďalších atribútov."));
        }
    }

}
