import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.DoubleUnaryOperator;

/////////////////////////////////////////////////////////////
public class SortingGUI extends JFrame {

    // ── Algorithm metadata ────────────────────────────────────────────────────
    private static final String[] NAMES = {
        "Selection Sort", "Bubble Sort", "Insertion Sort", "Shell Sort",
        "Merge Sort",     "Quick Sort",  "Quick Sort 3",   "Heap Sort"
    };
    private static final String[] BIG_O = {
        "O(n\u00B2)", "O(n\u00B2)", "O(n\u00B2)", "O(n log\u00B2n)",
        "O(n log n)", "O(n log n)", "O(n log n)", "O(n log n)"
    };
    private static final Color[] COLORS = {
        new Color(231,  76,  60),   // Red        – Selection
        new Color(230, 126,  34),   // Orange     – Bubble
        new Color(229, 192,  23),   // Yellow     – Insertion
        new Color( 39, 174,  96),   // Green      – Shell
        new Color( 52, 152, 219),   // Blue       – Merge
        new Color(155,  89, 182),   // Purple     – Quick
        new Color( 26, 188, 156),   // Teal       – Quick3
        new Color(236, 112,  99)    // Salmon     – Heap
    };

    // Sizes used for Big O multi-run analysis
    private static final int[] ANALYSIS_SIZES = { 500, 1_000, 2_000, 5_000, 10_000, 20_000 };

    // ── UI state ──────────────────────────────────────────────────────────────
    private final JProgressBar[] bars        = new JProgressBar[8];
    private final JLabel[]       timeLabels  = new JLabel[8];
    private final JLabel[]       statusLabels = new JLabel[8];
    private JButton   runBtn, bigOBtn;
    private JSpinner  sizeSpinner;
    private ChartPanel chartPanel;

    // ── Result storage ────────────────────────────────────────────────────────
    private final long[]   singleTimes = new long[8];
    private       long[][] multiTimes;          // [algo][size_index]
    private       int      doneCount;           // accessed only on EDT

    // ── Constructor ───────────────────────────────────────────────────────────
    public SortingGUI() {
        super("Sorting Algorithm Visualizer");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        add(buildControlPanel(), BorderLayout.NORTH);
        add(buildAlgoPanel(),    BorderLayout.CENTER);

        chartPanel = new ChartPanel();
        chartPanel.setPreferredSize(new Dimension(900, 310));
        add(chartPanel, BorderLayout.SOUTH);

        pack();
        setMinimumSize(new Dimension(860, 730));
        setLocationRelativeTo(null);
    }

