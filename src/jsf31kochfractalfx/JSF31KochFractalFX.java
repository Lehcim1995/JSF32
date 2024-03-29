/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jsf31kochfractalfx;

import calculate.Edge;
import calculate.KochManager;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import static Threads.GenerateEdge.*;

/**
 * @author Nico Kuijpers
 */
public class JSF31KochFractalFX extends Application {

    // Zoom and drag
    private double zoomTranslateX = 0.0;
    private double zoomTranslateY = 0.0;
    private double zoom = 1.0;
    private double startPressedX = 0.0;
    private double startPressedY = 0.0;
    private double lastDragX = 0.0;
    private double lastDragY = 0.0;

    // Koch manager
    // TO DO: Create class KochManager in package calculate
    private KochManager kochManager;

    // Current level of Koch fractal
    private int currentLevel = 1;

    // Labels for level, nr edges, calculation time, and drawing time
    private Label labelLevel;
    private Label labelNrEdges;
    private Label labelNrEdgesText;
    private Label labelCalc;
    private Label labelCalcText;
    private Label labelDraw;
    private Label labelDrawText;
    private Label labelProgressLeft;
    private Label labelProgressLeftText;
    private Label labelProgressMiddle;
    private Label labelProgressMiddleText;
    private Label labelProgressRight;
    private Label labelProgressRightText;

    private ProgressBar progressBarLeft;
    private ProgressBar progressBarMiddle;
    private ProgressBar progressBarRight;

    // Koch panel and its size
    private Canvas kochPanel;
    private final int kpWidth = 500;
    private final int kpHeight = 500;

    private Stage stage;

