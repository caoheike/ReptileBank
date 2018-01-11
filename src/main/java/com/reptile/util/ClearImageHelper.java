package com.reptile.util;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import javax.imageio.ImageIO;

public class ClearImageHelper {
	public static void main(String[] args) throws Exception {
		// 处理单个图片
		 cleanImage(new File("E:/img/jtImg/109.png"),"E:/img/handler1/");
			
		//识别整个文件底下的文件
//		File testDataDir = new File("E:/img/jtImg");
//		for (File file : testDataDir.listFiles()) {
//			cleanImage(file, "E:/img/handler1/");
//
//		}

	}

	/**
     * 二值化图片 方便图片更容易辨认
     * @param filePath
     * @return 二值化后图片的路径
     * @throws IOException
     */
    public static String binaryImage(File sfile, String destDir) throws IOException {
        BufferedInputStream inputStream=new BufferedInputStream(new FileInputStream(sfile));
        BufferedImage read = ImageIO.read(inputStream);
        int height = read.getHeight();
        int width = read.getWidth();
        BufferedImage image=new BufferedImage(width,height,BufferedImage.TYPE_BYTE_BINARY);
        for(int i=0;i<width;i++){
            for(int j=0;j<height;j++){
                int rgb = read.getRGB(i, j);
                String argb = Integer.toHexString(rgb);

                int r = Integer.parseInt(argb.substring(2, 4),16);
                int g = Integer.parseInt(argb.substring(4, 6),16);
                int b = Integer.parseInt(argb.substring(6, 8),16);
                int result=(int)((r+g+b)/3);
                if(result>=170){
                    image.setRGB(i,j, Color.WHITE.getRGB());
                }else{
                    image.setRGB(i,j, Color.black.getRGB());
                }
            }
        }
        ImageIO.write(image,"png",new File(destDir, sfile.getName()));
        return destDir+sfile.getName();
    }
	
	/**
	 * * * @param sfile * 需要去噪的图像 * @param destDir * 去噪后的图像保存地址 * @throws
	 * IOException
	 */
	public static void cleanImage(File sfile, String destDir)
			throws IOException {
		File destF = new File(destDir);
		if (!destF.exists()) {
			destF.mkdirs();
		}
		BufferedImage bufferedImage = ImageIO.read(sfile);
		ClearImageHelper.ignore(bufferedImage);
		int h = bufferedImage.getHeight();
		int w = bufferedImage.getWidth(); // 灰度化
		int[][] gray = new int[w][h];
		for (int x = 0; x < w; x++) {
			for (int y = 0; y < h; y++) {
				int argb = bufferedImage.getRGB(x, y);
				// 图像加亮（调整亮度识别率非常高）
				int r = (int) (((argb >> 16) & 0xFF) * 1.1 + 30);
				int g = (int) (((argb >> 8) & 0xFF) * 1.1 + 30);
				int b = (int) (((argb >> 0) & 0xFF) * 1.1 + 30);
				if (r >= 255) {
					r = 255;
				}
				if (g >= 255) {
					g = 255;
				}
				if (b >= 255) {
					b = 255;
				}
				gray[x][y] = (int) Math
						.pow((Math.pow(r, 2.2) * 0.2973 + Math.pow(g, 2.2)
								* 0.6274 + Math.pow(b, 2.2) * 0.0753), 1 / 2.2);
			}
		}
		// 二值化
		int threshold = ostu(gray, w, h);
		BufferedImage binaryBufferedImage = new BufferedImage(w, h,
				BufferedImage.TYPE_BYTE_BINARY);
		for (int x = 0; x < w; x++) {
			System.out.println();
			for (int y = 0; y < h; y++) {
				if (gray[x][y] > threshold) {
					gray[x][y] |= 0x00FFFF;
				} else {
					gray[x][y] &= 0xFF0000;
				}
				binaryBufferedImage.setRGB(x, y, gray[x][y]);
			}
		} // 矩阵打印
		for (int y = 0; y < h; y++) {
			for (int x = 0; x < w; x++) {
				if (isBlack(binaryBufferedImage.getRGB(x, y))) {
					System.out.print("*");
				} else {
					System.out.print(" ");
				}
			}
			System.out.println();
		}

		ImageIO.write(ClearImageHelper.zoomInImage(binaryBufferedImage, 10),
				"png", new File(destDir, sfile.getName()));
	}

	public static int count(int colorInt) {
		Color color = new Color(colorInt);
		return color.getRed() + color.getGreen() + color.getBlue();
	}

	public static boolean isBlack(int colorInt) {
		Color color = new Color(colorInt);
		if (color.getRed() + color.getGreen() + color.getBlue() <= 300) {
			return true;
		}
		return false;
	}

	public static boolean isWhite(int colorInt) {
		Color color = new Color(colorInt);
		if (color.getRed() + color.getGreen() + color.getBlue() > 300) {
			return true;
		}
		return false;
	}

	public static int isBlackOrWhite(int colorInt) {
		if (getColorBright(colorInt) < 30 || getColorBright(colorInt) > 730) {
			return 1;
		}
		return 0;
	}

	public static int getColorBright(int colorInt) {
		Color color = new Color(colorInt);
		return color.getRed() + color.getGreen() + color.getBlue();
	}

	public static int ostu(int[][] gray, int w, int h) {
		int[] histData = new int[w * h];
		// Calculatehistogram
		for (int x = 0; x < w; x++) {
			for (int y = 0; y < h; y++) {
				int red = 0xFF & gray[x][y];
				histData[red]++;
			}
		}
		// Total number of pixels
		int total = w * h;
		float sum = 0;
		for (int t = 0; t < 256; t++)
			sum += t * histData[t];
		float sumB = 0;
		int wB = 0;
		int wF = 0;
		float varMax = 0;
		int threshold = 0;
		for (int t = 0; t < 256; t++) {
			wB += histData[t]; // WeightBackground
			if (wB == 0)
				continue;
			wF = total - wB; // WeightForeground
			if (wF == 0)
				break;
			sumB += (float) (t * histData[t]);
			float mB = sumB / wB; // Mean Background
			float mF = (sum - sumB) / wF; // MeanForeground
			// Calculate BetweenClass Variance
			float varBetween = (float) wB * (float) wF * (mB - mF) * (mB - mF);
			// Check if new maximum found
			if (varBetween > varMax) {
				varMax = varBetween;
				threshold = t;
			}
		}
		return threshold;
	}

	public static void ignore(BufferedImage img) throws IOException {
		for (int y = 0; y < img.getHeight(); y++) {
			for (int x = 0; x < img.getWidth(); x++) {
				if ((img.getRGB(x, y) <= -11035174 && img.getRGB(x, y) >= -11169090)
						|| img.getRGB(x, y) == new Color(28, 108, 171).getRGB()
						|| img.getRGB(x, y) == new Color(77, 155, 217).getRGB()
//				 || img.getRGB(x, y) == new Color(81, 157, 218).getRGB()
				) {
					img.setRGB(x, y, -1);
				}
			}

		}

	}

	/**
	 * 
	 * 对图片进行放大
	 * 
	 * @param originalImage
	 *            原始图片
	 * 
	 * @param times
	 *            放大倍数
	 * 
	 * @return
	 */

	public static BufferedImage zoomInImage(BufferedImage originalImage,
			Integer times) {

		int width = originalImage.getWidth() * times;

		int height = originalImage.getHeight() * times;

		BufferedImage newImage = new BufferedImage(width, height,
				originalImage.getType());

		Graphics g = newImage.getGraphics();

		g.drawImage(originalImage, 0, 0, width, height, null);

		g.dispose();

		return newImage;

	}
}
