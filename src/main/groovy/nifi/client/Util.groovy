package nifi.client

/**
 * Created by mburgess on 1/5/16.
 */
class Util {

    static String getSimpleName(String name) {
        name[(name.lastIndexOf('.')+1)..(-1)]
    }
}
