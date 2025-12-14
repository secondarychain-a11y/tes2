package TMD;

import TMD.model.TBenefitTable;
import TMD.presenter.MenuPresenter;
import TMD.view.MenuView;
import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        // Jalankan GUI di Thread yang aman (Best Practice Java Swing)
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    // 1. Siapkan Model (Koneksi Database)
                    TBenefitTable model = new TBenefitTable();

                    // 2. Siapkan View (Tampilan Menu Awal)
                    MenuView view = new MenuView();

                    // 3. Hubungkan dengan Presenter
                    new MenuPresenter(view, model);

                    // 4. Tampilkan Layar
                    view.setVisible(true);
                    
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }
}