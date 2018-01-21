package com.sundy.ta;

import java.io.File;
import java.io.IOException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriverService;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;


public class ChromeTest {

  private ChromeDriverService service;
  private WebDriver driver;


  @Test
  public void testGoogleSearch() {
	 try {
		service = new ChromeDriverService.Builder()
				    .usingDriverExecutable(new File("E:/share/tools/chromedriver_win32/chromedriver.exe"))
				    .usingAnyFreePort()
				    .build();
		service.start();
		driver = new RemoteWebDriver(service.getUrl(),DesiredCapabilities.chrome());
		  
		driver.get("http://www.baidu.com");
		WebElement searchBox = driver.findElement(By.id("kw"));
		searchBox.sendKeys("暗算");
		WebElement su = driver.findElement(By.id("su"));
		su.click();
		String page = driver.getPageSource();
		System.out.println(page);
	} catch (Throwable e) {
		e.printStackTrace();
	} finally {
		service.stop();
		driver.quit();
	}
  }
}