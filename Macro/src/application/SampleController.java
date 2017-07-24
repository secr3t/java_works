package application;

import java.net.URL;
import java.util.ResourceBundle;

import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Point;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class SampleController implements Initializable {
	@FXML private TextField adminId;
	@FXML private TextField adminPw;
	@FXML private TextField otherId;
	@FXML private TextField otherPw;
	@FXML private Button run;
	@FXML private Button stop;
	@FXML private Button exit;
	public static WebDriver adminDriver;
	public static WebDriver otherDriver;
	public static Dimension windowSize;
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		System.setProperty("webdriver.chrome.driver", "src/chromedriver.exe");
		loadAdmin();
		setAdminURL();
		loadOther();
		setOtherURL();
		binding();
	}
	
	public void binding() {
		bindRun();
		bindStop();
		bindExit();
	}

	public void bindRun() {
		run.setOnAction(e-> {
			try {
				startBtn(e);
			} catch (InterruptedException ex) {
				ex.printStackTrace();
			}
		});
	}
	public void bindStop() {
//		stop.setOnAction();
	}
	public void bindExit() {
		exit.setOnAction(e->{
				try {
					exitBtn(e);
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
		});
	}
	public void startBtn(ActionEvent e) throws InterruptedException {
		if(!adminIdIsNull())
			adminLogin();
	}
	public void stopBtn(ActionEvent e) throws InterruptedException {
		
	}
	
	public void exitBtn(ActionEvent e) throws InterruptedException {
		adminDriver.quit();
		otherDriver.quit();
		Stage stage = (Stage) exit.getScene().getWindow();
		stage.close();
	}
	
	public void loadAdmin() {
		adminDriver = new ChromeDriver();
		adminDriver.manage().window().maximize();
		windowSize = adminDriver.manage().window().getSize();
		adminDriver.manage().window().setPosition(new Point(0, 0));   
		adminDriver.manage().window().setSize(new Dimension(windowSize.getWidth()/2, windowSize.getHeight()/2));
	}
	
	public void loadOther() {
		otherDriver = new ChromeDriver();
		otherDriver.manage().window().setPosition(new Point(windowSize.getWidth()/2,0));
		otherDriver.manage().window().setSize(new Dimension(windowSize.getWidth()/2, windowSize.getHeight()/2));
	}
	
	public void adminLogin() {
		adminDriver.findElement(By.cssSelector("#login_id")).sendKeys(adminId.getText());
		adminDriver.findElement(By.cssSelector("#login_pass")).sendKeys(adminPw.getText());
		try {
			adminDriver.findElement(By.cssSelector("#login-wrap > form > button")).click();
		} catch (Exception e) {
			runScript(adminDriver, "arguments[0].scrollIntoView(true)", adminDriver.findElement(By.cssSelector("#login-wrap > form > button")));
			adminDriver.findElement(By.cssSelector("#login-wrap > form > button")).click();
		}
//		adminDriver.findElement(By.cssSelector("#id")).sendKeys(adminId.getText());
//		adminDriver.findElement(By.cssSelector("#pw")).sendKeys(adminPw.getText());
//		adminDriver.findElement(By.cssSelector("#frmNIDLogin > fieldset > span > input[type=\"submit\"]")).click();
		
	}
	
	public void otherLogin() {
		
	}
	
	public void setAdminURL() {
		adminDriver.get("http://www.815asiabet.com/admin/index.php");
//		adminDriver.get("https://www.naver.com");
	}
	
	public void setOtherURL() {
		otherDriver.get("http://www.daum.net");
	}

	public boolean adminIdIsNull() {
		if(adminId.getText().trim() == null)
			return true;
		else
			return false;
	}
	public boolean adminPwIsNull() {
		if(adminPw.getText().trim() == null)
			return true;
		else
			return false;
	}
	public boolean otherIdIsNull() {
		if(otherId.getText().trim() == null)
			return true;
		else
			return false;
	}
	public boolean otherPwIsNull() {
		if(otherPw.getText().trim() == null)
			return true;
		else
			return false;
	}
	
	public void runScript(WebDriver driver, String script, WebElement target) {
		/*
		 * to run script shorter
		*/
		((JavascriptExecutor) driver).executeScript(script, target);
	}
	/*
	@Override
	public void finalize() {
		adminDriver.close();
		otherDriver.close();
	}
	*/
}
