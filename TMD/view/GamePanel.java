package TMD.view;

import TMD.model.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import javax.swing.JPanel;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.sql.*;

public class GamePanel extends JPanel implements KeyListener { // Tambah KeyListener
    
    // --- STATE MANAGEMENT ---
    public enum State { MENU, PLAYING, GAME_OVER }
    public State gameState = State.MENU; // Mulai dari Menu
    
    // --- ENTITIES ---
    private Player player;
    private List<GameObject> aliens;
    private List<EnemyBullet> enemyBullets;
    // Tambahan untuk Peluru Player
    private List<Rectangle> playerBullets = new ArrayList<>(); 
    
    // --- GAMBAR ---
    private BufferedImage[] runRightAnim = new BufferedImage[0];
    private BufferedImage[] runLeftAnim = new BufferedImage[0];
    private BufferedImage[] idleRightAnim = new BufferedImage[0];
    private BufferedImage[] idleLeftAnim = new BufferedImage[0];
    private BufferedImage[] ghostAnim = new BufferedImage[0];       
    private BufferedImage[] enemyBulletAnim = new BufferedImage[0]; 
    
    private BackgroundHandler backgroundHandler;
    
    // --- ANIMASI & LOGIKA ---
    private int animIndex = 0;
    private int animSpeed = 5;
    private int tick = 0;
    private int enemyShootTimer = 0;
    
    // Flag Gerak Player
    public boolean isFacingRight = true; 
    private boolean isPlayerMoving = false; // Default diam
    private int score = 0;

    public GamePanel() {
        this.setPreferredSize(new Dimension(800, 600));
        this.setDoubleBuffered(true);
        this.setFocusable(true);
        this.requestFocusInWindow();
        
        // Pasang pendengar keyboard
        this.addKeyListener(this);
        
        backgroundHandler = new BackgroundHandler();
        loadImages();
    }

    // Method ini dipanggil terus menerus oleh loop game kamu (Presenter/Main)
    public void render(Player player, List<GameObject> aliens, List<EnemyBullet> enemyBullets) {
        this.player = player;
        this.aliens = aliens;
        this.enemyBullets = enemyBullets;
        
        // Hanya update animasi jika sedang bermain
        if (gameState == State.PLAYING) {
            updateAnimation();
            updateGameLogic(); // Update logika peluru & tabrakan
        }
        
        repaint();
    }
    
    // --- LOGIKA TAMBAHAN (Peluru & Tabrakan) ---
    private void updateGameLogic() {
        // --- 1. LOGIKA PELURU PLAYER (YANG LAMA) ---
        Iterator<Rectangle> it = playerBullets.iterator();
        while (it.hasNext()) {
            Rectangle bullet = it.next();
            bullet.x += 10; // Peluru player ke kanan
            
            if (bullet.x > 800) it.remove();
            else {
                // Cek Kena Musuh
                if (aliens != null) {
                    Iterator<GameObject> alienIt = aliens.iterator();
                    while (alienIt.hasNext()) {
                        GameObject alien = alienIt.next();
                        // Bikin kotak hitung tabrakan (Hitbox)
                        Rectangle alienRect = new Rectangle(alien.getX(), alien.getY(), alien.getWidth(), alien.getHeight());
                        
                        if (bullet.intersects(alienRect)) {
                            alienIt.remove(); // Musuh mati
                            it.remove();      // Peluru hilang
                            score += 10;      // Tambah skor
                            break;
                        }
                    }
                }
            }
        }
        
        // --- 2. LOGIKA HANTU NEMBAK (BARU) ---
        // Pastikan list enemyBullets tidak null. Jika null, inisialisasi dulu.
        if (enemyBullets == null) enemyBullets = new ArrayList<>();

        enemyShootTimer++;
        if (enemyShootTimer >= 100) { // Setiap +/- 1.5 detik (tergantung FPS)
            if (aliens != null) {
                for (GameObject alien : aliens) {
                    // Hantu nembak peluru baru di posisi dia
                    // Asumsi class EnemyBullet punya constructor (x, y, width, height)
                    // Jika class EnemyBullet kamu beda, sesuaikan parameternya
                    enemyBullets.add(new EnemyBullet(alien.getX(), alien.getY() + 20, 30, 10));
                }
            }
            enemyShootTimer = 0; // Reset timer
        }

        // --- 3. GERAKAN PELURU MUSUH & TABRAKAN KE PLAYER ---
        if (enemyBullets != null) {
            Iterator<EnemyBullet> bulletIt = enemyBullets.iterator();
            while (bulletIt.hasNext()) {
                EnemyBullet eb = bulletIt.next();
                eb.setX(eb.getX() - 7); // Gerak ke kiri mendekati player

                // Hapus kalau keluar layar kiri
                if (eb.getX() < 0) {
                    bulletIt.remove();
                } else {
                    // Cek apakah kena Player?
                    if (player != null) {
                        Rectangle playerRect = new Rectangle(player.getX(), player.getY(), player.getWidth(), player.getHeight());
                        Rectangle bulletRect = new Rectangle(eb.getX(), eb.getY(), eb.getWidth(), eb.getHeight());

                        if (playerRect.intersects(bulletRect)) {
                            gameOver(); // GAME OVER JIKA KENA TEMBAK
                        }
                    }
                }
            }
        }
        
        // --- 4. TABRAKAN BADAN (Player vs Hantu) ---
        if (player != null && aliens != null) {
            Rectangle playerRect = new Rectangle(player.getX(), player.getY(), player.getWidth(), player.getHeight());
            for (GameObject alien : aliens) {
                Rectangle alienRect = new Rectangle(alien.getX(), alien.getY(), alien.getWidth(), alien.getHeight());
                if (playerRect.intersects(alienRect)) {
                    gameOver();
                }
            }
        }
    }
    
