package TMD.view;

import TMD.model.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import javax.swing.JPanel;
import java.util.List;

public class GamePanel extends JPanel {
    
    // --- STATE MANAGEMENT ---
    public enum State { MENU, PLAYING, GAME_OVER }
    public State gameState = State.PLAYING; 
    
    // --- ENTITIES (Hanya untuk digambar) ---
    private Player player;
    private List<GameObject> aliens;
    private List<EnemyBullet> enemyBullets;
    private List<Rectangle> playerBullets; // Tambahan untuk menerima data peluru player
    
    // --- GAMBAR ---
    private BufferedImage[] runRightAnim, runLeftAnim, idleRightAnim, idleLeftAnim;
    private BufferedImage[] ghostAnim;       
    private BufferedImage[] enemyBulletAnim; 
    
    private BackgroundHandler backgroundHandler;
    
    // --- ANIMASI ---
    private int animIndex = 0;
    private int animSpeed = 5;
    private int tick = 0;
    
    // Flag Visual (Diatur oleh Presenter/Key input)
    public boolean isFacingRight = true; 
    public boolean isPlayerMoving = false;
    private int score = 0;

    public GamePanel() {
        this.setPreferredSize(new Dimension(800, 600));
        this.setDoubleBuffered(true);
        this.setFocusable(true); // Tetap focusable agar keyListener di Presenter jalan
        
        backgroundHandler = new BackgroundHandler();
        loadImages();
    }

    // Method Render dipanggil oleh Presenter
    public void render(Player player, List<GameObject> aliens, List<EnemyBullet> enemyBullets, List<Rectangle> playerBullets, int score) {
        this.player = player;
        this.aliens = aliens;
        this.enemyBullets = enemyBullets;
        this.playerBullets = playerBullets;
        this.score = score;
        
        if (gameState == State.PLAYING) {
            updateAnimation();
        }
        repaint(); // Gambar ulang layar
    }
    
    public void setGameOver() {
        this.gameState = State.GAME_OVER;
        repaint();
    }

    private void loadImages() {
        try {
            ClassLoader cl = getClass().getClassLoader();
            
            // LOAD PLAYER
            BufferedImage runSheet = ImageIO.read(cl.getResource("assets/Walk.png"));
            BufferedImage idleSheet = ImageIO.read(cl.getResource("assets/Idle.png"));

            if (runSheet != null && idleSheet != null) {
                int runW = runSheet.getWidth() / 8;
                int idleW = idleSheet.getWidth() / 8;
                
                runRightAnim = new BufferedImage[8];
                runLeftAnim = new BufferedImage[8];
                idleRightAnim = new BufferedImage[8];
                idleLeftAnim = new BufferedImage[8];

                SpriteSheetHandler runH = new SpriteSheetHandler(runSheet);
                SpriteSheetHandler idleH = new SpriteSheetHandler(idleSheet);

                for (int i = 0; i < 8; i++) {
                    runRightAnim[i] = runH.crop(i, runW, runSheet.getHeight());
                    runLeftAnim[i] = flipImage(runRightAnim[i]);
                    idleRightAnim[i] = idleH.crop(i, idleW, idleSheet.getHeight());
                    idleLeftAnim[i] = flipImage(idleRightAnim[i]);
                }
            }
            
            // LOAD GHOST
            ghostAnim = new BufferedImage[11];
            for (int i = 0; i <= 10; i++) {
                String num = String.format("%02d", i);
                try { ghostAnim[i] = ImageIO.read(cl.getResource("assets/skeleton-animation_" + num + ".png")); } catch (Exception e){}
            }
            
            // LOAD BULLET (Gunakan salah satu aset api kamu, misal 004.png)
            BufferedImage bulletRaw = ImageIO.read(cl.getResource("assets/004.png"));
            enemyBulletAnim = new BufferedImage[1];
            enemyBulletAnim[0] = bulletRaw;
            
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

        if (gameState == State.GAME_OVER) {
            g.setColor(Color.BLACK);
            g.fillRect(0, 0, getWidth(), getHeight());
            g.setColor(Color.RED);
            g.setFont(new Font("Arial", Font.BOLD, 50));
            g.drawString("GAME OVER", 250, 200);
            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.PLAIN, 20));
            g.drawString("Score Akhir: " + score, 330, 280);
            return;
        }

        // --- DRAW GAME ---
        if (backgroundHandler != null) backgroundHandler.draw(g, getWidth(), getHeight());

        // Draw Player
        if (player != null && runRightAnim.length > 0) {
            BufferedImage[] currentAnim;
            if (isPlayerMoving) currentAnim = isFacingRight ? runRightAnim : runLeftAnim;
            else currentAnim = isFacingRight ? idleRightAnim : idleLeftAnim;
            
            int frameIndex = animIndex % currentAnim.length;
            if (currentAnim[frameIndex] != null) {
                g.drawImage(currentAnim[frameIndex], player.getX(), player.getY(), player.getWidth(), player.getHeight(), null);
            }
        }

        // Draw Hantu
        if (aliens != null && ghostAnim.length > 0) {
            for (GameObject alien : aliens) {
                int frame = animIndex % ghostAnim.length;
                if (ghostAnim[frame] != null)
                    g.drawImage(ghostAnim[frame], alien.getX(), alien.getY(), alien.getWidth(), alien.getHeight(), null);
            }
        }
        
        // Draw Peluru Musuh
        if (enemyBullets != null && enemyBulletAnim.length > 0) {
            for (EnemyBullet b : enemyBullets) {
                // Gambar peluru (animasi sederhana frame 0)
                g.drawImage(enemyBulletAnim[0], b.getX(), b.getY(), b.getWidth(), b.getHeight(), null);
            }
        }
        
        // Draw Peluru Player
        g.setColor(Color.YELLOW);
        if (playerBullets != null) {
            for (Rectangle bullet : playerBullets) {
                g.fillRect(bullet.x, bullet.y, bullet.width, bullet.height);
            }
        }

        // UI Score
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 18));
        g.drawString("Score: " + score, 20, 30);
    }

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

class SpriteSheetHandler {
    private BufferedImage sheet;
    public SpriteSheetHandler(BufferedImage sheet) { this.sheet = sheet; }
    public BufferedImage crop(int frameIndex, int width, int height) {
        return sheet.getSubimage(frameIndex * width, 0, width, height);
    }
}
