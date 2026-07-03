package com.mycompany.reproductormusicapoo.view;

import com.formdev.flatlaf.FlatDarkLaf;
import com.mycompany.reproductormusicapoo.model.Cancion;
import com.mycompany.reproductormusicapoo.repository.CancionRepository;
import com.mycompany.reproductormusicapoo.repository.PostgresCancionRepository;
import com.mycompany.reproductormusicapoo.service.MusicApiService;
import com.mycompany.reproductormusicapoo.service.ServicioAudio;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;

/**
 * Interfaz Gráfica Principal.
 * Ensambla los componentes visuales del reproductor y orquesta la lógica de usuario y eventos.
 */
public class ReproductorGUI extends JFrame {

    private JComboBox<String> cbPlaylists;
    private JSlider progressSlider;
    private JSlider volumeSlider;
    private JLabel lblAlbumImage, lblTituloCancion, lblArtista, lblVolumen;
    private JButton btnPrev, btnNext, btnStart, btnStop, btnPause, btnBuscarOnline, btnSalir;
    
    private final CancionRepository localRepository = new PostgresCancionRepository();
    private final MusicApiService apiService = new MusicApiService();
    private final ServicioAudio servicioAudio = new ServicioAudio();

    private List<Cancion> actualPlaylist;
    private Cancion cancionActual;
    private Timer barraTimer;
    private int indiceActual = 0;
    private boolean actualizandoDesdeTimer = false;
    private float hue = 0.55f;
    
