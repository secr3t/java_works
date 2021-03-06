package application;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.ResourceBundle;
import java.util.concurrent.LinkedBlockingQueue;

import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.ElementNotSelectableException;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NotFoundException;
import org.openqa.selenium.Point;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.Select;

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
	private int current, min, max, index;
	private WebDriver adminDriver;
	private WebDriver otherDriver;
	private Dimension windowSize;
	private final String postFront = "http://mhtt99.com/Service/Board/View.asp?BF_IDX=";	//IDX + postNumber
	private final String postBack = "&page=1&db=freeboard";			//page = n --> n is reply page number
	private org.openqa.selenium.Alert alert;
//	private SynchronousQueue<Writer> writeQueue = new SynchronousQueue<>();
	private LinkedBlockingQueue<Writer> writeQueue = new LinkedBlockingQueue<>();
	private static boolean flag = true;
	private static boolean writeFlag = false;
	private BufferedReader reader;
	private FileReader fileReader;
	private ArrayList<String> list1 = new ArrayList<>();
	private ArrayList<String> list2 = new ArrayList<>();
	private ArrayList<String> list3 = new ArrayList<>();
	private ArrayList<String> list4 = new ArrayList<>();
	private ArrayList<String> list5 = new ArrayList<>();
	
	public void suspending() {
		flag = false;
		System.out.println("플래그 상태" + flag);
	}
	public void resuming() {
		flag = true;
		System.out.println("플래그 상태" +flag);
	}
	gettingThread t1 = new gettingThread();
	settingThread t2 = new settingThread();
	
	class gettingThread extends Thread {
		@Override
		public void run() {
			while(true) {
				System.out.println("실행상태" + flag);
				while(current <= max) {
					movePage(current);
					try {
					writeQueue.put(getDetail());
					} catch (NotFoundException | InterruptedException e) {
						System.out.println("글이 없음");
					}
					current++;
					if(current >max)
						writeFlag = true;
				} // 게시판 글 긁어오기 완료.
			}
		}
	}
	
	class settingThread extends Thread{
		@Override
		public void run() {
			while(true) {
				System.out.println("실행상태" + flag);
				Writer writer = new Writer();
				if(writeFlag) {
					index = writeQueue.size();
					for(int i = 0; i < index; i++ ) {
							try {
								writer = writeQueue.take();
							} catch (InterruptedException e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();
							}
						writeMain(writer);
						try {
							writeComment(writer);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
					writeFlag = false;
				}
			}
		}
/*		public void run() {
			while(true) {
				System.out.println("실행상태" + flag);
				while(flag) {
					Writer writer = writeQueue.poll();
					if(writer == null)
						continue;
					else {
						writeMain(writer);
						try {
							writeComment(writer);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
				}
			}
		}
*/		
	}
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {
	/*	try {
			parsing();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
		System.setProperty("webdriver.chrome.driver", "src/chromedriver.exe");
		loadAdmin();
		setAdminURL();
		loadOther();
		setOtherURL();
		binding();
		run.setDisable(true);
		stop.setDisable(true);
		System.out.println(list1);
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
				e1.printStackTrace();
			}
		});
	}
	public void bindExit() {
		exit.setOnAction(e->{
				try {
					exit(e);
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
		});
	}
	public void bindLogin() {
		login.setOnAction(e->{
			try {
				login(e);
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
		});
	}
	
	// to prevent blocking UI while run use Threads
	public void start(ActionEvent e) throws InterruptedException {

		min = Integer.parseInt(copyMin.getText());
		max = Integer.parseInt(copyMax.getText());
		current = min;
		if(!t1.isAlive() && !t2.isAlive()) {
			t1.start();
			t2.start();
		}
		else
		{
			resuming();
		}
//			Thread thread3 = new Thread() {
//				int index = 0;
//				public void run() {
//					while(true) {
//						if(index < writes.size()) {
//							Writer w;
//							w = writes.get(index);
//							String value = ta.getText();
//							value = current + " 번 " + w.toString()+"\n" + value;
//							ta.setText(value);
//							index++;
//						}
//						if(!running)
//							this.stop();
//					}
//				}
//			};
//			thread3.start();
	}
	public void stop(ActionEvent e) throws InterruptedException {
		suspending();
		writeFlag=false;
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
	}
	public void setOtherURL() {
		otherDriver.get("http://mhtt99.com/Login.asp");
	}
	public void movePage(int postNum) {
		otherDriver.get(postFront+postNum+postBack);
	}
	
	public void getLastNum() {
		otherDriver.get("http://mhtt99.com/Service/Board/List.asp?db=freeboard");
		String max = "00";
		for(int i = 1; i < 30; i++) {
		String temp = otherDriver.findElement(By.cssSelector("#IHBoard > div.IHBoardList > div.totalcon > "
						+ "div > ul > li:nth-child("+i+") > div > ul > li.titlebox_s_01 > div")).getText();
		try{
			Integer.parseInt(temp);
			} catch (NumberFormatException e) {
				continue;
			}
			max = temp;
			break;
		}
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
			Thread.sleep(200);
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
			Thread.sleep(200);
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
		Select option = new Select(select);
		
/*		String value = new String();
		switch(level) {
		case 1: value = list1.get(rand.nextInt(list1.size()));
		case 2: value = list2.get(rand.nextInt(list2.size()));
		case 3: value = list3.get(rand.nextInt(list3.size()));
		case 4: value = list4.get(rand.nextInt(list4.size()));
		case 5: value = list5.get(rand.nextInt(list5.size()));
		}*/
		try {
//				option.selectByValue(value);
				option.selectByIndex(rand.nextInt(option.getOptions().size()-1)+1);
		}catch (ElementNotSelectableException e) {
			scrollIntoView(adminDriver, select);
//			option.selectByValue(value);
			option.selectByIndex(rand.nextInt(option.getOptions().size()-1)+1);
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
	public void parsing() throws IOException {
		fileReader = new FileReader("src/list.csv");
		reader = new BufferedReader(fileReader);
		String line;
		while((line = reader.readLine()) != null) {
			String val = line.split(",")[0];
			String level = line.split(",")[1];
			switch (level) {
			case "1" : list1.add(val);
			case "2" : list2.add(val);
			case "3" : list3.add(val);
			case "4" : list4.add(val);
			case "5" : list5.add(val);
			}
		}
	}
}
