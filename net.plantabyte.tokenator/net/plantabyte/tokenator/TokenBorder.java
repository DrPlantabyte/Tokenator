package net.plantabyte.tokenator;

import javafx.geometry.Point2D;
import javafx.scene.SnapshotParameters;
import javafx.scene.effect.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;

public class TokenBorder {
	private final Image overlay;
	private final Image colorLayer;
	private final Image mask;
	final String name;
	
	private final SnapshotParameters snapParams;
	
	public TokenBorder(String name, Image colorLayer, Image overlay, Image mask) throws IllegalArgumentException{
		if(overlay.getWidth() != mask.getWidth() || overlay.getHeight() != mask.getHeight()){
			throw new IllegalArgumentException("Border image overlay and mask must have same dimenions");
		}
		this.colorLayer = colorLayer;
		this.name = name;
		this.mask = mask;
		this.overlay = overlay;
		snapParams = new SnapshotParameters();
		snapParams.setFill(Color.TRANSPARENT);
	}
	private Image colorize(Image src, Color color){
//		if(Color.WHITE.equals(color) || color.isOpaque() == false){
//			// white = no change
//			return src;
//		}
		var hue = color.getHue();
		var sat = color.getSaturation();
		var val = color.getBrightness();
//
//		ImageView imageView = new ImageView(src);
//		ColorAdjust colorShift = new ColorAdjust();
//		colorShift.setHue(hue/360.);
//		colorShift.setBrightness(val);
//		imageView.effectProperty().setValue(colorShift);

//		ImageView imageView = new ImageView(src);
//		ColorAdjust grayscale = new ColorAdjust();
//		grayscale.setSaturation(-1.0);
//
//		Blend colorize = new Blend(
//				BlendMode.MULTIPLY,
//				grayscale,
//				new ColorInput(
//						0,
//						0,
//						imageView.getImage().getWidth(),
//						imageView.getImage().getHeight(),
//						color
//				)
//		);
//		imageView.effectProperty().setValue(colorize);
//		Image colorized = imageView.snapshot(snapParams, null);
		
		int ww = (int)src.getWidth(); int hh = (int)src.getHeight();
		WritableImage finalImg = new WritableImage(ww, hh);
		var w = finalImg.getPixelWriter();
		var r = src.getPixelReader();
		for(int y = 0; y < hh; y++){ for(int x = 0; x < ww; x++){
			var sColor = r.getColor(x, y);
			var srchue = sColor.getHue();
			var srcsat = sColor.getSaturation();
			var srcval = sColor.getBrightness();
			var alpha = sColor.getOpacity();
			var oColor = Color.hsb(hue, sat * srcsat, 0.5*(val+srcval) , alpha);
			w.setColor(x, y, oColor);
		}}
		return finalImg;
	}
	private Image scaleImage(Image src, int newWidth, int newHeight){
		ImageView imageView = new ImageView(src);
		imageView.setFitWidth(newWidth);
		imageView.setFitHeight(newHeight);
		return imageView.snapshot(snapParams, null);
	}
	
	private double clamp(double x, double min, double max){
		return Math.min(Math.max(x, min), max);
	}
	public Image makeOverlay(double outputSize, Color color){
		
		// then scale the clip, mask, and overlay to match output size
		int outW = (int)(outputSize * overlay.getWidth() / overlay.getHeight());
		int outH = (int)(outputSize);
		Image sColor = colorize(scaleImage(colorLayer, outW, outH), color);
		Image sOverlay = scaleImage(overlay, outW, outH);
		
		// then finally put overlay on top
		var imgStack = new StackPane();
		imgStack.getChildren().addAll(new ImageView(sColor), new ImageView(sOverlay));
		return imgStack.snapshot(snapParams, null);
	}
	public Image makeToken(Image srcImage, Point2D srcPos, double srcSize, double outputSize, Color color){
		// first, clip a sample from the source image
		if(srcSize > Math.min(srcImage.getWidth(), srcImage.getHeight())){
			srcSize = Math.min(srcImage.getWidth(), srcImage.getHeight());
		}
		int srcW = (int)srcSize; int srcH = (int)srcSize;
		srcPos = new Point2D(
				clamp(srcPos.getX(), 0.5*srcSize, srcImage.getWidth()-0.5*srcSize),
				clamp(srcPos.getY(), 0.5*srcSize, srcImage.getHeight()-0.5*srcSize)
		);
		WritableImage clip = new WritableImage(srcW, srcH);
		clip.getPixelWriter().setPixels(0, 0, srcW, srcH, srcImage.getPixelReader(),
				(int)(srcPos.getX()-0.5*srcSize), (int)(srcPos.getY()-0.5*srcSize));
		// then scale the clip, mask, and overlay to match output size
		int outW = (int)(outputSize * overlay.getWidth() / overlay.getHeight());
		int outH = (int)(outputSize);
		Image sClip = scaleImage(clip, outW, outH);
		Image sMask = scaleImage(mask, outW, outH);
		// then mask the scaled clip
		WritableImage imgOut = new WritableImage(outW, outH);
		var pxw = imgOut.getPixelWriter();
		var mpxr = sMask.getPixelReader();
		var spxr = sClip.getPixelReader();
		for(int y = 0; y < outH; y++){ for(int x = 0; x < outW; x++){
			int maskPx = mpxr.getArgb(x, y);
			int srcPx = spxr.getArgb(x, y);
			int outPx = (srcPx & 0x00FFFFFF) | (maskPx & 0xFF000000);
			pxw.setArgb(x, y, outPx);
		}}
		// then finally put overlay on top
		var imgStack = new StackPane();
		imgStack.getChildren().addAll(new ImageView(imgOut), new ImageView(makeOverlay(outputSize, color)));
		return imgStack.snapshot(snapParams, null);
		
	}
	
	@Override public String toString(){
		return name;
	}
}
