import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;  // Changed from JTextArea to JTextPane
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;  // Added for text operations

public class SimpleTextEditor {
   public SimpleTextEditor() {
   }

   public static void main(String[] var0) {
      SwingUtilities.invokeLater(() -> {
         JFrame var0 = new JFrame("Simple Text Editor");
         JTextPane var1 = new JTextPane();  // Changed from JTextArea to JTextPane
         JScrollPane var2 = new JScrollPane(var1);
         JMenuBar var3 = new JMenuBar();
         JMenu var4 = new JMenu("File");
         JMenuItem var5 = new JMenuItem("Open");
         JMenuItem var6 = new JMenuItem("Save");
         JMenuItem var7 = new JMenuItem("Exit");
         var4.add(var5);
         var4.add(var6);
         var4.add(var7);
         var3.add(var4);
         var0.setJMenuBar(var3);
         var0.add(var2, "Center");
         var0.setSize(600, 400);
         var0.setDefaultCloseOperation(3);
         var0.setVisible(true);
         var5.addActionListener((var2x) -> {
            JFileChooser var3 = new JFileChooser();
            int var4 = var3.showOpenDialog(var0);
            if (var4 == 0) {
               try {
                  BufferedReader var5 = new BufferedReader(new FileReader(var3.getSelectedFile()));
                  StringBuilder content = new StringBuilder();
                  String line;

                  try {
                     while ((line = var5.readLine()) != null) {
                        content.append(line).append("\n");
                     }
                     var1.setText(content.toString());
                  } catch (Throwable var9) {
                     try {
                        var5.close();
                     } catch (Throwable var8) {
                        var9.addSuppressed(var8);
                     }
                     throw var9;
                  }

                  var5.close();
               } catch (IOException var10) {
                  JOptionPane.showMessageDialog(var0, "Error opening file.");
               }
            }
         });

         var6.addActionListener((var2x) -> {
            JFileChooser var3 = new JFileChooser();
            int var4 = var3.showSaveDialog(var0);
            if (var4 == 0) {
               try {
                  BufferedWriter var5 = new BufferedWriter(new FileWriter(var3.getSelectedFile()));

                  try {
                     var5.write(var1.getText());
                  } catch (Throwable var9) {
                     try {
                        var5.close();
                     } catch (Throwable var8) {
                        var9.addSuppressed(var8);
                     }
                     throw var9;
                  }

                  var5.close();
               } catch (IOException var10) {
                  JOptionPane.showMessageDialog(var0, "Error saving file.");
               }
            }
         });

         var7.addActionListener((var1x) -> {
            var0.dispose();
         });
      });
   }
}
