import java.io.*;
import java.util.*;

class Node implements Comparable<Node>{
	int ch;
	int freq;
	Node left;
	Node right;
	Node(int ch,int freq){
		this.ch = ch;
		this.freq = freq;
	}
	public int compareTo(Node ob){
		return this.freq - ob.freq;
	}
}
class CodesSave implements Serializable{
	HashMap<Integer,String>codes;
	int padding;
	CodesSave(HashMap<Integer,String>codes,int padding){
		this.codes = codes;
		this.padding = padding;
	}
}
class HuffmanCoding{

	static int CHARACTER_SET_SIZE = 100000;
	static HashMap<Integer,String> codes;
	static String compressedString;
	static String relativePath;
	private void saveCode(CodesSave ob){
		try{
			FileOutputStream fos = new FileOutputStream(relativePath+"\\key.ser");
			ObjectOutputStream oos = new ObjectOutputStream(fos);
			oos.writeObject(ob);
			oos.close();
			fos.close();
		}catch(IOException ee){
			System.out.println("Error: "+ee.getMessage());
		}
	}

	public static void main(String [] args)throws IOException,ClassNotFoundException{
		HuffmanCoding hc = new HuffmanCoding();
		Scanner sc = new Scanner(System.in);
		System.out.println("Enter your file complete path(eg. d:\\myfolder\\file.txt) : ");
		String filePath = sc.next();

		relativePath = filePath.substring(0,filePath.lastIndexOf("\\"));
		compressedString = hc.compress(filePath,relativePath+"\\compressed.bin");
		System.out.println("50% completed.");
		int dotIndex = filePath.lastIndexOf(".");
		String extension = filePath.substring(dotIndex);
		hc.decompress(relativePath+"\\key.ser",relativePath+"\\compressed.bin",extension);
		System.out.println("100% completed");
	}
	public String compress(String path, String compressedFilePath)throws IOException{
		HashMap<Integer,Integer>map = new HashMap<Integer,Integer>();
		FileInputStream fis = new FileInputStream(path);
		BufferedReader br;
		InputStreamReader isr;
		int ch1;
		String s="";
		while((ch1=fis.read())!=-1){
			//for(int ch:s.getBytes()){
				//System.out.print((char)ch1);
				if(map.containsKey(ch1))
					map.put(ch1,map.get(ch1)+1);
				else
					map.put(ch1,1);
			//}
			//System.out.println();
		}
		//br.close();
		//isr.close();
		fis.close();

		PriorityQueue<Node>pq = new PriorityQueue<Node>();
		for(int key:map.keySet()){
			pq.add(new Node(key,map.get(key)));
		}
		while(pq.size()>1){
			Node min1 = pq.poll();
			Node min2 = pq.poll();
			Node parent = new Node(257,min1.freq+min2.freq);
			parent.left = min1;
			parent.right = min2;
			pq.add(parent);
		}
		generateCodes(pq.poll());

		fis = new FileInputStream(path);
		isr = new InputStreamReader(fis);
		br = new BufferedReader(isr);

		s = "";
		StringBuffer ans = new StringBuffer();

		while((ch1=fis.read())!=-1){
			//for(int ch:s.getBytes()){
				ans.append(codes.get(ch1));
			//}
		}

		br.close();
		isr.close();
		fis.close();


		int count = 0;
		FileOutputStream fos = new FileOutputStream(compressedFilePath);
		for(int i=0;i<ans.length();i+=8){

			String binStr = ans.substring(i,i+Math.min(8,ans.length()-i));
			int n = binStr.length();
			if(n<8){
				for(int j=1;j<=8-n;j++){
					binStr = binStr + "0";
					count++;
				}
			}
			int byte1 = Integer.parseInt(binStr,2);
			fos.write(byte1);
		}
		fos.close();

		saveCode(new CodesSave(codes,count));

		return ans.toString();
	}
	private void generateCodes(Node head){
		codes = new HashMap<Integer,String>();
		generateCodes(head,codes,"");
	}
	private void generateCodes(Node head,HashMap<Integer,String>codes,String code){
		if(head==null){
			return;
		}
		if(head.left==null && head.right==null){
			codes.put(head.ch,code);
			return;
		}
		generateCodes(head.left,codes,code+"0");
		generateCodes(head.right,codes,code+"1");
	}
	public String decompress(String key_path,String compressedFilePath,String extension)throws IOException,ClassNotFoundException{
		FileInputStream fis = new FileInputStream(key_path);
		ObjectInputStream ois = new ObjectInputStream(fis);

		CodesSave ob =(CodesSave)ois.readObject();
		codes = ob.codes;

		HashMap<String,Integer> reverse_codes = new HashMap<String,Integer>();
		for(int key:codes.keySet()){
			reverse_codes.put(codes.get(key),key);
		}

		ois.close();
		fis.close();

		fis = new FileInputStream(compressedFilePath);

		int ch = 0;
		StringBuffer binStr = new StringBuffer();
		while((ch=fis.read())!=-1){
			String temp =  Integer.toBinaryString(ch);
			int tempLen = temp.length();
			if(tempLen<8){

				for(int i=1;i<=8-tempLen;i++){
					temp = "0"+temp;
				}
			}
			binStr.append(temp);
		}
		binStr.delete(binStr.length()-ob.padding,binStr.length());
		StringBuffer final_ans = new StringBuffer();
		String curr_str = "";
		FileOutputStream fos = new FileOutputStream(relativePath+"\\decompressed"+extension);
		for(int i=0;i<binStr.length();i++){
			curr_str+=binStr.charAt(i);
			if(reverse_codes.containsKey(curr_str)){
				int byte2 = reverse_codes.get(curr_str);
				//System.out.print((char)byte2);
				fos.write(byte2);
				final_ans.append(reverse_codes.get(curr_str));
				curr_str = "";
			}
		}
		fos.close();
		fis.close();
		return final_ans.toString();
	}
}