    private void gameOver() {
        gameState = State.GAME_OVER;
        saveScoreToDB(score); // Simpan ke database
    }

    // --- DATABASE ---
    private void saveScoreToDB(int finalScore) {
        // Ganti config database sesuai XAMPP kamu
        String url = "jdbc:mysql://localhost:3306/db_hideandseek"; // Nama DB
        String user = "root";
        String pass = "";

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection con = DriverManager.getConnection(url, user, pass);
            String sql = "INSERT INTO highscore (score, date_played) VALUES (?, NOW())";
            PreparedStatement pst = con.prepareStatement(sql);
            pst.setInt(1, finalScore);
            pst.executeUpdate();
            System.out.println("Skor tersimpan: " + finalScore);
            con.close();
        } catch (Exception e) {
            System.err.println("Database Error (Abaikan jika belum setup DB): " + e.getMessage());
        }
    }

    // --- LOAD IMAGE (TIDAK BERUBAH BANYAK) ---
    private void loadImages() {
        try {
            ClassLoader cl = getClass().getClassLoader();
            // Player
            BufferedImage runSheet = ImageIO.read(cl.getResource("assets/Walk.png"));
            BufferedImage idleSheet = ImageIO.read(cl.getResource("assets/Idle.png"));

            if (runSheet != null && idleSheet != null) {
                int runFrameWidth = runSheet.getWidth() / 8;
                int idleFrameWidth = idleSheet.getWidth() / 8;
                
                runRightAnim = new BufferedImage[8];
                runLeftAnim = new BufferedImage[8];
                idleRightAnim = new BufferedImage[8];
                idleLeftAnim = new BufferedImage[8];

                SpriteSheetHandler runHandler = new SpriteSheetHandler(runSheet);
                SpriteSheetHandler idleHandler = new SpriteSheetHandler(idleSheet);

                for (int i = 0; i < 8; i++) {
                    runRightAnim[i] = runHandler.crop(i, runFrameWidth, runSheet.getHeight());
                    runLeftAnim[i] = flipImage(runRightAnim[i]); // Flip otomatis
                    
                    idleRightAnim[i] = idleHandler.crop(i, idleFrameWidth, idleSheet.getHeight());
                    idleLeftAnim[i] = flipImage(idleRightAnim[i]); // Flip otomatis
                }
            }
            
            // Ghost
            ghostAnim = new BufferedImage[11];
            for (int i = 0; i <= 10; i++) {
                String number = String.format("%02d", i);
                try { ghostAnim[i] = ImageIO.read(cl.getResource("assets/skeleton-animation_" + number + ".png")); } 
                catch (Exception e) {}
            }
            
            // Enemy Bullet (Placeholder jika error)
            // ... load bullet code ...
            
        } catch (Exception e) {
            System.err.println("Error Loading Images: " + e.getMessage());
        }
    }

    public void updateAnimation() {
        tick++;
        if (tick >= animSpeed) {
            tick = 0;
            animIndex++;
            if (animIndex >= 8) animIndex = 0; 
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        // 1. STATE: MENU
        if (gameState == State.MENU) {
            g.setColor(Color.BLACK);
            g.fillRect(0, 0, getWidth(), getHeight());
            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.BOLD, 40));
            g.drawString("HIDE AND SEEK", 250, 200);
            g.setFont(new Font("Arial", Font.PLAIN, 20));
            g.drawString("Tekan ENTER untuk Main", 280, 300);
            return; // Stop gambar yang lain
        }

        // 2. STATE: GAME OVER
        if (gameState == State.GAME_OVER) {
            g.setColor(Color.BLACK);
            g.fillRect(0, 0, getWidth(), getHeight());
            g.setColor(Color.RED);
            g.setFont(new Font("Arial", Font.BOLD, 50));
            g.drawString("GAME OVER", 250, 200);
            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.PLAIN, 20));
            g.drawString("Score Akhir: " + score, 330, 280);
            g.drawString("Tekan ENTER untuk Menu", 290, 350);
            return;
        }

        // 3. STATE: PLAYING (Gambar Game Asli)
        if (backgroundHandler != null) {
            backgroundHandler.draw(g, getWidth(), getHeight());
        }

        // Gambar Player
        if (player != null && runRightAnim.length > 0) {
            BufferedImage[] currentAnim;
            
            // LOGIKA FIX IDLE: Gunakan flag isPlayerMoving
            if (isPlayerMoving) {
                currentAnim = isFacingRight ? runRightAnim : runLeftAnim;
            } else {
                currentAnim = isFacingRight ? idleRightAnim : idleLeftAnim;
            }
            
            int frameIndex = animIndex % currentAnim.length;
            // Pastikan tidak null
            if (currentAnim[frameIndex] != null) {
                g.drawImage(currentAnim[frameIndex], player.getX(), player.getY(), player.getWidth(), player.getHeight(), null);
            }
        }

        // Gambar Hantu
        if (aliens != null && ghostAnim.length > 0) {
            for (GameObject alien : aliens) {
                // Pastikan array ghostAnim tidak kosong
                if (ghostAnim.length > 0) {
                    int ghostFrame = animIndex % ghostAnim.length; 
                    if (ghostAnim[ghostFrame] != null)
                        g.drawImage(ghostAnim[ghostFrame], alien.getX(), alien.getY(), alien.getWidth(), alien.getHeight(), null);
                }
            }
        }
        
        // Gambar Peluru Player
        g.setColor(Color.YELLOW);
        for (Rectangle bullet : playerBullets) {
            g.fillRect(bullet.x, bullet.y, bullet.width, bullet.height);
        }

        // UI Score
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 18));
        g.drawString("Score: " + score, 20, 30);
    }

    // --- INPUT KEYBOARD ---
    @Override
    public void keyPressed(KeyEvent e) {
        int key = e.getKeyCode();

        if (gameState == State.MENU) {
            if (key == KeyEvent.VK_ENTER) {
                gameState = State.PLAYING;
                score = 0;
                playerBullets.clear();
                // Reset posisi player jika perlu
            }
        } 
        else if (gameState == State.PLAYING) {
            // Gerak
            if (key == KeyEvent.VK_D || key == KeyEvent.VK_RIGHT) {
                isPlayerMoving = true;
                isFacingRight = true;
                if(player != null) player.setX(player.getX() + 5); // Gerak manual disini atau lewat Presenter
            }
            if (key == KeyEvent.VK_A || key == KeyEvent.VK_LEFT) {
                isPlayerMoving = true;
                isFacingRight = false;
                if(player != null) player.setX(player.getX() - 5);
            }
            // Nembak
            if (key == KeyEvent.VK_SPACE) {
                if (player != null) {
                    // Buat peluru baru
                    playerBullets.add(new Rectangle(player.getX() + 20, player.getY() + 20, 10, 5));
                }
            }
            // Pause/Menu
            if (key == KeyEvent.VK_ESCAPE) {
                gameState = State.MENU;
            }
        }
        else if (gameState == State.GAME_OVER) {
            if (key == KeyEvent.VK_ENTER) {
                gameState = State.MENU;
            }
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        if (gameState == State.PLAYING) {
            int key = e.getKeyCode();
            // Stop animasi jalan saat tombol dilepas
            if (key == KeyEvent.VK_D || key == KeyEvent.VK_A || 
                key == KeyEvent.VK_RIGHT || key == KeyEvent.VK_LEFT) {
                isPlayerMoving = false;
            }
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {}

    private BufferedImage flipImage(BufferedImage original) {
        int w = original.getWidth();
        int h = original.getHeight();
        BufferedImage flipped = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = flipped.createGraphics();
        g2.drawImage(original, w, 0, -w, h, null);
        g2.dispose();
        return flipped;
    }
}

// Class Helper sederhana
class SpriteSheetHandler {
    private BufferedImage sheet;
    public SpriteSheetHandler(BufferedImage sheet) { this.sheet = sheet; }
    public BufferedImage crop(int frameIndex, int width, int height) {
        return sheet.getSubimage(frameIndex * width, 0, width, height);
    }
}