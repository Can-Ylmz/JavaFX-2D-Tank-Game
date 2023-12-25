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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TankGame extends Application {

    private Rectangle tank; // Tankı temsil eden dikdörtgen
    private Pane pane;
    private java.util.Random random = new java.util.Random();
    private boolean canFire = true;
    private Point2D tankPosition;
    private List<Circle> bots = new ArrayList<>();
    private boolean canFireBot = true;
    private final long botFireRate = 1_500_000_000; // Ateş etme hızı (saniye)
    private Map<Circle, Long> lastBotFireTimes = new HashMap<>();
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
            Circle bot = new Circle(botX, botY, 18, Color.RED);
            bot.setFill(Color.RED);
            bots.add(bot);
            pane.getChildren().add(bot);

            // Botların birbirine yakın olmasını engelle
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
                    canFire = false; // Ateş et
                    new java.util.Timer().schedule(new java.util.TimerTask() {
                        @Override
                        public void run() {
                            canFire = true;
                        }
                    },250);
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
        // Tankın mermisi botları vurup vurmadığını kontrol et
        checkBulletBotCollision();

        // Oyun durumunu güncelleme (gerektiğinde)
        tankPosition = new Point2D(tank.getX() + tank.getWidth() / 2, tank.getY() + tank.getHeight() / 2);

        // Botları güncelle
        for (Circle bot : bots) {
            moveBotTowardsTank(bot);

            // Bot için lastBotFireTime kontrolü
            if (!lastBotFireTimes.containsKey(bot)) {
                lastBotFireTimes.put(bot, 0L);
            }

            // Belirli bir süre geçtikten sonra bot ateş eder
            if (System.nanoTime() - lastBotFireTimes.get(bot) > botFireRate) {
                fireBot(bot);
                lastBotFireTimes.put(bot, System.nanoTime());
            }
        }

    }
    private void moveBotTowardsTank(Circle bot) {
        Point2D botPosition = new Point2D(bot.getCenterX(), bot.getCenterY());

        // Hedefe doğru vektörü bul
        Point2D direction = tankPosition.subtract(botPosition).normalize();

        // Hareket hızı
        double speed = 10;

        // Yeni konumu hesapla
        double newX = botPosition.getX() + direction.getX() * speed;
        double newY = botPosition.getY() /*direction.getY() * speed*/;

        // Yeni konumu uygula
        if (random.nextDouble() * 100 >= 1 && random.nextDouble() * 100<= 5) {
            bot.setCenterX(newX);
        }
        else if (random.nextDouble() * 100 >= 97 && random.nextDouble() * 100 <= 100) {
            bot.setCenterY(newY);
        }
    }

    // Botların mermisi tankı vurdu mu kontrolü
    private void checkBulletBotCollision() {
        double botSpacingX = 20; // Botlar arasındaki x eksenindeki boşluk miktarı
        double botSpacingY = 20; // Botlar arasındaki y eksenindeki boşluk miktarı

        for (int i = 0; i < bots.size(); i++) {
            Circle bot = bots.get(i);

            for (Node node : pane.getChildren()) {
                if (node instanceof Rectangle && node.getBoundsInParent().intersects(bot.getBoundsInParent())) {
                    // Mermi ve bot çarpışırsa
                    pane.getChildren().remove(node); // Mermiyi kaldır
                    canFire = true; // Yeni ateşe izin ver

                    // Botu belirli bir aralık içinde rastgele bir x ve y eksenine başlat
                    double minX = i * (botSpacingX + 2 * bot.getRadius()); // Minimum x eksen değeri
                    double maxX = (i + 20) * botSpacingX + i * 2 * bot.getRadius(); // Maximum x eksen değeri
                    double randomX = minX + Math.random() * (maxX - minX);

                    double minY = i * (botSpacingY + 2 * bot.getRadius()); // Minimum y eksen değeri
                    double maxY = (i + 10) * botSpacingY + i * 2 * bot.getRadius(); // Maximum y eksen değeri
                    double randomY = minY + Math.random() * (maxY - minY);

                    bot.setCenterX(randomX);
                    bot.setCenterY(randomY);
                }
            }
        }
    }


    // Bot mermisi ateşleme
    private void fireBot(Circle bot) {
        Circle bullet = new Circle(bot.getCenterX(), bot.getCenterY(), 5);
        bullet.setFill(Color.RED);
        pane.getChildren().add(bullet);

        startBulletAnimation(bullet, 2); // Mermi hareket hızı belirleme
    }

    //Mermi Animasyonu
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
        // Mevcut tüm çocukları temizle
        pane.getChildren().clear();
        pane.getChildren().removeIf(node -> node instanceof Bullet);
        
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
