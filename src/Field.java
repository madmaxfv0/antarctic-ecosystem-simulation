import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

/**
 * Represent a rectangular grid of field positions.
 * Each position stores at most one {@link Animal} (which may
 * be a {@link Phytoplankton}).
 *
 * The field provides two kinds of "free" queries:
 *   - "truly free" (null cells only) — used by phytoplankton to spread
 *   - "free for animal" (null OR phytoplankton cells) — used by animals
 *     to move or give birth, since phytoplankton cells are walkable
 *
 * @author David J. Barnes, Michael Kölling, Jeffery Raphael & Fakhriyya Vazirova
 * @version 2025.02.23
 */
public class Field {

    private static final Random rand = Randomizer.getRandom();

    private int depth, width;
    private Animal[][] field;

    /**
     * Create a field of the given dimensions.
     * @param depth The depth of the field.
     * @param width The width of the field.
     */
    public Field(int depth, int width) {
        this.depth = depth;
        this.width = width;
        field = new Animal[depth][width];
    }

    /**
     * Empty every cell in the field.
     */
    public void clear() {
        for (int row = 0; row < depth; row++) {
            for (int col = 0; col < width; col++) {
                field[row][col] = null;
            }
        }
    }

    /**
     * Clear a single cell.
     * @param location The location to clear.
     */
    public void clear(Location location) {
        field[location.getRow()][location.getCol()] = null;
    }

    /**
     * Place an animal at (row, col).
     */
    public void place(Animal animal, int row, int col) {
        place(animal, new Location(row, col));
    }

    /**
     * Place an animal at a location.
     */
    public void place(Animal animal, Location location) {
        field[location.getRow()][location.getCol()] = animal;
    }

    /**
     * @return The animal at the given location, or null.
     */
    public Animal getObjectAt(Location location) {
        return getObjectAt(location.getRow(), location.getCol());
    }

    /**
     * @return The animal at (row, col), or null.
     */
    public Animal getObjectAt(int row, int col) {
        return field[row][col];
    }

    /**
     * @return A random adjacent location (shuffled list, first element).
     */
    public Location randomAdjacentLocation(Location location) {
        List<Location> adjacent = adjacentLocations(location);
        return adjacent.get(0);
    }

    /**
     * Return a shuffled list of locations adjacent to the given one.
     * The list will not include the location itself.
     *
     * @param location The centre location.
     * @return A shuffled list of valid adjacent locations.
     */
    public List<Location> adjacentLocations(Location location) {
        assert location != null : "Null location passed to adjacentLocations";

        List<Location> locations = new LinkedList<>();
        if (location != null) {
            int row = location.getRow();
            int col = location.getCol();
            for (int roffset = -1; roffset <= 1; roffset++) {
                int nextRow = row + roffset;
                if (nextRow >= 0 && nextRow < depth) {
                    for (int coffset = -1; coffset <= 1; coffset++) {
                        int nextCol = col + coffset;
                        if (nextCol >= 0 && nextCol < width
                                && (roffset != 0 || coffset != 0)) {
                            locations.add(new Location(nextRow, nextCol));
                        }
                    }
                }
            }
            Collections.shuffle(locations, rand);
        }
        return locations;
    }

    /**
     * Get a shuffled list of living, non-plant neighbours.
     * @param location The centre location.
     * @return A list of live animal neighbours (excluding phytoplankton).
     */
    public List<Animal> getLivingNeighbours(Location location) {
        assert location != null : "Null location passed";
        List<Animal> neighbours = new LinkedList<>();

        if (location != null) {
            List<Location> adjLocations = adjacentLocations(location);
            for (Location loc : adjLocations) {
                Animal animal = field[loc.getRow()][loc.getCol()];
                if (animal != null && animal.isAlive()
                        && !(animal instanceof Phytoplankton)) {
                    neighbours.add(animal);
                }
            }
            Collections.shuffle(neighbours, rand);
        }
        return neighbours;
    }

    /** @return The depth (number of rows). */
    public int getDepth() { return depth; }

    /** @return The width (number of columns). */
    public int getWidth() { return width; }

    // ---------------------------------------------------------------
    //  "Free for animal" methods  (null OR Phytoplankton cells)
    // ---------------------------------------------------------------

    /**
     * Get adjacent locations an animal can move into.
     * A cell is available if it is empty (null) or contains
     * phytoplankton.
     */
    public List<Location> getFreeAdjacentLocations(Location location) {
        List<Location> free = new LinkedList<>();
        List<Location> adjacent = adjacentLocations(location);
        for (Location next : adjacent) {
            Animal occupant = getObjectAt(next);
            if (occupant == null || occupant instanceof Phytoplankton) {
                free.add(next);
            }
        }
        return free;
    }

    /**
     * Try to find a single free-for-animal adjacent location.
     * @return A free adjacent location, or null if none.
     */
    public Location getFreeAdjacentLocation(Location location) {
        List<Location> free = getFreeAdjacentLocations(location);
        if (free.size() > 0) {
            return free.get(0);
        }
        return null;
    }

    // ---------------------------------------------------------------
    //  "Truly free" methods  (null cells only — used by phytoplankton)
    // ---------------------------------------------------------------

    /**
     * Get adjacent locations that are completely empty (null).
     * Used by phytoplankton to spread without overwriting other plants.
     */
    public List<Location> getTrulyFreeAdjacentLocations(Location location) {
        List<Location> free = new LinkedList<>();
        List<Location> adjacent = adjacentLocations(location);
        for (Location next : adjacent) {
            if (getObjectAt(next) == null) {
                free.add(next);
            }
        }
        return free;
    }
}
