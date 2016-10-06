//Author: Edan Meyer
public class Main {
    public static void main(String[] args) {
        Hamming.encode_7_4("wiki.txt", "enc_7_4.txt");
        Hamming.encode_15_11("wiki.txt", "enc_15_11.txt");
        Hamming.decode_7_4("enc_7_4.txt", "dec_7_4.txt");
        Hamming.decode_15_11("enc_15_11.txt", "dec_15_11.txt");
        Hamming.decode_7_4("wiki_7_4_flip.txt", "dec_7_4_flip.txt");
        Hamming.decode_15_11("wiki_15_11_flip.txt", "dec_15_11_flip.txt");
    }
}
