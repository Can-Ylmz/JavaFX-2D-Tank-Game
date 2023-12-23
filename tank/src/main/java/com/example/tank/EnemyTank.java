
/*
package com.example.tank;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class TankGame extends Application {

    private static final int WIDTH = 800;
    private static final int HEIGHT = 600;

    private Tank playerTank;
    private List<BotTank> botTanks;
    private List<Bullet> bullets;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Tank Oyunu");

        // Oyuncu tankını oluştur
        playerTank = new Tank(WIDTH / 2, HEIGHT / 2);

        // Bot tanklarını oluştur
        botTanks = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            botTanks.add(new BotTank());
        }

        // Ateş edilen mermileri tutmak için liste oluştur
        bullets = new ArrayList<>();

        // Oyun alanı
        Canvas canvas = new Canvas(WIDTH, HEIGHT);
        GraphicsContext gc = canvas.getGraphicsContext2D();

        // Oyun döngüsü
        new AnimationTimer() {
            @Override
            public void handle(long now) {
                update();
                render(gc);
            }
        }.start();

        // Klavye olaylarını dinle
        canvas.setFocusTraversable(true);
        canvas.setOnKeyPressed(e -> handleKeyPress(e.getCode()));
        canvas.setOnKeyReleased(e -> handleKeyRelease(e.getCode()));

        // Oyun alanını ekranına ekle
        StackPane root = new StackPane();
        root.getChildren().add(canvas);

        primaryStage.setScene(new Scene(root, WIDTH, HEIGHT));
        primaryStage.show();
    }

    private void update() {
        // Oyuncu ve bot tanklarını güncelle
        playerTank.update();

        for (BotTank botTank : botTanks) {
            botTank.update(playerTank.getX(), playerTank.getY());
            if (botTank.shouldFire()) {
                bullets.add(botTank.fire());
            }
        }

        // Mermileri güncelle
        for (Bullet bullet : bullets) {
            bullet.update();
        }

        // Çarpışma kontrolü
        checkCollisions();
    }

    private void render(GraphicsContext gc) {
        // Ekranı temizle
        gc.setFill(Color.BLACK);
        gc.fillRect(0, 0, WIDTH, HEIGHT);

        // Oyuncu ve bot tanklarını ekrana çiz
        playerTank.render(gc);

        for (BotTank botTank : botTanks) {
            botTank.render(gc);
        }

        // Mermileri ekrana çiz
        for (Bullet bullet : bullets) {
            bullet.render(gc);
        }
    }

    private void handleKeyPress(KeyCode code) {
        // Klavye tuşuna basıldığında işlemler
        switch (code) {
            case UP:
                playerTank.setMovingUp(true);
                break;
            case DOWN:
                playerTank.setMovingDown(true);
                break;
            case LEFT:
                playerTank.setMovingLeft(true);
                break;
            case RIGHT:
                playerTank.setMovingRight(true);
                break;
            case SPACE:
                // Space tuşuna basıldığında oyuncu ateş et
                bullets.add(playerTank.fire(playerTank.getX(), playerTank.getY()));
                break;
        }
    }

    private void handleKeyRelease(KeyCode code) {
        // Klavye tuşu bırakıldığında işlemler
        switch (code) {
            case UP:
                playerTank.setMovingUp(false);
                break;
            case DOWN:
                playerTank.setMovingDown(false);
                break;
            case LEFT:
                playerTank.setMovingLeft(false);
                break;
            case RIGHT:
                playerTank.setMovingRight(false);
                break;
        }
    }

    private void checkCollisions() {
        // Çarpışma kontrolü burada eklenebilir
    }

    public class Tank {
        private double x;
        private double y;
        private boolean movingUp;
        private boolean movingDown;
        private boolean movingLeft;
        private boolean movingRight;

        public Tank(double x, double y) {
            this.x = x;
            this.y = y;
        }

        public void update() {
            // Oyuncu tankının güncellenmesi
            if (movingUp && y > 0) {
                y -= 5;
            }
            if (movingDown && y < HEIGHT - 30) {
                y += 5;
            }
            if (movingLeft && x > 0) {
                x -= 5;
            }
            if (movingRight && x < WIDTH - 50) {
                x += 5;
            }
        }

        public void render(GraphicsContext gc) {
            // Oyuncu tankını ekrana çiz (mavi renkte)
            gc.setFill(Color.BLUE);
            gc.fillRect(x, y, 50, 30);
        }

        public void setMovingUp(boolean movingUp) {
            this.movingUp = movingUp;
        }

        public void setMovingDown(boolean movingDown) {
            this.movingDown = movingDown;
        }

        public void setMovingLeft(boolean movingLeft) {
            this.movingLeft = movingLeft;
        }

        public void setMovingRight(boolean movingRight) {
            this.movingRight = movingRight;
        }

        public double getX() {
            return x;
        }

        public double getY() {
            return y;
        }

        public Bullet fire(double playerX, double playerY) {
            // Bot tankı ateş ettiğinde yeni bir mermi oluştur
            return new Bullet(x + 25, y, playerX, playerY);
        }

    }

    public class BotTank {
        private double x;
        private double y;
        private double speed;
        private double targetX;
        private double targetY;
        private int fireCooldown;
        public Bullet fire() {
            // Bot tankı ateş ettiğinde yeni bir mermi oluştur
            return new Bullet(x + 25, y, targetX, targetY);
        }

        public BotTank() {
            Random random = new Random();
            this.x = random.nextDouble() * WIDTH;
            this.y = random.nextDouble() * HEIGHT;
            this.speed = random.nextDouble() * 1;
            this.targetX = random.nextDouble() * WIDTH;
            this.targetY = random.nextDouble() * HEIGHT;
            this.fireCooldown = 0;
        }

        public void update(double playerX, double playerY) {
            // Bot tankının güncellenmesi
            moveTowardsTarget(playerX, playerY);
            updateFireCooldown();
            if (canFire()) {
                bullets.add(fire(playerX, playerY));
                resetFireCooldown();
            }
        }
        private void moveTowardsTarget(double playerX, double playerY) {
            double angle = Math.atan2(playerY - y, playerX - x);
            x += speed * Math.cos(angle);
            y += speed * Math.sin(angle);
        }

        private void moveTowardsTarget() {
            double angle = Math.atan2(targetY - y, targetX - x);
            x += speed * Math.cos(angle);
            y += speed * Math.sin(angle);
        }

        private boolean canFire() {
            return fireCooldown <= 0;
        }

        private void resetFireCooldown() {
            fireCooldown = 60; // 60 frame (1 saniye) ateşleme aralığı
        }

        private void updateFireCooldown() {
            if (fireCooldown > 0) {
                fireCooldown--;
            }
        }

        public Bullet fire(double playerX, double playerY) {
            // Bot tankı ateş ettiğinde yeni bir mermi oluştur
            return new Bullet(x + 25, y, playerX, playerY);
        }


        public boolean shouldFire() {
            // Belirli bir olasılıkla ateş et
            Random random = new Random();
            return random.nextDouble() < 0.01;
        }

        public void render(GraphicsContext gc) {
            // Bot tankını ekrana çiz (kırmızı renkte)
            gc.setFill(Color.RED);
            gc.fillRect(x, y, 50, 30);
        }
    }

    public class Bullet {
        private double x;
        private double y;
        private double targetX;
        private double targetY;
        private double speed;

        public Bullet(double startX, double startY, double targetX, double targetY) {
            this.x = startX;
            this.y = startY;
            this.targetX = targetX;
            this.targetY = targetY;
            this.speed = 2; // Mermi hızı
        }

        public void update() {
            // Mermi hareketi
            double angle = Math.atan2(targetY - y, targetX - x);
            x += speed * Math.cos(angle);
            y += speed * Math.sin(angle);
        }

        public void render(GraphicsContext gc) {
            // Mermiyi ekrana çiz (sarı renkte)
            gc.setFill(Color.YELLOW);
            gc.fillRect(x, y, 5, 10);
        }
    }

}

*/

