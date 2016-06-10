import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.JButton;

import java.awt.Color;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayDeque;
import java.util.Queue;

public class AdaptiveHuffman {

	private JFrame frame;
	private JTextField textField;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					AdaptiveHuffman window = new AdaptiveHuffman();
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
	public AdaptiveHuffman() {
		initialize();
	}

	static String[] codes;
	static Node root;
	static Node maxNode, curNode, tmpRoot;
	static int maxNumber;

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frame = new JFrame();
		frame.setBounds(100, 100, 450, 300);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(null);

		JLabel lblEnterTextFiles = new JLabel("Enter text file's full path:");
		lblEnterTextFiles.setForeground(Color.BLUE);
		lblEnterTextFiles.setBounds(6, 23, 161, 16);
		frame.getContentPane().add(lblEnterTextFiles);

		textField = new JTextField();
		textField.setBounds(167, 17, 277, 28);
		frame.getContentPane().add(textField);
		textField.setColumns(10);

		JButton btnCompress = new JButton("Compress");
		btnCompress.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String fileName = textField.getText();
				try {
					FileInputStream file = new FileInputStream(fileName);
					boolean[] appeared = new boolean[257];
					
					String text = "";
					int r;
					Node cur = new Node(0, null, null, null, null);
					while ((r = file.read()) != -1) {
						char c = (char) r;
						if (!appeared[c]) {
							goToNYT(cur);
							cur = curNode;
							cur.right = new Node(1, c + "", null, null, cur);
							cur.left = new Node(0, null, null, null, cur);
							cur.weight = 1;
							
							while (cur.parent != null) {
								cur = cur.parent;
								root = cur;
								while (root.parent != null)
									root = root.parent;
								
								Swap(cur);
								cur = curNode;
							}
						} 
						else {
							traverse(cur, c + "");
							cur = curNode;
							
							while (true) {
								root = cur;
								while (root.parent != null)
									root = root.parent;
								
								Swap(cur);
								cur = curNode;
								if (cur.parent == null)
									break;
								
								cur = cur.parent;
							}
						}
						appeared[c] = true;
						text += c;
					}
					file.close();

					
					codes = new String[257];
					assignCodes(cur, "");
					
					System.out.println("Table:");
					int tableSize = 0;
					for (int i = 0; i < 257; ++i) {
						if (codes[i] != null) {
							System.out.println((char) i + " " + codes[i]);
							++tableSize;
						}
					}
					
					
					String compressedCode = "";
					for (int i = 0; i < text.length(); ++i)
						compressedCode += codes[text.charAt(i)];
					
					
					System.out.println("Compressed Code: " + compressedCode);
					System.out.println();
					System.out.println("Final Tree represented by 'symbol weight':");
					print(cur);

					int sz = compressedCode.length() / 7;
					if (compressedCode.length() % 7 != 0)
						++sz;
					
					byte[] bytes = new byte[(tableSize * 3) + 1 + sz + 1];

					bytes[0] = (byte) tableSize;
					int ind = 1;
					for (int i = 0; i < 257; ++i) {
						if (codes[i] == null)
							continue;
						
						bytes[ind++] = (byte) i;
						bytes[ind++] = (byte) codes[i].length();
						bytes[ind++] = (byte) binary(codes[i]);
					}
					
					bytes[ind++] = (byte) compressedCode.length();
					int cnt = 0;
					String tmp = "";
					for (int i = 0; i < compressedCode.length(); ++i) {
						tmp += compressedCode.charAt(i);
						++cnt;
						if (cnt == 7) {
							bytes[ind++] = (byte) binary(tmp);
							tmp = "";
							cnt = 0;
						}
					}
					if (!tmp.isEmpty())
						bytes[ind] = (byte) binary(tmp);

					
					fileName = fileName.substring(0, fileName.lastIndexOf('.'));
					fileName += "_Compressed.txt";
					
					FileOutputStream out = new FileOutputStream(fileName);
					out.write(bytes);
					out.close();
					
					JOptionPane.showMessageDialog(null,"Your Compressed file is at " + fileName);
					JOptionPane.showMessageDialog(null,"Check the console for some useful information");
				} 
				catch (IOException e1) {
					JOptionPane.showMessageDialog(null, "Couldn't Load File");
				}
			}
		});
		btnCompress.setBounds(6, 107, 176, 43);
		frame.getContentPane().add(btnCompress);

		JButton btnDecompress = new JButton("Decompress");
		btnDecompress.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String fileName = textField.getText();
				try {
					byte[] b = Files.readAllBytes(Paths.get(fileName));
					
					int tableSize = b[0], ind = 1;
					String[] table = new String[257];
					for (int i = 0; i < tableSize; ++i) {
						int len = b[ind + 1];
						String z = Integer.toBinaryString(b[ind + 2]);
						while (z.length() < len)
							z = "0" + z;
						
						table[b[ind]] = z;
						ind += 3;
					}
					
					
					int sz = b[ind];
					String compressedCode = "";
					for (int i = ind + 1;; ++i) {
						try {
							String z = Integer.toBinaryString(b[i]);
							sz -= z.length();
							while (z.length() < 7 && sz > 0) {
								z = "0" + z;
								--sz;
							}
							compressedCode += z;
						} 
						catch (Exception e3) {
							break;
						}
					}
					
					
					String res = "";
					for (int i = 0; i < compressedCode.length(); ++i) {
						for (int j = i + 1; j <= compressedCode.length(); ++j) {
							
							String z = compressedCode.substring(i, j);
							int symbol = -1;
							for (int k = 0; k < 257; ++k) {
								if (table[k] == null)
									continue;
								if (table[k].equals(z)) {
									symbol = k;
									break;
								}
							}
							if (symbol != -1) {
								res += (char) symbol + "";
								i = j - 1;
								break;
							}
						}
					}
					
					
					fileName = fileName.substring(0, fileName.lastIndexOf('.'));
					fileName += "_Decompressed.txt";
					
					File file = new File(fileName);
					if (file.exists())
						file.delete();
					
					file.createNewFile();
					PrintWriter out = new PrintWriter(file);
					out.print(res);
					out.close();
					
					JOptionPane.showMessageDialog(null,"Your Decompressed file is at " + fileName);
				} 
				catch (IOException e2) {
					JOptionPane.showMessageDialog(null, "Couldn't Load File");
				}
			}
		});
		btnDecompress.setBounds(268, 107, 176, 43);
		frame.getContentPane().add(btnDecompress);
	}

	public static void print(Node n) {
		System.out.println(n.symbol + " " + n.weight);
		
		if (n.right != null) {
			System.out.println("go right");
			print(n.right);
		}
		if (n.left != null) {
			System.out.println("go left");
			print(n.left);
		}
		System.out.println("return");
	}

	public static void traverse(Node n, String symbol) {
		if (n.symbol != null) {
			if (n.symbol.equals(symbol) || (n.symbol.length() > 1 && symbol.length() > 1)) {
				curNode = n;
				return;
			}
		}
		
		if (n.right != null)
			traverse(n.right, symbol);
		if (n.left != null)
			traverse(n.left, symbol);
	}

	public static void goToNYT(Node n) {
		if (n.left == null && n.right == null && n.weight == 0 && n.symbol == null) {
			curNode = n;
			return;
		}
		
		if (n.right != null)
			goToNYT(n.right);
		if (n.left != null)
			goToNYT(n.left);
	}

	public static void BFS(Node ROOT) {
		Queue<Node> q = new ArrayDeque<Node>();
		q.add(ROOT);
		int number = 256;
		while (!q.isEmpty()) {
			Node n = q.remove();
			if (n.weight == curNode.weight && number > maxNumber) {
				maxNumber = number;
				maxNode = n;
			}

			if (n.right != null)
				q.add(n.right);
			
			if (n.left != null)
				q.add(n.left);
			
			--number;
		}
	}

	public static void Swap(Node cur) {
		maxNumber = -1;
		curNode = cur;
		BFS(root);
		
		if (maxNumber != -1 && curNode != maxNode && maxNode != curNode.parent) {
			tmpRoot = new Node(0, null, null, null, null);
			if (curNode.symbol != null)
				curNode.symbol = "wanted" + curNode.symbol;
			else
				curNode.symbol = "wanted";
			
			
			root = curNode;
			while (root.parent != null)
				root = root.parent;
			
			
			reConstruct(root, new Node(root.weight, null, null, null, null));
			
			
			while (tmpRoot.parent != null)
				tmpRoot = tmpRoot.parent;
			
			traverse(tmpRoot, "wanted");
			
			
			if (curNode.symbol.length() == 7)
				curNode.symbol = curNode.symbol.charAt(6) + "";
			else
				curNode.symbol = null;
		}
		++curNode.weight;
	}

	public static void reConstruct(Node n, Node tmp) {
		if (n.right != null) {
			tmp.right = new Node(n.right.weight, n.right.symbol, null, null,tmp);
			
			if (n.right == curNode) {
				maxNode.parent = tmp;
				tmp.right = maxNode;
			} 
			else if (n.right == maxNode) {
				curNode.parent = tmp;
				tmp.right = curNode;
			} 
			else
				reConstruct(n.right, tmp.right);
		}
		if (n.left != null) {
			tmp.left = new Node(n.left.weight, n.left.symbol, null, null, tmp);
			
			if (n.left == curNode) {
				maxNode.parent = tmp;
				tmp.left = maxNode;
			} 
			else if (n.left == maxNode) {
				curNode.parent = tmp;
				tmp.left = curNode;
			} 
			else
				reConstruct(n.left, tmp.left);
		}
		
		tmpRoot = tmp;
	}

	public static void assignCodes(Node n, String code) {
		if (n.symbol != null)
			codes[n.symbol.charAt(0)] = code;

		if (n.right != null)
			assignCodes(n.right, code + "0");
		if (n.left != null)
			assignCodes(n.left, code + "1");
	}

	public static int binary(String z) {
		int ret = 0, c = 1;
		for (int i = z.length() - 1; i >= 0; --i) {
			if (z.charAt(i) == '1')
				ret += c;
			c *= 2;
		}
		return ret;
	}
}
