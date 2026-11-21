import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.List;

public class CampusExchangeSystem extends JFrame {

    private static final Color SIDEBAR_BG = new Color(46, 38, 35), SIDEBAR_SUB = new Color(60, 50, 46), SIDEBAR_HOVER = new Color(74, 62, 57);
    private static final Color BG_DIRTY_WHITE = new Color(247, 244, 238), CARD_CREAM = new Color(255, 253, 249), INPUT_CREAM = new Color(242, 237, 229);
    private static final Color BORDER_WARM = new Color(228, 217, 206), PEACH_ACCENT = new Color(244, 151, 108), PEACH_HOVER = new Color(235, 132, 86);
    private static final Color PEACH_SOFT = new Color(253, 232, 219), BADGE_GREEN_BG = new Color(214, 242, 224), BADGE_GREEN_FG = new Color(34, 128, 72);
    private static final Color DANGER_BG = new Color(252, 228, 224), DANGER_HOVER = new Color(246, 206, 198), DANGER_TEXT = new Color(180, 62, 52);
    private static final Color TEXT_DARK = new Color(56, 48, 44), TEXT_MUTED = new Color(130, 118, 110);

    static class Student {
        final String studentId, fullName, courseYear, contactInfo, password;
        Student(String id, String name, String course, String contact, String pass) {
            studentId = id; fullName = name; courseYear = course; contactInfo = contact; password = pass;
        }
        String toCsv() { return String.join("|", studentId, fullName, courseYear, contactInfo, password); }
        static Student fromCsv(String line) {
            String[] p = line.split("\\|", -1);
            return p.length < 5 ? null : new Student(p[0], p[1], p[2], p[3], p[4]);
        }
    }

    static class ExchangeItem {
        final int itemId;
        final String category, itemName, condition, pickupSpot, donorName, donorContact;
        String status, claimedBy;
        ExchangeItem(int id, String cat, String name, String cond, String spot, String donor, String contact, String st, String claimed) {
            itemId = id; category = cat; itemName = name; condition = cond; pickupSpot = spot;
            donorName = donor; donorContact = contact; status = st; claimedBy = claimed;
        }
        void claim(Student s) { status = "CLAIMED"; claimedBy = s.fullName + " (" + s.studentId + ")"; }
        String toCsv() { return String.join("|", String.valueOf(itemId), category, itemName, condition, pickupSpot, donorName, donorContact, status, claimedBy); }
        static ExchangeItem fromCsv(String line) {
            String[] p = line.split("\\|", -1);
            return p.length < 9 ? null : new ExchangeItem(Integer.parseInt(p[0]), p[1], p[2], p[3], p[4], p[5], p[6], p[7], p[8]);
        }
    }

    static class ItemRequest {
        final int requestId;
        final String category, neededItem, urgency, requesterName, requesterContact;
        String status, fulfilledBy;
        ItemRequest(int id, String cat, String item, String urg, String req, String contact, String st, String fulfilled) {
            requestId = id; category = cat; neededItem = item; urgency = urg;
            requesterName = req; requesterContact = contact; status = st; fulfilledBy = fulfilled;
        }
        void fulfill(Student s) { status = "FULFILLED"; fulfilledBy = s.fullName + " (" + s.studentId + ")"; }
        String toCsv() { return String.join("|", String.valueOf(requestId), category, neededItem, urgency, requesterName, requesterContact, status, fulfilledBy); }
        static ItemRequest fromCsv(String line) {
            String[] p = line.split("\\|", -1);
            return p.length < 8 ? null : new ItemRequest(Integer.parseInt(p[0]), p[1], p[2], p[3], p[4], p[5], p[6], p[7]);
        }
    }

    static class DataStorage {
        private static final String STUDENTS_FILE = "students_db.csv", ITEMS_FILE = "exchange_items_db.csv", REQUESTS_FILE = "item_requests_db.csv";

        static List<Student> loadStudents() {
            List<Student> list = new ArrayList<>();
            Path path = Paths.get(STUDENTS_FILE);
            if (!Files.exists(path)) {
                list.add(new Student("2026-00001", "Juan Dela Cruz", "BSIT - 3rd Yr", "0917-123-4567", "1234"));
                saveStudents(list);
                return list;
            }
            try (BufferedReader br = Files.newBufferedReader(path)) {
                for (String line; (line = br.readLine()) != null; ) { Student s = Student.fromCsv(line); if (s != null) list.add(s); }
            } catch (IOException e) { e.printStackTrace(); }
            return list;
        }

        static void saveStudents(List<Student> students) {
            try (BufferedWriter bw = Files.newBufferedWriter(Paths.get(STUDENTS_FILE))) {
                for (Student s : students) { bw.write(s.toCsv()); bw.newLine(); }
            } catch (IOException e) { e.printStackTrace(); }
        }

        static List<ExchangeItem> loadItems() {
            List<ExchangeItem> list = new ArrayList<>();
            Path path = Paths.get(ITEMS_FILE);
            if (!Files.exists(path)) {
                list.add(new ExchangeItem(101, "Textbook", "Object-Oriented Programming in Java", "Good", "Main Library Lobby", "Maria Clara (BSCS 4)", "0918-222-3333", "AVAILABLE", "-"));
                list.add(new ExchangeItem(102, "Uniform", "College Polo Uniform - Medium (2 pcs)", "Like New", "Student Council Office", "Jose Rizal (BSBA 4)", "FB: Jose Rizal", "AVAILABLE", "-"));
                list.add(new ExchangeItem(103, "Lab Gear", "Scientific Calculator (Casio fx-991ES)", "Good", "Engineering Bldg. Lobby", "Ana Santos (BSCE 3)", "0919-444-5555", "AVAILABLE", "-"));
                saveItems(list);
                return list;
            }
            try (BufferedReader br = Files.newBufferedReader(path)) {
                for (String line; (line = br.readLine()) != null; ) { ExchangeItem item = ExchangeItem.fromCsv(line); if (item != null) list.add(item); }
            } catch (IOException e) { e.printStackTrace(); }
            return list;
        }

