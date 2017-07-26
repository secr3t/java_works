package application;

//import java.io.File;
//import java.io.IOException;
import java.net.URL;
//import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.ResourceBundle;
import java.util.Vector;

import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.ElementNotSelectableException;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NotFoundException;
import org.openqa.selenium.Point;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;

//import com.google.common.io.Files;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class SampleController implements Initializable {
	@FXML private TextField adminId;
	@FXML private PasswordField adminPw;
	@FXML private TextField otherId;
	@FXML private PasswordField otherPw;
	@FXML private TextField copyMax;
	@FXML private TextField copyMin;
	@FXML private Button run;
	@FXML private Button stop;
	@FXML private Button exit;
	@FXML private Button login;
	@FXML private TextArea ta;
	private int current;
	private WebDriver adminDriver;
	private WebDriver otherDriver;
	private Dimension windowSize;
	private boolean running = true;
	private final String postFront = "http://alltoto99.com/Service/Board/View.asp?BF_IDX=";	//IDX + postNumber
	private final String postBack = "&page=1&db=freeboard";			//page = n --> n is reply page number
	private org.openqa.selenium.Alert alert;
	private Vector<Writer> writes = new Vector<>();
//	private final String title = "_subject.value";
//	private final String content = "_content.value";
//	private final String comment = "_comment.value";
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		System.setProperty("webdriver.chrome.driver", "src/chromedriver.exe");
		loadAdmin();
		setAdminURL();
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
						writes.add(getDetail());
						} catch (NotFoundException e) {
							System.out.println("글이 없음");
							continue;
						}
					} // 게시판 글 긁어오기 완료.
					
					if(!running)
						this.stop();
				}
			};
			thread.start();
			Thread thread2 = new Thread() {
				int index = 0;
				@Override
				public void run() {
					while(running) {
						if(index < writes.size()) {
							writeMain(writes.get(index));
							try {
								writeComment(writes.get(index));
							} catch (InterruptedException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							index++;
						}
					}
				}

			};
			thread2.start();
			Thread thread3 = new Thread() {
				int index = 0;
				public void run() {
					while(running) {
						if(index < writes.size()) {
							Writer w;
							w = writes.get(index);
							String value = ta.getText();
							value = current + " 번 " + w.toString()+"\n" + value;
							ta.setText(value);
							index++;
						}
					}
					this.stop();
				}
			};
			thread3.start();
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
			adminLogin();
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
//	public void writeMain(Writer w) throws IOException  {
//		adminDriver.get("http://www.815asiabet.com/admin/board_write.php?sitename=CityOfDream&tn=board");
//		String writingJs = Files.toString(new File("src/write.js"), Charset.forName("utf-8"));
//		writingJs = setTitle(writingJs, w.getTitle());
//		writingJs = setContent(writingJs, w.getContent());
//		setWriterLevel(w.getLevel());
//		((JavascriptExecutor) adminDriver).executeScript(writingJs);
//	}
	
	public void writeMain(Writer w) {
		adminDriver.get("http://www.815asiabet.com/admin/board_write.php?sitename=CityOfDream&tn=board");
		setTitle(w.getTitle());
		setWriterLevel(w.getLevel());
		setContent(w.getContent());
		WebElement submit = 
		adminDriver.findElement(By.cssSelector("#btn_submit"));
		try {
			submit.click();
		} catch (Exception ex) {
			scrollIntoView(adminDriver, submit);
			submit.click();
		}
		try {
		alert = adminDriver.switchTo().alert();
		alert.accept();
		} catch (Exception e) {
			System.out.println("alert창이 없음");
		}
		adminDriver.switchTo().defaultContent();
	}
	public void writeComment(Writer w) throws InterruptedException {
		ArrayList<Replier> r = w.getReps();
		for(int i = 0; i<r.size();i++) {
		WebElement topTopic = 
		adminDriver.findElement(By.cssSelector("#sub_content > div > table > tbody > tr:nth-child(18) > td:nth-child(2) > nobr > a"));
		scrollIntoView(adminDriver, topTopic);
		topTopic.click();
		WebElement comment = adminDriver.findElement(By.cssSelector("#comment"));
		scrollIntoView(adminDriver, comment);
		comment.clear();
		comment.sendKeys(r.get(i).getContent());
		Thread.sleep(200);
		String url = adminDriver.getCurrentUrl();
		String value = url.split("b_key=")[1];
		value = value.split("&")[0];
		((JavascriptExecutor) adminDriver).executeScript("Javascript:BoardReplyWrite(" + value +")");
		try {
			alert = adminDriver.switchTo().alert();
			alert.accept();
			} catch (Exception e) {
				System.out.println("alert창이 없음");
			}
			adminDriver.switchTo().defaultContent();
		}
	}
	public Writer getDetail() {
		Writer writer = new Writer();
		writer.setTitle(getTitle());
		writer.setLevel(parseLevel(getWriterLevel()));
		writer.setContent(getContent());
		
		try {
			List<WebElement> replyTables = otherDriver.switchTo().frame(otherDriver.findElement(By.cssSelector("#_iframeBoard")))
					.findElements(By.tagName("table"));
			int size = replyTables.size();
			for(int i = 0; i < size; i++) {
				Replier rep = new Replier();
				WebElement curr = replyTables.get(i);
				if(curr.equals(otherDriver.findElement(By.cssSelector("body > form > table"))))
						break;
				rep.setLevel(parseLevel(getReplyLevel(curr)));
				rep.setContent(curr.findElement(By.cssSelector("#_lblContent")).getText());
				writer.getReps().add(rep);
			}
		} catch (Exception e) {
			System.out.println(current);
			e.printStackTrace();
		}
		otherDriver.switchTo().defaultContent();
		return writer;
	}
	public String getTitle() {
		WebElement title = otherDriver.findElement(By.cssSelector("#IHBoard > div > div.bbsViewTitle > h4"));
		return title.getText();
	}
	public String getWriterLevel() {
		WebElement levelImg = otherDriver.findElement(By.cssSelector(".writer > .grade"));
		scrollIntoView(otherDriver, levelImg);
		return levelImg.getAttribute("src");
	}
	public String getReplyLevel(WebElement el) {
		WebElement levelImg = el.findElement(By.cssSelector(".table_04_ > img"));
		scrollIntoView(otherDriver, levelImg);
		return levelImg.getAttribute("src");
	}
	public int parseLevel(String src) {
		String level = src.split("level_")[1];
		String level2 = level.substring(1, 2);
		return Integer.parseInt(level2);
	}
	public String getContent() {
		WebElement content = otherDriver.findElement(By.xpath("//*[@id=\"IHBoard\"]/div/div[2]/div[2]"));
		return content.getText();
	}
	
	public void setWriterLevel(int level) {
		WebElement select = 
		adminDriver.findElement(By.cssSelector("#b_writer"));
		Random rand = new Random();
		WebElement option = 
				select.findElement(By.xpath("//option[contains(text(),'" + level +"_"+ (rand.nextInt(3)+1) + "')]"));
		try {
				option.click();
		}catch (ElementNotSelectableException e) {
			scrollIntoView(adminDriver, option);
			option.click();
		}
	}
	public void setTitle(String title) {
		try {
			adminDriver.findElement(By.cssSelector("#b_subject")).sendKeys(title);
		} catch (ElementNotSelectableException e) {
			scrollIntoView(adminDriver, adminDriver.findElement(By.cssSelector("#b_subject")));
			adminDriver.findElement(By.cssSelector("#b_subject")).sendKeys(title);
		}
	}
	public void setContent(String content) {
		adminDriver.switchTo().frame(adminDriver.findElement(By.cssSelector("#sub_content > "
				+ "div > table > tbody > tr:nth-child(7) > td:nth-child(2) > div.cleditorMain > iframe")));
		try {
			adminDriver.findElement(By.tagName("body")).sendKeys(content);
		} catch (ElementNotSelectableException e) {
			scrollIntoView(adminDriver, adminDriver.findElement(By.tagName("body")));
			adminDriver.findElement(By.tagName("body")).sendKeys(content);
		}
		adminDriver.switchTo().defaultContent();
	}
//	public String setTitle(String js, String replace) {
//		return js.replace(title, replace);
//	}
//	public String setContent(String js, String replace) {
//		return js.replace(content, replace);
//	}
}
