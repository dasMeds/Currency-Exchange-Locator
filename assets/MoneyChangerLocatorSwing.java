import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import java.util.*;
import java.util.stream.Collectors;

public class MoneyChangerLocatorSwing extends JFrame {

    // --- Modern Color Palette ---
    private static final Color BRAND_DARK = new Color(30, 41, 59);       // Slate 800
    private static final Color ACCENT_COLOR = new Color(37, 99, 235);    // Royal Blue
    
    // UI Colors
    private static final Color LIGHT_BG = new Color(243, 244, 246);      
    private static final Color LIGHT_PANEL = Color.WHITE;
    private static final Color LIGHT_TEXT_MAIN = new Color(17, 24, 39);  
    private static final Color LIGHT_TEXT_SEC = new Color(107, 114, 128);
    
    private static final Color DARK_BG = new Color(15, 23, 42);          
    private static final Color DARK_PANEL = new Color(30, 41, 59);       
    private static final Color DARK_TEXT_MAIN = new Color(241, 245, 249);
    
    // Status Colors
    private static final Color SUCCESS_COLOR = new Color(16, 185, 129);  
    private static final Color WARNING_COLOR = new Color(239, 68, 68);   
    private static final Color GOLD_COLOR = new Color(245, 158, 11);     

    // --- Fonts ---
    private static final Font TITLE_FONT = new Font("SansSerif", Font.BOLD, 32);
    private static final Font HEADER_FONT = new Font("SansSerif", Font.BOLD, 18);
    private static final Font DATA_FONT = new Font("SansSerif", Font.PLAIN, 16);
    private static final Font SMALL_FONT = new Font("SansSerif", Font.BOLD, 13);

    // --- State ---
    private CardLayout cardLayout;
    private JPanel mainPanel;
    private CustomerInput currentCustomer;
    
    private java.util.List<MoneyChanger> masterDatabase; 
    private java.util.List<MoneyChanger> currentCityChangers; 
    private java.util.List<MoneyChanger> visibleChangers; 
    
    private boolean isDarkMode = false;
    private boolean filterOpenNow = false;
    private String currentSortMode = "Closest"; 

    private Map<String, BufferedImage> cityMaps = new HashMap<>();
    
    // Components
    private MapPanel mapPanel;
    private JPanel sidebarPanel, resultsContainer;
    private JScrollPane sidebarScroll;
    private JTextPane summaryPane; 
    private JPanel currencyRowsPanel;
    private java.util.List<CurrencyRow> currencyRowComponents;
    private ModernToggleButton darkModeToggle;

    private static final Map<String, String> CURRENCY_NAMES = new LinkedHashMap<>();
    static {
        CURRENCY_NAMES.put("USD", "US Dollar");
        CURRENCY_NAMES.put("EUR", "Euro");
        CURRENCY_NAMES.put("JPY", "Japanese Yen");
        CURRENCY_NAMES.put("GBP", "Pound Sterling");
        CURRENCY_NAMES.put("HKD", "HK Dollar");
        CURRENCY_NAMES.put("SGD", "SG Dollar");
        CURRENCY_NAMES.put("SAR", "Saudi Riyal");
        CURRENCY_NAMES.put("AED", "UAE Dirham");
        CURRENCY_NAMES.put("KRW", "Korean Won");
        CURRENCY_NAMES.put("TWD", "Taiwan Dollar");
    }

