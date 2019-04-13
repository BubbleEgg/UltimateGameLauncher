package GUI.screens.AddGame.Steam;

import GUI.Menu;
import GUI.localization.Language;
import GUI.screens.Alert.Alert;
import GUI.screens.Alert.SimpleAlert;
import GUI.screens.Notification.*;
import GUI.screens.misc.Callback;
import api.GameLauncher.GameLauncher;
import api.GameLauncher.Steam.SteamConfigUser;
import api.GameLauncher.Steam.SteamUser;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXPasswordField;
import com.jfoenix.controls.JFXTextField;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

/**
 * Removing of this disclaimer is forbidden.
 *
 * @author BubbleEgg
 * @verions: 1.0.0
 **/

public class NewSteamUserController {
	
	@FXML
	private JFXTextField username;
	@FXML
	private JFXPasswordField password;
	@FXML
	private JFXTextField id;
	@FXML
	private Label error;
	@FXML
	private Label title;
	@FXML
	private Label usernameLabel;
	@FXML
	private Label passwordLabel;
	@FXML
	private JFXButton add;
	
	private Alert alert;
	private GameLauncher launcher;
	private NewSteamUserCallback callback;
	private EditSteamUserMode mode;
	private SteamUser user;
	
	public void init(Alert alert, EditSteamUserMode mode) {
		this.alert = alert;
		this.mode = mode;
		error.setVisible(false);
		
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				usernameLabel.setText(Language.format(Menu.lang.getLanguage().Username));
				passwordLabel.setText(Language.format(Menu.lang.getLanguage().Password));
				switch(mode){
					case NEW:{
						title.setText(Language.format(Menu.lang.getLanguage().WindowTitleAddSteamUser));
						add.setText(Language.format(Menu.lang.getLanguage().ButtonAdd));
						break;
					}
					case EDIT:{
						title.setText(Language.format(Menu.lang.getLanguage().WindowTitleEditSteamUser));
						add.setText(Language.format(Menu.lang.getLanguage().ButtonFinish));
						break;
					}
				}
			}
		});
	}
	
	@FXML
	void onFinish(){
		if(username.getText().isEmpty()||password.getText().isEmpty()||id.getText().isEmpty()){
			error.setText(Language.format(Menu.lang.getLanguage().ErrorFillAllFields));
			error.setVisible(true);
			return;
		}
		if(mode==EditSteamUserMode.NEW&&launcher.steam.getUser(username.getText()).exists()){
			error.setText(Language.format(Menu.lang.getLanguage().ErrorUserAlreadyExists));
			error.setVisible(true);
			return;
		}
		
		switch(mode) {
			case NEW: {
				launcher.getSteam().createUser(username.getText(), password.getText(), id.getText());
				if(launcher.getSteam().getUsernames().size()==1){
					launcher.getSteam().getUser(username.getText()).setAsMainAccount();
				}
				
				SimpleAlert sa = new SimpleAlert();
				sa.setTitle(Language.format(Menu.lang.getLanguage().AlertSteamUserImportAppsTitle));
				sa.setMessage(Language.format(Menu.lang.getLanguage().ImportLibrary));
				sa.addOption(Language.format(Menu.lang.getLanguage().ImportLibraryAll), ButtonAlignment.RIGHT, new ButtonCallback() {
					@Override
					public void onClick() {
						new Thread(new Task<Void>() {
							@Override
							protected Void call() throws Exception {
								launcher.getSteam().getUser(username.getText()).addGames(true);
								return null;
							}
						}).start();
					}
				}, true, ButtonStyle.GHOST);
				sa.addOption(Language.format(Menu.lang.getLanguage().ImportLibraryOnlyPaid), ButtonAlignment.RIGHT, new ButtonCallback() {
					@Override
					public void onClick() {
						new Thread(new Task<Void>() {
							@Override
							protected Void call() throws Exception {
								launcher.getSteam().getUser(username.getText()).addGames(false);
								return null;
							}
						}).start();
					}
				}, true, ButtonStyle.GHOST);
				sa.addOption(Language.format(Menu.lang.getLanguage().ImportLibraryNoThanks), ButtonAlignment.RIGHT, new ButtonCallback() {
					@Override
					public void onClick() {
					}
				}, true, ButtonStyle.GHOST);
				
				Alert alert = new Alert(Menu.root);
				alert.setContent(sa);
				alert.setBackground(Menu.rootAnchor);
				alert.setBackgroundBlur(10);
				alert.setBackgroundColorAdjust(0, 0, -0.3, 0);
				alert.show();
				break;
			}
			case EDIT:{
				launcher.getSteam().deleteUser(user.getUsername());
				launcher.getSteam().createUser(username.getText(), password.getText(), id.getText());
				break;
			}
		}
		
		
		this.alert.close();
	}
	
	@FXML
	void checkForID() {
		if(!username.getText().isEmpty()&&mode==EditSteamUserMode.NEW) {
			for(SteamConfigUser scu : launcher.getSteam().loadAccounts()){
				if(scu.getUsername().equalsIgnoreCase(username.getText())){
					this.id.setText(scu.getSteam64id());
				}
			}
		}
	}
	
	public void setUserData(SteamUser user){
		this.user = user;
		username.setText(user.getUsername());
		password.setText(user.getPassword());
		id.setText(user.getID());
	}
	
	public void setLauncher(GameLauncher launcher) {
		this.launcher = launcher;
	}
	
	public void setCallback(NewSteamUserCallback callback) {
		this.callback = callback;
	}
}
