import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.animation.AnimationTimer;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.shape.Rectangle;

import java.io.File;
import java.util.ArrayList;

public class FlappyBirdFX extends Application {
    
    // Game constants
    private static final int BOARD_WIDTH = 360;
    private static final int BOARD_HEIGHT = 640;
    
    // Images
    private Image backgroundImg;
    private Image[] backgroundImages;
    private Image birdImg;
    private Image[] birdImages;
    private Image topPipeImg;
    private Image bottomPipeImg;
    private Image[][] pipeImages;
    private Image settingsImg;
    private Image logoImg;
    
    // Font
    private Font gameFont;
    private Font gameFontSmall;

    // Audio
    private MediaPlayer backgroundMusicPlayer;
    private Media[] backgroundMusicFiles;
    private MediaPlayer flapSoundPlayer;
    private Media[] flapSoundFiles;
    
    // Settings UI
    private ImageView settingsIcon;
    private VBox settingsPanel;
    private ScrollPane settingsScrollPane;
    private Slider backgroundVolumeSlider;
    private Slider flapVolumeSlider;
    private boolean settingsVisible = false;
    
    // Menu UI
    private VBox menuBox;
    private Button startButton;
    private Button quitButton;
    
    // Preview images
    private ImageView backgroundPreview;
    private ImageView birdPreview;
    private ImageView pipePreview;
    
    // Bird properties
    private int birdX = BOARD_WIDTH / 8;
    private float birdY = BOARD_HEIGHT / 2;
    private int birdWidth = 34;
    private int birdHeight = 35;
    
    // Pipe properties
    private int pipeX = BOARD_WIDTH;
    private int pipeY = 0;
    private int pipeWidth = 64;
    private int pipeHeight = 512;
    
    // Game state
    private int velocityX = -2; // Pipe speed
    private float velocityY = 0;
    private float gravity = 0.5f;
    private boolean gameOver = false;
    private double score = 0;
    private double highScore = 0;
    private boolean gameStarted = false;
    
    // Current selections
    private int currentBackground = 0;
    private int currentBird = 0;
    private int currentPipes = 0;
    private int currentMusic = 0;
    private int currentFlapSound = 0;
    
    // Game objects
    private Bird bird;
    private ArrayList<Pipe> pipes;
    
    // JavaFX components
    private Canvas canvas;
    private GraphicsContext gc;
    private AnimationTimer gameLoop;
    private Timeline placePipeTimeline;
    private Pane root;
    private Stage primaryStage;
    
    // Bird class
    class Bird {
        int x = birdX;
        float y = birdY;
        int width = birdWidth;
        int height = birdHeight;
        Image img;
        
        Bird(Image img) {
            this.img = img;
        }
    }
    
    // Pipe class
    class Pipe {
        int x = pipeX;
        int y = pipeY;
        int width = pipeWidth;
        int height = pipeHeight;
        Image img;
        boolean passed = false;
        
