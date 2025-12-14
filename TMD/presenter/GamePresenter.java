package TMD.presenter;

import TMD.model.*;
import TMD.view.GamePanel;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.Timer;
import java.awt.Rectangle;

public class GamePresenter implements KeyListener, ActionListener {
    private GamePanel view;
    private Player player;
    private List<GameObject> aliens;
    private List<EnemyBullet> enemyBullets;
    private Timer gameTimer;
    
    // Status tombol ditekan
    private boolean leftPressed, rightPressed, downPressed, upPressed;

    public GamePresenter(GamePanel view) {
        this.view = view;
        this.aliens = new ArrayList<>();
        this.enemyBullets = new ArrayList<>();
        
        // POSISI AWAL: Di tanah (Y=450), Ukuran Besar (100x100)
        this.player = new Player(100, 450, 100, 100, 5); 

        this.view.addKeyListener(this);

        this.gameTimer = new Timer(16, this);
        this.gameTimer.start();
    }

    private void updateGame() {
        int screenWidth = view.getWidth();
        int screenHeight = view.getHeight();

        // 1. LOGIC SPAWN HANTU (Muncul dari ATAS)
        if (Math.random() < 0.02) { 
            int randomX = (int)(Math.random() * screenWidth);
            // Spawn di Y = -50 (di atas layar), supaya turun ke bawah
            aliens.add(new GameObject(randomX, -50, 50, 70, 2)); 
        }

        // 2. LOGIC GERAK HANTU (MENGEJAR PLAYER)
        for (int i = 0; i < aliens.size(); i++) {
            GameObject hantu = aliens.get(i);
            
            // Logic Kejar Sumbu X
            if (hantu.getX() < player.getX()) {
                hantu.setX(hantu.getX() + 1); // Ke Kanan
            } else {
                hantu.setX(hantu.getX() - 1); // Ke Kiri
            }

            // Logic Kejar Sumbu Y (Pelan-pelan turun)
            hantu.setY(hantu.getY() + hantu.speed); 
            
            // Hapus kalau sudah lewat bawah layar
            if (hantu.getY() > screenHeight) {
                aliens.remove(i);
                i--;
                player.setPeluruMeleset(player.getPeluruMeleset() + 1);
            }
        }

        // 3. Logic Musuh Menembak
        for (GameObject alien : aliens) {
            if (Math.random() < 0.005) { // Pelurunya agak jarang biar gak susah
                enemyBullets.add(new EnemyBullet(
                    alien.getX(), alien.getY(), 
                    player.getX(), player.getY() 
                ));
            }
        }

        // 4. Update Peluru Musuh
        for (int i = 0; i < enemyBullets.size(); i++) {
            EnemyBullet b = enemyBullets.get(i);
            b.update();

            // Cek Tabrakan dengan Player
            if (player.getBounds().intersects(b.getBounds())) {
                System.out.println("KENA PELURU! DARAH BERKURANG.");
                enemyBullets.remove(i);
                i--;
            } else if (b.isOffScreen(screenWidth, screenHeight)) {
                enemyBullets.remove(i);
                i--;
            }
        }
        
        // 5. UPDATE GERAK PLAYER (Input Keyboard)
        if (leftPressed) {
            player.setX(player.getX() - player.speed);
            view.isFacingRight = false;
        }
        if (rightPressed) {
            player.setX(player.getX() + player.speed);
            view.isFacingRight = true;
        }
        if (upPressed) player.setY(player.getY() - player.speed);
        if (downPressed) player.setY(player.getY() + player.speed);
        
        // Batasi Player biar gak keluar layar
        if (player.getX() < 0) player.setX(0);
        if (player.getX() > screenWidth - player.getWidth()) player.setX(screenWidth - player.getWidth());
        if (player.getY() < 0) player.setY(0);
        if (player.getY() > screenHeight - player.getHeight()) player.setY(screenHeight - player.getHeight());

        checkCollisions();
    }
    
    private void checkCollisions() {
        Rectangle pRect = player.getBounds();
        for (GameObject a : aliens) {
            if (pRect.intersects(a.getBounds())) {
                System.out.println("TABRAKAN SAMA HANTU!"); 
                // Opsional: gameTimer.stop();
            }
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        updateGame();
        view.render(player, aliens, enemyBullets);
    }

    // === INPUT KEYBOARD ===
    @Override
    public void keyPressed(KeyEvent e) {
        int code = e.getKeyCode();
        if (code == KeyEvent.VK_LEFT) leftPressed = true;
        if (code == KeyEvent.VK_RIGHT) rightPressed = true;
        if (code == KeyEvent.VK_UP) upPressed = true;     // Tambah Atas
        if (code == KeyEvent.VK_DOWN) downPressed = true; // Tambah Bawah
    }

    @Override
    public void keyReleased(KeyEvent e) {
        int code = e.getKeyCode();
        if (code == KeyEvent.VK_LEFT) leftPressed = false;
        if (code == KeyEvent.VK_RIGHT) rightPressed = false;
        if (code == KeyEvent.VK_UP) upPressed = false;
        if (code == KeyEvent.VK_DOWN) downPressed = false;
    }
    
    @Override public void keyTyped(KeyEvent e) {}
}