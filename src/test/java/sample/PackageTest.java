package sample;

import java.lang.invoke.MethodHandles;
import java.util.Arrays;
import org.junit.Test;

public class PackageTest {

  @Test
  public void getPackage() {
    Package[] packages = Package.getPackages();
    Arrays.stream(packages)
        .forEach(
            pck -> {
              System.out.println(pck.getName());
            });
    System.out.println("lookup=" + MethodHandles.lookup().lookupClass().getPackage().getName());
  }
}
