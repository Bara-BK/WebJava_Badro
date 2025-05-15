package tn.badro.images;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javax.imageio.ImageIO;
import javafx.embed.swing.SwingFXUtils;
import java.io.File;
import java.io.IOException;

/**
 * This utility class creates a translation icon and saves it as PNG
 */
public class TranslateIcon extends Application {

    @Override
    public void start(Stage primaryStage) {
        // Create a canvas to draw the icon
        Canvas canvas = new Canvas(24, 24);
        GraphicsContext gc = canvas.getGraphicsContext2D();
        
        // Set line properties
        gc.setStroke(Color.web("#307D91"));
        gc.setLineWidth(2);
        
        // Draw globe and language elements
        drawTranslationIcon(gc);
        
        // Save as PNG
        saveAsPng(canvas, "src/main/resources/tn/badro/images/translate_icon.png");
        
        // Show in a window for preview
        StackPane root = new StackPane(canvas);
        primaryStage.setScene(new Scene(root, 50, 50));
        primaryStage.setTitle("Translation Icon");
        primaryStage.show();
    }
    
    private void drawTranslationIcon(GraphicsContext gc) {
        // Draw horizontal line
        gc.strokeLine(5, 8, 19, 8);
        
        // Draw top arc
        gc.beginPath();
        gc.moveTo(7.3, 5);
        gc.lineTo(16.7, 5);
        gc.stroke();
        
        // Draw vertical line (the language selector indicator)
        gc.beginPath();
        gc.moveTo(12, 5);
        gc.lineTo(12, 22);
        gc.stroke();
        
        // Draw text line indicators
        gc.strokeLine(7, 15, 12, 15);
        gc.strokeLine(8, 11, 16, 11);
    }
    
    private void saveAsPng(Canvas canvas, String filePath) {
        WritableImage writableImage = canvas.snapshot(null, null);
        File file = new File(filePath);
        try {
            ImageIO.write(SwingFXUtils.fromFXImage(writableImage, null), "png", file);
            System.out.println("Icon saved to: " + file.getAbsolutePath());
        } catch (IOException e) {
            System.err.println("Failed to save icon: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    public static void main(String[] args) {
        launch(args);
    }
} 