/*
package com.example.tank;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.geometry.Point2D;

import java.util.ArrayList;
import java.util.List;

public class TankGame extends Application {

    private Rectangle tank; // Tankı temsil eden dikdörtgen
    private Pane pane;
    private boolean canFire = true;
    private Point2D tankPosition;
    private List<Circle> bots = new ArrayList<>();
    private boolean canFireBot = true;

    @Override
    public void start(Stage primaryStage) {
        // Tankı oluştur
        tank = new Rectangle(500, 500, 30, 30);
        tank.setFill(Color.BLUE);

        // Pane oluştur ve tankı pane'e ekle
        pane = new Pane();
        pane.getChildren().add(tank);

        // Botları oluştur
        createBots();

        // Sahneyi oluştur ve pane'i sahneye ekle
        Scene scene = new Scene(pane, 800, 600);
        primaryStage.setScene(scene);

        // Klavye girişlerini dinle
        scene.setOnKeyPressed(e -> handleKeyPress(e.getCode()));

        // Oyun döngüsü
        AnimationTimer timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                update();
            }
        };
        timer.start();

        // Pencereyi göster
        primaryStage.setTitle("Tank Game");
        primaryStage.show();
    }

    // Botları oluştur
    private void createBots() {
        double botX = 200;
        double botY = 100;

        for (int i = 0; i < 3; i++) {
            Circle bot = new Circle(botX, botY, 15);
            bot.setFill(Color.RED);
            bots.add(bot);
            pane.getChildren().add(bot);

            // Ayarla ki birbirine yakın olmasınlar
            botX += 200;
            botY += 50;
        }
    }

    // Klavye girişlerini işle
    private void handleKeyPress(KeyCode code) {
        double speed = 10; // Hareket hızı

        switch (code) {
            case UP:
                if (tank.getY() > 0) {
                    tank.setY(tank.getY() - speed); // Yukarı hareket
                }
                break;
            case DOWN:
                if (tank.getY() + tank.getHeight() < pane.getHeight()) {
                    tank.setY(tank.getY() + speed); // Aşağı hareket
                }
                break;
            case LEFT:
                if (tank.getX() > 0) {
                    tank.setX(tank.getX() - speed); // Sola hareket
                }
                break;
            case RIGHT:
                if (tank.getX() + tank.getWidth() < pane.getWidth()) {
                    tank.setX(tank.getX() + speed); // Sağa hareket
                }
                break;
            case SPACE:
                if (canFire) {
                    fire();
                    canFire = false;
                }
                break;
        }
    }

    // Mermi ateşleme
    private void fire() {
        Rectangle bullet = new Rectangle(tank.getX() + tank.getWidth() / 2 - 2.5, tank.getY(), 5, 10); // Boyutu küçültüldü
        bullet.setFill(Color.RED);
        pane.getChildren().add(bullet);

        // Mermi hareketi
        AnimationTimer bulletTimer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                bullet.setY(bullet.getY() - 2);

                // Mermi sınırlara ulaştığında kaldır
                if (bullet.getY() < 0) {
                    pane.getChildren().remove(bullet);
                    canFire = true;
                    this.stop(); // Timer'ı durdur
                }
            }
        };
        bulletTimer.start();
    }

    // Oyun durumunu güncelle
    private void update() {
        // Tankın mermisi botları vurdu mu kontrolü
        checkBulletBotCollision();

        // Oyun durumunu güncelleme (gerektiğinde)
        tankPosition = new Point2D(tank.getX() + tank.getWidth() / 2, tank.getY() + tank.getHeight() / 2);

        // Botları güncelle
        for (Circle bot : bots) {
            moveBotTowardsTank(bot);
            fireBot(bot);
        }
    }
    private void moveBotTowardsTank(Circle bot) {
        Point2D botPosition = new Point2D(bot.getCenterX(), bot.getCenterY());

        // Hedefe doğru vektörü bul
        Point2D direction = tankPosition.subtract(botPosition).normalize();

        // Hareket hızı
        double speed = 0.5;

        // Yeni konumu hesapla
        double newX = botPosition.getX() + direction.getX() * speed;
        double newY = botPosition.getY() + direction.getY() * speed;

        // Yeni konumu uygula
        bot.setCenterX(newX);
        bot.setCenterY(newY);
    }

    // Botların mermisi tankı vurdu mu kontrolü
    private void checkBulletBotCollision() {
        for (Circle bot : bots) {
            for (Node node : pane.getChildren()) {
                if (node instanceof Rectangle && node.getBoundsInParent().intersects(bot.getBoundsInParent())) {
                    // Mermi ve bot çarpıştı
                    pane.getChildren().remove(node); // Mermiyi kaldır
                    canFire = true; // Yeni ateşe izin ver
                    // Botu tekrar yukarıdan başlat
                    bot.setCenterY(0);
                }
            }
        }
    }

    // Bot mermisi ateşleme
    private void fireBot(Circle bot) {
        Circle bullet = new Circle(bot.getCenterX(), bot.getCenterY(), 5);
        bullet.setFill(Color.YELLOW);
        pane.getChildren().add(bullet);

        startBulletAnimation(bullet, 5); // 2 değeri mermi hareket hızını belirler
    }

    private void startBulletAnimation(Circle bullet, double bulletSpeed) {
        AnimationTimer bulletTimer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                moveBullet(bullet, bulletSpeed);

                // Mermi sınırlara ulaştığında kaldır
                if (isBulletOutOfPane(bullet)) {
                    removeBullet(bullet, this);
                } else {
                    // Tank ile mermi arasındaki çarpışma kontrolü
                    checkBulletTankCollision(bullet);
                }
            }
        };
        bulletTimer.start();
    }
    // Mermi ve tankın çarpışma kontrolü
    private void checkBulletTankCollision(Circle bullet) {
        if (tank.getBoundsInParent().intersects(bullet.getBoundsInParent())) {
            // Mermi ve tank çarpıştı
            gameOver(); // Oyunu bitir
        }
    }
    private void moveBullet(Circle bullet, double speed) {
        bullet.setCenterY(bullet.getCenterY() + speed);
    }

    private boolean isBulletOutOfPane(Circle bullet) {
        return bullet.getCenterY() > pane.getHeight();
    }

    private void removeBullet(Circle bullet, AnimationTimer timer) {
        pane.getChildren().remove(bullet);
        canFireBot = true;
        timer.stop();
    }
    // Oyun bitişini kontrol et ve gerekli aksiyonları al
    private void gameOver() {
        // Burada oyunun bitiş işlemlerini gerçekleştirebilirsiniz.
        // Örneğin, bir oyun ekranı göstermek veya oyunu sıfırlamak.

        // "Game Over" metnini ekrana ekleyen bir Label oluştur
        javafx.scene.text.Text gameOverText = new javafx.scene.text.Text("Game Over");
        gameOverText.setFill(Color.RED);
        gameOverText.setFont(Font.font("Arial", FontWeight.BOLD, 48));

        // Metni ekranın ortasına yerleştir
        double textX = (pane.getWidth() - gameOverText.getBoundsInLocal().getWidth()) / 2;
        double textY = (pane.getHeight() - gameOverText.getBoundsInLocal().getHeight()) / 2;
        gameOverText.setX(textX);
        gameOverText.setY(textY);

        // Game Over metnini pane'e ekle
        pane.getChildren().add(gameOverText);

        // Oyunu sıfırlamak için aşağıdaki satırı ekleyebilirsiniz.
        // resetGame();
    }


    public static void main(String[] args) {
        launch(args);
    }
}

 */