        static void saveItems(List<ExchangeItem> items) {
            try (BufferedWriter bw = Files.newBufferedWriter(Paths.get(ITEMS_FILE))) {
                for (ExchangeItem item : items) { bw.write(item.toCsv()); bw.newLine(); }
            } catch (IOException e) { e.printStackTrace(); }
        }

        static List<ItemRequest> loadRequests() {
            List<ItemRequest> list = new ArrayList<>();
            Path path = Paths.get(REQUESTS_FILE);
            if (!Files.exists(path)) {
                list.add(new ItemRequest(201, "Uniform", "PE Uniform Jogging Pants - Large", "Urgent (This Week)", "Liza Soberano (BSIT 1)", "0916-777-8888", "OPEN", "-"));
                list.add(new ItemRequest(202, "Textbook", "Discrete Mathematics 7th Edition", "Normal", "Miguel Tan (BSCS 2)", "FB: Miguel Tan", "OPEN", "-"));
                saveRequests(list);
                return list;
            }
            try (BufferedReader br = Files.newBufferedReader(path)) {
                for (String line; (line = br.readLine()) != null; ) { ItemRequest req = ItemRequest.fromCsv(line); if (req != null) list.add(req); }
            } catch (IOException e) { e.printStackTrace(); }
            return list;
        }

        static void saveRequests(List<ItemRequest> requests) {
            try (BufferedWriter bw = Files.newBufferedWriter(Paths.get(REQUESTS_FILE))) {
                for (ItemRequest req : requests) { bw.write(req.toCsv()); bw.newLine(); }
            } catch (IOException e) { e.printStackTrace(); }
        }
    }

    static class RoundedButton extends JButton {
        private Color baseColor, hoverColor, borderColor;
        private int radius = 10;

        RoundedButton(String text, Color base, Color hover, Color textCol, Color border) {
            super(text);
            updateColors(base, hover, textCol, border);
            setFont(new Font("SansSerif", Font.BOLD, 12));
            setFocusPainted(false); setBorderPainted(false); setContentAreaFilled(false); setOpaque(false);
            setCursor(new Cursor(Cursor.HAND_CURSOR));
            setBorder(new EmptyBorder(8, 14, 8, 14));
            addMouseListener(new MouseAdapter() {
                public void mouseEntered(MouseEvent e) { setBackground(hoverColor); repaint(); }
                public void mouseExited(MouseEvent e) { setBackground(baseColor); repaint(); }
            });
        }

        void setRadius(int r) { radius = r; }

