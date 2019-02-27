import lombok.Getter;
import lombok.Setter;

public class OtherTest {

  @Getter
  @Setter
  static class A<T> {
    T name;
  };

  static class B<T> extends A<T> {} // subclass reuse same type 'Type'

  static class C<T, E extends T> extends A<T> {}

  public static void main(String[] args) {
    B<String> b = new B<>();
    b.setName("b");

    B<Integer> bInter = new B<>();
    bInter.setName(123);
  }
}
