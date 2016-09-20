package nablarch.dev

import org.junit.rules.ExternalResource

/**
 * TODO write document comment.
 *
 * @author T.Kawasaki
 * @since
 */
class SystemPropertyRule extends ExternalResource {

  private Properties original

  @Override
  protected void before() throws Throwable {
    original = new Properties()
    original.putAll(System.properties)
  }

  @Override
  protected void after() {
    System.properties = original
  }
}
