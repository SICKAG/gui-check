gui-check
========

The gui-check library enables developers to write **automated user interface tests for Java programs**. **JavaFX** and **Swing** are currently supported. Tests are written in **pure Java** and use the popular **JUnit** framework. All Java IDEs have some support for JUnit tests which makes the creation, execution and debugging of gui-check tests very easy.  
Having the tests in the development environment right beside the product code increases the acceptance of automated user interface tests among developers. Additionally, gui-check tests may be executed on an integration test server.

##Requirements##
Java 7

JUnit 4 or higher

##First steps##
For the first steps we need a very simple JavaFX application showing a stage with a button we can press.
```java
package de.sick.guicheck;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;

public class FirstFXApp extends Application
{
    @Override
    public void start(Stage stage) throws Exception
    {
        stage.setTitle("My first FX test");
        
        // It's necessary to set an ID for the button in order to find it in the test
        final Button l_node = new Button("Press me!");
        l_node.setId("press-me-button");
        
        stage.setScene(new Scene(l_node));
        stage.show();
    }

    public static void main(String[] args)
    {
        launch(args);
    }
}
```
Our first test does the following:
- starts the application above
- retrieves the stage by title
- checks the title
- finds the button by ID
- moves the mouse to the center of the button
- and clicks the mouse
```java
package de.sick.guicheck;

import org.junit.BeforeClass;
import org.junit.Test;

import de.sick.guicheck.fx.GcStageFX;
import de.sick.guicheck.fx.GcTestFX;

public class FirstFXTest extends GcTestFX
{
    @BeforeClass
    public static void setUpBeforeClass()
    {
        startApp("de.sick.guicheck.FirstFXApp");
    }
    
    @Test
    public void test() throws Exception
    {
        // Find the stage via title start
        GcStageFX l_stage = stage("My first");
        // Check the title string meets our expectation
        l_stage.propertyIs("title", "My first FX test");
        // Find the button node via selector and press it
        l_stage.robot().mouseMoveToCenter("#press-me-button").mouseClick();
    }
}
```
