import java.awt.EventQueue;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.JButton;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;


public class VectorQuantizer {

	private JFrame frame;
	private JTextField textField;
	private JButton btnCompress;
	private JButton btnDecompress;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					VectorQuantizer window = new VectorQuantizer();
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
	public VectorQuantizer() {
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
		
		JLabel lblEnterFilesPath = new JLabel("Enter file's Path:");
		lblEnterFilesPath.setBounds(6, 18, 106, 16);
		frame.getContentPane().add(lblEnterFilesPath);
		
		textField = new JTextField();
		textField.setBounds(108, 12, 342, 28);
		frame.getContentPane().add(textField);
		textField.setColumns(10);
		
		btnCompress = new JButton("Compress");
		btnCompress.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String fileName=textField.getText();
				
				String tmp=JOptionPane.showInputDialog("Enter number of bits for quantization");
				int bits=Integer.parseInt(tmp);
				int levels=1<<bits;
				
				tmp=JOptionPane.showInputDialog("Enter height of the vector");
				N=Integer.parseInt(tmp);
				
				tmp=JOptionPane.showInputDialog("Enter width of the vector");
				M=Integer.parseInt(tmp);
				

				int pixels[][]=readImage(fileName);
				
				
				ArrayList<int[][]> list=new ArrayList<int[][]>();
				
				boolean[][] vis=new boolean[height][width];
				
				
				for(int i=0;i<height;++i){
					for(int j=0;j<width;++j){
						if(vis[i][j])continue;
						if(i+N>height || j+M>width)continue;
						
						int[][] arr=new int[N][M];
						int a=0;
						for(int x=i;x<i+N;++x){
							int b=0;
							for(int y=j;y<j+M;++y){
								vis[x][y]=true;
								arr[a][b++]=pixels[x][y];
							}
							++a;
						}
						list.add(arr);
					}
				}
				
				double[][] avg=new double[N][M];
				for(int i=0;i<N;++i){
					for(int j=0;j<M;++j){
						for(int k=0;k<list.size();++k){
							avg[i][j]+=list.get(k)[i][j];
						}
						avg[i][j]/=(double)list.size();
					}
				}
				ArrayList<double[][]> vecs=new ArrayList<double[][]>();
				vecs.add(avg);
				boolean last=false;
				while(true){
					ArrayList<double[][]> temp=new ArrayList<double[][]>();
					for(int i=0;i<vecs.size();++i){
						if(last){
							temp.add(vecs.get(i));
							continue;
						}
						temp.add(incDec(vecs.get(i),false));
						temp.add(incDec(vecs.get(i),true));
					}

					
					vecs=new ArrayList<double[][]>();
					for(int i=0;i<temp.size();++i)vecs.add(new double[N][M]);
					
					int[] cnt=new int[temp.size()];
					
					for(int i=0;i<list.size();++i){
						int ind=-1;
						double mn=-1;
						for(int j=0;j<temp.size();++j){
							double res=calc(list.get(i),temp.get(j));
							if(ind==-1){mn=res;ind=j;}
							else if(mn>res){mn=res;ind=j;}
						}
						
						
						for(int x=0;x<N;++x){
							for(int y=0;y<M;++y){
								vecs.get(ind)[x][y]+=list.get(i)[x][y];
							}
						}
						++cnt[ind];
					}
					
					for(int i=0;i<vecs.size();++i){
						for(int x=0;x<N;++x){
							for(int y=0;y<M;++y){
								vecs.get(i)[x][y]/=(double)cnt[i];
							}
						}
					}
					if(last)break;
					if(vecs.size()==levels)last=true;
				}
				
				
				String dataFile=fileName.substring(0, fileName.lastIndexOf('.'))+"_data.txt";
				try {
					DataOutputStream out=new DataOutputStream(new FileOutputStream(dataFile));
					out.writeInt(height);
					out.writeInt(width);
					out.writeInt(bits);
					out.writeInt(list.size());
					out.writeInt(N);
					out.writeInt(M);
					for(int i=0;i<vecs.size();++i){
						for(int x=0;x<N;++x){
							for(int y=0;y<M;++y){
								out.writeDouble(vecs.get(i)[x][y]);
							}
						}
					}
					for(int i=0;i<list.size();++i){
						int ind=-1;
						double mn=-1;
						for(int j=0;j<vecs.size();++j){
							double res=calc(list.get(i),vecs.get(j));
							if(ind==-1){mn=res;ind=j;}
							else if(res<mn){mn=res;ind=j;}
						}
						out.writeInt(ind);
					}
					out.close();
				} 
				catch (IOException e1) {
					JOptionPane.showMessageDialog(null, "Error writing on data file");
				}
			}
		});
		btnCompress.setBounds(22, 98, 117, 45);
		frame.getContentPane().add(btnCompress);
		
		btnDecompress = new JButton("Decompress");
		btnDecompress.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String fileName=textField.getText();
				try{
					DataInputStream in=new DataInputStream(new FileInputStream(fileName));
					height=in.readInt();
					width=in.readInt();
					int bits=in.readInt();
					int szList=in.readInt();
					N=in.readInt();
					M=in.readInt();
					int levels=1<<bits;
					
					ArrayList<double[][]> vecs=new ArrayList<double[][]>();
					
					for(int i=0;i<levels;++i){
						vecs.add(new double[N][M]);
						for(int x=0;x<N;++x){
							for(int y=0;y<M;++y){
								vecs.get(i)[x][y]=in.readDouble();
							}
						}
					}
					
					ArrayList<double[][]> list=new ArrayList<double[][]>();
					
					for(int i=0;i<szList;++i){
						int ind=in.readInt();
						list.add(vecs.get(ind));
					}
					in.close();
					
					int[][] pixels=new int[height][width];
					boolean[][] vis=new boolean[height][width];
					
					int ind=0;
					for(int i=0;i<height;++i){
						for(int j=0;j<width;++j){
							if(vis[i][j])continue;
							if(i+N>height || j+M>width)continue;
							
							int a=0;
							for(int x=i;x<i+N;++x){
								int b=0;
								for(int y=j;y<j+M;++y){
									vis[x][y]=true;
									pixels[x][y]=(int)list.get(ind)[a][b++];
								}
								++a;
							}
							++ind;
						}
					}
					
					fileName=fileName.substring(0, fileName.lastIndexOf('_'));
					writeImage(pixels,fileName+"_new.jpg");
					
					fileName+=".jpg";
					int pixels2[][]=readImage(fileName);
					double MSE=0.0;
					int numOfpixels=width*height;
					for(int i=0;i<height;++i){
						for(int j=0;j<width;++j){
							MSE+=(pixels[i][j]-pixels2[i][j])*(pixels[i][j]-pixels2[i][j]);
						}
					}
					MSE/=(double)numOfpixels;
					JOptionPane.showMessageDialog(null, "Mean Square Error= "+MSE);
					
				}
				catch(IOException e1){
					JOptionPane.showMessageDialog(null, "Error reading from file");
				}
			}
		});
		btnDecompress.setBounds(308, 98, 117, 45);
		frame.getContentPane().add(btnDecompress);
	}
	
	
	public static double[][] incDec(double[][] arr,boolean inc){
		double[][] ret=new double[N][M];
		for(int i=0;i<N;++i){
			for(int j=0;j<M;++j){
				if(inc)ret[i][j]=arr[i][j]+1;
				else ret[i][j]=arr[i][j]-1;
			}
		}
		return ret;
	}
	
	public static double calc(int[][] arr1,double[][] arr2){
		double ret=0.0;
		for(int i=0;i<N;++i){
			for(int j=0;j<M;++j){
				double a=arr1[i][j];
				ret+=(a-arr2[i][j])*(a-arr2[i][j]);
			}
		}
		return ret;
	}
	
	public static int N=0,M=0;
    public static int width=0;
    public static int height=0;
    public static int[][] readImage(String filePath)
    {
        File file=new File(filePath);
        BufferedImage image=null;
        try
        {
            image=ImageIO.read(file);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

          width=image.getWidth();
          height=image.getHeight();
        int[][] pixels=new int[height][width];

        for(int x=0;x<width;x++)
        {
            for(int y=0;y<height;y++)
            {
                int rgb=image.getRGB(x, y);
                int alpha=(rgb >> 24) & 0xff;
                int r = (rgb >> 16) & 0xff;
                int g = (rgb >> 8) & 0xff;
                int b = (rgb >> 0) & 0xff;

                pixels[y][x]=r;
            }
        }

        return pixels;
    }

    public static void writeImage(int[][] pixels,String outputFilePath)
    {
        File fileout=new File(outputFilePath);
        BufferedImage image2=new BufferedImage(width,height,BufferedImage.TYPE_INT_RGB );

        for(int x=0;x<width ;x++)
        {
            for(int y=0;y<height;y++)
            {
                image2.setRGB(x,y,(pixels[y][x]<<16)|(pixels[y][x]<<8)|(pixels[y][x]));
            }
        }
        try
        {
            ImageIO.write(image2, "jpg", fileout);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
}
