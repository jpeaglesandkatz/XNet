package mcjty.rftoolsbase.api.control.parameters;

public record Tuple(int x, int y) {
    @Override
    public String toString() {
        return x + "," + y;
    }
}
