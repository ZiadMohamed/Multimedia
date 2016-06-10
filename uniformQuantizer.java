import java.awt.EventQueue;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.JButton;

import java.awt.Color;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;


public class UniformQuantizer {

	private JFrame frame;
	private JTextField textField;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					UniformQuantizer window = new UniformQuantizer();
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
	public UniformQuantizer() {
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
		
		JLabel lblEnterFilesPath = new JLabel("Enter File's Path:");
		lblEnterFilesPath.setForeground(Color.BLUE);
		lblEnterFilesPath.setBackground(Color.BLACK);
		lblEnterFilesPath.setBounds(6, 18, 108, 16);
		frame.getContentPane().add(lblEnterFilesPath);
		
		textField = new JTextField();
		textField.setBounds(110, 12, 334, 28);
		frame.getContentPane().add(textField);
		textField.setColumns(10);
		
		JButton btnCompress = new JButton("Compress");
		btnCompress.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String fileName=textField.getText();
				
				String tmp=JOptionPane.showInputDialog("Enter number of bits for quantization");
				int n=Integer.parseInt(tmp);
				
				int range=(1<<n)-1;
				int[] arr=new int[256];
				int a=0,b=range,level=0;
				while(true){
					boolean stop=false;
					for(int i=a;i<=b;++i){
						if(i>=256){
							stop=true;break;
						}
						arr[i]=level;
					}
					if(stop)break;
					a=b+1;b=a+range;++level;
				}
				
				int pixels[][]=readImage(fileName);
				
				String dataFile=fileName.substring(0, fileName.lastIndexOf('.'))+"_data.txt";
				try {
					DataOutputStream out=new DataOutputStream(new FileOutputStream(dataFile));
					out.writeInt(width);
					out.writeInt(height);
					out.writeInt(n);
					for(int i=0;i<width;++i){
						for(int j=0;j<height;++j){
							out.writeInt(arr[pixels[j][i]]);
						}
					}
					out.close();
				} 
				catch (IOException e1) {
					JOptionPane.showMessageDialog(null, "Error writing on data file");
				}
				
			}
		});
		btnCompress.setBounds(18, 76, 140, 60);
		frame.getContentPane().add(btnCompress);
		
		JButton btnDecompress = new JButton("Decompress");
		btnDecompress.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String fileName=textField.getText();
				try {
					DataInputStream in=new DataInputStream(new FileInputStream(fileName));
					width=in.readInt();
					height=in.readInt();
					int n=in.readInt();
					int[][] pixels=new int[height][width];
					
					
					
					int range=(1<<n)-1;
					int[] arr=new int[256];
					int a=0,b=range,level=0,mid=(range+1)/2;
					while(true){
						boolean stop=false;
						for(int i=a;i<=b;++i){
							if(i>=256){
								stop=true;break;
							}
							arr[level]=a+mid;
						}
						if(stop)break;
						a=b+1;b=a+range;++level;
					}
					
					for(int i=0;i<width;++i){
						for(int j=0;j<height;++j){
							int x=in.readInt();
							//System.out.println(x+" "+arr[x]);
							pixels[j][i]=arr[x];
						}
					}
					in.close();
					
					fileName=fileName.substring(0, fileName.lastIndexOf('_'));
					writeImage(pixels,fileName+"_new.jpg");
					
					
					
					
					fileName+=".jpg";
					int pixels2[][]=readImage(fileName);
					double MSE=0.0;
					int numOfpixels=width*height;
					for(int i=0;i<width;++i){
						for(int j=0;j<height;++j){
							MSE+=(pixels[j][i]-pixels2[j][i])*(pixels[j][i]-pixels2[j][i]);
						}
					}
					MSE/=(double)numOfpixels;
					JOptionPane.showMessageDialog(null, "Mean Square Error= "+MSE);
				} 
				catch (IOException e1) {
					JOptionPane.showMessageDialog(null, "Error reading from file");
				}
			}
		});
		btnDecompress.setBounds(287, 76, 144, 60);
		frame.getContentPane().add(btnDecompress);
	}
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
