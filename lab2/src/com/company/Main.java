package com.company;

public class Main {

    static class Polynomial implements Comparable<Polynomial>, Cloneable {
        Map<Integer, Double> terms = new HashMap<>();

        public Polynomial() {
        }

        public Polynomial(Map<Integer, Double> map) {
            for (Map.Entry<Integer, Double> entry : map.entrySet()) {
                if (entry.getValue() != 0) {
                    terms.put(entry.getKey(), entry.getValue());
                }
            }
        }

        public Polynomial add(Polynomial other) {
            Map<Integer, Double> result = new HashMap<>(terms);
            for (Map.Entry<Integer, Double> entry : other.terms.entrySet()) {
                int power = entry.getKey();
                double value = entry.getValue();
                result.put(power, result.getOrDefault(power, 0.0) + value);
            }
            return new Polynomial(result);
        }

        public Polynomial sub(Polynomial other) {
            Map<Integer, Double> result = new HashMap<>(terms);
            for (Map.Entry<Integer, Double> entry : other.terms.entrySet()) {
                int power = entry.getKey();
                double value = entry.getValue();
                result.put(power, result.getOrDefault(power, 0.0) - value);
            }
            return new Polynomial(result);
        }

        public Polynomial mul(Polynomial other) {
            Map<Integer, Double> result = new HashMap<>();
            for (Map.Entry<Integer, Double> a : terms.entrySet()) {
                for (Map.Entry<Integer, Double> b : other.terms.entrySet()) {
                    int newPower = a.getKey() + b.getKey();
                    double newValue = a.getValue() * b.getValue();
                    result.put(newPower, result.getOrDefault(newPower, 0.0) + newValue);
                }
            }
            return new Polynomial(result);
        }

        public Polynomial mul(double number) {
            Map<Integer, Double> result = new HashMap<>();
            for (Map.Entry<Integer, Double> entry : terms.entrySet()) {
                result.put(entry.getKey(), entry.getValue() * number);
            }
            return new Polynomial(result);
        }

        public Polynomial div(Polynomial divisor) {
            Polynomial result = new Polynomial();
            Polynomial remainder = this.clone();
            int degD = divisor.degree();
            double leadD = divisor.leading();
            while (remainder.degree() >= degD && remainder.degree() >= 0) {
                int degDiff = remainder.degree() - degD;
                double coef = remainder.leading() / leadD;
                Map<Integer, Double> term = new HashMap<>();
                term.put(degDiff, coef);
                Polynomial t = new Polynomial(term);
                result = result.add(t);
                remainder = remainder.sub(t.mul(divisor));
            }
            return result;
        }

        public Polynomial mod(Polynomial divisor) {
            Polynomial remainder = this.clone();
            int degD = divisor.degree();
            double leadD = divisor.leading();
            while (remainder.degree() >= degD && remainder.degree() >= 0) {
                int degDiff = remainder.degree() - degD;
                double coef = remainder.leading() / leadD;
                Map<Integer, Double> term = new HashMap<>();
                term.put(degDiff, coef);
                Polynomial t = new Polynomial(term);
                remainder = remainder.sub(t.mul(divisor));
            }
            return remainder;
        }

        public int degree() {
            if (terms.isEmpty()) return -1;
            return Collections.max(terms.keySet());
        }

        public double leading() {
            return terms.getOrDefault(degree(), 0.0);
        }

        @Override
        public int compareTo(Polynomial other) {
            return Integer.compare(this.degree(), other.degree());
        }

        @Override
        public Polynomial clone() {
            return new Polynomial(new HashMap<>(terms));
        }

        @Override
        public String toString() {
            if (terms.isEmpty()) return "0";
            List<Integer> powers = new ArrayList<>(terms.keySet());
            Collections.sort(powers, Collections.reverseOrder());
            StringBuilder sb = new StringBuilder();
            for (int p : powers) {
                double coef = terms.get(p);
                if (sb.length() > 0) {
                    sb.append(coef >= 0 ? " + " : " - ");
                } else if (coef < 0) {
                    sb.append("-");
                }
                double absCoef = Math.abs(coef);
                if (p == 0) {
                    sb.append(absCoef);
                } else if (p == 1) {
                    if (absCoef != 1) sb.append(absCoef);
                    sb.append("x");
                } else {
                    if (absCoef != 1) sb.append(absCoef);
                    sb.append("x^").append(p);
                }
            }
            return sb.toString();
        }
    }

    public static void main(String[] args) {
        Map<Integer, Double> mapA = new HashMap<>();
        mapA.put(2, 1.0);
        mapA.put(0, -3.0);
        Polynomial a = new Polynomial(mapA);

        Map<Integer, Double> mapB = new HashMap<>();
        mapB.put(1, 1.0);
        mapB.put(0, 1.0);
        Polynomial b = new Polynomial(mapB);

        System.out.println("A = " + a);
        System.out.println("B = " + b);
        System.out.println("A + B = " + a.add(b));
        System.out.println("A - B = " + a.sub(b));
        System.out.println("A * B = " + a.mul(b));
        System.out.println("A * 2 = " + a.mul(2));
        System.out.println("A / B = " + a.div(b));
        System.out.println("A % B = " + a.mod(b));
    }
}
