package com.mycompany.reproductormusicapoo;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

public class ReproductorGUI extends JFrame {

    // Componentes de la interfaz
    private JComboBox<String> cbPlaylists;
    private JSlider progressSlider;
    private JLabel lblAlbumImage;
    private JButton btnPrev, btnNext, btnStart, btnStop, btnPause;
    private JButton btnBuscarOnline, btnPlayOnline, btnSalir;
    
    // Servicios y Repositorios
    private final LocalMusicaRepositorio localRepository = new LocalMusicaRepositorio();
    private final MusicApiService apiService = new MusicApiService();
    private ServicioAudio servicioAudio = new ServicioAudio();
    private List<Cancion> actualPlaylist;
    private Cancion cancionActual;
    
    private Timer barraTimer; 
    private int indiceActual = 0; // CONTROL DE LA CANCIÓN ACTUAL EN LA LISTA

    // Variables para el fondo animado
    private float hue = 0.0f;

    public ReproductorGUI() {
        // 1. CONFIGURACIÓN DE LA VENTANA
        setTitle("Reproductor de Música");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // 2. PANEL PRINCIPAL Y FONDO ANIMADO
        JPanel mainPanel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                Color color1 = Color.getHSBColor(hue, 0.8f, 0.6f);
                Color color2 = Color.getHSBColor(hue + 0.2f, 0.8f, 0.4f);
                GradientPaint gp = new GradientPaint(0, 0, color1, getWidth(), getHeight(), color2);
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };

        Timer timer = new Timer(50, e -> {
            hue += 0.005f;
            if (hue > 1.0f) {
                hue = 0.0f;
            }
            mainPanel.repaint();
        });
        timer.start();

        // 3. ZONA SUPERIOR: Imagen del Álbum
        lblAlbumImage = new JLabel("Imagen de la Canción", SwingConstants.CENTER);
        lblAlbumImage.setPreferredSize(new Dimension(300, 300));
        lblAlbumImage.setForeground(Color.WHITE);
        lblAlbumImage.setFont(new Font("Arial", Font.BOLD, 24));
        mainPanel.add(lblAlbumImage, BorderLayout.CENTER);

        // 4. ZONA INFERIOR: Controles
        JPanel controlsPanel = new JPanel();
        controlsPanel.setOpaque(false);
        controlsPanel.setLayout(new BoxLayout(controlsPanel, BoxLayout.Y_AXIS));

        // Fila 1: Prev | ComboBox | Next
        JPanel row1 = new JPanel(new FlowLayout(FlowLayout.CENTER, 30, 10));
        row1.setOpaque(false);
        btnPrev = new JButton("prev");
        String[] playlists = {"Rock", "Pop", "Anime (Evangelion, HxH, JoJo's)"};
        cbPlaylists = new JComboBox<>(playlists);
        btnNext = new JButton("Next");
        row1.add(btnPrev);
        row1.add(cbPlaylists);
        row1.add(btnNext);

        // Fila 2: Slider
        JPanel row2 = new JPanel(new FlowLayout(FlowLayout.CENTER));
        row2.setOpaque(false);
        progressSlider = new JSlider(0, 100, 0);
        progressSlider.setPreferredSize(new Dimension(400, 40));
        progressSlider.setOpaque(false);
        row2.add(progressSlider);

        // Fila 3: Start | Stop | Pause
        JPanel row3 = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        row3.setOpaque(false);
        btnStart = new JButton("Start");
        btnStop = new JButton("Stop");
        btnPause = new JButton("Pause");
        row3.add(btnStart);
        row3.add(btnStop);
        row3.add(btnPause);

        // Fila 4: Buscar Online | PlayOnline | Salir
        JPanel row4 = new JPanel(new BorderLayout());
        row4.setOpaque(false);
        row4.setBorder(BorderFactory.createEmptyBorder(10, 20, 20, 20));

        JPanel leftBottom = new JPanel(new FlowLayout(FlowLayout.LEFT));
        leftBottom.setOpaque(false);
        btnBuscarOnline = new JButton("Buscar Online");
        btnPlayOnline = new JButton("PlayOnline");
        leftBottom.add(btnBuscarOnline);
        leftBottom.add(btnPlayOnline);

        JPanel rightBottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        rightBottom.setOpaque(false);
        btnSalir = new JButton("Salir");
        rightBottom.add(btnSalir);

        row4.add(leftBottom, BorderLayout.WEST);
        row4.add(rightBottom, BorderLayout.EAST);

        controlsPanel.add(row1);
        controlsPanel.add(row2);
        controlsPanel.add(row3);
        controlsPanel.add(row4);

        mainPanel.add(controlsPanel, BorderLayout.SOUTH);
        setContentPane(mainPanel);

