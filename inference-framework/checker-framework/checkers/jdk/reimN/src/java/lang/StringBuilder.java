package java.lang;
import checkers.inference2.reimN.quals.*;

public final class StringBuilder
    extends AbstractStringBuilder
    implements java.io.Serializable, CharSequence
{

    static final long serialVersionUID = 4383685877147921099L;

    public StringBuilder() {
        throw new RuntimeException("skeleton method");
    }

    public StringBuilder(int capacity) {
        throw new RuntimeException("skeleton method");
    }

    public StringBuilder( String str) {
        throw new RuntimeException("skeleton method");
    }

    public StringBuilder(@ReadRead CharSequence seq) {
        throw new RuntimeException("skeleton method");
    }

    public StringBuilder append(@ReadRead Object obj) {
        throw new RuntimeException("skeleton method");
    }

    public StringBuilder append(String str) {
        throw new RuntimeException("skeleton method");
    }

    private StringBuilder append(StringBuilder sb) {
        throw new RuntimeException("skeleton method");
    }

    public StringBuilder append(@ReadRead StringBuffer sb) {
        throw new RuntimeException("skeleton method");
    }

    public StringBuilder append(@ReadRead CharSequence s) {
        throw new RuntimeException("skeleton method");
    }

    public StringBuilder append(@ReadRead CharSequence s, int start, int end) {
        throw new RuntimeException("skeleton method");
    }

    public StringBuilder append(char @ReadRead [] str) {
        throw new RuntimeException("skeleton method");
    }

    public StringBuilder append(char @ReadRead [] str, int offset, int len) {
        throw new RuntimeException("skeleton method");
    }

    public StringBuilder append(boolean b) {
        throw new RuntimeException("skeleton method");
    }

    public StringBuilder append(char c) {
        throw new RuntimeException("skeleton method");
    }

    public StringBuilder append(int i) {
        throw new RuntimeException("skeleton method");
    }

    public StringBuilder append(long lng) {
        throw new RuntimeException("skeleton method");
    }

    public StringBuilder append(float f) {
        throw new RuntimeException("skeleton method");
    }

    public StringBuilder append(double d) {
        throw new RuntimeException("skeleton method");
    }

    public StringBuilder appendCodePoint(int codePoint) {
        throw new RuntimeException("skeleton method");
    }

    public StringBuilder delete(int start, int end) {
        throw new RuntimeException("skeleton method");
    }

    public StringBuilder deleteCharAt(int index) {
        throw new RuntimeException("skeleton method");
    }

    public StringBuilder replace(int start, int end, String str) {
        throw new RuntimeException("skeleton method");
    }

    public StringBuilder insert(int index, char @ReadRead [] str, int offset, int len) {
        throw new RuntimeException("skeleton method");
    }

    public StringBuilder insert(int offset, @ReadRead Object obj) {
        throw new RuntimeException("skeleton method");
    }

    public StringBuilder insert(int offset, String str) {
        throw new RuntimeException("skeleton method");
    }

    public StringBuilder insert(int offset, char @ReadRead [] str) {
        throw new RuntimeException("skeleton method");
    }

    public StringBuilder insert(int dstOffset, @ReadRead CharSequence s) {
        throw new RuntimeException("skeleton method");
    }

    public StringBuilder insert(int dstOffset, @ReadRead CharSequence s, int start, int end) {
        throw new RuntimeException("skeleton method");
    }

    public StringBuilder insert(int offset, boolean b) {
        throw new RuntimeException("skeleton method");
    }

    public StringBuilder insert(int offset, char c) {
        throw new RuntimeException("skeleton method");
    }

    public StringBuilder insert(int offset, int i) {
        throw new RuntimeException("skeleton method");
    }

    public StringBuilder insert(int offset, long l) {
        throw new RuntimeException("skeleton method");
    }

    public StringBuilder insert(int offset, float f) {
        throw new RuntimeException("skeleton method");
    }

    public StringBuilder insert(int offset, double d) {
        throw new RuntimeException("skeleton method");
    }

    public int indexOf(@ReadRead StringBuilder this, String str)  {
        throw new RuntimeException("skeleton method");
    }

    public int indexOf(@ReadRead StringBuilder this, String str, int fromIndex)  {
        throw new RuntimeException("skeleton method");
    }

    public int lastIndexOf(@ReadRead StringBuilder this, String str)  {
        throw new RuntimeException("skeleton method");
    }

    public int lastIndexOf(@ReadRead StringBuilder this, String str, int fromIndex)  {
        throw new RuntimeException("skeleton method");
    }

    public StringBuilder reverse() {
        throw new RuntimeException("skeleton method");
    }

    public String toString(@ReadRead StringBuilder this)  {
        throw new RuntimeException("skeleton method");
    }

    private void writeObject(java.io.ObjectOutputStream s) throws java.io.IOException {
        throw new RuntimeException("skeleton method");
    }

    private void readObject(java.io.ObjectInputStream s) throws java.io.IOException, ClassNotFoundException {
        throw new RuntimeException("skeleton method");
    }
}
