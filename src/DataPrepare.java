
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Random;
import javax.imageio.ImageIO;


public class DataPrepare {
    private static final String FOLDER_NAME_TRAINING = "characters_training";
    private static final String FOLDER_NAME_TESTING = "characters_testing";
    private static final String EXTENSION_NAME = ".png";
    private static final String RELATION = "CHINESE_CHARACTERS";
    private static final String ATTRIBUTE_NAME = "pixel";
    private static final String TRAINING_FILENAME = "characters_training.arff";
    private static final String TESTING_FILENAME_PREFIX = "characters_testing_";
    private static final ArrayList<File> FILES = new ArrayList<>();
    private static final HashSet<String> CLASS_NAME = new HashSet<>();
    
    private static PrintWriter fo;
    
    public static void main(String[] args) {
	generate_noise(3);
	try {
	    generate_training_file();
	    generate_testing_file(4);
	} catch (FileNotFoundException ex) {
	    
	}
    }
    
    private static HashSet<String> get_class_name() {
	HashSet<String> class_names = new HashSet<>();
	Iterator it = FILES.iterator();
	while (it.hasNext()) {
	    class_names.add(((File)it.next()).getName().split("\\.")[0].toUpperCase());
	}
	return class_names;
    }
    
    private static File[] get_files(String folder_name) {
	return (new File(folder_name)).listFiles(new FileFilter() {
	    @Override
	    public boolean accept(File filename) {
		return filename.getName().toLowerCase().endsWith(EXTENSION_NAME);
	    }
	});
    }
    
    private static void print_meta_info() {
	CLASS_NAME.addAll(get_class_name());
	fo.println("@RELATION " + RELATION);
	DecimalFormat df = new DecimalFormat("00");
	for(int i = 0; i < 64; i++) {
	    for(int j = 0; j < 64; j++) {
		fo.println("@ATTRIBUTE "+ATTRIBUTE_NAME+df.format(i+1)+df.format(j+1)+" {0,1}");
	    }
	}
	fo.print("@ATTRIBUTE class {");
	Iterator it = CLASS_NAME.iterator();
	while (it.hasNext()) {
	    fo.print(it.next() + (it.hasNext()?",":""));
	}
	fo.println("}");
	fo.println("@DATA");
	fo.flush();
	CLASS_NAME.clear();
    }
    
    private static void generate_training_file() throws FileNotFoundException {
	FILES.addAll(Arrays.asList(get_files(FOLDER_NAME_TRAINING)));
	fo = new PrintWriter(TRAINING_FILENAME);
	print_meta_info();
	Iterator it = FILES.iterator();
	while(it.hasNext()) {
	    try {
		File thisFile = (File)it.next();
		BufferedImage img = ImageIO.read(thisFile);
		for(int i = 0; i < 64; i++) {
		    for(int j = 0; j < 64; j++) {
			int pixel = img.getRGB(j, i);
			int[] rgb = new int[3];
			rgb[0] = (pixel & 0xff0000) >> 16;
			rgb[1] = (pixel & 0xff00) >> 8;
			rgb[2] = (pixel & 0xff);
			fo.print(((rgb[0]+rgb[1]+rgb[2])/3<128?"1":"0") + ",");
		    }
		}
		fo.println(thisFile.getName().split("\\.")[0].toUpperCase());
	    } catch (IOException ex) {
	    }
	    fo.flush();
	}
	fo.close();
	FILES.clear();
    }
    
    private static void generate_testing_file(int numOfTypes) throws FileNotFoundException {
	FILES.addAll(Arrays.asList(get_files(FOLDER_NAME_TESTING)));
	DecimalFormat df = new DecimalFormat("00");
	for(int i = 0; i < numOfTypes; i++) {
	    fo = new PrintWriter(TESTING_FILENAME_PREFIX + df.format(i+1) + ".arff");
	    print_meta_info();
	    Iterator it = FILES.iterator();
	    while (it.hasNext()) {
		try {
		    File thisFile = (File)it.next();
		    if (!thisFile.getName().split("\\.")[1].equals(df.format(i+1))) {
			continue;
		    }
		    BufferedImage img = ImageIO.read(thisFile);
		    for(int j = 0; j < 64; j++) {
			for(int k = 0; k < 64; k++) {
			    int pixel = img.getRGB(k, j);
			    int[] rgb = new int[3];
			    rgb[0] = (pixel & 0xff0000) >> 16;
			    rgb[1] = (pixel & 0xff00) >> 8;
			    rgb[2] = (pixel & 0xff);
			    fo.print(((rgb[0]+rgb[1]+rgb[2])/3<128?"1":"0") + ",");
			}
		    }
		    fo.println(thisFile.getName().split("\\.")[0].toUpperCase());
		} catch (IOException ex) {
		}
		fo.flush();
	    }
	    fo.close();
	}
	FILES.clear();
    }
    
    private static void generate_noise(int numOfChanges) {
	FILES.addAll(Arrays.asList(get_files(FOLDER_NAME_TESTING)));
	Iterator it = FILES.iterator();
	DecimalFormat df = new DecimalFormat("00");
	Random rand = new Random();
	while (it.hasNext()) {
	    try {
		File thisFile = (File)it.next();
		BufferedImage originImg = ImageIO.read(thisFile);
		BufferedImage alteredImage = originImg.getSubimage(0, 0, originImg.getWidth(), originImg.getHeight());
		for(int i = 0; i < numOfChanges; i++) {
		    Graphics2D g = alteredImage.createGraphics();
		    g.setColor(Color.black);
		    g.drawLine(rand.nextInt(64), rand.nextInt(64), rand.nextInt(64), rand.nextInt(64));
		    String[] thisFileName = thisFile.getName().split("\\.");
		    File newFile = new File(FOLDER_NAME_TESTING + "/" + thisFileName[0] + "." + df.format(i+2) + EXTENSION_NAME);
		    ImageIO.write(alteredImage, "png", newFile);
		}
	    } catch (IOException ex) {
	    }
	}
	FILES.clear();
    }
}
