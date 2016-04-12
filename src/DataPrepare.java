
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
import javax.imageio.ImageIO;


public class DataPrepare {
    private static final String FOLDER_NAME = "characters_training";
    private static final String EXTENSION_NAME = ".png";
    private static final String RELATION = "CHINESE_CHARACTERS";
    private static final String ATTRIBUTE_NAME = "pixel";
    private static final String TRAINING_FILENAME = "characters_training.arff";
    private static final String TESTING_FILENAME = "characters_testing.arff";
    private static final ArrayList<File> FILES = new ArrayList<>(Arrays.asList(get_files()));
    private static final HashSet<String> CLASS_NAME = get_class_name();
    
    private static PrintWriter fo;
    
    public static void main(String[] args) {
	try {
	    generate_training_file();
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
    
    private static File[] get_files() {
	return (new File(FOLDER_NAME)).listFiles(new FileFilter() {
	    @Override
	    public boolean accept(File filename) {
		return filename.getName().toLowerCase().endsWith(EXTENSION_NAME);
	    }
	});
    }
    
    private static void print_meta_info() {
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
    }
    
    private static void generate_training_file() throws FileNotFoundException {
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
    }
    
    private static void generate_testing_file(int numOfChanges) {
	
    }
}