    // ── Control panel ─────────────────────────────────────────────────────────
    private JPanel buildControlPanel() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 14, 10));
        p.setBackground(new Color(36, 36, 36));
        p.setBorder(new MatteBorder(0, 0, 1, 0, new Color(55, 55, 55)));

        JLabel title = new JLabel("Sorting Algorithm Visualizer");
        title.setFont(new Font("SansSerif", Font.BOLD, 15));
        title.setForeground(Color.WHITE);
        p.add(title);

        p.add(Box.createHorizontalStrut(16));

        JLabel szLbl = new JLabel("Array Size:");
        szLbl.setForeground(new Color(170, 170, 170));
        szLbl.setFont(new Font("SansSerif", Font.PLAIN, 12));
        p.add(szLbl);

        sizeSpinner = new JSpinner(new SpinnerNumberModel(10_000, 100, 100_000, 1_000));
        sizeSpinner.setPreferredSize(new Dimension(95, 27));
        ((JSpinner.DefaultEditor) sizeSpinner.getEditor())
                .getTextField().setHorizontalAlignment(JTextField.CENTER);
        p.add(sizeSpinner);

        JLabel maxLbl = new JLabel("(max 100k)");
        maxLbl.setForeground(new Color(100, 100, 100));
        maxLbl.setFont(new Font("SansSerif", Font.ITALIC, 10));
        p.add(maxLbl);

        runBtn = makeButton("  Run All", new Color(41, 128, 185));
        runBtn.addActionListener(e -> runAll());
        p.add(runBtn);

        bigOBtn = makeButton("  Big O Analysis", new Color(125, 60, 152));
        bigOBtn.addActionListener(e -> runBigO());
        p.add(bigOBtn);

        JLabel hint = new JLabel("  Big O analysis runs sizes: 500 / 1k / 2k / 5k / 10k / 20k");
        hint.setForeground(new Color(90, 90, 90));
        hint.setFont(new Font("SansSerif", Font.ITALIC, 10));
        p.add(hint);

        return p;
    }

    private static JButton makeButton(String text, Color bg) {
        JButton b = new JButton(text);
        b.setBackground(bg);
        b.setForeground(Color.WHITE);
        b.setFont(new Font("SansSerif", Font.BOLD, 12));
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.setOpaque(true);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setPreferredSize(new Dimension(170, 28));
        return b;
    }

    // ── Algorithm rows panel ──────────────────────────────────────────────────
    private JPanel buildAlgoPanel() {
        JPanel outer = new JPanel(new BorderLayout(0, 4));
        outer.setBackground(new Color(26, 26, 26));
        outer.setBorder(new EmptyBorder(8, 10, 8, 10));

        // Header
        JPanel hdr = new JPanel(new BorderLayout(8, 0));
        hdr.setBackground(new Color(40, 40, 40));
        hdr.setBorder(new EmptyBorder(3, 8, 3, 8));
        hdr.add(hdrLabel("Algorithm",    190, SwingConstants.LEFT),  BorderLayout.WEST);
        hdr.add(hdrLabel("Progress",     100, SwingConstants.CENTER), BorderLayout.CENTER);
        hdr.add(hdrLabel("Time / Status", 155, SwingConstants.RIGHT), BorderLayout.EAST);
        outer.add(hdr, BorderLayout.NORTH);

        JPanel rows = new JPanel(new GridLayout(8, 1, 0, 2));
        rows.setBackground(new Color(26, 26, 26));
        for (int i = 0; i < 8; i++) rows.add(buildAlgoRow(i));
        outer.add(rows, BorderLayout.CENTER);

        return outer;
    }

    private static JLabel hdrLabel(String text, int w, int align) {
        JLabel l = new JLabel(text, align);
        l.setForeground(new Color(130, 130, 130));
        l.setFont(new Font("SansSerif", Font.BOLD, 10));
        l.setPreferredSize(new Dimension(w, 18));
        return l;
    }

    private JPanel buildAlgoRow(int i) {
        JPanel row = new JPanel(new BorderLayout(8, 0));
        row.setBackground(i % 2 == 0 ? new Color(33, 33, 33) : new Color(38, 38, 38));
        row.setBorder(new EmptyBorder(5, 8, 5, 8));

        // Left: name + Big O label
        JPanel left = new JPanel(new BorderLayout(6, 0));
        left.setOpaque(false);
        left.setPreferredSize(new Dimension(190, 28));
        JLabel name = new JLabel(NAMES[i]);
        name.setFont(new Font("Monospaced", Font.BOLD, 12));
        name.setForeground(COLORS[i]);
        JLabel bigO = new JLabel(BIG_O[i]);
        bigO.setFont(new Font("Monospaced", Font.PLAIN, 9));
        bigO.setForeground(new Color(105, 105, 105));
        left.add(name, BorderLayout.CENTER);
        left.add(bigO, BorderLayout.EAST);

        // Progress bar
        bars[i] = new JProgressBar(0, 100);
        bars[i].setForeground(COLORS[i]);
        bars[i].setBackground(new Color(55, 55, 55));
        bars[i].setBorderPainted(false);
        bars[i].setValue(0);

        // Right: elapsed time + status
        JPanel right = new JPanel(new BorderLayout(6, 0));
        right.setOpaque(false);
        right.setPreferredSize(new Dimension(155, 28));

        timeLabels[i] = new JLabel("--");
        timeLabels[i].setFont(new Font("Monospaced", Font.BOLD, 13));
        timeLabels[i].setForeground(Color.WHITE);
        timeLabels[i].setHorizontalAlignment(SwingConstants.RIGHT);

        statusLabels[i] = new JLabel("Ready");
        statusLabels[i].setFont(new Font("Monospaced", Font.PLAIN, 10));
        statusLabels[i].setForeground(new Color(95, 95, 95));
        statusLabels[i].setPreferredSize(new Dimension(52, 20));
        statusLabels[i].setHorizontalAlignment(SwingConstants.RIGHT);

        right.add(timeLabels[i],  BorderLayout.CENTER);
        right.add(statusLabels[i], BorderLayout.EAST);

        row.add(left,    BorderLayout.WEST);
        row.add(bars[i], BorderLayout.CENTER);
        row.add(right,   BorderLayout.EAST);
        return row;
    }

    // ── Run All (single size) ─────────────────────────────────────────────────
    private void runAll() {
        try { sizeSpinner.commitEdit(); } catch (java.text.ParseException ignored) {}
        int size = (int) sizeSpinner.getValue();
        setControlsEnabled(false);
        doneCount = 0;
        Arrays.fill(singleTimes, 0L);

        for (int i = 0; i < 8; i++) {
            bars[i].setValue(0);
            timeLabels[i].setText("--");
            setStatus(i, "Queued", new Color(110, 110, 110));
        }

        int[] src = randomArray(size);

        for (int i = 0; i < 8; i++) {
            final int idx = i;
            int[] copy = Arrays.copyOf(src, size);

            new SortWorker(idx, copy, size) {
                @Override protected void done() {
                    try {
                        singleTimes[idx] = get();
                        timeLabels[idx].setText(singleTimes[idx] + " ms");
                        setStatus(idx, "Done", new Color(39, 174, 96));
                        bars[idx].setValue(100);
                        if (++doneCount == 8) {
                            setControlsEnabled(true);
                            chartPanel.showBarChart(NAMES, singleTimes, BIG_O, COLORS);
                        }
                    } catch (Exception ex) { ex.printStackTrace(); }
                }
            }.execute();
        }
    }

    // ── Big O Analysis (multiple sizes) ──────────────────────────────────────
    private void runBigO() {
        try { sizeSpinner.commitEdit(); } catch (java.text.ParseException ignored) {}
        setControlsEnabled(false);
        multiTimes = new long[8][ANALYSIS_SIZES.length];

        for (int i = 0; i < 8; i++) {
            bars[i].setValue(0);
            timeLabels[i].setText("--");
            setStatus(i, "Running", new Color(52, 152, 219));
        }

        new Thread(() -> {
            for (int s = 0; s < ANALYSIS_SIZES.length; s++) {
                int size = ANALYSIS_SIZES[s];
                int[] src = randomArray(size);
                CountDownLatch latch = new CountDownLatch(8);

                for (int i = 0; i < 8; i++) {
                    final int idx = i, si = s;
                    int[] copy = Arrays.copyOf(src, size);
                    new SortWorker(idx, copy, size) {
                        @Override protected void done() {
                            try { multiTimes[idx][si] = get(); }
                            catch (Exception ex) { ex.printStackTrace(); }
                            latch.countDown();
                        }
                    }.execute();
                }

                try { latch.await(); } catch (InterruptedException ignored) {}

                final int fs = s;
                SwingUtilities.invokeLater(() -> {
                    int pct = (int)((fs + 1) * 100.0 / ANALYSIS_SIZES.length);
                    for (int i = 0; i < 8; i++) bars[i].setValue(pct);
                });
            }

            SwingUtilities.invokeLater(() -> {
                for (int i = 0; i < 8; i++) {
                    long total = 0;
                    for (long t : multiTimes[i]) total += t;
                    timeLabels[i].setText(total + " ms");
                    setStatus(i, "Done", new Color(39, 174, 96));
                    bars[i].setValue(100);
                }
                setControlsEnabled(true);
                chartPanel.showLineChart(NAMES, multiTimes, BIG_O, COLORS, ANALYSIS_SIZES);
            });
        }, "big-o-runner").start();
    }

    // ── Helpers ───────────────────────────────────────────────────────────────
    private void setControlsEnabled(boolean en) {
        runBtn.setEnabled(en);
        bigOBtn.setEnabled(en);
        sizeSpinner.setEnabled(en);
    }

    private void setStatus(int i, String text, Color color) {
        statusLabels[i].setText(text);
        statusLabels[i].setForeground(color);
    }

    private static int[] randomArray(int n) {
        Random rnd = new Random();
        int[] a = new int[n];
        for (int i = 0; i < n; i++) a[i] = rnd.nextInt(n * 10);
        return a;
    }

    // ── SortWorker (SwingWorker wrapping one algorithm) ───────────────────────
    private class SortWorker extends SwingWorker<Long, Integer> {
        private final int idx, size;
        private final int[] array;
        private final AtomicInteger ops = new AtomicInteger();

        SortWorker(int idx, int[] array, int size) {
            this.idx   = idx;
            this.array = array;
            this.size  = size;
        }

        @Override
        protected Long doInBackground() {
            SwingUtilities.invokeLater(() -> setStatus(idx, "Running", new Color(52, 152, 219)));

            int expected = estimatedOps(idx, size);
            java.util.Timer ticker = new java.util.Timer(true);
            ticker.scheduleAtFixedRate(new java.util.TimerTask() {
                public void run() {
                    publish(Math.min(99, (int)(ops.get() * 100L / expected)));
                }
            }, 0, 50);

            long start = System.currentTimeMillis();
            doSort(idx, array, ops);
            long elapsed = System.currentTimeMillis() - start;

            ticker.cancel();
            publish(100);
            return elapsed;
        }

        @Override
        protected void process(List<Integer> chunks) {
            bars[idx].setValue(chunks.get(chunks.size() - 1));
        }

        // Expected operation counts to drive progress bar
        private int estimatedOps(int algo, int n) {
            switch (algo) {
                case 0: case 1: case 2:
                    return n * n;
                case 3:
                    return Math.max(1, (int)(n * Math.log(n) * Math.log(n)));
                default:
                    return Math.max(1, (int)(n * Math.log(n) / Math.log(2)));
            }
        }
    }

    // ── Dispatch to SortingAlgorithms ─────────────────────────────────────────
    private static void doSort(int algo, int[] a, AtomicInteger ops) {
        switch (algo) {
            case 0: SortingAlgorithms.selectionSort(a, ops); break;
            case 1: SortingAlgorithms.bubbleSort(a, ops);    break;
            case 2: SortingAlgorithms.insertionSort(a, ops); break;
            case 3: SortingAlgorithms.shellSort(a, ops);     break;
            case 4: SortingAlgorithms.mergeSort(a, ops);     break;
            case 5: SortingAlgorithms.quickSort(a, ops);     break;
            case 6: SortingAlgorithms.quickSort3(a, ops);    break;
            case 7: SortingAlgorithms.heapSort(a, ops);      break;
        }
    }

    // ── Chart Panel ───────────────────────────────────────────────────────────
    private static class ChartPanel extends JPanel {

        private enum Mode { EMPTY, BAR, LINE }
        private Mode mode = Mode.EMPTY;

        // Bar chart data
        private String[] bNames;
        private long[]   bTimes;
        private String[] bBigO;
        private Color[]  bColors;

        // Line chart data
        private String[] lNames;
        private long[][] lTimes;   // [algo][size_index]
        private String[] lBigO;
        private Color[]  lColors;
        private int[]    lSizes;

        ChartPanel() {
            setBackground(new Color(20, 20, 20));
            setBorder(new MatteBorder(1, 0, 0, 0, new Color(50, 50, 50)));
        }

        void showBarChart(String[] names, long[] times, String[] bigO, Color[] colors) {
            mode = Mode.BAR;
            bNames = names; bTimes = times.clone();
            bBigO  = bigO;  bColors = colors;
            repaint();
        }

        void showLineChart(String[] names, long[][] times, String[] bigO,
                           Color[] colors, int[] sizes) {
            mode = Mode.LINE;
            lNames = names; lTimes = times;
            lBigO  = bigO;  lColors = colors; lSizes = sizes;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,      RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            switch (mode) {
                case BAR:  drawBar(g2);  break;
                case LINE: drawLine(g2); break;
                default:   drawEmpty(g2);
            }
            g2.dispose();
        }

        // ── Empty placeholder ─────────────────────────────────────────────────
        private void drawEmpty(Graphics2D g2) {
            g2.setColor(new Color(65, 65, 65));
            g2.setFont(new Font("SansSerif", Font.ITALIC, 13));
            String msg = "Run an analysis to see the performance chart";
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(msg, (getWidth() - fm.stringWidth(msg)) / 2, getHeight() / 2);
        }

        // ── Bar chart (single-run results) ────────────────────────────────────
        private void drawBar(Graphics2D g2) {
            int W = getWidth(), H = getHeight();
            int ml = 62, mr = 18, mt = 32, mb = 68;
            int cw = W - ml - mr, ch = H - mt - mb;

            long maxT = 1;
            for (long t : bTimes) if (t > maxT) maxT = t;

            // Title
            g2.setColor(new Color(185, 185, 185));
            g2.setFont(new Font("SansSerif", Font.BOLD, 12));
            g2.drawString("Execution Time per Algorithm (ms)", ml, mt - 10);

            // Y-axis grid lines + labels
            int ySteps = 5;
            for (int s = 0; s <= ySteps; s++) {
                long val  = maxT * s / ySteps;
                int  yPos = mt + ch - (int)(val * ch / maxT);
                g2.setColor(new Color(42, 42, 42));
                g2.drawLine(ml, yPos, ml + cw, yPos);
                g2.setColor(new Color(105, 105, 105));
                g2.setFont(new Font("Monospaced", Font.PLAIN, 9));
                String lbl = val + " ms";
                FontMetrics fm = g2.getFontMetrics();
                g2.drawLine(ml - 3, yPos, ml, yPos);
                g2.drawString(lbl, ml - fm.stringWidth(lbl) - 5, yPos + 4);
            }

            // Axes
            g2.setColor(new Color(75, 75, 75));
            g2.drawLine(ml, mt, ml, mt + ch);
            g2.drawLine(ml, mt + ch, ml + cw, mt + ch);

            // Bars
            int n      = bNames.length;
            int gap    = cw / n;
            int barW   = (int)(gap * 0.55);
            int barOff = (gap - barW) / 2;

            for (int i = 0; i < n; i++) {
                int barH = (int)(bTimes[i] * ch / maxT);
                int x    = ml + i * gap + barOff;
                int y    = mt + ch - barH;

                // Gradient fill
                GradientPaint gp = new GradientPaint(
                        x, y,        bColors[i].brighter(),
                        x, y + barH, bColors[i].darker());
                g2.setPaint(gp);
                g2.fillRoundRect(x, y, barW, barH, 5, 5);

                // Time label above bar
                if (barH > 2) {
                    g2.setColor(Color.WHITE);
                    g2.setFont(new Font("Monospaced", Font.BOLD, 9));
                    String tStr = bTimes[i] + "ms";
                    FontMetrics fm = g2.getFontMetrics();
                    g2.drawString(tStr, x + (barW - fm.stringWidth(tStr)) / 2, y - 3);
                }

                // Rotated label below axis: "Name  Big-O"
                AffineTransform orig = g2.getTransform();
                g2.translate(x + barW / 2 + 3, mt + ch + 7);
                g2.rotate(Math.PI / 4.5);
                g2.setColor(bColors[i]);
                g2.setFont(new Font("SansSerif", Font.PLAIN, 9));
                g2.drawString(bNames[i] + "  " + bBigO[i], 0, 0);
                g2.setTransform(orig);
            }
        }

        // ── Line chart (Big O analysis) ───────────────────────────────────────
        private void drawLine(Graphics2D g2) {
            int W = getWidth(), H = getHeight();
            int ml = 68, mr = 175, mt = 32, mb = 35;
            int cw = W - ml - mr, ch = H - mt - mb;

            long maxT = 1;
            for (long[] row : lTimes) for (long v : row) if (v > maxT) maxT = v;

            // Title
            g2.setColor(new Color(185, 185, 185));
            g2.setFont(new Font("SansSerif", Font.BOLD, 12));
            g2.drawString("Growth Curves: Execution Time vs Input Size", ml, mt - 10);

            int ns = lSizes.length;

            // X-axis grid + labels
            g2.setFont(new Font("Monospaced", Font.PLAIN, 9));
            for (int s = 0; s < ns; s++) {
                int x = ml + s * cw / (ns - 1);
                g2.setColor(new Color(40, 40, 40));
                g2.drawLine(x, mt, x, mt + ch);
                g2.setColor(new Color(105, 105, 105));
                g2.drawLine(x, mt + ch, x, mt + ch + 3);
                String lbl = fmtSize(lSizes[s]);
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(lbl, x - fm.stringWidth(lbl) / 2, mt + ch + 14);
            }

            // Y-axis grid + labels
            for (int s = 0; s <= 5; s++) {
                long val  = maxT * s / 5;
                int  yPos = mt + ch - (int)(val * ch / maxT);
                g2.setColor(new Color(40, 40, 40));
                g2.drawLine(ml, yPos, ml + cw, yPos);
                g2.setColor(new Color(105, 105, 105));
                g2.drawLine(ml - 3, yPos, ml, yPos);
                String lbl = val + "ms";
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(lbl, ml - fm.stringWidth(lbl) - 6, yPos + 4);
            }

            // Axes
            g2.setColor(new Color(75, 75, 75));
            g2.drawLine(ml, mt, ml, mt + ch);
            g2.drawLine(ml, mt + ch, ml + cw, mt + ch);

            // X-axis label
            g2.setColor(new Color(120, 120, 120));
            g2.setFont(new Font("SansSerif", Font.ITALIC, 10));
            String xLbl = "Input Size (n)";
            FontMetrics fmx = g2.getFontMetrics();
            g2.drawString(xLbl, ml + (cw - fmx.stringWidth(xLbl)) / 2, mt + ch + 28);

            // ── Theoretical Big O overlay curves (dashed) ─────────────────────
            // Normalize using selection sort for O(n²) and merge sort for O(n log n)
            double n0         = lSizes[0];
            double t0_sel     = lTimes[0][0];  // selection sort at smallest size
            double t0_merge   = lTimes[4][0];  // merge sort at smallest size

            if (t0_sel > 0) {
                double scale = t0_sel / (n0 * n0);
                drawTheoCurve(g2, lSizes, ml, mt, cw, ch, maxT,
                        n -> scale * n * n,
                        new Color(255, 130, 130, 70), "O(n\u00B2)");
            }
            if (t0_merge > 0) {
                double scale = t0_merge / (n0 * Math.log(n0));
                drawTheoCurve(g2, lSizes, ml, mt, cw, ch, maxT,
                        n -> scale * n * Math.log(n),
                        new Color(120, 190, 255, 70), "O(n log n)");
            }

            // ── Actual data lines ─────────────────────────────────────────────
            for (int i = 0; i < lNames.length; i++) {
                g2.setColor(lColors[i]);
                g2.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                GeneralPath path = new GeneralPath();
                for (int s = 0; s < ns; s++) {
                    int x = ml + s * cw / (ns - 1);
                    int y = mt + ch - (int)(lTimes[i][s] * ch / maxT);
                    if (s == 0) path.moveTo(x, y); else path.lineTo(x, y);
                    g2.fillOval(x - 3, y - 3, 6, 6);
                }
                g2.draw(path);
                g2.setStroke(new BasicStroke(1f));
            }

            drawLegend(g2, W, mt);
        }

        private void drawTheoCurve(Graphics2D g2, int[] sizes,
                                   int ml, int mt, int cw, int ch, long maxT,
                                   DoubleUnaryOperator fn, Color color, String label) {
            g2.setColor(color);
            g2.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER,
                    10f, new float[]{7f, 5f}, 0f));
            int ns = sizes.length;
            GeneralPath path = new GeneralPath();
            for (int s = 0; s < ns; s++) {
                double tVal = fn.applyAsDouble(sizes[s]);
                int x = ml + s * cw / (ns - 1);
                int y = mt + ch - (int)(tVal * ch / maxT);
                y = Math.max(mt - 10, y);
                if (s == 0) path.moveTo(x, y); else path.lineTo(x, y);
            }
            g2.draw(path);
            g2.setStroke(new BasicStroke(1f));
        }

        private void drawLegend(Graphics2D g2, int W, int mt) {
            int lx = W - 168, ly = mt;
            int lw = 160, lineH = 17;
            int lh = lNames.length * lineH + 36;

            // Background
            g2.setColor(new Color(25, 25, 25, 210));
            g2.fillRoundRect(lx - 6, ly - 6, lw + 12, lh, 10, 10);
            g2.setColor(new Color(58, 58, 58));
            g2.setStroke(new BasicStroke(1f));
            g2.drawRoundRect(lx - 6, ly - 6, lw + 12, lh, 10, 10);

            // Title
            g2.setColor(new Color(155, 155, 155));
            g2.setFont(new Font("SansSerif", Font.BOLD, 10));
            g2.drawString("Legend", lx, ly + 8);

            // Algorithm entries
            for (int i = 0; i < lNames.length; i++) {
                int y = ly + 20 + i * lineH;
                g2.setColor(lColors[i]);
                g2.setStroke(new BasicStroke(2f));
                g2.drawLine(lx, y + 5, lx + 16, y + 5);
                g2.fillOval(lx + 5, y + 2, 6, 6);
                g2.setColor(new Color(195, 195, 195));
                g2.setFont(new Font("SansSerif", Font.PLAIN, 9));
                g2.drawString(lNames[i] + "  " + lBigO[i], lx + 21, y + 9);
            }

            // Dashed theory curve entries
            int ty = ly + 20 + lNames.length * lineH + 4;
            g2.setColor(new Color(90, 90, 90));
            g2.setFont(new Font("SansSerif", Font.ITALIC, 9));
            Stroke dashed = new BasicStroke(1.5f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER,
                    10f, new float[]{5f, 4f}, 0f);
            g2.setStroke(dashed);
            g2.setColor(new Color(255, 130, 130, 140));
            g2.drawLine(lx, ty + 5, lx + 16, ty + 5);
            g2.setColor(new Color(180, 180, 180));
            g2.setFont(new Font("SansSerif", Font.ITALIC, 9));
            g2.drawString("O(n\u00B2) theoretical", lx + 21, ty + 9);

            ty += lineH;
            g2.setStroke(dashed);
            g2.setColor(new Color(120, 190, 255, 140));
            g2.drawLine(lx, ty + 5, lx + 16, ty + 5);
            g2.setColor(new Color(180, 180, 180));
            g2.drawString("O(n log n) theoretical", lx + 21, ty + 9);
            g2.setStroke(new BasicStroke(1f));
        }

        private static String fmtSize(int n) {
            return n >= 1_000 ? (n / 1_000) + "k" : String.valueOf(n);
        }
    }

    // ── Entry point ───────────────────────────────────────────────────────────
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new SortingGUI().setVisible(true));
    }
}
/////////////////////////////////////////////////////////////
