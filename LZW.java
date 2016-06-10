import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

import javax.swing.JButton;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JLabel;
import java.awt.Color;

public class LZW {

	private JFrame frame;
	private JTextField textField;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					LZW window = new LZW();
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public LZW() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frame = new JFrame();
		frame.setBounds(100, 100, 450, 300);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(null);

		textField = new JTextField();
		textField.setBounds(134, 21, 310, 28);
		frame.getContentPane().add(textField);
		textField.setColumns(10);

		JButton btnCompress = new JButton("Compress");
		btnCompress.setForeground(Color.BLUE);
		btnCompress.setBounds(0, 116, 170, 96);
		btnCompress.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String fileName = textField.getText();
				try {
					String s = new String(Files.readAllBytes(Paths
							.get(fileName)), StandardCharsets.UTF_8);
					String output = "";
					Map<String, Integer> m = new HashMap<String, Integer>();
					int cnt = 128;
					for (int i = 0; i < s.length(); ++i) {
						int val = s.charAt(i);
						String z = "";
						z += s.charAt(i);
						for (int j = i + 1; j < s.length(); ++j) {
							z += s.charAt(j);
							if (!m.containsKey(z)) {
								m.put(z, cnt++);
								i = j - 1;
								break;
							} else {
								val = m.get(z);
								i = j;
							}
						}
						output += val + " ";
					}
					output = output.substring(0, output.length() - 1);
					fileName = fileName.substring(0, fileName.lastIndexOf('.'));
					File file = new File(fileName + "_Compressed.txt");
					if (file.exists())
						file.delete();
					file.createNewFile();
					PrintWriter out = new PrintWriter(file);
					out.print(output);
					out.close();
				} catch (IOException e1) {
					JOptionPane.showMessageDialog(null, "Couldn't Load File");
				}
			}
		});
		frame.getContentPane().add(btnCompress);

		JButton btnDecompress = new JButton("Decompress");
		btnDecompress.setForeground(Color.BLUE);
		btnDecompress.setBounds(280, 112, 170, 96);
		btnDecompress.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String fileName = textField.getText();
				try {
					String rd = new String(Files.readAllBytes(Paths
							.get(fileName)), StandardCharsets.UTF_8);
					int[] arr = new int[1000];
					int sz = 0;
					for (String cur : rd.split(" ")) {
						arr[sz++] = Integer.parseInt(cur);
					}
					Map<Integer, String> m = new HashMap<Integer, String>();
					for (int i = 0; i <= 127; ++i) {
						String tmp = "";
						tmp += (char) i;
						m.put(i, tmp);
					}
					String s = "";
					int cnt = 128;
					s += (char) arr[0];
					for (int i = 1; i < sz; ++i) {
						if (m.containsKey(arr[i])) {
							String z = m.get(arr[i]);
							s += z;
							m.put(cnt++, m.get(arr[i - 1]) + z.charAt(0));
						} else {
							String z = m.get(arr[i - 1]);
							z += z.charAt(0);
							s += z;
							m.put(cnt++, z);
						}
					}
					fileName = fileName.substring(0, fileName.lastIndexOf('.'));
					File file = new File(fileName + "_Decompressed.txt");
					if (file.exists())
						file.delete();
					file.createNewFile();
					PrintWriter out = new PrintWriter(file);
					out.print(s);
					out.close();
				} catch (IOException e2) {
					JOptionPane.showMessageDialog(null, "Couldn't Load File");
				}
			}
		});
		frame.getContentPane().add(btnDecompress);

		JLabel lblEnterFilesName = new JLabel("Enter file's name:");
		lblEnterFilesName.setBounds(20, 27, 114, 16);
		frame.getContentPane().add(lblEnterFilesName);
	}
}