    public MoneyChangerLocatorSwing() {
        setTitle("CURENSEEK | Intelligent Money Changer Locator");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1400, 950); 
        setLocationRelativeTo(null);

        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception ignored) {}

        loadMapImages();
        masterDatabase = initializeFullDatabase();
        visibleChangers = new ArrayList<>();
        currentCityChangers = new ArrayList<>();
        currentCustomer = new CustomerInput();
        currencyRowComponents = new ArrayList<>();

        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        mainPanel.add(createInputPage(), "INPUT");
        mainPanel.add(createMapPage(), "MAP");
        mainPanel.add(createSummaryPage(), "SUMMARY");

        add(mainPanel);
    }

    private void loadMapImages() {
        loadImage("VALENZUELA CITY", "unnamed (1).jpg");
        loadImage("MALABON CITY", "unnamed (5).jpg");
        loadImage("CALOOCAN CITY", "unnamed (2).jpg");
        loadImage("QUEZON CITY", "unnamed (4).jpg");
        loadImage("MANILA CITY", "unnamed.jpg");
        loadImage("BULACAN PROVINCE", "unnamed (3).jpg");
    }

    private void loadImage(String city, String filename) {
        try {
            File f = new File(filename);
            if(f.exists()) cityMaps.put(city, ImageIO.read(f));
        } catch (IOException e) { System.out.println("Error reading: " + filename); }
    }

    // =================================================================================
    // PAGE 1: SPACIOUS INPUT PAGE
    // =================================================================================
    private JPanel createInputPage() {
        JPanel container = new JPanel(new GridBagLayout()); 
        container.setBackground(BRAND_DARK); 

        // Main Card
        JPanel card = new JPanel(new BorderLayout(0, 30));
        card.setBackground(Color.WHITE);
        card.setBorder(new EmptyBorder(50, 60, 50, 60));
        card.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(new Color(0,0,0,30), 1, true),
            new EmptyBorder(50, 60, 50, 60)
        ));
        card.setPreferredSize(new Dimension(700, 850));

        // Header
        JPanel header = new JPanel(new GridLayout(2, 1, 0, 5));
        header.setBackground(Color.WHITE);
        JLabel title = new JLabel("CURENSEEK", SwingConstants.CENTER);
        title.setFont(TITLE_FONT);
        title.setForeground(BRAND_DARK);
        
        JLabel subtitle = new JLabel("Find the best rates in your city instantly.", SwingConstants.CENTER);
        subtitle.setFont(DATA_FONT);
        subtitle.setForeground(LIGHT_TEXT_SEC);
        
        header.add(title);
        header.add(subtitle);
        card.add(header, BorderLayout.NORTH);

        // Form Container
        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setBackground(Color.WHITE);

        // Direct References to Inputs (Fixes previous error)
        JTextField nameField = createTextField();
        JTextField contactField = createTextField();

        // Row 1: Name & Contact
        JPanel row1 = new JPanel(new GridLayout(1, 2, 20, 0));
        row1.setBackground(Color.WHITE);
        row1.add(createLabeledPanel("Your Name", nameField));
        row1.add(createLabeledPanel("Contact Number", contactField));
        
        // Row 2: Location
        JPanel row2 = new JPanel(new BorderLayout());
        row2.setBackground(Color.WHITE);
        row2.setBorder(new EmptyBorder(15, 0, 0, 0));
        row2.add(createLabel("Select Location", LIGHT_TEXT_SEC), BorderLayout.NORTH);
        
        String[] locations = {
            "VALENZUELA CITY", "MALABON CITY", "CALOOCAN CITY", 
            "QUEZON CITY", "MANILA CITY", "BULACAN PROVINCE"
        };
        JComboBox<String> locationBox = new JComboBox<>(locations);
        locationBox.setFont(DATA_FONT);
        locationBox.setBackground(Color.WHITE);
        locationBox.setForeground(Color.BLACK);
        locationBox.setPreferredSize(new Dimension(0, 45));
        row2.add(locationBox, BorderLayout.CENTER);

        // Row 3: Currencies
        JPanel currencyHeader = new JPanel(new BorderLayout());
        currencyHeader.setBackground(Color.WHITE);
        currencyHeader.setBorder(new EmptyBorder(25, 0, 10, 0));
        currencyHeader.add(createLabel("Currencies to Exchange", LIGHT_TEXT_SEC), BorderLayout.WEST);

        currencyRowsPanel = new JPanel();
        currencyRowsPanel.setLayout(new BoxLayout(currencyRowsPanel, BoxLayout.Y_AXIS));
        currencyRowsPanel.setBackground(Color.WHITE);
        addCurrencyRow(); 
        
        JScrollPane scroll = new JScrollPane(currencyRowsPanel);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(229, 231, 235))); 
        scroll.setPreferredSize(new Dimension(0, 200)); 
        
        ModernButton addCurrBtn = new ModernButton("+ Add Another Currency", Color.WHITE, ACCENT_COLOR);
        addCurrBtn.setBorderColor(ACCENT_COLOR);
        addCurrBtn.addActionListener(e -> addCurrencyRow());

        // Add to Form
        form.add(row1);
        form.add(row2);
        form.add(currencyHeader);
        form.add(scroll);
        form.add(Box.createVerticalStrut(10));
        form.add(addCurrBtn);

        card.add(form, BorderLayout.CENTER);

        // Footer Action
        ModernButton nextBtn = new ModernButton("FIND BEST RATES ➔", ACCENT_COLOR, Color.WHITE);
        nextBtn.setPreferredSize(new Dimension(0, 60));
        nextBtn.setFont(new Font("SansSerif", Font.BOLD, 16));
        
        nextBtn.addActionListener(e -> {
            if(nameField.getText().isEmpty()) { JOptionPane.showMessageDialog(this, "Name required"); return; }
            if(contactField.getText().isEmpty()) { JOptionPane.showMessageDialog(this, "Contact Number required"); return; }
            
            currentCustomer.name = nameField.getText();
            currentCustomer.contact = contactField.getText();
            currentCustomer.locationName = (String) locationBox.getSelectedItem();
            currentCustomer.requests.clear();
            currentCustomer.userNormX = 0.5;
            currentCustomer.userNormY = 0.5;

            for(CurrencyRow row : currencyRowComponents) {
                String code = ((String)row.combo.getSelectedItem()).split(" - ")[0];
                double amt = 0;
                try { amt = Double.parseDouble(row.amountField.getText()); } catch (NumberFormatException ex) {}
                if(amt > 0) currentCustomer.requests.add(new CurrencyRequest(code, amt));
            }

            if(currentCustomer.requests.isEmpty()) { JOptionPane.showMessageDialog(this, "Enter at least one valid amount"); return; }

            currentCityChangers = masterDatabase.stream()
                .filter(mc -> mc.city.equalsIgnoreCase(currentCustomer.locationName))
                .collect(Collectors.toList());
            
            if(currentCityChangers.isEmpty()) {
                JOptionPane.showMessageDialog(this, "No data found for this city. Please check image filenames.");
                return;
            }

            calculateDistancesAndValues();
            applyFiltersAndSort();

            mapPanel.setCurrentMap(cityMaps.get(currentCustomer.locationName));
            mapPanel.repaint();
            cardLayout.show(mainPanel, "MAP");
        });
        
        card.add(nextBtn, BorderLayout.SOUTH);
        container.add(card);
        return container;
    }

    private JPanel createLabeledPanel(String label, Component field) {
        JPanel p = new JPanel(new BorderLayout(0, 8));
        p.setBackground(Color.WHITE);
        p.add(createLabel(label, LIGHT_TEXT_SEC), BorderLayout.NORTH);
        p.add(field, BorderLayout.CENTER);
        return p;
    }

    private void addCurrencyRow() {
        CurrencyRow row = new CurrencyRow();
        currencyRowComponents.add(row);
        currencyRowsPanel.add(row.panel);
        currencyRowsPanel.revalidate();
        currencyRowsPanel.repaint();
    }

    // =================================================================================
    // PAGE 2: MAP & SIDEBAR
    // =================================================================================
    private JPanel createMapPage() {
        JPanel container = new JPanel(new BorderLayout());

        // --- Top Bar ---
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(LIGHT_PANEL); 
        topBar.setBorder(new MatteBorder(0, 0, 1, 0, new Color(229, 231, 235)));
        topBar.setPreferredSize(new Dimension(0, 70));
        
        JPanel leftBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 15));
        leftBar.setOpaque(false);
        ModernButton backBtn = new ModernButton("← Back", Color.WHITE, LIGHT_TEXT_MAIN);
        backBtn.setBorderColor(new Color(209, 213, 219));
        backBtn.setPreferredSize(new Dimension(90, 40));
        backBtn.addActionListener(e -> cardLayout.show(mainPanel, "INPUT"));
        leftBar.add(backBtn);

        JPanel rightBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 18));
        rightBar.setOpaque(false);
        
        String[] sorts = {"Closest", "Best Value", "Highest Rated"};
        JComboBox<String> sortBox = new JComboBox<>(sorts);
        sortBox.setFont(SMALL_FONT);
        sortBox.setPreferredSize(new Dimension(140, 35));
        sortBox.addActionListener(e -> { 
            currentSortMode = (String)sortBox.getSelectedItem(); 
            applyFiltersAndSort(); 
        });
        
        JCheckBox openCheck = new JCheckBox("Open Now");
        openCheck.setOpaque(false);
        openCheck.setFont(SMALL_FONT);
        openCheck.addActionListener(e -> { 
            filterOpenNow = openCheck.isSelected(); 
            applyFiltersAndSort(); 
        });
        
        darkModeToggle = new ModernToggleButton("Dark Mode");
        darkModeToggle.addActionListener(e -> toggleDarkMode(container, topBar, rightBar));

        rightBar.add(new JLabel("Sort By:"));
        rightBar.add(sortBox);
        rightBar.add(Box.createHorizontalStrut(10));
        rightBar.add(openCheck);
        rightBar.add(Box.createHorizontalStrut(10));
        rightBar.add(darkModeToggle);

        topBar.add(leftBar, BorderLayout.WEST);
        topBar.add(rightBar, BorderLayout.EAST);
        container.add(topBar, BorderLayout.NORTH);

        // --- Main Content ---
        mapPanel = new MapPanel();
        
        // Sidebar
        sidebarPanel = new JPanel(new BorderLayout());
        sidebarPanel.setPreferredSize(new Dimension(340, 0)); 
        sidebarPanel.setBackground(LIGHT_BG);
        sidebarPanel.setBorder(new MatteBorder(0, 1, 0, 0, new Color(229, 231, 235)));
        
        JLabel resTitle = new JLabel("Top Matches");
        resTitle.setFont(HEADER_FONT);
        resTitle.setBorder(new EmptyBorder(20, 20, 10, 20));
        sidebarPanel.add(resTitle, BorderLayout.NORTH);

        resultsContainer = new JPanel();
        resultsContainer.setLayout(new BoxLayout(resultsContainer, BoxLayout.Y_AXIS));
        resultsContainer.setBackground(LIGHT_BG);
        resultsContainer.setBorder(new EmptyBorder(10, 15, 10, 15)); 
        
        sidebarScroll = new JScrollPane(resultsContainer);
        sidebarScroll.setBorder(null);
        sidebarScroll.getVerticalScrollBar().setUnitIncrement(16);
        sidebarPanel.add(sidebarScroll, BorderLayout.CENTER);
        
        ModernButton summaryBtn = new ModernButton("View Receipt", ACCENT_COLOR, Color.WHITE);
        summaryBtn.setPreferredSize(new Dimension(0, 50));
        summaryBtn.addActionListener(e -> { updateSummary(); cardLayout.show(mainPanel, "SUMMARY"); });
        
        JPanel bottomP = new JPanel(new BorderLayout());
        bottomP.setOpaque(false);
        bottomP.setBorder(new EmptyBorder(20, 20, 20, 20));
        bottomP.add(summaryBtn, BorderLayout.CENTER);
        sidebarPanel.add(bottomP, BorderLayout.SOUTH);

        container.add(mapPanel, BorderLayout.CENTER);
        container.add(sidebarPanel, BorderLayout.EAST); 
        
        return container;
    }

    private void toggleDarkMode(JPanel container, JPanel topBar, JPanel rightBar) {
        isDarkMode = darkModeToggle.isSelected();
        
        Color bg = isDarkMode ? DARK_BG : LIGHT_BG;
        Color panel = isDarkMode ? DARK_PANEL : LIGHT_PANEL;
        Color text = isDarkMode ? DARK_TEXT_MAIN : LIGHT_TEXT_MAIN;
        Color border = isDarkMode ? new Color(55, 65, 81) : new Color(229, 231, 235);

        container.setBackground(panel);
        topBar.setBackground(panel);
        topBar.setBorder(new MatteBorder(0, 0, 1, 0, border));
        sidebarPanel.setBackground(bg);
        sidebarPanel.setBorder(new MatteBorder(0, 1, 0, 0, border));
        resultsContainer.setBackground(bg);
        sidebarScroll.getViewport().setBackground(bg);
        
        Component[] comps = rightBar.getComponents();
        for(Component c : comps) {
            if(c instanceof JLabel || c instanceof JCheckBox) {
                c.setForeground(text);
            }
        }
        
        ((JLabel)sidebarPanel.getComponent(0)).setForeground(text);

        rebuildResultCards(); 
        mapPanel.repaint(); 
    }

    // --- SORTING & FILTERING ALGORITHM ---
    private void calculateDistancesAndValues() {
        double maxTotal = 0;
        for (MoneyChanger mc : currentCityChangers) {
            // Distance from BLUE MARKER (User Location)
            mc.distanceUnits = Math.sqrt(Math.pow(mc.normX - currentCustomer.userNormX, 2) + Math.pow(mc.normY - currentCustomer.userNormY, 2));
            
            double total = 0;
            int matches = 0;
            for(CurrencyRequest req : currentCustomer.requests) {
                if(mc.rates.containsKey(req.code)) {
                    total += req.amount * mc.rates.get(req.code);
                    matches++;
                }
            }
            mc.computedTotalValue = total;
            mc.matchedCount = matches;
            if(total > maxTotal) maxTotal = total;
        }
        // Set "Best Choice" Flag
        for(MoneyChanger mc : currentCityChangers) {
            mc.isBestChoice = (mc.computedTotalValue >= maxTotal && mc.matchedCount > 0 && maxTotal > 0);
        }
    }

    private void applyFiltersAndSort() {
        visibleChangers = currentCityChangers.stream()
            .filter(mc -> !filterOpenNow || mc.isOpenNow)
            .filter(mc -> mc.matchedCount > 0)
            .collect(Collectors.toList());

        Comparator<MoneyChanger> c;
        
        if(currentSortMode.equals("Closest")) {
            // Priority 1: Open Shops, Priority 2: Distance
            c = Comparator.comparing((MoneyChanger mc) -> !mc.isOpenNow) // Open first (false < true)
                          .thenComparingDouble(mc -> mc.distanceUnits);
        } else if(currentSortMode.equals("Highest Rated")) {
            c = Comparator.comparingDouble((MoneyChanger mc) -> mc.rating).reversed();
        } else {
            // Best Value (Total PHP)
            c = Comparator.comparingDouble((MoneyChanger mc) -> mc.computedTotalValue).reversed();
        }
        
        visibleChangers.sort(c);
        rebuildResultCards();
        mapPanel.repaint();
    }

    private void rebuildResultCards() {
        resultsContainer.removeAll();
        for(MoneyChanger mc : visibleChangers) {
            resultsContainer.add(createResultCard(mc));
            resultsContainer.add(Box.createVerticalStrut(12));
        }
        resultsContainer.revalidate();
        resultsContainer.repaint();
    }

    private JPanel createResultCard(MoneyChanger mc) {
        JPanel card = new JPanel(new BorderLayout());
        
        Color bg = isDarkMode ? DARK_PANEL : LIGHT_PANEL;
        Color titleColor = isDarkMode ? DARK_TEXT_MAIN : LIGHT_TEXT_MAIN;
        Color subColor = isDarkMode ? DARK_TEXT_MAIN : LIGHT_TEXT_MAIN;
        Color borderColor = isDarkMode ? new Color(75, 85, 99) : new Color(229, 231, 235);

        card.setBackground(bg);
        card.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(borderColor, 1, true),
            new EmptyBorder(15, 15, 15, 15)
        ));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));
        card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        card.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) { mapPanel.highlightChanger(mc); }
            public void mouseEntered(MouseEvent e) { card.setBorder(new LineBorder(ACCENT_COLOR, 1, true)); }
            public void mouseExited(MouseEvent e) { card.setBorder(new LineBorder(borderColor, 1, true)); }
        });

        // Left: Info
        JPanel info = new JPanel(new GridLayout(3, 1, 0, 4));
        info.setOpaque(false);
        
        JLabel name = new JLabel(mc.name);
        name.setFont(new Font("SansSerif", Font.BOLD, 14));
        name.setForeground(titleColor);
        
        String statusText = mc.isOpenNow ? "Open" : "Closed";
        Color statusColor = mc.isOpenNow ? SUCCESS_COLOR : WARNING_COLOR;
        
        JLabel details = new JLabel(String.format("%.1f ★ • %s", mc.rating, statusText));
        details.setFont(SMALL_FONT);
        details.setForeground(statusColor);
        
        JLabel dist = new JLabel(String.format("Rel. Dist: %.2f", mc.distanceUnits)); 
        dist.setFont(SMALL_FONT);
        dist.setForeground(subColor);

        info.add(name); info.add(details); info.add(dist);

        // Right: Price
        JPanel priceP = new JPanel(new BorderLayout());
        priceP.setOpaque(false);
        JLabel price = new JLabel(String.format("₱%,.0f", mc.computedTotalValue));
        price.setFont(new Font("SansSerif", Font.BOLD, 18));
        price.setForeground(mc.isBestChoice ? SUCCESS_COLOR : titleColor);
        
        if(mc.isBestChoice) {
            JLabel badge = new JLabel("BEST", SwingConstants.RIGHT);
            badge.setForeground(GOLD_COLOR);
            badge.setFont(SMALL_FONT);
            priceP.add(badge, BorderLayout.NORTH);
        }
        priceP.add(price, BorderLayout.CENTER);

        card.add(info, BorderLayout.CENTER);
        card.add(priceP, BorderLayout.EAST);
        
        return card;
    }

    // --- Map Panel ---
    class MapPanel extends JPanel {
        private MoneyChanger hoveredChanger = null;
        private BufferedImage currentMapImage = null;

        public MapPanel() {
            MouseAdapter ma = new MouseAdapter() {
                public void mousePressed(MouseEvent e) {
                    if (SwingUtilities.isLeftMouseButton(e)) {
                        // Update User Location
                        currentCustomer.userNormX = Math.max(0, Math.min(1, (double)e.getX()/getWidth()));
                        currentCustomer.userNormY = Math.max(0, Math.min(1, (double)e.getY()/getHeight()));
                        
                        // Recalculate Distances from this new point
                        calculateDistancesAndValues();
                        applyFiltersAndSort(); // Re-sort based on new distances
                        
                        repaint();
                    }
                }
                public void mouseMoved(MouseEvent e) {
                    hoveredChanger = null;
                    int w = getWidth(); int h = getHeight();
                    for (MoneyChanger mc : visibleChangers) {
                        if (e.getPoint().distance(mc.normX * w, mc.normY * h) < 25) {
                            hoveredChanger = mc; break;
                        }
                    }
                    repaint();
                }
            };
            addMouseListener(ma);
            addMouseMotionListener(ma);
        }

        public void setCurrentMap(BufferedImage img) { this.currentMapImage = img; }
        public void highlightChanger(MoneyChanger mc) { this.hoveredChanger = mc; repaint(); }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth(); int h = getHeight();

            if (currentMapImage != null) {
                g2.drawImage(currentMapImage, 0, 0, w, h, null);
                // No Dark Overlay
            } else {
                g2.setColor(new Color(229, 231, 235)); 
                g2.fillRect(0,0,w,h);
                g2.setColor(Color.GRAY);
                g2.drawString("Map Image Not Found", w/2 - 60, h/2);
            }

            // User Pin
            double ux = currentCustomer.userNormX * w;
            double uy = currentCustomer.userNormY * h;
            drawPin(g2, ux, uy, ACCENT_COLOR, true);

            // Changers
            for (MoneyChanger mc : visibleChangers) {
                double mx = mc.normX * w;
                double my = mc.normY * h;
                boolean isHovered = (mc == hoveredChanger);
                
                Color c = mc.isBestChoice ? GOLD_COLOR : WARNING_COLOR;
                if(!mc.isBestChoice && mc.matchedCount > 0) c = SUCCESS_COLOR;
                if(!mc.isOpenNow) c = Color.GRAY; 
                
                if(mc.isBestChoice) {
                    g2.setColor(new Color(245, 158, 11, 120)); 
                    g2.fillOval((int)mx-15, (int)my-15, 30, 30);
                    
                    g2.setColor(new Color(245, 158, 11, 180));
                    g2.setStroke(new BasicStroke(3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 10f, new float[]{10f}, 0f));
                    g2.draw(new Line2D.Double(ux, uy, mx, my));
                }

                drawPin(g2, mx, my, c, false);
                if (isHovered) drawPopup(g2, mc, (int)mx, (int)my);
            }
        }

        private void drawPin(Graphics2D g2, double x, double y, Color c, boolean isUser) {
            int r = isUser ? 10 : 8;
            g2.setColor(Color.WHITE); // Border
            g2.fillOval((int)x-r-2, (int)y-r-2, (r+2)*2, (r+2)*2);
            g2.setColor(c);
            g2.fillOval((int)x-r, (int)y-r, r*2, r*2);
        }

        private void drawPopup(Graphics2D g2, MoneyChanger mc, int x, int y) {
            String val = String.format("₱%,.0f", mc.computedTotalValue);
            int w = 180; int h = 75;
            int px = Math.min(getWidth() - w, Math.max(0, x - w/2)); 
            int py = Math.max(0, y - h - 15);
            
            g2.setColor(new Color(31, 41, 55, 240)); 
            g2.fillRoundRect(px, py, w, h, 8, 8);
            
            g2.setColor(Color.WHITE);
            g2.setFont(new Font("SansSerif", Font.BOLD, 12));
            g2.drawString(mc.name, px + 12, py + 25);
            
            g2.setColor(GOLD_COLOR);
            g2.setFont(new Font("SansSerif", Font.BOLD, 15));
            g2.drawString(val, px + 12, py + 50);
        }
    }

    // =================================================================================
    // PAGE 3: SUMMARY (HTML INVOICE)
    // =================================================================================
    private JPanel createSummaryPage() {
        JPanel container = new JPanel(new BorderLayout());
        container.setBackground(LIGHT_BG);

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(LIGHT_PANEL);
        header.setBorder(new MatteBorder(0,0,1,0, new Color(229, 231, 235)));
        header.setPreferredSize(new Dimension(0, 70));
        
        JLabel title = new JLabel("Transaction Details", SwingConstants.CENTER);
        title.setFont(TITLE_FONT);
        title.setForeground(LIGHT_TEXT_MAIN);
        header.add(title, BorderLayout.CENTER);
        container.add(header, BorderLayout.NORTH);

        summaryPane = new JTextPane();
        summaryPane.setEditable(false);
        summaryPane.setContentType("text/html");
        
        JScrollPane scroll = new JScrollPane(summaryPane);
        scroll.setBorder(null);
        
        JPanel paperContainer = new JPanel(new BorderLayout());
        paperContainer.setBackground(LIGHT_BG);
        paperContainer.setBorder(new EmptyBorder(30, 150, 30, 150)); 
        paperContainer.add(scroll, BorderLayout.CENTER);
        
        container.add(paperContainer, BorderLayout.CENTER);

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 20));
        footer.setBackground(LIGHT_BG);
        ModernButton printBtn = new ModernButton("🖨 Print Invoice", Color.WHITE, LIGHT_TEXT_MAIN);
        printBtn.setBorderColor(LIGHT_TEXT_SEC);
        ModernButton homeBtn = new ModernButton("Start New Search", ACCENT_COLOR, Color.WHITE);
        
        homeBtn.addActionListener(e -> cardLayout.show(mainPanel, "INPUT"));
        footer.add(printBtn);
        footer.add(homeBtn);
        container.add(footer, BorderLayout.SOUTH);

        return container;
    }

    private void updateSummary() {
        MoneyChanger best = visibleChangers.isEmpty() ? null : visibleChangers.get(0);
        String date = new java.text.SimpleDateFormat("MMM dd, yyyy - HH:mm").format(new Date());

        StringBuilder html = new StringBuilder();
        html.append("<html><body style='font-family: SansSerif; padding: 30px; background-color: white;'>");
        html.append("<div style='border-bottom: 2px solid #2563eb; padding-bottom: 10px; margin-bottom: 20px;'>");
        html.append("<h1 style='color: #1f2937; margin: 0;'>CURENSEEK</h1>");
        html.append("<p style='color: #6b7280; font-size: 10px;'>Official Estimation Receipt</p></div>");
        
        html.append("<table width='100%'><tr>");
        html.append("<td width='50%' valign='top'>");
        html.append("<b style='color: #4b5563;'>CUSTOMER</b><br>");
        html.append("<span style='font-size: 14px;'>").append(currentCustomer.name).append("</span><br>");
        html.append("<span style='color: #6b7280;'>").append(currentCustomer.contact).append("</span><br>");
        html.append("<span style='color: #6b7280;'>").append(currentCustomer.locationName).append("</span>");
        html.append("</td>");
        html.append("<td width='50%' valign='top' align='right'>");
        html.append("<b style='color: #4b5563;'>DATE</b><br>");
        html.append("<span>").append(date).append("</span><br><br>");
        html.append("</td></tr></table><br><br>");

        if(best != null) {
            html.append("<div style='background-color: #f3f4f6; padding: 15px; border-radius: 5px;'>");
            html.append("<h3 style='margin-top: 0; color: #1f2937;'>Recommended Provider</h3>");
            html.append("<b style='font-size: 16px; color: #2563eb;'>").append(best.name).append("</b><br>");
            html.append("<span style='color: #4b5563;'>").append(best.address).append("</span></div><br>");
            
            html.append("<table width='100%' cellpadding='8' cellspacing='0' style='border-collapse: collapse;'>");
            html.append("<tr style='background-color: #374151; color: white;'>");
            html.append("<th align='left'>CURRENCY</th><th align='left'>AMOUNT</th><th align='left'>RATE</th><th align='right'>TOTAL (PHP)</th></tr>");
            
            for(CurrencyRequest req : currentCustomer.requests) {
                if(best.rates.containsKey(req.code)) {
                    double rate = best.rates.get(req.code);
                    html.append("<tr style='border-bottom: 1px solid #e5e7eb;'>");
                    html.append("<td><b>").append(req.code).append("</b></td>");
                    html.append("<td>").append(String.format("%.2f", req.amount)).append("</td>");
                    html.append("<td>").append(String.format("%.4f", rate)).append("</td>");
                    html.append("<td align='right'>").append(String.format("%,.2f", req.amount*rate)).append("</td>");
                    html.append("</tr>");
                }
            }
            html.append("</table>");
            html.append("<h2 style='text-align: right; color: #059669; margin-top: 20px;'>PHP ").append(String.format("%,.2f", best.computedTotalValue)).append("</h2>");
        } else {
            html.append("<h3 style='color: #ef4444;'>No valid exchangers found matching criteria.</h3>");
        }
        
        html.append("</body></html>");
        summaryPane.setText(html.toString());
    }

    // --- Custom Components ---
    class ModernButton extends JButton {
        private Color bgColor;
        private Color fgColor;
        private Color borderColor = null;
        private Color defaultBg; 

        public ModernButton(String text, Color bg, Color fg) {
            super(text);
            this.bgColor = bg;
            this.defaultBg = bg; 
            this.fgColor = fg;
            setContentAreaFilled(false);
            setFocusPainted(false);
            setBorderPainted(false);
            setForeground(fg);
            setFont(new Font("SansSerif", Font.BOLD, 14));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            
            addMouseListener(new MouseAdapter() {
                public void mouseEntered(MouseEvent e) { 
                    if(defaultBg.equals(ACCENT_COLOR)) bgColor = BRAND_DARK;
                    else if(defaultBg.equals(Color.WHITE)) bgColor = new Color(243, 244, 246);
                    else if(defaultBg.equals(BRAND_DARK)) bgColor = new Color(30, 50, 90);
                    repaint(); 
                }
                public void mouseExited(MouseEvent e) { bgColor = defaultBg; repaint(); }
            });
        }
        
        public void setBorderColor(Color c) { this.borderColor = c; }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(bgColor);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
            
            if(borderColor != null) {
                g2.setColor(borderColor);
                g2.setStroke(new BasicStroke(1));
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 12, 12);
            }
            super.paintComponent(g);
            g2.dispose();
        }
    }

    class ModernToggleButton extends JToggleButton {
        public ModernToggleButton(String text) {
            super(text);
            setContentAreaFilled(false);
            setFocusPainted(false);
            setBorder(new EmptyBorder(5, 10, 5, 10));
            setFont(SMALL_FONT);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            if(isSelected()) g2.setColor(DARK_BG); else g2.setColor(Color.WHITE);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
            g2.setColor(Color.GRAY);
            g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 15, 15);
            super.paintComponent(g);
            g2.dispose();
        }
    }

    private JLabel createLabel(String text, Color color) {
        JLabel l = new JLabel(text);
        l.setFont(SMALL_FONT);
        l.setForeground(color);
        return l;
    }

    private JTextField createTextField() {
        JTextField tf = new JTextField();
        tf.setFont(DATA_FONT);
        tf.setForeground(Color.BLACK); // FORCE BLACK TEXT
        tf.setBackground(Color.WHITE); // FORCE WHITE BG
        tf.setPreferredSize(new Dimension(0, 40));
        tf.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(new Color(209, 213, 219), 1, true),
            new EmptyBorder(5, 10, 5, 10)
        ));
        return tf;
    }

    // --- Models ---
    class CurrencyRow {
        JPanel panel; JComboBox<String> combo; JTextField amountField; JButton removeBtn;
        public CurrencyRow() {
            panel = new JPanel(new BorderLayout(10, 0)); 
            panel.setBackground(Color.WHITE);
            panel.setBorder(new EmptyBorder(5, 5, 5, 5));
            panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
            
            combo = new JComboBox<>(CURRENCY_NAMES.keySet().toArray(new String[0]));
            combo.setFont(DATA_FONT);
            
            amountField = createTextField(); 
            amountField.setText("100");
            
            removeBtn = new JButton("×"); 
            removeBtn.setForeground(WARNING_COLOR);
            removeBtn.setBorderPainted(false); 
            removeBtn.setContentAreaFilled(false);
            removeBtn.setFont(new Font("SansSerif", Font.BOLD, 18));
            removeBtn.addActionListener(e -> { currencyRowComponents.remove(this); currencyRowsPanel.remove(panel); currencyRowsPanel.revalidate(); currencyRowsPanel.repaint(); });
            
            panel.add(combo, BorderLayout.WEST); 
            panel.add(amountField, BorderLayout.CENTER); 
            panel.add(removeBtn, BorderLayout.EAST);
        }
    }

    static class CustomerInput { String name=""; String contact=""; String locationName=""; double userNormX=0.5; double userNormY=0.5; java.util.List<CurrencyRequest> requests=new ArrayList<>(); }
    static class CurrencyRequest { String code; double amount; CurrencyRequest(String c, double a) { code=c; amount=a; } }

    static class MoneyChanger {
        String name, city, address; double normX, normY; Map<String, Double> rates;
        double distanceUnits, computedTotalValue, rating; int matchedCount;
        boolean isBestChoice, isOpenNow, isBookmarked;
        public MoneyChanger(String name, String city, double nx, double ny, Map<String, Double> rates, Random r) {
            this.name = name; this.city = city; this.normX = nx; this.normY = ny; this.rates = rates;
            this.rating = 3.0 + (r.nextDouble() * 2.0); // 3.0 - 5.0
            this.isOpenNow = r.nextBoolean(); // Randomly open or closed
            this.address = "Commercial District, " + city;
        }
    }

    // --- Data ---
    private java.util.List<MoneyChanger> initializeFullDatabase() {
        java.util.List<MoneyChanger> list = new ArrayList<>();
        Random rand = new Random();
        Map<String, Double> base = new HashMap<>();
        base.put("USD", 58.50); base.put("EUR", 63.20); base.put("JPY", 0.39);
        base.put("GBP", 74.10); base.put("HKD", 7.48); base.put("SGD", 43.50);
        base.put("SAR", 15.58); base.put("AED", 15.92); base.put("KRW", 0.042);
        base.put("TWD", 1.80);  base.put("MYR", 12.50);

        // VALENZUELA
        addLegacy(list, "Junlin Money Changer – 443 MacArthur Hwy", "VALENZUELA CITY", 0, 0, base, rand);
        addLegacy(list, "Junlin Money Changer – K-Plaza", "VALENZUELA CITY", 2, 2, base, rand);
        addLegacy(list, "Eunice Money Changer – C.J. Santos St", "VALENZUELA CITY", -3, 5, base, rand);
        addLegacy(list, "Eunice Money Changer – Poblacion Rd", "VALENZUELA CITY", -4, 6, base, rand);
        addLegacy(list, "Steward Money Changer – 254 MacArthur Hwy", "VALENZUELA CITY", 5, -2, base, rand);
        addLegacy(list, "Sweet Rate Money Changer – Pio Valenzuela", "VALENZUELA CITY", -8, 8, base, rand);
        addLegacy(list, "Xchange – QX7R+WMR", "VALENZUELA CITY", 6, 6, base, rand);
        addLegacy(list, "Prime R8 Foreign Exchange – Royal Family Mall", "VALENZUELA CITY", 10, -5, base, rand);
        addLegacy(list, "Good Rate Money Changer – 1400 Rizal Ave Ext", "VALENZUELA CITY", 5, 12, base, rand);
        addLegacy(list, "Green Line Money Exchange – Poblacion", "VALENZUELA CITY", -5, 5, base, rand);
        addLegacy(list, "Western Union – 197 MacArthur Hwy", "VALENZUELA CITY", 0, 15, base, rand);
        addLegacy(list, "Western Union – Esguerra St", "VALENZUELA CITY", 2, 18, base, rand);
        addLegacy(list, "Western Union – 584 MacArthur Hwy", "VALENZUELA CITY", 1, 16, base, rand);
        addLegacy(list, "Western Union – 215 T. Santiago", "VALENZUELA CITY", 3, 19, base, rand);
        addLegacy(list, "Western Union – BPI Marulas", "VALENZUELA CITY", 4, 14, base, rand);
        addLegacy(list, "Western Union – Villarica Pawnshop", "VALENZUELA CITY", 5, 17, base, rand);
        addLegacy(list, "ML Kwarta Padala – Kadiwa", "VALENZUELA CITY", -12, 0, base, rand);
        addLegacy(list, "M Lhuillier – T. Santiago", "VALENZUELA CITY", -10, 2, base, rand);
        addLegacy(list, "TrueMoney Philippines – G. Lazaro Rd", "VALENZUELA CITY", -8, 4, base, rand);
        addLegacy(list, "Xoom – Banco de Oro / Karuhatan", "VALENZUELA CITY", 7, 7, base, rand);
        addLegacy(list, "Xoom – Km 15 McArthur Hwy", "VALENZUELA CITY", 8, 8, base, rand);
        addLegacy(list, "Xoom – 97 Paso de Blas Rd", "VALENZUELA CITY", 9, 9, base, rand);
        addLegacy(list, "Xoom – 18 Karuhatan Rd", "VALENZUELA CITY", 7, 10, base, rand);
        addLegacy(list, "Globe GCash – MacArthur Hwy", "VALENZUELA CITY", 6, 12, base, rand);
        addLegacy(list, "2GO Asanion – T. Santiago", "VALENZUELA CITY", 4, 13, base, rand);
        addLegacy(list, "MoneyGram – G. Lazaro Rd", "VALENZUELA CITY", -9, 5, base, rand);
        addLegacy(list, "MoneyGram – T. Santiago", "VALENZUELA CITY", -11, 3, base, rand);

        // MALABON
        addLegacy(list, "CVM Money Changer – Santulan", "MALABON CITY", 5, -5, base, rand);
        addLegacy(list, "Best Rate Money Changer – ESO Malabon", "MALABON CITY", 0, 0, base, rand);
        addLegacy(list, "Florencio Foreign Exchange", "MALABON CITY", 2, 2, base, rand);
        addLegacy(list, "Goddess Money Changer – Araneta Square", "MALABON CITY", 8, 5, base, rand);
        addLegacy(list, "Lhuillier Foreign Exchange – Gov. Pascual", "MALABON CITY", -5, 5, base, rand);

        // CALOOCAN
        addLegacy(list, "Chern Money Changer – Bonifacio Mkt", "CALOOCAN CITY", 0, 0, base, rand);
        addLegacy(list, "Moromax Foreign Exchange – Grand Central", "CALOOCAN CITY", 2, 5, base, rand);
        addLegacy(list, "Moromax Foreign Exchange – Guido St", "CALOOCAN CITY", 3, 6, base, rand);
        addLegacy(list, "Ar-Ma Money Changer – LRT Monumento", "CALOOCAN CITY", 5, 2, base, rand);
        addLegacy(list, "Julliana Money Changer – Parco Supermarket", "CALOOCAN CITY", -5, 8, base, rand);
        addLegacy(list, "Angeli Money Changer – Rodmall Plaza", "CALOOCAN CITY", 6, 6, base, rand);
        addLegacy(list, "Jusco Money Changer – Amparo Novaville", "CALOOCAN CITY", 20, -20, base, rand);
        addLegacy(list, "Standard Foreign Exchange – San Bartolome", "CALOOCAN CITY", 15, -15, base, rand);
        addLegacy(list, "Annika’s Money Changer – Reparo Rd", "CALOOCAN CITY", -8, 10, base, rand);
        addLegacy(list, "My-Ann Money Changer – New Antipolo St", "CALOOCAN CITY", -3, 11, base, rand);
        addLegacy(list, "RJF Money Changer – Bisig ng Nayon", "CALOOCAN CITY", 12, -10, base, rand);
        addLegacy(list, "Perez Money Changer – Unang Hakbang", "CALOOCAN CITY", -2, 12, base, rand);
        addLegacy(list, "Xoom – 7th Ave", "CALOOCAN CITY", 4, 4, base, rand);
        addLegacy(list, "Xoom – KHO Building, Balintawak", "CALOOCAN CITY", 5, 3, base, rand);
        addLegacy(list, "Xoom – BPI Grace Park", "CALOOCAN CITY", 6, 5, base, rand);
        addLegacy(list, "Coinstar Money Transfer – Malhacan", "CALOOCAN CITY", 7, 8, base, rand);

        // QUEZON CITY
        addLegacy(list, "Tivoli Money Exchange – Trinoma", "QUEZON CITY", 0, 0, base, rand);
        addLegacy(list, "Tivoli Money Exchange – Glorietta (Branch)", "QUEZON CITY", 2, -5, base, rand);
        addLegacy(list, "A. K. Q. Foreign Exchange – West Ave", "QUEZON CITY", -5, 2, base, rand);
        addLegacy(list, "Moonlight Money Changer – A. Mabini", "QUEZON CITY", -2, 4, base, rand);
        addLegacy(list, "Sheeha Money Changer – A. Mabini", "QUEZON CITY", -2, 5, base, rand);
        addLegacy(list, "Sweet Money Changer – UN Ave (QC Branch)", "QUEZON CITY", -1, 3, base, rand);
        addLegacy(list, "Nikko Forex Money Changer – A. Mabini", "QUEZON CITY", -3, 6, base, rand);
        addLegacy(list, "Emerald Money Changer – Gen. Roxas Ave", "QUEZON CITY", 10, 5, base, rand);
        addLegacy(list, "World Wide Enterprises – A. Mabini", "QUEZON CITY", -4, 5, base, rand);
        addLegacy(list, "3 Aces Money Changer – Congressional Ave", "QUEZON CITY", 5, -8, base, rand);
        addLegacy(list, "Monies Money Changer – Samson Rd", "QUEZON CITY", -15, 0, base, rand);
        addLegacy(list, "Core Pacific – Robinsons Novaliches", "QUEZON CITY", 15, -15, base, rand);
        addLegacy(list, "Corinne Money Changer – Fairview Center Mall", "QUEZON CITY", 20, -20, base, rand);
        addLegacy(list, "Money Changer – SM Fairview", "QUEZON CITY", 22, -22, base, rand);
        addLegacy(list, "BSDC Foreign Exchange – N.S. Amoranto Ave", "QUEZON CITY", -8, 8, base, rand);
        addLegacy(list, "Jovy Money Changer – Congressional Ave", "QUEZON CITY", 4, -9, base, rand);
        addLegacy(list, "Mina Money Changer – Timog Ave", "QUEZON CITY", 0, 10, base, rand);
        addLegacy(list, "Moromax Money Changer – Ever Gotesco", "QUEZON CITY", 12, -5, base, rand);
        addLegacy(list, "NCEE Money Changer – Quirino Hwy", "QUEZON CITY", 14, -14, base, rand);
        
        // MANILA
        addLegacy(list, "SHEEHA Money Changer – A. Mabini St", "MANILA CITY", 0, 0, base, rand);
        addLegacy(list, "Sweet Money Changer – UN Ave", "MANILA CITY", 2, 2, base, rand);
        addLegacy(list, "Nikko Forex – A. Mabini", "MANILA CITY", 0, 3, base, rand);
        addLegacy(list, "My-Ann Money Changer – Tondo", "MANILA CITY", -5, -5, base, rand);
        addLegacy(list, "Mammoth Money Exchange – San Agustin", "MANILA CITY", 5, 5, base, rand);
        addLegacy(list, "Lady Diamond Foreign Exchange – Padre Faura", "MANILA CITY", 3, 3, base, rand);
        addLegacy(list, "Akiro Foreign Exchange – Tayuman", "MANILA CITY", -2, -2, base, rand);
        addLegacy(list, "Perez Money Changer – Tondo", "MANILA CITY", -6, -6, base, rand);

        // BULACAN
        addLegacy(list, "Western Union – Aliw Complex (Meycauayan)", "BULACAN PROVINCE", 0, 0, base, rand);
        addLegacy(list, "Ralen’s Money Changer – Calvario (Meycauayan)", "BULACAN PROVINCE", 2, -2, base, rand);
        addLegacy(list, "Coinstar Money Transfer – Liberty Bldg (Meycauayan)", "BULACAN PROVINCE", 4, 0, base, rand);
        addLegacy(list, "Jed Xen Money Changer – Iba Rd (Meycauayan)", "BULACAN PROVINCE", 6, -5, base, rand);
        addLegacy(list, "Coinstar – Northern Hills (Meycauayan)", "BULACAN PROVINCE", 7, -3, base, rand);
        addLegacy(list, "BDO Meycauayan – Zamora Branch", "BULACAN PROVINCE", 1, -1, base, rand);
        addLegacy(list, "Pmpco Foreign Exchange – M. Villarica Rd (Meycauayan)", "BULACAN PROVINCE", 3, 5, base, rand);
        addLegacy(list, "Xoom – BPI Grace Park (Meycauayan area)", "BULACAN PROVINCE", 5, 3, base, rand);
        addLegacy(list, "CVM Pawnshop Money Changer – Santa Monica (Meycauayan)", "BULACAN PROVINCE", 5, 2, base, rand);
        addLegacy(list, "Anel’s Money Changer – McArthur Hwy (Marilao)", "BULACAN PROVINCE", 0, -20, base, rand);
        addLegacy(list, "Mc Forex Money Changer – Grand Rial (Marilao)", "BULACAN PROVINCE", 2, -18, base, rand);
        addLegacy(list, "Coinstar – Cebuana Lhuillier (Marilao)", "BULACAN PROVINCE", 4, -21, base, rand);
        addLegacy(list, "John Yerffej Money Changer – General Luis (Marilao)", "BULACAN PROVINCE", 5, -22, base, rand);
        addLegacy(list, "MoneyGram – Marilao", "BULACAN PROVINCE", 6, -17, base, rand);
        addLegacy(list, "Tesorero Foreign Exchange – Francis Market (Marilao)", "BULACAN PROVINCE", 3, -19, base, rand);
        addLegacy(list, "Aserehe Forex – Santa Maria", "BULACAN PROVINCE", 15, -30, base, rand);
        addLegacy(list, "Green Line Money Exchange – Poblacion (Bulacan)", "BULACAN PROVINCE", 10, 10, base, rand);
        addLegacy(list, "Sweet Rate Money Changer – Pio Valenzuela (Bulacan area)", "BULACAN PROVINCE", -2, -5, base, rand);
        addLegacy(list, "Julliana Money Changer – Various (Bulacan)", "BULACAN PROVINCE", 12, 12, base, rand);
        addLegacy(list, "Ashley Money Exchange – Quirino Hwy (QC/Bulacan border)", "BULACAN PROVINCE", -5, 15, base, rand);

        return list;
    }

    private void addLegacy(java.util.List<MoneyChanger> list, String name, String city, int oldX, int oldY, Map<String, Double> base, Random r) {
        double nx = 0.5 + (oldX / 50.0);
        double ny = 0.5 + (oldY / 50.0);
        nx = Math.max(0.05, Math.min(0.95, nx));
        ny = Math.max(0.05, Math.min(0.95, ny));
        add(list, name, city, nx, ny, base, r);
    }

    private void add(java.util.List<MoneyChanger> list, String name, String city, double nx, double ny, Map<String, Double> base, Random r) {
        Map<String, Double> rates = new HashMap<>();
        for (String k : base.keySet()) rates.put(k, base.get(k) + (r.nextDouble() - 0.5));
        list.add(new MoneyChanger(name, city, nx, ny, rates, r));
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MoneyChangerLocatorSwing().setVisible(true));
    }
}