package net.plantabyte.tokenator;

import javafx.application.Application;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.stage.*;

import javax.imageio.ImageIO;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class Main extends Application {
	private Path appDir;
	private Path configFile;
	private static Main instance = null;
	private Stage mainStage;
	private List<TokenBorder> tokenBorders = new ArrayList<>();
	private Image defaultImg;
	private Map<String,String> config = new HashMap<>();
	public static void quit() {
		try {
			writeConfig();
		} catch(IOException e) {
			showError(e);
		}
		javafx.application.Platform.exit();
	}
	protected synchronized static String getConfig(String key, String defaultValue){
		if(instance.config.containsKey(key)){
			return instance.config.get(key);
		} else {
			return setConfig(key, defaultValue);
		}
	}
	protected synchronized static String setConfig(String key, String newValue){
		instance.config.put(key, newValue);
		return newValue;
	}
	private static void writeConfig() throws IOException{
		var p = new Properties();
		instance.config.forEach((String k, String v) -> p.setProperty(k, v));
		try(var w = Files.newBufferedWriter(instance.configFile, StandardCharsets.UTF_8)){
			p.store(w, "");
		}
	}
	protected static Window getWindow() {
		return instance.mainStage;
	}
	
	protected static Image getDefaultImage(){
		return instance.defaultImg;
	}
	
	protected static List<TokenBorder> getAvailableBorders(){
		return instance.tokenBorders;
	}
	
	protected static void showError(final Exception ex) {
		Button btn = new Button();
		btn.setText("Open Dialog");
		btn.setOnAction(
				new EventHandler<ActionEvent>() {
					@Override
					public void handle(ActionEvent event) {
						final Stage dialog = new Stage();
						dialog.initModality(Modality.APPLICATION_MODAL);
						dialog.initOwner(getWindow());
						VBox dialogVbox = new VBox(20);
						var detailedMessage = new TextArea(ex.toString());
						detailedMessage.setEditable(false);
						dialogVbox.getChildren().addAll(
								new Label("ERROR!"),
								new Label(ex.getLocalizedMessage()),
								detailedMessage
						);
						Scene dialogScene = new Scene(dialogVbox, 300, 200);
						dialog.setScene(dialogScene);
						dialog.show();
					}
				});
	}
	
	@Override
	public void init() throws Exception {
		instance = this;
		//System.getProperties().forEach((var k, var v)->System.out.println(k+" -> "+v));
		if(System.getProperty("os.name") != null && System.getProperty("os.name").toLowerCase(
				Locale.ENGLISH).startsWith("windows")){
			if(System.getProperty("user.home") != null){
				this.appDir = Paths.get(System.getProperty("user.home"), "AppData", "Roaming", "Tokenator");
			} else {
				this.appDir = Paths.get(".");
			}
		} else {
			if(System.getProperty("user.home") != null){
				this.appDir = Paths.get(System.getProperty("user.home"), ".Tokenator");
			} else {
				this.appDir = Paths.get(".");
			}
		}
		//
		configFile = appDir.resolve("tokenator.properties");
		if(!Files.exists(configFile)){
			Files.createDirectories(configFile.getParent());
			Files.writeString(configFile, "size=280");
		}
		var p = new Properties();
		try(var r = Files.newBufferedReader(configFile, StandardCharsets.UTF_8)) {
			p.load(r);
			p.forEach((Object k, Object v)->config.put(k.toString(), v.toString()));
		}
		//
		defaultImg = new Image(getClass().getResource("resources/starting_image.png").toString());
		// load token borders
		Path borderDir = Paths.get(appDir.toString(), "Token Borders");
		System.out.println("Place additional token borders in:");
		System.out.println(borderDir);
		if(!Files.isDirectory(borderDir)){
			Files.createDirectories(borderDir);
		}
		
		for(String example : Arrays.asList("Circle", "Hex", "Poker Chip")){
			try(var cin = Main.class.getResourceAsStream("resources/"+example+".color.png");
				 var cout = Files.newOutputStream(borderDir.resolve(example+".color.png"));
				 var min = Main.class.getResourceAsStream("resources/"+example+".mask.png");
				 var mout = Files.newOutputStream(borderDir.resolve(example+".mask.png"));
				 var oin = Main.class.getResourceAsStream("resources/"+example+".overlay.png");
				 var oout = Files.newOutputStream(borderDir.resolve(example+".overlay.png"))
			){
				writeTo(cin, cout);
				writeTo(min, mout);
				writeTo(oin, oout);
			}
		}
		
		var fileList = Files.list(borderDir).toList();
		for(Path f : fileList){
			String fname = f.getFileName().toString();
			if(fname.endsWith(".mask.png")){
				var parent = f.getParent();
				var maskName = fname;
				var overlayName = maskName.replace(".mask.png", ".overlay.png");
				var colorName = maskName.replace(".mask.png", ".color.png");
				var name = maskName.replace(".mask.png", "");
				if(Files.exists(parent.resolve(maskName))
				&& Files.exists(parent.resolve(overlayName))
				&& Files.exists(parent.resolve(colorName))){
					// found a token border
					var basicBorderColor = new Image(parent.resolve(colorName).toUri().toURL().toString());
					var basicBorderOverlay = new Image(parent.resolve(overlayName).toUri().toURL().toString());
					var basicBorderMask = new Image(parent.resolve(maskName).toUri().toURL().toString());
					tokenBorders.add(new TokenBorder(name, basicBorderColor, basicBorderOverlay, basicBorderMask));
				}
			}
		}
		
		
	}
	private static void writeTo(InputStream src, OutputStream dest)
			throws IOException {
		int b;
		while((b = src.read()) >= 0) dest.write(b);
	}
	public static void writeImageToFile(Image img, Path dest, boolean overwrite) throws
			IOException {
		String fname = dest.getFileName().toString();
		var i = fname.lastIndexOf(".");
		if(i < 0) {
			fname = fname+".png";
			dest = Paths.get(dest.getParent().toString(), fname);
			i = fname.lastIndexOf(".");
		}
		String type = fname.substring(i+1);
		if(!overwrite && Files.exists(dest)) return;
		ImageIO.write(SwingFXUtils.fromFXImage(img, null), type, dest.toFile());
	}
	
	@Override
	public void start(Stage primaryStage) throws Exception{
		mainStage = primaryStage;
		Parent root = FXMLLoader.load(Main.class.getResource(
				"resources/MainView.fxml"));
		primaryStage.setTitle("The Tokenator!");
		primaryStage.setScene(new Scene(root, 800, 600));
		primaryStage.show();
	}
	
	
	public static void main(String[] args) {
	launch(args);
	}
}
