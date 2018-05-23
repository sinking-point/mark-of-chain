import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;

public class Markov {
	
	private static final int MAX_MESSAGE_WORDS = 1000;
	private List<String> words = new ArrayList<>();
	private int depth;
	private String guildID;
	File archive;
	
	public Markov(int depth, String guildID) {
		this.depth = depth;
		this.guildID = guildID;
		archive = new File(guildID + ".mark");
		load();
	}
	
	private void load() {
		if(archive.exists()) {
			try(ObjectInputStream in = new ObjectInputStream(new FileInputStream(archive))) {
				words = clean((List<String>) in.readObject());
//				System.out.println(clean(words));
			} catch (FileNotFoundException e) {
				// nonsense, we just checked
				e.printStackTrace();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
		}
	}
	
	public synchronized void listen(String text) {
		System.out.println(text);
		String[] wordsLearnt = text.replaceAll("(\\p{Punct})", " $1").split("\\s+");
		words.addAll(Arrays.asList(wordsLearnt));
		words.add("\n");
		words = clean(words);
		
		save();
	}
	
	private void save() {
		try(ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(archive))) {
			out.writeObject(words);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}
	
	private List<String> clean(List<String> dirty) {
		List<String> clean = new ArrayList<>();
		for(int i = 0; i < dirty.size(); i++) {
			if(dirty.get(i).equals("<")) {
				if(dirty.get(i + 2).equals(">")) {
					clean.add("<" + dirty.get(i + 1) + ">");
					i += 2;
				} else if(dirty.get(i + 3).equals(">")) {
					clean.add("<" + dirty.get(i + 1) + dirty.get(i + 2) + ">");
					i += 3;
				}
			} else {
				clean.add(dirty.get(i));
			}
		}
		return clean;
	}

	public synchronized String speak() {
		if(words.size() < depth) {
			return "";
		}
		
		
		List<String> message = new ArrayList<>(words.subList(words.size() - depth, words.size()));
		
		do {
			Map<String, Integer> possibilities = new HashMap<>();
			int total = 0;

			for(int i = 0; i < words.size() - depth; i++) {
				List<String> messageSect = message.subList(message.size() - depth, message.size());
				List<String> wordsSect = words.subList(i, i + depth);
				
				for(int j = 0; j < messageSect.size(); j++) {
					if(wordsSect.get(j).equals(messageSect.get(j))) {
						String contender = words.get(i + depth);
						possibilities.put(contender, possibilities.containsKey(contender) ? possibilities.get(contender) + j : j);
						total += j;
					}
				}
			}
			
			int monteCarlo = (int)(Math.random() * total);
			
			for(Entry<String, Integer> entry : possibilities.entrySet()) {
				if(monteCarlo < entry.getValue()) {
					message.add(entry.getKey());
					break;
				}
				
				monteCarlo -= entry.getValue();
			}
		} while(!message.get(message.size() - 1).equals("\n") && message.size() <= MAX_MESSAGE_WORDS + depth);
		
		String result = "";
		for(String word : message.subList(depth, message.size())) {
			if(!word.matches("\\p{Punct}")) {
				result += " ";
			}
			
			result += word;
		}
		
		return !result.matches("\\s*") ? result : speak();
	}
	
	public static void main(String[] args) {
		Markov mark = new Markov(3, "test");
		Scanner sc = new Scanner(System.in);

		
		for (;;) {
			String in = sc.nextLine();
			
			if(in.equals("!speak")) {
				System.out.println(mark.speak());
			} else {
				mark.listen(in);
			}
		}
	}

}
