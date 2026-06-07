package com.itbenevides.genesys21.web;

import io.appium.java_client.AppiumBy;
import io.appium.java_client.android.AndroidDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.net.URL;
import java.time.Duration;
import java.util.Set;

public class OrderWebFlowTest {

    private AndroidDriver driver;

    @BeforeClass
    public void setUp() throws Exception {
        DesiredCapabilities caps = new DesiredCapabilities();
        caps.setCapability("deviceName", "Pixel_4_API_34");
        caps.setCapability("platformName", "Android");
        caps.setCapability("automationName", "UiAutomator2");
        caps.setCapability("appPackage", "com.itbenevides.genesys21");
        caps.setCapability("appActivity", ".MainActivity");
        caps.setCapability("noReset", true);
        caps.setCapability("autoWebview", true);
        // Optional: use system chromedriver if you have a specific version installed
        // caps.setCapability("chromedriverUseSystemExecutable", true);
        caps.setCapability("chromeOptions", new java.util.HashMap<String, Object>() {{
            put("w3c", true);
        }});
        driver = new AndroidDriver(new URL("http://localhost:4723/wd/hub"), caps);
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
    }

    @Test
    public void webViewOrderFlow() {
        // Abrir a tela que contém o WebView (ex.: botão "Abrir Loja")
        driver.findElement(AppiumBy.androidUIAutomator("new UiSelector().text(\"Abrir Loja\")")).click();

        // Trocar para o contexto WebView
        Set<String> contexts = driver.getContextHandles();
        System.out.println("Contexts found: " + contexts);
        driver.context(contexts.stream()
                .filter(c -> c.toLowerCase().startsWith("webview"))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("WEBVIEW context not found")));

        // Interagir com a página web
        driver.findElement(org.openqa.selenium.By.cssSelector("input[name='orderId']"))
                .sendKeys("12345");
        driver.findElement(org.openqa.selenium.By.cssSelector("button.search"))
                .click();

        // Verificar status
        WebElement status = driver.findElement(
                org.openqa.selenium.By.xpath("//div[contains(@class,'status')]")
        );
        assert status.getText().contains("Em preparação");

        // Capturar screenshot
        driver.getScreenshotAs(org.openqa.selenium.OutputType.FILE)
                .renameTo(new java.io.File("order_status_web.png"));
    }

    @Test
    public void invalidOrderFlow() {
        // Assumindo que o WebView já está aberto e contexto definido
        driver.findElement(AppiumBy.androidUIAutomator("new UiSelector().text(\"Abrir Loja\")")).click();
        driver.context(driver.getContextHandles().stream()
                .filter(c -> c.toLowerCase().startsWith("webview"))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("WEBVIEW context not found")));
        driver.findElement(org.openqa.selenium.By.cssSelector("input[name='orderId']"))
                .sendKeys("00000");
        driver.findElement(org.openqa.selenium.By.cssSelector("button.search"))
                .click();
        WebElement error = driver.findElement(org.openqa.selenium.By.xpath("//*[contains(text(),'Pedido não encontrado')]");
        assert error != null && error.isDisplayed();
        driver.getScreenshotAs(org.openqa.selenium.OutputType.FILE)
                .renameTo(new java.io.File("invalid_order_web.png"));
    }

    @Test
    public void emptyFieldFlow() {
        driver.findElement(AppiumBy.androidUIAutomator("new UiSelector().text(\"Abrir Loja\")")).click();
        driver.context(driver.getContextHandles().stream()
                .filter(c -> c.toLowerCase().startsWith("webview"))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("WEBVIEW context not found")));
        // Leave input empty
        driver.findElement(org.openqa.selenium.By.cssSelector("button.search"))
                .click();
        WebElement error = driver.findElement(org.openqa.selenium.By.xpath("//*[contains(text(),'Informe o número do pedido')]");
        assert error != null && error.isDisplayed();
        driver.getScreenshotAs(org.openqa.selenium.OutputType.FILE)
                .renameTo(new java.io.File("empty_field_web.png"));
    }