        Pipe(Image img) {
            this.img = img;
        }
    }
    
    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        try {
            // Load logo
            logoImg = new Image("file:resources/images/flappypaimon.png");
            
            // Load images
            backgroundImages = new Image[] {
                new Image("file:resources/images/flappybirdbg.png"),
                new Image("file:resources/images/flappybirdbg2.png")
            };
            backgroundImg = backgroundImages[0];
            
            birdImages = new Image[] {
                new Image("file:resources/images/paimon.png"),
                new Image("file:resources/images/flappybird.png")
            };
            birdImg = birdImages[0];
            
            // Pipe sets (0 = green, 1 = blue)
            pipeImages = new Image[2][2];
            pipeImages = new Image[2][2];
            pipeImages[0][0] = new Image("file:resources/images/toppipe.png"); // green top
            pipeImages[0][1] = new Image("file:resources/images/bottompipe.png"); // green bottom
            pipeImages[1][0] = new Image("file:resources/images/blue_toppipe.png"); // blue top
            pipeImages[1][1] = new Image("file:resources/images/blue_bottompipe.png"); // blue bottom
            topPipeImg = pipeImages[0][0];
            bottomPipeImg = pipeImages[0][1];
            
            settingsImg = new Image("file:resources/images/settings.png");

            // Load audio files
            backgroundMusicFiles = new Media[] {
                new Media(new File("resources/audio/backgroundmusic.mp3").toURI().toString()),
                new Media(new File("resources/audio/backgroundmusic2.mp3").toURI().toString()),
                new Media(new File("resources/audio/backgroundmusic3.mp3").toURI().toString())
            };
            
            flapSoundFiles = new Media[] {
                new Media(new File("resources/audio/flap.wav").toURI().toString()),
                new Media(new File("resources/audio/flap2.wav").toURI().toString())
            };

            // Load the custom font
            gameFont = Font.loadFont("file:resources/fonts/zh-cn.ttf", 30);
            gameFontSmall = Font.loadFont("file:resources/fonts/zh-cn.ttf", 24);
            
            // Fallback to system font if custom font fails to load
            if (gameFont == null) {
                gameFont = new Font(32);
                gameFontSmall = new Font(24);
            }

            // Initialize audio players
            backgroundMusicPlayer = new MediaPlayer(backgroundMusicFiles[0]);
            flapSoundPlayer = new MediaPlayer(flapSoundFiles[0]);
            
            // Set background music to loop with higher volume
            backgroundMusicPlayer.setCycleCount(MediaPlayer.INDEFINITE);
            backgroundMusicPlayer.setVolume(1.0); // Maximum volume
            
            // Add error listeners
            backgroundMusicPlayer.setOnError(() -> 
                System.err.println("Background music error: " + backgroundMusicPlayer.getError()));
            flapSoundPlayer.setOnError(() -> 
                System.err.println("Flap sound error: " + flapSoundPlayer.getError()));
            
        } catch (Exception e) {
            System.err.println("Error loading resources: " + e.getMessage());
            // Continue with reduced functionality if images fail to load
        }
        
        // Initialize game objects
        bird = new Bird(birdImg);
        pipes = new ArrayList<>();
        
        // Set up the game canvas
        canvas = new Canvas(BOARD_WIDTH, BOARD_HEIGHT);
        gc = canvas.getGraphicsContext2D();
        root = new Pane(canvas);
        
        // Create menu UI
        createMenuUI();
        
        // Create settings UI
        createSettingsUI();
        
        // Set up the scene
        Scene scene = new Scene(root, BOARD_WIDTH, BOARD_HEIGHT);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Flappy Bird");
        primaryStage.setResizable(false);
        
