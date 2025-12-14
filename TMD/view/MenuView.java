package TMD.view;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.List;
import TMD.model.Player;

public class MenuView extends JFrame {
    private JTable table;
    private DefaultTableModel tableModel;
    private JTextField tfUsername;
    private JButton btnPlay, btnQuit;

    public MenuView() {
        setTitle("Hide and Seek The Challenge");
        setSize(600, 500);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // 1. Setup Tabel
        String[] columns = {"Username", "Skor", "Peluru Meleset", "Sisa Peluru"};
        tableModel = new DefaultTableModel(columns, 0);
        table = new JTable(tableModel);
        add(new JScrollPane(table), BorderLayout.CENTER);

        // 2. Setup Input & Tombol (Bagian Bawah)
        JPanel panelBottom = new JPanel(new GridLayout(3, 1));
        
        JPanel panelUser = new JPanel();
        panelUser.add(new JLabel("Username:"));
        tfUsername = new JTextField(20);
        panelUser.add(tfUsername);
        
        JPanel panelBtn = new JPanel();
        btnPlay = new JButton("Play");
        btnQuit = new JButton("Quit");
        panelBtn.add(btnPlay);
        panelBtn.add(btnQuit);

        panelBottom.add(panelUser);
        panelBottom.add(panelBtn);
        add(panelBottom, BorderLayout.SOUTH);
    }

    // Method untuk mengisi tabel dari luar (akan dipanggil Presenter)
    public void setTableData(List<Player> players) {
        tableModel.setRowCount(0); // Reset data
        for (Player p : players) {
            tableModel.addRow(new Object[]{
                p.getUsername(), p.getSkor(), p.getPeluruMeleset(), p.getSisaPeluru()
            });
        }
    }
    
    public String getUsernameInput() {
        return tfUsername.getText();
    }

    // Listener untuk tombol (supaya Presenter bisa tahu kalau tombol ditekan)
    public void setPlayAction(ActionListener a) {
        btnPlay.addActionListener(a);
    }
}
