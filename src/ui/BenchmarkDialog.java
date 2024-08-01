package ui;
import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.ArrayList;

public class BenchmarkDialog extends JDialog {
    private JTextArea resultArea;
    private JButton okButton;
    private List<String> results;

    public BenchmarkDialog(JFrame parent) {
        super(parent, "Benchmark en progreso", true);
        setLayout(new BorderLayout());

        results = new ArrayList<>();
        resultArea = new JTextArea(20, 50);
        resultArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(resultArea);
        add(scrollPane, BorderLayout.CENTER);

        okButton = new JButton("OK");
        okButton.setEnabled(false);
        okButton.addActionListener(e -> dispose());
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(okButton);
        add(buttonPanel, BorderLayout.SOUTH);

        setSize(600, 400);
        setLocationRelativeTo(parent);
    }

    public void addResult(String result) {
        results.add(result);
        SwingUtilities.invokeLater(() -> {
            resultArea.append(result + "\n");
            resultArea.setCaretPosition(resultArea.getDocument().getLength());
        });
    }

    public void benchmarkCompleted() {
        okButton.setEnabled(true);
    }
}