    public ReproductorGUI() {
        setTitle("ReproductorDeMusicaPoo");
        setSize(950, 750);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        
        JPanel mainPanel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                Color color1 = Color.getHSBColor(hue, 0.5f, 0.35f);
                Color color2 = Color.getHSBColor(hue + 0.12f, 0.45f, 0.20f);

                GradientPaint gp = new GradientPaint(0, 0, color1, getWidth(), getHeight(), color2);
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        mainPanel.setBorder(new EmptyBorder(30, 40, 20, 40));
        
        Timer timer = new Timer(60, e -> {
            hue += 0.0008f;
            if (hue > 1.0f) { hue = 0.0f; }
            mainPanel.repaint();
        });
        timer.start();

        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setOpaque(false);

        lblAlbumImage = new JLabel("Sin Reproducción", SwingConstants.CENTER);
        lblAlbumImage.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblAlbumImage.setPreferredSize(new Dimension(350, 350));
        lblAlbumImage.setMaximumSize(new Dimension(350, 350));
        lblAlbumImage.setForeground(new Color(255, 255, 255, 180));
        lblAlbumImage.setFont(new Font("SansSerif", Font.ITALIC, 16));

        lblTituloCancion = new JLabel("Bienvenido", SwingConstants.CENTER);
        lblTituloCancion.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblTituloCancion.setForeground(Color.WHITE);
        lblTituloCancion.setFont(new Font("SansSerif", Font.BOLD, 28));
        lblTituloCancion.setBorder(new EmptyBorder(25, 0, 5, 0));

        lblArtista = new JLabel("Selecciona una lista o busca una canción", SwingConstants.CENTER);
        lblArtista.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblArtista.setForeground(new Color(220, 220, 220, 200));
        lblArtista.setFont(new Font("SansSerif", Font.PLAIN, 18));
        lblArtista.setBorder(new EmptyBorder(0, 0, 20, 0));

        centerPanel.add(Box.createVerticalGlue());
        centerPanel.add(lblAlbumImage);
        centerPanel.add(lblTituloCancion);
        centerPanel.add(lblArtista);
        centerPanel.add(Box.createVerticalGlue());
        mainPanel.add(centerPanel, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new BorderLayout(0, 15));
        bottomPanel.setOpaque(false);

        progressSlider = new JSlider(0, 100, 0);
        progressSlider.setOpaque(false);
        progressSlider.setCursor(new Cursor(Cursor.HAND_CURSOR));
        bottomPanel.add(progressSlider, BorderLayout.NORTH);

        JPanel controlBar = new JPanel(new BorderLayout());
        controlBar.setOpaque(false);
        
        JPanel leftControls = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 10));
        leftControls.setOpaque(false);
        String[] playlists = {"Rock", "Pop", "Anime"};
        cbPlaylists = new JComboBox<>(playlists);
        cbPlaylists.setPreferredSize(new Dimension(150, 35));
        cbPlaylists.setCursor(new Cursor(Cursor.HAND_CURSOR));
        leftControls.add(cbPlaylists);
        
        JPanel centerControls = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        centerControls.setOpaque(false);

        btnPrev = crearBotonCircular("⏮", 20);
        btnStop = crearBotonCircular("⏹", 20);
        btnStart = crearBotonCircular("▶", 28);
        btnPause = crearBotonCircular("⏸", 20);
        btnNext = crearBotonCircular("⏭", 20);

        centerControls.add(btnPrev);
        centerControls.add(btnStop);
        centerControls.add(btnStart);
        centerControls.add(btnPause);
        centerControls.add(btnNext);

        JPanel rightControls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        rightControls.setOpaque(false);

        lblVolumen = new JLabel("🔊");
        lblVolumen.setForeground(Color.WHITE);
        lblVolumen.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 18));

        volumeSlider = new JSlider(0, 100, 100);
        volumeSlider.setOpaque(false);
        volumeSlider.setPreferredSize(new Dimension(80, 35));
        volumeSlider.setCursor(new Cursor(Cursor.HAND_CURSOR));
        volumeSlider.putClientProperty("JSlider.isUseMaximumTrack", true);
        
        btnBuscarOnline = new JButton("🔍 Buscar");
        btnBuscarOnline.setPreferredSize(new Dimension(110, 35));
        btnBuscarOnline.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btnSalir = new JButton("Salir");
        btnSalir.setPreferredSize(new Dimension(70, 35));
        btnSalir.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnSalir.putClientProperty("JButton.buttonType", "roundRect");
        btnSalir.setBackground(new Color(180, 50, 50, 180));

        rightControls.add(lblVolumen);
        rightControls.add(volumeSlider);
        rightControls.add(btnBuscarOnline);
        rightControls.add(btnSalir);
        
        controlBar.add(leftControls, BorderLayout.WEST);
        controlBar.add(centerControls, BorderLayout.CENTER);
        controlBar.add(rightControls, BorderLayout.EAST);

        bottomPanel.add(controlBar, BorderLayout.CENTER);
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);
        setContentPane(mainPanel);

        configurarEventos();
        cargarPlaylistInicial();
    }

    /**
     * Construye un botón estilizado para los controles del reproductor.
     */
    private JButton crearBotonCircular(String icono, int fontSize) {
        JButton btn = new JButton(icono);
        btn.setFont(new Font("SansSerif", Font.PLAIN, fontSize));
        btn.setPreferredSize(new Dimension(55, 55));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.putClientProperty("JButton.buttonType", "roundRect");
        btn.setBackground(new Color(255, 255, 255, 25));
        btn.setForeground(Color.WHITE);
        return btn;
    }

    /**
     * Configura y asocia todos los listeners y acciones de la interfaz con la lógica.
     */
    private void configurarEventos() {

        volumeSlider.addChangeListener(e -> {
            int vol = volumeSlider.getValue();
            if (vol == 0) {
                lblVolumen.setText("🔇");
            } else if (vol < 50) {
                lblVolumen.setText("🔉");
            } else {
                lblVolumen.setText("🔊");
            }

            if (servicioAudio != null) {
                servicioAudio.setVolumen(vol);
            }
        });
        
        barraTimer = new Timer(1000, e -> {
            if (progressSlider.getValue() < progressSlider.getMaximum()) {
                actualizandoDesdeTimer = true;
                progressSlider.setValue(progressSlider.getValue() + 1);
                actualizandoDesdeTimer = false;
            } else {
                barraTimer.stop();
            }
        });
        
        progressSlider.addChangeListener(e -> {
            if (!actualizandoDesdeTimer && !progressSlider.getValueIsAdjusting()) {
                if (cancionActual != null) {
                    int segundoDestino = progressSlider.getValue();
                    int segundoMaximo = progressSlider.getMaximum();
                    boolean estabaSonando = barraTimer.isRunning();

                    servicioAudio.seek(cancionActual.getRutaArchivo(), segundoDestino, segundoMaximo);
                    if (estabaSonando) {
                        barraTimer.start();
                    }
                }
            }
        });
        
        cbPlaylists.addActionListener(e -> {
            String seleccion = (String) cbPlaylists.getSelectedItem();
            String genero = seleccion.split(" ")[0].toLowerCase();
            actualPlaylist = localRepository.obtenerPorGenero(genero);

            indiceActual = 0;
            if (actualPlaylist != null && !actualPlaylist.isEmpty()) {
                cargarCancionEnPantalla(actualPlaylist.get(0));
            }
        });
        
        btnNext.addActionListener(e -> {
            if (actualPlaylist != null && !actualPlaylist.isEmpty()) {
                indiceActual++;
                if (indiceActual >= actualPlaylist.size()) {
                    indiceActual = 0;
                }
                boolean estabaReproduciendo = barraTimer.isRunning();
                cargarCancionEnPantalla(actualPlaylist.get(indiceActual));
                if (estabaReproduciendo) {
                    servicioAudio.play(cancionActual.getRutaArchivo());
                    barraTimer.start();
                }
            }
        });
        
        btnPrev.addActionListener(e -> {
            if (actualPlaylist != null && !actualPlaylist.isEmpty()) {
                indiceActual--;
                if (indiceActual < 0) {
                    indiceActual = actualPlaylist.size() - 1;
                }
                boolean estabaReproduciendo = barraTimer.isRunning();
                cargarCancionEnPantalla(actualPlaylist.get(indiceActual));
                if (estabaReproduciendo) {
                    servicioAudio.play(cancionActual.getRutaArchivo());
                    barraTimer.start();
                }
            }
        });
        
        btnStart.addActionListener(e -> {
            if (cancionActual != null) {
                servicioAudio.play(cancionActual.getRutaArchivo());
                barraTimer.start();
            }
        });
        
        btnStop.addActionListener(e -> {
            servicioAudio.stop();
            barraTimer.stop();
            progressSlider.setValue(0);
        });
        
        btnPause.addActionListener(e -> {
            servicioAudio.pause();
            barraTimer.stop();
        });
        
        btnSalir.addActionListener(e -> System.exit(0));

        btnBuscarOnline.addActionListener(e -> {
            String busqueda = JOptionPane.showInputDialog(this, "Introduce el nombre de la canción o artista:");

            if (busqueda != null && !busqueda.trim().isEmpty()) {
                lblAlbumImage.setIcon(null);
                lblTituloCancion.setText("Conectando con la API...");
                lblArtista.setText("Por favor espera");

                apiService.buscarCancionOnline(busqueda, new MusicApiService.ApiCallback() {
                    @Override
                    public void onSuccess(List<Cancion> canciones) {
                        SwingUtilities.invokeLater(() -> {
                            actualPlaylist = canciones;
                            indiceActual = 0;
                            cargarCancionEnPantalla(actualPlaylist.get(indiceActual));
                            servicioAudio.play(actualPlaylist.get(indiceActual).getRutaArchivo());
                            barraTimer.start();
                        });
                    }

                    @Override
                    public void onError(String error) {
                        SwingUtilities.invokeLater(() -> {
                            JOptionPane.showMessageDialog(ReproductorGUI.this, "Error: " + error);
                            lblTituloCancion.setText("Error en la búsqueda");
                        });
                    }
                });
            }
        });
    }

    /**
     * Carga el primer elemento de la playlist local seleccionada por defecto.
     */
    private void cargarPlaylistInicial() {
        if (cbPlaylists.getItemCount() > 0) {
            String seleccion = (String) cbPlaylists.getSelectedItem();
            String genero = seleccion.split(" ")[0].toLowerCase();
            actualPlaylist = localRepository.obtenerPorGenero(genero);
            indiceActual = 0;
            if (actualPlaylist != null && !actualPlaylist.isEmpty()) {
                cargarCancionEnPantalla(actualPlaylist.get(0));
            }
        }
    }

    /**
     * Actualiza la carátula, los textos y la barra de progreso basándose en la canción elegida.
     */
    private void cargarCancionEnPantalla(Cancion cancion) {
        this.cancionActual = cancion;
        if (barraTimer != null) {
            barraTimer.stop();
        }
        progressSlider.setValue(0);

        lblTituloCancion.setText(cancion.getTitulo());
        lblArtista.setText(cancion.getArtista());
        
        try {
            ImageIcon iconoOriginal;
            if (cancion.isOnline()) {
                java.net.URL urlImagen = new java.net.URL(cancion.getRutaImagen());
                iconoOriginal = new ImageIcon(urlImagen);
            } else {
                iconoOriginal = new ImageIcon(cancion.getRutaImagen());
            }

            Image imagenEscalada = iconoOriginal.getImage().getScaledInstance(350, 350, Image.SCALE_SMOOTH);
            lblAlbumImage.setIcon(new ImageIcon(imagenEscalada));
            lblAlbumImage.setText("");

            if (cancion.isOnline()) {
                progressSlider.setMaximum(30);
            } else {
                progressSlider.setMaximum(180);
            }
        } catch (Exception e) {
            lblAlbumImage.setIcon(null);
            lblAlbumImage.setText("Imagen no encontrada");
        }
    }

    public static void main(String[] args) {
        try {
            FlatDarkLaf.setup();
            UIManager.put("Button.arc", 999);
            UIManager.put("Component.arc", 20);
            UIManager.put("Slider.thumbWidth", 15);
            UIManager.put("Slider.trackHeight", 5);

            UIManager.put("Slider.thumbColor", Color.WHITE);
            UIManager.put("Slider.trackSelectedColor", new Color(255, 255, 255, 200));
            UIManager.put("Slider.trackColor", new Color(255, 255, 255, 60));

        } catch (Exception e) {
            e.printStackTrace();
        }

        SwingUtilities.invokeLater(() -> {
            new ReproductorGUI().setVisible(true);
        });
    }
}