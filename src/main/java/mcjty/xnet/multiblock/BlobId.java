package mcjty.xnet.multiblock;

/**
 * Every connected set of cables (so cables with the same color) will form a blob
 * in a chunk and has a local blob ID for that chunk.
 */
public record BlobId(int id) {
}
