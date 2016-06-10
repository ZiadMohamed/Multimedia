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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.PriorityQueue;


public class StandardHuffman {

	private JFrame frame;
	private JTextField textField;

	/**
	 * Launch the application.
	 */
	static String[] arr;
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					StandardHuffman window = new StandardHuffman();
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
	
	public static void rec(node x){
		if(x.a==null && x.b==null){
			arr[x.symb.charAt(0)]=x.code;
			return;
		}
		x.a.code=x.code+"0";
		x.b.code=x.code+"1";
		rec(x.a);
		rec(x.b);
	}
	public static int binary(String z){
		int ret=0,c=1;
		for(int i=z.length()-1;i>=0;--i){
			if(z.charAt(i)=='1')ret+=c;
			c*=2;
		}
		return ret;
	}

	/**
	 * Create the application.
	 */
	public StandardHuffman() {
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
		
		JLabel lblEnterFilesName = new JLabel("Enter File's name:");
		lblEnterFilesName.setForeground(Color.BLUE);
		lblEnterFilesName.setBounds(24, 26, 116, 16);
		frame.getContentPane().add(lblEnterFilesName);
		
		textField = new JTextField();
		textField.setBounds(138, 20, 306, 28);
		frame.getContentPane().add(textField);
		textField.setColumns(10);
		
		
		JButton btnCompress = new JButton("Compress");
		btnCompress.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String fileName = textField.getText();
				try{
					String s=new String(Files.readAllBytes(Paths.get(fileName)));
					int freq[] = new int[257];
					for(int i=0;i<s.length();++i)++freq[s.charAt(i)];
					
					Comparator<node> comp=new Comp();
					PriorityQueue<node> q=new PriorityQueue<node>(11,comp);
					int tableSize=0;
					for(int i=0;i<257;++i){
						if(freq[i]==0)continue;
						++tableSize;
						node x=new node(freq[i],(char)i+"",null,null,null);
						q.add(x);
					}
					while(q.size()>1){
						node x=q.poll(),y=q.poll();
						node z=new node(x.prob+y.prob,x.symb+y.symb,x,y,"");
						q.add(z);
					}
					arr=new String[257];
					rec(q.peek());
					
					String codes="";
					for(int i=0;i<s.length();++i)codes+=arr[s.charAt(i)];
					
				
					int sz=codes.length()/7;
					if(codes.length()%7!=0)++sz;
					byte[] bytes=new byte[(tableSize*3)+1+sz+1];
					
					bytes[0]=(byte)tableSize;
					int ind=1;
					for(int i=0;i<257;++i){
						if(freq[i]==0)continue;
						bytes[ind++]=(byte)i;
						bytes[ind++]=(byte)arr[i].length();
						bytes[ind++]=(byte)binary(arr[i]);
					}
					bytes[ind++]=(byte)codes.length();
					int cnt=0;String tmp="";
					for(int i=0;i<codes.length();++i){
						tmp+=codes.charAt(i);
						++cnt;
						if(cnt==7){
							bytes[ind++]=(byte)binary(tmp);
							tmp="";
							cnt=0;
						}
					}
					if(!tmp.isEmpty())bytes[ind]=(byte)binary(tmp);

					
					
					fileName = fileName.substring(0, fileName.lastIndexOf('.'));
					fileName=fileName+"_Compressed.txt";
					FileOutputStream out=new FileOutputStream(fileName);
					out.write(bytes);
					out.close();
				}
				catch(IOException e1){
					JOptionPane.showMessageDialog(null, "Couldn't Load File");
				}
			}
		});
		btnCompress.setForeground(Color.BLACK);
		btnCompress.setBounds(23, 112, 117, 29);
		frame.getContentPane().add(btnCompress);
		
		JButton btnDecompress = new JButton("Decompress");
		btnDecompress.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String fileName = textField.getText();
				try{
					byte[] b=Files.readAllBytes(Paths.get(fileName));
					int tableSize=b[0],ind=1;
					String[] table=new String[257];
					for(int i=0;i<tableSize;++i){
						int len=b[ind+1];
						String z=Integer.toBinaryString(b[ind+2]);
						while(z.length()<len)z="0"+z;
						table[b[ind]]=z;
						ind+=3;
					}
					int sz=b[ind];
					String codes="";
					for(int i=ind+1;;++i){
						try{
							String z=Integer.toBinaryString(b[i]);
							sz-=z.length();
							while(z.length()<7 && sz>0){z="0"+z;--sz;}
							codes+=z;
						}
						catch(Exception e3){
							break;
						}
					}
					String res="";
					for(int i=0;i<codes.length();++i){
						for(int j=i+1;j<=codes.length();++j){
							String z=codes.substring(i, j);
							int symbol=-1;
							for(int k=0;k<257;++k){
								if(table[k]==null)continue;
								if(table[k].equals(z)){symbol=k;break;}
							}
							if(symbol!=-1){
								res+=(char)symbol+"";
								i=j-1;
								break;
							}
						}
					}
					fileName = fileName.substring(0, fileName.lastIndexOf('.'));
					File file = new File(fileName + "_Decompressed.txt");
					if(file.exists())file.delete();
					file.createNewFile();
					PrintWriter out=new PrintWriter(file);
					out.print(res);
					out.close();
				}
				catch(IOException e2){
					JOptionPane.showMessageDialog(null, "Couldn't Load File");
				}
			}
		});
		btnDecompress.setForeground(Color.BLACK);
		btnDecompress.setBounds(271, 112, 117, 29);
		frame.getContentPane().add(btnDecompress);
	}
}
