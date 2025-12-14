package TMD.presenter;

import TMD.model.*;
import TMD.view.GamePanel;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;
import javax.swing.Timer;
import java.awt.Rectangle;

public class GamePresenter implements KeyListener, ActionListener {
    private GamePanel view;
    private Player player;
    private List<GameObject> aliens;
    private List<EnemyBullet> enemyBullets;
    private List<Rectangle> playerBullets; // List peluru player
    private Timer gameTimer;
    
    // Status tombol
    private boolean leftPressed, rightPressed, downPressed, upPressed;

    public GamePresenter(GamePanel view) {
        this.view = view;
        this.aliens = new ArrayList<>();
        this.enemyBullets = new ArrayList<>();
        this.playerBullets = new ArrayList<>();
        
        // Setup Player
        this.player = new Player(100, 450, 100, 100, 5); 

        // Presenter yang mendengarkan Keyboard, BUKAN Panel
        this.view.addKeyListener(this);

        this.gameTimer = new Timer(16, this); // ~60 FPS
        this.gameTimer.start();
    }

    private void updateGame() {
        if (view.gameState == GamePanel.State.GAME_OVER) return;

        int screenWidth = view.getWidth();
        int screenHeight = view.getHeight();

        // --- 1. SPAWN HANTU ---
        if (Math.random() < 0.02) { 
            int randomX = (int)(Math.random() * (screenWidth - 100));
            aliens.add(new GameObject(randomX, -50, 60, 80, 2)); 
        }

        // --- 2. LOGIKA GERAK HANTU ---
        Iterator<GameObject> alienIt = aliens.iterator();
        while (alienIt.hasNext()) {
            GameObject hantu = alienIt.next();
            
            // Kejar Player (Sumbu X)
            if (hantu.getX() < player.getX()) hantu.setX(hantu.getX() + 1);
            else hantu.setX(hantu.getX() - 1);

            // Turun ke bawah (Sumbu Y)
            hantu.setY(hantu.getY() + hantu.speed); 
            
            // Hapus jika lewat bawah
            if (hantu.getY() > screenHeight) {
                alienIt.remove();
                player.setPeluruMeleset(player.getPeluruMeleset() + 1);
            }
            // Tabrakan Badan Hantu vs Player
            else if (player.getBounds().intersects(hantu.getBounds())) {
                view.setGameOver();
            }
        }

        // --- 3. LOGIKA HANTU NEMBAK ---
        for (GameObject alien : aliens) {
            // Random kecil agar tidak nembak terus menerus
            if (Math.random() < 0.01) { 
                // Constructor EnemyBullet kamu: (startX, startY, targetX, targetY)
                // Ini akan membuat peluru bergerak miring menuju posisi player saat ini
                enemyBullets.add(new EnemyBullet(
                    alien.getX() + 20, alien.getY() + 40, 
                    player.getX() + 50, player.getY() + 50
                ));
            }
        }

        // --- 4. UPDATE PELURU MUSUH ---
        Iterator<EnemyBullet> bulletIt = enemyBullets.iterator();
        while (bulletIt.hasNext()) {
            EnemyBullet b = bulletIt.next();
            b.update(); // Method update() di model EnemyBullet mengurus gerak vektor

            // Cek Tabrakan Peluru Musuh vs Player
            if (player.getBounds().intersects(b.getBounds())) {
                view.setGameOver();
            } 
            // Hapus jika keluar layar
            else if (b.isOffScreen(screenWidth, screenHeight)) {
                bulletIt.remove();
            }
        }

        // --- 5. UPDATE PELURU PLAYER ---
        Iterator<Rectangle> pbIt = playerBullets.iterator();
        while (pbIt.hasNext()) {
            Rectangle pb = pbIt.next();
            // Tentukan arah peluru player (misal ke kanan atau sesuai hadap)
            // Sederhana: tembak lurus ke kanan
            pb.x += 10;

            if (pb.x > screenWidth) {
                pbIt.remove(); // Hapus jika keluar layar kanan
            } else {
                // Cek kena Hantu
                boolean hit = false;
                for (int i = 0; i < aliens.size(); i++) {
                    GameObject alien = aliens.get(i);
                    if (pb.intersects(alien.getBounds())) {
                        aliens.remove(i); // Hapus hantu
                        player.setSkor(player.getSkor() + 10); // Tambah skor
                        hit = true;
                        break;
                    }
                }
                if (hit) pbIt.remove(); // Hapus peluru jika kena
            }
        }
        
        // --- 6. UPDATE GERAK PLAYER (Keyboard) ---
        view.isPlayerMoving = false; // Reset status idle
        if (leftPressed) {
            player.setX(player.getX() - player.speed);
            view.isFacingRight = false;
            view.isPlayerMoving = true;
        }
        if (rightPressed) {
            player.setX(player.getX() + player.speed);
            view.isFacingRight = true;
            view.isPlayerMoving = true;
        }
        if (upPressed) {
            player.setY(player.getY() - player.speed);
            view.isPlayerMoving = true;
        }
        if (downPressed) {
            player.setY(player.getY() + player.speed);
            view.isPlayerMoving = true;
        }
        
        // Batasi Player dalam layar
        if (player.getX() < 0) player.setX(0);
        if (player.getX() > screenWidth - player.getWidth()) player.setX(screenWidth - player.getWidth());
        if (player.getY() < 0) player.setY(0);
        if (player.getY() > screenHeight - player.getHeight()) player.setY(screenHeight - player.getHeight());
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        updateGame();
        // Kirim semua data terbaru ke View untuk digambar
        view.render(player, aliens, enemyBullets, playerBullets, player.getSkor());
    }

    // === INPUT KEYBOARD ===
    @Override
    public void keyPressed(KeyEvent e) {
        int code = e.getKeyCode();
        if (view.gameState == GamePanel.State.PLAYING) {
            if (code == KeyEvent.VK_A || code == KeyEvent.VK_LEFT) leftPressed = true;
            if (code == KeyEvent.VK_D || code == KeyEvent.VK_RIGHT) rightPressed = true;
            if (code == KeyEvent.VK_W || code == KeyEvent.VK_UP) upPressed = true;
            if (code == KeyEvent.VK_S || code == KeyEvent.VK_DOWN) downPressed = true;
            
            // LOGIKA PLAYER NEMBAK (SPACE)
            if (code == KeyEvent.VK_SPACE) {
                // Spawn peluru di tengah badan player
                playerBullets.add(new Rectangle(player.getX() + 50, player.getY() + 40, 20, 10));
                player.setSisaPeluru(player.getSisaPeluru() - 1); // Opsional kalau ada ammo
            }
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        int code = e.getKeyCode();
        if (code == KeyEvent.VK_A || code == KeyEvent.VK_LEFT) leftPressed = false;
        if (code == KeyEvent.VK_D || code == KeyEvent.VK_RIGHT) rightPressed = false;
        if (code == KeyEvent.VK_W || code == KeyEvent.VK_UP) upPressed = false;
        if (code == KeyEvent.VK_S || code == KeyEvent.VK_DOWN) downPressed = false;
    }
    
    @Override public void keyTyped(KeyEvent e) {}
}
