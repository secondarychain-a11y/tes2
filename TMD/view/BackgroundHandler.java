package TMD.view;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class BackgroundHandler {
    private List<BufferedImage> layers;

    public BackgroundHandler() {
        layers = new ArrayList<>();
        loadLayers();
    }

    private void loadLayers() {
        try {
            // URUTAN SANGAT PENTING: Dari Belakang (Jauh) ke Depan (Dekat)
            
            // 1. Langit & Bulan (Paling Belakang)
            layers.add(ImageIO.read(getClass().getResource("/assets/Layer_0011_0.png")));
            
            // 2. Pohon-pohon Jauh (Tengah) - Kamu bisa pilih beberapa saja biar ga berat
            layers.add(ImageIO.read(getClass().getResource("/assets/Layer_0008_3.png")));
            layers.add(ImageIO.read(getClass().getResource("/assets/Layer_0006_4.png")));
            layers.add(ImageIO.read(getClass().getResource("/assets/Layer_0005_5.png")));
            
            // 3. Cahaya/Kabut (Biar mistis)
            layers.add(ImageIO.read(getClass().getResource("/assets/Layer_0004_Lights.png")));
            
            // 4. Pohon Dekat
            layers.add(ImageIO.read(getClass().getResource("/assets/Layer_0003_6.png")));
            
            // 5. Tanah/Rumput (Pijakan Player) - WAJIB Paling Akhir
            layers.add(ImageIO.read(getClass().getResource("/assets/Layer_0000_9.png")));
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Gagal load background! Cek nama file.");
        }
    }

    public void draw(Graphics g, int screenWidth, int screenHeight) {
        // Gambar semua layer satu per satu menumpuk
        for (BufferedImage img : layers) {
            if (img != null) {
                // Gambar ditarik (stretch) memenuhi layar
                g.drawImage(img, 0, 0, screenWidth, screenHeight, null);
            }
        }
    }
}