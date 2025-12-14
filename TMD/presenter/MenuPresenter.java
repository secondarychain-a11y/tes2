package TMD.presenter;

import TMD.model.TBenefitTable;
import TMD.view.MenuView;
import TMD.view.GamePanel;           // Import baru
import javax.swing.JFrame;           // Import baru
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class MenuPresenter {
    private MenuView view;
    private TBenefitTable model;

    public MenuPresenter(MenuView view, TBenefitTable model) {
        this.view = view;
        this.model = model;

        loadData();

        this.view.setPlayAction(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String username = view.getUsernameInput();
                
                if (!username.isEmpty()) {
                    System.out.println("Mulai game dengan: " + username);
                    
                    // === LOGIKA MEMBUKA WINDOW GAME ===
                    
                    // 1. Tutup Menu Lama
                    view.dispose(); 
                    
                    // 2. Buat Jendela Baru untuk Game
                    JFrame gameWindow = new JFrame("Hide and Seek - Playing as " + username);
                    gameWindow.setSize(800, 600); // Sesuaikan ukuran layar
                    gameWindow.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                    gameWindow.setResizable(false);
                    
                    // 3. Pasang GamePanel (View)
                    GamePanel gamePanel = new GamePanel();
                    gameWindow.add(gamePanel);
                    gameWindow.pack(); // Menyesuaikan ukuran window dengan panel
                    gameWindow.setLocationRelativeTo(null); // Tengah layar
                    gameWindow.setVisible(true);
                    
                    // 4. Jalankan GamePresenter (Logic)
                    // GamePresenter akan otomatis menjalankan Timer game
                    new GamePresenter(gamePanel); 
                    
                    // Fokuskan ke panel supaya tombol keyboard terbaca
                    gamePanel.requestFocusInWindow();
                } else {
                    System.out.println("Username tidak boleh kosong!");
                    // Bisa tambahkan JOptionPane.showMessageDialog warning di sini
                }
            }
        });
    }

    private void loadData() {
        view.setTableData(model.getAllPlayers());
    }
}