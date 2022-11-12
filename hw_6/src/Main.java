import java.util.*;

public class Main {
    public static void main(String[] args) {
        Set<String> U = new HashSet<>(Arrays.asList("A", "B", "C", "D", "E"));  // Relation U(A,B,C,D,E)
        FD f1 = new FD(Arrays.asList("A", "E"), Arrays.asList("D")); // AE --> D
        FD f2 = new FD(Arrays.asList("A", "B"), Arrays.asList("C")); // AB --> C
        FD f3 = new FD(Arrays.asList("D"), Arrays.asList("B")); // D --> B
        FDSet fdsetU = new FDSet(f1, f2, f3);
        System.out.println("Final BCNF Schemas: " + Normalizer.BCNFDecompose(U, fdsetU) + "\n"+".".repeat(250)+"\n\n");

        Set<String> S = new HashSet<>(Arrays.asList("A", "B", "C", "D")); // Relation S(A,B,C,D)
        FD s1 = new FD(Arrays.asList("A"), Arrays.asList("B")); // A --> B
        FD s2 = new FD(Arrays.asList("B"), Arrays.asList("C")); // B --> C
        FDSet fdsetS = new FDSet(s1, s2);
        System.out.println("Final BCNF Schemas: " + Normalizer.BCNFDecompose(S, fdsetS) +"\n"+".".repeat(250) + "\n\n");
    }
}