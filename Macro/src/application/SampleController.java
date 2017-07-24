package application;

import java.net.URL;
import java.util.ResourceBundle;

import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NotFoundException;
import org.openqa.selenium.Point;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class SampleController implements Initializable {
	@FXML private TextField adminId;
	@FXML private TextField adminPw;
	@FXML private TextField otherId;
	@FXML private TextField otherPw;
	@FXML private TextField copyMax;
	@FXML private TextField copyMin;
	@FXML private Button run;
	@FXML private Button stop;
	@FXML private Button exit;
	@FXML private Button login;
	private int current;
	private WebDriver adminDriver;
	private WebDriver otherDriver;
	private Dimension windowSize;
	private boolean running = true;
	private final String postFront = "http://alltoto99.com/Service/Board/View.asp?BF_IDX=";	//IDX + postNumber
	private final String postBack = "&page=1&db=freeboard";			//page = n --> n is reply page number
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		System.setProperty("webdriver.chrome.driver", "src/chromedriver.exe");
		loadAdmin();
//		setAdminURL();
		loadOther();
		setOtherURL();
		binding();
		run.setDisable(true);
		stop.setDisable(true);
	}

	public void binding() {
		bindRun();
		bindStop();
		bindExit();
		bindLogin();
	}
 
	public void bindRun() {
		run.setOnAction(e-> {
			try {
				start(e);
			} catch (InterruptedException ex) {
				ex.printStackTrace();
			}
		});
	}
	public void bindStop() {
		stop.setOnAction(e->{
			try {
				stop(e);
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		});
	}
	public void bindExit() {
		exit.setOnAction(e->{
				try {
					exit(e);
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
		});
	}
	public void bindLogin() {
		login.setOnAction(e->{
			try {
				login(e);
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		});
	}
	
	// to prevent blocking UI while run use Threads
	public void start(ActionEvent e) throws InterruptedException {
		running = true;
			Thread thread = new Thread() {
				@Override
				public void run() {
					int min = Integer.parseInt(copyMin.getText());
					int max = Integer.parseInt(copyMax.getText());
					for(current=min; current <=max && running; current++) {
						movePage(current);
						try {
						System.out.println("글 번호 : " + current);
						System.out.println("제목 : " + getTitle());
						System.out.println("레벨 : " + getLevel());
						System.out.println("내용 : " + getContent());
						} catch (NotFoundException e) {
							System.out.println("패스!!!");
							continue;
						}
					}
				}
			};
			thread.start();
	}
	public void stop(ActionEvent e) throws InterruptedException {
		running = false;
	}
	public void exit(ActionEvent e) throws InterruptedException {
		adminDriver.quit();
		otherDriver.quit();
		Stage stage = (Stage) exit.getScene().getWindow();
		stage.close();
	}
	public void login(ActionEvent e) throws InterruptedException {
		if(!adminIsNull() && !otherIsNull()) {
//			adminLogin();
			otherLogin();
			run.setDisable(false);
			stop.setDisable(false);
			getLastNum();
		} else {
			loginfailAlert();
		}
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
		WebElement loginBtn = 
				adminDriver.findElement(By.cssSelector("#login-wrap > form > button"));
		try {
			loginBtn.click();
		} catch (Exception e) {
			scrollIntoView(adminDriver, loginBtn);
			loginBtn.click();
		}
	}
	public void otherLogin() {
		otherDriver.findElement(By.cssSelector("#IU_ID")).sendKeys(otherId.getText());
		otherDriver.findElement(By.cssSelector("#IU_PW")).sendKeys(otherPw.getText());
		WebElement loginBtn = 
				otherDriver.findElement(By.cssSelector("body > form > div > div.btnLogin > span > input"));
		try {
			loginBtn.click();
		} catch (Exception e) {
			scrollIntoView(otherDriver, loginBtn);
			loginBtn.click();
		}
	}
	
	public void setAdminURL() {
		adminDriver.get("http://www.815asiabet.com/admin/index.php");
//		adminDriver.get("https://www.naver.com");
	}
	public void setOtherURL() {
		otherDriver.get("http://alltoto99.com/Login.asp");
	}
	public void movePage(int postNum) {
		otherDriver.get(postFront+postNum+postBack);
	}
	
	public void getLastNum() {
		otherDriver.get("http://alltoto99.com/Service/Board/List.asp?db=freeboard");
		String max =
				otherDriver.findElement(By.cssSelector("#IHBoard > div.IHBoardList > div.totalcon > "
						+ "div > ul > li:nth-child(14) > div > ul > li.titlebox_s_01 > div")).getText();
		copyMax.setText(max);
	}
	public Object runScript(WebDriver driver, String script, WebElement target) {
		/*
		 * to run script shorter
		*/
		return ((JavascriptExecutor) driver).executeScript(script, target);
	}
	public void scrollIntoView(WebDriver driver, WebElement element) {
	runScript(driver, "arguments[0].scrollIntoView(true)", element);
	}
	public boolean adminIsNull() {
		if(adminId.getText().isEmpty() || adminPw.getText().isEmpty())
			return true;
		else
			return false;
	}
	public boolean otherIsNull() {
		if(otherId.getText().isEmpty() || otherPw.getText().isEmpty())
			return true;
		else
			return false;
	}
	public void loginfailAlert() {
		Alert alert = new Alert(AlertType.ERROR);
		alert.setTitle("로그인 실패");
		alert.setHeaderText("id 또는 pw가 비어있습니다.");
		alert.setContentText("두 탭의 id, pw를 모두 채워주십시오.");
		alert.showAndWait().ifPresent(rs -> {
		    if (rs == ButtonType.OK) {
		        System.out.println("Pressed OK.");
		    }
		});
	}
	
	public String getTitle() {
		WebElement title = otherDriver.findElement(By.cssSelector("#IHBoard > div > div.bbsViewTitle > h4"));
		return title.getText();
	}
	public int getLevel() {
		WebElement levelImg = otherDriver.findElement(By.cssSelector(".grade"));
		scrollIntoView(otherDriver, levelImg);
		String src =  levelImg.getAttribute("src");
		String level = src.split("level_")[1];
		String level2 = level.substring(1, 2);
		return Integer.parseInt(level2);
	}
	public String getContent() {
		WebElement content = otherDriver.findElement(By.xpath("//*[@id=\"IHBoard\"]/div/div[2]/div[2]"));
		return content.getText();
	}
}
