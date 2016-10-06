/*
 * Author: Edan Meyer
 * Note: I used the following video as a reference to better understand hamming codes while making this
 * https://www.youtube.com/watch?v=373FUw-2U2k
 */
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Hamming {
	public static void encode_7_4(String inFileName, String outFileName){
		byte[] data = null;
		try {
			data = Files.readAllBytes(Paths.get(inFileName)); //Read entire in file to data
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		byte[] encoded = new byte[data.length * 2]; //Make new array for the encoded file
		
		for(int i = 0; i < data.length; i++){ //Loop through initial in file data
			byte byte1 = 0;
			byte1 += (data[i] & 0x80) == 0x80 ? 0x10 : 0; //Distribute the 4 bits in the first block of
			byte1 += (data[i] & 0x40) == 0x40 ? 0x04 : 0; //the byte to where they belong in the new
			byte1 += (data[i] & 0x20) == 0x20 ? 0x02 : 0; //hamming-encoded byte
			byte1 += (data[i] & 0x10) == 0x10 ? 0x01 : 0;
			byte1 += parityCheck(1, 6, byte1); //Check what the three parity bit should be
			byte1 += parityCheck(2, 5, byte1); //then add them to the byte1
			byte1 += parityCheck(4, 3, byte1);
			encoded[i*2] = byte1; //Add byte1, the encoded byte, into the hamming-encoded array of bytes
			
			//The following does the same process over again, but for the second block of 
			//the byte from the original in file data
			byte byte2 = 0;
			byte2 += (data[i] & 0x08) == 0x08 ? 0x10 : 0;
			byte2 += (data[i] & 0x04) == 0x04 ? 0x04 : 0;
			byte2 += (data[i] & 0x02) == 0x02 ? 0x02 : 0;
			byte2 += (data[i] & 0x01) == 0x01 ? 0x01 : 0;
			byte2 += parityCheck(1, 6, byte2);
			byte2 += parityCheck(2, 5, byte2);
			byte2 += parityCheck(4, 3, byte2);
			encoded[i*2+1] = byte2;
		}
		
		FileOutputStream fos;
		try {
			fos = new FileOutputStream(outFileName);
			fos.write(encoded); //Write the new hamming-encoded bytes to the output file
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void encode_15_11(String inFileName, String outFileName){
		byte[] data = null;
		try {
			data = Files.readAllBytes(Paths.get(inFileName)); //Read entire in file to data
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		short[] encoded = new short[data.length]; //Make new array for the encoded file
		
		for(int i = 0; i < encoded.length; i++){
			encoded[i] += data[i] & 0x7F; //Set all data bytes to the correct places
			encoded[i] += (data[i] & 0x80) == 0x80 ? 0x0100 : 0;
			encoded[i] += parityCheck(1, 14, encoded[i]); //Add the parity bits
			encoded[i] += parityCheck(2, 13, encoded[i]);
			encoded[i] += parityCheck(4, 11, encoded[i]);
			encoded[i] += parityCheck(8, 7, encoded[i]);
		}
		
		byte[] byteEncoded = new byte[encoded.length*2];
		
		for(int i = 0; i < encoded.length; i++){ //Turning shorts into two bytes each for output
			byteEncoded[i*2+1] = (byte) (encoded[i] & 0xFF);
			encoded[i] = (short) (encoded[i] >> 8);
			byteEncoded[i*2] = (byte) (encoded[i] & 0xFF);
		}
		
		FileOutputStream fos;
		try {
			fos = new FileOutputStream(outFileName);
			fos.write(byteEncoded); //Write the new hamming-encoded bytes to the output file
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void decode_7_4 (String inFileName, String outFileName){
		byte[] data = null;
		try {
			data = Files.readAllBytes(Paths.get(inFileName)); //Read entire in file to data
		} catch (IOException e) {
			e.printStackTrace();
		}
		byte[] decoded = new byte[data.length / 2]; //Create new array for the decoded bytes
		for(int i = 0; i < decoded.length; i++){ //Loop through it

			//////////////////////////////////////////////////////////////////////
			
			int[] parityBits = new int[3]; //Creates an array to hold parity bits
			boolean error = false; //To show if an error has been detected
			for(int j = 0; j < parityBits.length; j++){ //Loop through every parity bit
				if(errorCheck((int)Math.pow(2, j), 7 - (int)Math.pow(2, j), data[i*2])){ //If parity bit indicates the existence of an error
					parityBits[j] = (byte)Math.pow(2, j); //Take the value of the parity bit where the error was found 
					error = true; //Mark the error to fixed
				}
			}
			if(error){ //Fix error if there was one
				int errorIndex = 7; //Size of byte - the initial 0
				for(int p : parityBits){
					errorIndex -= p; //Subtract every parity byte with an error from the byte index
				}
				data[i*2] += (data[i*2] & (byte)Math.pow(2, errorIndex)) == (byte)Math.pow(2, errorIndex) ?
						-(byte)Math.pow(2, errorIndex) : (byte)Math.pow(2, errorIndex); // If the error was a 1, make it a 0 and vice versa
			}
				
			//////////////////////////////////////////////////////////////////////
			
			//The following performs the same as above for the subsequent byte of the encoded data
			int[] parityBits2 = new int[3];
			boolean error2 = false;
			for(int j = 0; j < parityBits.length; j++){
				if(errorCheck((int)Math.pow(2, j), 7 - (int)Math.pow(2, j), data[i*2+1])){
					parityBits2[j] = (byte)Math.pow(2, j);
					error2 = true;
				}
			}
			if(error2){
				int errorIndex = 7;
				for(int p : parityBits2){
					errorIndex -= p;
				}
				data[i*2+1] += (data[i*2+1] & (byte)Math.pow(2, errorIndex)) == (byte)Math.pow(2, errorIndex) ?
						-(byte)Math.pow(2, errorIndex) : (byte)Math.pow(2, errorIndex);
			}

			//////////////////////////////////////////////////////////////////////
			
			//Reorganize non parity bits back into an uncoded byte
			decoded[i] += (data[i*2]   & 0x10) == 0x10 ? 0x80 : 0;
			decoded[i] += (data[i*2]   & 0x04) == 0x04 ? 0x40 : 0;
			decoded[i] += (data[i*2]   & 0x02) == 0x02 ? 0x20 : 0;
			decoded[i] += (data[i*2]   & 0x01) == 0x01 ? 0x10 : 0;
			decoded[i] += (data[i*2+1] & 0x10) == 0x10 ? 0x08 : 0;
			decoded[i] += (data[i*2+1] & 0x04) == 0x04 ? 0x04 : 0;
			decoded[i] += (data[i*2+1] & 0x02) == 0x02 ? 0x02 : 0;
			decoded[i] += (data[i*2+1] & 0x01) == 0x01 ? 0x01 : 0;
		}
		
		FileOutputStream fos;
		try {
			fos = new FileOutputStream(outFileName);
			fos.write(decoded); //Write the new decoded bytes to the output file
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	public static void decode_15_11 (String inFileName, String outFileName){
		byte[] data = null;
		try {
			data = Files.readAllBytes(Paths.get(inFileName)); //Read entire in file to data
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		short[] encoded = new short[data.length/2]; //Array to work with 16-bit codes
		byte[] decoded = new byte[encoded.length]; //Create new array for the decoded bytes
		
		for(int i = 0; i < encoded.length; i++){
			encoded[i] = (short) (data[i*2]<<8 | data[i*2+1]&0xFF); //Turn every two bytes into one short
			
			short errorIndex = 15;
			for(int j = 0; j < 4; j++){
				if(errorCheck((short) Math.pow(2, j), 15 - (short) Math.pow(2, j), encoded[i])) //If there is an error find which bit it's at
					errorIndex -= (short) Math.pow(2, j);
			}
			if(errorIndex != 15) //If there was an error in the data
				encoded[i] += (encoded[i] & (short) Math.pow(2, errorIndex)) == (short) Math.pow(2, errorIndex) ? //Reverse the changed bit
						(short) -Math.pow(2, errorIndex) : (short) Math.pow(2, errorIndex);
						
			encoded[i] -= (encoded[i] & 0x0080) == 0 ? 0 : 0x0080; //Get rid of the 8th parity bit to make computing easier
			decoded[i] = (byte) (encoded[i] & 0x00FF); //Add first 7 data bits to the decoded byte
			decoded[i] += (encoded[i] & 0x0100) == 0x0100 ? 0x80 : 0; //Add the last data bit to the decoded byte
		}
		
		FileOutputStream fos;
		try {
			fos = new FileOutputStream(outFileName);
			fos.write(decoded); //Write the new decoded bytes to the output file
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static boolean errorCheck(int increment, int index, byte baseByte){
		if(parityCheck(increment, index, baseByte) == (baseByte & (byte)Math.pow(2, index)))// If the parity bit is what it should be
			return false; //No error
		return true; //Error
	}
	
	public static boolean errorCheck(int increment, int index, short baseShort){
		if(parityCheck(increment, index, baseShort) == (baseShort & (short) Math.pow(2, index)))// If the parity bit is what it should be
			return false; //No error
		return true; //Error
	}
	
	public static byte parityCheck(int increment, int index, byte baseByte){ //Check what a parity bit of a byte should be
		byte count = 0;
		int skipCount = increment+1; //skipCount keeps track of whether or not a bit should be check by a parity bit
		for(int i = index-1; i >= 0; i--){ //Start at the bit after the parity and loop through all bits
			if(skipCount >= increment*2) //If the subsequent bits have all been checked
				skipCount = 0;
			else if(skipCount >= increment) //If subsequent bits are still being checked
				count += (baseByte & (byte)Math.pow(2, i)) == Math.pow(2, i) ? 1 : 0; //Add 1 to count if the bit is 1
			skipCount++; //Move onto the next bit
		}
		return (byte) (count % 2 == 0 ? 0 : Math.pow(2, index)); //Return 0 for an even number of 1s and 1 for and odd number of 1s
	}	
	
	public static short parityCheck(int increment, int index, short baseShort){ //Check what a parity bit of a byte should be
		short count = 0;
		int skipCount = increment+1; //skipCount keeps track of whether or not a bit should be check by a parity bit
		for(int i = index-1; i >= 0; i--){ //Start at the bit after the parity and loop through all bits
			if(skipCount >= increment*2) //If the subsequent bits have all been checked
				skipCount = 0;
			else if(skipCount >= increment) //If subsequent bits are still being checked
				count += (baseShort & (short)Math.pow(2, i)) == Math.pow(2, i) ? 1 : 0; //Add 1 to count if the bit is 1
			skipCount++; //Move onto the next bit
		}
		return (short) (count % 2 == 0 ? 0 : Math.pow(2, index)); //Return 0 for an even number of 1s and 1 for and odd number of 1s
	}
}