        void updateColors(Color base, Color hover, Color textCol, Color border) {
            baseColor = base; hoverColor = hover; borderColor = border;
            setForeground(textCol); setBackground(base); repaint();
        }

        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(getBackground());
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), radius, radius);
            if (borderColor != null) { g2.setColor(borderColor); g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, radius, radius); }
            g2.dispose();
            super.paintComponent(g);
        }
    }

    private final List<Student> students = DataStorage.loadStudents();
    private final List<ExchangeItem> items = DataStorage.loadItems();
    private final List<ItemRequest> requests = DataStorage.loadRequests();
    private Student currentUser;

    private final CardLayout cardLayout = new CardLayout(), contentCardLayout = new CardLayout();
    private final JPanel mainContainer = new JPanel(cardLayout), contentCards = new JPanel(contentCardLayout);
    private JPanel sidebarPanel;
    private JLabel topUserLabel, pageTitleLabel, statusBadgeLabel, courseMetaLabel, idMetaLabel, statsLabel, previewBannerLabel;
    private JTextField searchField;
    private String selectedCategory = "All Categories";
    private int activeSection = 0;

    private final List<RoundedButton> navButtons = new ArrayList<>(), categoryButtons = new ArrayList<>();
    private RoundedButton primaryAddBtn, secondaryActionBtn, deleteActionBtn;
    private DefaultTableModel itemsTableModel, requestsTableModel;
    private JTable itemsTable, requestsTable;

    public CampusExchangeSystem() {
        UIManager.put("OptionPane.background", CARD_CREAM);
        UIManager.put("Panel.background", CARD_CREAM);
        UIManager.put("OptionPane.messageForeground", TEXT_DARK);
        UIManager.put("Label.foreground", TEXT_DARK);
        UIManager.put("Button.background", PEACH_ACCENT);
        UIManager.put("Button.foreground", TEXT_DARK);

        setTitle("Campus Book & Uniform Exchange System");
        setSize(1120, 680);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        mainContainer.add(buildLoginPanel(), "LOGIN");
        mainContainer.add(buildDashboardPanel(), "DASHBOARD");
        add(mainContainer);
        cardLayout.show(mainContainer, "LOGIN");
    }

    private <T extends JTextField> T styleInput(T field) {
        field.setBackground(INPUT_CREAM);
        field.setForeground(TEXT_DARK);
        field.setCaretColor(PEACH_HOVER);
        field.setFont(new Font("SansSerif", Font.PLAIN, 13));
        field.setBorder(BorderFactory.createCompoundBorder(new LineBorder(BORDER_WARM, 1, true), new EmptyBorder(6, 10, 6, 10)));
        return field;
    }

    private JComboBox<String> styleCombo(String... options) {
        JComboBox<String> box = new JComboBox<>(options);
        box.setBackground(INPUT_CREAM);
        box.setForeground(TEXT_DARK);
        box.setFont(new Font("SansSerif", Font.PLAIN, 13));
        box.setBorder(new LineBorder(BORDER_WARM, 1, true));
        return box;
    }

    private JLabel makeLabel(String text, int style, int size, Color color) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("SansSerif", style, size));
        lbl.setForeground(color);
        return lbl;
    }

    private JScrollPane createStyledTableScroll(JTable table, int statusCol, String activeKey, int[] widths) {
        table.setRowHeight(38);
        table.setBackground(CARD_CREAM);
        table.setForeground(TEXT_DARK);
        table.setShowVerticalLines(false);
        table.setGridColor(BORDER_WARM);
        table.setSelectionBackground(PEACH_SOFT);
        table.setSelectionForeground(TEXT_DARK);
        table.setFont(new Font("SansSerif", Font.PLAIN, 12));
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JTableHeader header = table.getTableHeader();
        header.setBackground(CARD_CREAM);
        header.setForeground(TEXT_DARK);
        header.setFont(new Font("SansSerif", Font.BOLD, 12));
        header.setBorder(new MatteBorder(0, 0, 2, 0, BORDER_WARM));
        header.setPreferredSize(new Dimension(header.getWidth(), 36));

        table.getColumnModel().getColumn(statusCol).setCellRenderer(new DefaultTableCellRenderer() {
            public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int r, int c) {
                Component comp = super.getTableCellRendererComponent(t, v, sel, foc, r, c);
                setHorizontalAlignment(SwingConstants.CENTER);
                setFont(new Font("SansSerif", Font.BOLD, 11));
                setForeground(activeKey.equals(v) ? new Color(204, 92, 48) : TEXT_MUTED);
                if (!sel) comp.setBackground(CARD_CREAM);
                return comp;
            }
        });
        for (int i = 0; i < widths.length; i++) table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        table.getSelectionModel().addListSelectionListener(e -> { if (!e.getValueIsAdjusting()) updatePreviewBanner(); });

        JScrollPane scroll = new JScrollPane(table);
        scroll.getViewport().setBackground(CARD_CREAM);
        scroll.setBorder(new LineBorder(BORDER_WARM, 1, true));
        return scroll;
    }

    private JPanel buildLoginPanel() {
        JPanel wrapper = new JPanel(new GridBagLayout());
        wrapper.setBackground(BG_DIRTY_WHITE);

        JPanel card = new JPanel(new GridBagLayout());
        card.setBackground(CARD_CREAM);
        card.setBorder(BorderFactory.createCompoundBorder(new LineBorder(PEACH_ACCENT, 1, true), new EmptyBorder(24, 28, 24, 28)));
        card.setPreferredSize(new Dimension(330, 325));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;

        JLabel title = new JLabel("Campus Exchange", SwingConstants.CENTER);
        title.setFont(new Font("SansSerif", Font.BOLD, 19));
        title.setForeground(new Color(214, 102, 58));

        JLabel subtitle = new JLabel("Book & Uniform Student Portal", SwingConstants.CENTER);
        subtitle.setFont(new Font("SansSerif", Font.PLAIN, 11));
        subtitle.setForeground(TEXT_MUTED);

        JTextField idField = styleInput(new JTextField("2026-00001", 15));
        idField.setPreferredSize(new Dimension(260, 32));
        JPasswordField passField = styleInput(new JPasswordField("1234"));
        passField.setPreferredSize(new Dimension(260, 32));

        RoundedButton loginBtn = new RoundedButton("Login", PEACH_ACCENT, PEACH_HOVER, TEXT_DARK, null);
        loginBtn.setPreferredSize(new Dimension(260, 34));
        RoundedButton registerBtn = new RoundedButton("Create Student Account", PEACH_SOFT, PEACH_ACCENT, TEXT_DARK, PEACH_ACCENT);
        registerBtn.setPreferredSize(new Dimension(260, 34));

        gbc.gridy = 0; gbc.insets = new Insets(0, 0, 2, 0); card.add(title, gbc);
        gbc.gridy = 1; gbc.insets = new Insets(0, 0, 16, 0); card.add(subtitle, gbc);
        gbc.gridy = 2; gbc.insets = new Insets(0, 0, 4, 0); card.add(makeLabel("Student ID", Font.BOLD, 11, TEXT_DARK), gbc);
        gbc.gridy = 3; gbc.insets = new Insets(0, 0, 10, 0); card.add(idField, gbc);
        gbc.gridy = 4; gbc.insets = new Insets(0, 0, 4, 0); card.add(makeLabel("Password", Font.BOLD, 11, TEXT_DARK), gbc);
        gbc.gridy = 5; gbc.insets = new Insets(0, 0, 16, 0); card.add(passField, gbc);
        gbc.gridy = 6; gbc.insets = new Insets(0, 0, 8, 0); card.add(loginBtn, gbc);
        gbc.gridy = 7; gbc.insets = new Insets(0, 0, 0, 0); card.add(registerBtn, gbc);

        loginBtn.addActionListener(e -> {
            String id = idField.getText().trim(), pass = new String(passField.getPassword()).trim();
            for (Student s : students) {
                if (s.studentId.equalsIgnoreCase(id) && s.password.equals(pass)) {
                    currentUser = s;
                    topUserLabel.setText("  " + s.fullName + "  ▾");
                    courseMetaLabel.setText(s.courseYear);
                    idMetaLabel.setText(s.studentId);
                    switchSection(0);
                    cardLayout.show(mainContainer, "DASHBOARD");
                    return;
                }
            }
            JOptionPane.showMessageDialog(this, "Invalid Student ID or Password.\nDemo account: 2026-00001 / 1234", "Login Failed", JOptionPane.ERROR_MESSAGE);
        });
        registerBtn.addActionListener(e -> showRegisterDialog());

        wrapper.add(card);
        return wrapper;
    }

    private JDialog createFormDialog(String titleText, String subtitleText, int width, int height, String[] labels, JComponent[] inputs, String submitText, Runnable onSubmit) {
        JDialog dialog = new JDialog(this, titleText, true);
        dialog.setUndecorated(true);
        dialog.setSize(width, height);
        dialog.setLocationRelativeTo(this);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(CARD_CREAM);
        root.setBorder(BorderFactory.createCompoundBorder(new LineBorder(PEACH_ACCENT, 2, true), new EmptyBorder(20, 26, 20, 26)));

        JPanel headerPanel = new JPanel(new GridLayout(0, 1, 0, 2));
        headerPanel.setBackground(CARD_CREAM);
        headerPanel.setBorder(new EmptyBorder(0, 0, 12, 0));
        JLabel title = new JLabel(titleText, SwingConstants.CENTER);
        title.setFont(new Font("SansSerif", Font.BOLD, 17));
        title.setForeground(new Color(214, 102, 58));
        JLabel sub = new JLabel(subtitleText, SwingConstants.CENTER);
        sub.setFont(new Font("SansSerif", Font.PLAIN, 11));
        sub.setForeground(TEXT_MUTED);
        headerPanel.add(title); headerPanel.add(sub);

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(CARD_CREAM);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;

        for (int i = 0; i < labels.length; i++) {
            gbc.gridy = i * 2; gbc.insets = new Insets(0, 0, 3, 0); formPanel.add(makeLabel(labels[i], Font.BOLD, 11, TEXT_DARK), gbc);
            inputs[i].setPreferredSize(new Dimension(width - 60, 32));
            gbc.gridy = i * 2 + 1; gbc.insets = new Insets(0, 0, 10, 0); formPanel.add(inputs[i], gbc);
        }

        JPanel btnPanel = new JPanel(new GridLayout(1, 2, 10, 0));
        btnPanel.setBackground(CARD_CREAM);
        btnPanel.setBorder(new EmptyBorder(8, 0, 0, 0));
        RoundedButton cancelBtn = new RoundedButton("Cancel", INPUT_CREAM, PEACH_SOFT, TEXT_DARK, BORDER_WARM);
        RoundedButton saveBtn = new RoundedButton(submitText, PEACH_ACCENT, PEACH_HOVER, TEXT_DARK, null);
        cancelBtn.addActionListener(e -> dialog.dispose());
        saveBtn.addActionListener(e -> onSubmit.run());
        btnPanel.add(cancelBtn); btnPanel.add(saveBtn);

        root.add(headerPanel, BorderLayout.NORTH);
        root.add(formPanel, BorderLayout.CENTER);
        root.add(btnPanel, BorderLayout.SOUTH);
        dialog.setContentPane(root);
        return dialog;
    }

    private void showRegisterDialog() {
        JTextField idField = styleInput(new JTextField(16)), nameField = styleInput(new JTextField(16));
        JTextField courseField = styleInput(new JTextField(16)), contactField = styleInput(new JTextField(16));
        JPasswordField passField = styleInput(new JPasswordField());
        String[] labels = {"Student ID (e.g., 2026-00123)", "Full Name", "Course & Year (e.g., BSIT - 2nd Yr)", "Contact (Mobile / Messenger)", "Password"};
        JComponent[] inputs = {idField, nameField, courseField, contactField, passField};

        final JDialog[] ref = new JDialog[1];
        ref[0] = createFormDialog("Create Student Account", "Register with your campus details", 370, 440, labels, inputs, "Register", () -> {
            String id = idField.getText().trim(), name = nameField.getText().trim(), pass = new String(passField.getPassword()).trim();
            if (id.isEmpty() || name.isEmpty() || pass.isEmpty()) { JOptionPane.showMessageDialog(ref[0], "Student ID, Name, and Password are required."); return; }
            for (Student s : students) { if (s.studentId.equalsIgnoreCase(id)) { JOptionPane.showMessageDialog(ref[0], "Student ID already registered!"); return; } }
            students.add(new Student(id, name, courseField.getText().trim(), contactField.getText().trim(), pass));
            DataStorage.saveStudents(students);
            ref[0].dispose();
            JOptionPane.showMessageDialog(this, "Account created! You can now log in.");
        });
        ref[0].setVisible(true);
    }

    private JPanel buildDashboardPanel() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(BG_DIRTY_WHITE);

        sidebarPanel = new JPanel(new BorderLayout());
        sidebarPanel.setBackground(SIDEBAR_BG);
        sidebarPanel.setPreferredSize(new Dimension(225, 0));

        JPanel brandBox = new JPanel(new BorderLayout());
        brandBox.setBackground(SIDEBAR_BG);
        brandBox.setBorder(new EmptyBorder(18, 20, 14, 20));
        brandBox.add(makeLabel("✦  CAMPUS EXCHANGE", Font.BOLD, 14, PEACH_ACCENT), BorderLayout.CENTER);

        JPanel subHeader = new JPanel(new BorderLayout());
        subHeader.setBackground(SIDEBAR_SUB);
        subHeader.setBorder(new EmptyBorder(10, 20, 10, 20));
        subHeader.add(makeLabel("‹  Student Portal", Font.PLAIN, 12, PEACH_SOFT), BorderLayout.CENTER);

        JPanel sideTop = new JPanel(new BorderLayout());
        sideTop.setBackground(SIDEBAR_BG);
        sideTop.add(brandBox, BorderLayout.NORTH);
        sideTop.add(subHeader, BorderLayout.SOUTH);

        JPanel navList = new JPanel();
        navList.setLayout(new BoxLayout(navList, BoxLayout.Y_AXIS));
        navList.setBackground(SIDEBAR_BG);
        navList.setBorder(new EmptyBorder(14, 12, 14, 12));

        String[] navLabels = {"⇄   Available Donations", "♡   Student Wishlist", "+   Donate an Item", "✎   Request an Item"};
        navButtons.clear();
        for (int i = 0; i < navLabels.length; i++) {
            final int idx = i;
            RoundedButton navBtn = new RoundedButton(navLabels[i], SIDEBAR_BG, SIDEBAR_HOVER, PEACH_SOFT, null);
            navBtn.setRadius(8);
            navBtn.setHorizontalAlignment(SwingConstants.LEFT);
            navBtn.setMaximumSize(new Dimension(205, 38));
            navBtn.setBorder(new EmptyBorder(9, 14, 9, 14));
            navBtn.addActionListener(e -> handleSidebarAction(idx));
            navButtons.add(navBtn);
            navList.add(navBtn);
            navList.add(Box.createVerticalStrut(4));
        }

        JPanel sideBottom = new JPanel(new BorderLayout());
        sideBottom.setBackground(SIDEBAR_BG);
        sideBottom.setBorder(new EmptyBorder(12, 12, 16, 12));
        RoundedButton logoutBtn = new RoundedButton("⎋   Logout", SIDEBAR_SUB, SIDEBAR_HOVER, PEACH_SOFT, null);
        logoutBtn.setRadius(8);
        logoutBtn.setHorizontalAlignment(SwingConstants.LEFT);
        logoutBtn.addActionListener(e -> { currentUser = null; cardLayout.show(mainContainer, "LOGIN"); });
        sideBottom.add(logoutBtn, BorderLayout.CENTER);

        sidebarPanel.add(sideTop, BorderLayout.NORTH);
        sidebarPanel.add(navList, BorderLayout.CENTER);
        sidebarPanel.add(sideBottom, BorderLayout.SOUTH);

        JPanel rightArea = new JPanel(new BorderLayout());
        rightArea.setBackground(BG_DIRTY_WHITE);

        JPanel topNavBar = new JPanel(new BorderLayout());
        topNavBar.setBackground(CARD_CREAM);
        topNavBar.setBorder(BorderFactory.createCompoundBorder(new MatteBorder(0, 0, 1, 0, BORDER_WARM), new EmptyBorder(10, 20, 10, 24)));

        RoundedButton menuToggleBtn = new RoundedButton("☰", SIDEBAR_BG, SIDEBAR_HOVER, PEACH_SOFT, null);
        menuToggleBtn.setRadius(6);
        menuToggleBtn.setBorder(new EmptyBorder(6, 11, 6, 11));
        menuToggleBtn.addActionListener(e -> sidebarPanel.setVisible(!sidebarPanel.isVisible()));

        JPanel topRightTools = new JPanel(new FlowLayout(FlowLayout.RIGHT, 14, 0));
        topRightTools.setBackground(CARD_CREAM);
        JLabel avatarBadge = new JLabel("👤", SwingConstants.CENTER);
        avatarBadge.setOpaque(true);
        avatarBadge.setBackground(PEACH_SOFT);
        avatarBadge.setForeground(TEXT_DARK);
        avatarBadge.setPreferredSize(new Dimension(28, 28));
        avatarBadge.setBorder(new LineBorder(PEACH_ACCENT, 1, true));
        topUserLabel = makeLabel("  Student  ▾", Font.BOLD, 12, TEXT_DARK);

        topRightTools.add(makeLabel("🔔", Font.PLAIN, 13, TEXT_MUTED));
        topRightTools.add(makeLabel("⚙", Font.PLAIN, 13, TEXT_MUTED));
        topRightTools.add(avatarBadge);
        topRightTools.add(topUserLabel);
        topNavBar.add(menuToggleBtn, BorderLayout.WEST);
        topNavBar.add(topRightTools, BorderLayout.EAST);

        JPanel mainBody = new JPanel(new BorderLayout(0, 12));
        mainBody.setBackground(BG_DIRTY_WHITE);
        mainBody.setBorder(new EmptyBorder(20, 28, 16, 28));

        JPanel headerBlock = new JPanel(new BorderLayout(0, 8));
        headerBlock.setBackground(BG_DIRTY_WHITE);
        pageTitleLabel = makeLabel("Available Donations", Font.PLAIN, 22, new Color(214, 102, 58));

        JPanel metaRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        metaRow.setBackground(BG_DIRTY_WHITE);
        statusBadgeLabel = new JLabel(" active ");
        statusBadgeLabel.setOpaque(true);
        statusBadgeLabel.setBackground(BADGE_GREEN_BG);
        statusBadgeLabel.setForeground(BADGE_GREEN_FG);
        statusBadgeLabel.setFont(new Font("SansSerif", Font.BOLD, 11));
        statusBadgeLabel.setBorder(new EmptyBorder(2, 6, 2, 6));
        courseMetaLabel = makeLabel("BSIT - 3rd Yr", Font.BOLD, 12, TEXT_DARK);
        idMetaLabel = makeLabel("2026-00001", Font.BOLD, 12, TEXT_DARK);

        metaRow.add(makeLabel("status:", Font.PLAIN, 12, TEXT_MUTED)); metaRow.add(statusBadgeLabel);
        metaRow.add(makeLabel("   course:", Font.PLAIN, 12, TEXT_MUTED)); metaRow.add(courseMetaLabel);
        metaRow.add(makeLabel("   student id:", Font.PLAIN, 12, TEXT_MUTED)); metaRow.add(idMetaLabel);
        headerBlock.add(pageTitleLabel, BorderLayout.NORTH);
        headerBlock.add(metaRow, BorderLayout.SOUTH);

        JPanel controlsBlock = new JPanel(new BorderLayout(0, 10));
        controlsBlock.setBackground(BG_DIRTY_WHITE);

        JPanel countAndAddRow = new JPanel(new BorderLayout());
        countAndAddRow.setBackground(BG_DIRTY_WHITE);
        statsLabel = makeLabel("0 active donations in campus exchange", Font.PLAIN, 13, TEXT_MUTED);
        primaryAddBtn = new RoundedButton("+ Donate Item", PEACH_ACCENT, PEACH_HOVER, TEXT_DARK, null);
        primaryAddBtn.setRadius(6);
        countAndAddRow.add(statsLabel, BorderLayout.WEST);
        countAndAddRow.add(primaryAddBtn, BorderLayout.EAST);

        JPanel filterAndSearchBar = new JPanel(new BorderLayout(10, 0));
        filterAndSearchBar.setBackground(CARD_CREAM);
        filterAndSearchBar.setBorder(BorderFactory.createCompoundBorder(new LineBorder(BORDER_WARM, 1, true), new EmptyBorder(6, 10, 6, 10)));

        JPanel catGroup = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        catGroup.setBackground(CARD_CREAM);
        categoryButtons.clear();
        for (String cat : new String[]{"All Categories", "Textbook", "Uniform", "Lab Gear", "Supplies"}) {
            boolean active = cat.equals(selectedCategory);
            RoundedButton catBtn = new RoundedButton(cat, active ? PEACH_ACCENT : INPUT_CREAM, active ? PEACH_HOVER : PEACH_SOFT, TEXT_DARK, active ? PEACH_ACCENT : BORDER_WARM);
            catBtn.setRadius(8);
            catBtn.setBorder(new EmptyBorder(5, 11, 5, 11));
            catBtn.addActionListener(e -> { selectedCategory = cat; updateCategoryButtons(); refreshAllTables(); });
            categoryButtons.add(catBtn);
            catGroup.add(catBtn);
        }

        JPanel searchGroup = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        searchGroup.setBackground(CARD_CREAM);
        searchField = styleInput(new JTextField(16));
        RoundedButton searchBtn = new RoundedButton("Search", PEACH_ACCENT, PEACH_HOVER, TEXT_DARK, null);
        searchBtn.setRadius(8); searchBtn.setBorder(new EmptyBorder(5, 12, 5, 12));
        RoundedButton resetBtn = new RoundedButton("Reset", INPUT_CREAM, PEACH_SOFT, TEXT_DARK, BORDER_WARM);
        resetBtn.setRadius(8); resetBtn.setBorder(new EmptyBorder(5, 10, 5, 10));
        searchGroup.add(searchField); searchGroup.add(searchBtn); searchGroup.add(resetBtn);
        filterAndSearchBar.add(catGroup, BorderLayout.WEST);
        filterAndSearchBar.add(searchGroup, BorderLayout.EAST);

        JPanel previewBanner = new JPanel(new BorderLayout());
        previewBanner.setBackground(PEACH_SOFT);
        previewBanner.setBorder(BorderFactory.createCompoundBorder(new LineBorder(PEACH_ACCENT, 1, true), new EmptyBorder(7, 12, 7, 12)));
        previewBannerLabel = makeLabel("Selected Item Description: Click any row below to view full item details", Font.BOLD, 12, TEXT_DARK);
        previewBanner.add(previewBannerLabel, BorderLayout.CENTER);

        controlsBlock.add(countAndAddRow, BorderLayout.NORTH);
        controlsBlock.add(filterAndSearchBar, BorderLayout.CENTER);
        controlsBlock.add(previewBanner, BorderLayout.SOUTH);

        JPanel topSection = new JPanel(new BorderLayout(0, 14));
        topSection.setBackground(BG_DIRTY_WHITE);
        topSection.add(headerBlock, BorderLayout.NORTH);
        topSection.add(controlsBlock, BorderLayout.SOUTH);

        itemsTableModel = new DefaultTableModel(new String[]{"ID", "Category", "Item Description", "Condition", "Pickup Spot", "Donor", "Contact", "Status", "Claimed By"}, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        itemsTable = new JTable(itemsTableModel);
        JScrollPane itemsScroll = createStyledTableScroll(itemsTable, 7, "AVAILABLE", new int[]{45, 80, 250, 75, 135, 130, 110, 80, 115});

        requestsTableModel = new DefaultTableModel(new String[]{"Req ID", "Category", "Needed Item Description", "Urgency", "Requested By", "Contact", "Status", "Fulfilled By"}, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        requestsTable = new JTable(requestsTableModel);
        JScrollPane requestsScroll = createStyledTableScroll(requestsTable, 6, "OPEN", new int[]{55, 85, 270, 120, 135, 115, 80, 115});

        contentCards.add(itemsScroll, "DONATIONS");
        contentCards.add(requestsScroll, "WISHLIST");

        JPanel bottomActionBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        bottomActionBar.setBackground(BG_DIRTY_WHITE);
        bottomActionBar.setBorder(new EmptyBorder(4, 0, 0, 0));
        secondaryActionBtn = new RoundedButton("Claim Selected", PEACH_ACCENT, PEACH_HOVER, TEXT_DARK, null);
        secondaryActionBtn.setRadius(8);
        deleteActionBtn = new RoundedButton("Delete Selected", DANGER_BG, DANGER_HOVER, DANGER_TEXT, DANGER_TEXT);
        deleteActionBtn.setRadius(8);
        bottomActionBar.add(secondaryActionBtn);
        bottomActionBar.add(deleteActionBtn);

        searchBtn.addActionListener(e -> refreshAllTables());
        resetBtn.addActionListener(e -> { searchField.setText(""); selectedCategory = "All Categories"; updateCategoryButtons(); refreshAllTables(); });
        primaryAddBtn.addActionListener(e -> { if (activeSection == 0) showDonateDialog(); else showRequestDialog(); });
        secondaryActionBtn.addActionListener(e -> handleClaimOrFulfill());
        deleteActionBtn.addActionListener(e -> handleDeleteSelected());

        mainBody.add(topSection, BorderLayout.NORTH);
        mainBody.add(contentCards, BorderLayout.CENTER);
        mainBody.add(bottomActionBar, BorderLayout.SOUTH);

        rightArea.add(topNavBar, BorderLayout.NORTH);
        rightArea.add(mainBody, BorderLayout.CENTER);
        root.add(sidebarPanel, BorderLayout.WEST);
        root.add(rightArea, BorderLayout.CENTER);
        return root;
    }

    private void handleSidebarAction(int idx) {
        if (idx == 0) switchSection(0);
        else if (idx == 1) switchSection(1);
        else if (idx == 2) showDonateDialog();
        else if (idx == 3) showRequestDialog();
    }

    private void switchSection(int section) {
        activeSection = section;
        for (int i = 0; i < navButtons.size(); i++) {
            boolean active = (i == section);
            navButtons.get(i).updateColors(active ? PEACH_ACCENT : SIDEBAR_BG, active ? PEACH_HOVER : SIDEBAR_HOVER, active ? TEXT_DARK : PEACH_SOFT, null);
        }
        if (section == 0) {
            pageTitleLabel.setText("Available Donations");
            primaryAddBtn.setText("+ Donate Item");
            secondaryActionBtn.setText("Claim Selected");
            contentCardLayout.show(contentCards, "DONATIONS");
        } else {
            pageTitleLabel.setText("Student Wishlist & Requests");
            primaryAddBtn.setText("+ Request Item");
            secondaryActionBtn.setText("Fulfill Selected");
            contentCardLayout.show(contentCards, "WISHLIST");
        }
        refreshAllTables();
    }

    private void updatePreviewBanner() {
        if (activeSection == 0 && itemsTable.getSelectedRow() >= 0) {
            int r = itemsTable.getSelectedRow();
            previewBannerLabel.setText(String.format("Selected #%s [%s]: %s  |  Condition: %s  |  Meetup: %s  |  Contact: %s",
                    itemsTableModel.getValueAt(r, 0), itemsTableModel.getValueAt(r, 1), itemsTableModel.getValueAt(r, 2),
                    itemsTableModel.getValueAt(r, 3), itemsTableModel.getValueAt(r, 4), itemsTableModel.getValueAt(r, 6)));
        } else if (activeSection == 1 && requestsTable.getSelectedRow() >= 0) {
            int r = requestsTable.getSelectedRow();
            previewBannerLabel.setText(String.format("Wishlist #%s [%s]: %s  |  Urgency: %s  |  Contact: %s",
                    requestsTableModel.getValueAt(r, 0), requestsTableModel.getValueAt(r, 1), requestsTableModel.getValueAt(r, 2),
                    requestsTableModel.getValueAt(r, 3), requestsTableModel.getValueAt(r, 5)));
        } else {
            previewBannerLabel.setText("Selected Item Description: Click any row below to view full item details");
        }
    }

    private void updateCategoryButtons() {
        for (RoundedButton btn : categoryButtons) {
            boolean active = btn.getText().equals(selectedCategory);
            btn.updateColors(active ? PEACH_ACCENT : INPUT_CREAM, active ? PEACH_HOVER : PEACH_SOFT, TEXT_DARK, active ? PEACH_ACCENT : BORDER_WARM);
        }
    }

    private void refreshAllTables() {
        String kw = searchField.getText().trim().toLowerCase();
        itemsTableModel.setRowCount(0);
        int avail = 0;

        for (ExchangeItem item : items) {
            if ("AVAILABLE".equals(item.status)) avail++;
            boolean matchCat = "All Categories".equals(selectedCategory) || item.category.equalsIgnoreCase(selectedCategory);
            boolean matchKey = kw.isEmpty() || item.itemName.toLowerCase().contains(kw) || item.pickupSpot.toLowerCase().contains(kw) || item.donorName.toLowerCase().contains(kw);
            if (matchCat && matchKey) {
                itemsTableModel.addRow(new Object[]{item.itemId, item.category, item.itemName, item.condition, item.pickupSpot, item.donorName, item.donorContact, item.status, item.claimedBy});
            }
        }

        requestsTableModel.setRowCount(0);
        int openReq = 0;
        for (ItemRequest req : requests) {
            if ("OPEN".equals(req.status)) openReq++;
            boolean matchCat = "All Categories".equals(selectedCategory) || req.category.equalsIgnoreCase(selectedCategory);
            boolean matchKey = kw.isEmpty() || req.neededItem.toLowerCase().contains(kw) || req.requesterName.toLowerCase().contains(kw);
            if (matchCat && matchKey) {
                requestsTableModel.addRow(new Object[]{req.requestId, req.category, req.neededItem, req.urgency, req.requesterName, req.requesterContact, req.status, req.fulfilledBy});
            }
        }

        if (activeSection == 0 && itemsTableModel.getRowCount() > 0) itemsTable.setRowSelectionInterval(0, 0);
        if (activeSection == 1 && requestsTableModel.getRowCount() > 0) requestsTable.setRowSelectionInterval(0, 0);
        updatePreviewBanner();
        statsLabel.setText(activeSection == 0 ? avail + " active donations available for campus exchange" : openReq + " open student wishlist requests waiting for donors");
    }

    private void showDonateDialog() {
        JComboBox<String> catBox = styleCombo("Textbook", "Uniform", "Lab Gear", "Supplies");
        JTextField nameField = styleInput(new JTextField(18));
        JComboBox<String> condBox = styleCombo("Like New", "Good", "Fair");
        JTextField spotField = styleInput(new JTextField("Main Library Lobby", 18));

        final JDialog[] ref = new JDialog[1];
        ref[0] = createFormDialog("Donate an Item", "Share books, uniforms, or gear with students", 370, 390,
                new String[]{"Category", "Item Name / Size / Edition", "Condition", "Campus Meetup Spot"},
                new JComponent[]{catBox, nameField, condBox, spotField}, "Post Donation", () -> {
            String itemName = nameField.getText().trim();
            if (itemName.isEmpty()) { JOptionPane.showMessageDialog(ref[0], "Please enter the item name."); return; }
            int nextId = items.isEmpty() ? 101 : items.get(items.size() - 1).itemId + 1;
            items.add(new ExchangeItem(nextId, (String) catBox.getSelectedItem(), itemName, (String) condBox.getSelectedItem(),
                    spotField.getText().trim(), currentUser.fullName + " (" + currentUser.courseYear + ")", currentUser.contactInfo, "AVAILABLE", "-"));
            DataStorage.saveItems(items);
            ref[0].dispose();
            switchSection(0);
            JOptionPane.showMessageDialog(this, "Thank you! Your item (#" + nextId + ") is now listed.");
        });
        ref[0].setVisible(true);
    }

    private void showRequestDialog() {
        JComboBox<String> catBox = styleCombo("Textbook", "Uniform", "Lab Gear", "Supplies");
        JTextField itemField = styleInput(new JTextField(18));
        JComboBox<String> urgencyBox = styleCombo("Normal", "Urgent (This Week)", "Next Semester");

        final JDialog[] ref = new JDialog[1];
        ref[0] = createFormDialog("Request an Item", "Post what you need on the student wishlist", 370, 340,
                new String[]{"Category", "Needed Item (Include size / edition)", "Urgency Level"},
                new JComponent[]{catBox, itemField, urgencyBox}, "Post Request", () -> {
            String needed = itemField.getText().trim();
            if (needed.isEmpty()) { JOptionPane.showMessageDialog(ref[0], "Please describe the item you need."); return; }
            int nextId = requests.isEmpty() ? 201 : requests.get(requests.size() - 1).requestId + 1;
            requests.add(new ItemRequest(nextId, (String) catBox.getSelectedItem(), needed, (String) urgencyBox.getSelectedItem(),
                    currentUser.fullName + " (" + currentUser.courseYear + ")", currentUser.contactInfo, "OPEN", "-"));
            DataStorage.saveRequests(requests);
            ref[0].dispose();
            switchSection(1);
            JOptionPane.showMessageDialog(this, "Wishlist request (#" + nextId + ") posted!");
        });
        ref[0].setVisible(true);
    }

    private void handleClaimOrFulfill() {
        if (activeSection == 0) {
            int row = itemsTable.getSelectedRow();
            if (row == -1) { JOptionPane.showMessageDialog(this, "Please select a donated item from the table first."); return; }
            int id = (int) itemsTableModel.getValueAt(row, 0);
            for (ExchangeItem item : items) {
                if (item.itemId == id) {
                    if ("CLAIMED".equals(item.status)) { JOptionPane.showMessageDialog(this, "This item has already been claimed."); return; }
                    if (JOptionPane.showConfirmDialog(this, "Claim \"" + item.itemName + "\"?\nMeetup Spot: " + item.pickupSpot + "\nDonor Contact: " + item.donorContact, "Confirm Claim", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                        item.claim(currentUser); DataStorage.saveItems(items); refreshAllTables();
                        JOptionPane.showMessageDialog(this, "Item reserved! Please contact " + item.donorName + " (" + item.donorContact + ") for pickup.");
                    }
                    return;
                }
            }
        } else {
            int row = requestsTable.getSelectedRow();
            if (row == -1) { JOptionPane.showMessageDialog(this, "Please select a wishlist request from the table first."); return; }
            int id = (int) requestsTableModel.getValueAt(row, 0);
            for (ItemRequest req : requests) {
                if (req.requestId == id) {
                    if ("FULFILLED".equals(req.status)) { JOptionPane.showMessageDialog(this, "This request has already been fulfilled."); return; }
                    if (JOptionPane.showConfirmDialog(this, "Grant request for \"" + req.neededItem + "\"?\nStudent: " + req.requesterName + "\nContact: " + req.requesterContact, "Fulfill Student Request", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                        req.fulfill(currentUser); DataStorage.saveRequests(requests); refreshAllTables();
                        JOptionPane.showMessageDialog(this, "Awesome! Please message " + req.requesterName + " (" + req.requesterContact + ") to hand over the item.");
                    }
                    return;
                }
            }
        }
    }

    private void handleDeleteSelected() {
        if (activeSection == 0) {
            int row = itemsTable.getSelectedRow();
            if (row == -1) { JOptionPane.showMessageDialog(this, "Please select a donated item to delete."); return; }
            int id = (int) itemsTableModel.getValueAt(row, 0);
            if (JOptionPane.showConfirmDialog(this, "Are you sure you want to delete \"" + itemsTableModel.getValueAt(row, 2) + "\" (#" + id + ")?", "Delete Listing", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) == JOptionPane.YES_OPTION) {
                items.removeIf(i -> i.itemId == id); DataStorage.saveItems(items); refreshAllTables();
            }
        } else {
            int row = requestsTable.getSelectedRow();
            if (row == -1) { JOptionPane.showMessageDialog(this, "Please select a wishlist request to delete."); return; }
            int id = (int) requestsTableModel.getValueAt(row, 0);
            if (JOptionPane.showConfirmDialog(this, "Are you sure you want to delete wishlist request \"" + requestsTableModel.getValueAt(row, 2) + "\" (#" + id + ")?", "Delete Request", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) == JOptionPane.YES_OPTION) {
                requests.removeIf(r -> r.requestId == id); DataStorage.saveRequests(requests); refreshAllTables();
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new CampusExchangeSystem().setVisible(true));
    }
}