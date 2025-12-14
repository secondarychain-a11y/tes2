package TMD.model;
import java.awt.Rectangle;

public class GameObject {
    public int x, y, width, height;
    public int speed;

    public GameObject(int x, int y, int w, int h, int s) {
        this.x = x; 
        this.y = y; 
        this.width = w; 
        this.height = h; 
        this.speed = s;
    }

    // Untuk deteksi tabrakan
    public Rectangle getBounds() {
        return new Rectangle(x, y, width, height);
    }
    
    // Getter Setter standar...
    public int getX() { return x; }
    public int getY() { return y; }
    public void setX(int x) { this.x = x; }
    public void setY(int y) { this.y = y; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }
    public void setWidth(int width) { this.width = width; }
    public void setHeight(int height) { this.height = height; }
    public int getSpeed() { return speed; }
    public void setSpeed(int speed) { this.speed = speed; }
    
}