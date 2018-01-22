package com.sundy.ta.analysis.utils;

import java.io.File;
import java.io.IOException;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriverService;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BrowserUtil {

	private Logger logger = LoggerFactory.getLogger(getClass());
	
	private static ChromeDriverService service;
	private static WebDriver driver;
	
	static {
		start();
	}

	private static void start() {
		try {
			service = new ChromeDriverService.Builder()
				    .usingDriverExecutable(new File("E:/share/tools/chromedriver_win32/chromedriver.exe"))
				    .usingAnyFreePort()
				    .build();
			service.start();
			driver = new RemoteWebDriver(service.getUrl(),DesiredCapabilities.chrome());
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}
	
	public static void close(){
		service.stop();
	}
	
	public static void restart(){
		if(service!=null) {
			service.stop();
		}
		if(driver!=null) {
			driver.quit();
		}
		start();
	}
	
	public static String getPageSource(String url){
		 try {
			driver.get(url);
			String page = driver.getPageSource();
			return page;
		} catch (Throwable e) {
			e.printStackTrace();
		}
		return null;
	}
	
}
