import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * This class provides static methods for performing normalization
 * 
 * @author Alex
 * @version 2022-11-11
 */
public class Normalizer {

  /**
   * Performs BCNF decomposition
   * 
   * @param rel   A relation (as an attribute set)
   * @param fdset A functional dependency set
   * @return a set of relations (as attribute sets) that are in BCNF
   */
  public static Set<Set<String>> BCNFDecompose(Set<String> rel, FDSet fdset) {
      // print out some helpful info for Professor Chiu!
      System.out.println("Current schema = " + rel.toString());

      // check to see if we have hit our base case (schema is already in bcnf)
      if (isBCNF(rel, fdset)){
          System.out.println("Current schema is in BCNF\n\n");
          return Collections.singleton(rel);
      }
    // Identify a nontrivial FD that violates BCNF. Split the relation's
    // attributes using that FD, as seen in class.

      // find all superkeys
      Set<Set<String>> superKeys = findSuperkeys(rel, fdset);
      System.out.println("Current schema's superkeys = " + superKeys);
      // find a fd that violates bcnf
      FD violatingFD = fdset
              .getSet()
              .stream()
              .filter(fd -> !fd.getLeft().containsAll(fd.getRight()) && !superKeys.contains(fd.getLeft()))
              .findFirst()
              //the following error should only throw if something has gone horribly wrong.
              .orElseThrow(() -> new RuntimeException("BCNF check gave false neg. No violating dependencies found."));

      System.out.println("\uD83E\uDE93".repeat(5) + " Splitting on " + violatingFD + "\uD83E\uDE93".repeat(5));

    // Redistribute the FDs in the closure of fdset to the two new
    // relations (r1 and r2)
      Set<String> r1 = rel
              .stream()
              .filter(s -> violatingFD.getLeft().contains(s) || violatingFD.getRight().contains(s))
              .collect(Collectors.toSet());

      Set<String> r2 = rel
              .stream()
              .filter(s -> violatingFD.getLeft().contains(s) || !violatingFD.getRight().contains(s))
              .collect(Collectors.toSet());


      // Iterate through closure of the given set of FDs, then union all attributes
      // appearing in the FD, and test if the union is a subset of the r1 (or
      // r2) relation. If so, then the FD gets added to the r1's (or r2's) FD
      // set. If the union is not a subset of either new relation, then the FD is
      // discarded
      FDSet fplus = FDUtil.fdSetClosure(fdset);
      FDSet f1 = new FDSet();
      FDSet f2 = new FDSet();
      for (FD fd : fplus) {
          // get all attributes in both sides of fd:
          Set<String> attrs = Stream.concat(fd.getLeft().stream(), fd.getRight().stream()).collect(Collectors.toSet());
          if (r1.containsAll(attrs)) {
              f1.add(fd);
          }
          if (r2.containsAll(attrs)) {
              f2.add(fd);
          }
      }
      System.out.println("Left schema = " + r1 + "\nLeft schema's superkeys = " + findSuperkeys(r1, f1));
      System.out.println("Right schema = " + r2 + "\nRight schema's superkeys = " + findSuperkeys(r2, f2) + "\n\n");
      // recurse on each subproblem:
      return Stream.concat(BCNFDecompose(r1, f1).stream(), BCNFDecompose(r2, f2).stream()).collect(Collectors.toSet());
  }

  /**
   * Tests whether the given relation is in BCNF. A relation is in BCNF iff the
   * left-hand attribute set of all nontrivial FDs is a super key.
   * 
   * @param rel   A relation (as an attribute set)
   * @param fdset A functional dependency set
   * @return true if the relation is in BCNF with respect to the specified FD set
   */
  public static boolean isBCNF(Set<String> rel, FDSet fdset) {
    Set<Set<String>> superKeys = findSuperkeys(rel, fdset);
    // Test the BCNF criteria against every FD in fdset:
    for (FD fd : fdset.getSet()) {
        if (!fd.getLeft().containsAll(fd.getRight()) && !superKeys.contains(fd.getLeft())) {
            return false;
        }
    }
    return true;
  }

  /**
   * This method returns a set of super keys
   * 
   * @param rel   A relation (as an attribute set)
   * @param fdset A functional dependency set
   * @return a set of super keys
   */
  public static Set<Set<String>> findSuperkeys(Set<String> rel, FDSet fdset) {
      // First check to make sure that all attributes contained in the FDs are valid attributions in the relation.
      // Get a list of all attributes in the FDSet:
      Set<String> attrs = fdset
              .getSet()
              .stream()
              .flatMap(fd -> Stream.concat(fd.getLeft().stream(), fd.getRight().stream()))
              .collect(Collectors.toSet());

      // throw an exception if the relation does not contain every attribute in the FDs:
      if (!rel.containsAll(attrs)) {
        throw new IllegalArgumentException("Attributes in FDSet not present in relation.");
      }

      // make an empty set to store every superkey that we find:
      Set<Set<String>> superKeys = new HashSet<>();
      // this map will hold the closure of each subset of rel under fdset
      Set<String> canDetermine = new HashSet<>();
      // for every subset of attributes in our relation

      for (Set<String> relSubSet : FDUtil.powerSet(rel)) {
          int startSize;
          // reset the canDetermine map
          canDetermine.clear();
          canDetermine.addAll(relSubSet);
          // do the a+ closure algorithm
          do {
              startSize = canDetermine.size();
              for (FD fd : fdset) {
                  if (canDetermine.containsAll(fd.getLeft())) {
                      canDetermine.addAll(fd.getRight());
                  }
              }
          } while (startSize != canDetermine.size());

        // add another superkey if relSubSet can determine all attributes in rel under fdset
        if (canDetermine.equals(rel)) {
          superKeys.add(relSubSet);
        }
      }
      return superKeys;
  }
}