import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * This class provides static methods for performing normalization
 * 
 * @author <YOUR NAME>
 * @version <DATE>
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
    // TODO - First test if the given relation is already in BCNF with respect to
    // the provided FD set.
      System.out.println("Current schema = " + rel.toString());
      if (isBCNF(rel, fdset)){
          System.out.println("Current schema is in BCNF\n\n");
          return Collections.singleton(rel);
      }


    // TODO - Identify a nontrivial FD that violates BCNF. Split the relation's
    // attributes using that FD, as seen in class.
      Set<Set<String>> superKeys = findSuperkeys(rel, fdset);
      System.out.println("Current schema's superkeys = " + superKeys);
      FD violatingFD = fdset
              .getSet()
              .stream()
              .filter(fd -> !fd.getLeft().containsAll(fd.getRight()) && !superKeys.contains(fd.getLeft()))
              .findFirst()
              .orElseThrow(() -> new RuntimeException("BCNF check gave false neg. No violating dependencies found."));

      System.out.println("Splitting on " + violatingFD);

    // TODO - Redistribute the FDs in the closure of fdset to the two new
    // relations (R_Left and R_Right) as follows:
    //
    // Iterate through closure of the given set of FDs, then union all attributes
    // appearing in the FD, and test if the union is a subset of the R_Left (or
    // R_Right) relation. If so, then the FD gets added to the R_Left's (or R_Right's) FD
    // set. If the union is not a subset of either new relation, then the FD is
    // discarded
      Set<String> r1 = rel
              .stream()
              .filter(s -> violatingFD.getLeft().contains(s) || violatingFD.getRight().contains(s))
              .collect(Collectors.toSet());

      Set<String> r2 = rel
              .stream()
              .filter(s -> violatingFD.getLeft().contains(s) || !violatingFD.getRight().contains(s))
              .collect(Collectors.toSet());

      FDSet fplus = FDUtil.fdSetClosure(fdset);
      FDSet f1 = new FDSet();
      FDSet f2 = new FDSet();
      for (FD fd : fplus) {
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
    // TODO - sanity check: are all the attributes in the FD set even in the relation? Throw an IllegalArgumentException if not.
      Set<String> attrs = fdset
              .getSet()
              .stream()
              .flatMap(fd -> Stream.concat(fd.getLeft().stream(), fd.getRight().stream()))
              .collect(Collectors.toSet());

      if (!rel.containsAll(attrs)) {
        throw new IllegalArgumentException("Attributes in FDSet not present in relation.");
      }
      FD[] lonelyAttrs = rel.stream()
              .filter(Predicate.not(attrs::contains))
              .map(Collections::singleton)
              .map(a -> new FD(a, a))
              .toList()
              .toArray(new FD[0]);
      FD[] fdArr = fdset.getSet().toArray(new FD[0]);
      FDSet fd_complete = new FDSet(fdArr);
      fd_complete.addAll(new FDSet(lonelyAttrs));
      Set<Set<String>> superKeys = new HashSet<>();
      FDSet fdClos = FDUtil.fdSetClosure(fd_complete);
      for (Set<String> relSubSet : FDUtil.powerSet(rel)) {
        boolean canDetermineAllAttrs = fdClos
                .getSet()
                .stream()
                .filter(fd -> relSubSet.containsAll(fd.getLeft()))
                .flatMap(fd -> Stream.concat(fd.getLeft().stream(), fd.getRight().stream()))
                .collect(Collectors.toSet())
                .equals(rel);

        if (canDetermineAllAttrs) {
          superKeys.add(relSubSet);
        }
      }
      return superKeys;


    // TODO - iterate through each subset of the relation's attributes, and test
    // the attribute closure of each subset
  }

}