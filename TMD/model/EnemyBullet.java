package TMD.model;

import java.awt.Rectangle;

public class EnemyBullet extends GameObject {
    // Variabel untuk pergerakan vector (arah)
    private double dx;
    private double dy;
    
    // Variabel Animasi
    private int animIndex = 0;
    private int animDelay = 0;
    
    // Constructor
    public EnemyBullet(int startX, int startY, int targetX, int targetY) {
        // Posisi awal, ukuran 30x30, speed 4
        super(startX, startY, 30, 30, 4); 
        
        calculateDirection(targetX, targetY);
    }

    // Hitung sudut supaya peluru bergerak menuju posisi Player saat ditembakkan
    private void calculateDirection(int targetX, int targetY) {
        double angle = Math.atan2(targetY - y, targetX - x);
        dx = Math.cos(angle) * speed;
        dy = Math.sin(angle) * speed;
    }

    public void update() {
        // 1. Update Posisi
        x += dx;
        y += dy;

        // 2. Update Animasi (Ganti frame setiap 3 tick)
        animDelay++;
        if (animDelay >= 3) {
            animIndex++;
            if (animIndex >= 10) { // Karena ada 10 gambar (0-9)
                animIndex = 0;
            }
            animDelay = 0;
        }
    }

    public int getAnimIndex() {
        return animIndex;
    }
    
    // Helper untuk mengecek apakah peluru sudah keluar layar (Meleset)
    // Asumsi layar 800x600, kita kasih margin sedikit
    public boolean isOffScreen(int screenWidth, int screenHeight) {
        return x < -50 || x > screenWidth + 50 || y < -50 || y > screenHeight + 50;
    }
}