    @Override
    public void start(Stage primaryStage) {
        EventHandler<WindowEvent> event = new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent event) {
                System.exit(0);
            }
        };

        primaryStage.setOnCloseRequest(event);

        stage = primaryStage;

        // Define grid pane      
        GridPane grid;
        grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(25, 25, 25, 25));

        // For debug purposes
        // Make de grid lines visible
        // grid.setGridLinesVisible(true);
        // Drawing panel for Koch fractal
        kochPanel = new Canvas(kpWidth, kpHeight);
        grid.add(kochPanel, 0, 3, 25, 1);

        // Labels to present number of edges for Koch fractal
        labelNrEdges = new Label("Nr edges:");
        labelNrEdgesText = new Label();
        grid.add(labelNrEdges, 0, 0, 4, 1);
        grid.add(labelNrEdgesText, 3, 0, 22, 1);

        // Labels to present time of calculation for Koch fractal
        labelCalc = new Label("Calculating:");
        labelCalcText = new Label();
        grid.add(labelCalc, 0, 1, 4, 1);
        grid.add(labelCalcText, 3, 1, 22, 1);

        // Labels to present time of drawing for Koch fractal
        labelDraw = new Label("Drawing:");
        labelDrawText = new Label();
        grid.add(labelDraw, 0, 2, 4, 1);
        grid.add(labelDrawText, 3, 2, 22, 1);

        // Label to present current level of Koch fractal
        labelLevel = new Label("Level: " + currentLevel);
        grid.add(labelLevel, 0, 6);

        // Button to increase level of Koch fractal
        Button buttonChangeFile = new Button();
        buttonChangeFile.setText("Load Level");
        buttonChangeFile.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                changeFileButtonActionPerformed(event);
            }
        });
        grid.add(buttonChangeFile, 3, 6);

        // Button to fit Koch fractal in Koch panel
        Button buttonFitFractal = new Button();
        buttonFitFractal.setText("Fit Fractal");
        buttonFitFractal.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                fitFractalButtonActionPerformed(event);
            }
        });
        grid.add(buttonFitFractal, 14, 6);

        labelProgressLeft = new Label("Progress left:");
        labelProgressLeftText = new Label();
        grid.add(labelProgressLeft, 0, 8, 4, 1);
        grid.add(labelProgressLeftText, 6, 8, 22, 1);

        progressBarLeft = new ProgressBar(0);
        grid.add(progressBarLeft, 5, 8);

        labelProgressMiddle = new Label("Progress middle:");
        labelProgressMiddleText = new Label();
        grid.add(labelProgressMiddle, 0, 9, 4, 1);
        grid.add(labelProgressMiddleText, 6, 9, 22, 1);

        progressBarMiddle = new ProgressBar(0);
        grid.add(progressBarMiddle, 5, 9);

        labelProgressRight = new Label("Progress right:");
        labelProgressRightText = new Label();
        grid.add(labelProgressRight, 0, 10, 4, 1);
        grid.add(labelProgressRightText, 6, 10, 22, 1);

        progressBarRight = new ProgressBar(0);
        grid.add(progressBarRight, 5, 10);

        // Add mouse clicked event to Koch panel
        kochPanel.addEventHandler(MouseEvent.MOUSE_CLICKED,
                new EventHandler<MouseEvent>() {
                    @Override
                    public void handle(MouseEvent event) {
                        kochPanelMouseClicked(event);
                    }
                });

        // Add mouse pressed event to Koch panel
        kochPanel.addEventHandler(MouseEvent.MOUSE_PRESSED,
                new EventHandler<MouseEvent>() {
                    @Override
                    public void handle(MouseEvent event) {
                        kochPanelMousePressed(event);
                    }
                });

        // Add mouse dragged event to Koch panel
        kochPanel.setOnMouseDragged(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                kochPanelMouseDragged(event);
            }
        });

        // Create Koch manager and set initial level
        resetZoom();
        kochManager = new KochManager(this);
        kochManager.changeLevel(currentLevel);

        // Create the scene and add the grid pane
        Group root = new Group();
        Scene scene = new Scene(root, kpWidth + 100, kpHeight + 275);
        root.getChildren().add(grid);

        // Define title and assign the scene for main window
        primaryStage.setTitle("Koch Fractal");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public void clearKochPanel() {
        GraphicsContext gc = kochPanel.getGraphicsContext2D();
        gc.clearRect(0.0, 0.0, kpWidth, kpHeight);
        gc.setFill(Color.BLACK);
        gc.fillRect(0.0, 0.0, kpWidth, kpHeight);
    }

    public void drawEdge(Edge e) {
        // Graphics
        GraphicsContext gc = kochPanel.getGraphicsContext2D();

        // Adjust edge for zoom and drag
        Edge e1 = edgeAfterZoomAndDrag(e);

        // Set line color
        gc.setStroke(e1.color);

        // Set line width depending on level
        if (currentLevel <= 3) {
            gc.setLineWidth(1.5);
        } else if (currentLevel <= 5) {
            gc.setLineWidth(1.0);
        } else {
            gc.setLineWidth(.5);
        }

        // Draw line
        gc.strokeLine(e1.X1, e1.Y1, e1.X2, e1.Y2);

    }

    public void setTextNrEdges(String text) {
        labelNrEdgesText.setText(text);
    }

    public void setTextCalc(String text) {
        labelCalcText.setText(text);
    }

    public void setTextDraw(String text) {
        labelDrawText.setText(text);
    }

    public void setTextLevel(String text) {
        labelLevel.setText(text);
    }

    public void requestDrawEdges() {
        synchronized (this) {
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    kochManager.drawEdges();
                }
            });
        }
    }

    private void changeFileButtonActionPerformed(ActionEvent event) {
        kochManager.changeLevel(0);
    }

    private void decreaseLevelButtonActionPerformed(ActionEvent event) {
        if (currentLevel > 1) {
            // resetZoom();
            currentLevel--;
            labelLevel.setText("Level: " + currentLevel);
            kochManager.changeLevel(currentLevel);
        }
    }

    private void fitFractalButtonActionPerformed(ActionEvent event) {
        resetZoom();
        kochManager.drawEdges();
    }

    private void kochPanelMouseClicked(MouseEvent event) {
        if (Math.abs(event.getX() - startPressedX) < 1.0
                && Math.abs(event.getY() - startPressedY) < 1.0) {
            double originalPointClickedX = (event.getX() - zoomTranslateX) / zoom;
            double originalPointClickedY = (event.getY() - zoomTranslateY) / zoom;
            if (event.getButton() == MouseButton.PRIMARY) {
                zoom *= 2.0;
            } else if (event.getButton() == MouseButton.SECONDARY) {
                zoom /= 2.0;
            }
            zoomTranslateX = (int) (event.getX() - originalPointClickedX * zoom);
            zoomTranslateY = (int) (event.getY() - originalPointClickedY * zoom);
            kochManager.drawEdges();
        }
    }

    private void kochPanelMouseDragged(MouseEvent event) {
        zoomTranslateX = zoomTranslateX + event.getX() - lastDragX;
        zoomTranslateY = zoomTranslateY + event.getY() - lastDragY;
        lastDragX = event.getX();
        lastDragY = event.getY();
        //kochManager.drawEdges();
        requestDrawEdges();
    }

    private void kochPanelMousePressed(MouseEvent event) {
        startPressedX = event.getX();
        startPressedY = event.getY();
        lastDragX = event.getX();
        lastDragY = event.getY();
    }

    private void resetZoom() {
        int kpSize = Math.min(kpWidth, kpHeight);
        zoom = kpSize;
        zoomTranslateX = (kpWidth - kpSize) / 2.0;
        zoomTranslateY = (kpHeight - kpSize) / 2.0;
    }

    private Edge edgeAfterZoomAndDrag(Edge e) {
        return new Edge(
                e.X1 * zoom + zoomTranslateX,
                e.Y1 * zoom + zoomTranslateY,
                e.X2 * zoom + zoomTranslateX,
                e.Y2 * zoom + zoomTranslateY,
                e.color);
    }

    public void SetBind(Task task, int side) {
        switch (side) {
            case BOTTOM:
                progressBarMiddle.progressProperty().bind(task.progressProperty());
                labelProgressMiddleText.textProperty().bind(task.messageProperty());
                break;
            case LEFT:
                progressBarLeft.progressProperty().bind(task.progressProperty());
                labelProgressLeftText.textProperty().bind(task.messageProperty());
                break;
            case RIGHT:
                progressBarRight.progressProperty().bind(task.progressProperty());
                labelProgressRightText.textProperty().bind(task.messageProperty());
                break;
        }
    }

    public void UnbindAll() {
        progressBarMiddle.progressProperty().unbind();
        labelProgressMiddleText.textProperty().unbind();
        progressBarLeft.progressProperty().unbind();
        labelProgressLeftText.textProperty().unbind();
        progressBarRight.progressProperty().unbind();
        labelProgressRightText.textProperty().unbind();
    }

    public Stage getStage() {
        return stage;
    }

    /**
     * The main() method is ignored in correctly deployed JavaFX application.
     * main() serves only as fallback in case the application can not be
     * launched through deployment artifacts, e.g., in IDEs with limited FX
     * support. NetBeans ignores main().
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
}
