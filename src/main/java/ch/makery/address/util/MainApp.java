/*jadclipse*/// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.

package ch.makery.address.util;

import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import ch.makery.address.MyMainApp;
import ch.makery.address.anotation.DefaultPackage;
import ch.makery.address.anotation.DefaultView;
import ch.makery.address.view.RootController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;


/**
 * 定义了openDialog  showFragment方法 定义controller属性
 * 约定根文件fxml文件名为RootLayout.fxml
 * 需要自定义app指定defaultview 不然报错
 */
@Data
@Slf4j
public abstract class MainApp extends Application
{
    public String fxPackage ;
    public String DefaultView ;
    static String title="defaulTitle";
    static String frame="RootLayout.fxml";
    FXMLLoader loader = new FXMLLoader();
	public Controller controller=null;
    public RootController rootController=null;
    HashMap<String,Pane> frams=new HashMap<>();
    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        initRootLayout();
        showFragment(DefaultView);
    }
    public MainApp() {
        Thread.setDefaultUncaughtExceptionHandler((t, e) -> {
            DialogUtils.handleError(t,e);
        });
        DefaultView annotation =
                MyMainApp.class.getAnnotation(DefaultView.class);
        if(annotation==null){
            throw new RuntimeException("DefaultView not set!!");
        }
        DefaultView =  annotation.value();
        DefaultPackage defaultPackage =
                MyMainApp.class.getAnnotation(DefaultPackage.class);
        fxPackage=defaultPackage.value();
    }


    public  void initRootLayout(){
        try {
            String s = fxPackage+"/RootLayout.fxml";
            loader.setLocation(this.getClass().getResource(s));
            rootLayout = (BorderPane) loader.load();
            rootController=loader.getController();
            rootController.setMainApp(this);
            rootController.initController();
            Scene scene = new Scene(rootLayout);
            primaryStage.setScene(scene);
            primaryStage.setTitle(title);
            primaryStage.show();
            primaryStage.setOnCloseRequest(arg0 -> System.out.println("close"));
        }catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     *  显示一个对话框 简单的可以不需要fxml所以不怎么使用
     *
     * @param fxmlPath
     * @return
     */
    public DialogController openDialogForResult(String title, String fxmlPath)
    {
        try
        {
            Stage dialogStage=new Stage();
            dialogStage.setTitle(title);
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource(fxPackage+"/"+fxmlPath));
            Pane page = (Pane)loader.load();
            dialogStage.initModality(Modality.NONE);
            dialogStage.initOwner(primaryStage);
            Scene scene = new Scene(page);
            dialogStage.setScene(scene);
            DialogController controller = (DialogController)loader.getController();
            controller.setMainApp(this);
            controller.setDialogStage(dialogStage);
            controller.initController();
            dialogStage.showAndWait();
            return controller;
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }
        return null;
    }

    /**
     *  显示一个对象的编辑框
     * @param title
     * @param fxmlPath
     * @param obj
     * @return
     */
    public DialogController openEditDialogForResult(String title, String fxmlPath,Object obj)
    {
        try
        {
            Stage dialogStage=new Stage();
            dialogStage.setTitle(title);
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource(fxPackage+"/"+fxmlPath));
            Pane page = (Pane)loader.load();
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(primaryStage);
            Scene scene = new Scene(page);
            dialogStage.setScene(scene);
            EditDialogController controller = (EditDialogController)loader.getController();
            controller.setMainApp(this);
            controller.setDialogStage(dialogStage);
            controller.setObject(obj);
            controller.initController();
            dialogStage.showAndWait();
            return controller;
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }
        return null;
    }


    public Controller showFragment(String fxmlPath)
    {
        try
        {
            Pane personOverview;
            if(frams.containsKey(fxmlPath)){
                personOverview=frams.get(fxmlPath);
            }else {
                FXMLLoader loader = new FXMLLoader(getClass().getResource(fxPackage+"/"+fxmlPath));
                 personOverview = (Pane)loader.load();

                controller = (Controller)loader.getController();
                controller.setMainApp(this);
                controller.initController();
                frams.put(fxmlPath,personOverview);
            }
            rootLayout.setCenter(personOverview);
            rootLayout.autosize();
            primaryStage.sizeToScene();
            return controller;
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }
        return null;
    }

    public Stage primaryStage;
    public BorderPane rootLayout;
}


/*
	DECOMPILATION REPORT

	Decompiled from: C:\Workspaces\com.connor.unv.plm2\lib\MyFXUtil.jar
	Total time: 14 ms
	Jad reported messages/errors:
	Exit status: 0
	Caught exceptions:
*/
/**
 根据scanpacage确定fx的包
 扫到controller view等注解进行内存初始化
 mainapp启动项目
 */