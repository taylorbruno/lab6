package pkgPoker.app.controller;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.RadioMenuItem;
import javafx.scene.control.ToggleGroup;
import javafx.stage.FileChooser;
import pkgPoker.app.MainApp;
import pkgPokerBLL.Card;
import pkgPokerEnum.eGame;
import pkgPokerEnum.eRank;
import pkgPokerEnum.eSuit;


/**
 * The controller for the root layout. The root layout provides the basic
 * application layout containing a menu bar and space where other JavaFX
 * elements can be placed.
 * 
 */
public class RootLayoutController implements Initializable {

	private MainApp mainApp;

	@FXML
	private MenuBar mb;

	@FXML
	private Menu mnuGame;


	public String getRuleName()
	{	
		String strRuleID = null;
		for (Menu m: mb.getMenus())
		{
			if (m.getText() == "Pick Game")
			{
				for (MenuItem mi: m.getItems())
				{
					if (mi.getClass().equals(RadioMenuItem.class))
					{
						RadioMenuItem rmi = (RadioMenuItem)mi;
						if (rmi.isSelected() == true)
						{
							strRuleID = rmi.getId();
							break;
						}
					}
				}
			}
		}
		
		return strRuleID;
	}
	
	public void initialize(URL location, ResourceBundle resources) {

		BuildMenus();
	}
	
	public void BuildMenus()
	{

		
		
		Menu mnuGame = new Menu();
		mnuGame.setText("Pick Game");
		mb.getMenus().add(0,mnuGame);
				
		ToggleGroup tglGrpGame = new ToggleGroup();
		
		for (eGame eGame : eGame.values()) {
			RadioMenuItem rmi = new RadioMenuItem(eGame.toString());
			rmi.setId("PokerGame" + String.valueOf(eGame.getGame()));
			rmi.setToggleGroup(tglGrpGame);
			if (eGame.getDefault())
			{
				rmi.setSelected(true);
			}
			mnuGame.getItems().add(rmi);
		}
		
		Menu mnuBet = new Menu();
		mnuBet.setText("Betting");
		mb.getMenus().add(1,mnuBet);
		ToggleGroup tglBet = new ToggleGroup();
		
		RadioMenuItem rmi1 = new RadioMenuItem("No Limit");
		rmi1.setSelected(true);
		rmi1.setToggleGroup(tglBet);
		RadioMenuItem rmi2 = new RadioMenuItem("Pot Limit");
		rmi2.setToggleGroup(tglBet);
		
		mnuBet.getItems().add(rmi1);
		mnuBet.getItems().add(rmi2);


	}
  
	
	public void setMainApp(MainApp mainApp) {
		this.mainApp = mainApp;
	}


	@FXML
	private void handleAbout() {
		Alert alert = new Alert(AlertType.INFORMATION);
		alert.setTitle("AddressApp");
		alert.setHeaderText("About");
		alert.setContentText("Author: Bert Gibbons");

		alert.showAndWait();
	}

	/**
	 * Closes the application.
	 */
	@FXML
	private void handleExit() {
		System.exit(0);
	}
}