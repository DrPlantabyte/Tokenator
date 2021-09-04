package net.plantabyte.tokenator;

import javafx.beans.value.*;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.EventType;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.scene.control.*;
import javafx.scene.image.*;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.*;

import javax.imageio.ImageIO;
import java.io.File;
import java.net.URL;
import java.nio.file.Paths;
import java.util.*;

public class MainViewController {
	private FileChooser fileChooser;
	private Optional<Image> currentImage = Optional.empty();
	private Point2D tokenPoint = new Point2D(0, 0);
	private double zoom = 1;
	private Image hoverPreviewImg;
	private final Image emptyImg = new WritableImage(1,1);
	
	@FXML private ImageView fullImage;
	@FXML private ImageView borderImage;
	@FXML private ComboBox<TokenBorder> borderSelector;
	@FXML private Spinner<Integer> sizeSpinner;
	@FXML private ColorPicker colorPicker;
	@FXML private Pane previewPane;
	@FXML private ImageView hoverPreview;
	@FXML private Slider zoomSlider;
	@FXML private Label zoomLabel;
	
	public MainViewController(){
		//
	}
	
	
	@FXML
	private void initialize() {
		fileChooser = new FileChooser();
		fileChooser.getExtensionFilters().addAll(
				new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.gif"),
				new FileChooser.ExtensionFilter("All files", "*.*")
		);
		borderSelector.getItems().addAll(Main.getAvailableBorders());
		borderSelector.getSelectionModel().select(0);
		previewPane.setBackground(new Background(new BackgroundFill(Color.BLACK, new CornerRadii(5), new Insets(0,0,0,0))));
		hoverPreview.setPickOnBounds(false);
		hoverPreview.setMouseTransparent(true);
		
		var changeListener = new InputChangeListener();
		var zoomChangeListener = new ZoomChangeListener();
		
		sizeSpinner.setEditable(true);
		sizeSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(16, 1024, 280));
		sizeSpinner.valueProperty().addListener(changeListener);
		colorPicker.valueProperty().addListener(changeListener);
		zoomSlider.valueProperty().addListener(zoomChangeListener);
		
		try {
			int size = Integer.parseInt(Main.getConfig("size", "280"));
			sizeSpinner.getValueFactory().setValue(size);
		} catch(Exception e){
			Main.showError(e);
		}
		
		loadImage(Main.getDefaultImage());
		updateTokenBorder();
		
	}
	
	private class InputChangeListener implements ChangeListener<Object> {
		
		@Override
		public void changed(
				final ObservableValue<?> observable, final Object oldValue,
				final Object newValue
		) {
			updateTokenBorder();
		}
	}
	private class ZoomChangeListener implements ChangeListener<Object> {
		
		@Override
		public void changed(
				final ObservableValue<?> observable, final Object oldValue,
				final Object newValue
		) {
			setZoom();
			updateTokenBorder();
		}
	}
	
	private void loadImage(final Image img) {
		fullImage.setImage(img);
		fullImage.setFitWidth(img.getWidth() * zoom);
		fullImage.setFitHeight(img.getHeight() * zoom);
		
	}
	@FXML private void pasteEvent(KeyEvent e){
//		System.out.println("paste event?");
//		System.out.println(e);
		if(e.isControlDown() && KeyCode.V.equals(e.getCode())){
			loadFromClipboard();
		} else {
//			System.out.println("no");
		}
	}
	@FXML private void loadFromClipboard(){
		System.out.println("image in clipboard?");
		final Image image = Clipboard.getSystemClipboard().getImage();
		if(image != null){
			System.out.println("yes");
			loadImage(image);
		} else {
			System.out.println("no");
		}
	}
	@FXML private void zoomScroll(ScrollEvent e){
		if(e.isControlDown()){
			var delta = e.getDeltaY();
			zoomSlider.setValue(Math.max(zoomSlider.getMin(), Math.min(zoomSlider.getMax(), zoomSlider.getValue() + delta)));
			setZoom();
		}
	}
	@FXML private void zoomIn(){
		zoomSlider.setValue(Math.min(zoomSlider.getMax(),zoomSlider.getValue() + 10));
		setZoom();
	}
	@FXML private void zoomOut(){
		zoomSlider.setValue(Math.max(zoomSlider.getMin(),zoomSlider.getValue() - 10));
		setZoom();
	}
	private void setZoom(){
		zoom = Math.max(1, zoomSlider.getValue())/100.;
		zoomLabel.setText((int)(100*zoom)+"%");
		var img = fullImage.getImage();
		fullImage.setFitWidth(img.getWidth() * zoom);
		fullImage.setFitHeight(img.getHeight() * zoom);
	}
	
	@FXML private void clickOnImage(MouseEvent e){
		tokenPoint = new Point2D(e.getX() / zoom, e.getY() / zoom);
		updateTokenBorder();
	}
	@FXML private void hoverOnImage(MouseEvent e){
//		System.out.println(e.getX()+","+e.getY());
		hoverPreview.setX(e.getX() - 0.5*hoverPreviewImg.getWidth());
		hoverPreview.setY(e.getY() - 0.5*hoverPreviewImg.getHeight());
//		hoverPreview.setX(100);
//		hoverPreview.setY(100);
	}
	@FXML private void hoverEnter(){
		hoverPreview.setImage(hoverPreviewImg);
		hoverPreview.setFitWidth(hoverPreviewImg.getWidth());
		hoverPreview.setFitHeight(hoverPreviewImg.getHeight());
	}
	@FXML private void hoverExit(){
		hoverPreview.setImage(emptyImg);
		hoverPreview.setFitWidth(emptyImg.getWidth());
		hoverPreview.setFitHeight(emptyImg.getHeight());
	}
	@FXML private void updateTokenBorder(){
		if(fullImage.getImage() == null) return; // not yet fully initialized
		var tb = borderSelector.getSelectionModel().getSelectedItem();
		var size = sizeSpinner.getValue();
		var color = colorPicker.getValue();
		Image tokenImg = tb.makeToken(fullImage.getImage(), tokenPoint, size / zoom, size, color);
		borderImage.setImage(tokenImg);
		borderImage.setFitWidth(tokenImg.getWidth());
		borderImage.setFitHeight(tokenImg.getHeight());
		hoverPreviewImg = tb.makeOverlay(size, color);
		Main.setConfig("size", String.valueOf(size));
	}
	
	@FXML
	private void quit(){
		Main.quit();
	}
	
	@FXML
	private void open(){
		final File file = fileChooser.showOpenDialog(Main.getWindow());
		if (file != null){
			try{
				var img = new Image(file.toURI().toURL().toString());
				currentImage = Optional.of(img);
				fullImage.setImage(img);
			} catch (Exception ex){
				Main.showError(ex);
			}
		}
	}
	
	@FXML private void saveToken(){
		File file = fileChooser.showSaveDialog(Main.getWindow());
		if (file != null){
			try{
				Main.writeImageToFile(borderImage.getImage(), file.toPath(), true);
			} catch (Exception ex){
				Main.showError(ex);
			}
		}
	}
	
}
