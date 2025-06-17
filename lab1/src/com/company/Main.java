package com.company;

public class Main {

    static class BracketPair {
        char l, r;
        boolean sym;

        BracketPair(char l, char r) {
            this.l = l;
            this.r = r;
            this.sym = l == r;
        }
    }

    public static void main(String[] args) {
        List<BracketPair> pairs = Arrays.asList(
            new BracketPair('[', ']'),
            new BracketPair('{', '}'),
            new BracketPair('(', ')'),
            new BracketPair('|', '|')
        );

        String input = "[some(exe{1!|value|2?}jar)none]";
        System.out.println(check(input, pairs));
    }

    static String check(String s, List<BracketPair> p) {
        Stack<Character> st = new Stack<>();
        Stack<Integer> pos = new Stack<>();

        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            for (BracketPair b : p) {
                if (c == b.l) {
                    if (b.sym && !st.isEmpty() && st.peek() == c) {
                        st.pop(); pos.pop();
                    } else {
                        st.push(c); pos.push(i);
                    }
                    break;
                } else if (c == b.r) {
                    if (st.isEmpty()) return "Err at " + i;
                    char top = st.peek();
                    BracketPair match = null;
                    for (BracketPair bb : p)
                        if (bb.l == top) match = bb;
                    if (match != null && match.r == c) {
                        st.pop(); pos.pop();
                    } else return "Err at " + i;
                    break;
                }
            }
        }

        return st.isEmpty() ? "OK" : "Err at " + pos.peek();
    }
}
