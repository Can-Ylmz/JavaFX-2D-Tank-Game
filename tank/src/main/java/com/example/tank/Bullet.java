package com.example.tank;

import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

public class Bullet extends Circle {

    private static final double SPEED = 5;

    public Bullet(double x, double y) {
        super(x, y, 5, Color.RED);
    }

    public Bullet(double centerX, double centerY, int bulletRadius, Color red, double bulletSpeed) {
    }

    public void move() {
        setCenterY(getCenterY() - SPEED);
    }

    public boolean isOutOfScreen() {
        return getCenterY() < 0;
    }
}
