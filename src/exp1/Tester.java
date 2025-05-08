
package exp1;

import java.util.LinkedList;
import java.util.Queue;

public class Tester {

	public static void main(String args[]) {
		Queue<Integer> q = new LinkedList<Integer>();
		q.add(1);
		q.add(2);
		q.add(3);
		Object[] qArray = q.toArray();
		for(Object i : qArray) {
			System.out.println((Integer) i);
		}
	}
}
