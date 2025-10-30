import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public class MathQuizGame extends JFrame {
    private JLabel timerLabel, scoreLabel, accuracyLabel, questionLabel;
    private JTextField answerField;
    private JButton submitButton, playAgainButton;
    private JPanel topPanel, centerPanel, bottomPanel;

    private Random rand = new Random();
    private int timeLeft = 60; // seconds
    private Timer timer;
    private boolean running = false;

    private int currentAnswer = 0;
    private int totalAsked = 0;
    private int correctCount = 0;

    public MathQuizGame() {
        setTitle("Math Quiz Game");
        setSize(700, 300);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        // Blue theme
        Color bg = new Color(230, 245, 255);
        Color panel = new Color(200, 225, 255);
        getContentPane().setBackground(bg);

        // Top panel: timer + score + accuracy
        topPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        topPanel.setBackground(panel);
        timerLabel = new JLabel("Time: 60s");
        timerLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        scoreLabel = new JLabel("Score: 0");
        scoreLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        accuracyLabel = new JLabel("Accuracy: 0.0%");
        accuracyLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        topPanel.add(timerLabel);
        topPanel.add(scoreLabel);
        topPanel.add(accuracyLabel);
        add(topPanel, BorderLayout.NORTH);

        // Center panel: question display and answer field
        centerPanel = new JPanel(new GridBagLayout());
        centerPanel.setBackground(bg);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.gridx = 0; gbc.gridy = 0;
        questionLabel = new JLabel("Press Start to begin the quiz");
        questionLabel.setFont(new Font("Segoe UI", Font.PLAIN, 20));
        centerPanel.add(questionLabel, gbc);

        gbc.gridy = 1;
        answerField = new JTextField(15);
        answerField.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        answerField.setEnabled(false);
        centerPanel.add(answerField, gbc);

        gbc.gridy = 2;
        submitButton = new JButton("Submit Answer");
        submitButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        submitButton.setEnabled(false);
        centerPanel.add(submitButton, gbc);

        add(centerPanel, BorderLayout.CENTER);

        // Bottom panel: Play/Restart
        bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        bottomPanel.setBackground(panel);
        playAgainButton = new JButton("Start Quiz");
        playAgainButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        bottomPanel.add(playAgainButton);
        add(bottomPanel, BorderLayout.SOUTH);

        // Actions
        playAgainButton.addActionListener(e -> startQuiz());
        submitButton.addActionListener(e -> submitAnswer());
        answerField.addActionListener(e -> submitAnswer()); // enter key

        setVisible(true);
    }

    private void startQuiz() {
        resetStats();
        answerField.setEnabled(true);
        submitButton.setEnabled(true);
        playAgainButton.setEnabled(false);
        answerField.requestFocus();
        running = true;
        timeLeft = 60;
        timerLabel.setText("Time: 60s");
        generateQuestion();

        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            public void run() {
                SwingUtilities.invokeLater(() -> {
                    timeLeft--;
                    timerLabel.setText("Time: " + timeLeft + "s");
                    if (timeLeft <= 0) {
                        timer.cancel();
                        running = false;
                        finishQuiz();
                    }
                });
            }
        }, 1000, 1000);
    }

    private void resetStats() {
        totalAsked = 0;
        correctCount = 0;
        currentAnswer = 0;
        scoreLabel.setText("Score: 0");
        accuracyLabel.setText("Accuracy: 0.0%");
        questionLabel.setText("Get ready...");
        avgResetUI();
    }

    private void avgResetUI() {
        // no-op placeholder if extra UI changes required later
    }

    private void finishQuiz() {
        answerField.setEnabled(false);
        submitButton.setEnabled(false);
        playAgainButton.setText("Play Again");
        playAgainButton.setEnabled(true);

        double accuracy = totalAsked == 0 ? 0.0 : (correctCount * 100.0 / totalAsked);
        JOptionPane.showMessageDialog(this,
                String.format("Time's up!\nScore: %d\nCorrect: %d / %d\nAccuracy: %.2f%%",
                        correctCount, correctCount, totalAsked, accuracy),
                "Quiz Finished", JOptionPane.INFORMATION_MESSAGE);
    }

    private void submitAnswer() {
        if (!running) return;
        String text = answerField.getText().trim();
        if (text.isEmpty()) return;
        int userAns;
        try {
            // support integer answers only
            userAns = Integer.parseInt(text);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Please enter a valid integer answer.", "Invalid Input", JOptionPane.WARNING_MESSAGE);
            return;
        }

        totalAsked++;
        if (userAns == currentAnswer) {
            correctCount++;
        }

        updateStats();
        answerField.setText("");
        generateQuestion();
    }

    private void updateStats() {
        scoreLabel.setText("Score: " + correctCount);
        double accuracy = totalAsked == 0 ? 0.0 : (correctCount * 100.0 / totalAsked);
        accuracyLabel.setText(String.format("Accuracy: %.2f%%", accuracy));
    }

    private void generateQuestion() {
        // generate one of 4 ops: + - * /
        int op = rand.nextInt(4);
        int a, b;
        String q;
        switch (op) {
            case 0: // addition
                a = rand.nextInt(50) + 1; // 1..50
                b = rand.nextInt(50) + 1;
                currentAnswer = a + b;
                q = String.format("%d + %d = ?", a, b);
                break;
            case 1: // subtraction (ensure non-negative)
                a = rand.nextInt(100) + 1;
                b = rand.nextInt(a) + 1; // b <= a
                currentAnswer = a - b;
                q = String.format("%d - %d = ?", a, b);
                break;
            case 2: // multiplication
                a = rand.nextInt(12) + 1; // smaller numbers
                b = rand.nextInt(12) + 1;
                currentAnswer = a * b;
                q = String.format("%d × %d = ?", a, b);
                break;
            default: // division - ensure divisible
                b = rand.nextInt(12) + 1; // divisor 1..12
                int quotient = rand.nextInt(12) + 1;
                a = b * quotient;
                currentAnswer = quotient;
                q = String.format("%d ÷ %d = ?", a, b);
                break;
        }
        questionLabel.setText(q);
    }

    public static void main(String[] args) {
        // Ensure GUI uses system look for nicer appearance
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}
        SwingUtilities.invokeLater(MathQuizGame::new);
    }
}