        // 5. INICIALIZAR LÓGICA Y EVENTOS
        configurarEventos();
        cargarPlaylistInicial();
    }

    private void configurarEventos() {
        
        // Timer de la barra de progreso
        barraTimer = new Timer(1000, e -> {
            if (progressSlider.getValue() < progressSlider.getMaximum()) {
                progressSlider.setValue(progressSlider.getValue() + 1);
            } else {
                barraTimer.stop();
            }
        });

        // ComboBox de Listas Locales
        cbPlaylists.addActionListener(e -> {
            String seleccion = (String) cbPlaylists.getSelectedItem();
            String genero = seleccion.split(" ")[0].toLowerCase();
            actualPlaylist = localRepository.getPlaylistByGenero(genero);
            indiceActual = 0; // Al cambiar de género, reiniciamos al primer elemento

            if (actualPlaylist != null && !actualPlaylist.isEmpty()) {
                cargarCancionEnPantalla(actualPlaylist.get(0));
            }
        });

        // Botón Next (Siguiente canción)
        btnNext.addActionListener(e -> {
            if (actualPlaylist != null && !actualPlaylist.isEmpty()) {
                indiceActual++;
                if (indiceActual >= actualPlaylist.size()) {
                    indiceActual = 0; // Bucle: regresa a la primera si llegó al final
                }
                
                // Si estaba sonando música, paramos la anterior y reproducimos la nueva automáticamente
                boolean estabaReproduciendo = barraTimer.isRunning();
                cargarCancionEnPantalla(actualPlaylist.get(indiceActual));
                
                if (estabaReproduciendo) {
                    servicioAudio.play(cancionActual.getRutaArchivo());
                    barraTimer.start();
                }
            }
        });

        // Botón Prev (Canción anterior)
        btnPrev.addActionListener(e -> {
            if (actualPlaylist != null && !actualPlaylist.isEmpty()) {
                indiceActual--;
                if (indiceActual < 0) {
                    indiceActual = actualPlaylist.size() - 1; // Bucle: va a la última si retrocede de la primera
                }
                
                boolean estabaReproduciendo = barraTimer.isRunning();
                cargarCancionEnPantalla(actualPlaylist.get(indiceActual));
                
                if (estabaReproduciendo) {
                    servicioAudio.play(cancionActual.getRutaArchivo());
                    barraTimer.start();
                }
            }
        });

        // Botón Start
        btnStart.addActionListener(e -> {
            if (cancionActual != null) {
                servicioAudio.play(cancionActual.getRutaArchivo());
                barraTimer.start();
            } else {
                System.out.println("Selecciona una lista o busca una canción primero.");
            }
        });

        // Botón Stop (Detiene y Reinicia)
        btnStop.addActionListener(e -> {
            servicioAudio.stop();
            barraTimer.stop();      
            progressSlider.setValue(0); 
        });

        // Botón Pause
        btnPause.addActionListener(e -> {
            servicioAudio.stop();
            barraTimer.stop(); 
        });

        // Botón Salir
        btnSalir.addActionListener(e -> System.exit(0));

        // Botón Buscar Online
        btnBuscarOnline.addActionListener(e -> {
            String busqueda = JOptionPane.showInputDialog(this, "Introduce el nombre de la canción o artista:");
            if (busqueda != null && !busqueda.trim().isEmpty()) {
                lblAlbumImage.setText("Buscando en la API...");

                apiService.buscarCancionOnline(busqueda, new MusicApiService.ApiCallback() {
                    @Override
                    public void onSuccess(Cancion cancion) {
                        SwingUtilities.invokeLater(() -> cargarCancionEnPantalla(cancion));
                    }

                    @Override
                    public void onError(String error) {
                        SwingUtilities.invokeLater(() -> {
                            JOptionPane.showMessageDialog(ReproductorGUI.this, "Error de API: " + error);
                            lblAlbumImage.setText("Sin canción cargada");
                        });
                    }
                });
            }
        });
    }

    private void cargarPlaylistInicial() {
        if (cbPlaylists.getItemCount() > 0) {
            String seleccion = (String) cbPlaylists.getSelectedItem();
            String genero = seleccion.split(" ")[0].toLowerCase(); 
            
            actualPlaylist = localRepository.getPlaylistByGenero(genero);
            indiceActual = 0;
            if (actualPlaylist != null && !actualPlaylist.isEmpty()) {
                cargarCancionEnPantalla(actualPlaylist.get(0));
            }
        }
    }

    private void cargarCancionEnPantalla(Cancion cancion) {
        this.cancionActual = cancion;
        if (barraTimer != null) { barraTimer.stop(); }
        progressSlider.setValue(0);
        
        try {
            ImageIcon iconoOriginal = new ImageIcon(cancion.getRutaImagen());
            Image imagenEscalada = iconoOriginal.getImage().getScaledInstance(300, 300, Image.SCALE_SMOOTH);
            lblAlbumImage.setIcon(new ImageIcon(imagenEscalada));
            lblAlbumImage.setText(""); 
            progressSlider.setMaximum(180); // Límite por defecto (3 minutos)
            
            System.out.println("Lista para reproducir: " + cancion.getTitulo());
        } catch (Exception e) {
            lblAlbumImage.setIcon(null);
            lblAlbumImage.setText("Imagen no encontrada");
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new ReproductorGUI().setVisible(true);
        });
    }
}