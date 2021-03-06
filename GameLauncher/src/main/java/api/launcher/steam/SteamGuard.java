package api.launcher.steam;

import api.launcher.GameLauncher;
import codebehind.steam.mobileauthentication.SteamGuardAccount;
import codebehind.steam.mobileauthentication.UserLoginService;
import codebehind.steam.mobileauthentication.model.*;
import codebehind.toolbelt.JsonHelper;

import java.io.File;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Scanner;

/**
 * Removing of this disclaimer is forbidden.
 *
 * @author BubbleEgg
 * @verions: 1.0.0
 **/

public class SteamGuard {
	
	private GameLauncher launcher;
	
	public SteamGuard(GameLauncher launcher) {
		this.launcher = launcher;
	}
	
	private UserLoginService loginSvc = new UserLoginService();
	private File secretSGFile;
	
	private HashMap<String, SteamGuardAccount> sga = new HashMap<>();
	private HashMap<String, SessionData> session = new HashMap<>();
	
	public void enterCode(SteamUser user) {
		try {
			File loginHelper = new File(launcher.folderPath+"SteamLoginHelper.exe");
			if(loginHelper.exists()) {
				ProcessBuilder start = new ProcessBuilder(loginHelper.getAbsolutePath(), getCode(user));
				start.start();
			}
		} catch(Exception e) {
		
		} catch(Throwable throwable) {
			throwable.printStackTrace();
		}
	}
	
	public String getCode(SteamUser user) throws Throwable {
		secretSGFile = new File(launcher.folderPath + "Users/SteamGuard_" + user.getUsername() + ".json");
		if(secretSGFile.exists() && !secretSGFile.isDirectory()&& !sga.containsKey(user.getUsername())) {
			sga.put(user.getUsername(), new SteamGuardAccount(JsonHelper.Deserialize(SteamGuardAccount.Config.class, secretSGFile)));
		}
		return sga.get(user.getUsername()).GenerateSteamGuardCode();
	}
	
	public Confirmation[] getConfirmations(SteamUser user) {
		secretSGFile = new File(launcher.folderPath + "Users/SteamGuard_" + user.getUsername() + ".json");
		if(secretSGFile.exists() && !secretSGFile.isDirectory()&& !sga.containsKey(user.getUsername())) {
			try {
				sga.put(user.getUsername(), new SteamGuardAccount(JsonHelper.Deserialize(SteamGuardAccount.Config.class, secretSGFile)));
				doLogin(user);
			} catch(Exception e) {
				e.printStackTrace();
			} catch(Throwable throwable) {
				throwable.printStackTrace();
			}
		}
		try {
			try {
				Confirmation[] confirmations = sga.get(user.getUsername()).FetchConfirmations();
				for(Confirmation confirmation : confirmations) {
					confirmation.setUser(user);
				}
				return confirmations;
			} catch(Exception e) {
				e.printStackTrace();
			}
		} catch(Throwable throwable) {
			throwable.printStackTrace();
		}
		return null;
	}
	
	public HashMap<String, SteamGuardAccount> getSga() {
		return sga;
	}
	
	public HashMap<String, SessionData> getSession() {
		return session;
	}
	
	public void doLogin(SteamUser user) throws Throwable {
		secretSGFile = new File(launcher.folderPath + "Users/SteamGuard_" + user.getUsername() + ".json");
		if(secretSGFile.exists() && !secretSGFile.isDirectory()&& !sga.containsKey(user.getUsername())) {
			sga.put(user.getUsername(), new SteamGuardAccount(JsonHelper.Deserialize(SteamGuardAccount.Config.class, secretSGFile)));
		}
		
		LoginRequest req = new LoginRequest();
		req.setUsername(user.getUsername());
		req.setPassword(user.getPassword());
		req.setRequires2FA(true);
		req.setTwoFactorCode(sga.get(user.getUsername()).GenerateSteamGuardCode());
		LoginResult res = loginSvc.DoLogin(req);
		while(res.getState().equals(LoginResultState.NEED_CAPTCHA) || res.getState().equals(LoginResultState.NEED_2FA)) {
			System.out.println(res.getState().name());
			if(res.getState().equals(LoginResultState.NEED_CAPTCHA)) {
				System.out.println("Enter CAPTCHA from:");
				System.out.println("https://steamcommunity.com/public/captcha.php?gid=" + res.getCaptchaGID());
				
				Scanner scanner = new Scanner(System.in);
				req.setRequiresCaptcha(true);
				req.setCaptchaGID(res.getCaptchaGID());
				req.setCaptchaText(scanner.nextLine());
				scanner.close();
				res = loginSvc.DoLogin(req);
			}
			if(res.getState().equals(LoginResultState.NEED_2FA)) {
				req.setRequires2FA(true);
				req.setTwoFactorCode(sga.get(user.getUsername()).GenerateSteamGuardCode());
				res = loginSvc.DoLogin(req);
			}
		}
		if(!res.getState().equals(LoginResultState.LOGIN_OK)) {
			System.out.println("[Steam] Login failed for user "+user.getUsername()+": "+res.getState().name());
			throw new Exception("Error during login!");
		}
		session.put(user.getUsername(), res.getSession());
		sga.get(user.getUsername()).setSession(session.get(user.getUsername()));
		
	}
	
	public static void main(String[] args) {
	
	}
	
}
