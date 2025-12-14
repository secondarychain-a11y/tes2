package TMD.model;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TBenefitTable {
    // Koneksi DB (sesuaikan user/pass)
    private String url = "jdbc:mysql://localhost:3306/hide_seek_game";
    private String user = "root"; 
    private String pass = "";

    public List<Player> getAllPlayers() {
        List<Player> list = new ArrayList<>();
        // Tulis logika SELECT * FROM tbenefit di sini
        // Masukkan hasilnya ke dalam list
        return list;
    }

    public void addOrUpdatePlayer(Player p) {
        // Logika INSERT atau UPDATE sesuai spesifikasi [cite: 115]
    }
}