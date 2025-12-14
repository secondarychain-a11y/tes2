package TMD.model;

// 1. Tambahkan "extends GameObject" supaya Player punya X, Y, dan Speed
public class Player extends GameObject {
    
    // Variabel Database
    private String username;
    private int skor;
    private int peluruMeleset;
    private int sisaPeluru;

    // 2. CONSTRUCTOR UNTUK GAME (Yang dipanggil di GamePresenter)
    // Menerima posisi dan kecepatan
    public Player(int x, int y, int width, int height, int speed) {
        super(x, y, width, height, speed); // Oper ke GameObject
        this.skor = 0;
        this.peluruMeleset = 0;
        this.sisaPeluru = 0;
    }

    // 3. CONSTRUCTOR KOSONG (Untuk Database/Menu)
    // Penting supaya TBenefitTable tidak error saat bikin list player
    public Player() {
        super(0, 0, 0, 0, 0); // Posisi dummy
    }

    // Getter & Setter Database
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public int getSkor() { return skor; }
    public void setSkor(int skor) { this.skor = skor; }

    public int getPeluruMeleset() { return peluruMeleset; }
    public void setPeluruMeleset(int peluruMeleset) { this.peluruMeleset = peluruMeleset; }

    public int getSisaPeluru() { return sisaPeluru; }
    public void setSisaPeluru(int sisaPeluru) { this.sisaPeluru = sisaPeluru; }
    
    // Method helper untuk menambah counter (biar kode Presenter lebih rapi)
    public void tambahPeluruMeleset() {
        this.peluruMeleset++;
    }
    
    public void tambahSisaPeluru(int jumlah) {
        this.sisaPeluru += jumlah;
    }
}