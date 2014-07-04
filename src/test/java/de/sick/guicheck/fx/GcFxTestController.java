// Copyright 2013 SICK AG. All rights reserved.
package de.sick.guicheck.fx;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Control;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * @author  linggol (created)
 */
public class GcFxTestController
{
    @FXML
    public void onFileCloseAction(ActionEvent e)
    {
        System.out.println("File close called");
    }
    @FXML
    public void onButtonAction(ActionEvent e)
    {
        System.out.println("Button pressed: " + e.getSource());
    }
    @FXML
    public void onOpenDialog(ActionEvent e)
    {
        final Stage l_stage = new Stage();
        l_stage.initModality(Modality.WINDOW_MODAL);
        l_stage.initOwner(((Control)e.getSource()).getScene().getWindow());
        l_stage.setTitle("Modal Dialog");
        l_stage.showAndWait();
    }
}
