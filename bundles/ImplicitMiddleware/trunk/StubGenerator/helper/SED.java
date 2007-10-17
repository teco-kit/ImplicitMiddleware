package helper;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.CharBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SED {

	/**
	 * Substitute prefix, e.g. ".*world.*" by "universe"
	 * 
	 * @throws IOException
	 */
	public static boolean replaceAll(File   inFile,
			                         File   outFile,
			                         String substitute, 
			                         String substituteReplacement) 
	throws IOException 
	{
		Pattern pattern = Pattern.compile(substitute);

		// Open the file and then get a channel from the stream
		FileInputStream fis = new FileInputStream(inFile);
		FileChannel fc = fis.getChannel();

		// Get the file's size and then map it into memory
		int sz = (int)fc.size();
		MappedByteBuffer bb = fc.map(FileChannel.MapMode.READ_ONLY, 0, sz);

		// Decode the file into a char buffer
		// Charset and decoder for ISO-8859-15
		Charset charset        = Charset.forName("ISO-8859-15");
		CharsetDecoder decoder = charset.newDecoder();
		CharBuffer cb          = decoder.decode(bb);

		Matcher matcher  = pattern.matcher(cb);
		if (!matcher.find())
			return false;
			
		String outString = matcher.replaceAll(substituteReplacement);

		FileOutputStream fos = new FileOutputStream(outFile);
		PrintStream ps       = new PrintStream(fos);
		ps.print(outString);
		ps.close();
		fos.close();
		
		return true;
	}
}

