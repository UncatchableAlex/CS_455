import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.HashSet;

/**
 * This utility class is not meant to be instantitated, and just provides some
 * useful methods on FD sets.
 * 
 * @author Alex
 * @version 2022-11-11
 */
public final class FDUtil {

  /**
   * Resolves all trivial FDs in the given set of FDs
   * 
   * @param fdset (Immutable) FD Set
   * @return a set of trivial FDs with respect to the given FDSet
   */
  public static FDSet trivial(final FDSet fdset) {
    FDSet trivialFdSet = new FDSet();
    for (FD fd : fdset) {
      Set<String> leftDeps = fd.getLeft();
      for (Set<String> rightDep : powerSet(leftDeps)) {
        if (!rightDep.isEmpty()) {
          FD trivialFd = new FD(leftDeps, rightDep);
          trivialFdSet.add(trivialFd);
        }
      }
    }
    return trivialFdSet;
  }

  /**
   * Augments every FD in the given set of FDs with the given attributes
   * 
   * @param fdset FD Set (Immutable)
   * @param attrs a set of attributes with which to augment FDs (Immutable)
   * @return a set of augmented FDs
   */
  public static FDSet augment(final FDSet fdset, final Set<String> attrs) {
    FDSet augmentFdSet = new FDSet();
    for (FD fd : fdset) {
      List<String> fdLeftCopy = new ArrayList<>(fd.getLeft());
      List<String> fdRightCopy = new ArrayList<>(fd.getRight());
      fdLeftCopy.addAll(attrs);
      fdRightCopy.addAll(attrs);
      FD augFd = new FD(fdLeftCopy, fdRightCopy);
      augmentFdSet.add(augFd);
    }
    return augmentFdSet;
  }

  /**
   * Exhaustively resolves transitive FDs with respect to the given set of FDs
   * 
   * @param fdset (Immutable) FD Set
   * @return all transitive FDs with respect to the input FD set
   */
  public static FDSet transitive(final FDSet fdset) {
    FDSet fdSetCopy = new FDSet(fdset);
    FDSet transitiveSet = new FDSet(fdset);
    int startSize;
    do {
      startSize = transitiveSet.size();
      for (FD fd1 : fdSetCopy) {
        for (FD fd2 : fdSetCopy) {
          if (!fd1.equals(fd2) && fd1.getRight().equals(fd2.getLeft())) {
            FD transitiveFd = new FD(fd1.getLeft(), fd2.getRight());
            transitiveSet.add(transitiveFd);
          }
        }
      }
      fdSetCopy.addAll(transitiveSet);
    } while(transitiveSet.size() != startSize);
    fdSetCopy.getSet().removeAll(fdset.getSet());
    return fdSetCopy;
  }

  /**
   * Generates the closure of the given FD Set
   * 
   * @param fdset (Immutable) FD Set
   * @return the closure of the input FD Set
   */
  public static FDSet fdSetClosure(final FDSet fdset) {
    FDSet fdSetCopy = new FDSet(fdset);
    FDSet temp = new FDSet();
    Set<String> attributes = new HashSet<>();
    for (FD fd : fdSetCopy) {
      attributes.addAll(fd.getLeft());
      attributes.addAll(fd.getRight());
    }
    Set<Set<String>> attrPowerSet = powerSet(attributes);
    int startSize;
    do {
      startSize = fdSetCopy.size();
      fdSetCopy.addAll(trivial(fdSetCopy));
      for (Set<String> attr : attrPowerSet) {
        fdSetCopy.addAll(augment(fdSetCopy, attr));
      }
      fdSetCopy.addAll(transitive(fdSetCopy));
    } while (fdSetCopy.size() != startSize);

    return fdSetCopy;
  }

  /**
   * Generates the power set of the given set (that is, all subsets of
   * the given set of elements)
   * 
   * @param set Any set of elements (Immutable)
   * @return the power set of the input set
   */
  @SuppressWarnings("unchecked")
  public static <E> Set<Set<E>> powerSet(final Set<E> set) {

    // base case: power set of the empty set is the set containing the empty set
    if (set.size() == 0) {
      Set<Set<E>> basePset = new HashSet<>();
      basePset.add(new HashSet<>());
      return basePset;
    }

    // remove the first element from the current set
    E[] attrs = (E[]) set.toArray();
    set.remove(attrs[0]);

    // recurse and obtain the power set of the reduced set of elements
    Set<Set<E>> currentPset = FDUtil.powerSet(set);

    // restore the element from input set
    set.add(attrs[0]);

    // iterate through all elements of current power set and union with first
    // element
    Set<Set<E>> otherPset = new HashSet<>();
    for (Set<E> attrSet : currentPset) {
      Set<E> otherAttrSet = new HashSet<>(attrSet);
      otherAttrSet.add(attrs[0]);
      otherPset.add(otherAttrSet);
    }
    currentPset.addAll(otherPset);
    return currentPset;
  }
}