        // Set up keyboard controls
        scene.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.SPACE && gameStarted && !gameOver) {
                // Play flap sound
                if (flapSoundPlayer != null) {
                    flapSoundPlayer.stop();
                    flapSoundPlayer.play();
                }
                velocityY = -8; // Jump force
            } else if (e.getCode() == KeyCode.ESCAPE) {
                // Toggle settings panel
                toggleSettings();
            }
        });
        
        // Close settings when clicking outside
        scene.setOnMouseClicked(e -> {
            if (settingsVisible && !settingsScrollPane.getBoundsInParent().contains(e.getX(), e.getY()) 
                && !settingsIcon.getBoundsInParent().contains(e.getX(), e.getY())) {
                toggleSettings();
            }
        });
        
        // Initial draw before game starts
        drawStartScreen();
        primaryStage.show();
    }
    
    private void createMenuUI() {
        // Add drop shadow effect for buttons
        DropShadow dropShadow = new DropShadow();
        dropShadow.setRadius(5.0);
        dropShadow.setOffsetX(3.0);
        dropShadow.setOffsetY(3.0);
        dropShadow.setColor(Color.color(0, 0, 0, 0.5));
        
        startButton = new Button("Start Game");
        startButton.setStyle("-fx-font-size: 20; -fx-min-width: 150; -fx-min-height: 40; " +
                           "-fx-background-color: #2196F3; -fx-text-fill: white; " +  // Changed to blue
                           "-fx-background-radius: 20;");
        startButton.setEffect(dropShadow);
        startButton.setOnAction(e -> {
            gameStarted = true;
            setupGame();
            menuBox.setVisible(false);
            // Start background music when game starts
            if (backgroundMusicPlayer != null) {
                backgroundMusicPlayer.seek(Duration.ZERO);
                backgroundMusicPlayer.play();
            }
        });
        
        quitButton = new Button("Quit Game");
        quitButton.setStyle("-fx-font-size: 20; -fx-min-width: 150; -fx-min-height: 40; " +
                          "-fx-background-color: #000000; -fx-text-fill: white; " +  // Changed to black
                          "-fx-background-radius: 20;");
        quitButton.setEffect(dropShadow);
        quitButton.setOnAction(e -> primaryStage.close());
        
        menuBox = new VBox(20, startButton, quitButton);
        menuBox.setAlignment(Pos.CENTER);
        menuBox.setLayoutX(BOARD_WIDTH/2 - 75);
        menuBox.setLayoutY(BOARD_HEIGHT/2 + 20);
        
        root.getChildren().add(menuBox);
    }
    
    private void createSettingsUI() {
        // Settings icon with improved appearance
        settingsIcon = new ImageView(settingsImg);
        settingsIcon.setFitWidth(35);
        settingsIcon.setFitHeight(35);
        settingsIcon.setTranslateX(BOARD_WIDTH - 45);
        settingsIcon.setTranslateY(10);
        
        // Add drop shadow to settings icon
        DropShadow iconShadow = new DropShadow();
        iconShadow.setRadius(10.0);
        iconShadow.setColor(Color.BLACK);
        settingsIcon.setEffect(iconShadow);
        
        settingsIcon.setOnMouseClicked(this::toggleSettings);
        
        // Create preview images
        backgroundPreview = new ImageView(backgroundImages[0]);
        backgroundPreview.setFitWidth(80);
        backgroundPreview.setFitHeight(60);
        
        birdPreview = new ImageView(birdImages[0]);
        birdPreview.setFitWidth(40);
        birdPreview.setFitHeight(40);
        
        pipePreview = new ImageView(pipeImages[0][0]);
        pipePreview.setFitWidth(40);
        pipePreview.setFitHeight(60);
        
        // Common button style
        String buttonStyle = "-fx-font-size: 12; -fx-min-width: 50; -fx-min-height: 25; " +
                           "-fx-background-radius: 10; -fx-text-fill: white;";
        
        // Background selection buttons
        HBox backgroundButtons = new HBox(5);
        for (int i = 0; i < backgroundImages.length; i++) {
            int index = i;
            Button btn = new Button("BG " + (i+1));
            btn.setStyle(buttonStyle + "-fx-background-color: " + (currentBackground == index ? "#1a7bc8" : "#2196F3") + ";");
            btn.setOnAction(e -> {
                currentBackground = index;
                backgroundImg = backgroundImages[currentBackground];
                backgroundPreview.setImage(backgroundImg);
                drawStartScreen();
                // Update button colors
                for (int j = 0; j < backgroundButtons.getChildren().size(); j++) {
                    Button b = (Button)backgroundButtons.getChildren().get(j);
                    b.setStyle(buttonStyle + "-fx-background-color: " + (currentBackground == j ? "#1a7bc8" : "#2196F3") + ";");
                }
            });
            backgroundButtons.getChildren().add(btn);
        }
        
        // Bird selection buttons
        HBox birdButtons = new HBox(5);
        for (int i = 0; i < birdImages.length; i++) {
            int index = i;
            Button btn = new Button(i == 0 ? "Paimon" : "Bird");
            btn.setStyle(buttonStyle + "-fx-background-color: " + (currentBird == index ? "#cc7a00" : "#FF9800") + ";");
            btn.setOnAction(e -> {
                currentBird = index;
                birdImg = birdImages[currentBird];
                bird.img = birdImg;
                // Adjust bird dimensions based on selection
                if (currentBird == 1) { // flappybird.png
                    bird.width = 34;
                    bird.height = 24;
                } else { // paimon.png
                    bird.width = 34;
                    bird.height = 35;
                }
                birdPreview.setImage(birdImg);
                // Update button colors
                for (int j = 0; j < birdButtons.getChildren().size(); j++) {
                    Button b = (Button)birdButtons.getChildren().get(j);
                    b.setStyle(buttonStyle + "-fx-background-color: " + (currentBird == j ? "#cc7a00" : "#FF9800") + ";");
                }
            });
            birdButtons.getChildren().add(btn);
        }
        
        // Pipe selection buttons
        HBox pipeButtons = new HBox(5);
        String[] pipeNames = {"Green", "Blue"};
        for (int i = 0; i < pipeImages.length; i++) {
            int index = i;
            Button btn = new Button(pipeNames[i]);
            btn.setStyle(buttonStyle + "-fx-background-color: " + (currentPipes == index ? "#3d8b40" : "#4CAF50") + ";");
            btn.setOnAction(e -> {
                currentPipes = index;
                topPipeImg = pipeImages[currentPipes][0];
                bottomPipeImg = pipeImages[currentPipes][1];
                pipePreview.setImage(topPipeImg);
                // Update existing pipes
                for (Pipe pipe : pipes) {
                    pipe.img = pipe.y < BOARD_HEIGHT/2 ? topPipeImg : bottomPipeImg;
                }
                // Update button colors
                for (int j = 0; j < pipeButtons.getChildren().size(); j++) {
                    Button b = (Button)pipeButtons.getChildren().get(j);
                    b.setStyle(buttonStyle + "-fx-background-color: " + (currentPipes == j ? "#3d8b40" : "#4CAF50") + ";");
                }
            });
            pipeButtons.getChildren().add(btn);
        }
        
        // Music selection buttons
        HBox musicButtons = new HBox(5);
        for (int i = 0; i < backgroundMusicFiles.length; i++) {
            int index = i;
            Button btn = new Button("Music " + (i+1));
            btn.setStyle(buttonStyle + "-fx-background-color: " + (currentMusic == index ? "#7b1fa2" : "#9C27B0") + ";");
            btn.setOnAction(e -> {
                currentMusic = index;
                backgroundMusicPlayer.stop();
                backgroundMusicPlayer = new MediaPlayer(backgroundMusicFiles[currentMusic]);
                backgroundMusicPlayer.setCycleCount(MediaPlayer.INDEFINITE);
                backgroundMusicPlayer.setVolume(backgroundVolumeSlider.getValue());
                if (gameStarted && !gameOver) {
                    backgroundMusicPlayer.play();
                }
                // Update button colors
                for (int j = 0; j < musicButtons.getChildren().size(); j++) {
                    Button b = (Button)musicButtons.getChildren().get(j);
                    b.setStyle(buttonStyle + "-fx-background-color: " + (currentMusic == j ? "#7b1fa2" : "#9C27B0") + ";");
                }
            });
            musicButtons.getChildren().add(btn);
        }
        
        // Flap sound selection buttons
        HBox flapButtons = new HBox(5);
        for (int i = 0; i < flapSoundFiles.length; i++) {
            int index = i;
            Button btn = new Button("Sound " + (i+1));
            btn.setStyle(buttonStyle + "-fx-background-color: " + (currentFlapSound == index ? "#c2185b" : "#E91E63") + ";");
            btn.setOnAction(e -> {
                currentFlapSound = index;
                flapSoundPlayer.stop();
                flapSoundPlayer = new MediaPlayer(flapSoundFiles[currentFlapSound]);
                flapSoundPlayer.setVolume(flapVolumeSlider.getValue());
                // Update button colors
                for (int j = 0; j < flapButtons.getChildren().size(); j++) {
                    Button b = (Button)flapButtons.getChildren().get(j);
                    b.setStyle(buttonStyle + "-fx-background-color: " + (currentFlapSound == j ? "#c2185b" : "#E91E63") + ";");
                }
            });
            flapButtons.getChildren().add(btn);
        }
        
        // Volume sliders with improved styling
        backgroundVolumeSlider = new Slider(0, 1, 1.0);
        backgroundVolumeSlider.setPrefWidth(150);
        backgroundVolumeSlider.setStyle("-fx-control-inner-background: #9C27B0; -fx-accent: white;");
        backgroundVolumeSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (backgroundMusicPlayer != null) {
                backgroundMusicPlayer.setVolume(newVal.doubleValue());
            }
        });
        
        flapVolumeSlider = new Slider(0, 1, 0.5);
        flapVolumeSlider.setPrefWidth(150);
        flapVolumeSlider.setStyle("-fx-control-inner-background: #E91E63; -fx-accent: white;");
        flapVolumeSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (flapSoundPlayer != null) {
                flapSoundPlayer.setVolume(newVal.doubleValue());
            }
        });
        
        // Create title for settings panel
        Label titleLabel = new Label("Game Settings");
        titleLabel.setStyle("-fx-font-size: 20; -fx-text-fill: white; -fx-font-weight: bold;");
        titleLabel.setAlignment(Pos.CENTER);
        
        // Create settings labels with consistent styling
        String labelStyle = "-fx-font-size: 14; -fx-text-fill: white; -fx-font-weight: bold;";
        
        Label bgLabel = new Label("Background:");
        bgLabel.setStyle(labelStyle);
        
        Label birdLabel = new Label("Bird:");
        birdLabel.setStyle(labelStyle);
        
        Label pipeLabel = new Label("Pipes:");
        pipeLabel.setStyle(labelStyle);
        
        Label musicLabel = new Label("Music:");
        musicLabel.setStyle(labelStyle);
        
        Label flapLabel = new Label("Flap Sound:");
        flapLabel.setStyle(labelStyle);
        
        Label bgVolLabel = new Label("Music Volume:");
        bgVolLabel.setStyle(labelStyle);
        
        Label flapVolLabel = new Label("Flap Volume:");
        flapVolLabel.setStyle(labelStyle);
        
        // Container for preview images with border and padding
        HBox bgPreviewBox = new HBox(10, backgroundButtons, backgroundPreview);
        bgPreviewBox.setStyle("-fx-padding: 5; -fx-background-color: rgba(33, 150, 243, 0.3); -fx-background-radius: 5;");
        
        HBox birdPreviewBox = new HBox(10, birdButtons, birdPreview);
        birdPreviewBox.setStyle("-fx-padding: 5; -fx-background-color: rgba(255, 152, 0, 0.3); -fx-background-radius: 5;");
        
        HBox pipePreviewBox = new HBox(10, pipeButtons, pipePreview);
        pipePreviewBox.setStyle("-fx-padding: 5; -fx-background-color: rgba(76, 175, 80, 0.3); -fx-background-radius: 5;");
        
        // Create settings panel content
        settingsPanel = new VBox(8,
            titleLabel,
            bgLabel, bgPreviewBox,
            birdLabel, birdPreviewBox,
            pipeLabel, pipePreviewBox,
            musicLabel, musicButtons,
            flapLabel, flapButtons,
            bgVolLabel, backgroundVolumeSlider,
            flapVolLabel, flapVolumeSlider
        );
        settingsPanel.setAlignment(Pos.TOP_LEFT);
        settingsPanel.setStyle("-fx-padding: 15;");
        
        // Create a ScrollPane to contain the settings panel
        settingsScrollPane = new ScrollPane(settingsPanel);
        settingsScrollPane.setPrefWidth(250);
        settingsScrollPane.setPrefHeight(460); // Limit height to avoid overflow
        settingsScrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        settingsScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        settingsScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        
        // Add a background pane with rounded corners and drop shadow
        Rectangle settingsBg = new Rectangle(260, 470);
        settingsBg.setArcWidth(20);
        settingsBg.setArcHeight(20);
        settingsBg.setFill(Color.color(0, 0, 0, 0.85));
        
        DropShadow panelShadow = new DropShadow();
        panelShadow.setRadius(15);
        panelShadow.setOffsetX(5);
        panelShadow.setOffsetY(5);
        panelShadow.setColor(Color.color(0, 0, 0, 0.6));
        settingsBg.setEffect(panelShadow);
        
        // Position the settings elements
        settingsBg.setTranslateX(BOARD_WIDTH - 280);
        settingsBg.setTranslateY(50);
        settingsScrollPane.setTranslateX(BOARD_WIDTH - 275);
        settingsScrollPane.setTranslateY(55);
        
        // Setup z-index to ensure settings appears above menu buttons
        settingsBg.setVisible(false);
        settingsScrollPane.setVisible(false);
        
        root.getChildren().addAll(settingsBg, settingsScrollPane, settingsIcon);
        
        // Store reference to background for toggle
        settingsBg.setUserData("settingsBg");
    }
    
    private void toggleSettings(MouseEvent event) {
        settingsVisible = !settingsVisible;
        
        // Create a copy of the children list to avoid ConcurrentModificationException
        ArrayList<javafx.scene.Node> childrenCopy = new ArrayList<>(root.getChildren());
        
        // Find the settings background rectangle
        for (javafx.scene.Node node : childrenCopy) {
            if (node instanceof Rectangle && "settingsBg".equals(node.getUserData())) {
                node.setVisible(settingsVisible);
            }
        }
        
        settingsScrollPane.setVisible(settingsVisible);
        
        // Bring settings to front when visible
        if (settingsVisible) {
            settingsScrollPane.toFront();
            for (javafx.scene.Node node : childrenCopy) {
                if (node instanceof Rectangle && "settingsBg".equals(node.getUserData())) {
                    node.toFront();
                    settingsScrollPane.toFront();
                }
            }
            settingsIcon.toFront();
        } else {
            menuBox.toFront();
        }
        
        event.consume();
    }
    
    private void toggleSettings() {
        settingsVisible = !settingsVisible;
        
        // Create a copy of the children list to avoid ConcurrentModificationException
        ArrayList<javafx.scene.Node> childrenCopy = new ArrayList<>(root.getChildren());
        
        // Find the settings background rectangle
        for (javafx.scene.Node node : childrenCopy) {
            if (node instanceof Rectangle && "settingsBg".equals(node.getUserData())) {
                node.setVisible(settingsVisible);
            }
        }
        
        settingsScrollPane.setVisible(settingsVisible);
        
        // Bring settings to front when visible
        if (settingsVisible) {
            for (javafx.scene.Node node : childrenCopy) {
                if (node instanceof Rectangle && "settingsBg".equals(node.getUserData())) {
                    node.toFront();
                    settingsScrollPane.toFront();
                }
            }
            settingsIcon.toFront();
        } else {
            menuBox.toFront();
        }
    }
    
    private void resetGame() {
        bird.y = birdY;
        velocityY = 0;
        pipes.clear();
        gameOver = false;
        score = 0;
        gameStarted = false;
        menuBox.setVisible(true);
        settingsIcon.setVisible(true);
        drawStartScreen();
    }
    
    private void drawStartScreen() {
        // Clear canvas before drawing
        gc.clearRect(0, 0, BOARD_WIDTH, BOARD_HEIGHT);

        // Draw background
        gc.drawImage(backgroundImg, 0, 0, BOARD_WIDTH, BOARD_HEIGHT);
        
        // Draw logo at top center
        if (logoImg != null) {
            double logoWidth = 250;
            double logoHeight = 100;
            gc.drawImage(logoImg, BOARD_WIDTH/2 - logoWidth/2, 30, logoWidth, logoHeight);
        }
        
        // Draw high score at center
        gc.setFill(Color.WHITE);
        gc.setFont(gameFont);
        String highScoreText = "High Score: " + (int) highScore;
        double textWidth = gc.getFont().getSize() * highScoreText.length() * 0.6;
        gc.fillText(highScoreText, BOARD_WIDTH/2 - textWidth/2, BOARD_HEIGHT/2 - 30);
    }
    
    private void setupGame() {
        // Stop any existing animations
        if (gameLoop != null) gameLoop.stop();
        if (placePipeTimeline != null) placePipeTimeline.stop();
        
        // Hide settings icon during gameplay
        settingsIcon.setVisible(false);
        
        // Reset background music
        if (backgroundMusicPlayer != null) {
            backgroundMusicPlayer.stop();
            backgroundMusicPlayer.seek(Duration.ZERO);
            backgroundMusicPlayer.play();
        }

        // Set up pipe placement timeline
        placePipeTimeline = new Timeline(
            new KeyFrame(Duration.millis(1800), e -> placePipes())
        );
        placePipeTimeline.setCycleCount(Timeline.INDEFINITE);
        placePipeTimeline.play();
        
        // Set up game loop
        gameLoop = new AnimationTimer() {
            @Override
            public void handle(long now) {
                move();
                draw();
                if (gameOver) {
                    stop();
                    placePipeTimeline.stop();
                    drawGameOver();
                    if (backgroundMusicPlayer != null) {
                        backgroundMusicPlayer.stop();
                    }
                    // Show menu again after game over
                    resetGame();
                }
            }
        };
        gameLoop.start();
    }
    
    private void placePipes() {
        int randomPipeY = (int)(pipeY - pipeHeight/3 - Math.random()*(pipeHeight/3));
        int openingSpace = BOARD_HEIGHT/3;
        
        Pipe topPipe = new Pipe(topPipeImg);
        topPipe.y = randomPipeY;
        pipes.add(topPipe);
        
        Pipe bottomPipe = new Pipe(bottomPipeImg);
        bottomPipe.y = topPipe.y + pipeHeight + openingSpace;
        pipes.add(bottomPipe);
    }
    
    private void draw() {
        // Clear canvas
        gc.clearRect(0, 0, BOARD_WIDTH, BOARD_HEIGHT);
        
        // Draw background
        gc.drawImage(backgroundImg, 0, 0, BOARD_WIDTH, BOARD_HEIGHT);
        
        // Draw bird
        gc.drawImage(bird.img, bird.x, bird.y, bird.width, bird.height);
        
        // Draw pipes
        for (Pipe pipe : pipes) {
            gc.drawImage(pipe.img, pipe.x, pipe.y, pipe.width, pipe.height);
        }
        
        // Draw score
        gc.setFill(Color.WHITE);
        gc.setFont(gameFont);
        gc.fillText(String.valueOf((int)score), 10, 35);
    }
    
    private void drawGameOver() {
        // First draw the full game state
        draw();
        
        // Then draw the game over overlay
        gc.setFill(new Color(0, 0, 0, 0.5));
        gc.fillRect(0, 0, BOARD_WIDTH, BOARD_HEIGHT);
        
        gc.setFill(Color.WHITE);
        gc.setFont(gameFont);
        gc.fillText("Game Over", BOARD_WIDTH/2 - 70, BOARD_HEIGHT/2 - 80);
        gc.fillText("Score: " + (int)score, BOARD_WIDTH/2 - 70, BOARD_HEIGHT/2 - 30);
        gc.fillText("High Score: " + (int)highScore, BOARD_WIDTH/2 - 70, BOARD_HEIGHT/2 + 20);
    }
    
    private void move() {
        // Bird movement
        if (gameStarted && !gameOver) {
            velocityY += gravity;
            bird.y += velocityY;
            bird.y = Math.max(bird.y, 0);
        }
        
        // Pipe movement
        for (Pipe pipe : pipes) {
            pipe.x += velocityX;
            
            if (!pipe.passed && bird.x > pipe.x + pipe.width) {
                score += 0.5;
                pipe.passed = true;
                highScore = Math.max(score, highScore);
            }
            
            if (collision(bird, pipe)) {
                gameOver = true;
            }
        }
        
        // Remove off-screen pipes
        pipes.removeIf(pipe -> pipe.x + pipe.width < 0);
        
        if (bird.y > BOARD_HEIGHT) {
            gameOver = true;
        }
    }
    
    private boolean collision(Bird a, Pipe b) {
        int threshold = 5;
        return a.x + a.width - threshold > b.x + threshold &&
               a.x + threshold < b.x + b.width - threshold &&
               a.y + a.height - threshold > b.y + threshold &&
               a.y + threshold < b.y + b.height - threshold;
    }
    
    @Override
    public void stop() {
        // Clean up resources
        if (backgroundMusicPlayer != null) {
            backgroundMusicPlayer.stop();
            backgroundMusicPlayer.dispose();
        }
        if (flapSoundPlayer != null) {
            flapSoundPlayer.stop();
            flapSoundPlayer.dispose();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}