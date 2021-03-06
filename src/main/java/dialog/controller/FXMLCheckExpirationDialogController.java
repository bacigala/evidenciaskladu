
package dialog.controller;

import java.io.IOException;
import java.net.URL;
import java.util.*;
import databaseAccess.*;
import databaseAccess.CustomExceptions.UserWarningException;
import dialog.DialogFactory;
import domain.ExpiryDateWarningRecord;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.util.Callback;

/**
 * Dialog for soon expiration date display.
 * Lists all items with soon expiry date.
 */

public class FXMLCheckExpirationDialogController implements Initializable {
    @FXML private javafx.scene.control.TableView<ExpiryDateWarningRecord> mainTable;

    private final ObservableList<ExpiryDateWarningRecord> itemList = FXCollections.observableArrayList();

    /**
     * Requests current list of Items from DB and displays it in the table.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TableView setup
        TableColumn itemNameColumn = new TableColumn<ExpiryDateWarningRecord, String>("Názov");
        itemNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));

        TableColumn itemCurrentAmountColumn = new TableColumn<ExpiryDateWarningRecord, String>("Počet");
        itemCurrentAmountColumn.setCellValueFactory(new PropertyValueFactory<>("expiryAmount"));

        TableColumn itemDetailButtonColumn = new TableColumn<>("Detail");
        itemDetailButtonColumn.setSortable(false);

        itemDetailButtonColumn.setCellValueFactory(
                (Callback<TableColumn.CellDataFeatures<ExpiryDateWarningRecord, Boolean>, ObservableValue<Boolean>>)
                        p -> new SimpleBooleanProperty(p.getValue() != null));

        itemDetailButtonColumn.setCellFactory(
                (Callback<TableColumn<ExpiryDateWarningRecord, Boolean>, TableCell<ExpiryDateWarningRecord, Boolean>>) p -> new ButtonCell());

        mainTable.getColumns().addAll(itemNameColumn, itemCurrentAmountColumn, itemDetailButtonColumn);
        mainTable.setPlaceholder(new Label("Žiadne záznamy."));
        Property<ObservableList<ExpiryDateWarningRecord>> listProperty = new SimpleObjectProperty<>(itemList);
        mainTable.itemsProperty().bind(listProperty);
        populateTable();
    }

    // cell in action column
    private class ButtonCell extends TableCell<ExpiryDateWarningRecord, Boolean> {
        final Button trashButton = new Button("Vyhodiť");

        ButtonCell() {
            trashButton.setOnAction(t -> {
                FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/FXMLItemOfftakeDialog.fxml"));
                Parent root1;
                try {
                    root1 = fxmlLoader.load();
                } catch (IOException e) {
                    e.printStackTrace();
                    return;
                }
                Stage stage = new Stage();
                stage.setScene(new Scene(root1));
                stage.initModality(Modality.APPLICATION_MODAL);
                FXMLItemOfftakeDialogController controller = fxmlLoader.getController();
                controller.initData(getTableView().getItems().get(getIndex()), true);
                stage.setTitle("Odstránenie expirovaných položiek");
                stage.showAndWait();

                populateTable();
            });
        }

        HBox pane = new HBox(trashButton);

        //Display button if the row is not empty
        @Override
        protected void updateItem(Boolean t, boolean empty) {
            super.updateItem(t, empty);
            if (empty || t == null) {
                setGraphic(null);
                return;
            }
            setGraphic(pane);
        }
    }

    public void initData() {

    }

    /**
     * Button 'Zavriet' Closes the dialog.
     */
    @FXML
    private void closeButtonAction() {
        ((Stage) mainTable.getScene().getWindow()).close();
    }

    /**
     * Populates table with provided UserAccounts.
     */
    private void populateTable() {
        itemList.clear();
        try {
            ComplexQueryHandler.getInstance().getSoonExpiryItems(itemList);
        } catch (UserWarningException e) {
            DialogFactory.getInstance().showAlert(Alert.AlertType.ERROR, e.getMessage());
        } catch (Exception e) {
            DialogFactory.getInstance().showAlert(Alert.AlertType.ERROR, "Neočakávaná chyba.");
        }
    }

}
