package pkgPoker.app;

import java.io.IOException;
import java.io.Serializable;
import java.io.StringWriter;
import java.util.function.Consumer;
import javax.swing.SwingUtilities;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.VBoxBuilder;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import netgame.common.Client;
import pkgPoker.app.model.PokerHub;

import pkgPoker.app.controller.ClientServerStartController;
import pkgPoker.app.controller.PokerTableController;



import pkgPoker.app.controller.RootLayoutController;
import pkgPokerBLL.Action;
import pkgPokerBLL.GamePlay;
import pkgPokerBLL.Player;
import pkgPokerBLL.Table;
import pkgPokerEnum.eAction;

public class MainApp extends Application {

	private Stage primaryStage;
	private BorderPane rootLayout;

	private PokerHub pHub = null;
	private PokerClient pClient = null;

	private PokerTableController pokerController = null;
	private RootLayoutController rootController = null;

	private boolean isServer = false;

	private Player appPlayer;

	public int GetPlayerID()
	{
		return pClient.getID();
	}
	public Player getPlayer() {
		return appPlayer;
	}

	public void setPlayer(Player player) {
		this.appPlayer = player;
	}

	public static void main(String[] args) {
		launch(args);
	}

	@Override
	public void init() throws Exception {

	}

	@Override
	public void start(Stage primaryStage) throws Exception {

		BorderPane root = new BorderPane();
		Scene scene = new Scene(root, 500, 500);

		this.primaryStage = primaryStage;
		this.primaryStage.setTitle("Poker");
		this.primaryStage.setScene(scene);
		this.primaryStage.show();

		showClientServer();
	}

	public void showPoker(boolean bStartHub, String strComputerName, int iPort, String strPlayerName) {

		if (bStartHub) {
			try {
				pHub = new PokerHub(iPort);
			} catch (Exception e) {
				System.out.println("Error: Can't listen on port " + iPort);
				return;
			}
		}
		try {
			pClient = new PokerClient(strComputerName, iPort);
		} catch (IOException e) {
			e.printStackTrace();
		}

		setPlayer(new Player(strPlayerName, pClient.getID()));
		
		initRootLayout();

		showPokerTable();		
		
		if (!bStartHub) {
		
			Action act = new Action();
			act.setAction(eAction.TableState);
			act.setPlayer(this.getPlayer());
			this.messageSend(act);
		}
	}

	public void showClientServer() {
		try {
		
			FXMLLoader loader = new FXMLLoader();
			
			loader =  new FXMLLoader(getClass().getResource("/poker/app/view/ClientServerStart.fxml"));
			 
			BorderPane ClientServerOverview = (BorderPane) loader.load();

			Scene scene = new Scene(ClientServerOverview);

			primaryStage.setScene(scene);

			// Give the controller access to the main app.
			ClientServerStartController controller = loader.getController();
			controller.setMainApp(this);

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void initRootLayout() {
		try {
			
			Screen screen = Screen.getPrimary();
			Rectangle2D bounds = screen.getVisualBounds();

			primaryStage.setX(bounds.getMinX());
			primaryStage.setY(bounds.getMinY());
			primaryStage.setWidth(bounds.getWidth());
			primaryStage.setHeight(bounds.getHeight());
			
			// Load root layout from fxml file.
			FXMLLoader loader = new FXMLLoader();
			loader.setLocation(MainApp.class.getResource("/poker/app/view/RootLayout.fxml"));
			rootLayout = (BorderPane) loader.load();

			// Show the scene containing the root layout.
			Scene scene = new Scene(rootLayout);			
			scene.getStylesheets().add("include/css/DarkTheme.css");				
			primaryStage.setScene(scene);

			// Give the controller access to the main app.
			// RootLayoutController controller = loader.getController();
			rootController = loader.getController();

			rootController.setMainApp(this);

			primaryStage.show();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void showPokerTable() {
		try {
			FXMLLoader loader = new FXMLLoader();
			loader.setLocation(MainApp.class.getResource("/poker/app/view/PokerTable.fxml"));
			BorderPane pokerOverview = (BorderPane) loader.load();

			rootLayout.setCenter(pokerOverview);
			pokerController = loader.getController();
			pokerController.setMainApp(this);
			
			getPlayer().setiPlayerPosition(0);
			Action act = new Action(eAction.TableState, getPlayer());
			messageSend(act);

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void EndPoker() {
		
		System.out.println("Player quit");
		
		primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
			@Override
			public void handle(WindowEvent t) {
				Platform.exit();
				System.exit(0);
			}
		});
	}

	@Override
	public void stop() throws Exception {
		System.out.println("Stopped");
		
		Action act = new Action();
		act.setAction(eAction.Leave);
		act.setPlayer(this.getPlayer());
		this.messageSend(act);
	}

	public void messageSend(final Object message)
	{
		System.out.println("Sending message from MainApp");
		pClient.messageSend(message);	
	}
	
	public String getRuleName()
	{
		return rootController.getRuleName();
	}
	
	private class PokerClient extends Client {

		public PokerClient(String hubHostName, int hubPort) throws IOException {
			super(hubHostName, hubPort);
		}

		
		protected void messageSend(Object message)
		{
			System.out.println("Sending message from MainApp.Client");
			resetOutput();
			super.send(message);
		}
		
		@Override
		protected void messageReceived(final Object message) {
			Platform.runLater(() -> {		
				System.out.println("Message Received.  The message: " + message);
				
				if (message instanceof String)
				{				
					System.out.println("Message Received " + message);
				}
				else if (message instanceof Table)
				{
					pokerController.Handle_TableState((Table)message);
				}
				else if (message instanceof GamePlay)
				{
					pokerController.Handle_GameState((GamePlay)message);
				}
				
			});
		}
		
		
		@Override

	    protected void serverShutdown(String message) {
	    	
			Platform.runLater(() -> {		
				Platform.exit();
		        System.exit(0);
			});
	    }
	}
}